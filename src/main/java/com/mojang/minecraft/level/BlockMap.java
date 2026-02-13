package com.mojang.minecraft.level;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.BlockMap$Slot;
import com.mojang.minecraft.level.SyntheticClass;
import com.mojang.minecraft.model.Vec3D;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.render.Frustrum;
import com.mojang.minecraft.render.TextureManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a spatial grid-based entity management system for efficient entity querying.
 * BlockMap divides the game world into 16x16x16 unit cells for faster collision detection.
 * Entities are stored in a 3D grid allowing quick queries of nearby entities without checking all.
 * When entities move between grid cells, they are automatically updated to maintain grid integrity.
 * This spatial partitioning enables efficient rendering, collision detection, and entity updates.
 *
 * @author Mojang
 */
public class BlockMap implements Serializable {

	/**
	 * Serial version UID for serialization compatibility.
	 */
	public static final long serialVersionUID = 0L;

	/**
	 * Width of the entity grid in cells (each cell is 16 units).
	 * Calculated as worldWidth / 16, with minimum of 1 cell.
	 */
	private int gridWidth;

	/**
	 * Depth (Y-axis) of the entity grid in cells (each cell is 16 units).
	 * Calculated as worldDepth / 16, with minimum of 1 cell.
	 */
	private int gridDepth;

	/**
	 * Height (Z-axis) of the entity grid in cells (each cell is 16 units).
	 * Calculated as worldHeight / 16, with minimum of 1 cell.
	 */
	private int gridHeight;

	/**
	 * Slot helper for calculating grid positions at minimum coordinates.
	 * Reused for efficiency to avoid allocations during queries.
	 */
	private BlockMap$Slot slotMin = new BlockMap$Slot(this, (SyntheticClass)null);

	/**
	 * Slot helper for calculating grid positions at maximum coordinates.
	 * Reused for efficiency to avoid allocations during queries.
	 */
	private BlockMap$Slot slotMax = new BlockMap$Slot(this, (SyntheticClass)null);

	/**
	 * 3D grid of entity lists indexed by [gridIndex] = (z*depth + y)*width + x.
	 * Each cell contains a list of entities currently occupying that grid cell.
	 */
	public List[] entityGrid;

	/**
	 * Master list of all entities in the BlockMap.
	 * Used for global updates and iteration over all entities.
	 */
	public List allEntities = new ArrayList();

	/**
	 * Temporary list for storing query results.
	 * Reused across queries to minimize allocation overhead.
	 */
	private List temporaryQueryList = new ArrayList();


	/**
	 * Constructs a BlockMap with the specified world dimensions.
	 * Creates a spatial grid with cell size of 16x16x16 units.
	 * Automatically calculates optimal grid dimensions based on world size.
	 * Ensures minimum grid size of 1x1x1 cells.
	 *
	 * @param worldWidth the width of the world in units (typically 256)
	 * @param worldDepth the depth of the world in units (typically 256)
	 * @param worldHeight the height of the world in units (typically 64)
	 */
	public BlockMap(int worldWidth, int worldDepth, int worldHeight) {
		this.gridWidth = worldWidth / 16;
		this.gridDepth = worldDepth / 16;
		this.gridHeight = worldHeight / 16;
		if(this.gridWidth == 0) {
			this.gridWidth = 1;
		}

		if(this.gridDepth == 0) {
			this.gridDepth = 1;
		}

		if(this.gridHeight == 0) {
			this.gridHeight = 1;
		}

		this.entityGrid = new ArrayList[this.gridWidth * this.gridDepth * this.gridHeight];

		for(worldWidth = 0; worldWidth < this.gridWidth; ++worldWidth) {
			for(worldDepth = 0; worldDepth < this.gridDepth; ++worldDepth) {
				for(worldHeight = 0; worldHeight < this.gridHeight; ++worldHeight) {
					this.entityGrid[(worldHeight * this.gridDepth + worldDepth) * this.gridWidth + worldWidth] = new ArrayList();
				}
			}
		}

	}

	/**
	 * Inserts an entity into the BlockMap at its current position.
	 * Records the entity's position for future move detection.
	 * Stores reference to this BlockMap in the entity for callbacks.
	 *
	 * @param entityToInsert the entity to add to the BlockMap
	 */
	public void insert(Entity entityToInsert) {
		this.allEntities.add(entityToInsert);
		this.slotMin.init(entityToInsert.x, entityToInsert.y, entityToInsert.z).add(entityToInsert);
		entityToInsert.xOld = entityToInsert.x;
		entityToInsert.yOld = entityToInsert.y;
		entityToInsert.zOld = entityToInsert.z;
		entityToInsert.blockMap = this;
	}

