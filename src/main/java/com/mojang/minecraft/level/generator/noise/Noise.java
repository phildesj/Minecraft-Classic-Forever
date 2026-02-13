package com.mojang.minecraft.level.generator.noise;


/**
 * Base class for various noise generators used in level generation.
 */
public abstract class Noise {

	/**
	 * Computes a noise value at the given coordinates.
	 *
	 * @param x The X coordinate.
	 * @param z The Z coordinate.
	 * @return The computed noise value.
	 */
	public abstract double compute(double x, double z);
}
