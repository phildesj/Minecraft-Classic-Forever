package com.mojang.minecraft.render;

import com.mojang.minecraft.render.Frustrum;
import com.mojang.util.MathHelper;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * FrustrumImpl is the concrete implementation of view frustum culling for the Minecraft Classic Forever client.
 * It extracts the OpenGL projection and model-view matrices from the graphics card and computes the 6 clipping planes
 * that define the camera's field of view. Uses matrix multiplication to combine the projection and model-view matrices,
 * then extracts and normalizes plane equations for efficient frustum culling tests.
 * This class uses a singleton pattern to avoid repeated matrix extraction overhead.
 */
public final class FrustrumImpl extends Frustrum {

	/** Singleton instance of FrustrumImpl for reuse across frames. */
	private static FrustrumImpl instance = new FrustrumImpl();

	/** Float buffer for reading OpenGL projection matrix (4x4 = 16 elements). */
	private FloatBuffer projectionBuff = BufferUtils.createFloatBuffer(16);

	/** Float buffer for reading OpenGL model-view matrix (4x4 = 16 elements). */
	private FloatBuffer modelviewBuff = BufferUtils.createFloatBuffer(16);

	/** Temporary float buffer for unused matrix data (reserved for future use). */
	private FloatBuffer unused = BufferUtils.createFloatBuffer(16);

