package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;

/**
 * Represents a slab block in Minecraft Classic.
 * Slabs are half-height blocks (0.5 units tall) that can be combined to form full-height blocks.
 * Two slabs stacked vertically automatically merge into a double slab (full-height block).
 * Single slabs are non-solid for collision purposes but double slabs are solid.
 * Slabs have different textures on top/bottom (texture 6) versus sides (texture 5).
 *
 * @author Mojang
 */
public final class SlabBlock extends Block {

	/**
	 * Indicates whether this is a double slab (full height) or single slab (half height).
	 * Double slabs are solid and full-height (1.0 unit).
	 * Single slabs are non-solid and half-height (0.5 units).
	 */
	private boolean isDoubleSlab;


	/**
	 * Constructs a SlabBlock with the specified block ID and slab type.
	 * All slabs use texture ID 6 for rendering.
	 * If this is a single slab (not double), sets bounds to half-height (0.5F).
	 *
	 * @param blockId the unique block ID for this slab type
	 * @param isDoubleSlab whether this is a double slab (true) or single slab (false)
	 */
	public SlabBlock(int blockId, boolean isDoubleSlab) {
		super(blockId, 6);
		this.isDoubleSlab = isDoubleSlab;
		// Single slabs have half height, double slabs have full height
		if(!isDoubleSlab) {
			this.setBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		}

	}

	/**
	 * Gets the texture ID for the specified face of the slab block.
	 * Top and bottom faces (indices 0-1) use texture 6 (slab top/bottom texture).
	 * Side faces (indices 2-5) use texture 5 (slab side texture).
	 * This creates visual distinction between different faces of the slab.
	 *
	 * @param faceIndex the face index (0=bottom, 1=top, 2-5=sides)
	 * @return the texture ID for the specified face (6 for top/bottom, 5 for sides)
	 */
	@Override
	protected final int getTextureId(int faceIndex) {
		return faceIndex <= 1?6:5;
	}

	/**
	 * Determines if this slab block is solid for collision purposes.
	 * Only double slabs are solid - single slabs allow entity movement through them.
	 *
	 * @return true if this is a double slab (solid), false if single slab (non-solid)
	 */
	@Override
	public final boolean isSolid() {
		return this.isDoubleSlab;
	}

	/**
	 * Called when an adjacent block changes.
	 * Single slabs (not double) perform no special action on neighbor change.
	 * Double slabs do not need neighbor change handling.
	 *
	 * @param level the level containing this slab
	 * @param x the x coordinate of this slab block
	 * @param y the y coordinate of this slab block
	 * @param z the z coordinate of this slab block
	 * @param neighborBlockId the block ID of the neighbor that changed (unused)
	 */
	@Override
	public final void onNeighborChange(Level level, int x, int y, int z, int neighborBlockId) {
		// Single slabs have no special behavior on neighbor change
		if(this == Block.SLAB) {
			;
		}
	}

	/**
	 * Called when this slab block is added/placed in the world.
	 * Implements automatic slab merging: if a single slab is placed on top of another single slab,
	 * they merge into a double slab.
	 * Double slabs call the parent implementation for normal block placement.
	 * Single slabs check if there's another single slab below them and merge if found.
	 *
	 * @param level the level containing this slab
	 * @param x the x coordinate of the placed slab
	 * @param y the y coordinate of the placed slab
	 * @param z the z coordinate of the placed slab
	 */
	@Override
	public final void onAdded(Level level, int x, int y, int z) {
		// Double slabs use normal placement logic
		if(this != Block.SLAB) {
			super.onAdded(level, x, y, z);
		}

		// Check if there's a single slab below this position
		int blockBelowId = level.getTile(x, y - 1, z);
		if(blockBelowId == SLAB.id) {
			// Merge two single slabs into a double slab
			level.setTile(x, y, z, 0);
			level.setTile(x, y - 1, z, Block.DOUBLE_SLAB.id);
		}

	}

	/**
	 * Gets the item ID that drops when this slab is broken.
	 * Both single and double slabs drop single slab items.
	 *
	 * @return the block ID of single slab (Block.SLAB.id)
	 */
	@Override
	public final int getDrop() {
		return Block.SLAB.id;
	}

	/**
	 * Determines if this slab block is a cube shape.
	 * Only double slabs are cube-shaped (full 1x1x1 unit).
	 * Single slabs are half-height and therefore not cubes.
	 *
	 * @return true if this is a double slab (cube-shaped), false if single slab
	 */
	@Override
	public final boolean isCube() {
		return this.isDoubleSlab;
	}

	/**
	 * Determines if a particular side of this slab block should be rendered.
	 * For double slabs, uses parent class rendering logic.
	 * For single slabs, implements special logic:
	 * - Top face (side==1) always renders
	 * - Other faces render based on adjacent block and slab type
	 * - Side faces only render if adjacent block is not another slab
	 *
	 * @param level the level containing this slab
	 * @param x the x coordinate of the adjacent block
	 * @param y the y coordinate of the adjacent block
	 * @param z the z coordinate of the adjacent block
	 * @param sideIndex the side direction (0=bottom, 1=top, 2-5=sides)
	 * @return true if this side should be rendered, false if culled
	 */
	@Override
	public final boolean canRenderSide(Level level, int x, int y, int z, int sideIndex) {
		// Double slabs use parent class rendering
		if(this != Block.SLAB) {
			super.canRenderSide(level, x, y, z, sideIndex);
		}

		// Top face of single slab always renders
		if(sideIndex == 1) {
			return true;
		}

		// For other faces, check parent logic and adjacent block type
		if(!super.canRenderSide(level, x, y, z, sideIndex)) {
			return false;
		}

		// Bottom face always renders if parent allows it
		// Side faces only render if adjacent block is not another slab
		if(sideIndex == 0) {
			return true;
		}

		// Check if adjacent block is a slab
		int adjacentBlockId = level.getTile(x, y, z);
		return adjacentBlockId != this.id;
	}
}


