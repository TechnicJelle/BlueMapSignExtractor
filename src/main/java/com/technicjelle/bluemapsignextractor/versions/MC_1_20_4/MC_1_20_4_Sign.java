package com.technicjelle.bluemapsignextractor.versions.MC_1_20_4;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.HTMLUtils;
import com.technicjelle.bluemapsignextractor.common.SignColour;
import com.technicjelle.bluemapsignextractor.versions.MC_1_13_2.MC_1_13_2_Sign;
import de.bluecolored.bluenbt.NBTName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MC_1_20_4_Sign extends BlockEntity {
	private static class Side {
		@NBTName("messages")
		private String[] messages;

		@NBTName("color")
		private String colour;

		@NBTName("has_glowing_text")
		private boolean isGlowing;

		public static String unJSON(String text) {
			//Remove surrounding quotation marks and handle escaped characters
			try {
				final String key = "text";
				final JsonParser parser = new JsonParser();

				//TODO: This text might have extra JSON in it, like colour overrides.
				// Currently we just ignore everything but the text,
				// but perhaps we should parse it properly and return the coloured text.
				// Test `test_MC_1_20_4_SignWithCustomJSON` has an example of this.
				final JsonObject o = parser.parse("{\"" + key + "\":" + text + "}").getAsJsonObject();
				return o.get(key).getAsString();
			} catch (UnsupportedOperationException e) {
				//If the text could not be parsed by wrapping it in JSON, then it might BE a JSON already.
				//Which means it likely has colour codes or other formatting.
				//We can use 1.13's parser for that.
				//TODO: However, in some rare cases, the JSON might not even HAVE a "text" key, so we need to catch that.
				return MC_1_13_2_Sign.unJSON(text);
			}
		}

		public boolean isWrittenOn() {
			for (String message : messages) {
				if (!unJSON(message).isBlank()) {
					return true;
				}
			}
			return false;
		}

		public String getFormattedHTML() {
			final StringBuilder sb = new StringBuilder();
			for (String message : messages) {
				sb.append(HTMLUtils.formatSignLineToHTML(unJSON(message), SignColour.get(colour), isGlowing)).append("\n");
			}
			return sb.toString();
		}

		public @Nullable String getLabel() {
			for (String message : messages) {
				final String unJSON = unJSON(message);
				if (!unJSON.isBlank()) {
					return unJSON;
				}
			}

			return null;
		}
	}

	@NBTName("back_text")
	private Side back;

	@NBTName("front_text")
	private Side front;

	@Override
	public String getFormattedHTML() {
		final StringBuilder sb = new StringBuilder();
		if (front.isWrittenOn()) {
			sb.append(front.getFormattedHTML());
		}
		if (back.isWrittenOn()) {
			sb.append(back.getFormattedHTML());
		}
		return sb.toString().stripTrailing();
	}

	@Override
	public @NotNull String getLabel() {
		if (front.isWrittenOn()) {
			final String frontLabel = front.getLabel();
			if (frontLabel != null) return frontLabel;
		}
		if (back.isWrittenOn()) {
			final String backLabel = back.getLabel();
			if (backLabel != null) return backLabel;
		}
		return "Blank sign at " + getPosition().toString();
	}

	@Override
	public boolean isBlank() {
		return (!front.isWrittenOn() && !back.isWrittenOn());
	}
}
