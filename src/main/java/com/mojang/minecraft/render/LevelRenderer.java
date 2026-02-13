package com.mojang.minecraft.render;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.Chunk;
import com.mojang.minecraft.render.ChunkDistanceComparator;
import com.mojang.minecraft.render.ShapeRenderer;
import com.mojang.minecraft.render.TextureManager;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * LevelRenderer handles rendering of the entire game level including terrain chunks, boundaries, and water.
 * It manages a 3D grid of chunks, sorts them by distance from the player, and renders them in proper order.
 * Also renders level boundaries and water surfaces as background elements.
 */
public final class LevelRenderer {

	/** Reference to the current game level for accessing terrain data. */
	public Level level;

	/** Texture manager for loading and binding level textures. */
	public TextureManager textureManager;

	/** OpenGL display list ID for rendering level boundaries and water. */
	public int listId;

	/** Integer buffer for storing OpenGL display list IDs for rendering. */
	public IntBuffer buffer = BufferUtils.createIntBuffer(65536);

	/** List of actively loaded chunks that are visible and need rendering. */
	public List chunks = new ArrayList();

	/** Queue of chunks waiting to be loaded in priority order. */
	private Chunk[] loadQueue;

	/** 3D cache of all chunk objects indexed by position. */
	public Chunk[] chunkCache;

	/** Number of chunks in the X dimension. */
	private int xChunks;

	/** Number of chunks in the Y dimension (vertical). */
	private int yChunks;

	/** Number of chunks in the Z dimension. */
	private int zChunks;

	/** Base OpenGL display list ID for all chunk rendering lists. */
	private int baseListId;

	/** Reference to the main Minecraft instance. */
	public Minecraft minecraft;

	/** Cache array for chunk display list data used during sorting. */
	private int[] chunkDataCache = new int['\uc350'];

	/** Counter for the number of ticks since level was created. */
	public int ticks = 0;

	/** Last known X position of the player for chunk sorting optimization. */
	private float lastLoadX = -9999.0F;

	/** Last known Y position of the player for chunk sorting optimization. */
	private float lastLoadY = -9999.0F;

	/** Last known Z position of the player for chunk sorting optimization. */
	private float lastLoadZ = -9999.0F;

	/** Animation time for rendering block break cracks and effects. */
	public float cracks;

	/**
	 * Constructs a new LevelRenderer and initializes OpenGL display lists.
	 *
	 * @param minecraft the Minecraft instance containing game state
	 * @param textureManager the TextureManager for loading textures
	 */
	public LevelRenderer(Minecraft minecraft, TextureManager textureManager) {
		// Store reference to the main Minecraft instance
		this.minecraft = minecraft;

		// Store reference to the texture manager for terrain texture loading
		this.textureManager = textureManager;

		// Request OpenGL display list IDs for level rendering (2 lists for boundaries and water)
		this.listId = GL11.glGenLists(2);

		// Request display list IDs for all possible chunks (256*64*2 chunks in world)
		this.baseListId = GL11.glGenLists(4096 << 6 << 1);
	}

