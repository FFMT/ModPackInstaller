package fr.minecraftforgefrance.installer;

import static fr.minecraftforgefrance.common.Localization.LANG;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

public class LocalInfoReader
{
    private static final LocalInfoReader INSTANCE = new LocalInfoReader();
    public final JsonRootNode data;

    public LocalInfoReader()
    {
        JdomParser parser = new JdomParser();
        InputStream in = this.getClass().getResourceAsStream("/installer/local_info.json");
        try
        {
            data = parser.parse(new InputStreamReader(in, Charsets.UTF_8));
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotfindlocalinfo"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            throw Throwables.propagate(e);
        }
        catch(InvalidSyntaxException e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.invalidjson"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            throw Throwables.propagate(e);
        }
    }

    public String getRemoteUrl()
    {
        return data.getStringValue("remoteUrl");
    }

    public static LocalInfoReader instance()
    {
        return INSTANCE;
    }
}