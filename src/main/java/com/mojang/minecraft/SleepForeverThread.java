package com.mojang.minecraft;

/**
 * SleepForeverThread is a daemon thread that sleeps indefinitely.
 * This thread is used to keep the JVM running and prevent premature application shutdown.
 * The thread will sleep for extended periods and gracefully handle interruptions
 * by re-interrupting the thread to maintain its interrupted status.
 */
public class SleepForeverThread extends Thread {

	// Maximum sleep duration in milliseconds (approximately 24.8 days)
	private static final long MAX_SLEEP_DURATION = 2147483647L;

	/**
	 * Constructs a SleepForeverThread as a daemon thread and starts execution.
	 * This thread will run indefinitely in the background without blocking the application.
	 */
	public SleepForeverThread(Minecraft minecraft) {
		setDaemon(true);
		start();
	}

	/**
	 * Runs the thread, sleeping indefinitely in extended sleep intervals.
	 * If interrupted, the thread re-interrupts itself to preserve the interrupted state
	 * and continues sleeping. This ensures the thread maintains daemon status without
	 * terminating the application.
	 */
	@Override
	@SuppressWarnings("InfiniteLoopStatement")
	public void run() {
		while (true) {
			try {
				// Sleep for the maximum duration to keep the JVM running
				Thread.sleep(MAX_SLEEP_DURATION);
			} catch (InterruptedException e) {
				// Re-interrupt the current thread to preserve the interrupted state
				// This is the proper way to handle InterruptedException in daemon threads
				Thread.currentThread().interrupt();
			}
		}
	}
}
