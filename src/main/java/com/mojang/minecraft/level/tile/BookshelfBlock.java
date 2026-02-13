package com.mojang.minecraft.level.tile;

/**
 * Represents a bookshelf block in Minecraft Classic.
 * Bookshelves are decorative blocks that display different textures on the top and bottom faces
 * compared to their front and back faces. They do not drop any items when broken.
 *
 * @author Mojang
 */
public final class BookshelfBlock extends Block {

	/**
	 * Constructs a BookshelfBlock with the specified block ID and texture ID.
	 * Uses hardcoded block ID 47 and texture ID 35 for the bookshelf.
	 *
	 * @param var1 the block ID parameter (unused, fixed to 47)
	 * @param var2 the texture ID parameter (unused, fixed to 35)
	 */
	public BookshelfBlock(int var1, int var2) {
		super(47, 35);
	}

	/**
	 * Gets the texture ID for the specified face of the bookshelf.
	 * Top and bottom faces (texture indices 0-1) use texture 4,
	 * while side faces use the default bookshelf texture.
	 *
	 * @param texture the face index (0=bottom, 1=top, 2-5=sides)
	 * @return the texture ID to use for rendering this face
	 */
	@Override
	protected final int getTextureId(int texture) {
		return texture <= 1?4:this.textureId;
	}

	/**
	 * Gets the number of items dropped when this bookshelf is broken.
	 * Bookshelves do not drop any items.
	 *
	 * @return 0, indicating no items are dropped
	 */
	@Override
	public final int getDropCount() {
		return 0;
	}
}
