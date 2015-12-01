package fr.minecraftforgefrance.common;

import java.io.File;

public class LibEntry
{
    private final String url;
    private final String name;
    private final File dest;
    private final File packDest;
    private final long size;
    private final boolean isXZ;

    public LibEntry(String url, String name, File dest, File packDest, long size, boolean isXZ)
    {
        this.url = url;
        this.name = name;
        this.dest = dest;
        this.packDest = packDest;
        this.size = size;
        this.isXZ = isXZ;
    }

    public String getName()
    {
        return name;
    }

    public String getUrl()
    {
        return url;
    }

    public File getDest()
    {
        return dest;
    }

    public File getPackDest()
    {
        return packDest;
    }

    public long getSize()
    {
        return size;
    }

    public boolean isXZ()
    {
        return isXZ;
    }
}