package fr.minecraftforgefrance.updater;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launchwrapper.Launch;
import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import fr.minecraftforgefrance.common.FileChecker;
import fr.minecraftforgefrance.common.IInstallRunner;
import fr.minecraftforgefrance.common.ProcessInstall;
import fr.minecraftforgefrance.common.RemoteInfoReader;

public class Updater implements IInstallRunner
{
	private String[] arguments;
	public static void main(String[] args)
	{
		new Updater(args);
	}
	
	public Updater(String[] args)
	{
		long start = System.currentTimeMillis();
		System.out.println("Start updater !");
		final OptionParser parser = new OptionParser();
		parser.allowsUnrecognizedOptions();
		final OptionSpec<File> gameDirOption = parser.accepts("gameDir", "The game directory").withRequiredArg().ofType(File.class);
		final OptionSpec<String> modpackOption = parser.accepts("version", "The version we launched with").withRequiredArg();

		final OptionSet options = parser.parse(args);
		File mcDir = options.valueOf(gameDirOption);
		String modpackName = options.valueOf(modpackOption);
		File modPackDir = new File(new File(mcDir, "modpacks"), modpackName);

		for(int i = 0; i < args.length; i++)
		{
			if("--gameDir".equals(args[i]))
			{
				args[i + 1] = modPackDir.getAbsolutePath();
			}
		}
		arguments = args;

		System.out.println(modpackName);
		System.out.println(modPackDir.getAbsolutePath());

		File modpackInfo = new File(modPackDir, modpackName + ".json");
		if(!modpackInfo.exists())
		{
			JOptionPane.showMessageDialog(null, "Fatal error with this profile, please install again", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JdomParser jsonParser = new JdomParser();
		JsonRootNode jsonProfileData;

		try
		{
			jsonProfileData = jsonParser.parse(Files.newReader(modpackInfo, Charsets.UTF_8));
		}
		catch(InvalidSyntaxException e)
		{
			JOptionPane.showMessageDialog(null, "Fatal error with this profile, please install again", "Error", JOptionPane.ERROR_MESSAGE);
			throw Throwables.propagate(e);
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "Fatal error with this profile, please install again", "Error", JOptionPane.ERROR_MESSAGE);
			throw Throwables.propagate(e);
		}
		RemoteInfoReader.instance = new RemoteInfoReader(jsonProfileData.getStringValue("remote"));
		FileChecker checker = new FileChecker();
		if(!shouldUpdate(jsonProfileData.getStringValue("mc"), jsonProfileData.getStringValue("forge"), checker))
		{
			System.out.println("No update found, running minecraft !");
			long end = System.currentTimeMillis();
			System.out.println("time to check update : " + (end - start) + " ms");
			runMinecraft(args);
		}
		else
		{
			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			new ProcessInstall(checker, this, true);
		}
	}

	public boolean shouldUpdate(String mcVersion, String forgeVersion, FileChecker checker)
	{
		if(checker.remoteList.isEmpty())
		{
			return false;
		}
		return !checker.missingList.isEmpty() || !checker.outdatedList.isEmpty() || !RemoteInfoReader.instance().getMinecraftVersion().equals(mcVersion) || !RemoteInfoReader.instance().getForgeVersion().equals(forgeVersion);
	}

	public void runMinecraft(String[] args)
	{
		Launch.main(args);
	}

	@Override
	public void onFinish()
	{
		runMinecraft(arguments);
	}
}