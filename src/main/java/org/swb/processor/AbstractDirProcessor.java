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
    protected String filter;   // comma-separated Ant patterns; a file must match one of them
    protected String exclude;  // comma-separated Ant patterns; a file matching any is skipped

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
        this.exclude = config.getProperty("exclude");
    }

    /** True if the path (relative to dir, with '/' separators) matches any of the comma-separated Ant patterns. */
    private static boolean matchesAny(String patterns, String path)
    {
        for (String pattern: patterns.split(","))
        {
            pattern = pattern.trim();
            if (!pattern.isEmpty() && AntPathMatcher.match(pattern, path)) return true;
        }
        return false;
    }
    
    @Override
    public void execute(Map<String, Object> context) throws Exception 
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
    		    if (filter != null || exclude != null)
    		    {
    		        String path = srcFile.getCanonicalPath().substring(dirAbs).replace('\\', '/');
    		        if (filter != null && !matchesAny(filter, path)) continue;
    		        if (exclude != null && matchesAny(exclude, path)) continue;
    		    }
    			process(context, srcFile);
    		}
    		
    		// Log
    		endProcess(context);
		}
	}
    
    protected void startProcess(Map<String, Object> context) throws IOException
    {
        System.out.println("Processing: " + dir.getAbsolutePath());
    }
    protected void endProcess(Map<String, Object> context) throws IOException
    {
        System.out.println("End processing: " + dir.getAbsolutePath());
    }
    protected void process(Map<String, Object> context, File srcFile) throws IOException, Exception
    {
        System.out.println("Processing: " + srcFile.getCanonicalPath());
    }
}
