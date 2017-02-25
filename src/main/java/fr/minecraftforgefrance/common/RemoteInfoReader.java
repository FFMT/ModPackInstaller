package fr.minecraftforgefrance.common;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

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
            InputStreamReader reader = getRemoteStream(this.remoteUrl);
            this.data = this.parser.parse(reader);
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

    /**
     * An option to manage manually sub-folders
     * By default the check of files is recursive, if you put "mods" in the list all folder inside
     * "mods" will also be check. If sub-folder is enabled, is it will not be the case and you
     * need to add "mods/subfolder" in the syncDir to make the installer checking it
     * @return true if sub-folder is enabled
     */
    public boolean enableSubFolder()
    {
        return this.data.isBooleanValue("install", "subfolder") && this.data.getBooleanValue("install", "subfolder");
    }

    public ArrayList<String> getSyncDir()
    {
        return Lists.newArrayList(Splitter.on(',').trimResults().omitEmptyStrings().split(this.data.getStringValue("install", "syncDir")));
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
            InputStreamReader reader = getRemoteStream(this.data.getStringValue("install", "whiteList"));
            return this.parser.parse(reader);
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
            InputStreamReader reader = getRemoteStream(this.data.getStringValue("install", "changeLog"));
            return this.parser.parse(reader);
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
            InputStreamReader reader = getRemoteStream(this.data.getStringValue("install", "preset"));
            return this.parser.parse(reader);
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
    
    private InputStreamReader getRemoteStream(String str) throws MalformedURLException, IOException, URISyntaxException
    {
        URI uri = new URI(str);
        URLConnection connection = uri.toURL().openConnection();
        connection.setRequestProperty("Accept-Encoding", "gzip");
        InputStreamReader reader = null;
        if("gzip".equals(connection.getContentEncoding()))
        {
            reader = new InputStreamReader(new GZIPInputStream(connection.getInputStream()), Charsets.UTF_8);
        }
        else
        {
            reader = new InputStreamReader(connection.getInputStream(), Charsets.UTF_8);
        }
        return reader;
    }
}