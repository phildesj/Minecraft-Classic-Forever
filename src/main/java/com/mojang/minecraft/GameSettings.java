package com.mojang.minecraft;

import com.mojang.minecraft.render.TextureManager;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import javax.imageio.ImageIO;
import org.lwjgl.input.Keyboard;

/**
 * GameSettings manages all game settings and key bindings for the Minecraft Classic Forever client.
 * This class handles loading, saving, and modifying game options such as audio, graphics,
 * and keyboard bindings. Settings are persisted to an options.txt file in the minecraft folder.
 */
public final class GameSettings {
	/**
	 * Constructs a new GameSettings instance and initializes all settings.
	 * Loads previously saved settings from the options.txt file if it exists.
	 *
	 * @param minecraft the Minecraft instance
	 * @param minecraftFolder the folder where the options.txt file is stored
	 */
	public GameSettings(Minecraft minecraft, File minecraftFolder) {
		// Initialize key bindings array with all key binding instances
		bindings = new KeyBinding[] {forwardKey, leftKey, backKey, rightKey, jumpKey, buildKey, chatKey, toggleFogKey, saveLocationKey, loadLocationKey};

		// Total number of toggleable settings in the game
		settingCount = 8;

		// Store reference to the main Minecraft instance
		this.minecraft = minecraft;

		// Create file reference for storing options
		settingsFile = new File(minecraftFolder, "options.txt");

		// Load settings from file
		load();
	}

	/**
	 * Available render distance options in descending order of draw distance.
	 */
	private static final String[] renderDistances = new String[]{"FAR", "NORMAL", "SHORT", "TINY"};

	/** Enable or disable music playback. */
	public boolean music = true;

	/** Enable or disable sound effects. */
	public boolean sound = true;

	/** Inverts the Y-axis mouse movement for vertical camera control. */
	public boolean invertMouse = false;

	/** Display frames per second counter on screen. */
	public boolean showFrameRate = false;

	/** Render distance index (0=FAR, 1=NORMAL, 2=SHORT, 3=TINY). */
	public int viewDistance = 0;

	/** Enable or disable view bobbing effect when moving. */
	public boolean viewBobbing = true;

	/** Enable 3D anaglyph rendering for stereoscopic 3D glasses. */
	public boolean anaglyph = false;

	/** Limit the frame rate to improve performance. */
	public boolean limitFramerate = false;

	/** Key binding for moving forward (default: W key). */
	public KeyBinding forwardKey = new KeyBinding("Forward", 17);

	/** Key binding for moving left (default: A key). */
	public KeyBinding leftKey = new KeyBinding("Left", 30);

	/** Key binding for moving backward (default: S key). */
	public KeyBinding backKey = new KeyBinding("Back", 31);

	/** Key binding for moving right (default: D key). */
	public KeyBinding rightKey = new KeyBinding("Right", 32);

	/** Key binding for jumping (default: Space key). */
	public KeyBinding jumpKey = new KeyBinding("Jump", 57);

	/** Key binding for building blocks (default: V key). */
	public KeyBinding buildKey = new KeyBinding("Build", 48);

	/** Key binding for opening chat (default: T key). */
	public KeyBinding chatKey = new KeyBinding("Chat", 20);

	/** Key binding for toggling fog rendering (default: F key). */
	public KeyBinding toggleFogKey = new KeyBinding("Toggle fog", 33);

	/** Key binding for saving player location (default: N key). */
	public KeyBinding saveLocationKey = new KeyBinding("Save location", 28);

	/** Key binding for loading saved player location (default: L key). */
	public KeyBinding loadLocationKey = new KeyBinding("Load location", 19);

	/** Array containing all key bindings for iteration. */
	public KeyBinding[] bindings;

	/** Reference to the main Minecraft instance. */
	private Minecraft minecraft;

	/** File reference for the options.txt settings file. */
	private File settingsFile;

	/** Total number of toggleable settings. */
	public int settingCount;

	/**
	 * Gets the display name and key code for a specific key binding.
	 *
	 * @param key the index of the key binding in the bindings array
	 * @return a formatted string with the binding name and key name
	 */
	public String getBinding(int key) {
		return bindings[key].name + ": " + Keyboard.getKeyName(bindings[key].key);
	}

	/**
	 * Sets the key code for a specific key binding and saves the settings.
	 *
	 * @param key the index of the key binding in the bindings array
	 * @param keyID the LWJGL key code to set for the binding
	 */
	public void setBinding(int key, int keyID) {
		bindings[key].key = keyID;

		save();
	}

