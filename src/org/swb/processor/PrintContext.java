package org.swb.processor;

import java.util.Map;
import java.util.Properties;

import org.swb.Processor;

public class PrintContext implements Processor
{
    private String name = null;
    
    @Override
    public void init(String name, Properties config) throws Exception
    {
        this.name = name;
    }

    @Override
    public void execute(Map<String, Object> context) throws Exception
    {
        System.out.println("Context at " + name + ": ");
        if (context.isEmpty()) System.out.println("   --- EMPTY ---");
        for (String key: context.keySet())
        {
            System.out.println("  " + key + " -> " + context.get(key));
        }
    }
}
