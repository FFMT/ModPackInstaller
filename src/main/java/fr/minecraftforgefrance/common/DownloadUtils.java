package fr.minecraftforgefrance.common;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.awt.EventQueue;
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
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;

import org.tukaani.xz.XZInputStream;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

public class DownloadUtils
{
    public static final String LIBRARIES_URL = "https://libraries.minecraft.net/";
    public static final String PACK_NAME = ".pack.xz";

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();
    {
        DECIMAL_FORMAT.setMaximumFractionDigits(2);
    }

    /**
     * Fill the list of all files and the list of directory by reading the remote json file
     */
    public static void readRemoteList(List<FileEntry> files, List<String> dirs)
    {
        try
        {
            URL resourceUrl = new URL(RemoteInfoReader.instance().getSyncUrl());
            URLConnection connection = resourceUrl.openConnection();
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:10.0) Gecko/20100101 Firefox/55.0");

            JdomParser parser = new JdomParser();
            InputStreamReader reader = null;
            if("gzip".equals(connection.getContentEncoding()))
            {
                reader = new InputStreamReader(new GZIPInputStream(connection.getInputStream()), Charsets.UTF_8);
            }
            else
            {
                reader = new InputStreamReader(connection.getInputStream(), Charsets.UTF_8);
            }

            JsonRootNode data = parser.parse(reader);

            for(int i = 0; i < data.getElements().size(); i++)
            {
                JsonNode node = data.getElements().get(i);
                String key = node.getStringValue("name");
                long size = Long.parseLong(node.getStringValue("size"));
                String md5 = node.getStringValue("md5");

                if(size > 0L)
                {
                    String link = RemoteInfoReader.instance().getSyncUrl() + DownloadUtils.escapeURIPathParam(key);
                    files.add(new FileEntry(new URL(link), md5, key, size));
                }
                else if(RemoteInfoReader.instance().enableSubFolder())
                {
                    // add all folders if sub folder is enabled
                    dirs.add(key.substring(0, key.length() - 1));
                }
                else if(key.split("/").length == 1)
                {
                    // only add the folder if it's in modpack root folder
                    dirs.add(key.substring(0, key.length() - 1));
                }
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.networkerror"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean downloadFile(final URL url, final File dest, final InstallFrame installFrame)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                installFrame.fileProgressBar.setIndeterminate(true);
            }
        });

        FileOutputStream fos = null;
        BufferedReader reader = null;

        try
        {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:10.0) Gecko/20100101 Firefox/55.0");

            final int fileLength = connection.getContentLength();

            EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    installFrame.fileProgressBar.setMaximum(fileLength);
                    installFrame.fileProgressBar.setValue(0);
                    installFrame.fileProgressBar.setIndeterminate(false);
                }
            });

            InputStream in = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in));
            fos = new FileOutputStream(dest);

            long downloadStartTime = System.currentTimeMillis();
            int downloadedAmount = 0;
            byte[] buff = new byte[1024];

            int progress = 0;
            while((progress = in.read(buff)) != -1)
            {
                fos.write(buff, 0, progress);
                addProgress(installFrame, progress);
                downloadedAmount += progress;
                long timeLapse = System.currentTimeMillis() - downloadStartTime;
                if(timeLapse >= 250L)
                {
                    final float downloadSpeed = downloadedAmount / (float)timeLapse;

                    downloadedAmount = 0;
                    downloadStartTime += 250L;
                    if(downloadSpeed > 1024F)
                    {
                        changeDownloadSpeed(installFrame, String.format(LANG.getTranslation("misc.speed.mo"), String.valueOf(DECIMAL_FORMAT.format(downloadSpeed / 1024F))));

                    }
                    else
                    {
                        changeDownloadSpeed(installFrame, String.format(LANG.getTranslation("misc.speed.ko"), String.valueOf(DECIMAL_FORMAT.format(downloadSpeed))));
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

    private static void addProgress(final InstallFrame installFrame, final int progress)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                installFrame.fileProgressBar.setValue(installFrame.fileProgressBar.getValue() + progress);
                installFrame.fullProgressBar.setValue(installFrame.fullProgressBar.getValue() + progress);
            }
        });
    }

    private static void changeDownloadSpeed(final InstallFrame installFrame, final String text)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                installFrame.downloadSpeedLabel.setText(text);
            }
        });
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
        Logger.info(String.format("Checking %s internal checksums", libPath.getAbsolutePath()));

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
                System.err.println("Failed checksums.sha1 validation!");
            }
            else
            {
                Logger.info("Successfully validated checksums.sha1");
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
                Logger.info("Jar contents validated successfully");
            }

            return !failed;
        }
        else
        {
            Logger.info("checksums.sha1 was not found, validation failed");
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
            System.err.println("Unpacking failed, missing signature : " + end);
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
        return " %$&+,:;=?@<>#%".indexOf(ch) >= 0;
    }
}