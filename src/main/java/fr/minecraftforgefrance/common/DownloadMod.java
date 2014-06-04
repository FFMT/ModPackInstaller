package fr.minecraftforgefrance.common;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
	
    public List<DownloadEntry> getRemoteList()
    {
        List<DownloadEntry> result = Collections.synchronizedList(new ArrayList<DownloadEntry>());
        try
        {
            URL resourceUrl = new URL(Constants.MODS_URL);
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
                    String md5 = element.getElementsByTagName("ETag").item(0).getChildNodes().item(0).getNodeValue();
                    
                    if(size > 0L)
                    {
                        result.add(new DownloadEntry(new URL(Constants.MODS_URL + key), md5, size));
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
        return result;
    }
    
    public static DownloadMod instance()
    {
    	return instance;
    }
}