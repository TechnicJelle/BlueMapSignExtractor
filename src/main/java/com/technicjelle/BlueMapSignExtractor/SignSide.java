package com.technicjelle.BlueMapSignExtractor;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bluecolored.bluemap.core.world.mca.blockentity.SignBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SignSide {
	private static final String KEY_TEXT = "text";
	private static final Gson GSON = new Gson();

	@SuppressWarnings("unused")
	private static class SignTextLine {
		private @Nullable String text;
	}

	private final List<@Nullable String> messages;
	private final SignColour colour;
	private final boolean isGlowing;

	private final boolean isWrittenOn;

	public SignSide(@Nullable SignBlockEntity.TextData textData) {
		if (textData == null) {
			this.messages = List.of();
			this.colour = SignColour.BLACK;
			this.isGlowing = false;
		} else {
			//TODO: Some older versions save the messages as some kind of JSON, so those have to be unJSON'd
			// So I should figure out a proper way to do this.
			this.messages = textData.getMessages();
			this.colour = SignColour.get(textData.getColor());
			this.isGlowing = textData.isHasGlowingText();
		}
		this.isWrittenOn = setIsWrittenOn();
	}

	private boolean setIsWrittenOn() {
		for (@Nullable String message : messages) {
			if (message != null && !message.isBlank()) {
				return true;
			}
		}
		return false;
	}

	public boolean isWrittenOn() {
		return isWrittenOn;
	}

	public String getFormattedHTML() {
		final StringBuilder sb = new StringBuilder();
		for (@Nullable String message : messages) {
			sb.append(formatSignLineToHTML(message, colour, isGlowing)).append("\n");
		}
		return sb.toString();
	}

	public @Nullable String getLabel() {
		for (@Nullable String message : messages) {
			if (message != null && !message.isBlank()) {
				return message;
			}
		}
		return null;
	}

	private static @Nullable String unJSON(String text) {
		//Remove surrounding quotation marks and handle escaped characters
		try {
			//TODO: This text might have extra JSON in it, like colour overrides.
			// Currently we just ignore everything but the text,
			// but perhaps we should parse it properly and return the coloured text.
			// Test `test_MC_1_20_4_SignWithCustomJSON` has an example of this.
			final JsonObject o = JsonParser.parseString("{\"" + KEY_TEXT + "\":" + text + "}").getAsJsonObject();
			return o.get(KEY_TEXT).getAsString();
		} catch (UnsupportedOperationException e) {
			//If the text could not be parsed by wrapping it in JSON, then it might BE a JSON already.
			// Which means it likely has colour codes or other formatting.
			// In this case, we can try to parse it as a JSON object and extract the `text` key.
			final SignTextLine signTextLine = GSON.fromJson(text, SignTextLine.class);
			return signTextLine.text;
			//Can be null if there IS no `text` key in the JSON.
			// This can happen in the case of translated strings.
			// https://minecraft.wiki/w/Raw_JSON_text_format#Translated_Text
			// https://github.com/TechnicJelle/BlueMapSignExtractor/issues/64#issuecomment-2480620906
		}
	}

	private static String formatSignLineToHTML(String text, SignColour signColour, boolean isGlowing) {
		if (isGlowing) {
			return "<span class='glowing' style='color:" + signColour.glowCenter + ";--sign-glow-colour:" + signColour.glowOutline + ";'>" + text + "</span>";
		} else {
			return "<span style='color:" + signColour.standard + ";'>" + text + "</span>";
		}
	}
}
