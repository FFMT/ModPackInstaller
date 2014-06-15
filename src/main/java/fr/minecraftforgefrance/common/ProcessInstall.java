package fr.minecraftforgefrance.common;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import fr.minecraftforgefrance.installer.EnumOS;

public class ProcessInstall
{
	private JFrame frame;
	private JProgressBar progressBar;
	private JPanel panel;

	private List<FileEntry> remoteList = DownloadMod.instance().getRemoteList();
	private List<FileEntry> localList = new ArrayList<FileEntry>();

	private List<FileEntry> missingList;
	private List<FileEntry> outdatedList;

	private File mcDir = EnumOS.getMinecraftDefaultDir();
	private File modPackDir = new File(new File(mcDir, "modpacks"), Constants.MODSPACK_NAME);

	public ProcessInstall()
	{
		this.frame = new JFrame("Install progress ...");
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.frame.setVisible(true);
		this.frame.setResizable(false);
		this.frame.setSize(200, 100);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width / 2) - (frame.getSize().width / 2);
		int y = (dim.height / 2) - (frame.getSize().height / 2);
		this.frame.setLocation(x, y);
		progressBar = new JProgressBar(0, 10);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(progressBar);
		this.frame.setContentPane(panel);
		this.frame.pack();

		this.getLocalFile();
		this.compare();
		this.deleteDeprecated();
		this.downloadFiles();
	}

	private void getLocalFile()
	{
		if(!mcDir.exists() || !mcDir.isDirectory())
		{
			frame.dispose();
			JOptionPane.showMessageDialog(null, "Minecraft dir is missing, please run the minecraft launcher", "Error", JOptionPane.ERROR_MESSAGE);
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
		for(String dirName : Constants.SYNC_DIR)
		{
			File dir = new File(modPackDir, dirName);
			if(dir.exists() && dir.isDirectory())
			{
				this.recursifAdd(localList, dir, modPackDir.getAbsolutePath());
			}
		}
	}

	private void recursifAdd(List<FileEntry> list, File dir, String modpackPath)
	{
		for(File file : dir.listFiles())
		{
			if(file.isDirectory())
			{
				recursifAdd(list, file, modpackPath);
			}
			else
			{
				list.add(new FileEntry(getMd5(file), file.getAbsolutePath().replace(modpackPath + File.separator, "")));
			}
		}
	}

	private void compare()
	{
		this.missingList = new ArrayList<FileEntry>(remoteList);
		this.missingList.removeAll(localList);

		this.outdatedList = new ArrayList<FileEntry>(localList);
		this.outdatedList.removeAll(remoteList);
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
				read = stream.read(buffer);
		}
		catch(final Exception ignored)
		{
			return null;
		}
		finally
		{
			if(stream != null)
				try
				{
					stream.close();
				}
				catch(final IOException localIOException)
				{

				}
		}

		return String.format("%1$032x", new Object[] {new BigInteger(1, stream.getMessageDigest().digest())});
	}

	public void deleteDeprecated()
	{
		for(FileEntry entry : outdatedList)
		{
			File f = new File(modPackDir, entry.getPath());
			if(f.delete())
			{
				System.out.println(f.getPath() + " was removed. Its md5 was : " + entry.getMd5());
			}
			else
			{
				// TODO warn user
			}
		}
	}

	public void downloadFiles()
	{
		for(FileEntry entry : missingList)
		{
			File f = new File(modPackDir, entry.getPath());
			if(f.getParentFile() != null && !f.getParentFile().isDirectory())
			{
				f.getParentFile().mkdirs();
			}
			this.downloadFile(entry.getUrl(), f);
			System.out.println("Download file " + entry.getUrl() + " to " + f.getPath() + "(md5 is : " + entry.getMd5() + ")");
		}
	}

	public void downloadFile(URL url, File dest)
	{
		progressBar.setValue(0);
		try
		{
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			url = uri.toURL();
			InputStream input = null;
			FileOutputStream writeFile = null;
			try
			{
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				int fileLength = connection.getContentLength();
				progressBar.setMaximum(fileLength);
				if(fileLength == -1)
				{
					System.out.println("Invalide URL or file.");
					return;
				}
				System.out.println(fileLength);
				input = connection.getInputStream();
				writeFile = new FileOutputStream(dest);
				byte[] buffer = new byte[2048];
				int read;

				while((read = input.read(buffer)) > 0)
				{
					writeFile.write(buffer, 0, read);
					progress(read);
				}
				writeFile.flush();
			}
			catch(IOException e)
			{
				System.out.println("Couldn't download " + url);
				e.printStackTrace();
			}
			finally
			{
				try
				{
					writeFile.close();
					input.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(URISyntaxException ex)
		{
			ex.printStackTrace();
		}
		catch(MalformedURLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void progress(int i)
	{
		this.progressBar.setValue(this.progressBar.getValue() + i);
		System.out.println(progressBar.getValue());
	}
}