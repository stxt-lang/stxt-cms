package org.swb.processor;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.swb.Processor;

public class DeleteDir implements Processor
{
    private File dir = null;
    
    @Override
    public void execute(Map context) throws Exception
    {
        if (dir.exists() && dir.isDirectory())
        {
            System.out.println("Borrando directorio: " + dir.getAbsolutePath());
            FileUtils.deleteDirectory(dir);
            System.out.println("Directorio borrado.");
        }
        else
        {
            System.out.println("Directorio no existe para borrar: " + dir.getCanonicalPath());
        }
    }

    @Override
    public void init(String name, Properties config) throws Exception
    {
        dir = new File(config.getProperty("dir"));
    }
}
