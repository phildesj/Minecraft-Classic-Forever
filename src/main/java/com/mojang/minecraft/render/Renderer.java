package com.mojang.minecraft.render;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.liquid.LiquidType;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.model.Vec3D;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.HeldBlock;
import com.mojang.util.MathHelper;
import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Renderer handles all 3D rendering operations for the Minecraft Classic Forever client.
 * This includes player view transformations, camera bobbing, hurt effects, fog rendering,
 * lighting setup, and GUI mode initialization. It manages the held block display and
 * coordinates with the level rendering system.
 */
public final class Renderer {

	/** Reference to the main Minecraft instance for access to game state. */
	public Minecraft minecraft;

	/** Multiplier for fog color intensity (affects overall fog appearance). */
	public float fogColorMultiplier = 1.0F;

	/** Flag indicating whether the display is currently active and rendering. */
	public boolean displayActive = false;

	/** Distance at which fog becomes completely opaque (far clipping plane). */
	public float fogEnd = 0.0F;

	/** The block being held in the player's hand for rendering. */
	public HeldBlock heldBlock;

	/** Counter for the number of ticks since level was loaded. */
	public int levelTicks;

	/** Current entity being rendered or interacted with. */
	public Entity entity = null;

	/** Random number generator for particle effects and visual randomization. */
	public Random random = new Random();

	/** Unused volatile field reserved for future use. */
	private volatile int unused1 = 0;

	/** Unused volatile field reserved for future use. */
	private volatile int unused2 = 0;

	/** Float buffer for OpenGL matrix and color operations (16 floats for 4x4 matrices). */
	private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

	/** Red component of the fog color (normalized 0.0-1.0). */
	public float fogRed;

	/** Blue component of the fog color (normalized 0.0-1.0). */
	public float fogBlue;

	/** Green component of the fog color (normalized 0.0-1.0). */
	public float fogGreen;

	/**
	 * Constructs a new Renderer instance and initializes rendering components.
	 *
	 * @param minecraft the Minecraft instance containing game state
	 */
	public Renderer(Minecraft minecraft) {
		// Store reference to the main Minecraft instance
		this.minecraft = minecraft;

		// Initialize the held block display for rendering items in player's hand
		this.heldBlock = new HeldBlock(minecraft);
	}

	/**
	 * Gets the interpolated player position for the current frame.
	 * Interpolates between the previous and current position based on the partial tick time.
	 *
	 * @param partialTick the interpolation factor between frames (0.0-1.0)
	 * @return a Vec3D containing the interpolated player position
	 */
	public Vec3D getPlayerVector(float partialTick) {
		// Get the current player instance
		Player player = this.minecraft.player;

		// Interpolate X position between previous and current position
		float x = player.xo + (player.x - player.xo) * partialTick;

		// Interpolate Y position between previous and current position
		float y = player.yo + (player.y - player.yo) * partialTick;

		// Interpolate Z position between previous and current position
		float z = player.zo + (player.z - player.zo) * partialTick;

		// Return the interpolated position as a vector
		return new Vec3D(x, y, z);
	}

	/**
	 * Applies visual effects to the camera when the player is hurt.
	 * Creates a rotation effect that intensifies based on the remaining hurt duration.
	 * When the player dies, applies a death roll effect that accelerates over time.
	 *
	 * @param partialTick the interpolation factor between frames (0.0-1.0)
	 */
	public void hurtEffect(float partialTick) {
		// Get the current player instance
		Player player = this.minecraft.player;

		// Calculate the remaining hurt duration in ticks
		float remainingHurtTime = (float)player.hurtTime - partialTick;

		// Apply death roll rotation if player is dead
		if(player.health <= 0) {
			// Add to the partial tick to create accelerating rotation effect
			partialTick += (float)player.deathTime;

			// Apply spinning rotation around Z axis, accelerating as time increases
			GL11.glRotatef(40.0F - 8000.0F / (partialTick + 200.0F), 0.0F, 0.0F, 1.0F);
		}

		// Apply hurt camera shake if player is within hurt duration
		if(remainingHurtTime >= 0.0F) {
			// Normalize hurt time to 0.0-1.0 range for smooth animation
			remainingHurtTime = MathHelper.sin((remainingHurtTime /= (float)player.hurtDuration) * remainingHurtTime * remainingHurtTime * remainingHurtTime * 3.1415927F);

			// Store the hurt direction for rotation
			float hurtDirection = player.hurtDir;

			// Rotate opposite to hurt direction
			GL11.glRotatef(-player.hurtDir, 0.0F, 1.0F, 0.0F);

			// Apply camera tilt with sinusoidal intensity
			GL11.glRotatef(-remainingHurtTime * 14.0F, 0.0F, 0.0F, 1.0F);

			// Rotate back to original direction
			GL11.glRotatef(hurtDirection, 0.0F, 1.0F, 0.0F);
		}
	}

