package fr.minecraftforgefrance.updater;

import static fr.minecraftforgefrance.common.Localization.LANG;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import fr.minecraftforgefrance.common.FileChecker;
import fr.minecraftforgefrance.common.IInstallRunner;
import fr.minecraftforgefrance.common.Localization;
import fr.minecraftforgefrance.common.Logger;
import fr.minecraftforgefrance.common.ProcessInstall;
import fr.minecraftforgefrance.common.RemoteInfoReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launchwrapper.Launch;

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
        Logger.info("Starting updater !");
        final OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        final OptionSpec<File> gameDirOption = parser.accepts("gameDir", "The game directory").withRequiredArg().ofType(File.class);
        final OptionSpec<String> modpackOption = parser.accepts("version", "The version used").withRequiredArg();

        final OptionSet options = parser.parse(args);
        File gameDir = options.valueOf(gameDirOption);
        String modpackName = options.valueOf(modpackOption);
        File modPackDir;
        File mcDir;
        
        if(!gameDir.getAbsoluteFile().getPath().endsWith(modpackName))
        {
            mcDir = gameDir;
            modPackDir = new File(new File(gameDir, "modpacks"), modpackName);
            for(int i = 0; i < args.length; i++)
            {
                if("--gameDir".equals(args[i]))
                {
                    args[i + 1] = modPackDir.getAbsolutePath();
                }
            }
        }
        else
        {
            modPackDir = gameDir;
            mcDir = gameDir.getParentFile().getParentFile();
        }
        Logger.info(String.format("Running installer in folder: %s", gameDir.getPath()));
        this.arguments = args;

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
            jsonProfileData = jsonParser.parse(com.google.common.io.Files.newReader(modpackInfo, Charsets.UTF_8));
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
        
        this.injectLECert();
        
        RemoteInfoReader.instance = new RemoteInfoReader(jsonProfileData.getStringValue("remote"));
        if(!RemoteInfoReader.instance().init())
        {
            runMinecraft(args);
        }
        FileChecker checker = new FileChecker(modPackDir);
        if(!shouldUpdate(jsonProfileData.getStringValue("forge"), checker))
        {
            Logger.info("No update found, launching Minecraft !");
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
            install.createFrame();
        }
        long end = System.currentTimeMillis();
        Logger.info(String.format("Update checked in %d ms", (end - start)));

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
        Logger.info("Lauching Minecraft ...");
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
    
    // Minecraft new launcher use Java 8u25, so let's encrypt cert isn't recognized. This code add the let's encrypt root cert in trust certs list.
    public void injectLECert()
    {
        try
        {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Path ksPath = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath), null);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(Updater.class.getResourceAsStream("/letsencryptauthorityx3.pem"));
            Certificate crt = cf.generateCertificate(caInput);
            keyStore.setCertificateEntry("letsencryptauthorityx3", crt);
            Logger.info("Added Cert for " + ((X509Certificate)crt).getSubjectDN());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
            SSLContext.setDefault(sslContext);
        }
        catch(Exception e)
        {
        	System.err.println("Failed to import LE root cert:");
        	e.printStackTrace();
        }
    }

}