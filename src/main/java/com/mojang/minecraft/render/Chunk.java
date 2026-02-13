package com.mojang.minecraft.render;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.Frustrum;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.util.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Chunk represents a 16x16x16 block of terrain data compiled into OpenGL display lists for efficient rendering.
 * Each chunk maintains two display lists for opaque and transparent geometry rendering passes.
 * Chunks handle frustum culling visibility tests, dirty state tracking, and batch geometry compilation.
 * This class is the fundamental unit of terrain rendering in the Minecraft Classic Forever client.
 */
public final class Chunk {

	/** Reference to the game level containing block data for this chunk. */
	private Level level;

	/** Base OpenGL display list ID for rendering this chunk (offset for both passes). */
	private int baseListId = -1;

	/** Static reference to the global ShapeRenderer singleton for vertex submission. */
	private static ShapeRenderer renderer = ShapeRenderer.instance;

	/** Static counter tracking total number of chunk updates performed (for debugging). */
	public static int chunkUpdates = 0;

	/** X coordinate of this chunk's origin in block units. */
	private int x;

	/** Y coordinate of this chunk's origin in block units. */
	private int y;

	/** Z coordinate of this chunk's origin in block units. */
	private int z;

	/** Width of this chunk in blocks (always 16 for standard chunks). */
	private int width;

	/** Height of this chunk in blocks (always 16 for standard chunks). */
	private int height;

	/** Depth of this chunk in blocks (always 16 for standard chunks). */
	private int depth;

	/** Flag indicating whether this chunk is visible within the camera frustum. */
	public boolean visible = false;

	/** Array of dirty flags for each rendering pass (opaque=0, transparent=1). */
	private boolean[] dirty = new boolean[2];

	/** Flag indicating whether this chunk has been loaded and compiled. */
	public boolean loaded;

	/**
	 * Constructs a new Chunk with the specified position and display list IDs.
	 * Initializes chunk dimensions to 16x16x16 and marks all rendering passes as dirty
	 * (requiring compilation). The chunk diagonal distance is pre-calculated but not stored.
	 *
	 * @param level the game level containing block data for this chunk
	 * @param x the X coordinate of the chunk origin in blocks
	 * @param y the Y coordinate of the chunk origin in blocks
	 * @param z the Z coordinate of the chunk origin in blocks
	 * @param chunkSize the size of the chunk in blocks (typically 16)
	 * @param displayListId the base OpenGL display list ID for this chunk
	 */
	public Chunk(Level level, int x, int y, int z, int chunkSize, int displayListId) {
		// Store reference to the level containing this chunk's block data
		this.level = level;

		// Store the chunk's origin coordinates in block units
		this.x = x;
		this.y = y;
		this.z = z;

		// Set chunk dimensions to 16x16x16 (standard chunk size)
		this.width = this.height = this.depth = 16;

		// Pre-calculate the chunk's diagonal distance for frustum culling optimization
		// Result is not stored but computed for validation
		MathHelper.sqrt((float)(this.width * this.width + this.height * this.height + this.depth * this.depth));

		// Store the base display list ID for rendering both opaque and transparent passes
		this.baseListId = displayListId;

		// Mark all rendering passes as dirty (needing compilation)
		this.setAllDirty();
	}

	/**
	 * Compiles the chunk's geometry into OpenGL display lists for efficient rendering.
	 * Iterates through all blocks in the chunk and submits their faces to the renderer,
	 * organized into two passes: opaque geometry (pass 0) and transparent geometry (pass 1).
	 * Each pass is compiled into its own OpenGL display list and marked clean if successfully rendered.
	 * Stops at the first pass that produces no geometry (optimization for opaque-only chunks).
	 */
	public final void update() {
		// Increment the global chunk update counter for statistics tracking
		++chunkUpdates;

		// Define the boundary coordinates of this chunk in block units
		int minX = this.x;
		int minY = this.y;
		int minZ = this.z;
		int maxX = this.x + this.width;
		int maxY = this.y + this.height;
		int maxZ = this.z + this.depth;

		// Mark all rendering passes as requiring recompilation
		int renderPass;
		for(renderPass = 0; renderPass < 2; ++renderPass) {
			this.dirty[renderPass] = true;
		}

		// Compile geometry for both rendering passes (opaque and transparent)
		for(renderPass = 0; renderPass < 2; ++renderPass) {
			// Flags to track if any geometry was encountered in this pass
			boolean hasOpaqueGeometry = false;
			boolean renderedGeometry = false;

			// Start compiling a new OpenGL display list for this rendering pass
			GL11.glNewList(this.baseListId + renderPass, 4864);

			// Begin vertex submission for this pass
			renderer.begin();

			// Iterate through all blocks in this chunk
			for(int blockX = minX; blockX < maxX; ++blockX) {
				for(int blockY = minY; blockY < maxY; ++blockY) {
					for(int blockZ = minZ; blockZ < maxZ; ++blockZ) {
						// Get the block type at this position
						int blockTypeId;
						if((blockTypeId = this.level.getTile(blockX, blockY, blockZ)) > 0) {
							// Get the block class for rendering
							Block block;
							if((block = Block.blocks[blockTypeId]).getRenderPass() != renderPass) {
								// This block belongs to a different rendering pass (opaque/transparent)
								// Mark that we encountered geometry for other passes
								hasOpaqueGeometry = true;
							} else {
								// This block belongs to the current rendering pass
								// Submit the block's faces to the renderer and track if any faces were rendered
								renderedGeometry |= block.render(this.level, blockX, blockY, blockZ, renderer);
							}
						}
					}
				}
			}

			// Flush all pending vertices to the display list
			renderer.end();

			// End the OpenGL display list compilation
			GL11.glEndList();

			// Mark this pass as clean (not dirty) if any geometry was successfully rendered
			if(renderedGeometry) {
				this.dirty[renderPass] = false;
			}

			// Optimization: stop if no opaque geometry exists (transparent-only chunks)
			if(!hasOpaqueGeometry) {
				break;
			}
		}
	}