	/**
	 * Applies the view bobbing animation to simulate natural walking motion.
	 * Creates vertical and rotational camera movement synchronized with player walking.
	 * The effect includes vertical bobbing, side-to-side tilt, and head pitch.
	 *
	 * @param partialTick the interpolation factor between frames (0.0-1.0)
	 */
	public void applyBobbing(float partialTick) {
		// Get the current player instance
		Player player = this.minecraft.player;

		// Calculate the change in walk distance since last frame
		float walkDistanceDelta = player.walkDist - player.walkDistO;

		// Interpolate walk distance for smooth animation
		float interpolatedWalkDist = player.walkDist + walkDistanceDelta * partialTick;

		// Interpolate bobbing amplitude between old and new values
		float bobAmount = player.oBob + (player.bob - player.oBob) * partialTick;

		// Interpolate head tilt between old and new values
		float tiltAmount = player.oTilt + (player.tilt - player.oTilt) * partialTick;

		// Apply horizontal side-to-side movement synchronized with walk cycle
		GL11.glTranslatef(MathHelper.sin(interpolatedWalkDist * 3.1415927F) * bobAmount * 0.5F,
			-Math.abs(MathHelper.cos(interpolatedWalkDist * 3.1415927F) * bobAmount), 0.0F);

		// Apply Z-axis rotation (side tilt) synchronized with walk cycle
		GL11.glRotatef(MathHelper.sin(interpolatedWalkDist * 3.1415927F) * bobAmount * 3.0F, 0.0F, 0.0F, 1.0F);

		// Apply X-axis rotation (head pitch) with offset walk phase
		GL11.glRotatef(Math.abs(MathHelper.cos(interpolatedWalkDist * 3.1415927F + 0.2F) * bobAmount) * 5.0F, 1.0F, 0.0F, 0.0F);

		// Apply head tilt rotation around X-axis
		GL11.glRotatef(tiltAmount, 1.0F, 0.0F, 0.0F);
	}

	/**
	 * Enables or disables OpenGL lighting for the 3D scene.
	 * When enabled, configures directional light from above and sets up ambient lighting.
	 * Light direction is normalized vector pointing diagonally downward.
	 *
	 * @param enableLighting true to enable lighting calculations, false to use flat colors
	 */
	public final void setLighting(boolean enableLighting) {
		if(!enableLighting) {
			// Disable lighting calculations for unlit rendering
			GL11.glDisable(2896);

			// Disable light 0 (main directional light)
			GL11.glDisable(16384);
		} else {
			// Enable lighting calculations
			GL11.glEnable(2896);

			// Enable light 0 (main directional light)
			GL11.glEnable(16384);

			// Enable color material mode for dynamic color changes
			GL11.glEnable(2903);

			// Set color material to respond to ambient and diffuse lighting
			GL11.glColorMaterial(1032, 5634);

			// Set ambient light level (affects shadows)
			float ambientBrightness = 0.7F;

			// Set diffuse light level
			float diffuseBrightness = 0.3F;

			// Create light direction vector (pointing diagonally downward from above)
			Vec3D lightDirection = (new Vec3D(0.0F, -1.0F, 0.5F)).normalize();

			// Configure the main directional light position and intensity
			GL11.glLight(16384, 4611, this.createBuffer(lightDirection.x, lightDirection.y, lightDirection.z, 0.0F));

			// Set diffuse light color (brightness of lit surfaces)
			GL11.glLight(16384, 4609, this.createBuffer(diffuseBrightness, diffuseBrightness, diffuseBrightness, 1.0F));

			// Set specular light color (shininess highlights)
			GL11.glLight(16384, 4608, this.createBuffer(0.0F, 0.0F, 0.0F, 1.0F));

			// Set ambient light color (overall lighting level)
			GL11.glLightModel(2899, this.createBuffer(ambientBrightness, ambientBrightness, ambientBrightness, 1.0F));
		}
	}

	/**
	 * Configures OpenGL for 2D GUI rendering mode with orthographic projection.
	 * Clears the depth buffer, sets up an orthographic view, and positions the camera for 2D drawing.
	 * The projection is based on screen aspect ratio to maintain 240-unit height.
	 */
	public final void enableGuiMode() {
		// Calculate the width of the GUI viewport maintaining aspect ratio (240 units tall)
		int guiWidth = this.minecraft.width * 240 / this.minecraft.height;

		// GUI height is always 240 units
		int guiHeight = this.minecraft.height * 240 / this.minecraft.height;

		// Clear the depth buffer for fresh 2D rendering
		GL11.glClear(256);

		// Switch to projection matrix mode for view frustum setup
		GL11.glMatrixMode(5889);

		// Load identity matrix to reset projection
		GL11.glLoadIdentity();

		// Set up orthographic projection for 2D rendering
		// Origin (0,0) at top-left, coordinates increase right and down
		GL11.glOrtho(0.0D, (double)guiWidth, (double)guiHeight, 0.0D, 100.0D, 300.0D);

		// Switch to model-view matrix mode for camera positioning
		GL11.glMatrixMode(5888);

		// Load identity matrix to reset model-view
		GL11.glLoadIdentity();

		// Position camera 200 units back in Z for 2D rendering
		GL11.glTranslatef(0.0F, 0.0F, -200.0F);
	}

