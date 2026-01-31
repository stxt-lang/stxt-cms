package org.swb.processor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.swb.Processor;
import org.swb.utils.AntPathMatcher;

public abstract class AbstractDirProcessor implements Processor
{
    protected Properties config;
    protected String nameCommand;
	protected File dir;
    protected File todir;
    protected String filter;

    @Override
    public void init(String name, Properties config) throws Exception
    {
        // Init properties
        this.config = config;
        this.nameCommand = name;
        
        // Miramos directorio de entrada
        String cDir = config.getProperty("dir");
        if (cDir != null)
        {
            dir = new File(cDir);
            if (!dir.exists() || !dir.isDirectory()) throw new IllegalArgumentException("Directorio no existe: " + dir.getAbsolutePath());
        }
        
        // Miramos directorio de salida
        String cTodir = config.getProperty("todir");
        if (cTodir != null)
        {
            todir = new File(cTodir);
            if (todir.exists() && todir.isFile()) throw new IllegalArgumentException("Directorio " + todir.getAbsolutePath() + " no puede ser un fichero");
            if (!todir.exists()) todir.mkdirs();
        }
        
        // Miramos filter
        this.filter = config.getProperty("filter");
    }
    
    @Override
    public void execute(Map context) throws Exception 
	{
        // Si directorio es distinto de nulo procesamos entrada
		if (dir != null) 
		{
            // Log
		    startProcess(context);
            
            // Obtenemos ficheros de origen
            Collection<File> files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            int dirAbs = dir.getCanonicalPath().length();
    		for (File srcFile: files) 
    		{
    		    if (filter != null)
    		    {
    		        String path = srcFile.getCanonicalPath().substring(dirAbs).replace('\\', '/');
    		        if (!AntPathMatcher.match(filter, path))
    		        {
    		            //System.out.println("Not match: " + filter + " -> " + path);
    		            continue;
    		        }
    		        else
    		        {
    		            //System.out.println("Match: " + filter + " -> " + path);
    		        }
    		    }
    			process(context, srcFile);
    		}
    		
    		// Log
    		endProcess(context);
		}
	}
    
    protected void startProcess(Map context) throws IOException
    {
        System.out.println("Processing: " + dir.getAbsolutePath());
    }
    protected void endProcess(Map context) throws IOException
    {
        System.out.println("End processing: " + dir.getAbsolutePath());
    }
    protected void process(Map context, File srcFile) throws IOException, Exception
    {
        System.out.println("Processing: " + srcFile.getCanonicalPath());
    }
}
