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
	 * @param var1 the unique block ID for this flower type
	 * @param var2 the texture ID for this flower's appearance
	 */
	protected FlowerBlock(int var1, int var2) {
		super(var1);
		this.textureId = var2;
		this.setPhysics(true);
		float var3 = 0.2F;
		this.setBounds(0.5F - var3, 0.0F, 0.5F - var3, var3 + 0.5F, var3 * 3.0F, var3 + 0.5F);
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
			int var6 = level.getTile(x, y - 1, z);
			if(!level.isLit(x, y, z) || var6 != Block.DIRT.id && var6 != Block.GRASS.id) {
				level.setTile(x, y, z, 0);
			}

		}
	}

	/**
	 * Renders the flower as a cross-shaped set of two overlapping quads.
	 * The flower is rendered with proper texture coordinates and positioned in world space.
	 *
	 * @param var1 the shape renderer to use for drawing
	 * @param var2 the x coordinate offset for rendering
	 * @param var3 the y coordinate offset for rendering
	 * @param var4 the z coordinate offset for rendering
	 */
	private void render(ShapeRenderer var1, float var2, float var3, float var4) {
		// Get texture coordinates from the texture atlas
		int var15;
		int var5 = (var15 = this.getTextureId(15)) % 16 << 4;
		int var6 = var15 / 16 << 4;
		float var16 = (float)var5 / 256.0F;
		float var17 = ((float)var5 + 15.99F) / 256.0F;
		float var7 = (float)var6 / 256.0F;
		float var18 = ((float)var6 + 15.99F) / 256.0F;

		// Render two overlapping quads at 45 degree angles to create a cross shape
		for(int var8 = 0; var8 < 2; ++var8) {
			float var9 = (float)((double)MathHelper.sin((float)var8 * 3.1415927F / 2.0F + 0.7853982F) * 0.5D);
			float var10 = (float)((double)MathHelper.cos((float)var8 * 3.1415927F / 2.0F + 0.7853982F) * 0.5D);
			float var11 = var2 + 0.5F - var9;
			var9 += var2 + 0.5F;
			float var13 = var3 + 1.0F;
			float var14 = var4 + 0.5F - var10;
			var10 += var4 + 0.5F;
			var1.vertexUV(var11, var13, var14, var17, var7);
			var1.vertexUV(var9, var13, var10, var16, var7);
			var1.vertexUV(var9, var3, var10, var16, var18);
			var1.vertexUV(var11, var3, var14, var17, var18);
			var1.vertexUV(var9, var13, var10, var17, var7);
			var1.vertexUV(var11, var13, var14, var16, var7);
			var1.vertexUV(var11, var3, var14, var16, var18);
			var1.vertexUV(var9, var3, var10, var17, var18);
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
	 * @param var1 the shape renderer to use for drawing
	 */
	@Override
	public final void renderPreview(ShapeRenderer var1) {
		var1.normal(0.0F, 1.0F, 0.0F);
		var1.begin();
		this.render(var1, 0.0F, 0.4F, -0.3F);
		var1.end();
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
	 * @param var1 the level containing this flower
	 * @param var2 the x coordinate of the flower
	 * @param var3 the y coordinate of the flower
	 * @param var4 the z coordinate of the flower
	 * @param var5 the shape renderer to use for drawing
	 * @return true if the flower was rendered, false otherwise
	 */
	@Override
	public final boolean render(Level var1, int var2, int var3, int var4, ShapeRenderer var5) {
		float var6 = var1.getBrightness(var2, var3, var4);
		var5.color(var6, var6, var6);
		this.render(var5, (float)var2, (float)var3, (float)var4);
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