	/**
	 * Removes an entity from the BlockMap.
	 * Uses entity's previous position for efficient grid removal.
	 *
	 * @param entityToRemove the entity to remove from the BlockMap
	 */
	public void remove(Entity entityToRemove) {
		this.slotMin.init(entityToRemove.xOld, entityToRemove.yOld, entityToRemove.zOld).remove(entityToRemove);
		this.allEntities.remove(entityToRemove);
	}

	/**
	 * Updates entity's grid position when it moves between cells.
	 * Checks if entity has moved to a different grid cell and updates accordingly.
	 * Updates stored position for next movement check.
	 *
	 * @param movedEntity the entity that may have moved
	 */
	public void moved(Entity movedEntity) {
		BlockMap$Slot oldSlot = this.slotMin.init(movedEntity.xOld, movedEntity.yOld, movedEntity.zOld);
		BlockMap$Slot newSlot = this.slotMax.init(movedEntity.x, movedEntity.y, movedEntity.z);
		if(!oldSlot.equals(newSlot)) {
			oldSlot.remove(movedEntity);
			newSlot.add(movedEntity);
			movedEntity.xOld = movedEntity.x;
			movedEntity.yOld = movedEntity.y;
			movedEntity.zOld = movedEntity.z;
		}
	}

	/**
	 * Queries entities in a bounding box using temporary list.
	 * Convenience method that allocates temporary list internally.
	 *
	 * @param queryEntity the entity making the query (excluded from results)
	 * @param minX the minimum x coordinate of the query box
	 * @param minY the minimum y coordinate of the query box
	 * @param minZ the minimum z coordinate of the query box
	 * @param maxX the maximum x coordinate of the query box
	 * @param maxY the maximum y coordinate of the query box
	 * @param maxZ the maximum z coordinate of the query box
	 * @return list of entities in the specified box (excluding queryEntity)
	 */
	public List getEntities(Entity queryEntity, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		this.temporaryQueryList.clear();
		return this.getEntities(queryEntity, minX, minY, minZ, maxX, maxY, maxZ, this.temporaryQueryList);
	}

	/**
	 * Queries entities in a bounding box, storing results in provided list.
	 * Checks all grid cells overlapping the bounding box.
	 * Returns only entities that actually intersect the box (not just in grid cell).
	 *
	 * @param queryEntity the entity making the query (excluded from results)
	 * @param minX the minimum x coordinate of the query box
	 * @param minY the minimum y coordinate of the query box
	 * @param minZ the minimum z coordinate of the query box
	 * @param maxX the maximum x coordinate of the query box
	 * @param maxY the maximum y coordinate of the query box
	 * @param maxZ the maximum z coordinate of the query box
	 * @param resultList the list to store found entities in
	 * @return the resultList containing all matching entities
	 */
	public List getEntities(Entity queryEntity, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, List resultList) {
		BlockMap$Slot minSlot = this.slotMin.init(minX, minY, minZ);
		BlockMap$Slot maxSlot = this.slotMax.init(maxX, maxY, maxZ);

		for(int xSlot = BlockMap$Slot.getXSlot(minSlot) - 1; xSlot <= BlockMap$Slot.getXSlot(maxSlot) + 1; ++xSlot) {
			for(int ySlot = BlockMap$Slot.getYSlot(minSlot) - 1; ySlot <= BlockMap$Slot.getYSlot(maxSlot) + 1; ++ySlot) {
				for(int zSlot = BlockMap$Slot.getZSlot(minSlot) - 1; zSlot <= BlockMap$Slot.getZSlot(maxSlot) + 1; ++zSlot) {
					if(xSlot >= 0 && ySlot >= 0 && zSlot >= 0 && xSlot < this.gridWidth && ySlot < this.gridDepth && zSlot < this.gridHeight) {
						List cellEntities = this.entityGrid[(zSlot * this.gridDepth + ySlot) * this.gridWidth + xSlot];

						for(int entityIndex = 0; entityIndex < cellEntities.size(); ++entityIndex) {
							Entity adjacentEntity;
							if((adjacentEntity = (Entity)cellEntities.get(entityIndex)) != queryEntity && adjacentEntity.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
								resultList.add(adjacentEntity);
							}
						}
					}
				}
			}
		}

		return resultList;
	}

	/**
	 * Removes all entities that are not allowed in creative mode.
	 * Iterates through all grid cells and removes non-creative-allowed entities.
	 * Used when transitioning game modes or clearing temporary entities.
	 */
	public void removeAllNonCreativeModeEntities() {
		for(int xSlot = 0; xSlot < this.gridWidth; ++xSlot) {
			for(int ySlot = 0; ySlot < this.gridDepth; ++ySlot) {
				for(int zSlot = 0; zSlot < this.gridHeight; ++zSlot) {
					List cellEntities = this.entityGrid[(zSlot * this.gridDepth + ySlot) * this.gridWidth + xSlot];

					for(int entityIndex = 0; entityIndex < cellEntities.size(); ++entityIndex) {
						if(!((Entity)cellEntities.get(entityIndex)).isCreativeModeAllowed()) {
							cellEntities.remove(entityIndex--);
						}
					}
				}
			}
		}

	}

