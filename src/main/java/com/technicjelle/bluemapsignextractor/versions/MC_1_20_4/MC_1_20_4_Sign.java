package com.technicjelle.bluemapsignextractor.versions.MC_1_20_4;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.HTMLUtils;
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
			//TODO: This text might have extra JSON in it, like colour overrides. Currently we just ignore everything but the text, but perhaps we should parse it properly.
			// Test `test_MC_1_20_4_SignWithCustomJSON` has an example of this.
			try {
				final String key = "text";
				final JsonParser parser = new JsonParser();

				final JsonObject o = parser.parse("{\"" + key + "\":" + text + "}").getAsJsonObject();
				return o.get(key).getAsString();
			} catch (UnsupportedOperationException e) {
				System.err.println("Could not parse sign text in the expected 1.20 format, due to a GSON/JSON UnsupportedOperationException on Sign Text:\n" + text + "\n" + "Trying to parse as old format...");
				final String oldText = MC_1_13_2_Sign.unJSON(text);
				System.err.println("Successfully parsed as old format:\n" + oldText);
				return oldText;
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
				sb.append(HTMLUtils.formatSignLineToHTML(unJSON(message), colour, isGlowing)).append("\n");
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
}
