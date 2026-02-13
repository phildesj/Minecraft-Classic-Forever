package com.mojang.minecraft.gui;

/**
 * A specialized button used in option screens, typically with a fixed width.
 */
public final class OptionButton extends Button {

	/**
	 * Creates a new option button with default dimensions (150x20).
	 *
	 * @param id   The button ID.
	 * @param x    The X position.
	 * @param y    The Y position.
	 * @param text The button text.
	 */
	public OptionButton(int id, int x, int y, String text) {
		super(id, x, y, 150, 20, text);
	}
}


