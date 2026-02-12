package com.mojang.minecraft;

import com.mojang.minecraft.level.tile.Block;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SessionData encapsulates player session information including authentication details
 * and a list of allowed blocks that can be placed in the game world.
 * This class manages both player credentials and the inventory of buildable materials.
 */
public final class SessionData {

	// Unmodifiable list of blocks allowed for placement in the game world
	public static final List<Block> ALLOWED_BLOCKS;

	// Player username for the session
	private final String username;

	// Session identifier for authentication
	private final String sessionId;

	// Multiplayer password for server authentication
	private final String mppass;

	// Flag indicating whether the player account has been paid for
	private final boolean hasPaid;

	/**
	 * Constructs a SessionData object with the provided username and session ID.
	 * The multiplayer password and paid status are initialized to default values.
	 *
	 * @param username the player's username
	 * @param sessionId the session identifier for authentication
	 */
	public SessionData(String username, String sessionId) {
		this.username = username;
		this.sessionId = sessionId;
		this.mppass = "";
		this.hasPaid = false;
	}

	/**
	 * Static initializer that builds the list of allowed blocks.
	 * This list is immutable and shared across all SessionData instances.
	 * Includes all the basic building materials and ores from classic Minecraft.
	 */
	static {
		// Create the allowed blocks list with capacity for all block types
		List<Block> blocks = new ArrayList<>();

		// Natural stone and earth blocks
		blocks.add(Block.STONE);
		blocks.add(Block.COBBLESTONE);
		blocks.add(Block.BRICK);
		blocks.add(Block.DIRT);
		blocks.add(Block.SAND);
		blocks.add(Block.GRAVEL);
		blocks.add(Block.SPONGE);

		// Wood and vegetation blocks
		blocks.add(Block.WOOD);
		blocks.add(Block.LOG);
		blocks.add(Block.LEAVES);
		blocks.add(Block.SAPLING);
		blocks.add(Block.DANDELION);
		blocks.add(Block.ROSE);
		blocks.add(Block.BROWN_MUSHROOM);
		blocks.add(Block.RED_MUSHROOM);

		// Glass and decorative blocks
		blocks.add(Block.GLASS);
		blocks.add(Block.SLAB);
		blocks.add(Block.MOSSY_COBBLESTONE);
		blocks.add(Block.BOOKSHELF);

		// Wool blocks in various colors
		blocks.add(Block.RED_WOOL);
		blocks.add(Block.ORANGE_WOOL);
		blocks.add(Block.YELLOW_WOOL);
		blocks.add(Block.LIME_WOOL);
		blocks.add(Block.GREEN_WOOL);
		blocks.add(Block.AQUA_GREEN_WOOL);
		blocks.add(Block.CYAN_WOOL);
		blocks.add(Block.BLUE_WOOL);
		blocks.add(Block.PURPLE_WOOL);
		blocks.add(Block.INDIGO_WOOL);
		blocks.add(Block.VIOLET_WOOL);
		blocks.add(Block.MAGENTA_WOOL);
		blocks.add(Block.PINK_WOOL);
		blocks.add(Block.BLACK_WOOL);
		blocks.add(Block.GRAY_WOOL);
		blocks.add(Block.WHITE_WOOL);

		// Ore blocks
		blocks.add(Block.COAL_ORE);
		blocks.add(Block.IRON_ORE);
		blocks.add(Block.GOLD_ORE);

		// Refined ore blocks
		blocks.add(Block.IRON_BLOCK);
		blocks.add(Block.GOLD_BLOCK);

		// Explosive block
		blocks.add(Block.TNT);

		// Obsidian block
		blocks.add(Block.OBSIDIAN);

		// Make the list immutable and assign it to the constant
		ALLOWED_BLOCKS = Collections.unmodifiableList(blocks);
	}

	/**
	 * Gets the player's username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the session identifier.
	 *
	 * @return the session ID
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Gets the multiplayer password.
	 *
	 * @return the multiplayer password
	 */
	public String getMppass() {
		return mppass;
	}

	/**
	 * Checks if the player account has been paid for.
	 *
	 * @return true if the account is paid, false otherwise
	 */
	public boolean hasPaid() {
		return hasPaid;
	}
}
