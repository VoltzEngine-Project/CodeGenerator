package com.builtbroken.mc.codegen.data;

import java.util.HashMap;

/**
 * Data about a single file build process
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/3/2017.
 */
public class BuildData
{
    /** Annotations on the original file */
    public final HashMap<String, String> annotations;
    /** Class name of the original file */
    public final String className;
    /** Class package of the original file */
    public final String classPackage;

    /** Class name and file of the output file */
    public String outputClassName;
    /** Class package of the output file */
    public String outputClassPackage;

    public BuildData(HashMap<String, String> annotationToData, String classPackage, String className)
    {
        this.annotations = annotationToData;
        this.className = className;
        this.classPackage = classPackage;
        this.outputClassPackage = classPackage;
    }
}