	/**
	 * Updates fog rendering settings based on player location and game state.
	 * If the player is submerged in a liquid, applies underwater or lava fog effects.
	 * Otherwise, configures standard atmospheric fog for the sky.
	 * Supports anaglyph 3D color adjustments when enabled in game settings.
	 */
	public void updateFog() {
		// Get references to the current level and player
		Level level = this.minecraft.level;
		Player player = this.minecraft.player;

		// Set the fog color in OpenGL
		GL11.glFog(2918, this.createBuffer(this.fogRed, this.fogBlue, this.fogGreen, 1.0F));

		// Set the default normal vector for lighting calculations
		GL11.glNormal3f(0.0F, -1.0F, 0.0F);

		// Set the default color to white (full brightness)
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		// Check what block the player's head is in (eye level + 0.12F offset)
		Block blockAtEye = Block.blocks[level.getTile((int)player.x, (int)(player.y + 0.12F), (int)player.z)];

		// Determine if the player is submerged in a liquid
		if(blockAtEye != null && blockAtEye.getLiquidType() != LiquidType.NOT_LIQUID) {
			// Get the type of liquid the player is in
			LiquidType liquidType = blockAtEye.getLiquidType();

			// Use exponential fog model for liquid rendering
			GL11.glFogi(2917, 2048);

			// Variables for fog color components
			float fogColorR;
			float fogColorG;
			float fogColorB;
			float grayscale;

			// Configure fog and lighting for water
			if(liquidType == LiquidType.WATER) {
				// Set fog density for water (very dense, short visibility)
				GL11.glFogf(2914, 0.1F);

				// Set water color (cyan-blue)
				fogColorR = 0.4F;
				fogColorG = 0.4F;
				fogColorB = 0.9F;

				// Apply anaglyph 3D color conversion if enabled
				if(this.minecraft.settings.anaglyph) {
					// Calculate grayscale intensity using standard luminance formula
					grayscale = (fogColorR * 30.0F + fogColorG * 59.0F + fogColorB * 11.0F) / 100.0F;

					// Apply anaglyph formula: enhance red in one eye, cyan in the other
					fogColorG = (fogColorR * 30.0F + fogColorG * 70.0F) / 100.0F;
					fogColorB = (fogColorR * 30.0F + fogColorB * 70.0F) / 100.0F;
					fogColorR = grayscale;
				}

				// Set ambient light color for underwater lighting
				GL11.glLightModel(2899, this.createBuffer(fogColorR, fogColorG, fogColorB, 1.0F));

			} else if(liquidType == LiquidType.LAVA) {
				// Set fog density for lava (very dense, minimal visibility)
				GL11.glFogf(2914, 2.0F);

				// Set lava color (red-orange)
				fogColorR = 0.4F;
				fogColorG = 0.3F;
				fogColorB = 0.3F;

				// Apply anaglyph 3D color conversion if enabled
				if(this.minecraft.settings.anaglyph) {
					// Calculate grayscale intensity
					grayscale = (fogColorR * 30.0F + fogColorG * 59.0F + fogColorB * 11.0F) / 100.0F;

					// Apply anaglyph formula
					fogColorG = (fogColorR * 30.0F + fogColorG * 70.0F) / 100.0F;
					fogColorB = (fogColorR * 30.0F + fogColorB * 70.0F) / 100.0F;
					fogColorR = grayscale;
				}

				// Set ambient light color for lava lighting
				GL11.glLightModel(2899, this.createBuffer(fogColorR, fogColorG, fogColorB, 1.0F));
			}
		} else {
			// Player is in air - configure standard atmospheric fog

			// Use linear fog model for air (looks more natural)
			GL11.glFogi(2917, 9729);

			// Set fog start distance (fog begins at eye level)
			GL11.glFogf(2915, 0.0F);

			// Set fog end distance (maximum fog distance)
			GL11.glFogf(2916, this.fogEnd);

			// Set ambient light color to white (full brightness)
			GL11.glLightModel(2899, this.createBuffer(1.0F, 1.0F, 1.0F, 1.0F));
		}

		// Enable color material mode for dynamic color changes
		GL11.glEnable(2903);

		// Set color material to respond to specular lighting
		GL11.glColorMaterial(1028, 4608);
	}

	/**
	 * Creates a FloatBuffer containing the specified color components for OpenGL operations.
	 * Reuses an internal buffer to avoid allocation overhead. Proper buffer positioning
	 * is handled automatically for immediate use in OpenGL function calls.
	 *
	 * @param red the red component (normalized 0.0-1.0)
	 * @param green the green component (normalized 0.0-1.0)
	 * @param blue the blue component (normalized 0.0-1.0)
	 * @param alpha the alpha component (normalized 0.0-1.0)
	 * @return the configured FloatBuffer ready for OpenGL use
	 */
	private FloatBuffer createBuffer(float red, float green, float blue, float alpha) {
		// Clear the buffer and reset position to start
		this.buffer.clear();

		// Put color components into the buffer
		this.buffer.put(red).put(green).put(blue).put(alpha);

		// Flip the buffer to prepare it for reading (resets position to 0, sets limit to current position)
		this.buffer.flip();

		// Return the configured buffer ready for use in OpenGL calls
		return this.buffer;
	}
}