	/**
	 * Updates the frustum by extracting current OpenGL matrices and computing clipping planes.
	 * Combines the projection and model-view matrices via multiplication, then extracts
	 * the 6 clipping plane equations and normalizes them for use in culling tests.
	 * This method should be called once per frame after the view matrix is configured.
	 *
	 * @return the singleton Frustrum instance with updated plane equations
	 */
	public static Frustrum update() {
		// Get the singleton instance for matrix operations
		FrustrumImpl frustumImpl = instance;

		// Clear all buffers to prepare for matrix extraction from OpenGL
		frustumImpl.projectionBuff.clear();
		frustumImpl.modelviewBuff.clear();
		frustumImpl.unused.clear();

		// Extract the OpenGL projection matrix (defines camera field of view)
		// GL11.GL_PROJECTION_MATRIX = 2983
		GL11.glGetFloat(2983, frustumImpl.projectionBuff);

		// Extract the OpenGL model-view matrix (defines camera position and orientation)
		// GL11.GL_MODELVIEW_MATRIX = 2982
		GL11.glGetFloat(2982, frustumImpl.modelviewBuff);

		// Flip buffers to prepare for reading (set position to 0, limit to 16)
		frustumImpl.projectionBuff.flip().limit(16);

		// Copy projection matrix data into the frustum object
		frustumImpl.projectionBuff.get(frustumImpl.projection);

		// Flip model-view buffer and copy its data
		frustumImpl.modelviewBuff.flip().limit(16);

		// Copy model-view matrix data into the frustum object
		frustumImpl.modelviewBuff.get(frustumImpl.modelview);

		// Compute the combined projection-model-view matrix by multiplying them
		// This step combines camera transforms with perspective projection
		// Combined[i] = ModelView[row] · Projection[column]

		// Row 0 of combined matrix
		frustumImpl.clipping[0] = frustumImpl.modelview[0] * frustumImpl.projection[0] + frustumImpl.modelview[1] * frustumImpl.projection[4] + frustumImpl.modelview[2] * frustumImpl.projection[8] + frustumImpl.modelview[3] * frustumImpl.projection[12];
		frustumImpl.clipping[1] = frustumImpl.modelview[0] * frustumImpl.projection[1] + frustumImpl.modelview[1] * frustumImpl.projection[5] + frustumImpl.modelview[2] * frustumImpl.projection[9] + frustumImpl.modelview[3] * frustumImpl.projection[13];
		frustumImpl.clipping[2] = frustumImpl.modelview[0] * frustumImpl.projection[2] + frustumImpl.modelview[1] * frustumImpl.projection[6] + frustumImpl.modelview[2] * frustumImpl.projection[10] + frustumImpl.modelview[3] * frustumImpl.projection[14];
		frustumImpl.clipping[3] = frustumImpl.modelview[0] * frustumImpl.projection[3] + frustumImpl.modelview[1] * frustumImpl.projection[7] + frustumImpl.modelview[2] * frustumImpl.projection[11] + frustumImpl.modelview[3] * frustumImpl.projection[15];

		// Row 1 of combined matrix
		frustumImpl.clipping[4] = frustumImpl.modelview[4] * frustumImpl.projection[0] + frustumImpl.modelview[5] * frustumImpl.projection[4] + frustumImpl.modelview[6] * frustumImpl.projection[8] + frustumImpl.modelview[7] * frustumImpl.projection[12];
		frustumImpl.clipping[5] = frustumImpl.modelview[4] * frustumImpl.projection[1] + frustumImpl.modelview[5] * frustumImpl.projection[5] + frustumImpl.modelview[6] * frustumImpl.projection[9] + frustumImpl.modelview[7] * frustumImpl.projection[13];
		frustumImpl.clipping[6] = frustumImpl.modelview[4] * frustumImpl.projection[2] + frustumImpl.modelview[5] * frustumImpl.projection[6] + frustumImpl.modelview[6] * frustumImpl.projection[10] + frustumImpl.modelview[7] * frustumImpl.projection[14];
		frustumImpl.clipping[7] = frustumImpl.modelview[4] * frustumImpl.projection[3] + frustumImpl.modelview[5] * frustumImpl.projection[7] + frustumImpl.modelview[6] * frustumImpl.projection[11] + frustumImpl.modelview[7] * frustumImpl.projection[15];

		// Row 2 of combined matrix
		frustumImpl.clipping[8] = frustumImpl.modelview[8] * frustumImpl.projection[0] + frustumImpl.modelview[9] * frustumImpl.projection[4] + frustumImpl.modelview[10] * frustumImpl.projection[8] + frustumImpl.modelview[11] * frustumImpl.projection[12];
		frustumImpl.clipping[9] = frustumImpl.modelview[8] * frustumImpl.projection[1] + frustumImpl.modelview[9] * frustumImpl.projection[5] + frustumImpl.modelview[10] * frustumImpl.projection[9] + frustumImpl.modelview[11] * frustumImpl.projection[13];
		frustumImpl.clipping[10] = frustumImpl.modelview[8] * frustumImpl.projection[2] + frustumImpl.modelview[9] * frustumImpl.projection[6] + frustumImpl.modelview[10] * frustumImpl.projection[10] + frustumImpl.modelview[11] * frustumImpl.projection[14];
		frustumImpl.clipping[11] = frustumImpl.modelview[8] * frustumImpl.projection[3] + frustumImpl.modelview[9] * frustumImpl.projection[7] + frustumImpl.modelview[10] * frustumImpl.projection[11] + frustumImpl.modelview[11] * frustumImpl.projection[15];

		// Row 3 of combined matrix
		frustumImpl.clipping[12] = frustumImpl.modelview[12] * frustumImpl.projection[0] + frustumImpl.modelview[13] * frustumImpl.projection[4] + frustumImpl.modelview[14] * frustumImpl.projection[8] + frustumImpl.modelview[15] * frustumImpl.projection[12];
		frustumImpl.clipping[13] = frustumImpl.modelview[12] * frustumImpl.projection[1] + frustumImpl.modelview[13] * frustumImpl.projection[5] + frustumImpl.modelview[14] * frustumImpl.projection[9] + frustumImpl.modelview[15] * frustumImpl.projection[13];
		frustumImpl.clipping[14] = frustumImpl.modelview[12] * frustumImpl.projection[2] + frustumImpl.modelview[13] * frustumImpl.projection[6] + frustumImpl.modelview[14] * frustumImpl.projection[10] + frustumImpl.modelview[15] * frustumImpl.projection[14];
		frustumImpl.clipping[15] = frustumImpl.modelview[12] * frustumImpl.projection[3] + frustumImpl.modelview[13] * frustumImpl.projection[7] + frustumImpl.modelview[14] * frustumImpl.projection[11] + frustumImpl.modelview[15] * frustumImpl.projection[15];

		// Extract and normalize the 6 clipping planes from the combined matrix
		// These plane equations define the left, right, top, bottom, near, and far clipping boundaries

		// LEFT plane: x = -1 in clip space (plane = column3 - column0)
		frustumImpl.frustrum[0][0] = frustumImpl.clipping[3] - frustumImpl.clipping[0];
		frustumImpl.frustrum[0][1] = frustumImpl.clipping[7] - frustumImpl.clipping[4];
		frustumImpl.frustrum[0][2] = frustumImpl.clipping[11] - frustumImpl.clipping[8];
		frustumImpl.frustrum[0][3] = frustumImpl.clipping[15] - frustumImpl.clipping[12];
		normalize(frustumImpl.frustrum, 0);

		// RIGHT plane: x = 1 in clip space (plane = column3 + column0)
		frustumImpl.frustrum[1][0] = frustumImpl.clipping[3] + frustumImpl.clipping[0];
		frustumImpl.frustrum[1][1] = frustumImpl.clipping[7] + frustumImpl.clipping[4];
		frustumImpl.frustrum[1][2] = frustumImpl.clipping[11] + frustumImpl.clipping[8];
		frustumImpl.frustrum[1][3] = frustumImpl.clipping[15] + frustumImpl.clipping[12];
		normalize(frustumImpl.frustrum, 1);

		// TOP plane: y = 1 in clip space (plane = column3 + column1)
		frustumImpl.frustrum[2][0] = frustumImpl.clipping[3] + frustumImpl.clipping[1];
		frustumImpl.frustrum[2][1] = frustumImpl.clipping[7] + frustumImpl.clipping[5];
		frustumImpl.frustrum[2][2] = frustumImpl.clipping[11] + frustumImpl.clipping[9];
		frustumImpl.frustrum[2][3] = frustumImpl.clipping[15] + frustumImpl.clipping[13];
		normalize(frustumImpl.frustrum, 2);

		// BOTTOM plane: y = -1 in clip space (plane = column3 - column1)
		frustumImpl.frustrum[3][0] = frustumImpl.clipping[3] - frustumImpl.clipping[1];
		frustumImpl.frustrum[3][1] = frustumImpl.clipping[7] - frustumImpl.clipping[5];
		frustumImpl.frustrum[3][2] = frustumImpl.clipping[11] - frustumImpl.clipping[9];
		frustumImpl.frustrum[3][3] = frustumImpl.clipping[15] - frustumImpl.clipping[13];
		normalize(frustumImpl.frustrum, 3);

		// NEAR plane: z = -1 in clip space (plane = column3 - column2)
		frustumImpl.frustrum[4][0] = frustumImpl.clipping[3] - frustumImpl.clipping[2];
		frustumImpl.frustrum[4][1] = frustumImpl.clipping[7] - frustumImpl.clipping[6];
		frustumImpl.frustrum[4][2] = frustumImpl.clipping[11] - frustumImpl.clipping[10];
		frustumImpl.frustrum[4][3] = frustumImpl.clipping[15] - frustumImpl.clipping[14];
		normalize(frustumImpl.frustrum, 4);

		// FAR plane: z = 1 in clip space (plane = column3 + column2)
		frustumImpl.frustrum[5][0] = frustumImpl.clipping[3] + frustumImpl.clipping[2];
		frustumImpl.frustrum[5][1] = frustumImpl.clipping[7] + frustumImpl.clipping[6];
		frustumImpl.frustrum[5][2] = frustumImpl.clipping[11] + frustumImpl.clipping[10];
		frustumImpl.frustrum[5][3] = frustumImpl.clipping[15] + frustumImpl.clipping[14];
		normalize(frustumImpl.frustrum, 5);

		// Return the updated frustum instance ready for culling tests
		return instance;
	}

	/**
	 * Normalizes a clipping plane equation to unit length.
	 * Divides all plane coefficients (A, B, C, D) by the magnitude of the normal vector (A, B, C).
	 * This ensures consistent distance calculations for frustum culling.
	 *
	 * @param frustrumPlanes the 2D array of frustum plane equations
	 * @param planeIndex the index of the plane to normalize (0-5)
	 */
	private static void normalize(float[][] frustrumPlanes, int planeIndex) {
		// Calculate the magnitude of the plane's normal vector (A, B, C components)
		float magnitude = MathHelper.sqrt(frustrumPlanes[planeIndex][0] * frustrumPlanes[planeIndex][0] +
										  frustrumPlanes[planeIndex][1] * frustrumPlanes[planeIndex][1] +
										  frustrumPlanes[planeIndex][2] * frustrumPlanes[planeIndex][2]);

		// Normalize all 4 coefficients by dividing by the magnitude
		frustrumPlanes[planeIndex][0] /= magnitude;
		frustrumPlanes[planeIndex][1] /= magnitude;
		frustrumPlanes[planeIndex][2] /= magnitude;
		frustrumPlanes[planeIndex][3] /= magnitude;
	}

}
