package org.oyasunadev.minecraft;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.MinecraftApplet$1;

import javax.swing.*;
import java.awt.*;

public final class SPFrame extends JFrame
{
	public SPFrame()
	{
		setTitle("MinecraftMania - Single Player");
		setSize(1600, 900);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;

		if(getWidth() == width && getHeight() == height)
		{
			setUndecorated(true);
		}
	}

	public void startMinecraft(boolean serverMode) {

		if(!serverMode){
			MCApplet applet = new MCApplet();
			MinecraftApplet$1 canvas = new MinecraftApplet$1(applet);

			Minecraft minecraft = new Minecraft(canvas, applet, getWidth(), getHeight(), false);

			canvas.setSize(getWidth(), getHeight());

			add(canvas, "Center");

			pack();

			new Thread(minecraft).start();
		} else {
			//TODO : run the game headless
		}


	}

	public void finish()
	{
		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2);
	}
}
