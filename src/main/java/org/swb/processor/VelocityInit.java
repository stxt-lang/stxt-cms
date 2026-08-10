package org.swb.processor;

import java.util.Map;
import java.util.Properties;

import org.swb.Processor;
import org.swb.velocity.VelocityUtils;

public class VelocityInit implements Processor
{
    @Override
    public void init(String name, Properties config) throws Exception
    {
        String templatePath = config.getProperty("template_path");
        String macrosFile = config.getProperty("macros_file");
        
        System.out.println("Velocity init template path: " + templatePath);
        System.out.println("Velocity init macros file: " + macrosFile);
        VelocityUtils.init(templatePath, macrosFile);
    }

    @Override
    public void execute(Map<String, Object> context) throws Exception
    {
    }
}
