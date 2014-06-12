package fr.minecraftforgefrance.updater;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class UpdaterFrame extends JFrame
{
	public UpdaterFrame()
	{
		this.setTitle("ModPack Updater");
		this.setSize(400, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setVisible(true);
	}
}