package org.swb.processor;

import java.io.File;
import java.io.IOException;

import org.swb.utils.PropertiesLoader;

public class ReadProperties extends AbstractRead
{
    @Override
    protected Object read(File srcFile) throws IOException
    {
        return PropertiesLoader.loadProperties(srcFile);
    }

    @Override
    protected String getExtension()
    {
        return ".properties";
    }    
}
