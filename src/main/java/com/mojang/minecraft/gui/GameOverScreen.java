package com.mojang.minecraft.gui;

import org.lwjgl.opengl.GL11;

/**
 * Screen displayed when the player dies in the game.
 * Provides options to generate a new level or load an existing one.
 */
public final class GameOverScreen extends GuiScreen {

	// Button IDs for screen navigation
	private static final int BUTTON_ID_GENERATE = 1;
	private static final int BUTTON_ID_LOAD = 2;

	// UI positioning constants
	private static final int BUTTON_X_OFFSET = 100;
	private static final int BUTTON_Y_OFFSET_GENERATE = 72;
	private static final int BUTTON_Y_OFFSET_LOAD = 96;
	private static final int TEXT_SCALE = 2;
	private static final int TITLE_X = 30;
	private static final int SCORE_Y = 100;

	// Color constants (ARGB format)
	private static final int COLOR_FADE_BOX_START = 1615855616;
	private static final int COLOR_FADE_BOX_END = -1602211792;
	private static final int COLOR_TEXT_WHITE = 16777215;

	/**
	 * Initializes the screen when opened.
	 * Sets up buttons for generating a new level and loading an existing level.
	 */
	@Override
	public final void onOpen() {
		this.buttons.clear();

		// Add generate level button
		this.buttons.add(new Button(BUTTON_ID_GENERATE, this.width / 2 - BUTTON_X_OFFSET, this.height / 4 + BUTTON_Y_OFFSET_GENERATE, "Generate new level..."));

		// Add load level button
		Button loadButton = new Button(BUTTON_ID_LOAD, this.width / 2 - BUTTON_X_OFFSET, this.height / 4 + BUTTON_Y_OFFSET_LOAD, "Load level..");
		this.buttons.add(loadButton);

		// Disable load button if no session is available
		if (this.minecraft.session == null) {
			loadButton.active = false;
		}
	}

	/**
	 * Handles button click events.
	 *
	 * @param button the button that was clicked
	 */
	@Override
	protected final void onButtonClick(Button button) {
		switch (button.id) {
			case BUTTON_ID_GENERATE:
				this.minecraft.setCurrentScreen(new GenerateLevelScreen(this));
				break;
			case BUTTON_ID_LOAD:
				if (this.minecraft.session != null) {
					this.minecraft.setCurrentScreen(new LoadLevelScreen(this));
				}
				break;
		}
	}

	/**
	 * Renders the game over screen with title and player score.
	 *
	 * @param mouseX the X coordinate of the mouse cursor
	 * @param mouseY the Y coordinate of the mouse cursor
	 */
	@Override
	public final void render(int mouseX, int mouseY) {
		// Draw fading background
		drawFadingBox(0, 0, this.width, this.height, COLOR_FADE_BOX_START, COLOR_FADE_BOX_END);

		// Draw scaled "Game over!" title
		GL11.glPushMatrix();
		GL11.glScalef(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
		drawCenteredString(this.fontRenderer, "Game over!", this.width / 2 / TEXT_SCALE, TITLE_X, COLOR_TEXT_WHITE);
		GL11.glPopMatrix();

		// Draw player score
		drawCenteredString(this.fontRenderer, "Score: &e" + this.minecraft.player.getScore(), this.width / 2, SCORE_Y, COLOR_TEXT_WHITE);

		// Render parent components (buttons)
		super.render(mouseX, mouseY);
	}
}
