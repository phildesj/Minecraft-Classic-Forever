package com.mojang.minecraft.item;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;
import java.util.List;
import org.lwjgl.opengl.GL11;

/**
 * Represents an arrow entity shot by a player or a mob.
 */
public class Arrow extends Entity {

	/** Serial version UID for serialization. */
	private static final long serialVersionUID = 0L;

	private float yRot;
	private float xRot;
	private float yRotO;
	private float xRotO;

	private boolean hasHit = false;

	/** Number of ticks the arrow has been stuck in a block. */
	private int stickTime = 0;

	/** The entity that shot this arrow. */
	private final Entity owner;

	/** Total ticks the arrow has existed. */
	private int time = 0;
	/** The type of arrow (0 for player-shot, 1 for others). */
	private int type = 0;

	private float gravity;

	private int damage;

	/**
	 * Creates a new Arrow entity.
	 *
	 * @param level  The level the arrow belongs to.
	 * @param owner  The entity that shot the arrow.
	 * @param x      Initial X position.
	 * @param y      Initial Y position.
	 * @param z      Initial Z position.
	 * @param yRot   Initial horizontal rotation.
	 * @param xRot   Initial vertical rotation.
	 * @param speed  The initial speed/force of the arrow.
	 */
	public Arrow(Level level, Entity owner, float x, float y, float z, float yRot, float xRot, float speed) {
		super(level);

		this.owner = owner;

		setSize(0.3F, 0.5F);

		heightOffset = bbHeight / 2.0F;
		damage = 3;

		if (!(owner instanceof Player)) {
			type = 1;
		} else {
			damage = 7;
		}

		heightOffset = 0.25F;

		float cosY = MathHelper.cos(-yRot * 0.017453292F - 3.1415927F);
		float sinY = MathHelper.sin(-yRot * 0.017453292F - 3.1415927F);

		float cosX = MathHelper.cos(-xRot * 0.017453292F);
		float sinX = MathHelper.sin(-xRot * 0.017453292F);

		slide = false;

		gravity = 1.0F / speed;

		xo -= cosY * 0.2F;
		zo += sinY * 0.2F;

		this.x -= cosY * 0.2F;
		this.z += sinY * 0.2F;

		xd = sinY * cosX * speed;
		yd = sinX * speed;
		zd = cosY * cosX * speed;

		setPos(this.x, y, this.z);

		float xzDist = MathHelper.sqrt(xd * xd + zd * zd);

		yRotO = this.yRot = (float) (Math.atan2(xd, zd) * 180.0D / Math.PI);
		xRotO = this.xRot = (float) (Math.atan2(yd, xzDist) * 180.0D / Math.PI);

		makeStepSound = false;
	}

	@Override
	public void tick() {
		time++;

		xRotO = xRot;
		yRotO = yRot;

		xo = x;
		yo = y;
		zo = z;

		if (hasHit) {
			stickTime++;

			if (type == 0) {
				// Player arrows stay for a while then randomly despawn
				if (stickTime >= 300 && Math.random() < 0.01D) {
					remove();
				}
			} else if (type == 1 && stickTime >= 20) {
				// Mob arrows despawn quickly
				remove();
			}
		} else {
			xd *= 0.998F;
			yd *= 0.998F;
			zd *= 0.998F;

			yd -= 0.02F * gravity;

			// Sub-step movement for collision accuracy
			int steps = (int) (MathHelper.sqrt(xd * xd + yd * yd + zd * zd) / 0.2F + 1.0F);

			float stepX = xd / (float) steps;
			float stepY = yd / (float) steps;
			float stepZ = zd / (float) steps;

			for (int i = 0; i < steps && !collision; i++) {
				AABB collisionAABB = bb.expand(stepX, stepY, stepZ);

				if (!level.getCubes(collisionAABB).isEmpty()) {
					collision = true;
				}

				List<Entity> blockMapEntitiesList = level.blockMap.getEntities(this, collisionAABB);

				for (Entity entity : blockMapEntitiesList) {
					if (entity.isShootable() && (entity != owner || time > 5)) {
						entity.hurt(this, damage);

						collision = true;

						remove();

						return;
					}
				}

				if (!collision) {
					bb.move(stepX, stepY, stepZ);

					x += stepX;
					y += stepY;
					z += stepZ;

					blockMap.moved(this);
				}
			}

			if (collision) {
				hasHit = true;

				xd = yd = zd = 0.0F;
			}

			if (!hasHit) {
				float xzDist = MathHelper.sqrt(xd * xd + zd * zd);

				yRot = (float) (Math.atan2(xd, zd) * 180.0D / Math.PI);

				xRot = (float) (Math.atan2(yd, xzDist) * 180.0D / Math.PI);

				// Ensure rotation interpolates correctly
				while (xRot - xRotO < -180.0F) {
					xRotO -= 360.0F;
				}

				while (xRot - xRotO >= 180.0F) {
					xRotO += 360.0F;
				}

				while (yRot - yRotO < -180.0F) {
					yRotO -= 360.0F;
				}

				while (yRot - yRotO >= 180.0F) {
					yRotO += 360.0F;
				}
			}
		}
	}

