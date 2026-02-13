package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.liquid.LiquidType;

/**
 * Represents a sand block in Minecraft Classic.
 * Sand blocks implement gravity-based physics - they fall downward when unsupported.
 * Sand blocks continue falling until they hit a solid block, water, or lava.
 * Sand is affected by gravity on placement and whenever a neighboring block changes,
 * making it behave like a falling particle within the block-based world.
 *
 * @author Mojang
 */
public final class SandBlock extends Block {

	/**
	 * Constructs a SandBlock with the specified block ID and texture ID.
	 * Sand blocks are initialized with their block ID and texture for rendering.
	 *
	 * @param blockId the unique block ID for this sand block
	 * @param textureId the texture ID that determines how sand appears in the world
	 */
	public SandBlock(int blockId, int textureId) {
		super(blockId, textureId);
	}

	/**
	 * Called when this sand block is placed in the world.
	 * Initiates gravity simulation by checking if the sand should fall.
	 * Sand immediately falls after being placed if there is empty space below it.
	 *
	 * @param level the level containing this sand block
	 * @param x the x coordinate of the placed sand block
	 * @param y the y coordinate of the placed sand block
	 * @param z the z coordinate of the placed sand block
	 */
	@Override
	public final void onPlace(Level level, int x, int y, int z) {
		this.fall(level, x, y, z);
	}

	/**
	 * Called when an adjacent block changes.
	 * Checks if the change affects sand support, triggering gravity if needed.
	 * Sand will fall if a supporting block is removed or if liquid appears below it.
	 *
	 * @param level the level containing this sand block
	 * @param x the x coordinate of this sand block
	 * @param y the y coordinate of this sand block
	 * @param z the z coordinate of this sand block
	 * @param changedBlockId the block ID of the neighbor that changed (unused)
	 */
	@Override
	public final void onNeighborChange(Level level, int x, int y, int z, int changedBlockId) {
		this.fall(level, x, y, z);
	}

	/**
	 * Simulates gravity for sand blocks.
	 * Sand falls downward through empty spaces and through water/lava.
	 * Falling continues until hitting a solid block or reaching world bottom.
	 * If the sand moved, the original position is cleared and sand is placed at final position.
	 * If sand lands in liquid, the liquid is replaced by sand.
	 *
	 * @param level the level containing the sand block
	 * @param originalX the starting x coordinate of the sand block
	 * @param originalY the starting y coordinate of the sand block
	 * @param originalZ the starting z coordinate of the sand block
	 */
	private void fall(Level level, int originalX, int originalY, int originalZ) {
		int currentX = originalX;
		int currentY = originalY;
		int currentZ = originalZ;

		while(true) {
			// Check the block directly below the current position
			int yPositionBelow = currentY - 1;
			int blockBelowId;
			LiquidType liquidTypeBelow;

			// Sand can fall through empty spaces (blockId == 0) and liquids (water or lava)
			if(!((blockBelowId = level.getTile(currentX, yPositionBelow, currentZ)) == 0?true:((liquidTypeBelow = Block.blocks[blockBelowId].getLiquidType()) == LiquidType.WATER?true:liquidTypeBelow == LiquidType.LAVA)) || currentY <= 0) {
				// Sand has stopped falling - either hit a solid block or reached world bottom
				if(currentY != originalY) {
					// Sand moved - check if there's liquid at final position
					int blockAtFinalPosition = level.getTile(currentX, currentY, currentZ);
					if(blockAtFinalPosition > 0 && Block.blocks[blockAtFinalPosition].getLiquidType() != LiquidType.NOT_LIQUID) {
						// Sand landed in liquid - remove the liquid
						level.setTileNoUpdate(currentX, currentY, currentZ, 0);
					}

					// Move sand from original position to final position
					level.swap(originalX, originalY, originalZ, currentX, currentY, currentZ);
				}

				return;
			}

			// Continue falling downward
			--currentY;
		}
	}
}


