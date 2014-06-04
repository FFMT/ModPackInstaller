package fr.minecraftforgefrance.common;

import java.net.URL;

public class DownloadEntry
{
	private final URL url;
	private final String md5;
	private final long size;

	public DownloadEntry(URL url, String md5, long size)
	{
		this.url = url;
		this.md5 = md5;
		this.size = size;
	}
	
	public URL getUrl()
	{
		return url;
	}

	public String getMd5()
	{
		return md5;
	}

	public long getSize()
	{
		return size;
	}
}