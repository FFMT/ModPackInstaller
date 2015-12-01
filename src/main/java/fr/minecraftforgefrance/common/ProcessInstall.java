package fr.minecraftforgefrance.common;

import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static fr.minecraftforgefrance.common.Localization.LANG;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import argo.format.JsonFormatter;
import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;

public class ProcessInstall
{
    private JFrame frame;
    private JProgressBar fileProgressBar;
    private JProgressBar fullProgressBar;
    private JPanel panel;
    private JLabel downloadSpeedLabel;
    private JLabel currentDownload;

    private final File mcDir;
    private final File modPackDir;

    private final FileChecker fileChecker;
    private final IInstallRunner runner;
    private final boolean update;

    private static final JsonFormatter JSON_FORMATTER = new PrettyJsonFormatter();

    public ProcessInstall(FileChecker file, IInstallRunner runner, boolean update, File mcDir)
    {
        this.fileChecker = file;
        this.runner = runner;
        this.update = update;
        this.mcDir = mcDir;
        this.modPackDir = new File(new File(mcDir, "modpacks"), RemoteInfoReader.instance().getModPackName());
    }

    public void run()
    {
        this.frame = new JFrame();
        this.frame.setTitle(LANG.getTranslation("proc.downloadingmods"));
        this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.frame.setResizable(false);
        this.frame.setSize(500, 100);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width / 2) - (frame.getSize().width / 2);
        int y = (dim.height / 2) - (frame.getSize().height / 2);
        this.frame.setLocation(x, y);

        this.fileProgressBar = new JProgressBar(0, 10);
        this.fileProgressBar.setValue(0);
        this.fileProgressBar.setStringPainted(true);

        this.fullProgressBar = new JProgressBar(0, 10);
        this.fullProgressBar.setValue(0);
        this.fullProgressBar.setStringPainted(true);

        this.currentDownload = new JLabel(" ");
        this.downloadSpeedLabel = new JLabel(" ");

        this.panel = new JPanel();
        this.panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        this.panel.add(currentDownload);
        this.panel.add(fileProgressBar);
        this.panel.add(fullProgressBar);
        this.panel.add(downloadSpeedLabel);

        if(RemoteInfoReader.instance().hasChangeLog())
        {
            JTextArea area = new JTextArea();
            area.setBounds(4, 2, 492, 150);
            this.getChangeLog(area);
            if(!area.getText().isEmpty())
            {
                this.frame.setSize(500, 250);
                this.panel.add(area);
            }
        }

        this.frame.setContentPane(panel);

        this.fullProgressBar.setMaximum(this.getTotalDownloadSize());

        this.frame.setVisible(true);
        if(!this.fileChecker.remoteList.isEmpty())
        {
            this.deleteDeprecated();
        }
        else
        {
            this.frame.dispose();
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.networkerror"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.downloadFiles();
    }

    private void getChangeLog(JTextArea area)
    {
        String currentVersion = null;
        File modpackInfo = new File(modPackDir, RemoteInfoReader.instance().getModPackName() + ".json");
        if(modpackInfo.exists())
        {
            JdomParser jsonParser = new JdomParser();
            try
            {
                JsonRootNode jsonProfileData = jsonParser.parse(Files.newReader(modpackInfo, Charsets.UTF_8));
                currentVersion = jsonProfileData.getStringValue("currentVersion");
            }
            catch(Exception e)
            {

            }
        }
        for(JsonField field : RemoteInfoReader.instance().getChangeLog().getFieldList())
        {
            if(field.getName().getText().equals(currentVersion))
            {
                break;
            }
            area.append(field.getName().getText() + ":\n");
            String[] changes = field.getValue().getText().split("\n");
            for(String change : changes)
            {
                area.append("- " + change + "\n");
            }
        }
    }

    private int getTotalDownloadSize()
    {
        int size = 0;
        for(FileEntry entry : this.fileChecker.missingList)
        {
            size += entry.getSize();
        }
        return size;
    }

    public void deleteDeprecated()
    {
        for(FileEntry entry : this.fileChecker.outdatedList)
        {
            File f = new File(modPackDir, entry.getPath());
            if(f.delete())
            {
                System.out.println(String.format(LANG.getTranslation("file.removed.md5.success"), f.getPath(), entry.getMd5()));
            }
            else
            {
                frame.dispose();
                JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotdeletefile") + " : " + f.getPath(), "Error", JOptionPane.ERROR_MESSAGE);
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
                downloadMod(this);
                if(runner.shouldDownloadLib())
                {
                    downloadLib(this);
                }
                finish();
            }
        }.start();
    }

