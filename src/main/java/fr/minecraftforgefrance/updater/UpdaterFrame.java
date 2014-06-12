package fr.minecraftforgefrance.updater;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class UpdaterFrame extends JFrame
{
	public UpdaterFrame()
	{
		this.setTitle("ModPack Updater");
		this.setSize(400, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);

		JPanel pan = new JPanel();
		pan.setBackground(Color.ORANGE);
		this.setContentPane(pan);

		this.setVisible(true);
	}
}