package fr.minecraftforgefrance.installer;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.minecraftforgefrance.common.EnumOS;
import fr.minecraftforgefrance.common.RemoteInfoReader;

public class OptionFrame extends JDialog
{
    private static final long serialVersionUID = 1L;
    public JLabel modpackFolder;
    public JLabel infoLabel;
    private JTextField selectedDirText;

    public OptionFrame(Dimension dim)
    {
        this.setTitle(LANG.getTranslation("title.options"));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JLabel gameFolder = new JLabel(LANG.getTranslation("option.mcDir.info"));
        gameFolder.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(gameFolder);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        this.selectedDirText = new JTextField();
        this.selectedDirText.setEditable(false);
        this.selectedDirText.setColumns(35);
        panel1.add(this.selectedDirText);

        JButton dirSelect = new JButton();
        dirSelect.setAction(new AbstractAction()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser dirChooser = new JFileChooser();
                dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                dirChooser.setFileHidingEnabled(false);
                dirChooser.ensureFileIsVisible(EnumOS.getMinecraftDefaultDir());
                dirChooser.setSelectedFile(EnumOS.getMinecraftDefaultDir());
                int response = dirChooser.showOpenDialog(OptionFrame.this);
                switch(response)
                {
                    case JFileChooser.APPROVE_OPTION:
                        try
                        {
                            OptionFrame.this.updateMinecraftDir(dirChooser.getSelectedFile().getCanonicalFile());
                        }
                        catch(IOException e1)
                        {
                            e1.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }

        });
        dirSelect.setText("...");
        dirSelect.setToolTipText(LANG.getTranslation("option.mcDir.select"));
        panel1.add(dirSelect);
        panel1.setAlignmentY(TOP_ALIGNMENT);
        mainPanel.add(panel1);

        this.infoLabel = new JLabel();
        this.infoLabel.setForeground(Color.RED);
        this.infoLabel.setVisible(false);
        this.infoLabel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(this.infoLabel);

        JLabel label = new JLabel(LANG.getTranslation("option.modpackDir.info"));
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setAlignmentY(TOP_ALIGNMENT);
        mainPanel.add(label);

        this.modpackFolder = new JLabel();
        this.modpackFolder.setAlignmentX(CENTER_ALIGNMENT);
        this.modpackFolder.setAlignmentY(BOTTOM_ALIGNMENT);
        mainPanel.add(this.modpackFolder);

        JPanel buttonPanel = new JPanel();
        JButton confirm = new JButton(LANG.getTranslation("option.confirm"));
        confirm.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                OptionFrame.this.dispose();
            }
        });
        buttonPanel.add(confirm);

        mainPanel.add(buttonPanel);

        this.add(mainPanel);
        this.pack();
        int x = (dim.width / 2) - (this.getSize().width / 2);
        int y = (dim.height / 2) - (this.getSize().height / 2);
        this.setLocation(x, y);
        this.updateMinecraftDir(EnumOS.getMinecraftDefaultDir());
    }

    public void updateMinecraftDir(File newMCDir)
    {
        this.selectedDirText.setText(newMCDir.getPath());
        this.modpackFolder.setText(newMCDir.getPath() + File.separator + "modpack" + File.separator + RemoteInfoReader.instance().getModPackName());

        File launcherProfiles = new File(newMCDir, "launcher_profiles.json");
        if(!launcherProfiles.exists())
        {
            this.infoLabel.setText(LANG.getTranslation("option.folder.notValid"));
            this.infoLabel.setVisible(true);
        }
        else
        {
            Installer.frame.mcDir = newMCDir;
            this.infoLabel.setVisible(false);
        }
        this.pack();
    }
}
