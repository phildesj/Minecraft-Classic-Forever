package com.mojang.minecraft;

import com.mojang.minecraft.render.ShapeRenderer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/**
 * ProgressBarDisplay manages the rendering of a progress bar display during game loading.
 * It displays a background texture, progress bar, title, and descriptive text.
 * The display is updated at a maximum rate to avoid excessive rendering.
 */
public final class ProgressBarDisplay {

	// Minimum time between display updates in milliseconds
	private static final long UPDATE_INTERVAL_MS = 20L;

	// Texture tile size in pixels
	private static final float TEXTURE_TILE_SIZE = 32.0F;

	// Progress bar dimensions
	private static final int PROGRESS_BAR_WIDTH = 100;
	private static final int PROGRESS_BAR_HEIGHT = 2;
	private static final int PROGRESS_BAR_X_OFFSET = 50;
	private static final int PROGRESS_BAR_Y_OFFSET = 16;

	// OpenGL matrix mode constants
	private static final int GL_PROJECTION_MATRIX = 5889;
	private static final int GL_MODELVIEW_MATRIX = 5888;

	// OpenGL clear flags
	private static final int GL_CLEAR_DEPTH = 256;
	private static final int GL_CLEAR_COLOR_DEPTH = 16640;

	// Font and UI colors
	private static final int COLOR_TEXT_WHITE = 16777215;
	private static final int COLOR_PROGRESS_BACKGROUND = 8421504;
	private static final int COLOR_PROGRESS_FILL = 8454016;
	private static final int COLOR_DIRT_TEXTURE = 4210752;

	// Display text
	private String text = "";

	// Reference to the Minecraft instance
	private final Minecraft minecraft;

	// Display title
	private String title = "";

	// Timestamp of the last display update
	private long lastUpdateTime = System.currentTimeMillis();

	/**
	 * Constructs a ProgressBarDisplay with a reference to the Minecraft instance.
	 *
	 * @param minecraft the Minecraft instance to use for rendering
	 */
	public ProgressBarDisplay(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	/**
	 * Sets the title text displayed in the progress bar.
	 * This method initializes the OpenGL projection and modelview matrices.
	 *
	 * @param title the title text to display
	 * @throws StopGameException if the game is no longer running
	 */
	public void setTitle(String title) {
		if (!this.minecraft.running) {
			throw new StopGameException();
		}

		this.title = title;

		// Calculate display dimensions based on aspect ratio
		int displayWidth = this.minecraft.width * 240 / this.minecraft.height;
		int displayHeight = this.minecraft.height * 240 / this.minecraft.height;

		// Clear depth buffer
		GL11.glClear(GL_CLEAR_DEPTH);

		// Set up projection matrix for 2D rendering
		GL11.glMatrixMode(GL_PROJECTION_MATRIX);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, displayWidth, displayHeight, 0.0D, 100.0D, 300.0D);

		// Set up modelview matrix
		GL11.glMatrixMode(GL_MODELVIEW_MATRIX);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
	}

	/**
	 * Sets the descriptive text displayed below the title.
	 * Triggers a progress update with no progress (-1).
	 *
	 * @param text the descriptive text to display
	 * @throws StopGameException if the game is no longer running
	 */
	public void setText(String text) {
		if (!this.minecraft.running) {
			throw new StopGameException();
		}

		this.text = text;
		this.setProgress(-1);
	}

	/**
	 * Updates and renders the progress bar display.
	 * The display is only updated if at least UPDATE_INTERVAL_MS milliseconds have passed
	 * since the last update to avoid excessive rendering.
	 *
	 * @param progress the progress value (0-100), or -1 to hide the progress bar
	 * @throws StopGameException if the game is no longer running
	 */
	public void setProgress(int progress) {
		if (!this.minecraft.running) {
			throw new StopGameException();
		}

		// Check if enough time has passed since the last update
		long currentTime = System.currentTimeMillis();
		if (currentTime - this.lastUpdateTime < 0L || currentTime - this.lastUpdateTime >= UPDATE_INTERVAL_MS) {
			this.lastUpdateTime = currentTime;
			renderProgressDisplay(progress);
		}
	}

