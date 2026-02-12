package com.mojang.util;

/**
 * MathHelper provides optimized trigonometric and mathematical operations
 * using a pre-computed sine lookup table for performance.
 * This is particularly useful for older systems where trigonometric calculations
 * can be computationally expensive.
 */
public final class MathHelper {

	// Size of the sine lookup table (2^16)
	private static final int TABLE_SIZE = 65536;

	// Conversion factor to map radians to table indices
	private static final float RADIAN_TO_INDEX = 10430.378F;

	// Quarter circle offset (TABLE_SIZE / 4) for cosine calculation
	private static final float COS_OFFSET = 16384.0F;

	// Pre-computed sine values for fast lookup
	private static final float[] SIN_TABLE = new float[TABLE_SIZE];

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private MathHelper() {
	}

	/**
	 * Calculates the sine of an angle (in radians) using a pre-computed lookup table.
	 *
	 * @param angle the angle in radians
	 * @return the sine value of the given angle
	 */
	public static float sin(float angle) {
		return SIN_TABLE[(int)(angle * RADIAN_TO_INDEX) & 0xFFFF];
	}

	/**
	 * Calculates the cosine of an angle (in radians) using a pre-computed lookup table.
	 * Cosine is computed as sine with a quarter-circle phase shift.
	 *
	 * @param angle the angle in radians
	 * @return the cosine value of the given angle
	 */
	public static float cos(float angle) {
		return SIN_TABLE[(int)(angle * RADIAN_TO_INDEX + COS_OFFSET) & 0xFFFF];
	}

	/**
	 * Calculates the square root of a value.
	 *
	 * @param value the value to take the square root of
	 * @return the square root of the given value
	 */
	public static float sqrt(float value) {
		return (float) Math.sqrt(value);
	}

	/**
	 * Static initializer that pre-computes all sine values for the lookup table.
	 * Maps all angles from 0 to 2π into the table at equal intervals.
	 */
	static {
		for (int i = 0; i < TABLE_SIZE; ++i) {
			SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0D / TABLE_SIZE);
		}
	}
}