	/**
	 * Refreshes the level renderer by rebuilding the chunk grid and rendering boundaries.
	 * Disposes of old chunks, creates new chunk objects for the current level dimensions,
	 * and renders static level boundaries and water surface as OpenGL display lists.
	 * This method must be called whenever the level is changed or resized.
	 */
	public final void refresh() {
		// Dispose of previously loaded chunks to free GPU memory
		int chunkIndex;
		if(this.chunkCache != null) {
			for(chunkIndex = 0; chunkIndex < this.chunkCache.length; ++chunkIndex) {
				this.chunkCache[chunkIndex].dispose();
			}
		}

		// Calculate the number of chunks in each dimension (each chunk is 16 blocks)
		this.xChunks = this.level.width / 16;
		this.yChunks = this.level.depth / 16;
		this.zChunks = this.level.height / 16;

		// Create arrays to store all chunk objects and the loading queue
		this.chunkCache = new Chunk[this.xChunks * this.yChunks * this.zChunks];
		this.loadQueue = new Chunk[this.xChunks * this.yChunks * this.zChunks];
		chunkIndex = 0;

		// Create chunk objects for all positions in the 3D grid
		int xIndex;
		int zIndex;
		for(xIndex = 0; xIndex < this.xChunks; ++xIndex) {
			for(int yIndex = 0; yIndex < this.yChunks; ++yIndex) {
				for(zIndex = 0; zIndex < this.zChunks; ++zIndex) {
					// Calculate 1D array index from 3D chunk coordinates
					int cacheIndex = (zIndex * this.yChunks + yIndex) * this.xChunks + xIndex;

					// Create chunk with position in blocks and assign display list ID
					this.chunkCache[cacheIndex] = new Chunk(this.level, xIndex << 4, yIndex << 4, zIndex << 4, 16, this.baseListId + chunkIndex);

					// Add chunk to loading queue
					this.loadQueue[cacheIndex] = this.chunkCache[cacheIndex];

					// Each chunk uses 2 display list IDs
					chunkIndex += 2;
				}
			}
		}

		// Clear previously loaded chunks list
		for(xIndex = 0; xIndex < this.chunks.size(); ++xIndex) {
			((Chunk)this.chunks.get(xIndex)).loaded = false;
		}

		this.chunks.clear();

		// Compile OpenGL display list for level boundaries and ground texture
		GL11.glNewList(this.listId, 4864);
		LevelRenderer levelRenderer = this;
		float boundaryColor = 0.5F;
		GL11.glColor4f(0.5F, boundaryColor, boundaryColor, 1.0F);
		ShapeRenderer shapeRenderer = ShapeRenderer.instance;

		// Get the ground level (void floor height)
		float groundLevel = this.level.getGroundLevel();

		// Calculate grid size for rendering boundaries (minimum 128 blocks, max level dimension)
		int gridSize = 128;
		if(128 > this.level.width) {
			gridSize = this.level.width;
		}

		if(gridSize > this.level.height) {
			gridSize = this.level.height;
		}

		// Calculate number of grid squares to extend beyond level boundaries
		int gridExtension = 2048 / gridSize;
		shapeRenderer.begin();

		// Render ground plane with texture (void floor at level bottom)
		int gridX;
		for(gridX = -gridSize * gridExtension; gridX < levelRenderer.level.width + gridSize * gridExtension; gridX += gridSize) {
			for(int gridZ = -gridSize * gridExtension; gridZ < levelRenderer.level.height + gridSize * gridExtension; gridZ += gridSize) {
				// Ground plane is at void level, or 0 if inside level boundaries
				boundaryColor = groundLevel;
				if(gridX >= 0 && gridZ >= 0 && gridX < levelRenderer.level.width && gridZ < levelRenderer.level.height) {
					boundaryColor = 0.0F;
				}

				// Submit quad vertices with texture coordinates
				shapeRenderer.vertexUV((float)gridX, boundaryColor, (float)(gridZ + gridSize), 0.0F, (float)gridSize);
				shapeRenderer.vertexUV((float)(gridX + gridSize), boundaryColor, (float)(gridZ + gridSize), (float)gridSize, (float)gridSize);
				shapeRenderer.vertexUV((float)(gridX + gridSize), boundaryColor, (float)gridZ, (float)gridSize, 0.0F);
				shapeRenderer.vertexUV((float)gridX, boundaryColor, (float)gridZ, 0.0F, 0.0F);
			}
		}

		shapeRenderer.end();

		// Render front and back walls (X-Z plane at top and bottom)
		GL11.glColor3f(0.8F, 0.8F, 0.8F);
		shapeRenderer.begin();

		for(gridX = 0; gridX < levelRenderer.level.width; gridX += gridSize) {
			// Front wall (at Z = 0)
			shapeRenderer.vertexUV((float)gridX, 0.0F, 0.0F, 0.0F, 0.0F);
			shapeRenderer.vertexUV((float)(gridX + gridSize), 0.0F, 0.0F, (float)gridSize, 0.0F);
			shapeRenderer.vertexUV((float)(gridX + gridSize), groundLevel, 0.0F, (float)gridSize, groundLevel);
			shapeRenderer.vertexUV((float)gridX, groundLevel, 0.0F, 0.0F, groundLevel);

			// Back wall (at Z = level.height)
			shapeRenderer.vertexUV((float)gridX, groundLevel, (float)levelRenderer.level.height, 0.0F, groundLevel);
			shapeRenderer.vertexUV((float)(gridX + gridSize), groundLevel, (float)levelRenderer.level.height, (float)gridSize, groundLevel);
			shapeRenderer.vertexUV((float)(gridX + gridSize), 0.0F, (float)levelRenderer.level.height, (float)gridSize, 0.0F);
			shapeRenderer.vertexUV((float)gridX, 0.0F, (float)levelRenderer.level.height, 0.0F, 0.0F);
		}

		GL11.glColor3f(0.6F, 0.6F, 0.6F);

		// Render left and right walls (Y-Z plane at X = 0 and X = level.width)
		for(gridX = 0; gridX < levelRenderer.level.height; gridX += gridSize) {
			// Left wall (at X = 0)
			shapeRenderer.vertexUV(0.0F, groundLevel, (float)gridX, 0.0F, 0.0F);
			shapeRenderer.vertexUV(0.0F, groundLevel, (float)(gridX + gridSize), (float)gridSize, 0.0F);
			shapeRenderer.vertexUV(0.0F, 0.0F, (float)(gridX + gridSize), (float)gridSize, groundLevel);
			shapeRenderer.vertexUV(0.0F, 0.0F, (float)gridX, 0.0F, groundLevel);

			// Right wall (at X = level.width)
			shapeRenderer.vertexUV((float)levelRenderer.level.width, 0.0F, (float)gridX, 0.0F, groundLevel);
			shapeRenderer.vertexUV((float)levelRenderer.level.width, 0.0F, (float)(gridX + gridSize), (float)gridSize, groundLevel);
			shapeRenderer.vertexUV((float)levelRenderer.level.width, groundLevel, (float)(gridX + gridSize), (float)gridSize, 0.0F);
			shapeRenderer.vertexUV((float)levelRenderer.level.width, groundLevel, (float)gridX, 0.0F, 0.0F);
		}

		shapeRenderer.end();
		GL11.glEndList();

		// Compile OpenGL display list for water surface
		GL11.glNewList(this.listId + 1, 4864);
		levelRenderer = this;
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		float waterLevel = this.level.getWaterLevel();
		GL11.glBlendFunc(770, 771);
		shapeRenderer = ShapeRenderer.instance;

		// Calculate water grid size
		int waterGridSize = 128;
		if(128 > this.level.width) {
			waterGridSize = this.level.width;
		}

		if(waterGridSize > this.level.height) {
			waterGridSize = this.level.height;
		}

		// Calculate water grid extension
		int waterGridExtension = 2048 / waterGridSize;
		shapeRenderer.begin();

		// Render water surface
		for(int waterGridX = -waterGridSize * waterGridExtension; waterGridX < levelRenderer.level.width + waterGridSize * waterGridExtension; waterGridX += waterGridSize) {
			for(int waterGridZ = -waterGridSize * waterGridExtension; waterGridZ < levelRenderer.level.height + waterGridSize * waterGridExtension; waterGridZ += waterGridSize) {
				// Water surface is slightly below water level
				float waterSurfaceY = waterLevel - 0.1F;

				// Only render water outside the level boundary or when water is below surface
				if(waterGridX < 0 || waterGridZ < 0 || waterGridX >= levelRenderer.level.width || waterGridZ >= levelRenderer.level.height) {
					// Submit water surface quad (two triangles worth of vertices for blending)
					shapeRenderer.vertexUV((float)waterGridX, waterSurfaceY, (float)(waterGridZ + waterGridSize), 0.0F, (float)waterGridSize);
					shapeRenderer.vertexUV((float)(waterGridX + waterGridSize), waterSurfaceY, (float)(waterGridZ + waterGridSize), (float)waterGridSize, (float)waterGridSize);
					shapeRenderer.vertexUV((float)(waterGridX + waterGridSize), waterSurfaceY, (float)waterGridZ, (float)waterGridSize, 0.0F);
					shapeRenderer.vertexUV((float)waterGridX, waterSurfaceY, (float)waterGridZ, 0.0F, 0.0F);

					// Second set of vertices for backface rendering
					shapeRenderer.vertexUV((float)waterGridX, waterSurfaceY, (float)waterGridZ, 0.0F, 0.0F);
					shapeRenderer.vertexUV((float)(waterGridX + waterGridSize), waterSurfaceY, (float)waterGridZ, (float)waterGridSize, 0.0F);
					shapeRenderer.vertexUV((float)(waterGridX + waterGridSize), waterSurfaceY, (float)(waterGridZ + waterGridSize), (float)waterGridSize, (float)waterGridSize);
					shapeRenderer.vertexUV((float)waterGridX, waterSurfaceY, (float)(waterGridZ + waterGridSize), 0.0F, (float)waterGridSize);
				}
			}
		}

		shapeRenderer.end();
		GL11.glDisable(3042);
		GL11.glEndList();

		// Queue all chunks in the level for loading
		this.queueChunks(0, 0, 0, this.level.width, this.level.depth, this.level.height);
	}

