package org.swb.processor;

import java.util.Map;

import org.swb.Processor;

public class InsertProperties implements Processor
{
    private String name = null;
    private java.util.Properties config = null;
    
    @Override
    public void init(String name, java.util.Properties config) throws Exception
    {
        this.name = name;
        this.config = config;
        
        if (config.containsKey("out")) this.name = config.getProperty("out");
    }

    @Override
    public void execute(Map<String, Object> context) throws Exception
    {
        context.put(name, config);
    }
}
