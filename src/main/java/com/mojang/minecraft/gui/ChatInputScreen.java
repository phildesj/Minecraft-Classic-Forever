package com.mojang.minecraft.gui;

import com.mojang.minecraft.net.PacketType;
import org.lwjgl.input.Keyboard;

/**
 * A screen for typing and sending chat messages.
 */
public final class ChatInputScreen extends GuiScreen {

	/** The current message being typed. */
	private String message = "";
	/** A counter used for the blinking cursor effect. */
	private int counter = 0;
	/** Allowed characters in chat. */
	private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.:-_'*!\\\"#%/()=+?[]{}<>@|$;";

	@Override
	public void onOpen() {
		Keyboard.enableRepeatEvents(true);
	}

	@Override
	public void onClose() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void tick() {
		++this.counter;
	}

	@Override
	protected void onKeyPress(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			this.minecraft.setCurrentScreen(null);
		} else if (keyCode == Keyboard.KEY_RETURN) {
			String trimmedMessage = this.message.trim();

			if (!trimmedMessage.isEmpty()) {
				this.minecraft.networkManager.netHandler.send(PacketType.CHAT_MESSAGE, -1, trimmedMessage);
			}

			this.minecraft.setCurrentScreen(null);
		} else {
			if (keyCode == Keyboard.KEY_BACK && !this.message.isEmpty()) {
				this.message = this.message.substring(0, this.message.length() - 1);
			}

			if (ALLOWED_CHARS.indexOf(typedChar) >= 0 && this.message.length() < 64 - (this.minecraft.session.username.length() + 2)) {
				this.message = this.message + typedChar;
			}
		}
	}

	@Override
	public void render(int mouseX, int mouseY) {
		// Draw the chat input box at the bottom
		drawBox(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);

		// Blinking cursor logic
		String cursor = (this.counter / 6 % 2 == 0) ? "_" : "";
		drawString(this.fontRenderer, "> " + this.message + cursor, 4, this.height - 12, 14737632);
	}

	@Override
	protected void onMouseClick(int mouseX, int mouseY, int button) {
		if (button == 0 && this.minecraft.hud.hoveredPlayer != null) {
			if (!this.message.isEmpty() && !this.message.endsWith(" ")) {
				this.message = this.message + " ";
			}

			this.message = this.message + this.minecraft.hud.hoveredPlayer;
			int maxLength = 64 - (this.minecraft.session.username.length() + 2);
			if (this.message.length() > maxLength) {
				this.message = this.message.substring(0, maxLength);
			}
		}
	}
}



