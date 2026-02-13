package com.mojang.minecraft.render.texture;

/**
 * TextureFX is the base class for animated textures in the Minecraft Classic Forever client.
 * Subclasses implement the animate() method to modify texture pixel data each frame for effects
 * such as water, lava, and fire animations. Each texture animation is associated with an OpenGL
 * texture ID and can optionally apply anaglyph 3D color processing.
 */
public class TextureFX {
	/**
	 * Constructs a new TextureFX animation for the specified OpenGL texture.
	 *
	 * @param textureID the OpenGL texture ID that this animation modifies
	 */
	public TextureFX(int textureID) {
		// Store the OpenGL texture ID for updating during animation
		this.textureId = textureID;
	}

	/** Pixel data buffer for the animated texture (1024 bytes = 16x16 pixels at 4 bytes per pixel RGBA). */
	public byte[] textureData = new byte[1024];

	/** The OpenGL texture ID that this animation modifies and updates each frame. */
	public int textureId;

	/** Flag indicating whether to apply anaglyph 3D color processing to this animation. */
	public boolean anaglyph = false;

	/**
	 * Updates the texture animation state for the current frame.
	 * Subclasses should override this method to modify textureData each frame for animation effects.
	 * Called once per frame before texture data is uploaded to the GPU.
	 * Default implementation does nothing (no animation).
	 */
	public void animate() {
	}
}


