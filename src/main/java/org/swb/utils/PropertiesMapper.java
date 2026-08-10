package org.swb.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class PropertiesMapper
{
    private static final String POST_SET        = "__SET";
    private static final String POST_LIST       = "__LIST";
    
    private static final String POST_INTEGER    = "__INTEGER";
    private static final String POST_DOUBLE     = "__DOUBLE";
    private static final String POST_FLOAT      = "__FLOAT";
    private static final String POST_LONG       = "__LONG";
    private static final String POST_BOOLEAN    = "__BOOLEAN";
    
    private static final String POST_KEYS       = "__KEY_ORDER";
    
    public static Map propertiesToMap(Properties props) throws IOException
    {
        // Creamos var de resultado
        Map result = null;
        List<String> keysOrder = null;
        if (props.containsKey(POST_KEYS))
        {
            // Creamos resultado con claves de antemano
            result = new LinkedHashMap();
            
            // Obtenemos claves
            keysOrder = getClaves(props.getProperty(POST_KEYS));
            
            // Vamos recorriendo claves insertando para tener un orden inicial
            for (String clave: keysOrder) result.put(clave, null);
            
            // Eliminamos __KEYS
            props.remove(POST_KEYS);
        }
        else
        {
            result = new HashMap();
        }
        
        // Vamos recorriendo propiedades insertando
        // Map de subpropiedades
        Map subproperties = new HashMap();
        
        // Buscamos claves
        Set keys = props.keySet();
        for (Object keyO: keys)
        {
            // Obtenemos clave y miramos
            String key = (String)keyO;
            
            // Si no hay partes -> propiedades directas
            int index = key.indexOf('.'); 
            if (index == -1)
            {
                if (key.endsWith(POST_LIST))
                {
                    List list = getList(props, key);
                    list = Collections.unmodifiableList(list);
                    result.put(realKey(key, POST_LIST), list);
                }
                else if (key.endsWith(POST_SET))
                {
                    Set set = getSet(props, key);
                    set = Collections.unmodifiableSet(set);
                    result.put(realKey(key,POST_SET), set);
                }
                else if (key.endsWith(POST_INTEGER))
                {
                    result.put(realKey(key,POST_INTEGER), Integer.parseInt(props.getProperty(key)));
                }
                else if (key.endsWith(POST_DOUBLE))
                {
                    result.put(realKey(key,POST_DOUBLE), Double.parseDouble(props.getProperty(key)));
                }
                else if (key.endsWith(POST_FLOAT))
                {
                    result.put(realKey(key,POST_FLOAT), Float.parseFloat(props.getProperty(key)));
                }
                else if (key.endsWith(POST_LONG))
                {
                    result.put(realKey(key,POST_LONG), Long.parseLong(props.getProperty(key)));
                }
                else if (key.endsWith(POST_BOOLEAN))
                {
                    result.put(realKey(key,POST_BOOLEAN), Boolean.parseBoolean(props.getProperty(key)));
                }
                else if (key.endsWith(POST_KEYS))
                {
                    // Nada es sólo para orden!!
                }
                else
                {
                    result.put(key, props.getProperty(key));
                }
            }
            else // Miramos subpropiedades
            {
                String parte = key.substring(0,index+1);
                String keysSubprop = key.substring(0, index) + POST_KEYS;
                String prop = key.substring(0,index);
                if (!result.containsKey(prop) || result.get(prop)==null)
                {
                    Properties subprops = PropertiesUtils.getSubproperties(props, parte);
                    Map subpropsMap = propertiesToMap(subprops);
                    
                    if (props.containsKey(keysSubprop))
                    {
                        List<String> claves = getClaves(props.getProperty(keysSubprop));
                        Map finalMap = new LinkedHashMap();
                        for (String clave: claves) finalMap.put(clave, null);
                        finalMap.putAll(subpropsMap);
                        subpropsMap = Collections.unmodifiableMap(finalMap);
                        
                        check(claves, subpropsMap);
                    }
                    
                    result.put(prop, subpropsMap);
                }
            }
        }
        
        // Insertamos subpropiedades
        result.putAll(subproperties);
        
        // Si había claves comprobamos que todo ok
        if (keysOrder != null)
        {
            check(keysOrder, result);
        }
        
        // Retornamos mapa no modificable
        return Collections.unmodifiableMap(result);
    }

    // -------------------
    // Métodos utilitarios
    // -------------------
    
    private static String realKey(String key, String postFix)
    {
        return key.substring(0, key.length()-postFix.length());
    }


    private static void check(List<String> keysOrder, Map result)
    {
        // Validamos si hay claves no incluidas en listado
        Set keys = new HashSet(result.keySet());
        keys.removeAll(keysOrder);
        if (keys.size()>0)
        {
            String error = "Faltan por incluir en listado: " + keys; 
            //throw new IllegalArgumentException(error);
            System.out.println(error);
        }
        
        // Validamos que aparezcan todas las informadas
        keys = result.keySet();
        Set clavesNulas = new HashSet();
        for (Object key: keys)
        {
            Object value = result.get(key);
            if (value == null) clavesNulas.add(key);
        }
        if (clavesNulas.size()>0)
        {
            String error = "Claves incorrectas. No definidas: " + clavesNulas;
            throw new IllegalArgumentException(error);
        }
    }

    private static List<String> getClaves(String property)
    {
        String[] claves = property.split(",");
        List<String> result = new ArrayList<String>();
        for (String clave: claves)
        {
            clave = StringUtils.trimToNull(clave);
            if (clave != null) result.add(clave);
        }
        return result;
    }
    
    private static List<String> getList(Properties props, String prop)
    {
        List<String> result = new ArrayList<String>();
        
        // Buscamos propiedad
        String propList = props.getProperty(prop);
        if (propList == null) return result;
        
        // Dividimos por comas
        String[] parts = propList.split(",");
        for (String part: parts)
        {
            part = StringUtils.trimToNull(part);
            if (part != null) result.add(part);
        }

        // Retornamos el resultado
        return result;
    }
    
    private static Set<String> getSet(Properties props, String prop)
    {
        Set<String> result = new LinkedHashSet<String>();
        
        // Buscamos propiedad
        String propList = props.getProperty(prop);
        if (propList == null) return result;
        
        // Dividimos por comas
        String[] parts = propList.split(",");
        for (String part: parts)
        {
            part = StringUtils.trimToNull(part);
            if (part != null) result.add(part);
        }

        // Retornamos el resultado
        return result;
    }
    
}