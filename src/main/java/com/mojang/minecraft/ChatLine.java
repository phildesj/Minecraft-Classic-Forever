package com.mojang.minecraft;

/**
 * ChatLine represents a single message in the in-game chat system.
 * Each chat message stores the text content and tracks how long the message
 * has been displayed in the chat window for fade-out effects and cleanup.
 */
public class ChatLine {
	/**
	 * Constructs a new ChatLine with the specified message text.
	 * The message time is initialized to 0 ticks.
	 *
	 * @param message the text content of the chat message to display
	 */
	public ChatLine(String message) {
		// Store the chat message text for rendering in the chat interface
		this.message = message;

		// Initialize the time counter to track how long this message has been displayed
		this.time = 0;
	}

	/** The text content of this chat message to be displayed in the chat window. */
	public String message;

	/** The time in ticks this message has been displayed, used for fade-out effects and removal. */
	public int time;
}
