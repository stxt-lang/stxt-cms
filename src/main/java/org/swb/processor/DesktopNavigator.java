package org.swb.processor;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import org.swb.Processor;

public class DesktopNavigator implements Processor
{
    // Variable interna
    private URI uri = null;
    
    @Override
    public void init(String name, Properties config) throws Exception
    {
    	if (config.containsKey("uri"))	uri = new URI(config.getProperty("uri"));
    	else if (config.containsKey("file")) uri = new URI("file:///" + (new File(config.getProperty("file")).getCanonicalPath().replace('\\', '/')));
        
        System.out.println("INIT Desktop Navigagor with uri = " + uri);
    }

    @Override
    public void execute(Map<String, Object> context) throws Exception
    {
        Desktop.getDesktop().browse(uri);
    }
}
