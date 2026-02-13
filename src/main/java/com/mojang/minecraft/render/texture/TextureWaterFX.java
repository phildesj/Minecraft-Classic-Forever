package com.mojang.minecraft.render.texture;

import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.render.texture.TextureFX;

/**
 * TextureWaterFX implements animated water texture effects for the Minecraft Classic Forever client.
 * Uses a 16x16 pixel texture with wave propagation and ripple animations to create realistic water appearance.
 * The animation algorithm applies wave equation simulation with wave smoothing and random ripple generation.
 * Supports anaglyph 3D color processing for stereoscopic rendering.
 */
public final class TextureWaterFX extends TextureFX {

	/** Red channel intensity values for the 16x16 texture (256 pixels). */
	private float[] red = new float[256];

	/** Blue channel intensity values for the 16x16 texture (256 pixels). */
	private float[] blue = new float[256];

	/** Green channel intensity values for the 16x16 texture (256 pixels). */
	private float[] green = new float[256];

	/** Alpha channel (wave height) values for controlling animation intensity. */
	private float[] alpha = new float[256];

	/** Counter tracking the number of animation frames since texture creation. */
	private int updates = 0;

	/**
	 * Constructs a new TextureWaterFX animation for water texture effects.
	 * Associates this animation with the water block's texture ID.
	 */
	public TextureWaterFX() {
		// Initialize with the OpenGL texture ID of the water block
		super(Block.WATER.textureId);
	}

	/**
	 * Updates the water texture animation for the current frame.
	 * Applies wave equation simulation with wave propagation, smoothing, and random ripple generation.
	 * Each frame performs: wave height averaging, wave dissipation, ripple generation,
	 * and color value computation for RGBA output with cyan/blue coloring.
	 */
	public final void animate() {
		// Increment the animation frame counter
		++this.updates;

		// Define loop variables for pixel coordinates and height calculations
		int pixelX;
		int pixelY;
		float waveHeight;
		int neighborX;
		int neighborY;
		int wrappedX;

		// Apply wave propagation: average neighboring wave heights to smooth the wave field
		// This implements the wave equation with damping for smooth water surface
		for(pixelX = 0; pixelX < 16; ++pixelX) {
			for(pixelY = 0; pixelY < 16; ++pixelY) {
				// Initialize wave height accumulator for this pixel
				waveHeight = 0.0F;

				// Apply 1D horizontal averaging (vertical neighbors only)
				// Creates smooth wave propagation along the wave field
				for(neighborX = pixelX - 1; neighborX <= pixelX + 1; ++neighborX) {
					// Wrap X coordinate to handle texture boundaries (tiled wrapping)
					wrappedX = neighborX & 15;

					// Wrap Y coordinate to handle texture boundaries (tiled wrapping)
					neighborY = pixelY & 15;

					// Accumulate wave heights from horizontal neighbors
					waveHeight += this.red[wrappedX + (neighborY << 4)];
				}

				// Store the smoothed wave height in the blue channel for next frame
				// Combines averaged height (wave propagation) with damped previous height (smoothing)
				this.blue[pixelX + (pixelY << 4)] = waveHeight / 3.3F + this.green[pixelX + (pixelY << 4)] * 0.8F;
			}
		}

		// Apply wave dissipation and ripple generation
		for(pixelX = 0; pixelX < 16; ++pixelX) {
			for(pixelY = 0; pixelY < 16; ++pixelY) {
				// Update wave height from energy source (alpha channel)
				// Add 5% of the alpha (ripple energy) to the green channel each frame
				this.green[pixelX + (pixelY << 4)] += this.alpha[pixelX + (pixelY << 4)] * 0.05F;

				// Clamp green values to non-negative (no negative wave heights)
				if(this.green[pixelX + (pixelY << 4)] < 0.0F) {
					this.green[pixelX + (pixelY << 4)] = 0.0F;
				}

				// Decay alpha values (ripple energy dissipates over time) at 10% per frame
				this.alpha[pixelX + (pixelY << 4)] -= 0.1F;

				// Randomly generate new ripples (5% chance per pixel per frame)
				// High alpha values create bright "wave peaks" for ripple effect
				if(Math.random() < 0.05D) {
					this.alpha[pixelX + (pixelY << 4)] = 0.5F;
				}
			}
		}

		// Swap blue and red buffers to advance the simulation by one frame
		// This implements the "ping-pong" buffer pattern for updating wave field
		float[] tempBuffer = this.blue;
		this.blue = this.red;
		this.red = tempBuffer;

		// Convert floating-point height values to byte RGBA format for texture upload
		// Iterates through all 256 pixels in the texture
		for(pixelY = 0; pixelY < 256; ++pixelY) {
			// Normalize and clamp the wave height to 0.0-1.0 range
			if((waveHeight = this.red[pixelY]) > 1.0F) {
				waveHeight = 1.0F;
			}

			if(waveHeight < 0.0F) {
				waveHeight = 0.0F;
			}

			// Calculate brightness from wave height (squared for nonlinear contrast)
			float brightnessFactor = waveHeight * waveHeight;

			// Calculate RGB components with cyan/blue coloring for water
			// Red channel: dark (32-64 range, minimal red in water)
			neighborX = (int)(32.0F + brightnessFactor * 32.0F);

			// Green channel: medium (50-114 range, creates cyan when combined with blue)
			neighborY = (int)(50.0F + brightnessFactor * 64.0F);

			// Blue channel: bright (255 always, water is always fully blue)
			pixelX = 255;

			// Alpha channel (brightness) for water clarity (146-196 range)
			wrappedX = (int)(146.0F + brightnessFactor * 50.0F);

			// Apply anaglyph 3D color processing if enabled for stereoscopic rendering
			if(this.anaglyph) {
				// Convert to grayscale using standard luminance formula
				pixelX = (neighborX * 30 + neighborY * 59 + 2805) / 100;

				// Apply anaglyph separation: left eye gets grayscale, right eye gets color shifted
				int greenAnaglyph = (neighborX * 30 + neighborY * 70) / 100;

				// Preserve blue component with anaglyph processing
				int blueAnaglyph = (neighborX * 30 + 17850) / 100;

				// Update color values with anaglyph processing
				neighborX = pixelX;
				neighborY = greenAnaglyph;
				pixelX = blueAnaglyph;
			}

			// Store RGBA pixel data in texture buffer (4 bytes per pixel)
			// Pixel index to byte index: pixelIndex * 4
			this.textureData[pixelY << 2] = (byte)neighborX;
			this.textureData[(pixelY << 2) + 1] = (byte)neighborY;
			this.textureData[(pixelY << 2) + 2] = (byte)pixelX;
			this.textureData[(pixelY << 2) + 3] = (byte)wrappedX;
		}
	}
}


