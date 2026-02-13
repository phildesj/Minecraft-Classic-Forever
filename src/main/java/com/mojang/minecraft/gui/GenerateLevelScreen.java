package com.mojang.minecraft.gui;

/**
 * A screen for selecting the size of a new level to generate.
 */
public final class GenerateLevelScreen extends GuiScreen {

	/** The parent screen to return to. */
	private final GuiScreen parent;

	/**
	 * Creates a new generate level screen.
	 *
	 * @param parent The parent screen.
	 */
	public GenerateLevelScreen(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void onOpen() {
		this.buttons.clear();
		this.buttons.add(new Button(0, this.width / 2 - 100, this.height / 4, "Small"));
		this.buttons.add(new Button(1, this.width / 2 - 100, this.height / 4 + 24, "Normal"));
		this.buttons.add(new Button(2, this.width / 2 - 100, this.height / 4 + 48, "Huge"));
		this.buttons.add(new Button(3, this.width / 2 - 100, this.height / 4 + 120, "Cancel"));
	}

	@Override
	protected void onButtonClick(Button button) {
		if (button.id == 3) {
			this.minecraft.setCurrentScreen(this.parent);
		} else {
			this.minecraft.generateLevel(button.id);
			this.minecraft.setCurrentScreen(null);
			this.minecraft.grabMouse();
		}
	}

	@Override
	public void render(int mouseX, int mouseY) {
		// Draw semi-transparent background
		drawFadingBox(0, 0, this.width, this.height, 1610941696, -1607454624);
		drawCenteredString(this.fontRenderer, "Generate new level", this.width / 2, 40, 16777215);
		super.render(mouseX, mouseY);
	}
}


