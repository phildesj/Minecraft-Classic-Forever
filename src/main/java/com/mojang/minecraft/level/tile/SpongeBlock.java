package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;

/**
 * Represents a sponge block in Minecraft Classic.
 * Sponges are special utility blocks that absorb water in a 5x5x5 area around them.
 * When a sponge is placed, all water blocks within a 5x5x5 area centered on the sponge are removed.
 * When a sponge is removed, water flow is re-evaluated in the surrounding area (5x5x5).
 * This allows players to create dry spaces in water or drain water more efficiently.
 *
 * @author Mojang
 */
public final class SpongeBlock extends Block {

	/**
	 * Constructs a SpongeBlock with hardcoded block ID 19 and texture ID 48.
	 * The block ID and texture ID parameters are ignored in favor of hardcoded values.
	 *
	 * @param blockId the block ID parameter (unused, fixed to 19)
	 */
	protected SpongeBlock(int blockId) {
		super(19);
		this.textureId = 48;
	}

	/**
	 * Called when this sponge block is placed in the world.
	 * Immediately removes all water blocks in a 5x5x5 area centered on the sponge.
	 * Water removal occurs within a 2-block radius in all directions (x, y, z).
	 * This creates a dry cavity around the sponge when it is placed.
	 * Uses setTileNoNeighborChange to prevent cascading water updates during removal.
	 *
	 * @param level the level containing this sponge block
	 * @param x the x coordinate of the sponge block
	 * @param y the y coordinate of the sponge block
	 * @param z the z coordinate of the sponge block
	 */
	@Override
	public final void onAdded(Level level, int x, int y, int z) {
		// Check all blocks in a 5x5x5 area centered on the sponge (2 blocks in each direction)
		for(int checkX = x - 2; checkX <= x + 2; ++checkX) {
			for(int checkY = y - 2; checkY <= y + 2; ++checkY) {
				for(int checkZ = z - 2; checkZ <= z + 2; ++checkZ) {
					// If the block is water, remove it
					if(level.isWater(checkX, checkY, checkZ)) {
						level.setTileNoNeighborChange(checkX, checkY, checkZ, 0);
					}
				}
			}
		}

	}

	/**
	 * Called when this sponge block is removed from the world.
	 * Re-evaluates water flow in a 5x5x5 area around the sponge's former position.
	 * This allows water to flow back into the area that was previously dried by the sponge.
	 * Calls updateNeighborsAt for each block to trigger proper water/liquid simulation.
	 *
	 * @param level the level containing the former sponge block
	 * @param removedX the x coordinate of the removed sponge block
	 * @param removedY the y coordinate of the removed sponge block
	 * @param removedZ the z coordinate of the removed sponge block
	 */
	@Override
	public final void onRemoved(Level level, int removedX, int removedY, int removedZ) {
		// Check all blocks in a 5x5x5 area centered on the removed sponge (2 blocks in each direction)
		for(int checkX = removedX - 2; checkX <= removedX + 2; ++checkX) {
			for(int checkY = removedY - 2; checkY <= removedY + 2; ++checkY) {
				for(int checkZ = removedZ - 2; checkZ <= removedZ + 2; ++checkZ) {
					// Update neighbors to re-evaluate water flow at this position
					level.updateNeighborsAt(checkX, checkY, checkZ, level.getTile(checkX, checkY, checkZ));
				}
			}
		}

	}
}
