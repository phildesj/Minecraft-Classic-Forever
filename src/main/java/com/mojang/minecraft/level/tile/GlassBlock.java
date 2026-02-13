package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;

/**
 * Represents a glass block in Minecraft Classic.
 * Glass blocks are transparent, non-solid blocks that allow light to pass through.
 * They have special rendering logic to avoid rendering glass faces between adjacent glass blocks.
 * The showNeighborSides flag controls whether neighboring block sides should be rendered through the glass.
 *
 * @author Mojang
 */
public final class GlassBlock extends Block {

	/**
	 * Controls whether to render the sides of neighboring blocks through this glass block.
	 * When false, glass faces between adjacent glass blocks are culled (not rendered).
	 * When true, all neighboring block sides are rendered.
	 */
	private boolean showNeighborSides = false;

	/**
	 * Constructs a GlassBlock with the specified block ID and texture ID.
	 * Uses hardcoded block ID 20 and texture ID 49 for the glass block.
	 * The shouldShowNeighborSides parameter is provided but not used, defaulting to false.
	 *
	 * @param blockId the block ID parameter (unused, fixed to 20)
	 * @param textureId the texture ID parameter (unused, fixed to 49)
	 * @param shouldShowNeighborSides the showNeighborSides parameter (unused, defaults to false)
	 */
	protected GlassBlock(int blockId, int textureId, boolean shouldShowNeighborSides) {
		super(20, 49);
	}

	/**
	 * Determines if this glass block is solid.
	 * Glass blocks are not solid - entities can pass through them.
	 *
	 * @return false, indicating glass is not solid
	 */
	@Override
	public final boolean isSolid() {
		return false;
	}

	/**
	 * Determines if a particular side of this glass block should be rendered.
	 * Culls (skips rendering) glass faces between adjacent glass blocks to improve performance.
	 * For non-glass adjacent blocks, uses the default rendering logic.
	 *
	 * @param level the level containing this glass block
	 * @param x the x coordinate of the adjacent block
	 * @param y the y coordinate of the adjacent block
	 * @param z the z coordinate of the adjacent block
	 * @param side the side direction (0-5)
	 * @return false if rendering glass-to-glass faces and showNeighborSides is false, true otherwise
	 */
	@Override
	public final boolean canRenderSide(Level level, int x, int y, int z, int side) {
		int adjacentBlockId = level.getTile(x, y, z);
		return !this.showNeighborSides && adjacentBlockId == this.id?false:super.canRenderSide(level, x, y, z, side);
	}

	/**
	 * Determines if this glass block is opaque.
	 * Glass blocks are not opaque - they are transparent and allow light to pass through.
	 *
	 * @return false, indicating glass is transparent
	 */
	@Override
	public final boolean isOpaque() {
		return false;
	}
}


