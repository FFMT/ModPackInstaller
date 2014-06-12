package fr.minecraftforgefrance.installer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class InstallerFrame extends JFrame
{
	private Random rand = new Random();
	private JProgressBar progressBar;
	private int level = 1, currentClick, totalClick = 0, score;
	private JTextField field, field2;
	private JButton button, button2, button3;
	private JPanel pan, lab;

	public InstallerFrame()
	{
		this.setTitle("Installer");
		this.setSize(400, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setVisible(true);
	}
}