package fr.minecraftforgefrance.common;

public interface IInstallRunner
{
    void onFinish();

    boolean shouldDownloadLib();
}