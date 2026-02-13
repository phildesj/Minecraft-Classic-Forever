package com.mojang.minecraft.gui;

/**
 * A screen used to display an error message to the user.
 */
public final class ErrorScreen extends GuiScreen {

	/** The title of the error. */
	private final String title;
	/** The description text of the error. */
	private final String text;

	/**
	 * Creates a new error screen.
	 *
	 * @param title The title of the error.
	 * @param text  The description of the error.
	 */
	public ErrorScreen(String title, String text) {
		this.title = title;
		this.text = text;
	}

	@Override
	public void render(int mouseX, int mouseY) {
		drawFadingBox(0, 0, this.width, this.height, -12574688, -11530224);
		drawCenteredString(this.fontRenderer, this.title, this.width / 2, 90, 16777215);
		drawCenteredString(this.fontRenderer, this.text, this.width / 2, 110, 16777215);
		super.render(mouseX, mouseY);
	}

	@Override
	protected void onKeyPress(char typedChar, int keyCode) {}
}



