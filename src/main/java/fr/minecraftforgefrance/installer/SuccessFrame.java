package fr.minecraftforgefrance.installer;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import fr.minecraftforgefrance.common.EnumOS;

public class SuccessFrame extends JDialog
{
    private static final long serialVersionUID = 1L;
    private File launcherFile;

    public SuccessFrame()
    {
        this.setTitle(LANG.getTranslation("misc.success"));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setSize(300, 100);
        this.setLayout(new BorderLayout());
        this.setModalityType(ModalityType.APPLICATION_MODAL);

        JPanel panel = new JPanel();
        JLabel label = new JLabel(LANG.getTranslation("installation.success"));
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setAlignmentY(TOP_ALIGNMENT);
        panel.add(label);
        this.getContentPane().add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton exit = new JButton(LANG.getTranslation("scr.btn.exit"));
        exit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SuccessFrame.this.dispose();
            }
        });
        buttonPanel.add(exit);

        if(EnumOS.getPlatform() == EnumOS.WINDOWS)
        {
            if(System.getenv("ProgramW6432").isEmpty())
            {
                // 32 bits system
                this.launcherFile = new File("C:\\Program Files\\Minecraft\\MinecraftLauncher.exe");
            }
            else
            {
                // 64 bits system
                this.launcherFile = new File("C:\\Program Files (x86)\\Minecraft\\MinecraftLauncher.exe");
            }
        }
        else
        {
            this.launcherFile = new File(Installer.frame.mcDir.getPath(), "launcher.jar");
        }

        JButton runGame = new JButton(LANG.getTranslation("scr.btn.run"));
        runGame.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(EnumOS.getPlatform() == EnumOS.WINDOWS)
                {
                    try
                    {
                        Runtime.getRuntime().exec(SuccessFrame.this.launcherFile.getAbsolutePath());
                    }
                    catch(IOException ex2)
                    {
                        ex2.printStackTrace();
                        JOptionPane.showMessageDialog(null, LANG.getTranslation("err.runminecraft"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
                else
                {
                    try
                    {
                        Runtime.getRuntime().exec(EnumOS.getJavaExecutable() + " -jar " + SuccessFrame.this.launcherFile.getAbsolutePath());
                    }
                    catch(IOException ex)
                    {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, LANG.getTranslation("err.runminecraft"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
                SuccessFrame.this.dispose();
            }
        });
        if(SuccessFrame.this.launcherFile.exists())
        {
            buttonPanel.add(runGame);
        }
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        this.setLocationRelativeTo(null);
    }
}
