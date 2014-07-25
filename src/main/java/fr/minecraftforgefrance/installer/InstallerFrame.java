package fr.minecraftforgefrance.installer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.google.common.base.Throwables;

import fr.minecraftforgefrance.common.FileChecker;
import fr.minecraftforgefrance.common.IInstallRunner;
import static fr.minecraftforgefrance.common.Localization.LANG;
import fr.minecraftforgefrance.common.ProcessInstall;
import fr.minecraftforgefrance.common.RemoteInfoReader;
import fr.minecraftforgefrance.plusplus.PlusPlusGame;

public class InstallerFrame extends JFrame implements IInstallRunner
{
	private static final long serialVersionUID = 1L;
	
	public InstallerFrame()
	{
		this.setTitle(String.format(LANG.getTranslation("misc.installer"), RemoteInfoReader.instance().getModPackName()));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);

		BufferedImage image;
		try
		{
			image = ImageIO.read(this.getClass().getResourceAsStream("/installer/logo.png"));
		}
		catch(IOException e)
		{
			throw Throwables.propagate(e);
		}

		final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		ImageIcon icon = new ImageIcon(image);
		JLabel logoLabel = new JLabel(icon);
		logoLabel.setAlignmentX(CENTER_ALIGNMENT);
		logoLabel.setAlignmentY(CENTER_ALIGNMENT);
		if(image.getWidth() > dim.width || image.getHeight() + 10 > dim.height)
		{
			JOptionPane.showMessageDialog(null, LANG.getTranslation("err.bigimage"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
			this.dispose();
		}
		else
		{
			logoLabel.setSize(image.getWidth(), image.getHeight());
			panel.add(logoLabel);
		}

		JButton install = new JButton(LANG.getTranslation("scr.btn.install"));
		install.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
				FileChecker checker = new FileChecker();
				new ProcessInstall(checker, InstallerFrame.this, false);
			}
		});
		
		JButton credit = new JButton(LANG.getTranslation("scr.btn.credits"));
		credit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				CreditFrame credit = new CreditFrame(dim);
				credit.setVisible(true);
			}
		});

		JButton cancel = new JButton(LANG.getTranslation("misc.cancel"));
		cancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		JPanel buttonpanel = new JPanel();
		buttonpanel.add(install);
		buttonpanel.add(credit);
		buttonpanel.add(cancel);

		JLabel welcome = new JLabel(RemoteInfoReader.instance().getWelcome());
		welcome.setAlignmentX(CENTER_ALIGNMENT);
		welcome.setAlignmentY(CENTER_ALIGNMENT);

		JLabel mc = new JLabel("Minecraft : " + RemoteInfoReader.instance().getMinecraftVersion());
		mc.setAlignmentX(CENTER_ALIGNMENT);
		mc.setAlignmentY(CENTER_ALIGNMENT);

		JLabel forge = new JLabel("Forge : " + RemoteInfoReader.instance().getForgeVersion());
		forge.setAlignmentX(CENTER_ALIGNMENT);
		forge.setAlignmentY(CENTER_ALIGNMENT);

		panel.add(welcome);
		panel.add(mc);
		panel.add(forge);
		panel.add(buttonpanel);

		this.add(panel);
		addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e)
			{
				requestFocus();
			}
		});
		this.pack();

		int x = (dim.width / 2) - (this.getSize().width / 2);
		int y = (dim.height / 2) - (this.getSize().height / 2);
		this.setLocation(x, y);

		addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				if(e.getKeyChar() == '+' && !PlusPlusGame.isRunning)
				{
					new PlusPlusGame();
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{

			}

			@Override
			public void keyPressed(KeyEvent e)
			{

			}
		});
	}

	public void run()
	{
		this.setVisible(true);
	}


	@Override
	public void onFinish()
	{
		JOptionPane.showMessageDialog(null, LANG.getTranslation("ok.installationfinished"), LANG.getTranslation("misc.success"), JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public boolean shouldDownloadLib()
	{
		return true;
	}
}