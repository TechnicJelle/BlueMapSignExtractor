package com.technicjelle.bluemapsignextractor.versions.MC_1_20_4;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_20_4_Sign extends BlockEntity {
	private static class Side {
		@NBTName("messages")
		private String[] messages;

		@NBTName("color")
		private String colour;

		@NBTName("has_glowing_text")
		private boolean hasGlowingText;

		public String[] getMessages() {
			final String key = "\"text\"";
			JsonParser parser = new JsonParser();

			String[] cleanMessages = messages.clone();
			for (int i = 0; i < cleanMessages.length; i++) {
				JsonObject o = parser.parse("{" + key + ":" + cleanMessages[i] + "}").getAsJsonObject();
				cleanMessages[i] = o.get("text").getAsString();
			}

			return cleanMessages;
		}
	}

	@NBTName("back_text")
	private Side back;

	@NBTName("front_text")
	private Side front;

	@Override
	public String getAllSignMessages() {
		return String.join("\n", front.getMessages()) + "\n---\n" + String.join("\n", back.getMessages());
	}
}
