package fr.minecraftforgefrance.installer;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class InstallerFrame extends JFrame
{
	public InstallerFrame()
	{
		this.setTitle("ModPack Installer");
		this.setSize(400, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		JPanel pan = new JPanel();
		pan.setBackground(Color.ORANGE);
		this.setContentPane(pan);

		this.setVisible(true);
	}
}