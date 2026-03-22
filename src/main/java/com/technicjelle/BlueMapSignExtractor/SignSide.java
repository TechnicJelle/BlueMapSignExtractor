package com.technicjelle.BlueMapSignExtractor;

import de.bluecolored.bluemap.core.world.mca.blockentity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SignSide {
	private final List<SignLine> messages;
	private final boolean isWrittenOn;

	public SignSide() {
		this.messages = List.of();
		this.isWrittenOn = false;
	}

	public SignSide(@NotNull SignBlockEntity.TextData textData, int dataVersion) {
		this.messages = textData.getMessages().stream().map(m -> new SignLine(m, dataVersion)).toList();
		this.isWrittenOn = messages.stream().anyMatch(m -> m != null && !m.getPlainText().isBlank());
	}

	public boolean isWrittenOn() {
		return isWrittenOn;
	}

	public List<SignLine> getMessages() {
		return messages;
	}

	public @Nullable String getLabel() {
		for (SignLine message : messages) {
			if (message != null && !message.getPlainText().isBlank()) {
				return message.getPlainText();
			}
		}
		return null;
	}
}