	/**
	 * Clears all entities from all grid cells.
	 * Does not clear the master entity list - use separately if needed.
	 * Used for world resets and cleanup operations.
	 */
	public void clear() {
		for(int xSlot = 0; xSlot < this.gridWidth; ++xSlot) {
			for(int ySlot = 0; ySlot < this.gridDepth; ++ySlot) {
				for(int zSlot = 0; zSlot < this.gridHeight; ++zSlot) {
					this.entityGrid[(zSlot * this.gridDepth + ySlot) * this.gridWidth + xSlot].clear();
				}
			}
		}

	}

	/**
	 * Queries entities within an AABB using temporary list.
	 * Convenience method that converts AABB to coordinates.
	 *
	 * @param queryEntity the entity making the query (excluded from results)
	 * @param queryBox the bounding box to query
	 * @return list of entities in the bounding box
	 */
	public List getEntities(Entity queryEntity, AABB queryBox) {
		this.temporaryQueryList.clear();
		return this.getEntities(queryEntity, queryBox.x0, queryBox.y0, queryBox.z0, queryBox.x1, queryBox.y1, queryBox.z1, this.temporaryQueryList);
	}

	/**
	 * Queries entities within an AABB, storing results in provided list.
	 * Delegates to coordinate-based query after extracting AABB bounds.
	 *
	 * @param queryEntity the entity making the query (excluded from results)
	 * @param queryBox the bounding box to query
	 * @param resultList the list to store found entities in
	 * @return the resultList containing all matching entities
	 */
	public List getEntities(Entity queryEntity, AABB queryBox, List resultList) {
		return this.getEntities(queryEntity, queryBox.x0, queryBox.y0, queryBox.z0, queryBox.x1, queryBox.y1, queryBox.z1, resultList);
	}

	/**
	 * Updates all entities in the BlockMap.
	 * Ticks each entity and handles entity removal.
	 * Detects entity movement between grid cells and updates grid accordingly.
	 */
	public void tickAll() {
		for(int entityIndex = 0; entityIndex < this.allEntities.size(); ++entityIndex) {
			Entity currentEntity;
			(currentEntity = (Entity)this.allEntities.get(entityIndex)).tick();
			if(currentEntity.removed) {
				this.allEntities.remove(entityIndex--);
				this.slotMin.init(currentEntity.xOld, currentEntity.yOld, currentEntity.zOld).remove(currentEntity);
			} else {
				int oldGridX = (int)(currentEntity.xOld / 16.0F);
				int oldGridY = (int)(currentEntity.yOld / 16.0F);
				int oldGridZ = (int)(currentEntity.zOld / 16.0F);
				int newGridX = (int)(currentEntity.x / 16.0F);
				int newGridY = (int)(currentEntity.y / 16.0F);
				int newGridZ = (int)(currentEntity.z / 16.0F);
				if(oldGridX != newGridX || oldGridY != newGridY || oldGridZ != newGridZ) {
					this.moved(currentEntity);
				}
			}
		}

	}

