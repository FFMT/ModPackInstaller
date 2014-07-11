package fr.minecraftforgefrance.common;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

import javax.swing.JOptionPane;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

public class RemoteInfoReader
{
	public static RemoteInfoReader instance;
	public final JsonRootNode data;
	public final String remoteUrl;

	public RemoteInfoReader(String url)
	{
		this.remoteUrl = url;
		JdomParser parser = new JdomParser();
		try
		{
			URI uri = new URI(url);
			URLConnection connection = uri.toURL().openConnection();
			InputStream in = connection.getInputStream();
			data = parser.parse(new InputStreamReader(in, Charsets.UTF_8));
		}
		catch(URISyntaxException e)
		{
			JOptionPane.showMessageDialog(null, "Could not read remote info, please check your Internet connection", "Error", JOptionPane.ERROR_MESSAGE);
			throw Throwables.propagate(e);
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "Could not read remote info, please check your Internet connection", "Error", JOptionPane.ERROR_MESSAGE);
			throw Throwables.propagate(e);
		}
	}

	public static RemoteInfoReader instance()
	{
		return instance;
	}

	public String getModPackName()
	{
		return data.getStringValue("install", "name");
	}
	
	public String getMinecraftVersion()
	{
		return data.getStringValue("install", "minecraft");
	}
	
	public String getForgeVersion()
	{
		return data.getStringValue("install", "forge");
	}
	
	public String[] getSyncDir()
	{
		return Iterables.toArray(Splitter.on(',').omitEmptyStrings().split(data.getStringValue("install", "syncDir")), String.class);
	}
	
	public String getSyncUrl()
	{
		return data.getStringValue("install", "syncUrl");
	}

	public String getVersionTarget()
	{
		return data.getStringValue("install", "target");
	}
	
	public JsonNode getProfileInfo()
	{
		return data.getNode("profile");
	}
	
	public String getWelcome()
	{
		return data.getStringValue("install", "welcome");
	}
}