package com.mojang.minecraft.level.generator.noise;

public final class CombinedNoise extends Noise {

	/** The first noise source. */
	private final Noise noise1;
	/** The second noise source used to offset the first. */
	private final Noise noise2;

	/**
	 * Creates a new CombinedNoise source that combines two other noise sources.
	 *
	 * @param noise1 The primary noise source.
	 * @param noise2 The noise source used for coordinate displacement.
	 */
	public CombinedNoise(Noise noise1, Noise noise2) {
		this.noise1 = noise1;
		this.noise2 = noise2;
	}

	@Override
	public double compute(double x, double z) {
		return noise1.compute(x + noise2.compute(x, z), z);
	}
}
