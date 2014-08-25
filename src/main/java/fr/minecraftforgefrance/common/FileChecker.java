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

public class FileChecker
{
	public List<FileEntry> remoteList = Collections.synchronizedList(new ArrayList<FileEntry>());
	public List<FileEntry> syncList = new ArrayList<FileEntry>();
	public List<String> checkDir = new ArrayList<String>();
	public List<FileEntry> localList = new ArrayList<FileEntry>();

	public List<FileEntry> missingList;
	public List<FileEntry> outdatedList;

	private File mcDir = EnumOS.getMinecraftDefaultDir();
	private File modPackDir = new File(new File(mcDir, "modpacks"), RemoteInfoReader.instance().getModPackName());

	public FileChecker()
	{
		DownloadMod.instance().getRemoteList(remoteList, checkDir);
		this.getLocalFile();
		this.compare();
	}

	private void getLocalFile()
	{
		if(!mcDir.exists() || !mcDir.isDirectory())
		{
			JOptionPane.showMessageDialog(null, LANG.getTranslation("err.mcdirmissing"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		if(!modPackDir.exists())
		{
			modPackDir.mkdirs();
			return;
		}
		if(!modPackDir.isDirectory())
		{
			modPackDir.delete();
			modPackDir.mkdirs();
		}
		for(String dirName : this.checkDir)
		{
			File dir = new File(modPackDir, dirName);
			if(dir.exists() && dir.isDirectory())
			{
				if(RemoteInfoReader.instance().getSyncDir().contains(dirName))
				{
					this.recursifAdd(localList, syncList, dir, modPackDir.getAbsolutePath(), true);
				}
				else
				{
					this.recursifAdd(localList, syncList, dir, modPackDir.getAbsolutePath(), false);
				}
			}
		}
	}

	private void compare()
	{
		this.missingList = new ArrayList<FileEntry>(remoteList);
		this.missingList.removeAll(localList);

		this.outdatedList = new ArrayList<FileEntry>(syncList);
		this.outdatedList.removeAll(remoteList);

		if(RemoteInfoReader.instance().hasWhiteList())
		{
			for(String md5 : RemoteInfoReader.instance().getWhileList())
			{
				for(FileEntry file : this.outdatedList)
				{
					if(file.getMd5().equals(md5))
					{
						this.outdatedList.remove(file);
						break;
					}
				}
			}
		}
	}

	private void recursifAdd(List<FileEntry> list, List<FileEntry> syncList, File dir, String modpackPath, boolean syncDir)
	{
		for(File file : dir.listFiles())
		{
			if(file.isDirectory())
			{
				recursifAdd(list, syncList, file, modpackPath, syncDir);
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