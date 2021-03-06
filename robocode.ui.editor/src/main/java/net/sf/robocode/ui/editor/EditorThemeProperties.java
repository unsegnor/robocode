/*******************************************************************************
 * Copyright (c) 2001-2013 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *******************************************************************************/
package net.sf.robocode.ui.editor;


import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


/**
 * Handles the editor theme properties.
 * 
 * @author Flemming N. Larsen (original)
 * 
 * @since 1.8.3.0
 */
public class EditorThemeProperties implements IEditorThemeProperties {

	private static final String DEFAULT_THEME_NAME = EditorPropertiesManager.getEditorProperties().getThemeName();

	private static final String DEFAULT_FONT_NAME = "Monospaced";
	private static final int DEFAULT_FONT_SIZE = 14;
	private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
	private static final Color DEFAULT_NORMAL_TEXT_COLOR = Color.BLACK;
	private static final FontStyle DEFAULT_NORMAL_TEXT_STYLE = FontStyle.PLAIN;
	private static final Color DEFAULT_COMMENT_TEXT_COLOR = new Color(0x00, 0xAF, 0x00);
	private static final FontStyle DEFAULT_COMMENT_TEXT_STYLE = FontStyle.PLAIN;
	private static final Color DEFAULT_QUOTED_TEXT_COLOR = new Color(0x7f, 0x00, 0x00);
	private static final FontStyle DEFAULT_QUOTED_TEXT_STYLE = FontStyle.PLAIN;
	private static final Color DEFAULT_KEYWORD_TEXT_COLOR = new Color(0x00, 0x00, 0xAF);
	private static final FontStyle DEFAULT_KEYWORD_TEXT_STYLE = FontStyle.BOLD;
	private static final Color DEFAULT_LITERAL_TEXT_COLOR = new Color(0x00, 0x00, 0xAF);
	private static final FontStyle DEFAULT_LITERAL_TEXT_STYLE = FontStyle.BOLD;
	private static final Color DEFAULT_ANNOTATION_TEXT_COLOR = new Color(0x7F, 0x7F, 0x7F);
	private static final FontStyle DEFAULT_ANNOTATION_TEXT_STYLE = FontStyle.BOLD;

	private static final String THEME_NAME = "editor.theme";

	private static final String FONT_NAME = "editor.font.name";
	private static final String FONT_SIZE = "editor.font.size";

	private static final String BACKGROUND_COLOR = "editor.background.color";

	private static final String NORMAL_TEXT_COLOR = "editor.text.color.normal";
	private static final String NORMAL_TEXT_STYLE = "editor.text.style.normal";

	private static final String COMMENT_TEXT_COLOR = "editor.text.color.comment";
	private static final String COMMENT_TEXT_STYLE = "editor.text.style.comment";

	private static final String QUOTED_TEXT_COLOR = "editor.text.color.quoted";
	private static final String QUOTED_TEXT_STYLE = "editor.text.style.quoted";

	private static final String KEYWORD_TEXT_COLOR = "editor.text.color.keyword";
	private static final String KEYWORD_TEXT_STYLE = "editor.text.style.keyword";

	private static final String LITERAL_TEXT_COLOR = "editor.text.color.literal";
	private static final String LITERAL_TEXT_STYLE = "editor.text.style.literal";

	private static final String ANNOTATION_TEXT_COLOR = "editor.text.color.annotation";
	private static final String ANNOTATION_TEXT_STYLE = "editor.text.style.annotation";

	private String themeName;

	private String fontName;
	private Integer fontSize;

