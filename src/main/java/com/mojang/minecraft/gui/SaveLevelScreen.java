package com.mojang.minecraft.gui;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gui.Button;
import com.mojang.minecraft.gui.GuiScreen;
import com.mojang.minecraft.gui.LevelNameScreen;
import com.mojang.minecraft.gui.LoadLevelScreen;
import java.io.File;

/**
 * A screen for saving levels, either to online storage or a local file.
 * Extends LoadLevelScreen and overrides the behavior to save instead of load.
 */
public final class SaveLevelScreen extends LoadLevelScreen {

	/**
	 * Creates a new save level screen.
	 *
	 * @param parentScreen The parent screen to return to after saving.
	 */
	public SaveLevelScreen(GuiScreen parentScreen) {
		super(parentScreen);
		this.title = "Save level";
		this.saving = true;
	}

	@Override
	public final void onOpen() {
		super.onOpen();
		// Change button 5 text from "Load file..." to "Save file..."
		((Button) this.buttons.get(5)).text = "Save file...";
	}

	/**
	 * Sets up the online level save buttons with appropriate names and availability.
	 *
	 * @param levelNames The names of the online levels available.
	 */
	@Override
	protected final void setLevels(String[] levelNames) {
		// Set up buttons for online level saving
		for (int i = 0; i < 5; ++i) {
			Button button = ((Button) this.buttons.get(i));
			button.text = levelNames[i];
			button.visible = true;
			// Only allow saving to online storage if user has premium
			button.active = this.minecraft.session.hasPaid;
		}
	}

	@Override
	public final void render(int mouseX, int mouseY) {
		super.render(mouseX, mouseY);

		// Display premium-only message if user hasn't purchased the game
		if (!this.minecraft.session.hasPaid) {
			drawFadingBox(this.width / 2 - 80, 72, this.width / 2 + 80, 120, -536870912, -536870912);
			drawCenteredString(this.fontRenderer, "Premium only!", this.width / 2, 80, 16748688);
			drawCenteredString(this.fontRenderer, "Purchase the game to be able", this.width / 2, 96, 14712960);
			drawCenteredString(this.fontRenderer, "to save your levels online.", this.width / 2, 104, 14712960);
		}
	}

	/**
	 * Saves the level to a local file.
	 * Automatically adds .mine extension if not present.
	 *
	 * @param file The file to save to.
	 */
	@Override
	protected final void openLevel(File file) {
		// Ensure the file has the .mine extension
		if (!file.getName().endsWith(".mine")) {
			file = new File(file.getParentFile(), file.getName() + ".mine");
		}

		// Save the current level to the file
		this.minecraft.levelIo.save(this.minecraft.level, file);

		// Return to the parent screen
		this.minecraft.setCurrentScreen(this.parent);
	}

	/**
	 * Saves the level to online storage.
	 * Opens the level name screen to allow the user to enter a name.
	 *
	 * @param levelIndex The index of the selected online level.
	 */
	@Override
	protected final void openLevel(int levelIndex) {
		// Open the level name screen for saving to online storage
		String levelName = ((Button) this.buttons.get(levelIndex)).text;
		this.minecraft.setCurrentScreen(new LevelNameScreen(this, levelName, levelIndex));
	}

	/**
	 * Handles button clicks on the save screen.
	 * Routes to appropriate save method based on button id.
	 *
	 * @param button The button that was clicked.
	 */
	@Override
	protected void onButtonClick(Button button) {
		// Only handle online level saving through LevelNameScreen
		// For local file saving (button id 5), let the parent class handle the file dialog
		if (button.id < 5 && this.loaded) {
			// Save to online storage: open level name dialog
			this.minecraft.setCurrentScreen(new LevelNameScreen(this, ((Button) this.buttons.get(button.id)).text, button.id));
		} else if (button.id == 5) {
			// Save to local file: let parent class handle the file dialog
			super.onButtonClick(button);
		} else if (button.id == 6) {
			// Cancel button: return to parent screen
			super.onButtonClick(button);
		}
	}
}
