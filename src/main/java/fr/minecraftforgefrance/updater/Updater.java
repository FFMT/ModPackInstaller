package fr.minecraftforgefrance.updater;

import java.io.File;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class Updater implements ITweaker
{
	private ITweaker fmlTweaker;

	public Updater()
	{
		FrameThread thread = new FrameThread();
		thread.start();

		System.out.println("RUN UPDATER FRAME");
		try
		{
			Class<?> clazz = Class.forName("cpw.mods.fml.common.launcher.FMLTweaker");
			fmlTweaker = (ITweaker)clazz.newInstance();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			System.err.println("FML not found");
			System.exit(-1);
		}
		catch(InstantiationException e)
		{
			e.printStackTrace();
			System.err.println("FML not found");
			System.exit(-1);
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
			System.err.println("FML not found");
			System.exit(-1);
		}
	}

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile)
	{
		fmlTweaker.acceptOptions(args, gameDir, assetsDir, profile);
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader)
	{
		fmlTweaker.injectIntoClassLoader(classLoader);
	}

	@Override
	public String getLaunchTarget()
	{
		return fmlTweaker.getLaunchTarget();
	}

	@Override
	public String[] getLaunchArguments()
	{
		return fmlTweaker.getLaunchArguments();
	}
}