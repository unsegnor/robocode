/*******************************************************************************
 * Copyright (c) 2001-2013 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *******************************************************************************/
package net.sf.robocode.ui.editor;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;


/**
 * Editor panel containing editor pane in a scroll pane, a line number area, and a statusTextField text field.
 *
 * @author Flemming N. Larsen (original)
 */
@SuppressWarnings("serial")
public class EditorPanel extends JPanel {

	private JTextField statusTextField;
	private final JScrollPane scrollPane;
	private final EditorPane editorPane;
	private final LineNumberArea lineNumberArea;

	public EditorPanel() {
		super();

		setLayout(new BorderLayout());

		statusTextField = new JTextField();
		statusTextField.setEditable(false);

		scrollPane = new JScrollPane();

		editorPane = new EditorPane(scrollPane.getViewport());
		editorPane.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateStatus(getRow(e.getDot(), editorPane), getColumn(e.getDot(), editorPane));
			}
		});

		scrollPane.setViewportView(editorPane);

		EditorThemeProperties themeProps = EditorThemePropertiesManager.getEditorThemeProperties();
		setBackgroundColor(themeProps);

		lineNumberArea = new LineNumberArea(editorPane);
		scrollPane.setRowHeaderView(lineNumberArea);

		add(scrollPane, BorderLayout.CENTER);
		add(statusTextField, BorderLayout.SOUTH);

		updateStatus(1, 1);

		EditorThemePropertiesManager.addListener(new IEditorPropertyChangeListener() {
			@Override
			public void onChange(IEditorThemeProperties properties) {
				setBackgroundColor(properties);
			}
		});
	}
	
	@Override
	public void requestFocus() {
		super.requestFocus();
		if (editorPane != null) {
			editorPane.requestFocus();
			editorPane.requestFocusInWindow();
		}
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (editorPane != null) {
			editorPane.setFont(font);

			Border border = BorderFactory.createEmptyBorder(3, 3, 3, 3);
			editorPane.setBorder(border);
		}
		if (lineNumberArea != null) {
			lineNumberArea.setFont(font);

			FontMetrics fm = getFontMetrics(font);
			int delta = fm.getHeight() - fm.getDescent() - fm.getAscent();
			Border border = BorderFactory.createEmptyBorder(delta + 3, 3, 3, 3);
			lineNumberArea.setBorder(border);
		}
	}

	public EditorPane getEditorPane() {
		return editorPane;
	}
	
	private void updateStatus(int linenumber, int columnnumber) {
		statusTextField.setText("Line: " + linenumber + " Column: " + columnnumber);
	}

	private static int getRow(int pos, JTextComponent editor) {
		int rn = (pos == 0) ? 1 : 0;

		try {
			int offs = pos;

			while (offs > 0) {
				offs = Utilities.getRowStart(editor, offs) - 1;
				rn++;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return rn;
	}

	private static int getColumn(int pos, JTextComponent editor) {
		try {
			return pos - Utilities.getRowStart(editor, pos) + 1;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private void setBackgroundColor(IEditorThemeProperties properties) {
		Color bgColor = properties.getBackgroundColor();
		scrollPane.getViewport().setBackground(bgColor);
		editorPane.setBackground(bgColor);
	}
}
