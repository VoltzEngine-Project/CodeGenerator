package com.builtbroken.mc.codegen;

import com.builtbroken.mc.codegen.processor.Processor;
import com.builtbroken.mc.codegen.template.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/31/2017.
 */
public class Main
{
    public static Pattern packagePattern = Pattern.compile("package(.*?);");

    public static void main(String... args)
    {
        out("VoltzEngine Code Generator v0.1.0");
        out("Parsing arguments...");

        //Load arguments
        HashMap<String, String> launchSettings = loadArgs(args);

        if (launchSettings.containsKey("src") && launchSettings.containsKey("templates") && launchSettings.containsKey("output") && launchSettings.containsKey("processors"))
        {
            File runFolder = new File(".");
            File targetFolder;
            List<File> templateFolders = new ArrayList();
            File outputFolder;

            //Get source folder path
            String path = launchSettings.get("src");
            if (path.startsWith("."))
            {
                targetFolder = new File(runFolder, path.substring(1, path.length()));
            }
            else
            {
                targetFolder = new File(path);
            }

            //Load template folders
            String[] folders = launchSettings.get("templates").split(",");
            for (String folder : folders)
            {
                File file;
                if (folder.startsWith("."))
                {
                    file = new File(runFolder, folder.substring(1, folder.length()));
                }
                else
                {
                    file = new File(path);
                }
                //Ensure we have a template folder
                if (!file.exists() || !file.isDirectory())
                {
                    error("The template folder '" + file + "' does not exist.");
                }
                else
                {
                    templateFolders.add(file);
                }
            }
            if (templateFolders.isEmpty())
            {
                error("No template folders were loaded, can not continue as processors will have noting to generate.");
            }

            //Get output folder
            path = launchSettings.get("output");
            if (path.startsWith("."))
            {
                outputFolder = new File(runFolder, path.substring(1, path.length()));
            }
            else
            {
                outputFolder = new File(path);
            }

            //Ensure we have an output folder
            if (outputFolder.exists() && !outputFolder.isDirectory())
            {
                out("output folder is not a directory: " + outputFolder);
                System.exit(1);
            }
            if (!outputFolder.delete())
            {
                out("Failed to delete output folder: " + outputFolder);
            }
            outputFolder.mkdirs();

            //Ensure we have a target source folder
            if (targetFolder.exists() && targetFolder.isDirectory())
            {
                out("");
                //Load processors
                List<Processor> processors = new ArrayList();

                String[] processorEntries = launchSettings.get("processors").split(",");
                for (String processorEntry : processorEntries)
                {
                    try
                    {
                        Class clazz = Class.forName(processorEntry);
                        Processor processor = (Processor) clazz.newInstance();
                        processors.add(processor);
                    }
                    catch (ClassNotFoundException e)
                    {
                        error("Failed to locate processor class " + processorEntry, e);
                    }
                    catch (InstantiationException e)
                    {
                        error("Failed to create processor object " + processorEntry, e);
                    }
                    catch (IllegalAccessException e)
                    {
                        error("Failed to access processor class " + processorEntry, e);
                    }
                }

                //Ensure we have templates to use
                if (processors.isEmpty())
                {
                    out("No templates were loaded, can not continue with templates to use");
                    System.exit(1);
                }

                for (Processor processor : processors)
                {
                    out("Initializing processor: " + processor);
                    for (File file : templateFolders)
                    {
                        out("Loading templates from " + file);
                        processor.loadTemplates(file, 0);
                    }
                }

                //Load classes
                out("");
                out("Loading classes from " + targetFolder);
                handleDirectory(targetFolder, processors, outputFolder, 0);
            }
            else
            {
                out("The target folder does not exist. Folder: " + targetFolder);
                System.exit(1);
            }
        }
        else
        {
            out("In order for code to be parsed and generator you need to specify in the program arguments: -src=\"path/to/source/files\" -templates=\"path/to/source/templates\" -output=\"path/to/source/output\"");
            System.exit(1);
        }

        out("Exiting...");
    }

    public static void out(String msg)
    {
        System.out.println(msg);
    }

    public static void error(String msg, Throwable t)
    {
        System.err.println(msg);
        t.printStackTrace();
        System.exit(1);
    }

