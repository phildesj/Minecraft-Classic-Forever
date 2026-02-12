package com.mojang.minecraft;

/**
 * Timer manages game timing and tick synchronization with support for variable
 * speed control. It tracks elapsed time between frames and ensures consistent
 * game updates at the specified ticks per second.
 */
public class Timer {

	// Default speed multiplier (1.0 = normal speed)
	private static final float DEFAULT_SPEED = 1.0F;

	// Default elapsed delta accumulator
	private static final float DEFAULT_ELAPSED_DELTA = 0.0F;

	// Target ticks per second for game updates
	public final float tps;

	// Number of game ticks that have elapsed in this frame
	public int elapsedTicks;

	// Time delta (frame time) in seconds
	public float delta;

	// Game speed multiplier (1.0 = normal speed, adjusts game tick rate)
	public float speed = DEFAULT_SPEED;

	// Accumulated delta time for tick calculations
	public float elapsedDelta = DEFAULT_ELAPSED_DELTA;

	/**
	 * Constructs a Timer with a specified target ticks per second.
	 * Initializes the timer for frame synchronization and tick accumulation.
	 *
	 * @param tps the target ticks per second (updates per second)
	 */
	public Timer(float tps) {
		this.tps = tps;
	}
}
