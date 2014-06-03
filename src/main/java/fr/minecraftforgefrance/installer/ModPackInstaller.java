package fr.minecraftforgefrance.installer;

import javax.swing.UIManager;

public class ModPackInstaller
{
	public static void main(String[] args)
	{
		System.out.println(EnumOS.getPlatform().name());
		System.out.println(EnumOS.getMinecraftDefaultDir().getAbsolutePath());
		
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