package com.mojang.minecraft.item;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

/**
 * Represents an animation of an entity (like an item) being "taken" or collected by another entity (the player).
 * The entity moves quickly towards the player before being removed.
 */
public class TakeEntityAnim extends Entity {

	/** Serial version UID for serialization. */
	private static final long serialVersionUID = 1L;

	/** Number of ticks the animation has been running. */
	private int time = 0;

	/** The entity being collected. */
	private final Entity item;
	/** The entity collecting the item (usually a Player). */
	private final Entity player;

	/** Original X position of the item when the animation started. */
	private final float xOrg;
	/** Original Y position of the item when the animation started. */
	private final float yOrg;
	/** Original Z position of the item when the animation started. */
	private final float zOrg;

	/**
	 * Creates a new TakeEntityAnim animation.
	 *
	 * @param level  The level context.
	 * @param item   The entity to be animated.
	 * @param player The entity that is collecting the item.
	 */
	public TakeEntityAnim(Level level, Entity item, Entity player) {
		super(level);

		this.item = item;
		this.player = player;

		setSize(1.0F, 1.0F);

		xOrg = item.x;
		yOrg = item.y;
		zOrg = item.z;
	}

	/**
	 * Updates the animation state. Interpolates the item's position towards the player.
	 */
	@Override
	public void tick() {
		time++;

		// The animation lasts for 3 ticks.
		if (time >= 3) {
			remove();
		}

		// Calculate progress (0.0 to 1.0) and squared for an accelerating "ease-in" effect.
		float progress = (float) time / 3.0F;
		float speedFactor = progress * progress;

		xo = item.xo = item.x;
		yo = item.yo = item.y;
		zo = item.zo = item.z;

		// Move towards the player's current position (target is slightly below eye level)
		x = item.x = xOrg + (player.x - xOrg) * speedFactor;
		y = item.y = yOrg + (player.y - 1.0F - yOrg) * speedFactor;
		z = item.z = zOrg + (player.z - zOrg) * speedFactor;

		setPos(x, y, z);
	}

	/**
	 * Renders the animated entity by delegating to its own render method.
	 *
	 * @param textureManager The texture manager.
	 * @param partialTicks   The fraction of a tick passed since the last update.
	 */
	@Override
	public void render(TextureManager textureManager, float partialTicks) {
		item.render(textureManager, partialTicks);
	}
}