	/**
	 * Calculates the squared Euclidean distance from the player to this chunk's origin.
	 * Uses squared distance to avoid expensive square root calculation, suitable for comparisons.
	 * The distance is measured from the player position to the chunk's minimum coordinate corner (x, y, z).
	 *
	 * @param player the player whose distance from the chunk is being calculated
	 * @return the squared distance from the player to this chunk's origin in block units
	 */
	public final float distanceSquared(Player player) {
		// Calculate the difference in X coordinates between player and chunk origin
		float deltaX = player.x - (float)this.x;

		// Calculate the difference in Y coordinates between player and chunk origin
		float deltaY = player.y - (float)this.y;

		// Calculate the difference in Z coordinates between player and chunk origin
		float deltaZ = player.z - (float)this.z;

		// Return the squared Euclidean distance (sum of squared components)
		return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
	}

	/**
	 * Marks all rendering passes as dirty (requiring recompilation).
	 * This forces the chunk's geometry to be regenerated during the next update call.
	 * Used when block data changes, frustum culling changes, or the chunk is first created.
	 */
	private void setAllDirty() {
		// Iterate through both rendering passes (opaque and transparent)
		for(int passIndex = 0; passIndex < 2; ++passIndex) {
			// Mark this rendering pass as dirty (needing recompilation)
			this.dirty[passIndex] = true;
		}
	}

	/**
	 * Disposes of this chunk's resources and clears references.
	 * Marks the chunk as dirty (display lists will be recreated if needed) and releases the level reference.
	 * This method should be called when the chunk is no longer needed or when the level changes.
	 */
	public final void dispose() {
		// Mark all rendering passes as dirty so display lists are regenerated if needed
		this.setAllDirty();

		// Clear the level reference to allow garbage collection
		this.level = null;
	}

	/**
	 * Appends this chunk's display list ID to the render queue if the chunk is visible and ready.
	 * Used during rendering to accumulate all visible chunk display lists for batch execution.
	 * Only includes chunks that have been compiled (not dirty) to avoid rendering incomplete geometry.
	 *
	 * @param displayLists array to accumulate display list IDs for rendering
	 * @param offset the current position in the display list array to append to
	 * @param renderPass the rendering pass (0=opaque, 1=transparent) to append
	 * @return the updated offset after appending (offset unchanged if chunk not included)
	 */
	public final int appendLists(int[] displayLists, int offset, int renderPass) {
		// Only append display lists for visible chunks
		if(!this.visible) {
			return offset;
		} else {
			// Only append if this rendering pass has been compiled and is not dirty
			if(!this.dirty[renderPass]) {
				// Append the display list ID for this chunk and rendering pass
				displayLists[offset++] = this.baseListId + renderPass;
			}

			// Return the updated offset in the display list array
			return offset;
		}
	}

	/**
	 * Tests whether this chunk's bounding box is within the camera's view frustum.
	 * Updates the chunk's visibility status based on frustum culling test results.
	 * Chunks that fail the frustum test are marked as invisible and will not be rendered.
	 *
	 * @param frustum the view frustum for culling visibility tests
	 */
	public final void clip(Frustrum frustum) {
		// Test if this chunk's axis-aligned bounding box intersects the camera frustum
		// Chunk bounds are defined by corner (x, y, z) and (x+width, y+height, z+depth)
		this.visible = frustum.isBoxInFrustrum(
			(float)this.x, (float)this.y, (float)this.z,
			(float)(this.x + this.width), (float)(this.y + this.height), (float)(this.z + this.depth)
		);
	}

}
