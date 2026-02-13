package com.mojang.minecraft.item;

import com.mojang.minecraft.model.ModelPart;
import com.mojang.minecraft.model.TexturedQuad;
import com.mojang.minecraft.model.Vertex;

/**
 * Represents a 3D model for an item in the world.
 * This is typically rendered as a small cube showing a portion of the terrain texture.
 */
public class ItemModel {

	/** The model part used to render the item. */
	private final ModelPart model = new ModelPart(0, 0);

	/**
	 * Creates a new ItemModel.
	 *
	 * @param textureIndex The index of the texture in the terrain sprite sheet.
	 */
	public ItemModel(int textureIndex) {
		float minCoord = -2.0F;
		float maxCoord = 2.0F;

		model.vertices = new Vertex[8];
		model.quads = new TexturedQuad[6];

		// Define the 8 vertices of the cube
		Vertex v1 = new Vertex(minCoord, minCoord, minCoord, 0.0F, 0.0F);
		Vertex v2 = new Vertex(maxCoord, minCoord, minCoord, 0.0F, 8.0F);
		Vertex v3 = new Vertex(maxCoord, maxCoord, minCoord, 8.0F, 8.0F);
		Vertex v4 = new Vertex(minCoord, maxCoord, minCoord, 8.0F, 0.0F);
		Vertex v5 = new Vertex(minCoord, minCoord, maxCoord, 0.0F, 0.0F);
		Vertex v6 = new Vertex(maxCoord, minCoord, maxCoord, 0.0F, 8.0F);
		Vertex v7 = new Vertex(maxCoord, maxCoord, maxCoord, 8.0F, 8.0F);
		Vertex v8 = new Vertex(minCoord, maxCoord, maxCoord, 8.0F, 0.0F);

		model.vertices[0] = v1;
		model.vertices[1] = v2;
		model.vertices[2] = v3;
		model.vertices[3] = v4;
		model.vertices[4] = v5;
		model.vertices[5] = v6;
		model.vertices[6] = v7;
		model.vertices[7] = v8;

		// Calculate UV coordinates for the texture index
		// The 0.25F and 0.75F offsets seem to be shrinking the texture area slightly to avoid bleeding?
		// Or it's a specific coordinate mapping for the item model size.
		float uMin = ((float) (textureIndex % 16) + (1.0F - 0.25F)) / 16.0F;
		float vMin = ((float) (textureIndex / 16) + (1.0F - 0.25F)) / 16.0F;
		float uMax = ((float) (textureIndex % 16) + 0.25F) / 16.0F;
		float vMax = ((float) (textureIndex / 16) + 0.25F) / 16.0F;

		// Define the 6 faces of the cube
		Vertex[] faceRight = new Vertex[]{v6, v2, v3, v7};
		Vertex[] faceLeft = new Vertex[]{v1, v5, v8, v4};
		Vertex[] faceBottom = new Vertex[]{v6, v5, v1, v2};
		Vertex[] faceTop = new Vertex[]{v3, v4, v8, v7};
		Vertex[] faceBack = new Vertex[]{v2, v1, v4, v3};
		Vertex[] faceFront = new Vertex[]{v5, v6, v7, v8};

		model.quads[0] = new TexturedQuad(faceRight, uMin, vMin, uMax, vMax);
		model.quads[1] = new TexturedQuad(faceLeft, uMin, vMin, uMax, vMax);
		model.quads[2] = new TexturedQuad(faceBottom, uMin, vMin, uMax, vMax);
		model.quads[3] = new TexturedQuad(faceTop, uMin, vMin, uMax, vMax);
		model.quads[4] = new TexturedQuad(faceBack, uMin, vMin, uMax, vMax);
		model.quads[5] = new TexturedQuad(faceFront, uMin, vMin, uMax, vMax);
	}

	/**
	 * Renders the item model using its display list.
	 */
	public void generateList() {
		model.render(0.0625F);
	}
}
