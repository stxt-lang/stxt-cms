package org.swb.utils;

import java.util.Properties;
import java.util.Set;

public class PropertiesUtils
{
    public static Properties getSubproperties(Properties props, String prefix)
    {
        Properties result = new Properties();
        int index = prefix.lastIndexOf(".");
        
        Set keys = props.keySet();
        for (Object o: keys)
        {
            // Get key
            String key = (String) o;
            
            // CheckProperty
            if (key.startsWith(prefix))
            {
                // Get value
                String value = props.getProperty(key);
                
                // Change key
                if (index != -1) key = key.substring(index+1);

                // Insert prop
                result.setProperty(key, value);
            }
        }
        return result;
    }
    public static String makeRealPath(String name, String parentName)
    {
        if (!name.startsWith("/"))
        {
            int i = parentName.lastIndexOf('/');
            name = parentName.substring(0, i+1) + name;
        }
        return name;
    }


}
