package com.mojang.minecraft.render;

import com.mojang.minecraft.GameSettings;
import com.mojang.minecraft.render.texture.TextureFX;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * TextureManager handles loading, caching, and rendering of textures for the Minecraft Classic Forever client.
 * It manages OpenGL texture objects, maintains a cache of loaded textures, and supports texture animations
 * such as anaglyph 3D rendering and dynamic texture effects.
 */
public class TextureManager {

	/** Cache mapping texture resource paths to their OpenGL texture IDs. */
	public HashMap textures = new HashMap();

	/** Cache mapping OpenGL texture IDs to their BufferedImage data for reloading. */
	public HashMap textureImages = new HashMap();

	/** OpenGL buffer for storing generated texture IDs. */
	public IntBuffer idBuffer = BufferUtils.createIntBuffer(1);

	/** Byte buffer for storing raw texture pixel data (262144 bytes = 512x512 RGBA). */
	public ByteBuffer textureBuffer = BufferUtils.createByteBuffer(262144);

	/** List of texture animations to be updated each frame. */
	public List animations = new ArrayList();

	/** Reference to the game settings for retrieving configuration like anaglyph mode. */
	public GameSettings settings;

	/**
	 * Constructs a new TextureManager with the specified game settings.
	 *
	 * @param var1 the GameSettings instance containing game configuration
	 */
	public TextureManager(GameSettings var1) {
		// Store the game settings reference for accessing configuration options
		this.settings = var1;
	}

	/**
	 * Loads a texture from the classpath resource and caches it.
	 * If the texture is already loaded, returns the cached OpenGL texture ID.
	 * Textures prefixed with "##" are processed through load1() for special handling.
	 *
	 * @param resourcePath the classpath resource path to the texture file
	 * @return the OpenGL texture ID for use in rendering
	 * @throws RuntimeException if the texture file cannot be read
	 */
	public final int load(String resourcePath) {
		// Check if texture is already cached to avoid reloading
		Integer cachedTextureId;
		if((cachedTextureId = (Integer)this.textures.get(resourcePath)) != null) {
			return cachedTextureId.intValue();
		} else {
			try {
				// Clear the ID buffer and request a new texture ID from OpenGL
				this.idBuffer.clear();
				GL11.glGenTextures(this.idBuffer);
				int textureId = this.idBuffer.get(0);

				// Handle special texture processing for anaglyph and animation textures
				if(resourcePath.startsWith("##")) {
					this.load(load1(ImageIO.read(TextureManager.class.getResourceAsStream(resourcePath.substring(2)))), textureId);
				} else {
					this.load(ImageIO.read(TextureManager.class.getResourceAsStream(resourcePath)), textureId);
				}

				// Cache the texture ID for future use
				this.textures.put(resourcePath, Integer.valueOf(textureId));
				return textureId;
			} catch (IOException var3) {
				throw new RuntimeException("!!");
			}
		}
	}

	/**
	 * Processes an animated texture by arranging frames horizontally.
	 * Takes a texture with multiple animation frames stacked vertically and rearranges them
	 * into a single-strip texture suitable for animation processing.
	 *
	 * @param animatedTexture the input texture containing stacked animation frames
	 * @return a new BufferedImage with frames arranged in a single horizontal strip
	 */
	public static BufferedImage load1(BufferedImage animatedTexture) {
		// Calculate the number of animation frames by dividing width into 16-pixel segments
		int frameCount = animatedTexture.getWidth() / 16;

		// Create a new image buffer to hold the rearranged frames
		BufferedImage rearrangedTexture;
		Graphics graphics = (rearrangedTexture = new BufferedImage(16, animatedTexture.getHeight() * frameCount, 2)).getGraphics();

		// Draw each animation frame in sequence, moving them horizontally
		for(int frameIndex = 0; frameIndex < frameCount; ++frameIndex) {
			// Draw frame at horizontal offset, stacked vertically in the output
			graphics.drawImage(animatedTexture, -frameIndex << 4, frameIndex * animatedTexture.getHeight(), (ImageObserver)null);
		}

		// Release graphics resources
		graphics.dispose();
		return rearrangedTexture;
	}

