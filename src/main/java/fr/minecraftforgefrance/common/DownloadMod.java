package fr.minecraftforgefrance.common;

import java.net.URL;
import java.net.URLEncoder;
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
                        String path = key.substring(0, key.lastIndexOf("/") +1);
                        String link = RemoteInfoReader.instance().getSyncUrl() + path + URLEncoder.encode(name, "UTF-8");
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
}