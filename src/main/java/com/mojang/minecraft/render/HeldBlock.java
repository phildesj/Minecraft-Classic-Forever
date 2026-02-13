package com.mojang.minecraft.render;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.tile.Block;

/**
 * HeldBlock manages the visual representation and animation of a block being held in the player's hand.
 * This class tracks the block type, animation state, and movement of the held item in the player's view.
 * It handles smooth position transitions and movement animations for the held block display.
 */
public class HeldBlock {
	/**
	 * Constructs a new HeldBlock instance for rendering a held block item.
	 *
	 * @param minecraft the Minecraft instance containing game state
	 */
	public HeldBlock(Minecraft minecraft) {
		// Store reference to the main Minecraft instance
		this.minecraft = minecraft;
	}

	/** Reference to the main Minecraft instance for accessing game state. */
	public Minecraft minecraft;

	/** The block type currently being held by the player (null if no block is held). */
	public Block block = null;

	/** The current animation position of the held block (0.0-1.0 range for smooth movement). */
	public float pos = 0.0F;

	/** The previous frame's animation position for smooth interpolation. */
	public float lastPos = 0.0F;

	/** The pixel offset for rendering the held block in the hand position (screen space). */
	public int offset = 0;

	/** Flag indicating whether the held block is currently being moved or animated. */
	public boolean moving = false;
}