    public static void error(String msg)
    {
        System.err.println(msg);
        System.exit(1);
    }

    public static void warn(String msg)
    {
        System.err.println(msg);
    }

    public static void handleDirectory(File directory, List<Processor> processors, File outputFolder, int depth)
    {
        //Generate spacer to make debug look nice
        String spacer;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= depth; i++)
        {
            builder.append("  ");
        }
        spacer = builder.toString();

        out(spacer + "*Directory: " + directory.getName());

        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                handleDirectory(file, processors, outputFolder, ++depth);
            }
            else
            {
                out("");
                out(spacer + "--File: " + file.getName());
                out(spacer + " |------------------------->");
                try
                {
                    handleFile(file, processors, outputFolder, spacer + " |");
                }
                catch (IOException e)
                {
                    error("Unexpected exception while parsing " + file, e);
                    System.exit(1);
                }
                out(spacer + " |------------------------->");
            }
        }
    }

    public static void handleFile(File file, List<Processor> allProcessors, File outputFolder, String spacer) throws IOException
    {
        String fileClassName = file.getName();
        if (fileClassName.endsWith(".java"))
        {
            fileClassName = fileClassName.substring(0, fileClassName.length() - 5);
            String classPackage = null;
            List<String> annotations = new ArrayList();
            BufferedReader br = new BufferedReader(new FileReader(file));
            try
            {
                String line;

                while ((line = br.readLine()) != null)
                {
                    //Ignore all import lines so not to parse {} or @ in imports
                    if (!line.contains("import"))
                    {
                        if (line.contains("package"))
                        {
                            final Matcher matcher = packagePattern.matcher(line);
                            if (matcher.matches())
                            {
                                classPackage = matcher.group(1).trim();
                            }
                        }
                        else if (line.contains("@"))
                        {
                            annotations.addAll(Parser.getAnnotations(line));
                        }
                        //First { should be the end of the class header
                        else if (line.contains("{"))
                        {
                            break;
                        }
                    }
                }
            }
            finally
            {
                br.close();
            }

            HashMap<String, String> annotationToData = new HashMap();
            //Debug data
            out(spacer + "  Package: " + classPackage);
            out(spacer + "  Annotations:");

            //Output annotation and parse
            for (String string : annotations)
            {
                out(spacer + "      " + string);

                int firstParn = string.indexOf("(");
                String annotation = string.substring(0, firstParn);
                String data = string.substring(firstParn + 1, string.length() - 1);
                annotationToData.put(annotation, data);
            }

            for (Processor processor : allProcessors)
            {
                if (annotationToData.containsKey(processor.annotationKey))
                {
                    processor.handleFile(outputFolder, annotationToData, classPackage, fileClassName, spacer);
                }
            }
            //TODO build list of all generated data to be registered
        }
    }


    /**
     * Converts arguments into a hashmap for usage
     *
     * @param args
     * @return
     */
    public static HashMap<String, String> loadArgs(String... args)
    {
        final HashMap<String, String> map = new HashMap();
        if (args != null)
        {
            String currentArg = null;
            String currentValue = "";
            for (int i = 0; i < args.length; i++)
            {
                String next = args[i].trim();
                if (next == null)
                {
                    throw new IllegalArgumentException("Null argument detected in launch arguments");
                }
                else if (next.startsWith("-"))
                {
                    if (currentArg != null)
                    {
                        map.put(currentArg, currentValue);
                        currentValue = "";
                    }

                    if (next.contains("="))
                    {
                        String[] split = next.split("=");
                        currentArg = split[0].substring(1).trim();
                        currentValue = split[1].trim();
                    }
                    else
                    {
                        currentArg = next.substring(1).trim();
                    }
                }
                else if (currentArg != null)
                {
                    if (!currentValue.isEmpty())
                    {
                        currentValue += ",";
                    }
                    currentValue += next.replace("\"", "").replace("'", "").trim();
                }
                else
                {
                    throw new IllegalArgumentException("Value has no argument associated with it [" + next + "]");
                }
            }
            //Add the last loaded value to the map
            if (currentArg != null)
            {
                map.put(currentArg, currentValue);
            }
        }
        return map;
    }
}