	private ColorPropertyStrategy backgroundColor = new ColorPropertyStrategy(BACKGROUND_COLOR, DEFAULT_BACKGROUND_COLOR);
	private ColorPropertyStrategy normalTextColor = new ColorPropertyStrategy(NORMAL_TEXT_COLOR,
			DEFAULT_NORMAL_TEXT_COLOR);
	private FontStylePropertyStrategy normalTextStyle = new FontStylePropertyStrategy(NORMAL_TEXT_STYLE,
			DEFAULT_NORMAL_TEXT_STYLE);
	private ColorPropertyStrategy commentTextColor = new ColorPropertyStrategy(COMMENT_TEXT_COLOR,
			DEFAULT_COMMENT_TEXT_COLOR);
	private FontStylePropertyStrategy commentTextStyle = new FontStylePropertyStrategy(COMMENT_TEXT_STYLE,
			DEFAULT_COMMENT_TEXT_STYLE);
	private ColorPropertyStrategy quotedTextColor = new ColorPropertyStrategy(QUOTED_TEXT_COLOR,
			DEFAULT_QUOTED_TEXT_COLOR);
	private FontStylePropertyStrategy quotedTextStyle = new FontStylePropertyStrategy(QUOTED_TEXT_STYLE,
			DEFAULT_QUOTED_TEXT_STYLE);
	private ColorPropertyStrategy keywordTextColor = new ColorPropertyStrategy(KEYWORD_TEXT_COLOR,
			DEFAULT_KEYWORD_TEXT_COLOR);
	private FontStylePropertyStrategy keywordTextStyle = new FontStylePropertyStrategy(KEYWORD_TEXT_STYLE,
			DEFAULT_KEYWORD_TEXT_STYLE);
	private ColorPropertyStrategy literalTextColor = new ColorPropertyStrategy(LITERAL_TEXT_COLOR,
			DEFAULT_LITERAL_TEXT_COLOR);
	private FontStylePropertyStrategy literalTextStyle = new FontStylePropertyStrategy(LITERAL_TEXT_STYLE,
			DEFAULT_LITERAL_TEXT_STYLE);
	private ColorPropertyStrategy annotationTextColor = new ColorPropertyStrategy(ANNOTATION_TEXT_COLOR,
			DEFAULT_ANNOTATION_TEXT_COLOR);
	private FontStylePropertyStrategy annotationTextStyle = new FontStylePropertyStrategy(ANNOTATION_TEXT_STYLE,
			DEFAULT_ANNOTATION_TEXT_STYLE);
	
	private IPropertyStrategy<?>[] colorAndStyleProps = new IPropertyStrategy[] {
		backgroundColor, normalTextColor, normalTextStyle, commentTextColor, commentTextStyle, quotedTextColor,
		quotedTextStyle, keywordTextColor, keywordTextStyle, literalTextColor, literalTextStyle, annotationTextColor,
		annotationTextStyle
	};
	
	private final Properties props = new Properties();

	public String getThemeName() {
		String themeName = this.themeName;
		if (themeName == null) {
			themeName = props.getProperty(THEME_NAME);
		}
		if (themeName == null) {
			themeName = DEFAULT_THEME_NAME;
		}
		this.themeName = themeName;
		return themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
		notifyChange();
	}

	public Font getFont() {
		return new Font(getFontName(), getNormalTextStyle().getFontStyleFlags(), getFontSize());
	}
	
	public void setFontName(String fontName) {
		this.fontName = fontName;
		notifyChange();
	}

	@Override
	public String getFontName() {
		String fontName = this.fontName;
		if (fontName == null) {
			fontName = props.getProperty(FONT_NAME);
		}
		if (fontName == null) {
			fontName = DEFAULT_FONT_NAME;
		}
		this.fontName = fontName;
		return fontName;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
		notifyChange();
	}

	@Override
	public int getFontSize() {
		Integer fontSize = this.fontSize;
		if (fontSize == null) {
			String value = props.getProperty(FONT_SIZE);
			try {
				fontSize = Integer.parseInt(value);
			} catch (NumberFormatException ignore) {}
		}
		if (fontSize == null) {
			fontSize = DEFAULT_FONT_SIZE;
		}
		this.fontSize = fontSize;
		return fontSize;
	}

	public void setBackgroundColor(Color textColor) {
		backgroundColor.set(textColor);
	}

	@Override
	public Color getBackgroundColor() {
		return backgroundColor.get();
	}

	public void setNormalTextColor(Color textColor) {
		normalTextColor.set(textColor);
	}

	@Override
	public Color getNormalTextColor() {
		return normalTextColor.get();
	}
	
	public void setNormalTextStyle(FontStyle fontStyle) {
		normalTextStyle.set(fontStyle);
	}

	@Override
	public FontStyle getNormalTextStyle() {
		return normalTextStyle.get();
	}

	public void setCommentTextColor(Color textColor) {
		commentTextColor.set(textColor);
	}

	@Override
	public Color getCommentTextColor() {
		return commentTextColor.get();
	}
	
	public void setCommentTextStyle(FontStyle fontStyle) {
		commentTextStyle.set(fontStyle);
	}

	@Override
	public FontStyle getCommentTextStyle() {
		return commentTextStyle.get();
	}

	public void setQuotedTextColor(Color textColor) {
		quotedTextColor.set(textColor);
	}

	@Override
	public Color getQuotedTextColor() {
		return quotedTextColor.get();
	}
	
	public void setQuotedTextStyle(FontStyle fontStyle) {
		quotedTextStyle.set(fontStyle);
	}

	@Override
	public FontStyle getQuotedTextStyle() {
		return quotedTextStyle.get();
	}

	public void setKeywordTextColor(Color textColor) {
		keywordTextColor.set(textColor);
	}

	@Override
	public Color getKeywordTextColor() {
		return keywordTextColor.get();
	}
	