	/**
	 * Sorts chunks by distance from the player and renders visible chunks in proper order.
	 * Only re-sorts chunks if the player has moved more than 8 blocks from last sort position.
	 * Uses OpenGL display lists for efficient batch rendering of compiled chunk geometry.
	 *
	 * @param player the player whose position determines chunk sorting order
	 * @param maxChunksPerFrame maximum number of chunks to render per frame (-1 for all)
	 * @return the number of display list calls issued
	 */
	public final int sortChunks(Player player, int maxChunksPerFrame) {
		// Calculate player movement since last chunk sorting
		float distanceX = player.x - this.lastLoadX;
		float distanceY = player.y - this.lastLoadY;
		float distanceZ = player.z - this.lastLoadZ;

		// Re-sort chunks if player has moved more than 8 blocks (64 units squared)
		if(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ > 64.0F) {
			// Update last known player position
			this.lastLoadX = player.x;
			this.lastLoadY = player.y;
			this.lastLoadZ = player.z;

			// Sort all chunks by distance from player (closest first for efficient back-to-front rendering)
			Arrays.sort(this.loadQueue, new ChunkDistanceComparator(player));
		}

		// Accumulate display list IDs from chunks in sorted order
		int displayListCount = 0;

		for(int queueIndex = 0; queueIndex < this.loadQueue.length; ++queueIndex) {
			// Append chunk's display list IDs to cache, respecting max chunks per frame limit
			displayListCount = this.loadQueue[queueIndex].appendLists(this.chunkDataCache, displayListCount, maxChunksPerFrame);
		}

		// Transfer display list IDs to OpenGL buffer for rendering
		this.buffer.clear();
		this.buffer.put(this.chunkDataCache, 0, displayListCount);
		this.buffer.flip();

		// Render accumulated chunks if buffer contains data
		if(this.buffer.remaining() > 0) {
			// Bind terrain texture for chunk rendering
			GL11.glBindTexture(3553, this.textureManager.load("/terrain.png"));

			// Execute all accumulated display lists in sequence
			GL11.glCallLists(this.buffer);
		}

		// Return number of chunks rendered
		return this.buffer.remaining();
	}

