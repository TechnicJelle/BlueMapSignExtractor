package com.technicjelle.BlueMapSignExtractor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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
		final String text = PLAIN_TEXT_COMPONENT_SERIALIZER.serialize(component);

		//Get Colour
		TextColor componentColour = component.color();
		final String colour;
		if (componentColour != null) {
			colour = componentColour.asHexString();
		} else {
			colour = null;
		}

		//Generate HTML
		if (colour != null) {
			//The colour for this specific line has been overridden
			return "<span style='color:" + colour + ";'>" + text + "</span>";
		} else {
			//There are no overrides, so glowing here is not necessary. It is handled by the parent sign
			return "<span>" + text + "</span>";
		}
	}
}
