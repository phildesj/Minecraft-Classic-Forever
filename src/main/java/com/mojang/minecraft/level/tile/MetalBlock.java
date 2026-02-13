package com.mojang.minecraft.level.tile;

/**
 * Represents a metal block in Minecraft Classic.
 * Metal blocks are solid, opaque blocks that have different textures on top, bottom, and sides.
 * The top face uses a darker/different variant of the texture, the bottom face uses a lighter variant,
 * and the side faces use the standard texture. This creates visual distinction for metal materials.
 *
 * @author Mojang
 */
public final class MetalBlock extends Block {

	/**
	 * Constructs a MetalBlock with the specified block ID and texture ID.
	 * Metal blocks are initialized with a custom texture ID that determines how the block appears.
	 *
	 * @param blockId the unique block ID for this metal type
	 * @param textureId the base texture ID for this metal's appearance
	 */
	public MetalBlock(int blockId, int textureId) {
		super(blockId);
		this.textureId = textureId;
	}

	/**
	 * Gets the texture ID for the specified face of the metal block.
	 * Top face (index 1) uses textureId - 16 (darker variant for top surface).
	 * Bottom face (index 0) uses textureId + 16 (lighter variant for bottom surface).
	 * Side faces use the base textureId (standard metal texture).
	 * This creates visual distinction between different faces of the metal block.
	 *
	 * @param faceIndex the face index (0=bottom, 1=top, 2-5=sides)
	 * @return the texture ID for the specified face
	 */
	@Override
	protected final int getTextureId(int faceIndex) {
		return faceIndex == 1?this.textureId - 16:(faceIndex == 0?this.textureId + 16:this.textureId);
	}
}


