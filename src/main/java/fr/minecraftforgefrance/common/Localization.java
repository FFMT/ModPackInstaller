package fr.minecraftforgefrance.common;

import java.io.File;
import java.util.Locale;

import utybo.minkj.locale.MinkJ;

public class Localization
{
	public static final MinkJ LANG = new MinkJ();

	public static void init()
	{
		try
		{
			long systime = System.currentTimeMillis();
			LANG.loadTranslationsFromFile(Locale.FRENCH, new File(Localization.class.getResource("/langs/FR.lang").toURI()));
			LANG.loadTranslationsFromFile(Locale.ENGLISH, new File(Localization.class.getResource("/langs/EN.lang").toURI()));
			long time = System.currentTimeMillis() - systime;
			System.out.println("Loaded localization files in " + time + " ms!");
			if(time > 1000)
			{
				System.err.println("SEVERE : System appears to be running really slowly, reading through localization files took more than 1 second.");
				System.err.println("SEVERE : The maximum should be 250 ms. If the system is too slow, you will not be able to use the installer at full speed!");
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		try
		{
			LANG.setSelectedLanguage(new Locale(System.getProperty("user.language")));
		}
		catch(NullPointerException ex)
		{
			return;
		}
	}
}