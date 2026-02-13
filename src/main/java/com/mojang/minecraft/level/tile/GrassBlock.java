package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import java.util.Random;

/**
 * Represents a grass block in Minecraft Classic.
 * Grass blocks are natural terrain blocks that grow on dirt when exposed to light.
 * They have different textures on the top (grass), bottom (dirt), and sides (grass with dirt).
 * Grass blocks will convert to dirt blocks when not lit, and can spread to adjacent dirt blocks when lit.
 *
 * @author Mojang
 */
public final class GrassBlock extends Block {

	/**
	 * Constructs a GrassBlock with the specified block ID.
	 * Uses hardcoded block ID 2 and texture ID 3 for the grass block.
	 * Enables physics for this block type.
	 *
	 * @param var1 the block ID parameter (unused, fixed to 2)
	 */
	protected GrassBlock(int var1) {
		super(2);
		this.textureId = 3;
		this.setPhysics(true);
	}

	/**
	 * Gets the texture ID for the specified face of the grass block.
	 * Top face (index 1) uses dirt texture (0).
	 * Bottom face (index 0) uses dirt texture (2).
	 * Side faces use grass with dirt texture (3).
	 *
	 * @param texture the face index (0=bottom, 1=top, 2-5=sides)
	 * @return the texture ID for the specified face
	 */
	@Override
	protected final int getTextureId(int texture) {
		return texture == 1?0:(texture == 0?2:3);
	}

	/**
	 * Updates the grass block's state each game tick.
	 * With a 25% chance per tick:
	 * - Converts to dirt if not lit (darkness kills grass)
	 * - Spreads to adjacent dirt blocks within a 3x5x3 area if lit
	 * This creates the natural grass growth and decay mechanics.
	 *
	 * @param level the level containing this grass block
	 * @param x the x coordinate of the grass block
	 * @param y the y coordinate of the grass block
	 * @param z the z coordinate of the grass block
	 * @param rand a random number generator for probability calculations
	 */
	@Override
	public final void update(Level level, int x, int y, int z, Random rand) {
		if(rand.nextInt(4) == 0) {
			if(!level.isLit(x, y, z)) {
				// Grass dies in darkness - convert to dirt
				level.setTile(x, y, z, Block.DIRT.id);
			} else {
				// Grass spreads to nearby dirt blocks in light
				for(int var9 = 0; var9 < 4; ++var9) {
					int var6 = x + rand.nextInt(3) - 1;
					int var7 = y + rand.nextInt(5) - 3;
					int var8 = z + rand.nextInt(3) - 1;
					if(level.getTile(var6, var7, var8) == Block.DIRT.id && level.isLit(var6, var7, var8)) {
						level.setTile(var6, var7, var8, Block.GRASS.id);
					}
				}

			}
		}
	}

	/**
	 * Gets the item ID that drops when this grass block is broken.
	 * Grass blocks drop dirt items, not grass items.
	 *
	 * @return the drop ID from the DIRT block (equals DIRT.id)
	 */
	@Override
	public final int getDrop() {
		return Block.DIRT.getDrop();
	}
}


