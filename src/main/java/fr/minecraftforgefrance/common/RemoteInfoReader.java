package fr.minecraftforgefrance.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

public class RemoteInfoReader
{
    public static RemoteInfoReader instance;
    public JsonRootNode data;
    public final String remoteUrl;

    public RemoteInfoReader(String url)
    {
        this.remoteUrl = url;
    }

    public boolean init()
    {
        JdomParser parser = new JdomParser();
        try
        {
            URI uri = new URI(this.remoteUrl);
            URLConnection connection = uri.toURL().openConnection();
            InputStream in = connection.getInputStream();
            data = parser.parse(new InputStreamReader(in, Charsets.UTF_8));
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
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

    public ArrayList<String> getSyncDir()
    {
        return Lists.newArrayList(Splitter.on(',').omitEmptyStrings().split(data.getStringValue("install", "syncDir")));
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

    public boolean hasArgument()
    {
        return data.isStringValue("install", "JVMarg");
    }

    public String getArgument()
    {
        return data.getStringValue("install", "JVMarg");
    }

    public boolean hasWhiteList()
    {
        return data.isStringValue("install", "whiteList");
    }

    public List<String> getWhileList()
    {
        try
        {
            URI uri = new URI(data.getStringValue("install", "whiteList"));
            URLConnection connection = uri.toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            InputSupplier<InputStream> urlSupplier = new URLISSupplier(connection);
            return CharStreams.readLines(CharStreams.newReaderSupplier(urlSupplier, Charsets.UTF_8));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean hasWebSite()
    {
        return data.isStringValue("install", "webSite");
    }

    public String getWebSite()
    {
        return data.getStringValue("install", "webSite");
    }

    public boolean hasCredits()
    {
        return data.isStringValue("install", "credits");
    }

    public String getCredits()
    {
        return data.getStringValue("install", "credits");
    }

    static class URLISSupplier implements InputSupplier<InputStream>
    {
        private final URLConnection connection;

        private URLISSupplier(URLConnection connection)
        {
            this.connection = connection;
        }

        @Override
        public InputStream getInput() throws IOException
        {
            return connection.getInputStream();
        }
    }
}