	/**
	 * Renders all visible entities in the world.
	 * Uses frustum culling to skip entities not in view.
	 * Performs fine-grained culling checks to skip individual entities outside frustum.
	 *
	 * @param cameraPosition the camera's world position
	 * @param viewFrustum the camera's viewing frustum for culling
	 * @param textureManager the texture manager for rendering
	 * @param partialTickTime the partial tick time for interpolation
	 */
	public void render(Vec3D cameraPosition, Frustrum viewFrustum, TextureManager textureManager, float partialTickTime) {
		for(int gridXIndex = 0; gridXIndex < this.gridWidth; ++gridXIndex) {
			float cornerX0 = (float)((gridXIndex << 4) - 2);
			float cornerX1 = (float)((gridXIndex + 1 << 4) + 2);

			for(int gridYIndex = 0; gridYIndex < this.gridDepth; ++gridYIndex) {
				float cornerY0 = (float)((gridYIndex << 4) - 2);
				float cornerY1 = (float)((gridYIndex + 1 << 4) + 2);

				for(int gridZIndex = 0; gridZIndex < this.gridHeight; ++gridZIndex) {
					List cellEntities;
					if((cellEntities = this.entityGrid[(gridZIndex * this.gridDepth + gridYIndex) * this.gridWidth + gridXIndex]).size() != 0) {
						float cornerZ0 = (float)((gridZIndex << 4) - 2);
						float cornerZ1 = (float)((gridZIndex + 1 << 4) + 2);
						if(viewFrustum.isBoxInFrustrum(cornerX0, cornerY0, cornerZ0, cornerX1, cornerY1, cornerZ1)) {
							// Test all 8 corners of cell against frustum planes for exact culling
							float cornerX0Y0Z0 = cornerZ0;
							float cornerX1Y0Z0 = cornerY0;
							float cornerX0Y1Z0 = cornerX1;
							float cornerX1Y1Z0 = cornerX0;
							Frustrum frustumPlanes = viewFrustum;
							int frustumPlaneIndex = 0;

							boolean isInFrustum;
							while(true) {
								if(frustumPlaneIndex >= 6) {
									isInFrustum = true;
									break;
								}

								if(frustumPlanes.frustrum[frustumPlaneIndex][0] * cornerX1Y1Z0 + frustumPlanes.frustrum[frustumPlaneIndex][1] * cornerX1Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][2] * cornerX0Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][3] <= 0.0F) {
									isInFrustum = false;
									break;
								}

								if(frustumPlanes.frustrum[frustumPlaneIndex][0] * cornerX0Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][1] * cornerX1Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][2] * cornerX0Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][3] <= 0.0F) {
									isInFrustum = false;
									break;
								}

								if(frustumPlanes.frustrum[frustumPlaneIndex][0] * cornerX1Y1Z0 + frustumPlanes.frustrum[frustumPlaneIndex][1] * cornerY1 + frustumPlanes.frustrum[frustumPlaneIndex][2] * cornerX0Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][3] <= 0.0F) {
									isInFrustum = false;
									break;
								}

								if(frustumPlanes.frustrum[frustumPlaneIndex][0] * cornerX0Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][1] * cornerY1 + frustumPlanes.frustrum[frustumPlaneIndex][2] * cornerX0Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][3] <= 0.0F) {
									isInFrustum = false;
									break;
								}

								if(frustumPlanes.frustrum[frustumPlaneIndex][0] * cornerX1Y1Z0 + frustumPlanes.frustrum[frustumPlaneIndex][1] * cornerX1Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][2] * cornerZ1 + frustumPlanes.frustrum[frustumPlaneIndex][3] <= 0.0F) {
									isInFrustum = false;
									break;
								}

								if(frustumPlanes.frustrum[frustumPlaneIndex][0] * cornerX0Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][1] * cornerX1Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][2] * cornerZ1 + frustumPlanes.frustrum[frustumPlaneIndex][3] <= 0.0F) {
									isInFrustum = false;
									break;
								}

								if(frustumPlanes.frustrum[frustumPlaneIndex][0] * cornerX1Y1Z0 + frustumPlanes.frustrum[frustumPlaneIndex][1] * cornerY1 + frustumPlanes.frustrum[frustumPlaneIndex][2] * cornerZ1 + frustumPlanes.frustrum[frustumPlaneIndex][3] <= 0.0F) {
									isInFrustum = false;
									break;
								}

								if(frustumPlanes.frustrum[frustumPlaneIndex][0] * cornerX0Y0Z0 + frustumPlanes.frustrum[frustumPlaneIndex][1] * cornerY1 + frustumPlanes.frustrum[frustumPlaneIndex][2] * cornerZ1 + frustumPlanes.frustrum[frustumPlaneIndex][3] <= 0.0F) {
									isInFrustum = false;
									break;
								}

								++frustumPlaneIndex;
							}

							boolean cellIsFullyInFrustum = isInFrustum;

							for(int entityIndexInCell = 0; entityIndexInCell < cellEntities.size(); ++entityIndexInCell) {
								Entity currentEntity;
								if((currentEntity = (Entity)cellEntities.get(entityIndexInCell)).shouldRender(cameraPosition)) {
									if(!cellIsFullyInFrustum) {
										AABB entityAABB = currentEntity.bb;
										if(!viewFrustum.isBoxInFrustrum(entityAABB.x0, entityAABB.y0, entityAABB.z0, entityAABB.x1, entityAABB.y1, entityAABB.z1)) {
											continue;
										}
									}

									currentEntity.render(textureManager, partialTickTime);
								}
							}
						}
					}
				}
			}
		}

	}

	// $FF: synthetic method
	static int getWidth(BlockMap blockMap) {
		return blockMap.gridWidth;
	}

	// $FF: synthetic method
	static int getDepth(BlockMap blockMap) {
		return blockMap.gridDepth;
	}

	// $FF: synthetic method
	static int getHeight(BlockMap blockMap) {
		return blockMap.gridHeight;
	}
}


