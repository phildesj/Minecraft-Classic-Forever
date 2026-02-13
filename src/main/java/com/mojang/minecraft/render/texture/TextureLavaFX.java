package com.mojang.minecraft.render.texture;

import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.render.texture.TextureFX;
import com.mojang.util.MathHelper;

/**
 * TextureLavaFX implements animated lava texture effects for the Minecraft Classic Forever client.
 * Uses a 16x16 pixel texture with flowing and bubbling animations to create realistic lava appearance.
 * The animation algorithm applies fluid dynamics simulation with heat dissipation and bubble generation.
 * Supports anaglyph 3D color processing for stereoscopic rendering.
 */
public final class TextureLavaFX extends TextureFX {

	/** Red channel intensity values for the 16x16 texture (256 pixels). */
	private float[] red = new float[256];

	/** Green channel intensity values for the 16x16 texture (256 pixels). */
	private float[] green = new float[256];

	/** Blue channel intensity values for the 16x16 texture (256 pixels). */
	private float[] blue = new float[256];

	/** Alpha channel (heat/brightness) values for controlling animation intensity. */
	private float[] alpha = new float[256];

	/**
	 * Constructs a new TextureLavaFX animation for lava texture effects.
	 * Associates this animation with the lava block's texture ID.
	 */
	public TextureLavaFX() {
		// Initialize with the OpenGL texture ID of the lava block
		super(Block.LAVA.textureId);
	}

	/**
	 * Updates the lava texture animation for the current frame.
	 * Applies fluid dynamics simulation with heat diffusion, bubble generation, and heat decay.
	 * Each frame performs: height field averaging, heat dissipation, bubble generation,
	 * and color value computation for RGBA output.
	 */
	public final void animate() {
		// Iterate through each pixel in the 16x16 texture
		int pixelX;
		int pixelY;
		float heightAverage;
		int sineOffsetX;
		int sineOffsetY;
		int neighborX;
		int neighborY;
		int wrappedX;
		int wrappedY;
		for(pixelX = 0; pixelX < 16; ++pixelX) {
			for(pixelY = 0; pixelY < 16; ++pixelY) {
				// Initialize height accumulator for this pixel
				heightAverage = 0.0F;

				// Calculate sine-based offset for wave animation (creates flowing effect)
				// sineOffsetX oscillates based on current X position in the wave
				int sineOffsetX_val = (int)(MathHelper.sin((float)pixelY * 3.1415927F * 2.0F / 16.0F) * 1.2F);

				// sineOffsetY oscillates based on current Y position in the wave
				int sineOffsetY_val = (int)(MathHelper.sin((float)pixelX * 3.1415927F * 2.0F / 16.0F) * 1.2F);

				// Apply 3x3 neighborhood averaging (Laplacian filter for height diffusion)
				// This smooths the height field to simulate fluid flow
				for(neighborX = pixelX - 1; neighborX <= pixelX + 1; ++neighborX) {
					for(neighborY = pixelY - 1; neighborY <= pixelY + 1; ++neighborY) {
						// Wrap coordinates to handle texture boundaries (tiled wrapping)
						wrappedX = neighborX + sineOffsetX_val & 15;
						wrappedY = neighborY + sineOffsetY_val & 15;

						// Accumulate height values from 3x3 neighborhood
						heightAverage += this.red[wrappedX + (wrappedY << 4)];
					}
				}

				// Store the smoothed height value in the green channel for next frame
				// Combines averaged height (diffusion) with temperature-driven convection from blue channel
				this.green[pixelX + (pixelY << 4)] = heightAverage / 10.0F + (this.blue[(pixelX & 15) + ((pixelY & 15) << 4)] + this.blue[(pixelX + 1 & 15) + ((pixelY & 15) << 4)] + this.blue[(pixelX + 1 & 15) + ((pixelY + 1 & 15) << 4)] + this.blue[(pixelX & 15) + ((pixelY + 1 & 15) << 4)]) / 4.0F * 0.8F;

				// Update blue channel (heat dissipation) based on alpha channel (energy source)
				// Heat dissipates at 1% of current alpha value per frame
				this.blue[pixelX + (pixelY << 4)] += this.alpha[pixelX + (pixelY << 4)] * 0.01F;

				// Clamp blue values to non-negative (no negative heat)
				if(this.blue[pixelX + (pixelY << 4)] < 0.0F) {
					this.blue[pixelX + (pixelY << 4)] = 0.0F;
				}

				// Decay alpha values (energy dissipates over time) at 6% per frame
				this.alpha[pixelX + (pixelY << 4)] -= 0.06F;

				// Randomly generate new bubbles (0.5% chance per pixel per frame)
				// High alpha values create bright "hot spots" for bubble effect
				if(Math.random() < 0.005D) {
					this.alpha[pixelX + (pixelY << 4)] = 1.5F;
				}
			}
		}

		// Swap red and green buffers to advance the simulation by one frame
		// This implements the "ping-pong" buffer pattern for updating height field
		float[] tempBuffer = this.green;
		this.green = this.red;
		this.red = tempBuffer;

		// Convert floating-point color values to byte RGBA format for texture upload
		// Iterates through all 256 pixels in the texture
		for(pixelY = 0; pixelY < 256; ++pixelY) {
			// Normalize and clamp the red channel (height) to 0.0-1.0 range
			// Multiply by 2.0 to enhance contrast between hot and cool areas
			if((heightAverage = this.red[pixelY] * 2.0F) > 1.0F) {
				heightAverage = 1.0F;
			}

			if(heightAverage < 0.0F) {
				heightAverage = 0.0F;
			}

			// Calculate RGB components from normalized height value
			// Red channel: dark orange to bright red (100-255 range)
			pixelX = (int)(heightAverage * 100.0F + 155.0F);

			// Green channel: intensity squared (0-255 range, darker in cool areas)
			pixelY = (int)(heightAverage * heightAverage * 255.0F);

			// Blue channel: intensity to the 4th power (0-128 range, very dark in cool areas)
			neighborX = (int)(heightAverage * heightAverage * heightAverage * heightAverage * 128.0F);

			// Apply anaglyph 3D color processing if enabled for stereoscopic rendering
			if(this.anaglyph) {
				// Convert to grayscale using standard luminance formula
				neighborY = (pixelX * 30 + pixelY * 59 + neighborX * 11) / 100;

				// Apply anaglyph separation: left eye gets grayscale, right eye gets color channels shifted
				wrappedX = (pixelX * 30 + pixelY * 70) / 100;
				wrappedY = (pixelX * 30 + neighborX * 70) / 100;

				// Update color values with anaglyph processing
				pixelX = neighborY;
				pixelY = wrappedX;
				neighborX = wrappedY;
			}

			// Store RGBA pixel data in texture buffer (4 bytes per pixel)
			// Pixel index to byte index: pixelIndex * 4
			this.textureData[pixelY << 2] = (byte)pixelX;
			this.textureData[(pixelY << 2) + 1] = (byte)pixelY;
			this.textureData[(pixelY << 2) + 2] = (byte)neighborX;
			this.textureData[(pixelY << 2) + 3] = -1;
		}
	}
}


