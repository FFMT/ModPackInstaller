package fr.minecraftforgefrance.common;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class RemoteInfoReader
{
    public static RemoteInfoReader instance;
    public JsonRootNode data;
    public final String remoteUrl;
    private final JdomParser parser = new JdomParser();

    public RemoteInfoReader(String url)
    {
        this.remoteUrl = url;
    }

    public boolean init()
    {
        try
        {
            URI uri = new URI(this.remoteUrl);
            URLConnection connection = uri.toURL().openConnection();
            InputStream in = connection.getInputStream();
            this.data = this.parser.parse(new InputStreamReader(in, Charsets.UTF_8));
            return true;
        }
        catch(InvalidSyntaxException e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.jsoninvalid"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotreadremote"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
        catch(URISyntaxException e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotreadremote"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
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
        return this.data.getStringValue("profile", "id");
    }
    
    public String getModPackDisplayName()
    {
        return this.data.getStringValue("install", "name");
    }

    public String getMinecraftVersion()
    {
        return this.data.getStringValue("install", "minecraft");
    }

    public String getForgeVersion()
    {
        return this.data.getStringValue("install", "forge");
    }

    public ArrayList<String> getSyncDir()
    {
        return Lists.newArrayList(Splitter.on(',').omitEmptyStrings().split(this.data.getStringValue("install", "syncDir")));
    }

    public String getSyncUrl()
    {
        return this.data.getStringValue("install", "syncUrl");
    }

    public String getVersionTarget()
    {
        return this.data.getStringValue("install", "target");
    }

    public JsonNode getProfileInfo()
    {
        return this.data.getNode("profile");
    }

    public String getWelcome()
    {
        return this.data.getStringValue("install", "welcome");
    }

    public boolean hasArgument()
    {
        return this.data.isStringValue("install", "JVMarg");
    }

    public String getArgument()
    {
        return this.data.getStringValue("install", "JVMarg");
    }

    public boolean hasWhiteList()
    {
        return this.data.isStringValue("install", "whiteList");
    }
    
    public JsonRootNode getWhileList()
    {
        try
        {
            URI uri = new URI(this.data.getStringValue("install", "whiteList"));
            URLConnection connection = uri.toURL().openConnection();
            InputStream in = connection.getInputStream();
            return this.parser.parse(new InputStreamReader(in, Charsets.UTF_8));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasWebSite()
    {
        return this.data.isStringValue("install", "webSite");
    }

    public String getWebSite()
    {
        return this.data.getStringValue("install", "webSite");
    }

    public boolean hasCredits()
    {
        return this.data.isStringValue("install", "credits");
    }

    public String getCredits()
    {
        return this.data.getStringValue("install", "credits");
    }
    
    public boolean hasChangeLog()
    {
        return this.data.isStringValue("install", "changeLog");
    }

    public JsonRootNode getChangeLog()
    {
        try
        {
            URI uri = new URI(this.data.getStringValue("install", "changeLog"));
            URLConnection connection = uri.toURL().openConnection();
            InputStream in = connection.getInputStream();
            return this.parser.parse(new InputStreamReader(in, Charsets.UTF_8));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean hasPreset()
    {
        return this.data.isStringValue("install", "preset");
    }

    public JsonRootNode getPreset()
    {
        try
        {
            URI uri = new URI(this.data.getStringValue("install", "preset"));
            URLConnection connection = uri.toURL().openConnection();
            InputStream in = connection.getInputStream();
            return this.parser.parse(new InputStreamReader(in, Charsets.UTF_8));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getPresetUrl()
    {
        return this.data.getStringValue("install", "preset");
    }
}