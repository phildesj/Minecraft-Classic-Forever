package com.mojang.minecraft.render;

/**
 * Frustrum implements view frustum culling for efficient rendering of the 3D world.
 * The frustum is a 3D geometric volume that represents the camera's field of view.
 * This class checks whether bounding boxes (chunks, entities) are within the camera's view
 * to determine if they need to be rendered, improving performance by skipping off-screen geometry.
 */
public class Frustrum {

	/** 2D array storing the 6 clipping plane equations (left, right, top, bottom, near, far).
	 * Each plane is defined by coefficients [A, B, C, D] for equation: Ax + By + Cz + D = 0 */
	public float[][] frustrum = new float[6][4];

	/** 4x4 projection matrix extracted from OpenGL for camera frustum calculation. */
	public float[] projection = new float[16];

	/** 4x4 model-view matrix extracted from OpenGL for camera frustum calculation. */
	public float[] modelview = new float[16];

	/** Combined clipping plane coefficients used in frustum calculations. */
	public float[] clipping = new float[16];

	/**
	 * Tests whether an axis-aligned bounding box is visible within the camera frustum.
	 * Performs 6-plane frustum culling by testing all 8 corners of the bounding box
	 * against each clipping plane. Returns false if the entire box is outside any plane.
	 *
	 * @param minX the minimum X coordinate of the bounding box
	 * @param minY the minimum Y coordinate of the bounding box
	 * @param minZ the minimum Z coordinate of the bounding box
	 * @param maxX the maximum X coordinate of the bounding box
	 * @param maxY the maximum Y coordinate of the bounding box
	 * @param maxZ the maximum Z coordinate of the bounding box
	 * @return true if the bounding box intersects the frustum, false if completely outside
	 */
	public final boolean isBoxInFrustrum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		// Test all 6 clipping planes for frustum visibility
		for(int planeIndex = 0; planeIndex < 6; ++planeIndex) {
			// Extract plane coefficients for the distance equation: Ax + By + Cz + D
			float planeA = this.frustrum[planeIndex][0];
			float planeB = this.frustrum[planeIndex][1];
			float planeC = this.frustrum[planeIndex][2];
			float planeD = this.frustrum[planeIndex][3];

			// Test all 8 corners of the bounding box against this plane
			// If all corners are on the negative side of the plane, the box is completely outside
			if(planeA * minX + planeB * minY + planeC * minZ + planeD <= 0.0F &&
			   planeA * maxX + planeB * minY + planeC * minZ + planeD <= 0.0F &&
			   planeA * minX + planeB * maxY + planeC * minZ + planeD <= 0.0F &&
			   planeA * maxX + planeB * maxY + planeC * minZ + planeD <= 0.0F &&
			   planeA * minX + planeB * minY + planeC * maxZ + planeD <= 0.0F &&
			   planeA * maxX + planeB * minY + planeC * maxZ + planeD <= 0.0F &&
			   planeA * minX + planeB * maxY + planeC * maxZ + planeD <= 0.0F &&
			   planeA * maxX + planeB * maxY + planeC * maxZ + planeD <= 0.0F) {
				// All 8 corners are outside this plane, so the box is completely culled
				return false;
			}
		}

		// Box intersects at least one plane and is not completely outside the frustum
		return true;
	}
}
