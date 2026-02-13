package com.mojang.minecraft.item;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class Item extends Entity {

	/** Serial version UID for serialization. */
	public static final long serialVersionUID = 0L;

	/** Models for each block type. */
	private static final ItemModel[] models = new ItemModel[256];

	/** Velocity on the X axis. */
	private float xd;
	/** Velocity on the Y axis. */
	private float yd;
	/** Velocity on the Z axis. */
	private float zd;

	/** Current rotation of the item. */
	private final float rot;

	/** The block ID this item represents. */
	private final int resource;

	/** Counter for ticks lived, used for animation. */
	private int tickCount;

	/** Total age of the item in ticks, used for despawning. */
	private int age = 0;

	/**
	 * Creates a new Item entity.
	 *
	 * @param level   The level the item is in.
	 * @param x       Initial X coordinate.
	 * @param y       Initial Y coordinate.
	 * @param z       Initial Z coordinate.
	 * @param blockId The block ID for this item.
	 */
	public Item(Level level, float x, float y, float z, int blockId) {
		super(level);

		setSize(0.25F, 0.25F);

		heightOffset = bbHeight / 2.0F;

		setPos(x, y, z);

		this.resource = blockId;

		// Random initial rotation
		rot = (float) (Math.random() * 360.0D);

		// Random initial velocity
		xd = (float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D);
		yd = 0.2F;
		zd = (float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D);

		makeStepSound = false;
	}

	/**
	 * Updates the item's state, including movement and despawning.
	 */
	@Override
	public void tick() {
		xo = x;
		yo = y;
		zo = z;

		// Apply gravity
		yd -= 0.04F;

		move(xd, yd, zd);

		// Apply air friction
		xd *= 0.98F;
		yd *= 0.98F;
		zd *= 0.98F;

		if (onGround) {
			xd *= 0.7F;
			zd *= 0.7F;
			yd *= -0.5F; // Bounce slightly on landing
		}

		tickCount++;
		age++;

		// Despawn after 5 minutes (6000 ticks)
		if (age >= 6000) {
			remove();
		}
	}

	/**
	 * Renders the item in the world.
	 *
	 * @param textureManager The texture manager.
	 * @param partialTicks   The fraction of a tick that has passed since the last update.
	 */
	@Override
	public void render(TextureManager textureManager, float partialTicks) {
		textureId = textureManager.load("/terrain.png");

		GL11.glBindTexture(3553, this.textureId); // GL_TEXTURE_2D

		float brightness = level.getBrightness((int) x, (int) y, (int) z);
		// Calculate total rotation
		float totalRotation = rot + ((float) tickCount + partialTicks) * 3.0F;

		GL11.glPushMatrix();
		GL11.glColor4f(brightness, brightness, brightness, 1.0F);

		// Bobbing effect
		float bobbing = MathHelper.sin(totalRotation / 10.0F) * 0.1F + 0.1F;

		// Interpolated position + bobbing
		GL11.glTranslatef(xo + (x - xo) * partialTicks, yo + (y - yo) * partialTicks + bobbing, zo + (z - zo) * partialTicks);
		GL11.glRotatef(totalRotation, 0.0F, 1.0F, 0.0F);

		// Render the block model
		models[resource].generateList();

		// Calculate alpha for a "glow" or translucent effect
		float alpha = MathHelper.sin(totalRotation / 10.0F);
		alpha = (alpha * 0.5F + 0.5F);
		alpha = alpha * alpha * alpha;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha * 0.4F);
		GL11.glDisable(3553); // GL_TEXTURE_2D
		GL11.glEnable(3042); // GL_BLEND
		GL11.glBlendFunc(770, 1); // GL_SRC_ALPHA, GL_ONE
		GL11.glDisable(3008); // GL_ALPHA_TEST

		// Render second pass with glow effect
		models[resource].generateList();

		GL11.glEnable(3008); // GL_ALPHA_TEST
		GL11.glDisable(3042); // GL_BLEND
		GL11.glBlendFunc(770, 771); // GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
		GL11.glEnable(3553); // GL_TEXTURE_2D
	}

	/**
	 * Called when a player touches the item to collect it.
	 *
	 * @param entity The entity that touched this item.
	 */
	@Override
	public void playerTouch(Entity entity) {
		Player player = (Player) entity;

		if (player.addResource(resource)) {
			// Add collection animation
			TakeEntityAnim takeEntityAnim = new TakeEntityAnim(level, this, player);
			level.addEntity(takeEntityAnim);

			remove();
		}
	}

	/**
	 * Initializes the item models for all available blocks.
	 */
	public static void initModels() {
		for (int i = 0; i < 256; i++) {
			Block block = Block.blocks[i];

			if (block != null) {
				models[i] = new ItemModel(block.textureId);
			}
		}

	}
}
