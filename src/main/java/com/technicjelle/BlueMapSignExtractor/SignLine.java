package com.technicjelle.BlueMapSignExtractor;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.technicjelle.BlueMapSignExtractor.BlueMapSignExtractor.logger;

public class SignLine {
	private static final Gson GSON = new Gson();
	private static final GsonComponentSerializer GSON_COMPONENT_SERIALIZER = GsonComponentSerializer.gson();
	private static final PlainTextComponentSerializer PLAIN_TEXT_COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();
	private static final HTMLComponentSerializer HTML_COMPONENT_SERIALIZER = HTMLComponentSerializer.html();

	private final @NotNull String html;
	private final @NotNull String plainText;

	public SignLine(Object message, int dataVersion) {
		final @NotNull Component component;

		switch (message) {
			case String string:
				//https://minecraft.wiki/w/Data_version#List_of_data_versions
				if (dataVersion == -1) {
					logger.noFloodWarning("message was a String, but the world doesn't seem to be MCA, so the DataVersion was -1.\n\t" + string);
					component = Component.text(string); //We have no clue what it is, so we just let it be.
				} else if (dataVersion >= 4298) {
					// Text format changed from JSON to SNBT (Text Component) here: https://minecraft.wiki/w/Java_Edition_25w02a
					// But this line is apparently just a simple line. More complicated lines use the `Map<>` case below.
					component = Component.text(string);
				} else {
					// Sign format is JSON
					component = GSON_COMPONENT_SERIALIZER.deserialize(string);
				}
				break;
			case List<?> list:
				throw new SignException("message was a List: " + message.getClass().getName(), list);
			case Map<?, ?> map:
				// For some reason, there is a "" key sometimes, with the text of the sign in it.
				// Adventure cannot deal with that, so we do it manually.
				if (map.get("") instanceof String string) {
					component = Component.text(string);
					break;
				}

				String json = GSON.toJson(map);
				component = GSON_COMPONENT_SERIALIZER.deserialize(json);
				break;
			case null:
				throw new SignException("message was null!");
			default:
				throw new SignException("message was an unexpected type: " + message.getClass().getName(), message);
		}
		this.html = HTML_COMPONENT_SERIALIZER.serialize(component);
		this.plainText = PLAIN_TEXT_COMPONENT_SERIALIZER.serialize(component);
	}

	public @NotNull String getHtml() {
		return html;
	}

	public @NotNull String getPlainText() {
		return plainText;
	}
}
