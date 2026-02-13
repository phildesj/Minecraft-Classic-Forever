package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.liquid.LiquidType;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.render.ShapeRenderer;

import java.util.Random;

/**
 * Represents a liquid block (water or lava) in Minecraft Classic.
 * Liquids are non-solid, non-opaque blocks that flow and spread to adjacent spaces.
 * Water flows downward and horizontally, while lava flows more slowly and is affected by sponges.
 * When water and lava mix, they create stone blocks, implementing the classic crafting mechanic.
 *
 * @author Mojang
 */
public class LiquidBlock extends Block {

	/**
	 * The type of liquid this block represents (WATER or LAVA).
	 * Determines flow behavior, brightness, and rendering properties.
	 */
	protected LiquidType liquidType;

	/**
	 * The block ID of the still liquid variant (e.g., stationary water or lava).
	 * Used when liquid is not flowing or spreading.
	 */
	protected int stillLiquidId;

	/**
	 * The block ID of the flowing/moving liquid variant.
	 * Used when liquid is actively flowing or spreading.
	 */
	protected int flowingLiquidId;


	/**
	 * Constructs a LiquidBlock with the specified block ID and liquid type.
	 * Initializes liquid physics, bounds, texture, and determines still/flowing variant IDs.
	 * Lava blocks have a slower tick delay than water blocks.
	 *
	 * @param blockId the unique block ID for the flowing liquid
	 * @param liquidType the type of liquid (WATER or LAVA)
	 */
	protected LiquidBlock(int blockId, LiquidType liquidType) {
		super(blockId);
		this.liquidType = liquidType;
		this.textureId = 14;
		if(liquidType == LiquidType.LAVA) {
			this.textureId = 30;
		}

		Block.liquid[blockId] = true;
		this.flowingLiquidId = blockId;
		this.stillLiquidId = blockId + 1;
		float borderWidth = 0.01F;
		float sizeOffset = 0.1F;
		this.setBounds(borderWidth + 0.0F, 0.0F - sizeOffset + borderWidth, borderWidth + 0.0F, borderWidth + 1.0F, 1.0F - sizeOffset + borderWidth, borderWidth + 1.0F);
		this.setPhysics(true);
		if(liquidType == LiquidType.LAVA) {
			this.setTickDelay(16);
		}

	}

	/**
	 * Determines if this liquid block is a cube.
	 * Liquid blocks are not cubes - they have reduced bounds for flow behavior.
	 *
	 * @return false, indicating liquids are not cubes
	 */
	@Override
	public final boolean isCube() {
		return false;
	}

	/**
	 * Called when this liquid block is placed in the world.
	 * Adds the liquid position to the tick queue to initiate spreading and flow.
	 *
	 * @param level the level containing this liquid
	 * @param x the x coordinate of the liquid block
	 * @param y the y coordinate of the liquid block
	 * @param z the z coordinate of the liquid block
	 */
	@Override
	public final void onPlace(Level level, int x, int y, int z) {
		level.addToTickNextTick(x, y, z, this.flowingLiquidId);
	}

	/**
	 * Updates the liquid block each game tick.
	 * Handles spreading downward, then horizontally (water) or just checks if flowing (lava).
	 * Creates stone when water and lava meet, and respects sponge blocks blocking water.
	 *
	 * @param level the level containing this liquid
	 * @param x the x coordinate of the liquid block
	 * @param y the y coordinate of the liquid block
	 * @param z the z coordinate of the liquid block
	 * @param randomGenerator random number generator for spread calculations
	 */
	@Override
	public void update(Level level, int x, int y, int z, Random randomGenerator) {
		boolean hasSpreadHorizontally = false;
		z = z;
		y = y;
		x = x;
		level = level;
		boolean hasSpreadDownward = false;

		boolean canFlowDown;
		do {
			--y;
			if(level.getTile(x, y, z) != 0 || !this.canFlow(level, x, y, z)) {
				break;
			}

			if(canFlowDown = level.setTile(x, y, z, this.flowingLiquidId)) {
				hasSpreadDownward = true;
			}
		} while(canFlowDown && this.liquidType != LiquidType.LAVA);

		++y;
		if(this.liquidType == LiquidType.WATER || !hasSpreadDownward) {
			hasSpreadDownward = hasSpreadDownward | this.flow(level, x - 1, y, z) | this.flow(level, x + 1, y, z) | this.flow(level, x, y, z - 1) | this.flow(level, x, y, z + 1);
		}

		if(!hasSpreadDownward) {
			level.setTileNoUpdate(x, y, z, this.stillLiquidId);
		} else {
			level.addToTickNextTick(x, y, z, this.flowingLiquidId);
		}

	}

