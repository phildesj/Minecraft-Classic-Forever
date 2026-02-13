package com.mojang.minecraft.level.tile;

import java.util.Random;

/**
 * Represents an ore block in Minecraft Classic.
 * Ore blocks are solid, opaque blocks that drop specific materials when broken.
 * Different ore types (coal, iron, gold) drop different materials based on their type.
 * Ore blocks drop 1-3 items when broken, providing resource gathering mechanics for progression.
 *
 * @author Mojang
 */
public final class OreBlock extends Block {

	/**
	 * Random number generator for determining the count of items dropped when ore is broken.
	 * Ore blocks drop between 1 and 3 items (nextInt(3) returns 0-2, +1 makes it 1-3).
	 */
	private static Random randomOreDropCount = new Random();

	/**
	 * Constructs an OreBlock with the specified block ID and texture ID.
	 * Ore blocks are initialized with a unique block ID and texture ID that determines appearance.
	 *
	 * @param blockId the unique block ID for this ore type (coal, iron, or gold)
	 * @param textureId the texture ID that determines how this ore appears in the world
	 */
	public OreBlock(int blockId, int textureId) {
		super(blockId, textureId);
	}

	/**
	 * Gets the item ID that drops when this ore block is broken.
	 * Different ore types drop different materials:
	 * - Coal Ore drops SLAB items (used for crafting)
	 * - Gold Ore drops GOLD_BLOCK items (valuable material)
	 * - Iron Ore drops IRON_BLOCK items (crafting material)
	 * - Unknown ore types drop themselves (the ore block itself)
	 * This implements the resource gathering mechanic where ores yield specific materials.
	 *
	 * @return the block ID of the item that will be dropped (SLAB, GOLD_BLOCK, IRON_BLOCK, or self)
	 */
	@Override
	public final int getDrop() {
		return this == Block.COAL_ORE?Block.SLAB.id:(this == Block.GOLD_ORE?Block.GOLD_BLOCK.id:(this == Block.IRON_ORE?Block.IRON_BLOCK.id:this.id));
	}

	/**
	 * Gets the number of items dropped when this ore block is broken.
	 * Ore blocks drop a random amount between 1 and 3 items, providing variable resource yields.
	 * This randomness encourages mining multiple ore blocks and makes resource gathering less predictable.
	 *
	 * @return a random integer between 1 and 3 representing the number of items dropped
	 */
	@Override
	public final int getDropCount() {
		return randomOreDropCount.nextInt(3) + 1;
	}
}


