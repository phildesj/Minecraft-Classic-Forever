package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.util.MathHelper;

import java.util.Random;

/**
 * Represents a flower block in Minecraft Classic.
 * Flowers are decorative, non-solid blocks that can be placed on grass and dirt.
 * They have a cross-shaped rendering made of two overlapping quads and do not have collision boxes.
 * Flowers will disappear if the block below is not dirt or grass, or if the area is not lit.
 *
 * @author Mojang
 */
public class FlowerBlock extends Block {

	/**
	 * Constructs a FlowerBlock with the specified block ID and texture ID.
	 * Sets up physics, bounds, and initializes the flower as a small cross-shaped block.
	 *
	 * @param blockId the unique block ID for this flower type
	 * @param textureId the texture ID for this flower's appearance
	 */
	protected FlowerBlock(int blockId, int textureId) {
		super(blockId);
		this.textureId = textureId;
		this.setPhysics(true);
		float size = 0.2F;
		this.setBounds(0.5F - size, 0.0F, 0.5F - size, size + 0.5F, size * 3.0F, size + 0.5F);
	}

	/**
	 * Updates the flower's state each game tick.
	 * Removes the flower if it's not on dirt/grass or if the area is not lit (unless tree growth is enabled).
	 *
	 * @param level the level containing this flower
	 * @param x the x coordinate of the flower
	 * @param y the y coordinate of the flower
	 * @param z the z coordinate of the flower
	 * @param rand a random number generator
	 */
	@Override
	public void update(Level level, int x, int y, int z, Random rand) {
		if(!level.growTrees) {
			int blockBelowId = level.getTile(x, y - 1, z);
			if(!level.isLit(x, y, z) || blockBelowId != Block.DIRT.id && blockBelowId != Block.GRASS.id) {
				level.setTile(x, y, z, 0);
			}

		}
	}

	/**
	 * Renders the flower as a cross-shaped set of two overlapping quads.
	 * The flower is rendered with proper texture coordinates and positioned in world space.
	 *
	 * @param shapeRenderer the shape renderer to use for drawing
	 * @param xOffset the x coordinate offset for rendering
	 * @param yOffset the y coordinate offset for rendering
	 * @param zOffset the z coordinate offset for rendering
	 */
	private void render(ShapeRenderer shapeRenderer, float xOffset, float yOffset, float zOffset) {
		// Get texture coordinates from the texture atlas
		int textureIndex;
		int texturePixelX = (textureIndex = this.getTextureId(15)) % 16 << 4;
		int texturePixelY = textureIndex / 16 << 4;
		float minU = (float)texturePixelX / 256.0F;
		float maxU = ((float)texturePixelX + 15.99F) / 256.0F;
		float minV = (float)texturePixelY / 256.0F;
		float maxV = ((float)texturePixelY + 15.99F) / 256.0F;

		// Render two overlapping quads at 45 degree angles to create a cross shape
		for(int quadIndex = 0; quadIndex < 2; ++quadIndex) {
			float sinValue = (float)((double)MathHelper.sin((float)quadIndex * 3.1415927F / 2.0F + 0.7853982F) * 0.5D);
			float cosValue = (float)((double)MathHelper.cos((float)quadIndex * 3.1415927F / 2.0F + 0.7853982F) * 0.5D);
			float x1 = xOffset + 0.5F - sinValue;
			float x2 = sinValue + xOffset + 0.5F;
			float y1 = yOffset + 1.0F;
			float z1 = zOffset + 0.5F - cosValue;
			float z2 = cosValue + zOffset + 0.5F;
			shapeRenderer.vertexUV(x1, y1, z1, maxU, minV);
			shapeRenderer.vertexUV(x2, y1, z2, minU, minV);
			shapeRenderer.vertexUV(x2, yOffset, z2, minU, maxV);
			shapeRenderer.vertexUV(x1, yOffset, z1, maxU, maxV);
			shapeRenderer.vertexUV(x2, y1, z2, maxU, minV);
			shapeRenderer.vertexUV(x1, y1, z1, minU, minV);
			shapeRenderer.vertexUV(x1, yOffset, z1, minU, maxV);
			shapeRenderer.vertexUV(x2, yOffset, z2, maxU, maxV);
		}

	}

	/**
	 * Determines if this flower block is opaque.
	 * Flowers are not opaque - they are transparent and allow light through.
	 *
	 * @return false, indicating flowers do not block light
	 */
	@Override
	public final boolean isOpaque() {
		return false;
	}

	/**
	 * Determines if this flower block is solid.
	 * Flowers are not solid - entities can pass through them.
	 *
	 * @return false, indicating flowers are not solid
	 */
	@Override
	public final boolean isSolid() {
		return false;
	}

	/**
	 * Renders a preview of this flower for inventory or UI display.
	 * Renders the flower with normal shading from above.
	 *
	 * @param shapeRenderer the shape renderer to use for drawing
	 */
	@Override
	public final void renderPreview(ShapeRenderer shapeRenderer) {
		shapeRenderer.normal(0.0F, 1.0F, 0.0F);
		shapeRenderer.begin();
		this.render(shapeRenderer, 0.0F, 0.4F, -0.3F);
		shapeRenderer.end();
	}

	/**
	 * Determines if this flower block is a cube.
	 * Flowers are cross-shaped, not cubic.
	 *
	 * @return false, indicating flowers are not cubes
	 */
	@Override
	public final boolean isCube() {
		return false;
	}

	/**
	 * Renders this flower in the world with proper lighting and brightness.
	 * Applies brightness calculations before rendering the cross shape.
	 *
	 * @param level the level containing this flower
	 * @param x the x coordinate of the flower
	 * @param y the y coordinate of the flower
	 * @param z the z coordinate of the flower
	 * @param shapeRenderer the shape renderer to use for drawing
	 * @return true if the flower was rendered, false otherwise
	 */
	@Override
	public final boolean render(Level level, int x, int y, int z, ShapeRenderer shapeRenderer) {
		float brightness = level.getBrightness(x, y, z);
		shapeRenderer.color(brightness, brightness, brightness);
		this.render(shapeRenderer, (float)x, (float)y, (float)z);
		return true;
	}

	/**
	 * Renders this flower at full brightness for preview or special effects.
	 * Used for inventory display and fullbright rendering modes.
	 *
	 * @param shapeRenderer the shape renderer to use for drawing
	 */
	@Override
	public final void renderFullbright(ShapeRenderer shapeRenderer) {
		shapeRenderer.color(1.0F, 1.0F, 1.0F);
		this.render(shapeRenderer, (float)-2, 0.0F, 0.0F);
	}

	/**
	 * Gets the collision box for this flower.
	 * Flowers have no collision - entities pass through them.
	 *
	 * @param x the x coordinate of the flower
	 * @param y the y coordinate of the flower
	 * @param z the z coordinate of the flower
	 * @return null, indicating no collision box
	 */
	@Override
	public AABB getCollisionBox(int x, int y, int z) {
		return null;
	}
}