	/**
	 * Renders the complete progress bar display including background, progress bar, and text.
	 *
	 * @param progress the progress value (0-100), or -1 to hide the progress bar
	 */
	private void renderProgressDisplay(int progress) {
		// Calculate display dimensions
		int displayWidth = this.minecraft.width * 240 / this.minecraft.height;
		int displayHeight = this.minecraft.height * 240 / this.minecraft.height;

		// Clear color and depth buffers
		GL11.glClear(GL_CLEAR_COLOR_DEPTH);

		// Render background texture
		ShapeRenderer renderer = ShapeRenderer.instance;
		int dirtTextureId = this.minecraft.textureManager.load("/dirt.png");
		GL11.glBindTexture(3553, dirtTextureId);

		renderer.begin();
		renderer.color(COLOR_DIRT_TEXTURE);
		renderer.vertexUV(0.0F, displayHeight, 0.0F, 0.0F, displayHeight / TEXTURE_TILE_SIZE);
		renderer.vertexUV(displayWidth, displayHeight, 0.0F, displayWidth / TEXTURE_TILE_SIZE, displayHeight / TEXTURE_TILE_SIZE);
		renderer.vertexUV(displayWidth, 0.0F, 0.0F, displayWidth / TEXTURE_TILE_SIZE, 0.0F);
		renderer.vertexUV(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		renderer.end();

		// Render progress bar if progress is valid
		if (progress >= 0) {
			renderProgressBar(displayWidth, displayHeight, progress, renderer);
		}

		// Render title and descriptive text
		int titleX = (displayWidth - this.minecraft.fontRenderer.getWidth(this.title)) / 2;
		int titleY = displayHeight / 2 - 4 - PROGRESS_BAR_Y_OFFSET;
		this.minecraft.fontRenderer.render(this.title, titleX, titleY, COLOR_TEXT_WHITE);

		int textX = (displayWidth - this.minecraft.fontRenderer.getWidth(this.text)) / 2;
		int textY = displayHeight / 2 - 4 + 8;
		this.minecraft.fontRenderer.render(this.text, textX, textY, COLOR_TEXT_WHITE);

		// Update display
		Display.update();

		// Yield to other threads
		try {
			Thread.yield();
		} catch (Exception e) {
			// Thread.yield() should not throw, but handle gracefully if it does
		}
	}

	/**
	 * Renders the progress bar rectangle with background and fill.
	 *
	 * @param displayWidth the width of the display area
	 * @param displayHeight the height of the display area
	 * @param progress the progress value (0-100)
	 * @param renderer the ShapeRenderer instance
	 */
	private void renderProgressBar(int displayWidth, int displayHeight, int progress, ShapeRenderer renderer) {
		// Calculate progress bar position
		int progressBarX = displayWidth / 2 - PROGRESS_BAR_X_OFFSET;
		int progressBarY = displayHeight / 2 + PROGRESS_BAR_Y_OFFSET;

		// Disable texture rendering for solid color primitives
		GL11.glDisable(3553);

		renderer.begin();

		// Render progress bar background (light gray)
		renderer.color(COLOR_PROGRESS_BACKGROUND);
		renderer.vertex(progressBarX, progressBarY, 0.0F);
		renderer.vertex(progressBarX, progressBarY + PROGRESS_BAR_HEIGHT, 0.0F);
		renderer.vertex(progressBarX + PROGRESS_BAR_WIDTH, progressBarY + PROGRESS_BAR_HEIGHT, 0.0F);
		renderer.vertex(progressBarX + PROGRESS_BAR_WIDTH, progressBarY, 0.0F);

		// Render progress bar fill (darker gray, filled up to progress percentage)
		renderer.color(COLOR_PROGRESS_FILL);
		renderer.vertex(progressBarX, progressBarY, 0.0F);
		renderer.vertex(progressBarX, progressBarY + PROGRESS_BAR_HEIGHT, 0.0F);
		renderer.vertex(progressBarX + progress, progressBarY + PROGRESS_BAR_HEIGHT, 0.0F);
		renderer.vertex(progressBarX + progress, progressBarY, 0.0F);

		renderer.end();

		// Re-enable texture rendering
		GL11.glEnable(3553);
	}
}
