package com.mojang.minecraft.item;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.particle.SmokeParticle;
import com.mojang.minecraft.particle.TerrainParticle;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;
import java.util.Random;
import org.lwjgl.opengl.GL11;

/**
 * Represents a Primed TNT entity that counts down and explodes.
 */
public class PrimedTnt extends Entity {

	/** Serial version UID for serialization. */
	private static final long serialVersionUID = 0L;

	/** Ticks remaining until explosion. */
	public int life;

	/** Random instance for explosion effects. */
	private static final Random random = new Random();

	/** Whether the TNT has been defused. */
	private boolean defused;

	/**
	 * Creates a new PrimedTNT entity.
	 *
	 * @param level The level the TNT is in.
	 * @param x     Initial X position.
	 * @param y     Initial Y position.
	 * @param z     Initial Z position.
	 */
	public PrimedTnt(Level level, float x, float y, float z) {
		super(level);

		setSize(0.98F, 0.98F);

		heightOffset = bbHeight / 2.0F;

		setPos(x, y, z);

		// Random horizontal velocity
		float angle = (float) (Math.random() * Math.PI * 2.0D);

		// Note: The original code used degrees conversion (3.14 / 180) on a value that was already in radians (random * 2PI).
		// This resulted in very small velocities. I will keep the logic equivalent but clean it up.
		xd = -MathHelper.sin(angle * (float) Math.PI / 180.0F) * 0.02F;
		yd = 0.2F;
		zd = -MathHelper.cos(angle * (float) Math.PI / 180.0F) * 0.02F;

		makeStepSound = false;

		life = 40;

		xo = x;
		yo = y;
		zo = z;
	}

	@Override
	public void tick() {
		xo = x;
		yo = y;
		zo = z;

		// Apply gravity
		yd -= 0.04F;

		move(xd, yd, zd);

		// Apply friction
		xd *= 0.98F;
		yd *= 0.98F;
		zd *= 0.98F;

		if (onGround) {
			xd *= 0.7F;
			zd *= 0.7F;
			yd *= -0.5F; // Bounce
		}

		if (!defused) {
			if (life-- > 0) {
				// Spawn smoke while burning
				SmokeParticle smokeParticle = new SmokeParticle(level, x, y + 0.6F, z);
				level.particleEngine.spawnParticle(smokeParticle);
			} else {
				// Explode!
				remove();

				float radius = 4.0F;

				level.explode(null, x, y, z, radius);

				// Spawn explosion particles
				for (int i = 0; i < 100; i++) {
					float dx = (float) random.nextGaussian() * radius / 4.0F;
					float dy = (float) random.nextGaussian() * radius / 4.0F;
					float dz = (float) random.nextGaussian() * radius / 4.0F;
					float distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
					float vx = dx / distance / distance;
					float vy = dy / distance / distance;
					float vz = dz / distance / distance;

					TerrainParticle terrainParticle = new TerrainParticle(level, x + dx, y + dy, z + dz, vx, vy, vz, Block.TNT);
					level.particleEngine.spawnParticle(terrainParticle);
				}
			}
		}
	}

	@Override
	public void render(TextureManager textureManager, float partialTicks) {
		int textureID = textureManager.load("/terrain.png");

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

		float brightness = level.getBrightness((int) x, (int) y, (int) z);

		GL11.glPushMatrix();
		GL11.glColor4f(brightness, brightness, brightness, 1.0F);

		// Interpolate position
		float renderX = xo + (x - xo) * partialTicks - 0.5F;
		float renderY = yo + (y - yo) * partialTicks - 0.5F;
		float renderZ = zo + (z - zo) * partialTicks - 0.5F;

		GL11.glTranslatef(renderX, renderY, renderZ);
		GL11.glPushMatrix();

		ShapeRenderer shapeRenderer = ShapeRenderer.instance;

		// Render the base TNT block
		Block.TNT.renderPreview(shapeRenderer);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);

		// Flash effect
		float flashAlpha = (float) ((life / 4 + 1) % 2) * 0.4F;

		if (life <= 16) {
			flashAlpha = (float) ((life + 1) % 2) * 0.6F;
		}

		if (life <= 2) {
			flashAlpha = 0.9F;
		}

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, flashAlpha);

		// Render the flash overlay
		Block.TNT.renderPreview(shapeRenderer);

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}

	@Override
	public void playerTouch(Entity entity) {
		// No default touch behavior for primed TNT
	}

	@Override
	public void hurt(Entity entity, int damage) {
		if (!removed) {
			super.hurt(entity, damage);

			if (entity instanceof Player) {
				remove();

				// Drop TNT item when hit by player
				Item item = new Item(level, x, y, z, Block.TNT.id);
				level.addEntity(item);
			}
		}
	}

	@Override
	public boolean isPickable() {
		return !this.removed;
	}
}
