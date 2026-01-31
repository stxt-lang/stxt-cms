package org.swb.processor;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.swb.utils.WikiRender;

public class ReadWiki extends AbstractRead
{
    private String encoding = "UTF-8";
    
    @Override
    protected Object read(File srcFile) throws Exception
    {
        String wikiText = FileUtils.readFileToString(srcFile, encoding);
        return WikiRender.render(wikiText);
    }

    @Override
    protected String getExtension()
    {
        return ".wiki";
    }
}
