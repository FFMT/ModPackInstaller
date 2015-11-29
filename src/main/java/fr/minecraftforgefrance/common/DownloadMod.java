package fr.minecraftforgefrance.common;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

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
                    String link = RemoteInfoReader.instance().getSyncUrl() + escapeURIPathParam(key);
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
        }
    }

    public static DownloadMod instance()
    {
        return instance;
    }

    public static String escapeURIPathParam(String input)
    {
        StringBuilder resultStr = new StringBuilder();
        for(char ch : input.toCharArray())
        {
            if(isUnsafe(ch))
            {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            }
            else
            {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch)
    {
        return (char)(ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch)
    {
        if(ch > 128 || ch < 0)
            return true;
        return " %$&+,:;=?@<>#%".indexOf(ch) >= 0;
    }
}