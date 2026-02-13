package com.mojang.minecraft.level.generator.noise;

import java.util.Random;

/**
 * Generates octave-based noise by combining multiple layers of Perlin noise.
 */
public class OctaveNoise extends Noise {

	/** The array of Perlin noise generators for each octave. */
	private final PerlinNoise[] perlin;
	/** The number of octaves to combine. */
	private final int octaves;

	/**
	 * Creates a new OctaveNoise generator.
	 *
	 * @param random  The random number generator to seed the Perlin noise sources.
	 * @param octaves The number of octaves to use.
	 */
	public OctaveNoise(Random random, int octaves) {
		this.octaves = octaves;
		perlin = new PerlinNoise[octaves];

		for (int count = 0; count < octaves; count++) {
			perlin[count] = new PerlinNoise(random);
		}
	}

	@Override
	public double compute(double x, double z) {
		double result = 0.0D;
		double scale = 1.0D;

		for (int count = 0; count < octaves; count++) {
			// Combine noise layers. Each octave has half the frequency and double the amplitude of the previous one.
			result += perlin[count].compute(x / scale, z / scale) * scale;
			scale *= 2.0D;
		}

		return result;
	}
}
