package com.mojang.minecraft.render;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * ShapeRenderer is a singleton utility class for rendering arbitrary 3D shapes with vertex, color, and texture data.
 * It provides a buffered vertex submission API that accumulates vertices and automatically flushes to OpenGL
 * when the buffer becomes full or when explicitly ended. Supports optional per-vertex colors and texture coordinates.
 */
public final class ShapeRenderer {

	/** Float buffer for storing vertex, color, and texture coordinate data (524288 floats ≈ 2MB). */
	private FloatBuffer buffer = BufferUtils.createFloatBuffer(524288);

	/** Backing array for vertex data before being transferred to the float buffer. */
	private float[] data = new float[524288];

	/** Current number of vertices that have been submitted. */
	private int vertices = 0;

	/** Texture U coordinate for the next vertex. */
	private float u;

	/** Texture V coordinate for the next vertex. */
	private float v;

	/** Red color component for the next vertex (normalized 0.0-1.0). */
	private float r;

	/** Green color component for the next vertex (normalized 0.0-1.0). */
	private float g;

	/** Blue color component for the next vertex (normalized 0.0-1.0). */
	private float b;

	/** Flag indicating whether color data should be included in vertices. */
	private boolean color = false;

	/** Flag indicating whether texture coordinate data should be included in vertices. */
	private boolean texture = false;

	/** Size of each vertex in float elements (3 base + 2 for texture + 3 for color). */
	private int vertexLength = 3;

	/** Current position in the data array. */
	private int length = 0;

	/** Flag to disable color data for vertices (forces grayscale rendering). */
	private boolean noColor = false;

	/** Singleton instance of ShapeRenderer. */
	public static ShapeRenderer instance = new ShapeRenderer();


	/**
	 * Flushes all accumulated vertex data to OpenGL and renders the queued vertices.
	 * Configures interleaved vertex arrays based on enabled data types (texture, color),
	 * submits the data to the GPU, and then clears the buffer for the next batch.
	 * If no vertices have been submitted, this method returns immediately without rendering.
	 */
	public final void end() {
		if(this.vertices > 0) {
			// Transfer vertex data from backing array to float buffer
			this.buffer.clear();
			this.buffer.put(this.data, 0, this.length);
			this.buffer.flip();

			// Configure OpenGL vertex array format based on which data types are present
			// OpenGL will interpret interleaved data according to the specified format
			if(this.texture && this.color) {
				// Format: texture coordinates (2) + color (3) + position (3)
				GL11.glInterleavedArrays(10794, 0, this.buffer);
			} else if(this.texture) {
				// Format: texture coordinates (2) + position (3)
				GL11.glInterleavedArrays(10791, 0, this.buffer);
			} else if(this.color) {
				// Format: color (3) + position (3)
				GL11.glInterleavedArrays(10788, 0, this.buffer);
			} else {
				// Format: position only (3)
				GL11.glInterleavedArrays(10785, 0, this.buffer);
			}

			// Enable vertex position array (always required)
			GL11.glEnableClientState('\u8074');

			// Enable texture coordinate array if texture data is present
			if(this.texture) {
				GL11.glEnableClientState('\u8078');
			}

			// Enable color array if color data is present
			if(this.color) {
				GL11.glEnableClientState('\u8076');
			}

			// Render all vertices as quads (mode 7 = GL_QUADS)
			GL11.glDrawArrays(7, 0, this.vertices);

			// Disable all enabled client-side arrays to prevent state leakage
			GL11.glDisableClientState('\u8074');
			if(this.texture) {
				GL11.glDisableClientState('\u8078');
			}

			if(this.color) {
				GL11.glDisableClientState('\u8076');
			}
		}

		// Reset state for next batch
		this.clear();
	}

	/**
	 * Clears the vertex buffer and resets internal counters.
	 * Prepares the renderer for accumulating a new batch of vertices.
	 */
	private void clear() {
		// Reset vertex count to zero
		this.vertices = 0;

		// Reset buffer to empty state
		this.buffer.clear();

		// Reset position in backing array
		this.length = 0;
	}

	/**
	 * Begins a new batch of vertex submission.
	 * Clears all accumulated vertex data and resets rendering flags to their initial state.
	 * Call this method before submitting vertices for rendering.
	 */
	public final void begin() {
		// Clear any previously accumulated vertex data
		this.clear();

		// Reset rendering flags for new batch
		this.color = false;
		this.texture = false;
		this.noColor = false;
	}

