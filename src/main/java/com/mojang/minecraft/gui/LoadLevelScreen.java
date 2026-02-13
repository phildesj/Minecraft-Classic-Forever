package com.mojang.minecraft.gui;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.Level;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A screen for loading levels, either from online storage or a local file.
 */
public class LoadLevelScreen extends GuiScreen implements Runnable {

	/** The parent screen to return to. */
	protected GuiScreen parent;
	/** Whether the level list loading thread has finished. */
	private boolean finished = false;
	/** Whether the level list has been successfully loaded. */
	private boolean loaded = false;
	/** The list of level names retrieved from the server. */
	private String[] levels = null;
	/** Current status message shown while loading. */
	private String status = "";
	/** The title displayed at the top of the screen. */
	protected String title = "Load level";
	/** Whether the UI is frozen (e.g., when a file chooser is open). */
	boolean frozen = false;
	/** The file chooser instance. */
	JFileChooser chooser;
	/** Whether this screen is being used for saving instead of loading. */
	protected boolean saving = false;
	/** The file selected from the local file system. */
	protected File selectedFile;

	/**
	 * Creates a new load level screen.
	 *
	 * @param parent The parent screen to return to.
	 */
	public LoadLevelScreen(GuiScreen parent) {
		this.parent = parent;
	}

	@Override
	public void run() {
		try {
			if (this.frozen) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			this.status = "Getting level list..";
			URL listUrl = new URL("http://" + this.minecraft.host + "/listmaps.jsp?user=" + this.minecraft.session.username);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(listUrl.openConnection().getInputStream()))) {
				String line = reader.readLine();
				if (line != null) {
					this.levels = line.split(";");
					if (this.levels.length >= 5) {
						this.setLevels(this.levels);
						this.loaded = true;
						return;
					}
					this.status = this.levels[0];
				} else {
					this.status = "Failed to load levels";
				}
				this.finished = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.status = "Failed to load levels";
			this.finished = true;
		}
	}

	/**
	 * Updates the button labels with the retrieved level names.
	 *
	 * @param levelNames The array of level names.
	 */
	protected void setLevels(String[] levelNames) {
		for (int i = 0; i < 5; ++i) {
			Button button = this.buttons.get(i);
			button.active = !levelNames[i].equals("-");
			button.text = levelNames[i];
			button.visible = true;
		}
	}

	@Override
	public void onOpen() {
		new Thread(this).start();

		for (int i = 0; i < 5; ++i) {
			Button button = new Button(i, this.width / 2 - 100, this.height / 6 + i * 24, "---");
			button.visible = false;
			button.active = false;
			this.buttons.add(button);
		}

		this.buttons.add(new Button(5, this.width / 2 - 100, this.height / 6 + 120 + 12, "Load file..."));
		this.buttons.add(new Button(6, this.width / 2 - 100, this.height / 6 + 168, "Cancel"));
	}

	@Override
	protected void onButtonClick(Button button) {
		if (!this.frozen) {
			if (button.active) {
				if (this.loaded && button.id < 5) {
					this.openLevel(button.id);
				}

				if (this.finished || this.loaded && button.id == 5) {
					this.frozen = true;
					LevelDialog dialog = new LevelDialog(this);
					dialog.setDaemon(true);
					// Start the dialog thread to show the file chooser
					dialog.start();
				}

				if (this.finished || this.loaded && button.id == 6) {
					this.minecraft.setCurrentScreen(this.parent);
				}
			}
		}
	}

	/**
	 * Loads a level from a local file.
	 *
	 * @param file The level file to load.
	 */
	protected void openLevel(File file) {
		Minecraft mc = this.minecraft;
		Level level = mc.levelIo.load(file);

		if (level != null) {
			// Ensure transient fields are initialized
			level.initTransient();
			// Set the loaded level as the current level
			mc.setLevel(level);
			// Return to the parent screen
			this.minecraft.setCurrentScreen(this.parent);
		}
	}

	/**
	 * Loads a level from the online storage.
	 *
	 * @param id The ID of the online level.
	 */
	protected void openLevel(int id) {
		this.minecraft.loadOnlineLevel(this.minecraft.session.username, id);
		this.minecraft.setCurrentScreen(null);
		this.minecraft.grabMouse();
	}

	@Override
	public void render(int mouseX, int mouseY) {
		drawFadingBox(0, 0, this.width, this.height, 1610941696, -1607454624);
		drawCenteredString(this.fontRenderer, this.title, this.width / 2, 20, 16777215);

		if (this.frozen) {
			drawCenteredString(this.fontRenderer, "Selecting file..", this.width / 2, this.height / 2 - 4, 16777215);

			try {
				Thread.sleep(20L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			if (!this.loaded) {
				drawCenteredString(this.fontRenderer, this.status, this.width / 2, this.height / 2 - 4, 16777215);
			}

			super.render(mouseX, mouseY);
		}
	}

	@Override
	public void onClose() {
		super.onClose();
		if (this.chooser != null) {
			this.chooser.cancelSelection();
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.selectedFile != null) {
			this.openLevel(this.selectedFile);
			this.selectedFile = null;
		}
	}
}


