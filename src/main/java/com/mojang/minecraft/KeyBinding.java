package com.mojang.minecraft;

/**
 * KeyBinding represents a single keyboard control binding for the Minecraft Classic Forever client.
 * Each binding consists of a human-readable name and an LWJGL key code that defines
 * which physical key on the keyboard is associated with that control action.
 */
public class KeyBinding {
	/**
	 * Constructs a new KeyBinding with the specified name and key code.
	 *
	 * @param name the human-readable name of this key binding (e.g., "Forward", "Jump", "Chat")
	 * @param key the LWJGL key code identifying the physical keyboard key
	 */
	public KeyBinding(String name, int key) {
		// Store the binding name for display in settings menus
		this.name = name;

		// Store the LWJGL key code for input handling
		this.key = key;
	}

	/** The human-readable name of this key binding used in settings and UI. */
	public String name;

	/** The LWJGL key code representing the physical keyboard key for this binding. */
	public int key;
}
