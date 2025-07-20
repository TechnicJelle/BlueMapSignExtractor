package com.technicjelle.BlueMapSignExtractor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class HTMLComponentSerializer {
	//TODO: Do not use a Plain Text Serializer in the HTML Serializer.
	//Do it custom, and properly support nesting.
	private final static PlainTextComponentSerializer PLAIN_TEXT_COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();

	private static final HTMLComponentSerializer INSTANCE = new HTMLComponentSerializer();

	public static HTMLComponentSerializer html() {
		return INSTANCE;
	}

	public String serialize(Component component) {
		//Get Text
		String text = PLAIN_TEXT_COMPONENT_SERIALIZER.serialize(component);

		//Styles
		final Style textStyle = component.style();
		final List<String> cssStyles = new ArrayList<>();

		//Get Colour
		TextColor componentColour = textStyle.color();
		if (componentColour != null) {
			cssStyles.add("color: " + componentColour.asHexString() + ";");
		}

		//Text Decorations
		if (textStyle.hasDecoration(TextDecoration.OBFUSCATED)) {
			//TODO: Handle this better
			text = "█".repeat(text.length()); //replace the text with blocks
			cssStyles.add("");
		} else if (textStyle.hasDecoration(TextDecoration.BOLD)) {
			cssStyles.add("font-weight: bold;");
		} else if (textStyle.hasDecoration(TextDecoration.STRIKETHROUGH) && textStyle.hasDecoration(TextDecoration.UNDERLINED)) {
			cssStyles.add("text-decoration: line-through underline;");
		} else if (textStyle.hasDecoration(TextDecoration.STRIKETHROUGH)) {
			cssStyles.add("text-decoration: line-through;");
		} else if (textStyle.hasDecoration(TextDecoration.UNDERLINED)) {
			cssStyles.add("text-decoration: underline;");
		} else if (textStyle.hasDecoration(TextDecoration.ITALIC)) {
			cssStyles.add("font-style: italic;");
		}

		//Generate HTML
		if (cssStyles.isEmpty()) {
			//There are no overrides. The styles are handled by the parent sign
			return "<span>" + text + "</span>";
		} else {
			//The styles for this specific line have been overridden
			return "<span style='" + String.join(" ", cssStyles) + "'>" + text + "</span>";
		}
	}
}
