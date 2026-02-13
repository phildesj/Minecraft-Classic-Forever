package com.mojang.minecraft.level.tile;

/**
 * Represents a stone block in Minecraft Classic.
 * Stone blocks are solid, opaque blocks commonly found in the world as part of terrain.
 * When broken, stone blocks drop cobblestone instead of stone itself.
 * This implements the mining mechanic where stone must be processed to collect it as an item.
 * Cobblestone can then be used for crafting and building purposes.
 *
 * @author Mojang
 */
public final class StoneBlock extends Block {

	/**
	 * Constructs a StoneBlock with the specified block ID and texture ID.
	 * Stone blocks are initialized with their block ID and texture for rendering.
	 *
	 * @param blockId the unique block ID for this stone block
	 * @param textureId the texture ID that determines how stone appears in the world
	 */
	public StoneBlock(int blockId, int textureId) {
		super(blockId, textureId);
	}

	/**
	 * Gets the item ID that drops when this stone block is broken.
	 * Stone blocks drop cobblestone items instead of stone items.
	 * This is the defining mechanic of stone - breaking it yields cobblestone for crafting.
	 * Cobblestone can then be smelted to get stone back, or used directly in recipes.
	 *
	 * @return the block ID of cobblestone (Block.COBBLESTONE.id)
	 */
	@Override
	public final int getDrop() {
		return Block.COBBLESTONE.id;
	}
}


