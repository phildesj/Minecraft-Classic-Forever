package com.mojang.minecraft.render;

import com.mojang.minecraft.player.Player;
import java.util.Comparator;

/**
 * ChunkDirtyDistanceComparator implements a specialized comparator for sorting chunks based on visibility and distance.
 * Chunks that need updating (marked as dirty) and are visible are prioritized and sorted by distance from the player.
 * Invisible chunks are deprioritized, ensuring that visible geometry is processed and rendered before off-screen chunks.
 * This comparator is used when updating chunk data to optimize load times for visible terrain.
 */
public class ChunkDirtyDistanceComparator implements Comparator {
	/**
	 * Constructs a new ChunkDirtyDistanceComparator for the specified player.
	 *
	 * @param player the player whose position determines the chunk sort order
	 */
	public ChunkDirtyDistanceComparator(Player player) {
		// Store reference to the player for distance calculations
		this.player = player;
	}

	/**
	 * Compares two chunks by visibility status and distance from the player.
	 * Prioritizes visible chunks over invisible ones, and within the same visibility status,
	 * sorts by squared distance in descending order (farthest first).
	 * This ensures visible chunks are processed before invisible chunks, optimizing render times.
	 *
	 * @param o1 the first chunk to compare
	 * @param o2 the second chunk to compare
	 * @return negative if o1 should be sorted first, positive if o2 should be sorted first, zero if equal priority
	 */
	@Override
	public int compare(Object o1, Object o2) {
		// Cast both objects to Chunk type for comparison
		Chunk chunk = (Chunk)o1;
		Chunk other = (Chunk)o2;

		// Check visibility status of both chunks to determine sort priority
		if(chunk.visible || !other.visible) {
			// First chunk is visible OR second chunk is invisible - check further conditions
			if(other.visible) {
				// Both chunks are visible - sort by distance from player (descending order)
				// Calculate squared distance from player to first chunk (avoiding sqrt for performance)
				float sqDist = chunk.distanceSquared(player);

				// Calculate squared distance from player to second chunk
				float otherSqDist = other.distanceSquared(player);

				// Compare distances and return sort order for visible chunks
				if(sqDist == otherSqDist) {
					// Both chunks are equidistant from the player
					return 0;
				} else if(sqDist > otherSqDist) {
					// First chunk is farther, sort it before the second chunk (descending order)
					return -1;
				} else {
					// Second chunk is farther, sort the first chunk before it (descending order)
					return 1;
				}
			} else {
				// First chunk is visible but second chunk is invisible
				// Prioritize the visible chunk by sorting it after invisible ones
				return 1;
			}
		} else {
			// First chunk is invisible AND second chunk is visible
			// Deprioritize the invisible chunk by sorting it before visible ones
			return -1;
		}
	}

	/** Reference to the player whose position is used for distance calculations. */
	private Player player;
}
