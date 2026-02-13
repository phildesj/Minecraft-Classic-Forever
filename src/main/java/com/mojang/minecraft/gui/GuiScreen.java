package com.mojang.minecraft.gui;

import com.mojang.minecraft.Minecraft;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * The base class for all GUI screens in the game.
 */
public class GuiScreen extends Screen {

	/** Reference to the Minecraft instance. */
	protected Minecraft minecraft;
	/** The width of the screen. */
	protected int width;
	/** The height of the screen. */
	protected int height;
	/** A list of buttons on this screen. */
	protected List<Button> buttons = new ArrayList<>();
	/** Whether the screen grabs the mouse cursor. */
	public boolean grabsMouse = false;
	/** Reference to the font renderer for drawing text. */
	protected FontRenderer fontRenderer;

	/**
	 * Renders the screen and its components.
	 *
	 * @param mouseX The X coordinate of the mouse.
	 * @param mouseY The Y coordinate of the mouse.
	 */
	public void render(int mouseX, int mouseY) {
		for (Button button : this.buttons) {
			if (button.visible) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.minecraft.textureManager.load("/gui/gui.png"));
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

				byte state = 1; // Default state
				boolean hovered = mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height;

				if (!button.active) {
					state = 0; // Disabled state
				} else if (hovered) {
					state = 2; // Hovered state
				}

				button.drawImage(button.x, button.y, 0, 46 + state * 20, button.width / 2, button.height);
				button.drawImage(button.x + button.width / 2, button.y, 200 - button.width / 2, 46 + state * 20, button.width / 2, button.height);

				int color = 14737632; // Normal color
				if (!button.active) {
					color = -6250336; // Disabled color
				} else if (hovered) {
					color = 16777120; // Hovered color
				}

				Button.drawCenteredString(this.fontRenderer, button.text, button.x + button.width / 2, button.y + (button.height - 8) / 2, color);
			}
		}
	}

	/**
	 * Handles key press events.
	 *
	 * @param typedChar The character typed.
	 * @param keyCode   The code of the key pressed.
	 */
	protected void onKeyPress(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			this.minecraft.setCurrentScreen(null);
			this.minecraft.grabMouse();
		}
	}

	/**
	 * Handles mouse click events.
	 *
	 * @param mouseX      The X coordinate of the mouse.
	 * @param mouseY      The Y coordinate of the mouse.
	 * @param mouseButton The button that was clicked.
	 */
	protected void onMouseClick(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) { // Left click
			for (Button button : this.buttons) {
				if (button.active && mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height) {
					this.onButtonClick(button);
				}
			}
		}
	}

	/**
	 * Called when a button on the screen is clicked.
	 *
	 * @param button The button that was clicked.
	 */
	protected void onButtonClick(Button button) {}

	/**
	 * Opens the screen and initializes its dimensions.
	 *
	 * @param minecraft The Minecraft instance.
	 * @param width     The initial width.
	 * @param height    The initial height.
	 */
	public final void open(Minecraft minecraft, int width, int height) {
		this.minecraft = minecraft;
		this.fontRenderer = minecraft.fontRenderer;
		this.width = width;
		this.height = height;
		this.onOpen();
	}

	/**
	 * Called when the screen is opened.
	 */
	public void onOpen() {}

	/**
	 * Processes mouse and keyboard input.
	 */
	public final void doInput() {
		while (Mouse.next()) {
			this.mouseEvent();
		}

		while (Keyboard.next()) {
			this.keyboardEvent();
		}
	}

	/**
	 * Processes a single mouse event.
	 */
	public final void mouseEvent() {
		if (Mouse.getEventButtonState()) {
			int mouseX = Mouse.getEventX() * this.width / this.minecraft.width;
			int mouseY = this.height - Mouse.getEventY() * this.height / this.minecraft.height - 1;
			this.onMouseClick(mouseX, mouseY, Mouse.getEventButton());
		}
	}

	/**
	 * Processes a single keyboard event.
	 */
	public final void keyboardEvent() {
		if (Keyboard.getEventKeyState()) {
			this.onKeyPress(Keyboard.getEventCharacter(), Keyboard.getEventKey());
		}
	}

	/**
	 * Called every game tick to update the screen.
	 */
	public void tick() {}

	/**
	 * Called when the screen is closed.
	 */
	public void onClose() {}
}


