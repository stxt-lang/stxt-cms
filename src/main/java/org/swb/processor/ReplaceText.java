package org.swb.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.swb.utils.PropertiesUtils;

public class ReplaceText extends AbstractDirProcessor
{
    // --------------------------
    // Variables de configuración
    // --------------------------
    
    private String encoding;
    private Properties replaceStrings = new Properties(); 
    
    // ----
    // Init
    // ----
    
    @Override
    public void init(String name, Properties config) throws Exception
    {
        // Configuración super()
        super.init(name, config);
        
        // ContentType
        encoding = config.getProperty("encoding", "UTF-8");
        
        // Validamos directorio
        if (dir == null)
        {
            System.err.println("Debe especificar directorio");
            System.exit(1);
        }
        
        // Características a reemplazar
        replaceStrings = PropertiesUtils.getSubproperties(config, "replace.");
    }
    
    // ----------------
    // Método principal
    // ----------------
    
    @Override
    protected void process(Map context, File srcFile) throws IOException, Exception
    {
        // Obtenemos contenido
        String content = FileUtils.readFileToString(srcFile, encoding);
        
        // Reemplazamos propiedades
        List<String> keys = new ArrayList(replaceStrings.keySet());
        Collections.sort(keys);
        System.out.println("Replace: " + srcFile.getAbsolutePath() + " -> " + keys);
        for (String key: keys)
        {
            content = StringUtils.replace(content, key, replaceStrings.getProperty(key));
        }
        
        // Guardamos fichero
        FileUtils.writeStringToFile(srcFile, content, encoding);
    }
}
