package com.mojang.minecraft.gui;

import com.mojang.minecraft.render.ShapeRenderer;
import org.lwjgl.opengl.GL11;

public class Screen {

	/**
	 * Position on the Z axis for images.
	 */
	protected float imgZ = 0.0F;

	/**
	 * Draws a colored box.
	 *
	 * @param x1    Starting X coordinate.
	 * @param y1    Starting Y coordinate.
	 * @param x2    Ending X coordinate.
	 * @param y2    Ending Y coordinate.
	 * @param color Color of the box in ARGB format.
	 */
	protected static void drawBox(int x1, int y1, int x2, int y2, int color) {
		float alpha = (float) (color >>> 24) / 255.0F;
		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;

		ShapeRenderer shapeRenderer = ShapeRenderer.instance;

		GL11.glEnable(3042); // GL_BLEND
		GL11.glDisable(3553); // GL_TEXTURE_2D
		GL11.glBlendFunc(770, 771); // GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
		GL11.glColor4f(red, green, blue, alpha);

		shapeRenderer.begin();
		shapeRenderer.vertex((float) x1, (float) y2, 0.0F);
		shapeRenderer.vertex((float) x2, (float) y2, 0.0F);
		shapeRenderer.vertex((float) x2, (float) y1, 0.0F);
		shapeRenderer.vertex((float) x1, (float) y1, 0.0F);
		shapeRenderer.end();

		GL11.glEnable(3553); // GL_TEXTURE_2D
		GL11.glDisable(3042); // GL_BLEND
	}

	/**
	 * Draws a box with a vertical gradient.
	 *
	 * @param x1          Starting X coordinate.
	 * @param y1          Starting Y coordinate.
	 * @param x2          Ending X coordinate.
	 * @param y2          Ending Y coordinate.
	 * @param topColor    Color at the top of the box in ARGB format.
	 * @param bottomColor Color at the bottom of the box in ARGB format.
	 */
	protected static void drawFadingBox(int x1, int y1, int x2, int y2, int topColor, int bottomColor) {
		float topAlpha = (float) (topColor >>> 24) / 255.0F;
		float topRed = (float) (topColor >> 16 & 255) / 255.0F;
		float topGreen = (float) (topColor >> 8 & 255) / 255.0F;
		float topBlue = (float) (topColor & 255) / 255.0F;

		float bottomAlpha = (float) (bottomColor >>> 24) / 255.0F;
		float bottomRed = (float) (bottomColor >> 16 & 255) / 255.0F;
		float bottomGreen = (float) (bottomColor >> 8 & 255) / 255.0F;
		float bottomBlue = (float) (bottomColor & 255) / 255.0F;

		GL11.glDisable(3553); // GL_TEXTURE_2D
		GL11.glEnable(3042); // GL_BLEND
		GL11.glBlendFunc(770, 771); // GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
		GL11.glBegin(7); // GL_QUADS

		GL11.glColor4f(topRed, topGreen, topBlue, topAlpha);
		GL11.glVertex2f((float) x2, (float) y1);
		GL11.glVertex2f((float) x1, (float) y1);

		GL11.glColor4f(bottomRed, bottomGreen, bottomBlue, bottomAlpha);
		GL11.glVertex2f((float) x1, (float) y2);
		GL11.glVertex2f((float) x2, (float) y2);

		GL11.glEnd();
		GL11.glDisable(3042); // GL_BLEND
		GL11.glEnable(3553); // GL_TEXTURE_2D
	}

	/**
	 * Renders a string centered on the X axis.
	 *
	 * @param fontRenderer Font renderer to use.
	 * @param text         Text to render.
	 * @param x            X coordinate of the center.
	 * @param y            Y coordinate.
	 * @param color        Text color.
	 */
	public static void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color) {
		fontRenderer.render(text, x - fontRenderer.getWidth(text) / 2, y, color);
	}

	/**
	 * Renders a string.
	 *
	 * @param fontRenderer Font renderer to use.
	 * @param text         Text to render.
	 * @param x            X coordinate.
	 * @param y            Y coordinate.
	 * @param color        Text color.
	 */
	public static void drawString(FontRenderer fontRenderer, String text, int x, int y, int color) {
		fontRenderer.render(text, x, y, color);
	}

	/**
	 * Draws a portion of the currently bound texture.
	 *
	 * @param x      X coordinate on the screen.
	 * @param y      Y coordinate on the screen.
	 * @param u      X coordinate in the texture.
	 * @param v      Y coordinate in the texture.
	 * @param width  Width of the image.
	 * @param height Height of the image.
	 */
	public final void drawImage(int x, int y, int u, int v, int width, int height) {
		float uScale = 0.00390625F; // 1/256
		float vScale = 0.00390625F; // 1/256

		ShapeRenderer shapeRenderer = ShapeRenderer.instance;
		shapeRenderer.begin();
		shapeRenderer.vertexUV((float) x, (float) (y + height), this.imgZ, (float) u * uScale, (float) (v + height) * vScale);
		shapeRenderer.vertexUV((float) (x + width), (float) (y + height), this.imgZ, (float) (u + width) * uScale, (float) (v + height) * vScale);
		shapeRenderer.vertexUV((float) (x + width), (float) y, this.imgZ, (float) (u + width) * uScale, (float) v * vScale);
		shapeRenderer.vertexUV((float) x, (float) y, this.imgZ, (float) u * uScale, (float) v * vScale);
		shapeRenderer.end();
	}
}
