package fr.minecraftforgefrance.common;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonRootNode;

public class InstallFrame extends JFrame
{
    private static final long serialVersionUID = 1L;

    public JProgressBar fileProgressBar;
    public JProgressBar fullProgressBar;
    private JPanel panel;
    public JLabel downloadSpeedLabel;
    public JLabel currentDownload;

    private final ProcessInstall installThread;

    public InstallFrame(ProcessInstall install)
    {
        this.installThread = install;
    }

    public void run()
    {
        this.setTitle(LANG.getTranslation("proc.verifyfiles"));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setSize(500, 100);
        this.setLocationRelativeTo(null);

        this.fileProgressBar = new JProgressBar(0, 10);
        this.fileProgressBar.setValue(0);
        this.fileProgressBar.setStringPainted(true);
        this.fileProgressBar.setIndeterminate(true);

        this.fullProgressBar = new JProgressBar(0, 10);
        this.fullProgressBar.setValue(0);
        this.fullProgressBar.setStringPainted(true);
        this.fullProgressBar.setIndeterminate(true);

        this.currentDownload = new JLabel(" ");
        this.downloadSpeedLabel = new JLabel(" ");

        this.panel = new JPanel();
        this.panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        this.panel.add(this.currentDownload);
        this.panel.add(this.fileProgressBar);
        this.panel.add(this.fullProgressBar);
        this.panel.add(this.downloadSpeedLabel);

        if(RemoteInfoReader.instance().hasChangeLog())
        {
            JTextArea area = new JTextArea();
            area.setBounds(4, 2, 492, 150);
            this.getChangeLog(area);
            if(!area.getText().isEmpty())
            {
                this.setSize(500, 250);
                this.panel.add(area);
            }
        }
        this.setContentPane(this.panel);
        this.setVisible(true);

        new Thread(this.installThread).start();
    }

    private void getChangeLog(JTextArea area)
    {
        String currentVersion = null;
        File modpackInfo = new File(installThread.modPackDir, RemoteInfoReader.instance().getModPackName() + ".json");
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
        if(RemoteInfoReader.instance().getChangeLog() != null)
        {
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
    }
}