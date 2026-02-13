package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import java.util.Random;

/**
 * Represents a sapling block in Minecraft Classic.
 * Saplings are small plants that grow into trees when conditions are met.
 * A sapling requires light and must be placed on dirt or grass to survive.
 * With a 20% chance per tick, a sapling will attempt to grow into a tree.
 * If tree growth succeeds, the sapling is replaced by the grown tree structure.
 * If tree growth fails or conditions are not met, the sapling is destroyed.
 *
 * @author Mojang
 */
public final class SaplingBlock extends FlowerBlock {

	/**
	 * Constructs a SaplingBlock with hardcoded block ID 6 and texture ID 15.
	 * Extends FlowerBlock and initializes sapling-specific bounds (0.4 multiplier for narrower shape).
	 * Saplings have a cross-shaped rendering with reduced height compared to flowers.
	 *
	 * @param blockId the block ID parameter (unused, fixed to 6)
	 * @param textureId the texture ID parameter (unused, fixed to 15)
	 */
	protected SaplingBlock(int blockId, int textureId) {
		super(6, 15);
		float saplingSize = 0.4F;
		this.setBounds(0.5F - saplingSize, 0.0F, 0.5F - saplingSize, saplingSize + 0.5F, saplingSize * 2.0F, saplingSize + 0.5F);
	}

	/**
	 * Updates the sapling's state each game tick.
	 * Saplings require light and proper substrate (dirt or grass) to survive and grow.
	 * Each tick: 20% chance to attempt tree growth (nextInt(5) == 0).
	 * If tree growth succeeds, sapling is replaced by grown tree.
	 * If tree growth fails, sapling remains (can try again next update).
	 * If conditions not met (no light or wrong substrate), sapling is destroyed.
	 * This implements the gameplay mechanics where saplings must be properly planted.
	 *
	 * @param level the level containing this sapling
	 * @param x the x coordinate of the sapling
	 * @param y the y coordinate of the sapling
	 * @param z the z coordinate of the sapling
	 * @param randomGenerator random number generator for growth chance calculation
	 */
	@Override
	public final void update(Level level, int x, int y, int z, Random randomGenerator) {
		int blockBelowId = level.getTile(x, y - 1, z);
		// Sapling survives only in light AND on proper substrate (dirt or grass)
		if(level.isLit(x, y, z) && (blockBelowId == Block.DIRT.id || blockBelowId == Block.GRASS.id)) {
			// 20% chance per tick to attempt tree growth
			if(randomGenerator.nextInt(5) == 0) {
				// Clear the sapling position
				level.setTileNoUpdate(x, y, z, 0);
				// Try to grow a tree at this location
				if(!level.maybeGrowTree(x, y, z)) {
					// Tree growth failed - replace the sapling (allow it to try again later)
					level.setTileNoUpdate(x, y, z, this.id);
				}
				// If tree growth succeeded, the tree structure replaces the sapling
			}

		} else {
			// Conditions not met - sapling is destroyed
			level.setTile(x, y, z, 0);
		}
	}
}


