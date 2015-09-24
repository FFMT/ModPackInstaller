package fr.minecraftforgefrance.common;

import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DownloadMod
{
    private static final DownloadMod instance = new DownloadMod();

    public int time = 0;

    public void getRemoteList(List<FileEntry> list, List<String> dir)
    {
        try
        {
            URL resourceUrl = new URL(RemoteInfoReader.instance().getSyncUrl());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(resourceUrl.openStream());
            NodeList nodeLst = doc.getElementsByTagName("Contents");

            long start = System.nanoTime();
            for(int i = 0; i < nodeLst.getLength(); i++)
            {
                Node node = nodeLst.item(i);

                if(node.getNodeType() == 1)
                {
                    Element element = (Element)node;
                    String key = element.getElementsByTagName("Key").item(0).getChildNodes().item(0).getNodeValue();
                    long size = Long.parseLong(element.getElementsByTagName("Size").item(0).getChildNodes().item(0).getNodeValue());
                    String md5 = element.getElementsByTagName("MD5").item(0).getChildNodes().item(0).getNodeValue();

                    if(size > 0L)
                    {
                        String name = key.substring(key.lastIndexOf("/") + 1);
                        String path = key.substring(0, key.lastIndexOf("/") + 1);
                        String link = RemoteInfoReader.instance().getSyncUrl() + path + escapeURIPathParam(name);
                        list.add(new FileEntry(new URL(link), md5, key, size));
                    }
                    else if(key.split("/").length == 1)
                    {
                        dir.add(key.replace("/", ""));
                    }
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
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }
}