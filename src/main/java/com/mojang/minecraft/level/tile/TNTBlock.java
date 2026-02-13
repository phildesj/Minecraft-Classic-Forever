package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.item.PrimedTnt;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.particle.ParticleManager;

/**
 * Represents a TNT (Trinitrotoluene) block in Minecraft Classic.
 * TNT blocks are explosive blocks that detonate when ignited or destroyed.
 * TNT blocks have unique textures on top, bottom, and sides showing the explosive material.
 * When TNT is ignited, it creates a PrimedTnt entity with a randomized fuse time.
 * When TNT is broken in creative mode, it spawns break particles instead of detonating.
 * TNT drops no items when broken - it either explodes or creates particles.
 *
 * @author Mojang
 */
public final class TNTBlock extends Block {

	/**
	 * Constructs a TNTBlock with hardcoded block ID and texture.
	 * Uses hardcoded block ID 46 and texture ID 8 for the TNT block.
	 * The blockId and textureId parameters are ignored in favor of hardcoded values.
	 *
	 * @param blockId the block ID parameter (unused, fixed to 46)
	 * @param textureId the texture ID parameter (unused, fixed to 8)
	 */
	public TNTBlock(int blockId, int textureId) {
		super(46, 8);
	}

	/**
	 * Gets the texture ID for the specified face of the TNT block.
	 * Bottom face (index 0) uses textureId + 2 (bottom TNT variant).
	 * Top face (index 1) uses textureId + 1 (top TNT variant).
	 * Side faces (indices 2-5) use textureId (side TNT variant).
	 * This creates visual distinction between different faces of the TNT block.
	 *
	 * @param faceIndex the face index (0=bottom, 1=top, 2-5=sides)
	 * @return the texture ID for the specified face
	 */
	@Override
	protected final int getTextureId(int faceIndex) {
		return faceIndex == 0?this.textureId + 2:(faceIndex == 1?this.textureId + 1:this.textureId);
	}

	/**
	 * Gets the number of items dropped when this TNT block is broken.
	 * TNT blocks drop no items - they either explode or create particles instead.
	 *
	 * @return 0, indicating no items are dropped
	 */
	@Override
	public final int getDropCount() {
		return 0;
	}

	/**
	 * Called when this TNT block explodes.
	 * Creates a PrimedTnt entity at the block's position with a randomized fuse time.
	 * The fuse time is randomized to prevent synchronized explosions.
	 * Fuse time = random(0, life/4) + life/8, creating variation between life/8 and 3*life/8.
	 * In creative mode, TNT does not explode - no entity is created.
	 *
	 * @param level the level containing this TNT block
	 * @param x the x coordinate of the TNT block
	 * @param y the y coordinate of the TNT block
	 * @param z the z coordinate of the TNT block
	 */
	@Override
	public final void explode(Level level, int x, int y, int z) {
		if(!level.creativeMode) {
			// Create a primed TNT entity at the center of the block
			PrimedTnt primedTntEntity = new PrimedTnt(level, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
			// Set randomized fuse time to prevent synchronized explosions
			primedTntEntity.life = random.nextInt(primedTntEntity.life / 4) + primedTntEntity.life / 8;
			level.addEntity(primedTntEntity);
		}

	}

	/**
	 * Called when this TNT block is broken/destroyed.
	 * In creative mode, spawns break particles instead of detonating.
	 * In survival mode, creates a PrimedTnt entity to simulate detonation.
	 * This allows TNT to be broken in creative mode for building/editing without explosive damage.
	 *
	 * @param level the level containing this TNT block
	 * @param positionX the x coordinate of the broken TNT block
	 * @param positionY the y coordinate of the broken TNT block
	 * @param positionZ the z coordinate of the broken TNT block
	 * @param particleManager the particle manager for spawning visual effects
	 */
	@Override
	public final void spawnBreakParticles(Level level, int positionX, int positionY, int positionZ, ParticleManager particleManager) {
		if(!level.creativeMode) {
			// Survival mode: create primed TNT entity for explosion
			level.addEntity(new PrimedTnt(level, (float)positionX + 0.5F, (float)positionY + 0.5F, (float)positionZ + 0.5F));
		} else {
			// Creative mode: spawn break particles instead of detonating
			super.spawnBreakParticles(level, positionX, positionY, positionZ, particleManager);
		}
	}
}



