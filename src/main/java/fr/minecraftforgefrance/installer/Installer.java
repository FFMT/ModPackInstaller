package fr.minecraftforgefrance.installer;

import javax.swing.UIManager;

import fr.minecraftforgefrance.common.DownloadEntry;
import fr.minecraftforgefrance.common.DownloadMod;

public class Installer
{
	public static void main(String[] args)
	{
		System.out.println(EnumOS.getPlatform().name());
		System.out.println(EnumOS.getMinecraftDefaultDir().getAbsolutePath());
		for(DownloadEntry entry : DownloadMod.instance().getRemoteList())
		{
			System.out.println(entry.getUrl());
			System.out.println(entry.getMd5());
			System.out.println(entry.getSize());
		}
		System.out.println(DownloadMod.instance().time / 1000000L + "ms");
		
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		InstallerFrame frame = new InstallerFrame();
	}
}