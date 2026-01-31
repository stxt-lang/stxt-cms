package org.swb.processor;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ListFiles extends AbstractDirProcessor
{
    @Override
    protected void process(Map context, File file) throws IOException, Exception
    {
        System.out.println("File: " + file.getCanonicalPath());
    }
}