    public void downloadMod(Thread thread)
    {
        for(FileEntry entry : fileChecker.missingList)
        {
            File f = new File(modPackDir, entry.getPath());
            if(f.getParentFile() != null && !f.getParentFile().isDirectory())
            {
                f.getParentFile().mkdirs();
            }
            currentDownload.setText(entry.getPath());
            System.out.println(String.format(LANG.getTranslation("proc.downloadingfile"), entry.getUrl().toString(), f.getPath(), entry.getMd5()));
            if(!DownloadUtils.downloadFile(entry.getUrl(), f, fileProgressBar, fullProgressBar, downloadSpeedLabel))
            {
                frame.dispose();
                thread.interrupt();
                JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotdownload") + " : " + entry.getUrl().toString(), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void downloadLib(Thread thread)
    {
        this.frame.setTitle(LANG.getTranslation("title.libs"));
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

                System.out.println(String.format(LANG.getTranslation("proc.consideringlib"), libName));
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
                if(library.isStringValue("directURL"))
                {
                    libURL = library.getStringValue("directURL");
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
            currentDownload.setText(String.format(LANG.getTranslation("proc.downloadinglib"), entry.getName()));
            try
            {
                if(entry.isXZ())
                {
                    if(!DownloadUtils.downloadFile(new URL(entry.getUrl()), entry.getPackDest(), fileProgressBar, fullProgressBar, downloadSpeedLabel))
                    {
                        thread.interrupt();
                        frame.dispose();
                        JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotdownload") + " : " + entry.getUrl().toString() + DownloadUtils.PACK_NAME, LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        try
                        {
                            currentDownload.setText(LANG.getTranslation("proc.unpackingfile") + " : " + entry.getPackDest().toString());
                            DownloadUtils.unpackLibrary(entry.getDest(), Files.toByteArray(entry.getPackDest()));
                            currentDownload.setText(String.format(LANG.getTranslation("file.unpacked.success"), entry.getPackDest().toString()));
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
                    thread.interrupt();
                    frame.dispose();
                    JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotdownload") + " : " + entry.getUrl().toString() + DownloadUtils.PACK_NAME, "Error", JOptionPane.ERROR_MESSAGE);
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
    }

    public void finish()
    {
        this.frame.setTitle(LANG.getTranslation("misc.finishing"));
        this.createOrUpdateProfile();
        this.writeModPackInfo();
        if(!this.update)
        {
            this.addToProfileList();
        }

        this.frame.dispose();
        this.runner.onFinish();
    }

    private void createOrUpdateProfile()
    {
        String modpackName = RemoteInfoReader.instance().getModPackName();
        File versionRootDir = new File(mcDir, "versions");
        File modpackVersionDir = new File(versionRootDir, modpackName);
        if(!modpackVersionDir.exists())
        {
            modpackVersionDir.mkdirs();
        }
        File modpackJson = new File(modpackVersionDir, modpackName + ".json");

        JsonRootNode versionJson = JsonNodeFactories.object(RemoteInfoReader.instance().getProfileInfo().getFields());
        try
        {
            BufferedWriter newWriter = Files.newWriter(modpackJson, Charsets.UTF_8);
            JSON_FORMATTER.format(versionJson, newWriter);
            newWriter.close();
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotwriteversion"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addToProfileList()
    {
        String modpackName = RemoteInfoReader.instance().getModPackName();
        File launcherProfiles = new File(mcDir, "launcher_profiles.json");
        if(!launcherProfiles.exists())
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.mcprofilemissing"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            this.frame.dispose();
        }
        JdomParser parser = new JdomParser();
        JsonRootNode jsonProfileData;

        try
        {
            jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
        }
        catch(InvalidSyntaxException e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.mcprofilecorrupted"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            throw Throwables.propagate(e);
        }
        catch(Exception e)
        {
            throw Throwables.propagate(e);
        }

        JsonField[] fields = null;
        if(RemoteInfoReader.instance().hasArgument())
        {
            fields = new JsonField[] {JsonNodeFactories.field("name", JsonNodeFactories.string(modpackName)), JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(modpackName)), JsonNodeFactories.field("javaArgs", JsonNodeFactories.string(RemoteInfoReader.instance().getArgument()))};
        }
        else
        {
            fields = new JsonField[] {JsonNodeFactories.field("name", JsonNodeFactories.string(modpackName)), JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(modpackName))};
        }

        HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
        HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
        profileCopy.put(JsonNodeFactories.string(modpackName), JsonNodeFactories.object(fields));
        JsonRootNode profileJsonCopy = JsonNodeFactories.object(profileCopy);

        rootCopy.put(JsonNodeFactories.string("profiles"), profileJsonCopy);

        jsonProfileData = JsonNodeFactories.object(rootCopy);

        try
        {
            BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
            JSON_FORMATTER.format(jsonProfileData, newWriter);
            newWriter.close();
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotwriteprofile"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void writeModPackInfo()
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

        JsonObjectNodeBuilder jsonBuilder = JsonNodeBuilders.anObjectBuilder().withField("forge", aStringBuilder(RemoteInfoReader.instance().getForgeVersion()));
        jsonBuilder.withField("remote", aStringBuilder(RemoteInfoReader.instance().remoteUrl));
        if(RemoteInfoReader.instance().hasChangeLog())
        {
            JsonRootNode changeLog = RemoteInfoReader.instance().getChangeLog();
            if(changeLog != null && changeLog.hasFields())
                jsonBuilder.withField("currentVersion", aStringBuilder(changeLog.getFieldList().get(0).getName().getText()));
        }
        JsonRootNode json = jsonBuilder.build();
        try
        {
            BufferedWriter writer = Files.newWriter(info, Charsets.UTF_8);
            JSON_FORMATTER.format(json, writer);
            writer.close();
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.cannotwriteversion"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
        }
    }
}