	/**
	 * Queues a rectangular region of chunks for loading and rendering.
	 * Marks chunks in the specified region as loaded and adds them to the active chunk list.
	 * Coordinates are in block units and are automatically converted to chunk coordinates.
	 * Boundary coordinates are clamped to valid chunk grid ranges.
	 *
	 * @param minX the minimum X coordinate in blocks
	 * @param minY the minimum Y coordinate in blocks
	 * @param minZ the minimum Z coordinate in blocks
	 * @param maxX the maximum X coordinate in blocks
	 * @param maxY the maximum Y coordinate in blocks
	 * @param maxZ the maximum Z coordinate in blocks
	 */
	public final void queueChunks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		// Convert block coordinates to chunk coordinates (each chunk is 16 blocks)
		minX /= 16;
		minY /= 16;
		minZ /= 16;
		maxX /= 16;
		maxY /= 16;
		maxZ /= 16;

		// Clamp minimum coordinates to valid chunk grid range
		if(minX < 0) {
			minX = 0;
		}

		if(minY < 0) {
			minY = 0;
		}

		if(minZ < 0) {
			minZ = 0;
		}

		// Clamp maximum coordinates to valid chunk grid range
		if(maxX > this.xChunks - 1) {
			maxX = this.xChunks - 1;
		}

		if(maxY > this.yChunks - 1) {
			maxY = this.yChunks - 1;
		}

		if(maxZ > this.zChunks - 1) {
			maxZ = this.zChunks - 1;
		}

		// Iterate over all chunks in the specified region
		for(minX = minX; minX <= maxX; ++minX) {
			for(int yIndex = minY; yIndex <= maxY; ++yIndex) {
				for(int zIndex = minZ; zIndex <= maxZ; ++zIndex) {
					// Calculate 1D array index from 3D chunk coordinates
					Chunk chunk;
					if(!(chunk = this.chunkCache[(zIndex * this.yChunks + yIndex) * this.xChunks + minX]).loaded) {
						// Mark chunk as loaded
						chunk.loaded = true;

						// Add chunk to active rendering list
						this.chunks.add(this.chunkCache[(zIndex * this.yChunks + yIndex) * this.xChunks + minX]);
					}
				}
			}
		}
	}
}
