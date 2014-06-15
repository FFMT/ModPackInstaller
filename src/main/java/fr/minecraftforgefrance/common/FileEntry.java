package fr.minecraftforgefrance.common;

import java.net.URL;

public class FileEntry
{
	private final URL url;
	private final String md5;
	private final String path;
	private final long size;

	public FileEntry(URL url, String md5, String path, long size)
	{
		this.url = url;
		this.md5 = md5;
		this.path = path;
		this.size = size;
	}

	public FileEntry(String md5, String path, long size)
	{
		this.url = null;
		this.md5 = md5;
		this.path = path.replace("\\", "/");
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

	public String getPath()
	{
		return path;
	}

	public long getSize()
	{
		return size;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((md5 == null) ? 0 : md5.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		FileEntry other = (FileEntry)obj;
		if(md5 == null)
		{
			if(other.md5 != null)
			{
				return false;
			}
		}
		else if(!md5.equals(other.md5))
		{
			return false;
		}
		return true;
	}
}