package fr.minecraftforgefrance.common;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

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
		this.frame = new JFrame();
		this.frame.setTitle("Download mods and config ...");
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

		this.frame.setVisible(true);
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
					if(!DownloadUtils.downloadFile(entry.getUrl(), f, fileProgressBar, fullProgressBar, downloadSpeedLabel))
					{
						frame.dispose();
						interrupt();
						JOptionPane.showMessageDialog(null, "Couldn't download : " + entry.getUrl().toString(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				downloadLib();
			}
		}.start();
	}

	public void downloadLib()
	{
		this.frame.setTitle("Download and extract libraries ...");
		this.fullProgressBar.setValue(0);

		File librariesDir = new File(mcDir, "libraries");
		List<JsonNode> libraries = RemoteInfoReader.instance().getProfileInfo().getArrayNode("libraries");

		List<LibEntry> libEntryList = new ArrayList<LibEntry>();
		int max = 0;
		for(JsonNode library : libraries)
		{
			List<String> checksums = null;
			String libName = library.getStringValue("name");
			if(library.isBooleanValue("required") && library.getBooleanValue("required"))
			{
				if(library.isArrayNode("checksums"))
				{
					checksums = Lists.newArrayList(Lists.transform(library.getArrayNode("checksums"), new Function<JsonNode, String>()
					{
						public String apply(JsonNode node)
						{
							return node.getText();
						}
					}));
				}

				System.out.println(String.format("Considering library %s", libName));
				String[] nameparts = Iterables.toArray(Splitter.on(':').split(libName), String.class);
				nameparts[0] = nameparts[0].replace('.', '/');
				String jarName = nameparts[1] + '-' + nameparts[2] + ".jar";
				String pathName = nameparts[0] + '/' + nameparts[1] + '/' + nameparts[2] + '/' + jarName;
				File libPath = new File(librariesDir, pathName.replace('/', File.separatorChar));
				String libURL = DownloadUtils.LIBRARIES_URL;
				if(library.isStringValue("url"))
				{
					libURL = library.getStringValue("url") + "/";
				}
				if(libPath.exists() && DownloadUtils.checksumValid(libPath, checksums))
				{
					continue;
				}

				libPath.getParentFile().mkdirs();
				libURL += pathName;
				File pack = null;
				boolean xz = false;
				if(library.isBooleanValue("xz") && library.getBooleanValue("xz"))
				{
					xz = true;
					pack = new File(libPath.getParentFile(), libPath.getName() + DownloadUtils.PACK_NAME);
					libURL += DownloadUtils.PACK_NAME;
				}
				if(library.isStringValue("download"))
				{
					libURL = library.getStringValue("download");
				}
				try
				{
					URL url = new URL(libURL);
					URLConnection connection = url.openConnection();
					int fileLength = connection.getContentLength();
					max += fileLength;
					libEntryList.add(new LibEntry(libURL, libName, libPath, pack, fileLength, xz));
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		this.fullProgressBar.setMaximum(max);

		for(LibEntry entry : libEntryList)
		{
			currentDownload.setText(String.format("Downloading library : %s", entry.getName()));
			try
			{
				if(entry.isXZ())
				{
					if(!DownloadUtils.downloadFile(new URL(entry.getUrl()), entry.getPackDest(), fileProgressBar, fullProgressBar, downloadSpeedLabel))
					{
						frame.dispose();
						JOptionPane.showMessageDialog(null, "Couldn't download : " + entry.getUrl().toString() + DownloadUtils.PACK_NAME, "Error", JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						try
						{
							currentDownload.setText(String.format("Unpacking packed file %s", entry.getPackDest().toString()));
							DownloadUtils.unpackLibrary(entry.getDest(), Files.toByteArray(entry.getPackDest()));
							currentDownload.setText(String.format("Successfully unpacked packed file %s", entry.getPackDest().toString()));
							entry.getPackDest().delete();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				else if(!DownloadUtils.downloadFile(new URL(entry.getUrl()), entry.getDest(), fileProgressBar, fullProgressBar, downloadSpeedLabel))
				{
					frame.dispose();
					JOptionPane.showMessageDialog(null, "Couldn't download : " + entry.getUrl().toString() + DownloadUtils.PACK_NAME, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			catch(HeadlessException e)
			{
				e.printStackTrace();
			}
			catch(MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
		this.createProfile();
	}

	private void createProfile()
	{
		String mcVersion = RemoteInfoReader.instance().getMinecraftVersion();
		String modpackName = RemoteInfoReader.instance().getModPackName();
		File launcherProfiles = new File(mcDir, "launcher_profiles.json");
		if(!launcherProfiles.exists())
		{
			JOptionPane.showMessageDialog(null, "Minecraft launcher profile no found, you need to run the launcher first !", "Error", JOptionPane.ERROR_MESSAGE);
			this.frame.dispose();
		}
		File versionRootDir = new File(mcDir, "versions");
		File modpackVersionDir = new File(versionRootDir, modpackName);
		if(!modpackVersionDir.exists())
		{
			modpackVersionDir.mkdirs();
		}
		File modpackJar = new File(modpackVersionDir, modpackName + ".jar");
		File modpackJson = new File(modpackVersionDir, modpackName + ".json");
		File minecraftJar = new File(new File(versionRootDir, mcVersion), mcVersion + ".jar");

		if(minecraftJar.exists())
		{
			try
			{
				Files.copy(minecraftJar, modpackJar);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				DownloadUtils.downloadFile(new URL("https://s3.amazonaws.com/Minecraft.Download/versions/" + mcVersion + "/" + mcVersion + ".jar"), modpackJar, this.fileProgressBar, this.fullProgressBar, this.downloadSpeedLabel);
			}
			catch(MalformedURLException e)
			{
				e.printStackTrace();
			}
		}

		JsonRootNode versionJson = JsonNodeFactories.object(RemoteInfoReader.instance().getProfileInfo().getFields());
		try
		{
			BufferedWriter newWriter = Files.newWriter(modpackJson, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(versionJson, newWriter);
			newWriter.close();
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "There was a problem writing the launcher version data,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
		}

		JdomParser parser = new JdomParser();
		JsonRootNode jsonProfileData;

		try
		{
			jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
		}
		catch(InvalidSyntaxException e)
		{
			JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
			throw Throwables.propagate(e);
		}
		catch(Exception e)
		{
			throw Throwables.propagate(e);
		}

		JsonField[] fields = new JsonField[] {JsonNodeFactories.field("name", JsonNodeFactories.string(modpackName)), JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(modpackName)),};

		HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
		HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
		profileCopy.put(JsonNodeFactories.string(modpackName), JsonNodeFactories.object(fields));
		JsonRootNode profileJsonCopy = JsonNodeFactories.object(profileCopy);

		rootCopy.put(JsonNodeFactories.string("profiles"), profileJsonCopy);

		jsonProfileData = JsonNodeFactories.object(rootCopy);

		try
		{
			BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData, newWriter);
			newWriter.close();
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
		}
		this.writeLauncherInfo();
	}

	public void writeLauncherInfo()
	{
		File info = new File(this.modPackDir, RemoteInfoReader.instance().getModPackName() + ".json");
		if(!info.exists())
		{
			try
			{
				info.createNewFile();
			}
			catch(IOException e)
			{
				throw Throwables.propagate(e);
			}
		}

		JsonRootNode json = JsonNodeFactories.object(JsonNodeFactories.field("mc", JsonNodeFactories.string(RemoteInfoReader.instance().getMinecraftVersion())), JsonNodeFactories.field("forge", JsonNodeFactories.string(RemoteInfoReader.instance().getForgeVersion())), JsonNodeFactories.field("remote", JsonNodeFactories.string(RemoteInfoReader.instance().remoteUrl)));
		try
		{
			BufferedWriter writer = Files.newWriter(info, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(json, writer);
			writer.close();
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "There was a problem writing the launcher version data,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
		}
		this.finish();
	}

	public void finish()
	{
		frame.dispose();
		JOptionPane.showMessageDialog(null, "Installation is finish !", "Success", JOptionPane.INFORMATION_MESSAGE);
	}
}