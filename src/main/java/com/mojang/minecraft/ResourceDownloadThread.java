package com.mojang.minecraft;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Thread for loading and registering game resources (sounds and music).
 * Loads audio files from the classpath resources directory on startup.
 */
public class ResourceDownloadThread extends Thread
{
	/** Reference to the Minecraft instance for accessing the sound manager */
	private Minecraft minecraft;
	/** Directory where downloaded resources will be stored */
	private File dir;
	/** Whether the resource loading thread is currently running */
	boolean running = false;
	/** Whether the resource loading has finished */
	private boolean finished = false;
	/** Current progress of resource download (0-100) */
	private int progress = 0;

	/**
	 * Creates a new resource download thread.
	 *
	 * @param minecraftFolder The Minecraft installation folder
	 * @param minecraft The Minecraft instance
	 */
	public ResourceDownloadThread(File minecraftFolder, Minecraft minecraft)
	{
		this.minecraft = minecraft;

		this.setName("Resource download thread");
		this.setDaemon(true);

		dir = new File(minecraftFolder, "resources/");

		if(!dir.exists() && !dir.mkdirs())
		{
			throw new RuntimeException("The working directory could not be created: " + dir);
		}
	}

	/**
	 * Loads and registers all game sound and music files.
	 * This runs on a separate thread to avoid blocking the main game loop.
	 */
	@Override
	public void run()
	{
		BufferedReader reader = null;

		try {
			// Try to load step sounds (footstep sounds)
			System.out.println("Loading step sounds...");
			for(int i = 1; i <= 4; i++)
			{
				// Load from resources using resource paths instead of file paths
				// This works both in development and when packaged in a JAR
				loadSound("step/grass" + i + ".ogg", "step/grass" + i + ".ogg");
				loadSound("step/gravel" + i + ".ogg", "step/gravel" + i + ".ogg");
				loadSound("step/stone" + i + ".ogg", "step/stone" + i + ".ogg");
				loadSound("step/wood" + i + ".ogg", "step/wood" + i + ".ogg");
			}

			// Load music files
			System.out.println("Loading music files...");
			for(int i = 1; i <= 3; i++)
			{
				loadMusic("music/game/calm" + i + ".ogg", "calm" + i + ".ogg");
			}

			System.out.println("Successfully loaded all resources!");
		} catch (Exception e) {
			System.err.println("Error loading resources: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.finished = true;
	}

	/**
	 * Loads a sound file from the classpath resources.
	 *
	 * @param resourcePath The path to the resource relative to src/main/resources (e.g., "sound/step/grass1.ogg")
	 * @param soundId The identifier for the sound in the sound manager
	 */
	private void loadSound(String resourcePath, String soundId)
	{
		try {
			// Create the full resource path
			String fullResourcePath = "/sound/" + resourcePath;
			URL soundUrl = this.getClass().getResource(fullResourcePath);

			if (soundUrl == null) {
				System.err.println("Could not find sound resource: " + fullResourcePath);
				return;
			}

			// Convert URL to File - this works in development
			// For JAR files, we'd need a different approach
			File soundFile = new File(soundUrl.toURI());

			if (soundFile.exists()) {
				minecraft.sound.registerSound(soundFile, soundId);
				System.out.println("Registered sound: " + soundId);
			} else {
				System.err.println("Sound file not found: " + soundFile.getAbsolutePath());
			}
		} catch (Exception e) {
			System.err.println("Error loading sound " + resourcePath + ": " + e.getMessage());
		}
	}

	/**
	 * Loads a music file from the classpath resources.
	 *
	 * @param resourcePath The path to the resource relative to src/main/resources (e.g., "music/game/calm1.ogg")
	 * @param musicId The identifier for the music in the sound manager
	 */
	private void loadMusic(String resourcePath, String musicId)
	{
		try {
			// Create the full resource path
			String fullResourcePath = "/" + resourcePath;
			URL musicUrl = this.getClass().getResource(fullResourcePath);

			if (musicUrl == null) {
				System.err.println("Could not find music resource: " + fullResourcePath);
				return;
			}

			// Convert URL to File - this works in development
			// For JAR files, we'd need a different approach
			File musicFile = new File(musicUrl.toURI());

			if (musicFile.exists()) {
				minecraft.sound.registerMusic(musicId, musicFile);
				System.out.println("Registered music: " + musicId);
			} else {
				System.err.println("Music file not found: " + musicFile.getAbsolutePath());
			}
		} catch (Exception e) {
			System.err.println("Error loading music " + resourcePath + ": " + e.getMessage());
		}
	}

	/**
	 * Downloads a file from the specified URL.
	 * This method is currently not used but kept for future implementation.
	 *
	 * @param url The URL to download from
	 * @param file The file to save to
	 * @param size The expected file size
	 */
	private void download(URL url, File file, int size)
	{
		System.out.println("Downloading: " + file.getName() + "...");

		DataInputStream in = null;
		DataOutputStream out = null;

		try
		{
			byte[] data = new byte[4096];

			in = new DataInputStream(url.openStream());
			FileOutputStream fos = new FileOutputStream(file);
			out = new DataOutputStream(fos);

			int done = 0;

			do
			{
				int length = in.read(data);

				if(length < 0)
				{
					in.close();
					out.close();

					return;
				}

				out.write(data, 0, length);

				done += length;
				progress = (int)(((double)done / (double)size) * 100);
			} while(!running);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try
			{
				if(in != null)
				{
					in.close();
				}

				if(out != null)
				{
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		progress = 0;

		System.out.println("Downloaded: " + file.getName());
	}

	/**
	 * Checks if the resource loading has finished.
	 *
	 * @return true if finished, false otherwise
	 */
	public boolean isFinished()
	{
		return finished;
	}

	/**
	 * Gets the current progress of resource download.
	 *
	 * @return progress percentage (0-100)
	 */
	public int getProgress()
	{
		return progress;
	}
}
