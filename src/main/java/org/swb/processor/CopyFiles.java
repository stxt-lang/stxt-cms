package org.swb.processor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class CopyFiles extends AbstractDirProcessor
{
    // --------------------------
    // Variables de configuración
    // --------------------------
    
    private int lengthBaseDir = 0;
    private boolean overwrite = true;
    // flatten=true copies every file straight into todir, without its subfolders (the images of
    // the book, one folder per part); two sources with the same name stop the build.
    private boolean flatten = false;
    private final Map<String, File> flattened = new HashMap<>();

    // ----
    // Init
    // ----
    
    @Override
    public void init(String name, Properties config) throws Exception
    {
        // Configuración super()
        super.init(name, config);
        
        // Validamos directorio de salida
        if (todir == null)
        {
            System.err.println("Debe especificar directorio de salida");
            System.exit(1);
        }
        
        // Obtenemos path inicial
        lengthBaseDir = dir.getCanonicalPath().length();
        
        // Miramos si hay overwrite
        if (config.containsKey("overwrite"))
        {
            overwrite = Boolean.parseBoolean(config.getProperty("overwrite"));
        }
        flatten = Boolean.parseBoolean(config.getProperty("flatten", "false"));
    }
    
    // -----------------
    // Log de inicio/fin
    // -----------------
    
    @Override
    protected void startProcess(Map<String, Object> context) throws IOException
    {
        System.out.println("Copy start: " + dir.getCanonicalPath() + " to " + todir.getCanonicalPath());
    }
    @Override
    protected void endProcess(Map<String, Object> context) throws IOException
    {
        System.out.println("Copy end.");
    }    
    
    // ----------------
    // Método principal
    // ----------------
    
    @Override
    protected void process(Map<String, Object> context, File srcFile) throws IOException, Exception
    {
        File dstFile;
        if (flatten)
        {
            dstFile = new File(todir, srcFile.getName());
            File previous = flattened.put(srcFile.getName(), srcFile);
            if (previous != null)
            {
                throw new IllegalArgumentException("Two files named " + srcFile.getName() + " in " + dir.getCanonicalPath()
                        + ": " + previous.getCanonicalPath() + " and " + srcFile.getCanonicalPath());
            }
        }
        else
        {
            dstFile = new File(todir, srcFile.getCanonicalPath().substring(lengthBaseDir));
        }
        if (dstFile.exists() && !overwrite) return;
        
        System.out.println((dstFile.exists() ? "Overwrite: " : "Copy: ") + srcFile.getCanonicalPath() + " to " + dstFile.getCanonicalPath());
        FileUtils.copyFile(srcFile, dstFile);
    }
}
