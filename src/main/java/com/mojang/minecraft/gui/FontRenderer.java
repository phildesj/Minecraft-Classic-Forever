package com.mojang.minecraft.gui;

import com.mojang.minecraft.GameSettings;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.TextureManager;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL11;

/**
 * Handles font loading and text rendering in the game.
 */
public final class FontRenderer {

	/** Map of character widths for all 256 possible byte values. */
	private final int[] widthMap = new int[256];
	/** The OpenGL texture ID for the font. */
	private final int fontTexture;
	/** Reference to the game settings for anaglyph 3D support. */
	private final GameSettings settings;

	/**
	 * Initializes the FontRenderer, loading the font texture and calculating character widths.
	 *
	 * @param settings       The game settings.
	 * @param fontName       The resource path to the font texture.
	 * @param textureManager The texture manager to load the font texture.
	 */
	public FontRenderer(GameSettings settings, String fontName, TextureManager textureManager) {
		this.settings = settings;

		BufferedImage fontImage;
		try (java.io.InputStream fontStream = TextureManager.class.getResourceAsStream(fontName)) {
			if (fontStream == null) {
				throw new IOException("Font resource not found: " + fontName);
			}
			fontImage = ImageIO.read(fontStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		int imgWidth = fontImage.getWidth();
		int imgHeight = fontImage.getHeight();
		int[] pixels = new int[imgWidth * imgHeight];
		fontImage.getRGB(0, 0, imgWidth, imgHeight, pixels, 0, imgWidth);

		// Calculate the width of each character by scanning the bitmap
		for (int i = 0; i < 128; ++i) {
			int column = i % 16;
			int row = i / 16;
			int charWidth = 0;

			for (boolean foundPixel = false; charWidth < 8 && !foundPixel; ++charWidth) {
				int x = (column << 3) + charWidth;
				foundPixel = true;

				for (int y = 0; y < 8; ++y) {
					int pixelIndex = ((row << 3) + y) * imgWidth;
					// Check if pixel is not transparent
					if ((pixels[x + pixelIndex] & 255) > 128) {
						foundPixel = false;
						break;
					}
				}
			}

			if (i == 32) { // Space character
				charWidth = 4;
			}

			this.widthMap[i] = charWidth;
		}

		this.fontTexture = textureManager.load(fontName);
	}

	/**
	 * Renders a string with a shadow.
	 *
	 * @param text  The string to render.
	 * @param x     The X coordinate.
	 * @param y     The Y coordinate.
	 * @param color The color of the text.
	 */
	public void render(String text, int x, int y, int color) {
		this.render(text, x + 1, y + 1, color, true);
		this.renderNoShadow(text, x, y, color);
	}

	/**
	 * Renders a string without a shadow.
	 *
	 * @param text  The string to render.
	 * @param x     The X coordinate.
	 * @param y     The Y coordinate.
	 * @param color The color of the text.
	 */
	public void renderNoShadow(String text, int x, int y, int color) {
		this.render(text, x, y, color, false);
	}

	/**
	 * Internal rendering method that handles both shadow and normal text.
	 *
	 * @param text   The string to render.
	 * @param x      The X coordinate.
	 * @param y      The Y coordinate.
	 * @param color  The color of the text.
	 * @param shadow Whether to render the text as a shadow (darker).
	 */
	private void render(String text, int x, int y, int color, boolean shadow) {
		if (text == null) {
			return;
		}

		char[] chars = text.toCharArray();
		if (shadow) {
			// Darken the color for shadow (mask out significant bits and shift)
			color = (color & 16579836) >> 2;
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.fontTexture);
		ShapeRenderer shapeRenderer = ShapeRenderer.instance;
		shapeRenderer.begin();
		shapeRenderer.color(color);

		int currentX = 0;
		for (int i = 0; i < chars.length; ++i) {
			// Handle color codes starting with '&'
			if (chars[i] == '&' && chars.length > i + 1) {
				int colorIndex = "0123456789abcdef".indexOf(chars[i + 1]);
				if (colorIndex < 0) {
					colorIndex = 15; // Reset to white if unknown
				}

				int br = (colorIndex & 8) << 3;
				int r = (colorIndex & 1) * 191 + br;
				int g = ((colorIndex & 2) >> 1) * 191 + br;
				int b = ((colorIndex & 4) >> 2) * 191 + br;

				if (this.settings.anaglyph) {
					int avgR = (b * 30 + g * 59 + r * 11) / 100;
					int avgG = (b * 30 + g * 70) / 100;
					int avgB = (b * 30 + r * 70) / 100;
					b = avgR;
					g = avgG;
					r = avgB;
				}

				color = b << 16 | g << 8 | r;
				i += 2;

				if (shadow) {
					color = (color & 16579836) >> 2;
				}

				shapeRenderer.color(color);
			}

			// Render the character as a quad
			int u = chars[i] % 16 << 3;
			int v = chars[i] / 16 << 3;
			float size = 7.99F;

			shapeRenderer.vertexUV((float) (x + currentX), (float) y + size, 0.0F, (float) u / 128.0F, ((float) v + size) / 128.0F);
			shapeRenderer.vertexUV((float) (x + currentX) + size, (float) y + size, 0.0F, ((float) u + size) / 128.0F, ((float) v + size) / 128.0F);
			shapeRenderer.vertexUV((float) (x + currentX) + size, (float) y, 0.0F, ((float) u + size) / 128.0F, (float) v / 128.0F);
			shapeRenderer.vertexUV((float) (x + currentX), (float) y, 0.0F, (float) u / 128.0F, (float) v / 128.0F);

			currentX += this.widthMap[chars[i]];
		}

		shapeRenderer.end();
	}

	/**
	 * Calculates the rendered width of a string, taking into account character widths and ignoring color codes.
	 *
	 * @param text The string to measure.
	 * @return The width in pixels.
	 */
	public int getWidth(String text) {
		if (text == null) {
			return 0;
		}

		char[] chars = text.toCharArray();
		int width = 0;

		for (int i = 0; i < chars.length; ++i) {
			if (chars[i] == '&') {
				++i; // Skip color code character
			} else {
				width += this.widthMap[chars[i]];
			}
		}

		return width;
	}

	/**
	 * Removes all color codes (starting with '&') from a string.
	 *
	 * @param text The string to strip colors from.
	 * @return The stripped string.
	 */
	public static String stripColor(String text) {
		if (text == null) {
			return null;
		}

		char[] chars = text.toCharArray();
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < chars.length; ++i) {
			if (chars[i] == '&') {
				++i; // Skip color code character
			} else {
				builder.append(chars[i]);
			}
		}

		return builder.toString();
	}
}
