package fr.minecraftforgefrance.common;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import fr.minecraftforgefrance.installer.EnumOS;

public class ProcessInstall
{
	private JFrame frame;
	private JProgressBar fileProgressBar;
	private JProgressBar fullProgressBar;
	private JPanel panel;
	private JLabel downloadSpeedLabel;
	private JLabel currentDownload;

	private List<FileEntry> remoteList = DownloadMod.instance().getRemoteList();
	private List<FileEntry> localList = new ArrayList<FileEntry>();

	private List<FileEntry> missingList;
	private List<FileEntry> outdatedList;

	private File mcDir = EnumOS.getMinecraftDefaultDir();
	private File modPackDir = new File(new File(mcDir, "modpacks"), RemoteInfoReader.instance().getModPackName());

	public ProcessInstall()
	{
		this.frame = new JFrame("Download mods and config ...");
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.frame.setVisible(true);
		this.frame.setResizable(false);
		this.frame.setSize(500, 100);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width / 2) - (frame.getSize().width / 2);
		int y = (dim.height / 2) - (frame.getSize().height / 2);
		this.frame.setLocation(x, y);

		fileProgressBar = new JProgressBar(0, 10);
		fileProgressBar.setValue(0);
		fileProgressBar.setStringPainted(true);

		fullProgressBar = new JProgressBar(0, 10);
		fullProgressBar.setValue(0);
		fullProgressBar.setStringPainted(true);

		currentDownload = new JLabel(" ");
		downloadSpeedLabel = new JLabel(" ");
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(currentDownload);
		panel.add(fileProgressBar);
		panel.add(fullProgressBar);
		panel.add(downloadSpeedLabel);

		this.frame.setContentPane(panel);

		this.getLocalFile();
		this.compare();
		fullProgressBar.setMaximum(this.getTotalDownloadSize());
		System.out.println(fullProgressBar.getMaximum());
		if(!remoteList.isEmpty())
		{
			this.deleteDeprecated();
		}
		else
		{
			this.frame.dispose();
			JOptionPane.showMessageDialog(null, "Network error, check you connection", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.downloadFiles();
	}

	private int getTotalDownloadSize()
	{
		int size = 0;
		for(FileEntry entry : missingList)
		{
			size += entry.getSize();
		}
		return size;
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
		for(String dirName : RemoteInfoReader.instance().getSyncDir())
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
				list.add(new FileEntry(getMd5(file), file.getAbsolutePath().replace(modpackPath + File.separator, ""), file.length()));
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
				frame.dispose();
				JOptionPane.showMessageDialog(null, "Couldn't delete file : " + f.getPath(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void downloadFiles()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				for(FileEntry entry : missingList)
				{
					File f = new File(modPackDir, entry.getPath());
					if(f.getParentFile() != null && !f.getParentFile().isDirectory())
					{
						f.getParentFile().mkdirs();
					}
					currentDownload.setText(entry.getPath());
					System.out.println("Download file " + entry.getUrl() + " to " + f.getPath() + "(md5 is : " + entry.getMd5() + ")");
					downloadFile(entry.getUrl(), f, fileProgressBar, fullProgressBar);
				}
				finish();
				downloadLib();
			}

			public void downloadFile(final URL url, final File dest, final JProgressBar bar, final JProgressBar fullBar)
			{
				bar.setIndeterminate(true);

				FileOutputStream fos = null;
				BufferedReader reader = null;

				try
				{
					URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
					URL url2 = uri.toURL();
					URLConnection connection = url2.openConnection();

					int fileLength = connection.getContentLength();
					if(fileLength == -1)
					{
						throw new IOException("Fichier non valide.");
					}
					else
					{
						bar.setMaximum(fileLength);
					}

					InputStream in = connection.getInputStream();
					reader = new BufferedReader(new InputStreamReader(in));
					fos = new FileOutputStream(dest);

					long downloadStartTime = System.currentTimeMillis();
					int downloadedAmount = 0;
					byte[] buff = new byte[1024];

					bar.setValue(0);
					bar.setIndeterminate(false);

					int n;
					while((n = in.read(buff)) != -1)
					{
						fos.write(buff, 0, n);
						bar.setValue(bar.getValue() + n);
						fullBar.setValue(fullBar.getValue() + n);
						downloadedAmount += n;
						long timeLapse = System.currentTimeMillis() - downloadStartTime;
						if(timeLapse >= 1000L)
						{
							float downloadSpeed = downloadedAmount / (float)timeLapse;
							downloadedAmount = 0;
							downloadStartTime += 1000L;
							DecimalFormat df = new DecimalFormat();
							df.setMaximumFractionDigits(2);
							if(downloadSpeed > 1000.0F)
							{
								downloadSpeedLabel.setText("Speed : " + String.valueOf(df.format(downloadSpeed / 1024F)) + " mo/s");
							}
							else
							{
								downloadSpeedLabel.setText("Speed : " + String.valueOf(df.format(downloadSpeed)) + " ko/s");
							}
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					frame.dispose();
					JOptionPane.showMessageDialog(null, "Couldn't download : " + url.toString(), "Error", JOptionPane.ERROR_MESSAGE);
					interrupt();
				}
				finally
				{
					try
					{
						fos.flush();
						fos.close();
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}

					try
					{
						reader.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public void finish()
	{
		frame.dispose();
		JOptionPane.showMessageDialog(null, "Installation is finish !", "Success", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void downloadLib()
	{
		frame.setName("Download libraries ...");
		
		/*
        File librariesDir = new File(mcDir, "libraries");
        //List<JsonNode> libraries = VersionInfo.getVersionInfo().getArrayNode("libraries");
        int progress = 2; 
        List<String> grabbed = Lists.newArrayList();
        List<String> bad = Lists.newArrayList();
        //progress = LibraryDownload.downloadInstalledLibraries(librariesDir, libraries, progress, grabbed, bad);
         */
	}
}