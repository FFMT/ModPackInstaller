package fr.minecraftforgefrance.common;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.common.base.Charsets;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

public class DownloadMod
{
    private static final DownloadMod instance = new DownloadMod();

    public int time = 0;

    public void getRemoteList(List<FileEntry> list, List<String> dir)
    {
        try
        {
            URL resourceUrl = new URL(RemoteInfoReader.instance().getSyncUrl());
            JdomParser parser = new JdomParser();
            JsonRootNode data = parser.parse(new InputStreamReader(resourceUrl.openStream(), Charsets.UTF_8));

            long start = System.nanoTime();

            for(int i = 0; i < data.getElements().size(); i++)
            {
                JsonNode node = data.getElements().get(i);
                String key = node.getStringValue("name");
                long size = Long.parseLong(node.getStringValue("size"));
                String md5 = node.getStringValue("md5");

                if(size > 0L)
                {
                    String link = RemoteInfoReader.instance().getSyncUrl() + DownloadUtils.escapeURIPathParam(key);
                    list.add(new FileEntry(new URL(link), md5, key, size));
                }
                else if(key.split("/").length == 1)
                {
                    dir.add(key.replace("/", ""));
                }
            }
            long end = System.nanoTime();
            long delta = end - start;
            time += delta;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.networkerror"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static DownloadMod instance()
    {
        return instance;
    }
}