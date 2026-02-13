package com.mojang.minecraft.gui;

import com.mojang.minecraft.SessionData;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.TextureManager;
import org.lwjgl.opengl.GL11;

/**
 * A screen for selecting a block to put into the inventory.
 */
public final class BlockSelectScreen extends GuiScreen {

	/**
	 * Creates a new block selection screen.
	 */
	public BlockSelectScreen() {
		this.grabsMouse = true;
	}

	/**
	 * Gets the block index at the specified screen coordinates.
	 *
	 * @param mouseX The X coordinate of the mouse.
	 * @param mouseY The Y coordinate of the mouse.
	 * @return The index of the block at the coordinates, or -1 if no block is there.
	 */
	private int getBlockOnScreen(int mouseX, int mouseY) {
		for (int i = 0; i < SessionData.ALLOWED_BLOCKS.size(); ++i) {
			int x = this.width / 2 + i % 9 * 24 - 108 - 3;
			int y = this.height / 2 + i / 9 * 24 - 60 + 3;
			if (mouseX >= x && mouseX <= x + 24 && mouseY >= y - 12 && mouseY <= y + 12) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Renders the block selection screen.
	 *
	 * @param mouseX The X coordinate of the mouse.
	 * @param mouseY The Y coordinate of the mouse.
	 */
	@Override
	public void render(int mouseX, int mouseY) {
		int selectedBlock = this.getBlockOnScreen(mouseX, mouseY);
		// Draw the background box
		drawFadingBox(this.width / 2 - 120, 30, this.width / 2 + 120, 180, -1878719232, -1070583712);

		if (selectedBlock >= 0) {
			int x = this.width / 2 + selectedBlock % 9 * 24 - 108;
			int y = this.height / 2 + selectedBlock / 9 * 24 - 60;
			// Draw selection highlight
			drawFadingBox(x - 3, y - 8, x + 23, y + 24 - 6, -1862270977, -1056964609);
		}

		drawCenteredString(this.fontRenderer, "Select block", this.width / 2, 40, 16777215);

		TextureManager textureManager = this.minecraft.textureManager;
		ShapeRenderer shapeRenderer = ShapeRenderer.instance;
		int terrainTexture = textureManager.load("/terrain.png");
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrainTexture);

		for (int i = 0; i < SessionData.ALLOWED_BLOCKS.size(); ++i) {
			Block block = SessionData.ALLOWED_BLOCKS.get(i);
			GL11.glPushMatrix();
			int x = this.width / 2 + i % 9 * 24 - 108;
			int y = this.height / 2 + i / 9 * 24 - 60;
			GL11.glTranslatef((float) x, (float) y, 0.0F);
			GL11.glScalef(10.0F, 10.0F, 10.0F);
			GL11.glTranslatef(1.0F, 0.5F, 8.0F);
			GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);

			if (selectedBlock == i) {
				GL11.glScalef(1.6F, 1.6F, 1.6F);
			}

			GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
			GL11.glScalef(-1.0F, -1.0F, -1.0F);

			shapeRenderer.begin();
			block.renderFullbright(shapeRenderer);
			shapeRenderer.end();

			GL11.glPopMatrix();
		}

	}

	/**
	 * Handles mouse click events.
	 *
	 * @param mouseX The X coordinate of the mouse.
	 * @param mouseY The Y coordinate of the mouse.
	 * @param mouseButton The button that was clicked.
	 */
	@Override
	protected void onMouseClick(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			this.minecraft.player.inventory.replaceSlot(this.getBlockOnScreen(mouseX, mouseY));
			this.minecraft.setCurrentScreen(null);
		}

	}
}







