package com.technicjelle.BlueMapSignExtractor;

import de.bluecolored.bluemap.core.world.mca.blockentity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class SignSide {
	private final List<SignLine> messages;
	private final @NotNull SignSideColour signColour;
	private final boolean isGlowing;

	private final boolean isWrittenOn;

	public SignSide() {
		this.messages = List.of();
		this.signColour = SignSideColour.BLACK;
		this.isGlowing = false;
		this.isWrittenOn = false;
	}

	public SignSide(@NotNull SignBlockEntity.TextData textData, int dataVersion) {
		BlueMapSignExtractor.logger.logInfo("----------------------------------------------------------------------------------------------------");
		this.messages = textData.getMessages().stream().map(m -> new SignLine(m, dataVersion)).toList();
		this.signColour = Objects.requireNonNullElse(SignSideColour.get(textData.getColor()), SignSideColour.BLACK);
		this.isGlowing = textData.isHasGlowingText();
		this.isWrittenOn = setIsWrittenOn();
	}

	private boolean setIsWrittenOn() {
		for (SignLine message : messages) {
			if (message != null && !message.getPlainText().isBlank()) {
				return true;
			}
		}
		return false;
	}

	public boolean isWrittenOn() {
		return isWrittenOn;
	}

	public @Nullable String getLabel() {
		for (SignLine message : messages) {
			if (message != null && !message.getPlainText().isBlank()) {
				return message.getPlainText();
			}
		}
		return null;
	}

	public String getFormattedHTML() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div ").append(signColour.getHTMLAttributes(isGlowing, "sign-side")).append(">");
		for (SignLine message : messages) {
			sb.append(message.getHtml()).append("\n");
		}
		sb.append("</div>");
		return sb.toString();
	}
}