	/**
	 * Checks if liquid can flow to a specific location.
	 * Water cannot flow near sponge blocks (within a 5x5x5 area).
	 * Lava can always flow regardless of surroundings.
	 *
	 * @param level the level containing the liquid
	 * @param checkX the x coordinate to check for obstructions
	 * @param checkY the y coordinate to check for obstructions
	 * @param checkZ the z coordinate to check for obstructions
	 * @return true if the liquid can flow to this location, false if blocked by sponge
	 */
	private boolean canFlow(Level level, int checkX, int checkY, int checkZ) {
		if(this.liquidType == LiquidType.WATER) {
			// Check for sponge blocks in a 5x5x5 area around the flow destination
			for(int spongeCheckX = checkX - 2; spongeCheckX <= checkX + 2; ++spongeCheckX) {
				for(int spongeCheckY = checkY - 2; spongeCheckY <= checkY + 2; ++spongeCheckY) {
					for(int spongeCheckZ = checkZ - 2; spongeCheckZ <= checkZ + 2; ++spongeCheckZ) {
						if(level.getTile(spongeCheckX, spongeCheckY, spongeCheckZ) == Block.SPONGE.id) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 * Attempts to flow the liquid to an adjacent space horizontally.
	 * If the adjacent space is empty and flow is allowed, spreads the liquid.
	 *
	 * @param level the level containing the liquid
	 * @param neighborX the x coordinate of the neighboring block
	 * @param neighborY the y coordinate of the neighboring block
	 * @param neighborZ the z coordinate of the neighboring block
	 * @return true if the liquid successfully spread, false otherwise
	 */
	private boolean flow(Level level, int neighborX, int neighborY, int neighborZ) {
		if(level.getTile(neighborX, neighborY, neighborZ) == 0) {
			if(!this.canFlow(level, neighborX, neighborY, neighborZ)) {
				return false;
			}

			if(level.setTile(neighborX, neighborY, neighborZ, this.flowingLiquidId)) {
				level.addToTickNextTick(neighborX, neighborY, neighborZ, this.flowingLiquidId);
			}
		}

		return false;
	}

	/**
	 * Gets the brightness value for this liquid block.
	 * Lava emits light (brightness value 100.0F), water reflects ambient light.
	 *
	 * @param level the level containing this liquid
	 * @param x the x coordinate of the liquid block
	 * @param y the y coordinate of the liquid block
	 * @param z the z coordinate of the liquid block
	 * @return 100.0F for lava (full brightness), or ambient brightness for water
	 */
	@Override
	protected final float getBrightness(Level level, int x, int y, int z) {
		return this.liquidType == LiquidType.LAVA?100.0F: level.getBrightness(x, y, z);
	}

	/**
	 * Determines if a particular side of this liquid block should be rendered.
	 * Liquids render differently based on whether the adjacent block is also a liquid variant.
	 * Top faces only render if surrounded by empty spaces (liquid surface detection).
	 *
	 * @param level the level containing this liquid
	 * @param x the x coordinate of the adjacent block
	 * @param y the y coordinate of the adjacent block
	 * @param z the z coordinate of the adjacent block
	 * @param sideIndex the side direction (0-5)
	 * @return true if this side should be rendered, false if culled
	 */
	@Override
	public final boolean canRenderSide(Level level, int x, int y, int z, int sideIndex) {
		int adjacentBlockId;
		return x >= 0 && y >= 0 && z >= 0 && x < level.width && z < level.height?((adjacentBlockId = level.getTile(x, y, z)) != this.flowingLiquidId && adjacentBlockId != this.stillLiquidId?(sideIndex == 1 && (level.getTile(x - 1, y, z) == 0 || level.getTile(x + 1, y, z) == 0 || level.getTile(x, y, z - 1) == 0 || level.getTile(x, y, z + 1) == 0)?true:super.canRenderSide(level, x, y, z, sideIndex)):false):false;
	}

	/**
	 * Renders the inside of this liquid block and its side faces.
	 * Combines internal and side rendering for proper liquid appearance.
	 *
	 * @param shapeRenderer the shape renderer to use for drawing
	 * @param x the x coordinate of the liquid block
	 * @param y the y coordinate of the liquid block
	 * @param z the z coordinate of the liquid block
	 * @param sideIndex the side direction to render (0-5)
	 */
	@Override
	public final void renderInside(ShapeRenderer shapeRenderer, int x, int y, int z, int sideIndex) {
		super.renderInside(shapeRenderer, x, y, z, sideIndex);
		super.renderSide(shapeRenderer, x, y, z, sideIndex);
	}

	/**
	 * Determines if this liquid block is opaque.
	 * Liquids are opaque - they block light from passing through.
	 *
	 * @return true, indicating liquids are opaque
	 */
	@Override
	public final boolean isOpaque() {
		return true;
	}

	/**
	 * Determines if this liquid block is solid.
	 * Liquids are not solid - entities can move through them.
	 *
	 * @return false, indicating liquids are not solid
	 */
	@Override
	public final boolean isSolid() {
		return false;
	}

	/**
	 * Gets the type of liquid this block represents.
	 * Allows other systems to determine if this is water or lava.
	 *
	 * @return the LiquidType of this block (WATER or LAVA)
	 */
	@Override
	public final LiquidType getLiquidType() {
		return this.liquidType;
	}

	/**
	 * Called when an adjacent block changes.
	 * When water and lava meet, creates stone blocks (the classic water+lava=stone mechanic).
	 * Otherwise, adds this liquid to the tick queue to continue spreading.
	 *
	 * @param level the level containing this liquid
	 * @param x the x coordinate of this liquid block
	 * @param y the y coordinate of this liquid block
	 * @param z the z coordinate of this liquid block
	 * @param changedBlockId the block ID of the neighbor that changed
	 */
	@Override
	public void onNeighborChange(Level level, int x, int y, int z, int changedBlockId) {
		if(changedBlockId != 0) {
			LiquidType neighborLiquidType = Block.blocks[changedBlockId].getLiquidType();
			if(this.liquidType == LiquidType.WATER && neighborLiquidType == LiquidType.LAVA || neighborLiquidType == LiquidType.WATER && this.liquidType == LiquidType.LAVA) {
				level.setTile(x, y, z, Block.STONE.id);
				return;
			}
		}

		level.addToTickNextTick(x, y, z, changedBlockId);
	}

	/**
	 * Gets the tick delay for this liquid block.
	 * Lava updates slower (every 5 ticks) than water (every tick).
	 * This creates the gameplay difference where lava flows more slowly.
	 *
	 * @return 5 for lava, 0 for water
	 */
	@Override
	public final int getTickDelay() {
		return this.liquidType == LiquidType.LAVA?5:0;
	}

	/**
	 * Handles dropping items when this liquid block breaks.
	 * Liquids do not drop any items when destroyed.
	 *
	 * @param level the level containing this liquid
	 * @param x the x coordinate of the liquid block
	 * @param y the y coordinate of the liquid block
	 * @param z the z coordinate of the liquid block
	 * @param dropChance the probability multiplier for item drops (unused for liquids)
	 */
	@Override
	public final void dropItems(Level level, int x, int y, int z, float dropChance) {}

	/**
	 * Called when this liquid block is broken in the world.
	 * Liquids perform no special action when broken.
	 *
	 * @param level the level containing this liquid
	 * @param x the x coordinate of the liquid block
	 * @param y the y coordinate of the liquid block
	 * @param z the z coordinate of the liquid block
	 */
	@Override
	public final void onBreak(Level level, int x, int y, int z) {}

	/**
	 * Gets the number of items dropped when this liquid is broken.
	 * Liquids never drop items.
	 *
	 * @return 0, indicating no items are dropped
	 */
	@Override
	public final int getDropCount() {
		return 0;
	}

	/**
	 * Gets the render pass for this liquid block.
	 * Water uses render pass 1 (transparent), lava uses render pass 0 (opaque).
	 * This controls when the liquid is rendered relative to other blocks.
	 *
	 * @return 1 for water (render after opaque blocks), 0 for lava (render with opaque blocks)
	 */
	@Override
	public final int getRenderPass() {
		return this.liquidType == LiquidType.WATER?1:0;
	}

	/**
	 * Gets the collision box for this liquid block.
	 * Liquids have no collision boxes - entities move freely through them.
	 *
	 * @param x the x coordinate of the liquid block
	 * @param y the y coordinate of the liquid block
	 * @param z the z coordinate of the liquid block
	 * @return null, indicating no collision box
	 */
	@Override
	public AABB getCollisionBox(int x, int y, int z) {
		return null;
	}
}


