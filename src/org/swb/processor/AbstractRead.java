package org.swb.processor;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractRead extends AbstractDirProcessor
{
    // -------------
    // Configuración
    // -------------
    
    private String outName = "out";
    private Map result = new LinkedHashMap();
    
    @Override
    public void init(String name, Properties config) throws Exception
    {
        super.init(name, config);
        outName = config.getProperty("out", outName);
    }
    
    // ------------------------------------------------
    // Finalización del proceso -> insertamos resultado
    // ------------------------------------------------
    
    protected void endProcess(Map context)
    {
        context.put(outName, result);
    }        
    
    // ------------------------------------ 
    // Proceso principal: parseamos fichero
    // ------------------------------------ 
    
    @Override
    protected void process(Map context, File srcFile) throws IOException, Exception
    {
        final String EXTENSION = getExtension();
        final int EXTENSION_SIZE = EXTENSION.length();
        
        // Creamos fichero
        String name = srcFile.getName();
        if (!name.endsWith(EXTENSION)) return;
        name = name.substring(0, name.length()-EXTENSION_SIZE);
        
        // Creamos contexto
        System.out.println("Parsing file: " + srcFile.getCanonicalPath());
        result.put(name, read(srcFile));
    }

    protected abstract Object read(File srcFile) throws Exception;
    protected abstract String getExtension();
}
