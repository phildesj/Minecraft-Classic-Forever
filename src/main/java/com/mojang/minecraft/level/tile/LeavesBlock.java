package com.mojang.minecraft.level.tile;

import java.util.Random;

/**
 * Represents a leaf block in Minecraft Classic.
 * Leaves are non-solid, transparent blocks that form the foliage of trees.
 * When broken, leaves have a 10% chance to drop a sapling, otherwise they drop nothing.
 * This encourages tree farming and adds strategic value to harvesting trees.
 *
 * @author Mojang
 */
public final class LeavesBlock extends LeavesBaseBlock {

	/**
	 * Random number generator for determining sapling drops.
	 * Used to calculate the 10% chance that breaking leaves yields a sapling.
	 */
	private static Random randomGenerator = new Random();

	/**
	 * Constructs a LeavesBlock with hardcoded block ID 18 and texture ID 22.
	 * Uses the parent LeavesBaseBlock constructor with showNeighborSides set to true
	 * to allow rendering through leaf blocks.
	 *
	 * @param blockId the block ID parameter (unused, fixed to 18)
	 * @param textureId the texture ID parameter (unused, fixed to 22)
	 */
	protected LeavesBlock(int blockId, int textureId) {
		super(18, 22, true);
	}

	/**
	 * Gets the number of items dropped when this leaf block is broken.
	 * Leaves have a 10% chance to drop one sapling, 90% chance to drop nothing.
	 * This implements the randomized loot mechanic for leaf blocks.
	 *
	 * @return 1 if random check succeeds (10% chance), 0 otherwise (90% chance)
	 */
	@Override
	public final int getDropCount() {
		return randomGenerator.nextInt(10) == 0?1:0;
	}

	/**
	 * Gets the item ID that drops when this leaf block is broken.
	 * Leaves always drop saplings when they yield an item (see getDropCount).
	 * The sapling can be used to grow new trees.
	 *
	 * @return the drop ID from the SAPLING block (equals SAPLING.id)
	 */
	@Override
	public final int getDrop() {
		return Block.SAPLING.id;
	}
}





