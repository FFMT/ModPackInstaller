package fr.minecraftforgefrance.installer;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class CreditFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	public CreditFrame(Dimension dim)
	{
		this.setTitle("Credit");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JButton sponsorButton = new JButton("Minecraft Forge France website");
		sponsorButton.setAlignmentX(CENTER_ALIGNMENT);
		sponsorButton.setAlignmentY(CENTER_ALIGNMENT);
		sponsorButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					Desktop.getDesktop().browse(new URI("http://www.minecraftforgefrance.fr"));
				}
				catch(Exception ex)
				{
					JOptionPane.showMessageDialog(CreditFrame.this, "An error occurred launching the browser", "Error launching browser", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		JPanel sponsorPanel = new JPanel();
		sponsorPanel.setLayout(new BoxLayout(sponsorPanel, BoxLayout.X_AXIS));
		sponsorPanel.setAlignmentX(CENTER_ALIGNMENT);
		sponsorPanel.setAlignmentY(CENTER_ALIGNMENT);
		sponsorPanel.add(sponsorButton);
		
		JLabel text = new JLabel();
		text.setText("<html><center>Created by :</center><br>"
				+ "robin4002 - main programm, project initiator<br>"
				+ "kevin_68 - graphic interface<br>"
				+ "<br><center>Other contribution :</center><br>"
				+ "cpw - creator of the forge installer that served as inspiration<br>"
				+ "Forge team</html>");
		text.setAlignmentX(CENTER_ALIGNMENT);
		text.setAlignmentY(CENTER_ALIGNMENT);
		
		panel.add(text);
		panel.add(sponsorPanel);
		this.add(panel);
		this.pack();
		int x = (dim.width / 2) - (this.getSize().width / 2);
		int y = (dim.height / 2) - (this.getSize().height / 2);
		this.setLocation(x, y);
	}
}