package com.mojang.minecraft.gui;

import com.mojang.minecraft.GameSettings;

/**
 * A screen for configuring various game options.
 */
public final class OptionsScreen extends GuiScreen {

	/** The parent screen to return to. */
	private final GuiScreen parent;
	/** The title of this screen. */
	private final String title = "Options";
	/** Reference to the game settings. */
	private final GameSettings settings;

	/**
	 * Creates a new options screen.
	 *
	 * @param parent   The parent screen.
	 * @param settings The game settings to modify.
	 */
	public OptionsScreen(GuiScreen parent, GameSettings settings) {
		this.parent = parent;
		this.settings = settings;
	}

	@Override
	public void onOpen() {
		for (int i = 0; i < this.settings.settingCount; ++i) {
			this.buttons.add(new OptionButton(i, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), this.settings.getSetting(i)));
		}

		this.buttons.add(new Button(100, this.width / 2 - 100, this.height / 6 + 120 + 12, "Controls..."));
		this.buttons.add(new Button(200, this.width / 2 - 100, this.height / 6 + 168, "Done"));
	}

	@Override
	protected void onButtonClick(Button button) {
		if (button.active) {
			if (button.id < 100) {
				this.settings.toggleSetting(button.id, 1);
				button.text = this.settings.getSetting(button.id);
			}

			if (button.id == 100) {
				this.minecraft.setCurrentScreen(new ControlsScreen(this, this.settings));
			}

			if (button.id == 200) {
				this.minecraft.setCurrentScreen(this.parent);
			}
		}
	}

	@Override
	public void render(int mouseX, int mouseY) {
		// Draw semi-transparent background
		drawFadingBox(0, 0, this.width, this.height, 1610941696, -1607454624);
		drawCenteredString(this.fontRenderer, this.title, this.width / 2, 20, 16777215);
		super.render(mouseX, mouseY);
	}
}


