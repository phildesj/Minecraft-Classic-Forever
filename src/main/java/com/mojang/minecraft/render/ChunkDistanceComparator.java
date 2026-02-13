package com.mojang.minecraft.render;

import com.mojang.minecraft.player.Player;
import java.util.Comparator;

/**
 * ChunkDistanceComparator implements a comparator for sorting chunks by their distance from the player.
 * This comparator sorts chunks in descending order of distance (farthest first), which is optimal for
 * back-to-front rendering to minimize state changes and improve GPU cache coherency.
 * Used by Arrays.sort() to organize chunks for efficient culling and rendering.
 */
public class ChunkDistanceComparator implements Comparator {
	/**
	 * Constructs a new ChunkDistanceComparator for the specified player.
	 *
	 * @param player the player whose position determines the chunk sort order
	 */
	public ChunkDistanceComparator(Player player) {
		// Store reference to the player for distance calculations
		this.player = player;
	}

	/**
	 * Compares two chunks by their squared distance from the player.
	 * Returns negative if first chunk is farther, positive if second chunk is farther,
	 * or zero if both chunks are equidistant. This results in descending distance order
	 * (farthest chunks first), optimal for back-to-front rendering.
	 *
	 * @param o1 the first chunk to compare
	 * @param o2 the second chunk to compare
	 * @return negative if o1 is farther, positive if o2 is farther, zero if equal distance
	 */
	@Override
	public int compare(Object o1, Object o2) {
		// Cast both objects to Chunk type for comparison
		Chunk chunk = (Chunk)o1;
		Chunk other = (Chunk)o2;

		// Calculate squared distance from player to first chunk (avoiding sqrt for performance)
		float sqDist = chunk.distanceSquared(player);

		// Calculate squared distance from player to second chunk
		float otherSqDist = other.distanceSquared(player);

		// Compare distances and return sort order
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
	}

	/** Reference to the player whose position is used for distance calculations. */
	private Player player;
}
