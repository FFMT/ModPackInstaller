package fr.minecraftforgefrance.installer;

import static fr.minecraftforgefrance.common.Localization.LANG;

import javax.swing.JOptionPane;
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
		if(!RemoteInfoReader.instance().init())
		{
			JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotreadremote"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		InstallerFrame frame = new InstallerFrame();
		frame.run();
	}
}