	/**
	 * Loads a BufferedImage into OpenGL as a texture and caches the image data.
	 * Automatically generates a new OpenGL texture ID and uploads the image data.
	 *
	 * @param image the BufferedImage containing texture data to load
	 * @return the OpenGL texture ID assigned to this texture
	 */
	public final int load(BufferedImage image) {
		// Clear the ID buffer and request a new texture ID from OpenGL
		this.idBuffer.clear();
		GL11.glGenTextures(this.idBuffer);
		int textureId = this.idBuffer.get(0);

		// Upload the image data to the OpenGL texture
		this.load(image, textureId);

		// Cache the original image data for reloading when settings change (e.g., anaglyph mode)
		this.textureImages.put(Integer.valueOf(textureId), image);
		return textureId;
	}

	/**
	 * Uploads a BufferedImage to OpenGL as a texture with pixel format conversion and anaglyph processing.
	 * Converts ARGB pixel data to RGBA format and applies anaglyph 3D color filtering if enabled.
	 * This method handles the low-level OpenGL texture upload and pixel processing.
	 *
	 * @param image the BufferedImage containing texture pixel data
	 * @param textureId the OpenGL texture ID to upload the data to
	 */
	public void load(BufferedImage image, int textureId) {
		// Bind the texture and set filtering parameters
		GL11.glBindTexture(3553, textureId);
		GL11.glTexParameteri(3553, 10241, 9728);
		GL11.glTexParameteri(3553, 10240, 9728);

		// Extract image dimensions
		int width = image.getWidth();
		int height = image.getHeight();

		// Create arrays to hold pixel data during processing
		// pixelData stores original ARGB pixels, pixelBytes stores converted RGBA bytes
		int[] pixelData = new int[width * height];
		byte[] pixelBytes = new byte[width * height << 2];

		// Read all pixel data from the image
		image.getRGB(0, 0, width, height, pixelData, 0, width);

		// Convert each pixel from ARGB to RGBA format and apply anaglyph effect if enabled
		for(int pixelIndex = 0; pixelIndex < pixelData.length; ++pixelIndex) {
			// Extract color components from ARGB pixel
			int alpha = pixelData[pixelIndex] >>> 24;
			int red = pixelData[pixelIndex] >> 16 & 255;
			int green = pixelData[pixelIndex] >> 8 & 255;
			int blue = pixelData[pixelIndex] & 255;

			// Apply anaglyph 3D effect by converting color to grayscale and separating channels
			if(this.settings.anaglyph) {
				// Calculate grayscale intensity using standard luminance formula
				int grayscale = (red * 30 + green * 59 + blue * 11) / 100;

				// Apply anaglyph formula: enhance red in one eye, cyan (green+blue) in the other
				green = (red * 30 + green * 70) / 100;
				blue = (red * 30 + blue * 70) / 100;
				red = grayscale;
			}

			// Store converted pixel data in RGBA format in the byte buffer
			pixelBytes[pixelIndex << 2] = (byte)red;
			pixelBytes[(pixelIndex << 2) + 1] = (byte)green;
			pixelBytes[(pixelIndex << 2) + 2] = (byte)blue;
			pixelBytes[(pixelIndex << 2) + 3] = (byte)alpha;
		}

		// Upload the converted pixel data to OpenGL
		this.textureBuffer.clear();
		this.textureBuffer.put(pixelBytes);
		this.textureBuffer.position(0).limit(pixelBytes.length);
		GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 6408, 5121, this.textureBuffer);
	}

	/**
	 * Registers a texture animation to be updated and rendered each frame.
	 * This method adds the animation to the internal list and initializes it.
	 *
	 * @param textureFX the TextureFX animation object to register
	 */
	public final void registerAnimation(TextureFX textureFX) {
		// Add the animation to the list of active animations
		this.animations.add(textureFX);

		// Initialize the animation by calling its animate method
		textureFX.animate();
	}
}
