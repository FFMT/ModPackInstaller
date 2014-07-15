package fr.minecraftforgefrance.installer;

import javax.swing.UIManager;

import fr.minecraftforgefrance.common.Localization;
import fr.minecraftforgefrance.common.RemoteInfoReader;

public class Installer
{
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		Localization.init();
		RemoteInfoReader.instance = new RemoteInfoReader(LocalInfoReader.instance().getRemoteUrl());
		InstallerFrame frame = new InstallerFrame();
		frame.run();
	}
}