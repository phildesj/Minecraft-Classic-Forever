package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.liquid.LiquidType;

import java.util.Random;

/**
 * Represents a stationary/still liquid block in Minecraft Classic.
 * Still liquids are the non-flowing variant of water or lava.
 * When a still liquid block has an adjacent empty space or neighboring flowing liquid,
 * it converts to its flowing variant and spreads in that direction.
 * Still liquids do not spread on their own - they only convert when adjacent to empty space.
 * Water and lava still create stone when they meet (same mechanic as flowing liquids).
 *
 * @author Mojang
 */
public final class StillLiquidBlock extends LiquidBlock {

	/**
	 * Constructs a StillLiquidBlock with the specified block ID and liquid type.
	 * The flowing liquid ID is set to blockId - 1 (the flowing variant).
	 * The still liquid ID is set to blockId (this block).
	 * Physics is disabled for still liquids (they don't fall like flowing liquids).
	 *
	 * @param blockId the unique block ID for this still liquid type
	 * @param liquidType the type of liquid (WATER or LAVA)
	 */
	protected StillLiquidBlock(int blockId, LiquidType liquidType) {
		super(blockId, liquidType);
		this.flowingLiquidId = blockId - 1;
		this.stillLiquidId = blockId;
		this.setPhysics(false);
	}

	/**
	 * Updates the still liquid block each game tick.
	 * Still liquids do not update on their own - they remain stationary.
	 * Spreading only occurs when neighbors change (see onNeighborChange).
	 *
	 * @param level the level containing this still liquid
	 * @param x the x coordinate of the still liquid block
	 * @param y the y coordinate of the still liquid block
	 * @param z the z coordinate of the still liquid block
	 * @param randomGenerator random number generator (unused for still liquids)
	 */
	@Override
	public final void update(Level level, int x, int y, int z, Random randomGenerator) {
		// Still liquids do not update - no action needed
	}

	/**
	 * Called when an adjacent block changes.
	 * Still liquid converts to flowing liquid if there is adjacent empty space.
	 * Checks all 5 directions: left, right, front, back, and down.
	 * If any adjacent space is empty (block ID 0), still liquid becomes flowing.
	 * Also implements water+lava=stone mechanic (same as flowing liquids).
	 * When conversion occurs, the flowing liquid is added to tick queue for further spreading.
	 *
	 * @param level the level containing this still liquid
	 * @param x the x coordinate of this still liquid block
	 * @param y the y coordinate of this still liquid block
	 * @param z the z coordinate of this still liquid block
	 * @param neighborBlockId the block ID of the neighbor that changed
	 */
	@Override
	public final void onNeighborChange(Level level, int x, int y, int z, int neighborBlockId) {
		// Check all adjacent horizontal and downward positions for empty space
		boolean hasAdjacentEmpty = false;

		// Check left (x - 1)
		if(level.getTile(x - 1, y, z) == 0) {
			hasAdjacentEmpty = true;
		}

		// Check right (x + 1)
		if(level.getTile(x + 1, y, z) == 0) {
			hasAdjacentEmpty = true;
		}

		// Check front (z - 1)
		if(level.getTile(x, y, z - 1) == 0) {
			hasAdjacentEmpty = true;
		}

		// Check back (z + 1)
		if(level.getTile(x, y, z + 1) == 0) {
			hasAdjacentEmpty = true;
		}

		// Check down (y - 1)
		if(level.getTile(x, y - 1, z) == 0) {
			hasAdjacentEmpty = true;
		}

		// Check if neighboring block is liquid and if water+lava=stone reaction occurs
		if(neighborBlockId != 0) {
			LiquidType neighborLiquidType = Block.blocks[neighborBlockId].getLiquidType();
			if(this.liquidType == LiquidType.WATER && neighborLiquidType == LiquidType.LAVA || neighborLiquidType == LiquidType.WATER && this.liquidType == LiquidType.LAVA) {
				// Water and lava meet - create stone
				level.setTile(x, y, z, Block.STONE.id);
				return;
			}
		}

		// If there's adjacent empty space, convert to flowing liquid and trigger spreading
		if(hasAdjacentEmpty) {
			level.setTileNoUpdate(x, y, z, this.flowingLiquidId);
			level.addToTickNextTick(x, y, z, this.flowingLiquidId);
		}

	}


}


