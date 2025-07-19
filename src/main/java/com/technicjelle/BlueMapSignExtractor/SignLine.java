package com.technicjelle.BlueMapSignExtractor;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class SignLine {
	final String text;
	final String lineColourOverride;

	public SignLine(Object message, int dataVersion) {
		switch (message) {
			case String string:
				//https://minecraft.wiki/w/Data_version#List_of_data_versions
				if (dataVersion == -1) {
					BlueMapSignExtractor.logger.logInfo("String, Not MCA, DataVersion == -1: " + string);
					this.text = string; //We have no clue what it is, so we just let it be.
					this.lineColourOverride = null;
				} else if (dataVersion >= 4298) {
					// Text format changed from JSON (Only Quoted) to SNBT (Text Component) here: https://minecraft.wiki/w/Java_Edition_25w02a
					BlueMapSignExtractor.logger.logInfo("String, Text Component Format: " + string);
					this.text = string;
					this.lineColourOverride = null; //Colour overrides are possible in this version, but will use the `Map<>` case below, instead of here.
				} else if (dataVersion >= 3442) {
					// Sign format changed from JSON (Full) to JSON (Only Quoted) here: https://minecraft.wiki/w/Java_Edition_23w12a
					BlueMapSignExtractor.logger.logInfo("String, Quoted Format: " + string);
					this.text = unQuote(string);
					this.lineColourOverride = null; //TODO: Research how this version does colour overrides
				} else {
					// Sign format is JSON (Full)
					BlueMapSignExtractor.logger.logInfo("String, JSON Format: " + string);
					SignTextLine line = unJSON(string);
					this.text = line.text;
					this.lineColourOverride = line.color;
				}
				break;
			case List<?> list:
				BlueMapSignExtractor.logger.logInfo("List: " + list);
				this.text = list.toString();
				this.lineColourOverride = null;
				break;
			case Map<?, ?> map:
				//Text
				BlueMapSignExtractor.logger.logInfo("Map: " + map);
				@Nullable Object text = map.get("text");
				if (text instanceof String textString) {
					this.text = textString;
				} else {
					BlueMapSignExtractor.logger.logWarning("text was not a String!?");
					this.text = "";
				}
				//Colour
				@Nullable Object colour = map.get("color");
				if (colour instanceof String colourString) {
					this.lineColourOverride = colourString;
				} else {
					BlueMapSignExtractor.logger.logWarning("color was not a String!?");
					this.lineColourOverride = null;
				}
				break;
			case null:
				BlueMapSignExtractor.logger.logWarning("text was null!?");
				this.text = "";
				this.lineColourOverride = null;
				break;
			default:
				BlueMapSignExtractor.logger.logWarning("Unknown: " + message);
				this.text = message.toString(); //We have no clue what it is, so we just let it be.
				this.lineColourOverride = null;
				break;
		}
	}

	public String formatSignLineToHTML(boolean signIsGlowing) {
		if (lineColourOverride != null) {
			//The colour for this specific line has been overridden
			@Nullable TextColour textColour = TextColour.get(lineColourOverride);
			if (textColour != null) {
				return "<span " + textColour.getHTMLAttributes(signIsGlowing, "") + ">" + text + "</span>";
			} else {
				//if the enum doesn't have it, it was probably a hex colour, so we can pass it right on through to the css
				return "<span style='color:" + lineColourOverride + ";'>" + text + "</span>";
			}
		} else {
			//There are no overrides, so glowing here is not necessary. It is handled by the parent sign
			return "<span>" + text + "</span>";
		}
	}

	private static final String KEY_TEXT = "text";
	private static final Gson GSON = new Gson();

	@SuppressWarnings("unused") //Ignore that there is no setter, because it's set through GSON
	private static class SignTextLine {
		private @Nullable String text;
		private @Nullable String color;
	}

	private static String unQuote(String text) {
		return unJSON("{\"" + KEY_TEXT + "\":" + text + "}").text;
	}

	private static SignTextLine unJSON(String json) {
		return GSON.fromJson(json, SignTextLine.class);
	}
}
