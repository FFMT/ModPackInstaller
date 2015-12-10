package fr.minecraftforgefrance.common;

import java.util.Locale;

import utybo.minkj.locale.MinkJ;

public class Localization
{
    public static final MinkJ LANG = new MinkJ();

    public static void init()
    {
        try
        {
            LANG.loadTranslationsFromFile(Locale.FRENCH, Localization.class.getResourceAsStream("/langs/FR.lang"));
            LANG.loadTranslationsFromFile(Locale.ENGLISH, Localization.class.getResourceAsStream("/langs/EN.lang"));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}