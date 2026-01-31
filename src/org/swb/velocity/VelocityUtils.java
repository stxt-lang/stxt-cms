package org.swb.velocity;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class VelocityUtils
{
    protected static VelocityEngine ve = null;
    
    public static void init(String templatePath, String macrosFile)
    {
        // Inicializamos engine
        ve = new VelocityEngine();
        
        // Creamos properties
        Properties props = new Properties();
        props.setProperty("input.encoding", "Cp1252");
        props.setProperty("output.encoding", "UTF-8");
        props.setProperty("runtime.log.logsystem.class", "org.swb.velocity.VelocityLogChute");
        props.setProperty("resource.loader", "file");
        props.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("file.resource.loader.path", templatePath);
        props.setProperty("file.resource.loader.cache", "true");
        props.setProperty("file.resource.loader.modificationCheckInterval", "1");
        
        if (macrosFile != null) props.setProperty("velocimacro.library", macrosFile);
        
        // Inicializamos engine
        ve.init(props);
    }
    
    public static String render(String template, Map model) throws IOException
    {
        StringWriter out = null;
        try
        {
            out = new StringWriter();
            VelocityContext ctx = new VelocityContext(model);
            Template tmp = ve.getTemplate(template); 
            tmp.merge(ctx, out);
            return out.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException(e);
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        System.out.println("Inici");
        
        VelocityUtils.init("web/templates", null);
        String out = VelocityUtils.render("page.vm", null);
        System.out.println("out = " + out);
        
        System.out.println("Fi");
    }

}
