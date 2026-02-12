package com.mojang.minecraft;

import com.mojang.minecraft.model.Vec3D;

/**
 * MovingObjectPosition represents a collision position when a ray or entity movement
 * intersects with either a block or an entity in the game world.
 *
 * This immutable data class stores information about the intersection point, either
 * as block coordinates with face information or as an entity reference.
 * The entityPos field distinguishes between block collisions and entity collisions.
 */
public final class MovingObjectPosition {

	// Type identifier for block collision
	private static final int TYPE_BLOCK = 0;

	// Type identifier for entity collision
	private static final int TYPE_ENTITY = 1;

	// Collision type: 0 for block, 1 for entity
	private final int entityPos;

	// Block coordinates for block collisions
	private final int x;
	private final int y;
	private final int z;

	// Block face direction for block collisions (0-5)
	private final int face;

	// Precise collision position vector for block collisions
	private final Vec3D vec;

	// Entity reference for entity collisions
	private final Entity entity;

	/**
	 * Constructs a MovingObjectPosition for a block collision.
	 * Stores the block coordinates, collision face, and precise collision vector.
	 *
	 * @param x the block X coordinate
	 * @param y the block Y coordinate
	 * @param z the block Z coordinate
	 * @param face the block face direction (0-5)
	 * @param blockPos the precise collision position as a Vec3D
	 */
	public MovingObjectPosition(int x, int y, int z, int face, Vec3D blockPos) {
		this.entityPos = TYPE_BLOCK;
		this.x = x;
		this.y = y;
		this.z = z;
		this.face = face;
		this.vec = new Vec3D(blockPos.x, blockPos.y, blockPos.z);
		this.entity = null;
	}

	/**
	 * Constructs a MovingObjectPosition for an entity collision.
	 * Stores a reference to the collided entity.
	 *
	 * @param entity the entity that was hit
	 */
	public MovingObjectPosition(Entity entity) {
		this.entityPos = TYPE_ENTITY;
		this.entity = entity;
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.face = 0;
		this.vec = null;
	}

	/**
	 * Gets the collision type.
	 *
	 * @return 0 for block collision, 1 for entity collision
	 */
	public int getEntityPos() {
		return entityPos;
	}

	/**
	 * Gets the block X coordinate (only valid for block collisions).
	 *
	 * @return the X coordinate
	 */
	public int getX() {
		return x;
	}

	/**
	 * Gets the block Y coordinate (only valid for block collisions).
	 *
	 * @return the Y coordinate
	 */
	public int getY() {
		return y;
	}

	/**
	 * Gets the block Z coordinate (only valid for block collisions).
	 *
	 * @return the Z coordinate
	 */
	public int getZ() {
		return z;
	}

	/**
	 * Gets the block face direction (only valid for block collisions).
	 *
	 * @return the face direction (0-5)
	 */
	public int getFace() {
		return face;
	}

	/**
	 * Gets the precise collision position vector (only valid for block collisions).
	 *
	 * @return the collision position as a Vec3D
	 */
	public Vec3D getVec() {
		return vec;
	}

	/**
	 * Gets the collided entity (only valid for entity collisions).
	 *
	 * @return the entity that was hit, or null for block collisions
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Checks if this collision is with a block.
	 *
	 * @return true if this is a block collision, false if entity collision
	 */
	public boolean isBlockCollision() {
		return entityPos == TYPE_BLOCK;
	}

	/**
	 * Checks if this collision is with an entity.
	 *
	 * @return true if this is an entity collision, false if block collision
	 */
	public boolean isEntityCollision() {
		return entityPos == TYPE_ENTITY;
	}
}
