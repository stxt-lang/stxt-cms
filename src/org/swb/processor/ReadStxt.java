package org.swb.processor;

import java.io.File;
import java.util.List;

import dev.stxt.Node;
import dev.stxt.Parser;

public class ReadStxt extends AbstractRead
{
    @Override
    protected Object read(File srcFile)
    {
        Parser parser = new Parser();
        List<Node> nodes = parser.parseFile(srcFile);
        return nodes.get(0);
    }

    @Override
    protected String getExtension()
    {
        return ".stxt";
    }    
}
