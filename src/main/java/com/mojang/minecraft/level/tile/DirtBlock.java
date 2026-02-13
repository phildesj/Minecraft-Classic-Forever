package com.mojang.minecraft.level.tile;

/**
 * Represents a dirt block in Minecraft Classic.
 * Dirt blocks are natural terrain blocks found in the world.
 * They have a simple cubic shape with uniform texture on all sides.
 *
 * @author Mojang
 */
public final class DirtBlock extends Block {

	/**
	 * Constructs a DirtBlock with the specified block ID and texture ID.
	 * Uses hardcoded block ID 3 and texture ID 2 for the dirt block.
	 *
	 * @param blockId the block ID parameter (unused, fixed to 3)
	 * @param textureId the texture ID parameter (unused, fixed to 2)
	 */
	protected DirtBlock(int blockId, int textureId) {
		super(3, 2);
	}
}


