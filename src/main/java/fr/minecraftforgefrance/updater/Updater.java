package fr.minecraftforgefrance.updater;

import java.io.File;

import javax.swing.JOptionPane;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import fr.minecraftforgefrance.common.RemoteInfoReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launchwrapper.Launch;

public class Updater
{
	public static void main(String[] args)
	{
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

		Launch.main(args);
	}
}