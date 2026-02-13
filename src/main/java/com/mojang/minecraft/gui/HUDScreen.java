package com.mojang.minecraft.gui;

import com.mojang.minecraft.ChatLine;
import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.player.Inventory;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * Handles the rendering of the Heads-Up Display (HUD) in-game.
 */
public final class HUDScreen extends Screen {

	/** List of chat history. */
	public List<ChatLine> chat = new ArrayList<>();
	/** Random instance for UI effects like heart jitter. */
	private final Random random = new Random();
	/** Reference to the Minecraft instance. */
	private final Minecraft mc;
	/** The width of the HUD. */
	private final int width;
	/** The height of the HUD. */
	private final int height;
	/** The name of the player currently hovered in the player list. */
	public String hoveredPlayer = null;
	/** Ticks elapsed since the HUD was created. */
	public int ticks = 0;

	/**
	 * Creates a new HUD screen.
	 *
	 * @param mc     The Minecraft instance.
	 * @param width  The window width.
	 * @param height The window height.
	 */
	public HUDScreen(Minecraft mc, int width, int height) {
		this.mc = mc;
		this.width = width * 240 / height;
		this.height = height * 240 / height;
	}

	/**
	 * Renders the HUD.
	 *
	 * @param partialTicks Time elapsed since last tick (for interpolation).
	 * @param hasMouseOver Whether the mouse is currently over a GUI element.
	 * @param mouseX       Mouse X coordinate.
	 * @param mouseY       Mouse Y coordinate.
	 */
	public void render(float partialTicks, boolean hasMouseOver, int mouseX, int mouseY) {
		FontRenderer fontRenderer = this.mc.fontRenderer;
		this.mc.renderer.enableGuiMode();
		TextureManager textureManager = this.mc.textureManager;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureManager.load("/gui/gui.png"));
		ShapeRenderer shapeRenderer = ShapeRenderer.instance;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_BLEND);

		Inventory inventory = this.mc.player.inventory;
		this.imgZ = -90.0F;

		// Render Hotbar
		this.drawImage(this.width / 2 - 91, this.height - 22, 0, 0, 182, 22);
		this.drawImage(this.width / 2 - 91 - 1 + inventory.selected * 20, this.height - 22 - 1, 0, 22, 24, 22);

		// Render Crosshair
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureManager.load("/gui/icons.png"));
		this.drawImage(this.width / 2 - 7, this.height / 2 - 7, 0, 0, 16, 16);

		boolean flickerHearts = this.mc.player.invulnerableTime / 3 % 2 == 1;
		if (this.mc.player.invulnerableTime < 10) {
			flickerHearts = false;
		}

		int health = this.mc.player.health;
		int lastHealth = this.mc.player.lastHealth;
		this.random.setSeed((long) this.ticks * 312871);

		// Render Survival UI elements (Health, Air)
		if (this.mc.gamemode.isSurvival()) {
			for (int i = 0; i < 10; ++i) {
				byte yOffset = 0;
				if (flickerHearts) {
					yOffset = 1;
				}

				int x = this.width / 2 - 91 + (i << 3);
				int y = this.height - 32;
				if (health <= 4) {
					y += this.random.nextInt(2);
				}

				this.drawImage(x, y, 16 + yOffset * 9, 0, 9, 9);
				if (flickerHearts) {
					if ((i << 1) + 1 < lastHealth) {
						this.drawImage(x, y, 70, 0, 9, 9);
					}

					if ((i << 1) + 1 == lastHealth) {
						this.drawImage(x, y, 79, 0, 9, 9);
					}
				}

				if ((i << 1) + 1 < health) {
					this.drawImage(x, y, 52, 0, 9, 9);
				}

				if ((i << 1) + 1 == health) {
					this.drawImage(x, y, 61, 0, 9, 9);
				}
			}

			if (this.mc.player.isUnderWater()) {
				int airAmount = (int) Math.ceil((double) (this.mc.player.airSupply - 2) * 10.0D / 300.0D);
				int extraAir = (int) Math.ceil((double) this.mc.player.airSupply * 10.0D / 300.0D) - airAmount;

				for (int i = 0; i < airAmount + extraAir; ++i) {
					if (i < airAmount) {
						this.drawImage(this.width / 2 - 91 + (i << 3), this.height - 32 - 9, 16, 18, 9, 9);
					} else {
						this.drawImage(this.width / 2 - 91 + (i << 3), this.height - 32 - 9, 25, 18, 9, 9);
					}
				}
			}
		}

		GL11.glDisable(GL11.GL_BLEND);

		// Render Hotbar Items
		for (int i = 0; i < inventory.slots.length; ++i) {
			int x = this.width / 2 - 90 + i * 20;
			int y = this.height - 16;
			int blockId = inventory.slots[i];
			if (blockId > 0) {
				GL11.glPushMatrix();
				GL11.glTranslatef((float) x, (float) y, -50.0F);

				// Item pop-up animation
				if (inventory.popTime[i] > 0) {
					float popPercent = ((float) inventory.popTime[i] - partialTicks) / 5.0F;
					float yAnimOffset = -MathHelper.sin(popPercent * popPercent * 3.1415927F) * 8.0F;
					float scaleX = MathHelper.sin(popPercent * popPercent * 3.1415927F) + 1.0F;
					float scaleY = MathHelper.sin(popPercent * 3.1415927F) + 1.0F;
					GL11.glTranslatef(10.0F, yAnimOffset + 10.0F, 0.0F);
					GL11.glScalef(scaleX, scaleY, 1.0F);
					GL11.glTranslatef(-10.0F, -10.0F, 0.0F);
				}

				// Render Block Preview
				GL11.glScalef(10.0F, 10.0F, 10.0F);
				GL11.glTranslatef(1.0F, 0.5F, 0.0F);
				GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(-1.5F, 0.5F, 0.5F);
				GL11.glScalef(-1.0F, -1.0F, -1.0F);

				int terrainTexture = textureManager.load("/terrain.png");
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrainTexture);
				shapeRenderer.begin();
				Block.blocks[blockId].renderFullbright(shapeRenderer);
				shapeRenderer.end();
				GL11.glPopMatrix();

				// Item count
				if (inventory.count[i] > 1) {
					String countStr = "" + inventory.count[i];
					fontRenderer.render(countStr, x + 19 - fontRenderer.getWidth(countStr), y + 6, 16777215);
				}
			}
		}

		// Miscellaneous info (Version, FPS, Survival stats)
		fontRenderer.render("0.30", 2, 2, 16777215);
		if (this.mc.settings.showFrameRate) {
			fontRenderer.render(this.mc.debug, 2, 12, 16777215);
		}

		if (this.mc.gamemode instanceof SurvivalGameMode) {
			String score = "Score: &e" + this.mc.player.getScore();
			fontRenderer.render(score, this.width - fontRenderer.getWidth(score) - 2, 2, 16777215);
			fontRenderer.render("Arrows: " + this.mc.player.arrows, this.width / 2 + 8, this.height - 33, 16777215);
		}

		// Rendering chat
		int visibleChatLines = 10;
		boolean isChatVisible = false;
		if (this.mc.currentScreen instanceof ChatInputScreen) {
			visibleChatLines = 20;
			isChatVisible = true;
		}

		for (int i = 0; i < this.chat.size() && i < visibleChatLines; ++i) {
			if (this.chat.get(i).time < 200 || isChatVisible) {
				fontRenderer.render(this.chat.get(i).message, 2, this.height - 8 - i * 9 - 20, 16777215);
			}
		}

		// Render Player List (TAB)
		int centerX = this.width / 2;
		int centerY = this.height / 2;
		this.hoveredPlayer = null;

		if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && this.mc.networkManager != null && this.mc.networkManager.isConnected()) {
			List<?> playerNames = this.mc.networkManager.getPlayers();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			// Background box for player list
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.7F);
			GL11.glVertex2f((float) (centerX + 128), (float) (centerY - 68 - 12));
			GL11.glVertex2f((float) (centerX - 128), (float) (centerY - 68 - 12));
			GL11.glColor4f(0.2F, 0.2F, 0.2F, 0.8F);
			GL11.glVertex2f((float) (centerX - 128), (float) (centerY + 68));
			GL11.glVertex2f((float) (centerX + 128), (float) (centerY + 68));
			GL11.glEnd();

			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			String header = "Connected players:";
			fontRenderer.render(header, centerX - fontRenderer.getWidth(header) / 2, centerY - 64 - 12, 16777215);

			for (int i = 0; i < playerNames.size(); ++i) {
				int playerX = centerX + i % 2 * 120 - 120;
				int playerY = centerY - 64 + (i / 2 << 3);
				String playerName = (String) playerNames.get(i);

				if (hasMouseOver && mouseX >= playerX && mouseY >= playerY && mouseX < playerX + 120 && mouseY < playerY + 8) {
					this.hoveredPlayer = playerName;
					fontRenderer.renderNoShadow(playerName, playerX + 2, playerY, 16777215);
				} else {
					fontRenderer.renderNoShadow(playerName, playerX, playerY, 15658734);
				}
			}
		}
	}

	/**
	 * Adds a message to the chat display.
	 *
	 * @param message The message to add.
	 */
	public void addChat(String message) {
		this.chat.add(0, new ChatLine(message));

		// Keep only the last 50 messages
		while (this.chat.size() > 50) {
			this.chat.remove(this.chat.size() - 1);
		}
	}
}






