package com.mojang.minecraft.gui;

/**
 * A clickable GUI button.
 */
public class Button extends Screen {

	/** The width of the button. */
	int width;
	/** The height of the button. */
	int height;
	/** The X position of the button on screen. */
	public int x;
	/** The Y position of the button on screen. */
	public int y;
	/** The text displayed on the button. */
	public String text;
	/** The ID of the button. */
	public int id;
	/** Whether the button is currently active and clickable. */
	public boolean active;
	/** Whether the button is currently visible. */
	public boolean visible;

	/**
	 * Creates a new button with default dimensions (200x20).
	 *
	 * @param id   The button ID.
	 * @param x    The X position.
	 * @param y    The Y position.
	 * @param text The button text.
	 */
	public Button(int id, int x, int y, String text) {
		this(id, x, y, 200, 20, text);
	}

	/**
	 * Creates a new button with custom dimensions.
	 *
	 * @param id     The button ID.
	 * @param x      The X position.
	 * @param y      The Y position.
	 * @param width  The width.
	 * @param height The height.
	 * @param text   The button text.
	 */
	protected Button(int id, int x, int y, int width, int height, String text) {
		this.active = true;
		this.visible = true;
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text;
	}
}



