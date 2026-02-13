package org.oyasunadev.minecraft;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver Yasuna
 * Date: 9/23/12
 * Time: 4:04 PM
 */
public class Start {
	private static boolean serverMode = false;

	public static void main(String[] args) {
		new Start(args);
	}

	void parseArgs(String[] args){
		for (String arg : args) {
			if (arg.equals("-server")) {
				serverMode = true;
			}
		}
	}

	public Start(String[] args) {

		parseArgs(args);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		SPFrame spFrame = new org.oyasunadev.minecraft.SPFrame();

		spFrame.setVisible(true);

		spFrame.startMinecraft(serverMode);
		spFrame.finish();
	}
}
