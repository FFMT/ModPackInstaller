package fr.minecraftforgefrance.updater;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launchwrapper.Launch;
import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import fr.minecraftforgefrance.common.FileChecker;
import fr.minecraftforgefrance.common.IInstallRunner;
import fr.minecraftforgefrance.common.Localization;
import fr.minecraftforgefrance.common.ProcessInstall;
import fr.minecraftforgefrance.common.RemoteInfoReader;

public class Updater implements IInstallRunner
{
    final private String[] arguments;
    private boolean forgeUpdate;

    public static void main(String[] args)
    {
        Localization.init();
        new Updater(args);
    }

    public Updater(String[] args)
    {
        long start = System.currentTimeMillis();
        System.out.println("Starting updater !");
        final OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        final OptionSpec<File> gameDirOption = parser.accepts("gameDir", "The game directory").withRequiredArg().ofType(File.class);
        final OptionSpec<String> modpackOption = parser.accepts("version", "The version used").withRequiredArg();

        final OptionSet options = parser.parse(args);
        File mcDir = options.valueOf(gameDirOption);
        String modpackName = options.valueOf(modpackOption);
        File modPackDir = new File(new File(mcDir, "modpacks"), modpackName);

        for(int i = 0; i < args.length; i++)
        {
            if("--gameDir".equals(args[i]))
            {
                args[i + 1] = modPackDir.getAbsolutePath();
            }
        }
        arguments = args;

        File modpackInfo = new File(modPackDir, modpackName + ".json");
        if(!modpackInfo.exists())
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.erroredprofile"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        JdomParser jsonParser = new JdomParser();
        JsonRootNode jsonProfileData;

        try
        {
            jsonProfileData = jsonParser.parse(Files.newReader(modpackInfo, Charsets.UTF_8));
        }
        catch(InvalidSyntaxException e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.erroredprofile"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            throw Throwables.propagate(e);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("err.erroredprofile"), LANG.getTranslation("misc.error"), JOptionPane.ERROR_MESSAGE);
            throw Throwables.propagate(e);
        }
        RemoteInfoReader.instance = new RemoteInfoReader(jsonProfileData.getStringValue("remote"));
        if(!RemoteInfoReader.instance().init())
        {
            runMinecraft(args);
        }
        FileChecker checker = new FileChecker(mcDir);
        if(!shouldUpdate(jsonProfileData.getStringValue("forge"), checker))
        {
            System.out.println(LANG.getTranslation("no.update.found"));
            long end = System.currentTimeMillis();
            System.out.println(String.format(LANG.getTranslation("update.checked.in"), (end - start)));
            runMinecraft(args);
        }
        else
        {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            ProcessInstall install = new ProcessInstall(checker, this, mcDir, null);
            install.run();
        }
    }

    public boolean shouldUpdate(String forgeVersion, FileChecker checker)
    {
        if(checker.remoteList.isEmpty())
        {
            return false;
        }
        if(!RemoteInfoReader.instance().getForgeVersion().equals(forgeVersion))
        {
            this.forgeUpdate = true;
            return true;
        }
        return !checker.missingList.isEmpty() || !checker.outdatedList.isEmpty();
    }

    public void runMinecraft(String[] args)
    {
        Launch.main(args);
    }

    @Override
    public void onFinish()
    {
        if(this.forgeUpdate)
        {
            JOptionPane.showMessageDialog(null, LANG.getTranslation("update.finished.success"), LANG.getTranslation("misc.success"), JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            runMinecraft(this.arguments);
        }
    }

    @Override
    public boolean shouldDownloadLib()
    {
        return forgeUpdate;
    }
}