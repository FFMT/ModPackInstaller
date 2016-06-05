package fr.minecraftforgefrance.common;

public class Logger
{
    public final static boolean DEBUG = Boolean.valueOf(System.getProperty("fr.minecraftforgefrance.installer.debug", "false"));

    public static void info(String info)
    {
        if(DEBUG)
        {
            System.out.println(info);
        }
    }
}