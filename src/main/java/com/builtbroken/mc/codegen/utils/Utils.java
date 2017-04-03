package com.builtbroken.mc.codegen.utils;

import com.builtbroken.mc.codegen.Main;

import java.io.File;

public class Utils
{
    /**
     * Called to get a file from a file value
     *
     * @param workingDirectory - working directory to use in case the value is relative
     * @param value            - value
     * @return folder
     */
    public static File getFile(File workingDirectory, String value)
    {
        if (value.startsWith("."))
        {
            return new File(workingDirectory, value.substring(2));
        }
        else
        {
            return new File(value);
        }
    }

    /**
     * Called to clean out a directory
     * <p>
     * Deletes all files that are not folders.
     * Recursively moves through sub folders.
     *
     * @param outputFolder
     */
    public static void cleanFolder(File outputFolder)
    {
        File[] files = outputFolder.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                cleanFolder(file);
            }
            else
            {
                if (file.delete())
                {
                    Main.out("Deleted: " + file);
                }
                else
                {
                    Main.warn("Failed to delete file: " + file);
                }
            }
        }
    }
}
