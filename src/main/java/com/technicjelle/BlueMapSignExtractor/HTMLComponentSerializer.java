package com.technicjelle.BlueMapSignExtractor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class HTMLComponentSerializer {
	private static final HTMLComponentSerializer INSTANCE = new HTMLComponentSerializer();

	public static HTMLComponentSerializer html() {
		return INSTANCE;
	}

	public String serialize(Component component) {
		StringBuilder sb = new StringBuilder();

		loop(sb, component);

		return sb.toString();
	}

	private void loop(StringBuilder sb, Component component) {
		String text;
		final List<String> cssClasses = new ArrayList<>();
		final List<String> cssStyles = new ArrayList<>();

		//Data Extraction
		{
			//Text
			{
				if (component instanceof TextComponent textComponent) {
					text = textComponent.content();
				} else {
					BlueMapSignExtractor.logger.logError("component was not a TextComponent!?");
					text = "";
				}
			}

			//Styles
			{
				final Style textStyle = component.style();

				//Colour
				TextColor componentColour = textStyle.color();
				if (componentColour != null) {
					cssStyles.add("color: " + componentColour.asHexString() + ";");
				}

				//Decorations
				if (textStyle.hasDecoration(TextDecoration.OBFUSCATED)) {
					cssClasses.add("obfuscated");
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
			}
		}

		sb.append("<span ");
		if (!cssClasses.isEmpty()) {
			sb.append("class='");
			sb.append(String.join(" ", cssClasses));
			sb.append("' ");
		}
		if (!cssStyles.isEmpty()) {
			sb.append("style='");
			sb.append(String.join(" ", cssStyles));
			sb.append("'");
		}
		sb.append(">");

		sb.append(text);

		for (Component child : component.children()) {
			loop(sb, child);
		}

		sb.append("</span>");
	}
}
