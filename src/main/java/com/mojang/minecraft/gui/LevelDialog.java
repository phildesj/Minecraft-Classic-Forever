package com.mojang.minecraft.gui;

import com.mojang.minecraft.gui.LoadLevelScreen;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * Dialog for selecting a level file to load or save.
 * Runs on a separate thread to prevent blocking the game.
 */
final class LevelDialog extends Thread {

	/** Reference to the parent LoadLevelScreen */
	private LoadLevelScreen screen;

	/**
	 * Creates a new level file dialog.
	 *
	 * @param screen the LoadLevelScreen instance that owns this dialog
	 */
	LevelDialog(LoadLevelScreen screen) {
		super();
		this.screen = screen;
	}

	/**
	 * Shows the file chooser dialog and sets the selected file on the screen.
	 * This runs on a separate thread to avoid blocking the game loop.
	 */
	public final void run() {
		JFileChooser fileChooser;
		LoadLevelScreen screenRef;
		try {
			// Create and configure the file chooser
			fileChooser = new JFileChooser();
			this.screen.chooser = fileChooser;

			// Set up file filter for Minecraft level files
			FileNameExtensionFilter levelFilter = new FileNameExtensionFilter("Minecraft levels", new String[]{"mine"});
			this.screen.chooser.setFileFilter(levelFilter);
			this.screen.chooser.setMultiSelectionEnabled(false);

			// Show either save or open dialog based on screen mode
			int dialogResult;
			if(this.screen.saving) {
				dialogResult = this.screen.chooser.showSaveDialog(this.screen.minecraft.canvas);
			} else {
				dialogResult = this.screen.chooser.showOpenDialog(this.screen.minecraft.canvas);
			}

			// If user confirmed the selection (JFileChooser.APPROVE_OPTION = 0)
			if(dialogResult == 0) {
				// Set the selected file on the screen so it can be loaded in the next tick
				this.screen.selectedFile = this.screen.chooser.getSelectedFile();
			}
		} finally {
			// Clean up: unfreeze the UI and clear the chooser reference
			this.screen.frozen = false;
			this.screen.chooser = null;
		}
	}
}
