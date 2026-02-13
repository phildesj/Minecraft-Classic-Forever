package com.mojang.minecraft.phys;

import com.mojang.minecraft.MovingObjectPosition;
import com.mojang.minecraft.model.Vec3D;
import java.io.Serializable;

/**
 * AABB (Axis-Aligned Bounding Box) represents a 3D rectangular collision volume for physics calculations.
 * Used for entity collision detection, ray casting, and bounding box intersection tests.
 * All coordinates are in world space and represent the minimum (x0, y0, z0) and maximum (x1, y1, z1) corners
 * of the bounding box. Supports collision clipping, intersection testing, and geometric operations.
 */
public class AABB implements Serializable {

	/** Serial version ID for serialization compatibility. */
	public static final long serialVersionUID = 0L;

	/** Small epsilon value for collision margin to prevent clipping issues. */
	private float epsilon = 0.0F;

	/** Minimum X coordinate of the bounding box. */
	public float x0;

	/** Minimum Y coordinate of the bounding box. */
	public float y0;

	/** Minimum Z coordinate of the bounding box. */
	public float z0;

	/** Maximum X coordinate of the bounding box. */
	public float x1;

	/** Maximum Y coordinate of the bounding box. */
	public float y1;

	/** Maximum Z coordinate of the bounding box. */
	public float z1;

	/**
	 * Constructs a new AABB with the specified bounding box coordinates.
	 *
	 * @param minX the minimum X coordinate of the box
	 * @param minY the minimum Y coordinate of the box
	 * @param minZ the minimum Z coordinate of the box
	 * @param maxX the maximum X coordinate of the box
	 * @param maxY the maximum Y coordinate of the box
	 * @param maxZ the maximum Z coordinate of the box
	 */
	public AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		// Store the minimum corner coordinates
		this.x0 = minX;
		this.y0 = minY;
		this.z0 = minZ;

