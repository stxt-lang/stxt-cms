package org.swb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class PropertiesLoader
{
    public static Properties loadProperties(File f) throws IOException
    {
        Properties result = null;
        InputStream in = null;
        try
        {
            in = new FileInputStream(f);
            if (in != null)
            {
                result = new Properties();
                result.load(in);
            }
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (Throwable ignore)
                {
                }
            }
        }

        result = trim(result);
        result = replaceValues(result);
        
        return result;
    }



    /* ************** */
    /* Utility method */
    /* ************** */
    
    private static Properties trim(Properties result)
    {
        Set keys = result.keySet();
        for (Object key: keys)
        {
            String k = (String) key;
            result.setProperty(k, result.getProperty(k).trim());
        }
        return result;
    }
    
    private static Properties replaceValues(Properties p)
    {
        Map<String, String> replaceVars = new HashMap<String, String>(); 
        
        Set keys = p.keySet();
        for (Object key: keys)
        {
            String k = (String) key;
            if (k.startsWith("$"))
            {
                replaceVars.put(k, p.getProperty(k));
            }
        }
        
        // Obtenemos replace vars
        Set<String> repKeys = replaceVars.keySet();
        
        // Si hay replace -> reemplazamos
        if (repKeys.size()>0)
        {
            // Creamos claves
            List<String> repKeysList = new ArrayList<String>(replaceVars.keySet());
            
            Collections.sort(repKeysList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2)
                {
                    if (o1.length()>o2.length()) return -1;
                    else if (o1.length()==o2.length()) return 0;
                    else return 1;
                }
            });
            
            // Eliminamos todas las variables
            for (String key: repKeysList) p.remove(key);   

            // Vamos iternado substituyendo
            keys = p.keySet();
            for (Object key: keys)
            {
                String k = (String) key;
                String value = p.getProperty(k);
                value = replace(value, repKeysList, replaceVars);
                p.setProperty(k, value);
            }            
        }
        
        // Retorno del modificado
        return p;
    }

    private static String replace(String value, List<String> repKeysList, Map<String, String> replaceVars)
    {
        for (String key: repKeysList)
        {
            String valueToReplace = replaceVars.get(key);
            value = StringUtils.replace(value, key, valueToReplace);
        }
        return value;
    }
    
}

