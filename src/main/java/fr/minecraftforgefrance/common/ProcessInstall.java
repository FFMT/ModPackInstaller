package fr.minecraftforgefrance.common;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class ProcessInstall
{
	public ProcessInstall()
	{
		for(DownloadEntry entry : DownloadMod.instance().getRemoteList())
		{
			System.out.println(entry.getUrl());
			System.out.println(entry.getMd5());
			System.out.println(entry.getSize());
		}
		JFrame frame = new JFrame("Install progress ...");
		frame.setVisible(true);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width / 2) - (frame.getSize().width / 2);
		int y = (dim.height / 2) - (frame.getSize().height / 2);
		frame.setLocation(x, y);
	}
}