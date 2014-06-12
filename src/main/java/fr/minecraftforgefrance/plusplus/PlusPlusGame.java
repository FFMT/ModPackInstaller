package fr.minecraftforgefrance.plusplus;

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

public class PlusPlusGame extends JFrame
{
	private Random rand = new Random();
	private JProgressBar progressBar;
	private int level = 1, currentClick, totalClick = 0, score;
	private JTextField field, field2;
	private JButton button, button2, button3;
	private JPanel pan, lab;

	public PlusPlusGame()
	{
		this.setTitle("The ++ Game");
		this.setSize(400, 200);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		progressBar = new JProgressBar(0, 10);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		button = new JButton(" ++ ");
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				progress(progressBar.getValue() + 1);
			}
		});

		button2 = new JButton(" +++ ");
		button2.setEnabled(false);
		button2.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				progress(progressBar.getValue() + 50);
			}
		});

		button3 = new JButton(" ++++ ");
		button3.setEnabled(false);
		button3.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				progress(progressBar.getValue() + 1000);
			}
		});

		field = new JTextField(20);
		field.setText("Level: " + 1);
		field.setEditable(false);

		field2 = new JTextField(20);
		field2.setText("Clicks for next level: " + 10);
		field2.setEditable(false);

		pan = new JPanel();
		pan.setLayout(new BoxLayout(pan, BoxLayout.PAGE_AXIS));
		pan.setBackground(Color.ORANGE);
		pan.add(progressBar);
		lab = new JPanel();
		lab.setBackground(Color.ORANGE);
		lab.add(button);
		lab.add(button2);
		lab.add(button3);
		pan.add(lab);
		pan.add(field);
		pan.add(field2);
		this.setContentPane(pan);

		this.setVisible(true);
	}

	public void progress(int i)
	{
		if(progressBar.getValue() == progressBar.getMaximum())
		{
			progressBar.setValue(0);
			progressBar.setMaximum(progressBar.getMaximum() * 2);
			level++;
			currentClick = 0;
			Color color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
			pan.setBackground(color);
			lab.setBackground(color);
			if(level == 10)
			{
				button2.setEnabled(true);;
			}
			if(level == 20)
			{
				button3.setEnabled(true);;
			}
		}
		else
		{
			currentClick += i - progressBar.getValue();
			score += i - progressBar.getValue();
			progressBar.setValue(i);
			totalClick++;
		}
		
		field.setText("Level: " + level + "|Score: " + score);
		field2.setText("Clicks for next level: " + (int)(10 * Math.pow(2, level - 1)) + " | Current: " + currentClick + " | Total real click: " + totalClick);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = rand.nextInt(dim.width - 400);
		int y = rand.nextInt(dim.height - 200);
		this.setLocation(x, y);
	}
}