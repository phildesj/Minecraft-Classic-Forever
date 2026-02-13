package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;

/**
 * Represents a base class for leaf blocks in Minecraft Classic.
 * Leaf blocks are non-solid, transparent blocks that form the foliage of trees.
 * They have special rendering logic to control whether neighboring block sides are rendered through them.
 * The showNeighborSides flag determines if leaves render adjacent block faces.
 *
 * @author Mojang
 */
public class LeavesBaseBlock extends Block {

	/**
	 * Controls whether to render the sides of neighboring blocks through this leaf block.
	 * When true, neighboring block sides are rendered through the leaves (transparent).
	 * When false, leaf faces between adjacent leaf blocks are culled (not rendered).
	 */
	private boolean showNeighborSides = true;

	/**
	 * Constructs a LeavesBaseBlock with the specified block ID and texture ID.
	 * Initializes the leaf block with a configurable texture and transparency settings.
	 *
	 * @param blockId the unique block ID for this leaf type
	 * @param textureId the texture ID for this leaf's appearance
	 * @param shouldShowNeighborSides whether to render neighboring block sides through leaves (unused)
	 */
	protected LeavesBaseBlock(int blockId, int textureId, boolean shouldShowNeighborSides) {
		super(blockId, textureId);
	}

	/**
	 * Determines if this leaf block is solid.
	 * Leaf blocks are not solid - entities can pass through them without collision.
	 *
	 * @return false, indicating leaf blocks are not solid
	 */
	@Override
	public final boolean isSolid() {
		return false;
	}

	/**
	 * Determines if a particular side of this leaf block should be rendered.
	 * Culls (skips rendering) leaf faces between adjacent leaf blocks to improve performance.
	 * For non-leaf adjacent blocks, uses the default rendering logic.
	 *
	 * @param level the level containing this leaf block
	 * @param x the x coordinate of the adjacent block
	 * @param y the y coordinate of the adjacent block
	 * @param z the z coordinate of the adjacent block
	 * @param side the side direction (0-5)
	 * @return false if rendering leaf-to-leaf faces and showNeighborSides is false, true otherwise
	 */
	@Override
	public final boolean canRenderSide(Level level, int x, int y, int z, int side) {
		int adjacentBlockId = level.getTile(x, y, z);
		return !this.showNeighborSides && adjacentBlockId == this.id?false:super.canRenderSide(level, x, y, z, side);
	}

	/**
	 * Determines if this leaf block is opaque.
	 * Leaf blocks are not opaque - they are transparent and allow light to pass through.
	 *
	 * @return false, indicating leaf blocks are transparent
	 */
	@Override
	public final boolean isOpaque() {
		return false;
	}
}





