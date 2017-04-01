package com.builtbroken.mc.codegen.processor;

import com.builtbroken.mc.codegen.Main;
import com.builtbroken.mc.codegen.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/1/2017.
 */
public abstract class Processor
{
    protected final HashMap<String, Template> templateMap = new HashMap();

    /** Name of the annotation that defines this processor should be used */
    public final String annotationKey;

    /**
     * @param annotationKey - needs to be the exact name of the annotation
     */
    public Processor(String annotationKey)
    {
        this.annotationKey = annotationKey;
    }

    /**
     * Called to handle a file
     *
     * @param outputFolder  - folder to output the file to, this is the global src folder not the exact file
     * @param annotations   - all annotations processed as name to data map
     * @param classPackage  - package of the class file that is being processed
     * @param fileClassName - name of the class file that is being processed
     * @param spacer        - current spacer for debug output
     * @throws IOException
     */
    public abstract void handleFile(File outputFolder, HashMap<String, String> annotations, String classPackage, String fileClassName, String spacer) throws IOException;

    /**
     * Called to build the file and write it to disk
     *
     * @param outputFolder
     * @param templates
     * @param classPackage
     * @param className
     * @param fileClassName
     * @param spacer
     */
    protected void build(File outputFolder, List<Template> templates, String classPackage, String className, String fileClassName, String spacer)
    {
        //Start building file
        StringBuilder builder = new StringBuilder();

        //Write top of file notes
        builder.append("//THIS IS A GENERATED CLASS FILE\n");

        //Write package
        builder.append("package " + classPackage + ";\n");
        builder.append("\n");

        //Write imports
        createImports(builder, templates);
        builder.append("\n");

        //Write class header
        createClassHeader(builder, className, templates);

        //Body start
        builder.append("\n{\n");

        //Writer constructor
        createConstructor(builder, className, fileClassName, templates);

        //Write body
        createBody(builder, templates);

        //Body end
        builder.append("}");

        //Write file to disk
        try
        {
            File outFile = new File(outputFolder, classPackage.replace(".", File.separator) + File.separator + className + ".java");
            Main.out(spacer + "  Writing file to disk, file = " + outFile);
            if (!outFile.getParentFile().exists())
            {
                outFile.getParentFile().mkdirs();
                Main.out(spacer + "   Created directories");
            }
            else if (outFile.exists())
            {
                Main.out(spacer + "   Overriding existing file");
            }

            FileWriter fileWriter = new FileWriter(outFile);
            fileWriter.write(builder.toString());
            fileWriter.flush();
            fileWriter.close();
        }
        catch (Exception e)
        {
            Main.error(spacer + "    Error writing file", e);
            System.exit(1);
        }
    }

    /**
     * Called to crate imports section of the file
     *
     * @param builder
     * @param templates
     */
    protected void createImports(StringBuilder builder, List<Template> templates)
    {
        List<String> imports = new ArrayList();
        collectIgnoredImports(imports, templates);

        //Add imports
        for (Template template : templates)
        {
            List<String> importsFromProcessor = template.getImports();
            for (String imp : importsFromProcessor)
            {
                //Prevent duplication
                if (!imports.contains(imp))
                {
                    imports.add(imp);
                    builder.append("import ");
                    builder.append(imp);
                    builder.append(";\n");
                }
            }
        }
    }

    /**
     * Called to get imports that should be ignored from output file
     *
     * @param imports   - place to add imports to ignore
     * @param templates - templates to check against
     */
    protected void collectIgnoredImports(List<String> imports, List<Template> templates)
    {

    }

    /**
     * Called to create the class header
     *
     * @param builder
     * @param className
     * @param templates
     */
    protected void createClassHeader(StringBuilder builder, String className, List<Template> templates)
    {
        //TODO implement annotations
        //Create header
        builder.append("public class " + className + " extends TileEntityWrapper");

        //Add implements
        List<String> interfaces = new ArrayList();

        for (Template template : templates)
        {
            List<String> interfacesFromProcessor = template.getInterfaces();
            for (String imp : interfacesFromProcessor)
            {
                //Prevent duplication
                if (!interfaces.contains(imp))
                {
                    interfaces.add(imp);
                }
            }
        }
        if (!interfaces.isEmpty())
        {
            builder.append(" implements ");
            for (int i = 0; i < interfaces.size(); i++)
            {
                builder.append(interfaces.get(i));
                if (i != (interfaces.size() - 1))
                {
                    builder.append(", ");
                }
            }
        }
    }

    /**
     * Called to create the constructor for the class
     *
     * @param builder
     * @param className
     * @param fileClassName
     * @param templates
     */
    protected void createConstructor(StringBuilder builder, String className, String fileClassName, List<Template> templates)
    {

    }

    /**
     * Called to create the body for the class
     *
     * @param builder
     * @param templates
     */
    protected void createBody(StringBuilder builder, List<Template> templates)
    {
        for (Template template : templates)
        {
            if (template.fieldBody != null)
            {
                builder.append("\t//Fields from ");
                builder.append(template.getKey());
                builder.append("\n");

                String[] fields = template.fieldBody.split(";");
                for (String field : fields)
                {
                    if (!field.isEmpty())
                    {
                        builder.append("\t");
                        builder.append(field.trim());
                        builder.append(";\n");
                    }
                }
            }
        }

        for (Template template : templates)
        {
            if (template.methodBody != null)
            {
                builder.append("\t//============================\n\t//==Methods:");
                builder.append(template.getKey());
                builder.append("\n\t//============================\n");
                builder.append("\n");

                builder.append(template.methodBody);
                builder.append("\n");
            }
        }
    }

    /**
     * Called to load all templates
     *
     * @param directory
     * @param depth
     * @return
     */
    public void loadTemplates(File directory, int depth)
    {
        String spacer;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= depth; i++)
        {
            builder.append("  ");
        }
        spacer = builder.toString();

        Main.out(spacer + "*Directory: " + directory.getName());

        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                loadTemplates(file, ++depth);
            }
            else
            {
                Main.out("");
                Main.out(spacer + "--File: " + file.getName());
                Main.out(spacer + " |------------------------->");
                Template template = new Template();
                try
                {

                    template = template.loadFile(file, spacer + " | ");
                    //If returns null the file was not a template
                    if (template != null)
                    {
                        if (template.isValid())
                        {
                            templateMap.put(template.getKey(), template);
                        }
                        else
                        {
                            Main.error("Template file is invalid, exiting to prevent issues " + file);
                        }
                    }
                }
                catch (Exception e)
                {
                    Main.error("Unexpected error while loading template from file " + file, e);
                }
                Main.out(spacer + " |------------------------->");
            }
        }
    }
}
