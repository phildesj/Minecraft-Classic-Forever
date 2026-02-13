package com.mojang.minecraft.level.generator.noise;

import java.util.Random;

/**
 * An implementation of Perlin Noise, used for generating smooth, natural-looking textures and terrain.
 */
public class PerlinNoise extends Noise {

	/** The permutation array used for noise generation. */
	private final int[] permutations;

	/**
	 * Creates a new PerlinNoise generator with a random seed.
	 */
	public PerlinNoise() {
		this(new Random());
	}

	/**
	 * Creates a new PerlinNoise generator with the specified seed.
	 *
	 * @param random The random number generator used to seed the permutations.
	 */
	public PerlinNoise(Random random) {
		permutations = new int[512];

		for (int i = 0; i < 256; i++) {
			permutations[i] = i;
		}

		for (int i = 0; i < 256; i++) {
			int target = random.nextInt(256 - i) + i;
			int temp = permutations[i];

			permutations[i] = permutations[target];
			permutations[target] = temp;
			permutations[i + 256] = permutations[i];
		}
	}

	@Override
	public double compute(double x, double z) {
		double y = 0.0D;

		int floorX = (int) Math.floor(x) & 255;
		int floorZ = (int) Math.floor(z) & 255;
		int floorY = (int) Math.floor(y) & 255;

		double relativeX = x - Math.floor(x);
		double relativeZ = z - Math.floor(z);
		double relativeY = y - Math.floor(y);

		double fadeX = fade(relativeX);
		double fadeZ = fade(relativeZ);
		double fadeY = fade(relativeY);

		int pA = permutations[floorX] + floorZ;
		int pAA = permutations[pA] + floorY;
		int pAB = permutations[pA + 1] + floorY;
		int pB = permutations[floorX + 1] + floorZ;
		int pBA = permutations[pB] + floorY;
		int pBB = permutations[pB + 1] + floorY;

		return lerp(fadeY,
				lerp(fadeZ,
						lerp(fadeX,
								grad(permutations[pAA], relativeX, relativeZ, relativeY),
								grad(permutations[pBA], relativeX - 1.0D, relativeZ, relativeY)),
						lerp(fadeX,
								grad(permutations[pAB], relativeX, relativeZ - 1.0D, relativeY),
								grad(permutations[pBB], relativeX - 1.0D, relativeZ - 1.0D, relativeY))),
				lerp(fadeZ,
						lerp(fadeX,
								grad(permutations[pAA + 1], relativeX, relativeZ, relativeY - 1.0D),
								grad(permutations[pBA + 1], relativeX - 1.0D, relativeZ, relativeY - 1.0D)),
						lerp(fadeX,
								grad(permutations[pAB + 1], relativeX, relativeZ - 1.0D, relativeY - 1.0D),
								grad(permutations[pBB + 1], relativeX - 1.0D, relativeZ - 1.0D, relativeY - 1.0D))));
	}

	/**
	 * Quintic interpolation curve (6t^5 - 15t^4 + 10t^3).
	 *
	 * @param t Progress value (0.0 to 1.0).
	 * @return The faded value.
	 */
	private static double fade(double t) {
		return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
	}

	/**
	 * Linear interpolation between two values.
	 *
	 * @param t Progress value (0.0 to 1.0).
	 * @param a Start value.
	 * @param b End value.
	 * @return The interpolated value.
	 */
	private static double lerp(double t, double a, double b) {
		return a + t * (b - a);
	}

	/**
	 * Computes the dot product between a pseudo-random gradient vector and the distance vector.
	 *
	 * @param hash A hashed value from the permutation table.
	 * @param x    X component of the distance vector.
	 * @param z    Z component of the distance vector.
	 * @param y    Y component of the distance vector.
	 * @return The gradient value.
	 */
	private static double grad(int hash, double x, double z, double y) {
		int h = hash & 15;
		double u = h < 8 ? x : z;
		double v = h < 4 ? z : (h != 12 && h != 14 ? y : x);

		return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
	}
}
