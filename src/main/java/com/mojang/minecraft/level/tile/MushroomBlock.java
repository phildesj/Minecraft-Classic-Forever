package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import java.util.Random;

/**
 * Represents a mushroom block in Minecraft Classic.
 * Mushrooms are non-solid, decorative blocks that grow on stone, gravel, and cobblestone.
 * Unlike flowers that prefer grass and light, mushrooms grow in dark places and on mineral blocks.
 * Mushrooms are shorter than flowers (half height) and disappear if exposed to light or placed on wrong substrate.
 *
 * @author Mojang
 */
public final class MushroomBlock extends FlowerBlock {

	/**
	 * Constructs a MushroomBlock with the specified block ID and texture ID.
	 * Extends FlowerBlock and initializes mushroom-specific bounds (shorter than flowers).
	 * Mushrooms have a cross-shaped rendering but with reduced height (0.4 instead of 0.6).
	 *
	 * @param blockId the unique block ID for this mushroom type
	 * @param textureId the texture ID for this mushroom's appearance
	 */
	protected MushroomBlock(int blockId, int textureId) {
		super(blockId, textureId);
		float mushroomSize = 0.2F;
		this.setBounds(0.5F - mushroomSize, 0.0F, 0.5F - mushroomSize, mushroomSize + 0.5F, mushroomSize * 2.0F, mushroomSize + 0.5F);
	}

	/**
	 * Updates the mushroom's state each game tick.
	 * Mushrooms require dark conditions and specific substrate blocks (stone, gravel, cobblestone).
	 * If the mushroom is in light OR the block below is not stone/gravel/cobblestone, it disappears.
	 * This implements the characteristic behavior of mushrooms growing only in dark mineral areas.
	 *
	 * @param level the level containing this mushroom
	 * @param x the x coordinate of the mushroom
	 * @param y the y coordinate of the mushroom
	 * @param z the z coordinate of the mushroom
	 * @param randomGenerator random number generator (unused for mushrooms)
	 */
	@Override
	public final void update(Level level, int x, int y, int z, Random randomGenerator) {
		int blockBelowId = level.getTile(x, y - 1, z);
		// Mushroom survives only in darkness AND on specific substrate blocks
		if(level.isLit(x, y, z) || blockBelowId != Block.STONE.id && blockBelowId != Block.GRAVEL.id && blockBelowId != Block.COBBLESTONE.id) {
			level.setTile(x, y, z, 0);
		}

	}
}