	/**
	 * Toggles a game setting and saves the configuration.
	 * Setting indices: 0=Music, 1=Sound, 2=Invert Mouse, 3=Show FPS,
	 * 4=Render Distance, 5=View Bobbing, 6=3D Anaglyph, 7=Limit Framerate
	 *
	 * @param setting the index of the setting to toggle
	 * @param fogValue adjustment value for render distance changes
	 */
	public void toggleSetting(int setting, int fogValue) {
		// Toggle music setting (0)
		if(setting == 0) {
			music = !music;
		}

		// Toggle sound setting (1)
		if(setting == 1) {
			sound = !sound;
		}

		// Toggle mouse Y-axis inversion (2)
		if(setting == 2) {
			invertMouse = !invertMouse;
		}

		// Toggle FPS counter display (3)
		if(setting == 3) {
			showFrameRate = !showFrameRate;
		}

		// Cycle through render distance options (4)
		if(setting == 4) {
			viewDistance = viewDistance + fogValue & 3;
		}

		// Toggle view bobbing effect (5)
		if(setting == 5) {
			viewBobbing = !viewBobbing;
		}

		// Toggle 3D anaglyph mode and reload all textures with anaglyph effect (6)
		if(setting == 6) {
			anaglyph = !anaglyph;

			TextureManager textureManager = minecraft.textureManager;
			// Reload all buffered texture images to apply anaglyph effect
			Iterator iterator = this.minecraft.textureManager.textureImages.keySet().iterator();

			int i;
			BufferedImage image;

			while(iterator.hasNext()) {
				i = (Integer)iterator.next();
				image = (BufferedImage)textureManager.textureImages.get(Integer.valueOf(i));

				// Reload texture with current anaglyph setting
				textureManager.load(image, i);
			}

			// Reload all texture resources from file system
			iterator = textureManager.textures.keySet().iterator();

			while(iterator.hasNext()) {
				String s = (String)iterator.next();

				try {
					// Load texture from resource stream, applying anaglyph filter if enabled
					if(s.startsWith("##")) {
						image = TextureManager.load1(ImageIO.read(TextureManager.class.getResourceAsStream(s.substring(2))));
					} else {
						image = ImageIO.read(TextureManager.class.getResourceAsStream(s));
					}

					i = (Integer)textureManager.textures.get(s);

					textureManager.load(image, i);
				} catch (IOException var6) {
					var6.printStackTrace();
				}
			}
		}

		// Toggle frame rate limiter (7)
		if(setting == 7) {
			limitFramerate = !limitFramerate;
		}

		// Save all settings to options.txt
		save();
	}

	/**
	 * Gets a human-readable string representation of a setting's current state.
	 * This is used to display the setting name and value in the settings menu.
	 *
	 * @param id the index of the setting (0-7)
	 * @return a formatted string showing the setting name and current value
	 */
	public String getSetting(int id) {
		return id == 0 ? "Music: " + (music ? "ON" : "OFF")
				: (id == 1 ? "Sound: " + (sound ? "ON" : "OFF")
				: (id == 2 ? "Invert mouse: " + (invertMouse ? "ON" : "OFF")
				: (id == 3 ? "Show FPS: " + (showFrameRate ? "ON" : "OFF")
				: (id == 4 ? "Render distance: " + renderDistances[viewDistance]
				: (id == 5 ? "View bobbing: " + (viewBobbing ? "ON" : "OFF")
				: (id == 6 ? "3d anaglyph: " + (anaglyph ? "ON" : "OFF")
				: (id == 7 ? "Limit framerate: " + (limitFramerate ? "ON" : "OFF")
				: "")))))));
	}

	/**
	 * Loads game settings from the options.txt file in the Minecraft folder.
	 * Parses key-value pairs separated by colons and restores all game settings and key bindings.
	 * If the settings file does not exist, the method returns silently without error.
	 */
	private void load() {
		try {
			// Only attempt to load if the settings file exists
			if(settingsFile.exists()) {
				FileReader fileReader = new FileReader(settingsFile);
				BufferedReader reader = new BufferedReader(fileReader);

				String line = null;

				// Read each line from the settings file
				while((line = reader.readLine()) != null) {
					// Split the line by colon to extract setting name and value
					String[] setting = line.split(":");

					// Load music setting
					if(setting[0].equals("music")) {
						music = setting[1].equals("true");
					}

					// Load sound setting
					if(setting[0].equals("sound")) {
						sound = setting[1].equals("true");
					}

					// Load mouse Y-axis inversion setting
					if(setting[0].equals("invertYMouse")) {
						invertMouse = setting[1].equals("true");
					}

					// Load FPS display setting
					if(setting[0].equals("showFrameRate")) {
						showFrameRate = setting[1].equals("true");
					}

					// Load render distance setting
					if(setting[0].equals("viewDistance")) {
						viewDistance = Integer.parseInt(setting[1]);
					}

					// Load view bobbing setting
					if(setting[0].equals("bobView")) {
						viewBobbing = setting[1].equals("true");
					}

					// Load 3D anaglyph setting
					if(setting[0].equals("anaglyph3d")) {
						anaglyph = setting[1].equals("true");
					}

					// Load frame rate limiter setting
					if(setting[0].equals("limitFramerate")) {
						limitFramerate = setting[1].equals("true");
					}

					// Load all key bindings by matching setting name with binding name
					for(int index = 0; index < this.bindings.length; index++) {
						if(setting[0].equals("key_" + bindings[index].name)) {
							bindings[index].key = Integer.parseInt(setting[1]);
						}
					}
				}

				reader.close();
			}
		} catch (Exception e) {
			System.out.println("Failed to load options");

			e.printStackTrace();
		}
	}

	/**
	 * Saves all game settings and key bindings to the options.txt file.
	 * Writes settings as key-value pairs separated by colons, one per line.
	 * This method is called whenever any setting is changed to persist the changes.
	 */
	private void save() {
		try {
			FileWriter fileWriter = new FileWriter(this.settingsFile);
			PrintWriter writer = new PrintWriter(fileWriter);

			// Write all game settings to the file
			writer.println("music:" + music);
			writer.println("sound:" + sound);
			writer.println("invertYMouse:" + invertMouse);
			writer.println("showFrameRate:" + showFrameRate);
			writer.println("viewDistance:" + viewDistance);
			writer.println("bobView:" + viewBobbing);
			writer.println("anaglyph3d:" + anaglyph);
			writer.println("limitFramerate:" + limitFramerate);

			// Write all key bindings to the file
			for(int binding = 0; binding < bindings.length; binding++) {
				writer.println("key_" + bindings[binding].name + ":" + bindings[binding].key);
			}

			writer.close();
		} catch (Exception e) {
			System.out.println("Failed to save options");

			e.printStackTrace();
		}
	}

}
