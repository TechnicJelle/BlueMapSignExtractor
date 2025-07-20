package com.technicjelle.BlueMapSignExtractor;

import de.bluecolored.bluemap.core.world.mca.blockentity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class SignSide {
	private final List<SignLine> messages;
	private final @NotNull TextColour signColour;
	private final boolean isGlowing;

	private final boolean isWrittenOn;

	public SignSide() {
		this.messages = List.of();
		this.signColour = TextColour.BLACK;
		this.isGlowing = false;
		this.isWrittenOn = false;
	}

	public SignSide(@NotNull SignBlockEntity.TextData textData, int dataVersion) {
		BlueMapSignExtractor.logger.logInfo("----------------------------------------------------------------------------------------------------");
		this.messages = textData.getMessages().stream().map(m -> new SignLine(m, dataVersion)).toList();
		this.signColour = Objects.requireNonNullElse(TextColour.get(textData.getColor()), TextColour.BLACK);
		this.isGlowing = textData.isHasGlowingText();
		this.isWrittenOn = setIsWrittenOn();
	}

	private boolean setIsWrittenOn() {
		for (SignLine message : messages) {
			if (message != null && !message.text.isBlank()) {
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
			if (message != null && !message.text.isBlank()) {
				return message.text;
			}
		}
		return null;
	}

	public String getFormattedHTML() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div ").append(signColour.getHTMLAttributes(isGlowing, "sign-side")).append(">");
		for (SignLine message : messages) {
			sb.append(message.formatSignLineToHTML(isGlowing)).append("\n");
		}
		sb.append("</div>");
		return sb.toString();
	}
}
