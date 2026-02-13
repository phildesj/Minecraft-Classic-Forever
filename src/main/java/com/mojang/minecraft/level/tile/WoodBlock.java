package com.mojang.minecraft.level.tile;

/**
 * Represents a wood block (log) in Minecraft Classic.
 * Wood blocks are solid, opaque blocks harvested from trees.
 * Wood blocks have different textures on the top/bottom (bark variant) versus sides (bark).
 * When broken, wood blocks drop 3-5 wood items (logs) for use in crafting and building.
 * Wood is one of the primary building materials in Minecraft, essential for early game progression.
 *
 * @author Mojang
 */
public final class WoodBlock extends Block {

	/**
	 * Constructs a WoodBlock with hardcoded block ID 17 and texture ID 20.
	 * The blockId parameter is ignored in favor of the hardcoded block ID.
	 * Sets texture ID to 20 for the side faces of the log.
	 *
	 * @param blockId the block ID parameter (unused, fixed to 17)
	 */
	protected WoodBlock(int blockId) {
		super(17);
		this.textureId = 20;
	}

	/**
	 * Gets the number of items dropped when this wood block is broken.
	 * Wood blocks drop a random amount between 3 and 5 logs (items).
	 * The randomization encourages harvesting multiple wood blocks for more resources.
	 * Formula: random(0-2) + 3 = drops between 3 and 5 logs.
	 *
	 * @return a random integer between 3 and 5 representing the number of logs dropped
	 */
	@Override
	public final int getDropCount() {
		return random.nextInt(3) + 3;
	}

	/**
	 * Gets the item ID that drops when this wood block is broken.
	 * Wood blocks always drop WOOD items regardless of how many are dropped.
	 * The WOOD item represents a single log that can be used for crafting and building.
	 *
	 * @return the block ID of wood/logs (Block.WOOD.id)
	 */
	@Override
	public final int getDrop() {
		return Block.WOOD.id;
	}

	/**
	 * Gets the texture ID for the specified face of the wood block.
	 * Top and bottom faces (indices 0-1) use texture 21 (bark variant).
	 * Side faces (indices 2-5) use texture 20 (side bark texture).
	 * This creates visual distinction between different orientations of the log.
	 *
	 * @param faceIndex the face index (0=bottom, 1=top, 2-5=sides)
	 * @return the texture ID for the specified face (21 for top/bottom, 20 for sides)
	 */
	@Override
	protected final int getTextureId(int faceIndex) {
		return faceIndex == 1?21:(faceIndex == 0?21:20);
	}
}