	/**
	 * Sets the color for subsequent vertices using normalized RGB components.
	 * The color will be applied to all vertices submitted after this call until changed.
	 * This method is ignored if noColor() has been called to disable color rendering.
	 *
	 * @param red the red component (0.0-1.0)
	 * @param green the green component (0.0-1.0)
	 * @param blue the blue component (0.0-1.0)
	 */
	public final void color(float red, float green, float blue) {
		if(!this.noColor) {
			// Enable color data if not already enabled and update vertex size
			if(!this.color) {
				this.vertexLength += 3;
			}

			// Store color components for the next vertex submission
			this.color = true;
			this.r = red;
			this.g = green;
			this.b = blue;
		}
	}

	/**
	 * Submits a vertex with texture coordinates to the vertex buffer.
	 * The vertex position, texture U and V coordinates are stored.
	 * Color data from previous color() calls will be included if set.
	 *
	 * @param x the X position of the vertex
	 * @param y the Y position of the vertex
	 * @param z the Z position of the vertex
	 * @param textureU the texture U coordinate (0.0-1.0)
	 * @param textureV the texture V coordinate (0.0-1.0)
	 */
	public final void vertexUV(float x, float y, float z, float textureU, float textureV) {
		// Enable texture coordinate data if not already enabled and update vertex size
		if(!this.texture) {
			this.vertexLength += 2;
		}

		// Store texture coordinates for the vertex
		this.texture = true;
		this.u = textureU;
		this.v = textureV;

		// Submit the vertex with its texture coordinates
		this.vertex(x, y, z);
	}

	/**
	 * Submits a vertex to the vertex buffer with interleaved data format.
	 * Stores texture coordinates (if set), color data (if set), and position.
	 * Automatically flushes the buffer when full or after every 4 vertices.
	 *
	 * @param x the X position of the vertex
	 * @param y the Y position of the vertex
	 * @param z the Z position of the vertex
	 */
	public final void vertex(float x, float y, float z) {
		// Store texture coordinates in the data array if texture mode is enabled
		if(this.texture) {
			this.data[this.length++] = this.u;
			this.data[this.length++] = this.v;
		}

		// Store color components in the data array if color mode is enabled
		if(this.color) {
			this.data[this.length++] = this.r;
			this.data[this.length++] = this.g;
			this.data[this.length++] = this.b;
		}

		// Always store vertex position coordinates
		this.data[this.length++] = x;
		this.data[this.length++] = y;
		this.data[this.length++] = z;

		// Increment vertex count
		++this.vertices;

		// Flush buffer when a quad is complete (4 vertices) and buffer is nearly full
		if(this.vertices % 4 == 0 && this.length >= 524288 - (this.vertexLength << 2)) {
			this.end();
		}
	}

	/**
	 * Sets the color for subsequent vertices using a packed 24-bit integer color.
	 * The color format is expected to be RRGGBB (8 bits red, 8 bits green, 8 bits blue).
	 * Color components are extracted, normalized to 0.0-1.0 range, and stored for vertex submission.
	 * This method is ignored if noColor() has been called to disable color rendering.
	 *
	 * @param packedColor the packed RGB color as a 24-bit integer (0xRRGGBB)
	 */
	public final void color(int packedColor) {
		// Extract red component from bits 16-23 of the packed color
		int red = packedColor >> 16 & 255;

		// Extract green component from bits 8-15 of the packed color
		int green = packedColor >> 8 & 255;

		// Extract blue component from bits 0-7 of the packed color
		int blue = packedColor & 255;

		// Convert from 8-bit integer components to normalized float components (0.0-1.0)
		if(!this.noColor) {
			// Enable color data if not already enabled and update vertex size
			if(!this.color) {
				this.vertexLength += 3;
			}

			// Store color flag and convert 8-bit values to normalized floats
			this.color = true;
			this.r = (float)(red & 255) / 255.0F;
			this.g = (float)(green & 255) / 255.0F;
			this.b = (float)(blue & 255) / 255.0F;
		}
	}

	/**
	 * Disables color rendering for subsequent vertices until begin() is called.
	 * Vertices submitted after this call will be rendered without color data,
	 * even if color() methods have been called previously.
	 */
	public final void noColor() {
		// Set flag to disable color data for all subsequent vertices
		this.noColor = true;
	}

	/**
	 * Sets the surface normal vector for lighting calculations.
	 * The normal vector should be normalized (unit length) for correct lighting results.
	 * This setting applies to all subsequently rendered geometry until changed.
	 *
	 * @param x the X component of the surface normal vector
	 * @param y the Y component of the surface normal vector
	 * @param z the Z component of the surface normal vector
	 */
	public final void normal(float x, float y, float z) {
		// Submit the normal vector to OpenGL for use in lighting calculations
		GL11.glNormal3f(x, y, z);
	}

}
