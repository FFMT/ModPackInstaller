package fr.minecraftforgefrance.installer;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.minecraftforgefrance.common.EnumOS;

public class OptionFrame extends JFrame
{
    private static final long serialVersionUID = 1L;
    public static File targetDir = EnumOS.getMinecraftDefaultDir();
    public JTextField selectedDirText;

    public OptionFrame(Dimension dim)
    {
        this.setTitle(LANG.getTranslation("title.options"));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        selectedDirText = new JTextField();
        selectedDirText.setEditable(false);
        selectedDirText.setToolTipText("Path to minecraft");
        selectedDirText.setColumns(30);
        selectedDirText.setText(targetDir.getPath());
        panel.add(selectedDirText);
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
                dirChooser.ensureFileIsVisible(OptionFrame.targetDir);
                dirChooser.setSelectedFile(OptionFrame.targetDir);
                int response = dirChooser.showOpenDialog(OptionFrame.this);
                switch(response)
                {
                    case JFileChooser.APPROVE_OPTION:
                        try
                        {
                            OptionFrame.targetDir = dirChooser.getSelectedFile().getCanonicalFile();
                        }
                        catch(IOException e1)
                        {
                            e1.printStackTrace();
                        }
                        OptionFrame.this.selectedDirText.setText(targetDir.getPath());
                        break;
                    default:
                        break;
                }
            }

        });
        dirSelect.setText("...");
        dirSelect.setToolTipText("Select an alternative minecraft directory");
        panel.add(dirSelect);

        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setAlignmentY(TOP_ALIGNMENT);
        JLabel infoLabel = new JLabel();
        infoLabel.setHorizontalTextPosition(JLabel.LEFT);
        infoLabel.setVerticalTextPosition(JLabel.TOP);
        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        infoLabel.setAlignmentY(TOP_ALIGNMENT);
        infoLabel.setForeground(Color.RED);
        infoLabel.setVisible(false);

        JPanel fileEntryPanel = new JPanel();
        fileEntryPanel.setLayout(new BoxLayout(fileEntryPanel, BoxLayout.Y_AXIS));
        fileEntryPanel.add(infoLabel);
        fileEntryPanel.add(Box.createVerticalGlue());
        fileEntryPanel.add(panel);
        fileEntryPanel.setAlignmentX(CENTER_ALIGNMENT);
        fileEntryPanel.setAlignmentY(TOP_ALIGNMENT);
        this.add(fileEntryPanel);
        this.add(panel);
        this.pack();
        int x = (dim.width / 2) - (this.getSize().width / 2);
        int y = (dim.height / 2) - (this.getSize().height / 2);
        this.setLocation(x, y);
    }
}