	public void setKeywordTextStyle(FontStyle fontStyle) {
		keywordTextStyle.set(fontStyle);
	}

	@Override
	public FontStyle getKeywordTextStyle() {
		return keywordTextStyle.get();
	}

	public void setLiteralTextColor(Color textColor) {
		literalTextColor.set(textColor);
	}

	@Override
	public Color getLiteralTextColor() {
		return literalTextColor.get();
	}
	
	public void setLiteralTextStyle(FontStyle fontStyle) {
		literalTextStyle.set(fontStyle);
	}

	@Override
	public FontStyle getLiteralTextStyle() {
		return literalTextStyle.get();
	}

	public void setAnnotationTextColor(Color textColor) {
		annotationTextColor.set(textColor);
	}

	@Override
	public Color getAnnotationTextColor() {
		return annotationTextColor.get();
	}
	
	public void setAnnotationTextStyle(FontStyle fontStyle) {
		annotationTextStyle.set(fontStyle);
	}

	@Override
	public FontStyle getAnnotationTextStyle() {
		return annotationTextStyle.get();
	}

	public void load(InputStream is) throws IOException {
		props.load(is);

		themeName = props.getProperty(THEME_NAME, DEFAULT_THEME_NAME);

		fontName = props.getProperty(FONT_NAME, DEFAULT_FONT_NAME);

		try {
			fontSize = Integer.parseInt(props.getProperty(FONT_SIZE, "" + DEFAULT_FONT_SIZE));
		} catch (NumberFormatException e) {
			fontSize = DEFAULT_FONT_SIZE;
		}

		for (IPropertyStrategy<?> propertyStrategy : colorAndStyleProps) {
			propertyStrategy.load();
		}
	}

	public void store(OutputStream os, String header) throws IOException {
		props.setProperty(THEME_NAME, themeName);
		props.setProperty(FONT_NAME, fontName);
		props.setProperty(FONT_SIZE, "" + fontSize);

		for (IPropertyStrategy<?> propertyStrategy : colorAndStyleProps) {
			propertyStrategy.save();
		}

		props.store(os, header);
	}
	
	public void notifyChange() {
		EditorThemePropertiesManager.notifyChange(this);
	}

	interface IPropertyStrategy<V> {
		V get();
		void set(V value);
		void load();
		void save();
	}


	class ColorPropertyStrategy implements IPropertyStrategy<Color> {

		final String propertyName;
		final Color defaultColor;
		Color color;

		ColorPropertyStrategy(String propertyName, Color defaultColor) {
			this.propertyName = propertyName;
			this.defaultColor = defaultColor;
			this.color = defaultColor;
		}

		@Override
		public Color get() {
			return color;
		}

		@Override
		public void set(Color color) {
			this.color = color;
			notifyChange();
		}

		@Override
		public void load() {
			Color color = fromPropertyToColor(propertyName);
			this.color = (color == null) ? defaultColor : color;
		}

		@Override
		public void save() {
			props.setProperty(propertyName, toHexString(color));
		}

		private Color fromPropertyToColor(String propertyName) {
			String hexValue = props.getProperty(propertyName);
			if (hexValue != null) {
				try {
					// Yes, we need to use a Long instead of an Integer here +
					// we mask out the alpha channel
					int rgb = (int) Long.parseLong(hexValue, 16) & 0x00FFFFFF;
					return new Color(rgb);
				} catch (NumberFormatException ignore) {}
			}
			return null;
		}
	}


	class FontStylePropertyStrategy implements IPropertyStrategy<FontStyle> {

		final String propertyName;
		final FontStyle defaultStyle;
		FontStyle style;

		FontStylePropertyStrategy(String propertyName, FontStyle defaultStyle) {
			this.propertyName = propertyName;
			this.defaultStyle = defaultStyle;
			this.style = defaultStyle;
		}

		@Override
		public FontStyle get() {
			return style;
		}
		
		@Override
		public void set(FontStyle style) {
			this.style = style;
			notifyChange();
		}

		@Override
		public void load() {
			FontStyle style = fromPropertyToFontStyle(propertyName);
			this.style = (style == null) ? defaultStyle : style;
		}

		@Override
		public void save() {
			props.setProperty(propertyName, "" + style.getName());
		}

		private FontStyle fromPropertyToFontStyle(String propertyName) {
			String styleName = props.getProperty(propertyName);
			return (styleName == null) ? null : FontStyle.fromName(styleName);
		}
	}	
	
	private static String toHexString(Color color) {
		// We mask out the alpha channel and format as RRGGBB
		return String.format("%06X", color.getRGB() & 0x00FFFFFF);
	}
}
