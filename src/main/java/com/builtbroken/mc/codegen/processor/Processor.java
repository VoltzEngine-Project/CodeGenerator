package com.builtbroken.mc.codegen.processor;

import com.builtbroken.mc.codegen.Main;
import com.builtbroken.mc.codegen.data.BuildData;
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
    /** Name of the annotation that defines what templates to load */
    public final String templateAnnotationKey;

    public final String classToExtend;

    /**
     * @param annotationKey - needs to be the exact name of the annotation
     */
    public Processor(String annotationKey, String templateAnnotationKey, String classToExtend)
    {
        this.annotationKey = annotationKey;
        this.templateAnnotationKey = templateAnnotationKey;
        this.classToExtend = classToExtend;
    }

    /**
     * Called to load additional settings for this processor
     * from program arguments.
     *
     * @param arguments - map of argument to value
     */
    public void initialized(File mainDirectory, HashMap<String, String> arguments)
    {

    }

    /**
     * Called to handle a file.
     * <p>
     * Make sure to call build.
     *
     * @param outputFolder - folder to output the file to, this is the global src folder not the exact file
     * @param buildData    - data about the build process
     * @param spacer       - current spacer for debug output
     * @throws IOException
     */
    public abstract void handleFile(File outputFolder, BuildData buildData, String spacer) throws IOException;

    /**
     * Called to do any actions that need to wait until all files have
     * been processed.
     * <p>
     * This is normally used to generate registry files.
     *
     * @param outputFolder
     */
    public void finalize(File outputFolder)
    {

    }

    /**
     * Called to build the file and write it to disk
     *
     * @param outputFolder - location to store the output file
     * @param templates    - list of templates to use during building
     * @param spacer       - spacer to make debug look nice
     */
    protected void build(final File outputFolder, final List<Template> templates, BuildData buildData, final String spacer)
    {
        //Start building file
        StringBuilder builder = new StringBuilder();

        //Write top of file notes
        builder.append("//=======================================================\n");
        builder.append("//DISCLAIMER: THIS IS A GENERATED CLASS FILE\n");
        builder.append("//THUS IS PROVIDED 'AS-IS' WITH NO WARRANTY\n");
        builder.append("//FUNCTIONALITY CAN NOT BE GUARANTIED IN ANY WAY \n");
        builder.append("//USE AT YOUR OWN RISK \n");
        builder.append("//-------------------------------------------------------\n");
        //builder.append("//Build date: ");
        //builder.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss.SSS")));
        //builder.append("\n");
        builder.append("//Built on: ");
        builder.append(System.getProperty("user.name"));
        builder.append("\n");
        builder.append("//=======================================================\n");

        //Figure out what to use for class name and package path
        if (buildData.outputClassName.contains("."))
        {
            if (!buildData.outputClassName.startsWith("."))
            {
                buildData.outputClassPackage = "";
            }
            buildData.outputClassPackage += buildData.outputClassName.substring(0, buildData.outputClassName.lastIndexOf("."));
            buildData.outputClassName = buildData.outputClassName.substring(buildData.outputClassName.lastIndexOf(".") + 1, buildData.outputClassName.length());
        }
        //Else name is just a name and will need to be prefixed by package path
        else
        {
            buildData.outputClassPackage = buildData.classPackage;
        }

        //Write package
        builder.append("package " + buildData.outputClassPackage + ";\n");
        builder.append("\n");

        //Write imports
        createImports(builder, templates, buildData);
        builder.append("\n");

        //Write class header
        createClassHeader(builder, templates, buildData);

        //Body start
        builder.append("\n{\n");

        //Writer constructor
        createConstructor(builder, templates, buildData);

        //Write body
        createBody(builder, templates, buildData);

        //Body end
        builder.append("}");

        //Write file to disk
        try
        {
            File outFile;
            //If contains dot the class name starts with a package path
            outFile = new File(outputFolder, buildData.outputClassPackage.replace(".", File.separator) + File.separator + buildData.outputClassName + ".java");
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
    protected void createImports(StringBuilder builder, List<Template> templates, BuildData buildData)
    {
        //Get imports to use
        final List<String> imports = new ArrayList();
        //Add import to class we are wrapping
        imports.add(buildData.classPackage + "." + buildData.className);
        collectImports(imports, templates, buildData);

        //Get imports to ignore
        final List<String> ignored = new ArrayList();
        collectIgnoredImports(ignored, templates, buildData);

        //TODO ensure all imports are used

        //Output imports
        for (String imp : imports)
        {
            if (!ignored.contains(imp))
            {
                builder.append("import ");
                builder.append(imp);
                builder.append(";\n");
            }
        }
    }

    /**
     * Called to collect imports that may be needed
     *
     * @param imports
     * @param templates
     */
    protected void collectImports(List<String> imports, List<Template> templates, BuildData buildData)
    {
        for (Template template : templates)
        {
            if (template != null)
            {
                List<String> importsFromProcessor = template.getImports();
                for (String imp : importsFromProcessor)
                {
                    //Prevent duplication
                    if (!imports.contains(imp))
                    {
                        imports.add(imp);
                    }
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
    protected void collectIgnoredImports(List<String> imports, List<Template> templates, BuildData buildData)
    {

    }

    /**
     * Called to create the class header
     *
     * @param builder
     * @param templates
     */
    protected void createClassHeader(StringBuilder builder, List<Template> templates, BuildData buildData)
    {
        //TODO implement annotations
        //Create header
        builder.append("public class " + buildData.outputClassName + " extends " + classToExtend);

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
     * @param templates
     */
    protected void createConstructor(StringBuilder builder, List<Template> templates, BuildData buildData)
    {

    }

    /**
     * Called to create the body for the class
     *
     * @param builder
     * @param templates
     */
    protected void createBody(StringBuilder builder, List<Template> templates, BuildData buildData)
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
                Template template = new Template(templateAnnotationKey, classToExtend);
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
