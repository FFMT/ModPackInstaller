package fr.minecraftforgefrance.common;

import java.net.URL;

public class FileEntry
{
	private final URL url;
	private final String md5;
	private final String path;

	public FileEntry(URL url, String md5, String path)
	{
		this.url = url;
		this.md5 = md5;
		this.path = path;
	}
	
	public FileEntry(String md5, String path)
	{
		this.url = null;
		this.md5 = md5;
		this.path = path.replace("\\", "/");
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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((md5 == null) ? 0 : md5.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		FileEntry other = (FileEntry)obj;
		if(md5 == null)
		{
			if(other.md5 != null)
				return false;
		}
		else if(!md5.equals(other.md5))
			return false;
		if(path == null)
		{
			if(other.path != null)
				return false;
		}
		else if(!path.equals(other.path))
			return false;
		return true;
	}
}