/*******************************************************************************
 * Copyright (c) 2001, 2008 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/cpl-v10.html
 *
 * Contributors:
 *     Pavel Savara
 *     - Initial implementation
 *******************************************************************************/
package robocode.peer.proxies;


import robocode.exception.AbortedException;
import robocode.exception.DeathException;
import robocode.exception.DisabledException;
import robocode.exception.WinException;
import static robocode.io.Logger.logMessage;
import robocode.manager.HostManager;
import robocode.peer.RobotPeer;
import robocode.peer.RobotStatics;
import robocode.peer.robot.*;
import robocode.robotinterfaces.IBasicRobot;
import robocode.robotinterfaces.peer.IBasicRobotPeer;
import robocode.security.RobocodeClassLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


/**
 * @author Pavel Savara (original)
 */
public abstract class HostingRobotProxy implements IHostingRobotProxy {
	protected EventManager eventManager;
	protected RobotThreadManager robotThreadManager;
	protected RobotFileSystemManager robotFileSystemManager;
	protected RobotClassManager robotClassManager;
	protected RobotStatics statics;
	protected RobotOutputStream out;
	protected RobotPeer peer;
	protected HostManager hostManager;
	protected IBasicRobot robot;

	HostingRobotProxy(RobotClassManager robotClassManager, HostManager hostManager, RobotPeer peer, RobotStatics statics) {
		this.peer = peer;
		this.statics = statics;
		this.hostManager = hostManager;
		this.robotClassManager = robotClassManager;

		out = new RobotOutputStream();
		robotThreadManager = new RobotThreadManager(this);

		loadClassBattle();

		robotFileSystemManager = new RobotFileSystemManager(this, hostManager.getRobotFilesystemQuota(),
				robotClassManager.getRobotClassLoader().getClassDirectory(),
				robotClassManager.getRobotClassLoader().getRootPackageDirectory());
		robotFileSystemManager.initializeQuota();
	}

	public void cleanup() {
		// Clear all static field on the robot (at class level)
		cleanupStaticFields();

		robot = null;

		// Remove the file system and the manager
		robotFileSystemManager = null;
		if (out != null) {
			out.close();
			out = null;
		}

		if (robotThreadManager != null) {
			robotThreadManager.cleanup();
		}
		robotThreadManager = null;

		// Cleanup and remove class manager
		if (robotClassManager != null) {
			robotClassManager.cleanup();
			robotClassManager = null;
		}
	}

	private void cleanupStaticFields() {
		if (robot == null) {
			return;
		}

		Field[] fields = new Field[0];

		// This try-catch-throwable must be here, as it is not always possible to get the
		// declared fields without getting a Throwable like java.lang.NoClassDefFoundError.
		try {
			fields = robot.getClass().getDeclaredFields();
		} catch (Throwable t) {// Do nothing
		}

		for (Field f : fields) {
			int m = f.getModifiers();

			if (Modifier.isStatic(m) && !(Modifier.isFinal(m) || f.getType().isPrimitive())) {
				try {
					f.setAccessible(true);
					f.set(robot, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public RobotOutputStream getOut() {
		return out;
	}

	public void println(String s) {
		out.println(s);
	}

	public void print(Throwable ex) {
		out.print(ex);
	}

	public RobotStatics getStatics() {
		return statics;
	}

	public Class getRobotClass() {
		return robotClassManager.getRobotClass();
	}

	public RobotFileSystemManager getRobotFileSystemManager() {
		return robotFileSystemManager;
	}

	// -----------
	// battle driven methods
	// -----------

	public void startThread() {
		hostManager.getThreadManager().addThreadGroup(robotThreadManager.getThreadGroup(), this);
		robotThreadManager.start();
	}

	public void forceStopThread() {
		if (!robotThreadManager.forceStop()) {
			peer.getRobotStatistics().setInactive();
			peer.setRunning(false);
		}
	}

	public boolean waitForStopThread() {
		return robotThreadManager.waitForStop();
	}

	private void loadClassBattle() {
		try {
			Class<?> c;

			String className = robotClassManager.getFullClassName();
			RobocodeClassLoader classLoader = robotClassManager.getRobotClassLoader(); 

			// Pre-load robot classes without security...
			// loadClass WILL NOT LINK the class, so static "cheats" will not work.
			// in the safe robot loader the class is linked.
			if (RobotClassManager.isSecutityOn()) {
				c = classLoader.loadRobotClass(className, true);
			} else {
				c = classLoader.loadClass(className);
			}

			robotClassManager.setRobotClass(c);

		} catch (Throwable e) {
			println("SYSTEM: Could not load " + statics.getName() + " : " + e);
			print(e);
			drainEnergy();
		}
	}

	public boolean unsafeLoadRound() {
		robot = null;
		Class<?> robotClass;

		try {
			hostManager.getThreadManager().setLoadingRobot(this);
			robotClass = robotClassManager.getRobotClass();
			if (robotClass == null) {
				peer.println("SYSTEM: Skipping robot: " + statics.getName());
				return false;
			}
			robot = (IBasicRobot) robotClass.newInstance();
			robot.setOut(getOut());
			robot.setPeer((IBasicRobotPeer) this);
			eventManager.setRobot(robot);
		} catch (IllegalAccessException e) {
			peer.println("SYSTEM: Unable to instantiate this robot: " + e);
			peer.println("SYSTEM: Is your constructor marked public?");
			peer.print(e);
			robot = null;
			logMessage(e);
			return false;
		} catch (Throwable e) {
			peer.println("SYSTEM: An error occurred during initialization of " + statics.getName());
			peer.println("SYSTEM: " + e);
			peer.print(e);
			robot = null;
			logMessage(e);
			return false;
		} finally {
			hostManager.getThreadManager().setLoadingRobot(null);
		}
		return true;
	}

	// /


	protected abstract void executeImpl();

	public void run() {
		peer.setRunning(true);
		try {
			if (robot != null) {

				// Process all events for the first turn.
				// This is done as the first robot status event must occur before the robot
				// has started running.
				eventManager.processEvents();

				Runnable runnable = robot.getRobotRunnable();

				if (runnable != null) {
					runnable.run();
				}
			}

			// noinspection InfiniteLoopStatement
			for (;;) {
				executeImpl();
			}
		} catch (WinException e) {// Do nothing
		} catch (AbortedException e) {// Do nothing
		} catch (DeathException e) {
			peer.println("SYSTEM: " + statics.getName() + " has died");
		} catch (DisabledException e) {
			peer.drainEnergy();
			String msg = e.getMessage();

			if (msg == null) {
				msg = "";
			} else {
				msg = ": " + msg;
			}
			peer.println("SYSTEM: Robot disabled" + msg);
		} catch (Exception e) {
			peer.drainEnergy();
			final String message = statics.getName() + ": Exception: " + e;

			peer.print(e);
			logMessage(message);
		} catch (Throwable t) {
			peer.drainEnergy();
			if (!(t instanceof ThreadDeath)) {
				final String message = peer.getName() + ": Throwable: " + t;

				peer.print(t);
				logMessage(message);
			} else {
				logMessage(statics.getName() + " stopped successfully.");
			}
		} finally {
			waitForBattleEndImpl();
		}

		// If battle is waiting for us, well, all done!
		synchronized (this) {
			peer.setRunning(false);
			notifyAll();
		}
	}

	protected abstract void waitForBattleEndImpl();

	// TODO do something with that
	public void drainEnergy() {
		peer.drainEnergy();
	}
}