	@Override
	public void render(TextureManager textureManager, float partialTicks) {
		textureId = textureManager.load("/item/arrows.png");

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

		float brightness = level.getBrightness((int) x, (int) y, (int) z);

		GL11.glPushMatrix();
		GL11.glColor4f(brightness, brightness, brightness, 1.0F);
		GL11.glTranslatef(xo + (x - xo) * partialTicks, this.yo + (this.y - this.yo) * partialTicks - this.heightOffset / 2.0F, this.zo + (this.z - this.zo) * partialTicks);
		GL11.glRotatef(yRotO + (yRot - yRotO) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(xRotO + (xRot - xRotO) * partialTicks, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);

		ShapeRenderer shapeRenderer = ShapeRenderer.instance;

		float texSize = 0.5F;

		float v1 = (type * 10) / 32.0F;
		float v2 = (5 + type * 10) / 32.0F;
		float u2 = 0.15625F;

		float v3 = (5 + type * 10) / 32.0F;
		float v4 = (10 + type * 10) / 32.0F;
		float scale = 0.05625F;

		GL11.glScalef(scale, scale, scale);

		GL11.glNormal3f(scale, 0.0F, 0.0F);

		shapeRenderer.begin();
		shapeRenderer.vertexUV(-7.0F, -2.0F, -2.0F, 0.0F, v3);
		shapeRenderer.vertexUV(-7.0F, -2.0F, 2.0F, u2, v3);
		shapeRenderer.vertexUV(-7.0F, 2.0F, 2.0F, u2, v4);
		shapeRenderer.vertexUV(-7.0F, 2.0F, -2.0F, 0.0F, v4);
		shapeRenderer.end();

		GL11.glNormal3f(-scale, 0.0F, 0.0F);

		shapeRenderer.begin();
		shapeRenderer.vertexUV(-7.0F, 2.0F, -2.0F, 0.0F, v3);
		shapeRenderer.vertexUV(-7.0F, 2.0F, 2.0F, u2, v3);
		shapeRenderer.vertexUV(-7.0F, -2.0F, 2.0F, u2, v4);
		shapeRenderer.vertexUV(-7.0F, -2.0F, -2.0F, 0.0F, v4);
		shapeRenderer.end();

		for (int i = 0; i < 4; i++) {
			GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);

			GL11.glNormal3f(0.0F, -scale, 0.0F);

			shapeRenderer.begin();
			shapeRenderer.vertexUV(-8.0F, -2.0F, 0.0F, 0.0F, v1);
			shapeRenderer.vertexUV(8.0F, -2.0F, 0.0F, texSize, v1);
			shapeRenderer.vertexUV(8.0F, 2.0F, 0.0F, texSize, v2);
			shapeRenderer.vertexUV(-8.0F, 2.0F, 0.0F, 0.0F, v2);
			shapeRenderer.end();
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}

	@Override
	public void playerTouch(Entity entity) {
		if (!(entity instanceof Player)) {
			return;
		}

		Player player = (Player) entity;

		if (hasHit && owner == player && player.arrows < 99) {
			TakeEntityAnim takeEntityAnim = new TakeEntityAnim(level, this, player);

			level.addEntity(takeEntityAnim);

			player.arrows++;

			remove();
		}
	}

	@Override
	public void awardKillScore(Entity entity, int score) {
		owner.awardKillScore(entity, score);
	}

	/**
	 * Gets the entity that shot this arrow.
	 *
	 * @return The owner entity.
	 */
	public Entity getOwner() {
		return owner;
	}
}
