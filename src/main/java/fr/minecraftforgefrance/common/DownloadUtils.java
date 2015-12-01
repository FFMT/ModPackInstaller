package fr.minecraftforgefrance.common;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.tukaani.xz.XZInputStream;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class DownloadUtils
{
	public static final String LIBRARIES_URL = "https://libraries.minecraft.net/";
	public static final String PACK_NAME = ".pack.xz";

	public static boolean downloadFile(final URL url, final File dest, final JProgressBar bar, final JProgressBar fullBar, JLabel speedLabel)
	{
		bar.setIndeterminate(true);

		FileOutputStream fos = null;
		BufferedReader reader = null;

		try
		{
			URLConnection connection = url.openConnection();

			int fileLength = connection.getContentLength();
			bar.setMaximum(fileLength);

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
						speedLabel.setText(LANG.getTranslation("misc.speed") + " : " + String.valueOf(df.format(downloadSpeed / 1024F)) + " mo/s");
					}
					else
					{
						speedLabel.setText(LANG.getTranslation("misc.speed") + " : " + String.valueOf(df.format(downloadSpeed)) + " ko/s");
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println(String.format(LANG.getTranslation("err.invalidurl"), url.toString()));
			return false;
		}
		finally
		{
			try
			{
				if(fos != null)
				{
					fos.flush();
					fos.close();
				}
				if(reader != null)
				{
					reader.close();
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static boolean checksumValid(File libPath, List<String> checksums)
	{
		try
		{
			byte[] fileData = Files.toByteArray(libPath);
			boolean valid = checksums == null || checksums.isEmpty() || checksums.contains(Hashing.sha1().hashBytes(fileData).toString());
			if(!valid && libPath.getName().endsWith(".jar"))
			{
				valid = validateJar(libPath, fileData, checksums);
			}
			return valid;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static boolean validateJar(File libPath, byte[] data, List<String> checksums) throws IOException
	{
		System.out.println(LANG.getTranslation("proc.checkingchecksum").replace("$p", libPath.getAbsolutePath()));

		HashMap<String, String> files = new HashMap<String, String>();
		String[] hashes = null;
		JarInputStream jar = new JarInputStream(new ByteArrayInputStream(data));
		JarEntry entry = jar.getNextJarEntry();
		while(entry != null)
		{
			byte[] eData = readFully(jar);

			if(entry.getName().equals("checksums.sha1"))
			{
				hashes = new String(eData, Charset.forName("UTF-8")).split("\n");
			}

			if(!entry.isDirectory())
			{
				files.put(entry.getName(), Hashing.sha1().hashBytes(eData).toString());
			}
			entry = jar.getNextJarEntry();
		}
		jar.close();

		if(hashes != null)
		{
			boolean failed = !checksums.contains(files.get("checksums.sha1"));
			if(failed)
			{
				System.err.println(LANG.getTranslation("err.checksumvalidation"));
			}
			else
			{
				System.out.println(LANG.getTranslation("file.checksumvalidation.success"));
				for(String hash : hashes)
				{
					if(hash.trim().equals("") || !hash.contains(" "))
						continue;
					String[] e = hash.split(" ");
					String validChecksum = e[0];
					String target = e[1];
					String checksum = files.get(target);

					if(!files.containsKey(target) || checksum == null)
					{
						System.err.println("    " + target + " : " + LANG.getTranslation("misc.missing").toLowerCase());
						failed = true;
					}
					else if(!checksum.equals(validChecksum))
					{
						System.err.println("    " + target + " : " + LANG.getTranslation("misc.failed").toLowerCase() + " (" + checksum + ", " + validChecksum + ")");
						failed = true;
					}
				}
			}

			if(!failed)
			{
				System.out.println(LANG.getTranslation("jar.validated.success"));
			}

			return !failed;
		}
		else
		{
			System.out.println(LANG.getTranslation("err.checksumnotfound"));
			return false; // Missing checksums
		}
	}

	public static byte[] readFully(InputStream stream) throws IOException
	{
		byte[] data = new byte[4096];
		ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
		int len;
		do
		{
			len = stream.read(data);
			if(len > 0)
			{
				entryBuffer.write(data, 0, len);
			}
		}
		while(len != -1);

		return entryBuffer.toByteArray();
	}

	public static void unpackLibrary(File output, byte[] data) throws IOException
	{
		if(output.exists())
		{
			output.delete();
		}

		byte[] decompressed = readFully(new XZInputStream(new ByteArrayInputStream(data)));

		// Snag the checksum signature
		String end = new String(decompressed, decompressed.length - 4, 4);
		if(!end.equals("SIGN"))
		{
			System.err.println(LANG.getTranslation("err.missingsignature") + " : " + end);
			return;
		}

		int x = decompressed.length;
		int len = ((decompressed[x - 8] & 0xFF)) | ((decompressed[x - 7] & 0xFF) << 8) | ((decompressed[x - 6] & 0xFF) << 16) | ((decompressed[x - 5] & 0xFF) << 24);
		byte[] checksums = Arrays.copyOfRange(decompressed, decompressed.length - len - 8, decompressed.length - 8);

		FileOutputStream jarBytes = new FileOutputStream(output);
		JarOutputStream jos = new JarOutputStream(jarBytes);

		Pack200.newUnpacker().unpack(new ByteArrayInputStream(decompressed), jos);

		jos.putNextEntry(new JarEntry("checksums.sha1"));
		jos.write(checksums);
		jos.closeEntry();

		jos.close();
		jarBytes.close();
	}
}