		// Store the maximum corner coordinates
		this.x1 = maxX;
		this.y1 = maxY;
		this.z1 = maxZ;
	}

	/**
	 * Expands the bounding box in the specified directions.
	 * Positive values expand the maximum corner, negative values expand the minimum corner.
	 * Creates a new AABB without modifying the original.
	 *
	 * @param expandX the amount to expand in the X direction
	 * @param expandY the amount to expand in the Y direction
	 * @param expandZ the amount to expand in the Z direction
	 * @return a new AABB with expanded bounds
	 */
	public AABB expand(float expandX, float expandY, float expandZ) {
		// Initialize new bounds with current box corners
		float newMinX = this.x0;
		float newMinY = this.y0;
		float newMinZ = this.z0;
		float newMaxX = this.x1;
		float newMaxY = this.y1;
		float newMaxZ = this.z1;

		// Expand negative X direction if expandX is negative
		if(expandX < 0.0F) {
			newMinX += expandX;
		}

		// Expand positive X direction if expandX is positive
		if(expandX > 0.0F) {
			newMaxX += expandX;
		}

		// Expand negative Y direction if expandY is negative
		if(expandY < 0.0F) {
			newMinY += expandY;
		}

		// Expand positive Y direction if expandY is positive
		if(expandY > 0.0F) {
			newMaxY += expandY;
		}

		// Expand negative Z direction if expandZ is negative
		if(expandZ < 0.0F) {
			newMinZ += expandZ;
		}

		// Expand positive Z direction if expandZ is positive
		if(expandZ > 0.0F) {
			newMaxZ += expandZ;
		}

		// Return a new AABB with the expanded bounds
		return new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
	}

	/**
	 * Grows the bounding box symmetrically by the specified amount in each direction.
	 * Expands both the minimum and maximum corners away from the center by the given amounts.
	 * Creates a new AABB without modifying the original.
	 *
	 * @param growX the amount to grow in the X direction (both min and max)
	 * @param growY the amount to grow in the Y direction (both min and max)
	 * @param growZ the amount to grow in the Z direction (both min and max)
	 * @return a new AABB with symmetrically grown bounds
	 */
	public AABB grow(float growX, float growY, float growZ) {
		// Decrease minimum corner by the grow amount
		float newMinX = this.x0 - growX;
		float newMinY = this.y0 - growY;
		float newMinZ = this.z0 - growZ;

		// Increase maximum corner by the grow amount
		growX += this.x1;
		growY += this.y1;
		float newMaxZ = this.z1 + growZ;

		// Return a new AABB with the grown bounds
		return new AABB(newMinX, newMinY, newMinZ, growX, growY, newMaxZ);
	}

	/**
	 * Creates a copy of this bounding box translated by the specified offset.
	 * Does not modify the original bounding box.
	 *
	 * @param offsetX the X offset to apply to the cloned box
	 * @param offsetY the Y offset to apply to the cloned box
	 * @param offsetZ the Z offset to apply to the cloned box
	 * @return a new AABB with the translated coordinates
	 */
	public AABB cloneMove(float offsetX, float offsetY, float offsetZ) {
		// Create a new AABB with all coordinates offset by the specified amounts
		return new AABB(this.x0 + offsetZ, this.y0 + offsetY, this.z0 + offsetZ, this.x1 + offsetX, this.y1 + offsetY, this.z1 + offsetZ);
	}

	/**
	 * Calculates the maximum movement distance along the X-axis without collision.
	 * Tests if the given bounding box collides with this box along the X-axis.
	 * Returns the clipped distance that prevents intersection.
	 *
	 * @param collisionBox the bounding box to test collision against
	 * @param moveDistance the desired movement distance along the X-axis
	 * @return the maximum safe movement distance (clipped if collision detected)
	 */
	public float clipXCollide(AABB collisionBox, float moveDistance) {
		// Check if boxes overlap in Y direction
		if(collisionBox.y1 > this.y0 && collisionBox.y0 < this.y1) {
			// Check if boxes overlap in Z direction (required for collision in X-axis)
			if(collisionBox.z1 > this.z0 && collisionBox.z0 < this.z1) {
				float clippedDistance;

				// Test positive X movement (moving right)
				// Check if other box is to the left and would collide
				if(moveDistance > 0.0F && collisionBox.x1 <= this.x0 && (clippedDistance = this.x0 - collisionBox.x1 - this.epsilon) < moveDistance) {
					moveDistance = clippedDistance;
				}

				// Test negative X movement (moving left)
				// Check if other box is to the right and would collide
				if(moveDistance < 0.0F && collisionBox.x0 >= this.x1 && (clippedDistance = this.x1 - collisionBox.x0 + this.epsilon) > moveDistance) {
					moveDistance = clippedDistance;
				}

				return moveDistance;
			} else {
				return moveDistance;
			}
		} else {
			return moveDistance;
		}
	}

	/**
	 * Calculates the maximum movement distance along the Y-axis without collision.
	 * Tests if the given bounding box collides with this box along the Y-axis.
	 * Returns the clipped distance that prevents intersection.
	 *
	 * @param collisionBox the bounding box to test collision against
	 * @param moveDistance the desired movement distance along the Y-axis
	 * @return the maximum safe movement distance (clipped if collision detected)
	 */
	public float clipYCollide(AABB collisionBox, float moveDistance) {
		// Check if boxes overlap in X direction
		if(collisionBox.x1 > this.x0 && collisionBox.x0 < this.x1) {
			// Check if boxes overlap in Z direction (required for collision in Y-axis)
			if(collisionBox.z1 > this.z0 && collisionBox.z0 < this.z1) {
				float clippedDistance;

				// Test positive Y movement (moving up)
				// Check if other box is below and would collide
				if(moveDistance > 0.0F && collisionBox.y1 <= this.y0 && (clippedDistance = this.y0 - collisionBox.y1 - this.epsilon) < moveDistance) {
					moveDistance = clippedDistance;
				}

				// Test negative Y movement (moving down)
				// Check if other box is above and would collide
				if(moveDistance < 0.0F && collisionBox.y0 >= this.y1 && (clippedDistance = this.y1 - collisionBox.y0 + this.epsilon) > moveDistance) {
					moveDistance = clippedDistance;
				}

				return moveDistance;
			} else {
				return moveDistance;
			}
		} else {
			return moveDistance;
		}
	}

	/**
	 * Calculates the maximum movement distance along the Z-axis without collision.
	 * Tests if the given bounding box collides with this box along the Z-axis.
	 * Returns the clipped distance that prevents intersection.
	 *
	 * @param collisionBox the bounding box to test collision against
	 * @param moveDistance the desired movement distance along the Z-axis
	 * @return the maximum safe movement distance (clipped if collision detected)
	 */
	public float clipZCollide(AABB collisionBox, float moveDistance) {
		// Check if boxes overlap in X direction
		if(collisionBox.x1 > this.x0 && collisionBox.x0 < this.x1) {
			// Check if boxes overlap in Y direction (required for collision in Z-axis)
			if(collisionBox.y1 > this.y0 && collisionBox.y0 < this.y1) {
				float clippedDistance;

				// Test positive Z movement (moving forward)
				// Check if other box is in front and would collide
				if(moveDistance > 0.0F && collisionBox.z1 <= this.z0 && (clippedDistance = this.z0 - collisionBox.z1 - this.epsilon) < moveDistance) {
					moveDistance = clippedDistance;
				}

				// Test negative Z movement (moving backward)
				// Check if other box is behind and would collide
				if(moveDistance < 0.0F && collisionBox.z0 >= this.z1 && (clippedDistance = this.z1 - collisionBox.z0 + this.epsilon) > moveDistance) {
					moveDistance = clippedDistance;
				}

				return moveDistance;
			} else {
				return moveDistance;
			}
		} else {
			return moveDistance;
		}
	}

	/**
	 * Tests if this AABB strictly overlaps with another AABB.
	 * Uses strict inequality (> and <) for test boundaries.
	 *
	 * @param other the other AABB to test overlap against
	 * @return true if the boxes overlap in all three dimensions
	 */
	public boolean intersects(AABB other) {
		return other.x1 > this.x0 && other.x0 < this.x1 ? (other.y1 > this.y0 && other.y0 < this.y1 ? other.z1 > this.z0 && other.z0 < this.z1 : false) : false;
	}

	/**
	 * Tests if this AABB overlaps with another AABB using inclusive boundaries.
	 * Considers touching boxes as intersecting (uses >= and <=).
	 *
	 * @param other the other AABB to test overlap against
	 * @return true if the boxes overlap or touch in all three dimensions
	 */
	public boolean intersectsInner(AABB other) {
		return other.x1 >= this.x0 && other.x0 <= this.x1 ? (other.y1 >= this.y0 && other.y0 <= this.y1 ? other.z1 >= this.z0 && other.z0 <= this.z1 : false) : false;
	}

	/**
	 * Translates the bounding box by the specified offset in all dimensions.
	 * Modifies the box in place (does not create a new AABB).
	 *
	 * @param offsetX the X offset to apply
	 * @param offsetY the Y offset to apply
	 * @param offsetZ the Z offset to apply
	 */
	public void move(float offsetX, float offsetY, float offsetZ) {
		// Apply offset to minimum corner
		this.x0 += offsetX;
		this.y0 += offsetY;
		this.z0 += offsetZ;

		// Apply offset to maximum corner
		this.x1 += offsetX;
		this.y1 += offsetY;
		this.z1 += offsetZ;
	}

	/**
	 * Tests if this AABB overlaps with a bounding box defined by coordinates.
	 * Uses strict inequality for boundaries.
	 *
	 * @param minX the minimum X coordinate
	 * @param minY the minimum Y coordinate
	 * @param minZ the minimum Z coordinate
	 * @param maxX the maximum X coordinate
	 * @param maxY the maximum Y coordinate
	 * @param maxZ the maximum Z coordinate
	 * @return true if the boxes overlap in all three dimensions
	 */
	public boolean intersects(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return maxX > this.x0 && minX < this.x1 ? (maxY > this.y0 && minY < this.y1 ? maxZ > this.z0 && minZ < this.z1 : false) : false;
	}

	/**
	 * Tests if a point is strictly contained within this bounding box.
	 * Uses strict inequality (> and <) for boundaries.
	 *
	 * @param point the point to test for containment
	 * @return true if the point is inside the bounding box
	 */
	public boolean contains(Vec3D point) {
		return point.x > this.x0 && point.x < this.x1 ? (point.y > this.y0 && point.y < this.y1 ? point.z > this.z0 && point.z < this.z1 : false) : false;
	}

	/**
	 * Calculates the average dimension size of this bounding box.
	 * Returns the sum of width, height, and depth divided by 3.
	 *
	 * @return the average dimension size
	 */
	public float getSize() {
		// Calculate width (X dimension)
		float width = this.x1 - this.x0;

		// Calculate height (Y dimension)
		float height = this.y1 - this.y0;

		// Calculate depth (Z dimension)
		float depth = this.z1 - this.z0;

		// Return average dimension
		return (width + height + depth) / 3.0F;
	}

	/**
	 * Shrinks the bounding box by reducing its dimensions.
	 * Negative values reduce the minimum corner (shrink inward), positive values reduce the maximum corner (shrink outward).
	 * Creates a new AABB without modifying the original.
	 *
	 * @param shrinkX the amount to shrink in the X direction
	 * @param shrinkY the amount to shrink in the Y direction
	 * @param shrinkZ the amount to shrink in the Z direction
	 * @return a new AABB with shrunk bounds
	 */
	public AABB shrink(float shrinkX, float shrinkY, float shrinkZ) {
		// Initialize new bounds with current box corners
		float newMinX = this.x0;
		float newMinY = this.y0;
		float newMinZ = this.z0;
		float newMaxX = this.x1;
		float newMaxY = this.y1;
		float newMaxZ = this.z1;

		// Shrink minimum X if shrinkX is negative
		if(shrinkX < 0.0F) {
			newMinX -= shrinkX;
		}

		// Shrink maximum X if shrinkX is positive
		if(shrinkX > 0.0F) {
			newMaxX -= shrinkX;
		}

		// Shrink minimum Y if shrinkY is negative
		if(shrinkY < 0.0F) {
			newMinY -= shrinkY;
		}

		// Shrink maximum Y if shrinkY is positive
		if(shrinkY > 0.0F) {
			newMaxY -= shrinkY;
		}

		// Shrink minimum Z if shrinkZ is negative
		if(shrinkZ < 0.0F) {
			newMinZ -= shrinkZ;
		}

		// Shrink maximum Z if shrinkZ is positive
		if(shrinkZ > 0.0F) {
			newMaxZ -= shrinkZ;
		}

		// Return a new AABB with the shrunk bounds
		return new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
	}

	/**
	 * Creates a duplicate of this bounding box with the same coordinates.
	 * Modifications to the copy do not affect the original.
	 *
	 * @return a new AABB with identical bounds
	 */
	public AABB copy() {
		// Create a new AABB with the same coordinates as this box
		return new AABB(this.x0, this.y0, this.z0, this.x1, this.y1, this.z1);
	}

	/**
	 * Performs ray casting against this AABB to find intersection point.
	 * Tests the ray defined by start and end points against all 6 faces of the box.
	 * Returns the closest intersection point and the face that was hit.
	 *
	 * @param rayStart the starting point of the ray
	 * @param rayEnd the ending point of the ray
	 * @return MovingObjectPosition containing intersection point and face, or null if no hit
	 */
	public MovingObjectPosition clip(Vec3D rayStart, Vec3D rayEnd) {
		// Calculate ray intersection with each face of the box
		// Test intersection with minimum X face (x = x0)
		Vec3D intersectMinX = rayStart.getXIntersection(rayEnd, this.x0);

		// Test intersection with maximum X face (x = x1)
		Vec3D intersectMaxX = rayStart.getXIntersection(rayEnd, this.x1);

		// Test intersection with minimum Y face (y = y0)
		Vec3D intersectMinY = rayStart.getYIntersection(rayEnd, this.y0);

		// Test intersection with maximum Y face (y = y1)
		Vec3D intersectMaxY = rayStart.getYIntersection(rayEnd, this.y1);

		// Test intersection with minimum Z face (z = z0)
		Vec3D intersectMinZ = rayStart.getZIntersection(rayEnd, this.z0);

		// Test intersection with maximum Z face (z = z1)
		rayEnd = rayStart.getZIntersection(rayEnd, this.z1);

		// Validate that intersection points actually lie on the box surface
		// Discard intersections that are outside the box bounds
		if(!this.xIntersects(intersectMinX)) {
			intersectMinX = null;
		}

		if(!this.xIntersects(intersectMaxX)) {
			intersectMaxX = null;
		}

		if(!this.yIntersects(intersectMinY)) {
			intersectMinY = null;
		}

		if(!this.yIntersects(intersectMaxY)) {
			intersectMaxY = null;
		}

		if(!this.zIntersects(intersectMinZ)) {
			intersectMinZ = null;
		}

		if(!this.zIntersects(rayEnd)) {
			rayEnd = null;
		}

		// Find the closest valid intersection point
		Vec3D closestIntersection = null;

		// Check minimum X face intersection
		if(intersectMinX != null) {
			closestIntersection = intersectMinX;
		}

		// Check maximum X face intersection
		if(intersectMaxX != null && (closestIntersection == null || rayStart.distanceSquared(intersectMaxX) < rayStart.distanceSquared(closestIntersection))) {
			closestIntersection = intersectMaxX;
		}

		// Check minimum Y face intersection
		if(intersectMinY != null && (closestIntersection == null || rayStart.distanceSquared(intersectMinY) < rayStart.distanceSquared(closestIntersection))) {
			closestIntersection = intersectMinY;
		}

		// Check maximum Y face intersection
		if(intersectMaxY != null && (closestIntersection == null || rayStart.distanceSquared(intersectMaxY) < rayStart.distanceSquared(closestIntersection))) {
			closestIntersection = intersectMaxY;
		}

		// Check minimum Z face intersection
		if(intersectMinZ != null && (closestIntersection == null || rayStart.distanceSquared(intersectMinZ) < rayStart.distanceSquared(closestIntersection))) {
			closestIntersection = intersectMinZ;
		}

		// Check maximum Z face intersection
		if(rayEnd != null && (closestIntersection == null || rayStart.distanceSquared(rayEnd) < rayStart.distanceSquared(closestIntersection))) {
			closestIntersection = rayEnd;
		}

		// Return null if no valid intersection found
		if(closestIntersection == null) {
			return null;
		} else {
			// Determine which face was hit based on the intersection point
			byte faceHit = -1;

			// Check if intersection is on minimum X face (x = x0)
			if(closestIntersection == intersectMinX) {
				faceHit = 4;
			}

			// Check if intersection is on maximum X face (x = x1)
			if(closestIntersection == intersectMaxX) {
				faceHit = 5;
			}

			// Check if intersection is on minimum Y face (y = y0)
			if(closestIntersection == intersectMinY) {
				faceHit = 0;
			}

			// Check if intersection is on maximum Y face (y = y1)
			if(closestIntersection == intersectMaxY) {
				faceHit = 1;
			}

			// Check if intersection is on minimum Z face (z = z0)
			if(closestIntersection == intersectMinZ) {
				faceHit = 2;
			}

			// Check if intersection is on maximum Z face (z = z1)
			if(closestIntersection == rayEnd) {
				faceHit = 3;
			}

			// Return moving object position with intersection point and face information
			return new MovingObjectPosition(0, 0, 0, faceHit, closestIntersection);
		}
	}

	/**
	 * Tests if a point lies on the minimum or maximum X face of this box.
	 * Validates that Y and Z coordinates are within box bounds.
	 *
	 * @param point the point to test
	 * @return true if the point is on an X face of the box
	 */
	private boolean xIntersects(Vec3D point) {
		return point == null ? false : point.y >= this.y0 && point.y <= this.y1 && point.z >= this.z0 && point.z <= this.z1;
	}

	/**
	 * Tests if a point lies on the minimum or maximum Y face of this box.
	 * Validates that X and Z coordinates are within box bounds.
	 *
	 * @param point the point to test
	 * @return true if the point is on a Y face of the box
	 */
	private boolean yIntersects(Vec3D point) {
		return point == null ? false : point.x >= this.x0 && point.x <= this.x1 && point.z >= this.z0 && point.z <= this.z1;
	}

	/**
	 * Tests if a point lies on the minimum or maximum Z face of this box.
	 * Validates that X and Y coordinates are within box bounds.
	 *
	 * @param point the point to test
	 * @return true if the point is on a Z face of the box
	 */
	private boolean zIntersects(Vec3D point) {
		return point == null ? false : point.x >= this.x0 && point.x <= this.x1 && point.y >= this.y0 && point.y <= this.y1;
	}
}
