package com.mojang.minecraft.gui;

import com.mojang.minecraft.GameSettings;

/**
 * A screen for configuring game controls and key bindings.
 */
public final class ControlsScreen extends GuiScreen {

	/** The parent screen to return to. */
	private final GuiScreen parent;
	/** The title of this screen. */
	private final String title = "Controls";
	/** Reference to the game settings. */
	private final GameSettings settings;
	/** The index of the binding currently being edited, or -1. */
	private int selectedBinding = -1;

	/**
	 * Creates a new controls screen.
	 *
	 * @param parent   The parent screen.
	 * @param settings The game settings to modify.
	 */
	public ControlsScreen(GuiScreen parent, GameSettings settings) {
		this.parent = parent;
		this.settings = settings;
	}

	@Override
	public void onOpen() {
		for (int i = 0; i < this.settings.bindings.length; ++i) {
			this.buttons.add(new OptionButton(i, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), this.settings.getBinding(i)));
		}

		this.buttons.add(new Button(200, this.width / 2 - 100, this.height / 6 + 168, "Done"));
	}

	@Override
	protected void onButtonClick(Button button) {
		// Reset text for all buttons
		for (int i = 0; i < this.settings.bindings.length; ++i) {
			((Button) this.buttons.get(i)).text = this.settings.getBinding(i);
		}

		if (button.id == 200) {
			this.minecraft.setCurrentScreen(this.parent);
		} else {
			this.selectedBinding = button.id;
			button.text = "> " + this.settings.getBinding(button.id) + " <";
		}
	}

	@Override
	protected void onKeyPress(char typedChar, int keyCode) {
		if (this.selectedBinding >= 0) {
			this.settings.setBinding(this.selectedBinding, keyCode);
			((Button) this.buttons.get(this.selectedBinding)).text = this.settings.getBinding(this.selectedBinding);
			this.selectedBinding = -1;
		} else {
			super.onKeyPress(typedChar, keyCode);
		}
	}

	@Override
	public void render(int mouseX, int mouseY) {
		drawFadingBox(0, 0, this.width, this.height, 1610941696, -1607454624);
		drawCenteredString(this.fontRenderer, this.title, this.width / 2, 20, 16777215);
		super.render(mouseX, mouseY);
	}
}



