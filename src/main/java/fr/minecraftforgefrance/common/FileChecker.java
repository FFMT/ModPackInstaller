package fr.minecraftforgefrance.common;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import argo.jdom.JsonField;

public class FileChecker
{
    public List<FileEntry> remoteList = Collections.synchronizedList(new ArrayList<FileEntry>());
    public List<FileEntry> syncList = new ArrayList<FileEntry>();
    public List<String> checkDir = new ArrayList<String>();
    public List<FileEntry> localList = new ArrayList<FileEntry>();

    public List<FileEntry> missingList;
    public List<FileEntry> outdatedList;

    private final File mcDir;
    private final File modPackDir;

    public FileChecker(File mcDir)
    {
        this.mcDir = mcDir;
        this.modPackDir = new File(new File(mcDir, "modpacks"), RemoteInfoReader.instance().getModPackName());
        DownloadUtils.readRemoteList(this.remoteList, this.checkDir);
        this.getLocalFile();
        this.compare();
    }

    private void getLocalFile()
    {
        if(!this.mcDir.exists() || !this.mcDir.isDirectory())
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.mcdirmissing"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(!this.modPackDir.isDirectory())
        {
            this.modPackDir.delete();
        }

        if(!this.modPackDir.exists())
        {
            this.modPackDir.mkdirs();
            return; // no need to check files as the folder is empty
        }

        for(String dirName : this.checkDir)
        {
            File dir = new File(this.modPackDir, dirName);
            if(dir.exists() && dir.isDirectory())
            {
                if(RemoteInfoReader.instance().getSyncDir().contains(dirName))
                {
                    this.addFiles(this.localList, this.syncList, dir, this.modPackDir.getAbsolutePath(), true);
                }
                else
                {
                    this.addFiles(this.localList, this.syncList, dir, this.modPackDir.getAbsolutePath(), false);
                }
            }
        }
    }

    private void compare()
    {
        this.missingList = new ArrayList<FileEntry>(this.remoteList);
        this.missingList.removeAll(this.localList);

        this.outdatedList = new ArrayList<FileEntry>(this.syncList);
        this.outdatedList.removeAll(this.remoteList);

        if(RemoteInfoReader.instance().hasWhiteList() && !this.outdatedList.isEmpty())
        {
            for(JsonField field : RemoteInfoReader.instance().getWhileList().getFieldList())
            {
                for(FileEntry file : this.outdatedList)
                {
                    if(file.getMd5().equals(field.getValue().getText()))
                    {
                        this.outdatedList.remove(file);
                        break;
                    }
                }
            }
        }
    }

    private void addFiles(List<FileEntry> list, List<FileEntry> syncList, File dir, String modpackPath, boolean syncDir)
    {
        for(File file : dir.listFiles())
        {
            if(file.isDirectory())
            {
                if(!RemoteInfoReader.instance().enableSubFolder())
                {
                    // only use recursive mode if sub folder option is disabled
                    addFiles(list, syncList, file, modpackPath, syncDir);
                }
            }
            else
            {
                if(syncDir)
                {
                    syncList.add(new FileEntry(getMd5(file), file.getAbsolutePath().replace(modpackPath + File.separator, ""), file.length()));
                }
                list.add(new FileEntry(getMd5(file), file.getAbsolutePath().replace(modpackPath + File.separator, ""), file.length()));
            }
        }
    }

    public String getMd5(final File file)
    {
        DigestInputStream stream = null;
        try
        {
            stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("MD5"));
            final byte[] buffer = new byte[65536];

            int read = stream.read(buffer);
            while(read >= 1)
            {
                read = stream.read(buffer);
            }
        }
        catch(final Exception ignored)
        {
            return null;
        }
        finally
        {
            if(stream != null)
            {
                try
                {
                    stream.close();
                }
                catch(final IOException localIOException)
                {

                }
            }
        }
        return String.format("%1$032x", new Object[] {new BigInteger(1, stream.getMessageDigest().digest())});
    }
}