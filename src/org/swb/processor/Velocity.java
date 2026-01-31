package org.swb.processor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.swb.utils.PropertiesUtils;
import org.swb.utils.Utils;
import org.swb.utils.WikiRender;
import org.swb.velocity.VelocityUtils;

public class Velocity extends AbstractDirProcessor
{
    private String extension = "html";
    private String in   = null;
    private String out  = null;
    private Properties model = null;
    private String template;
    private String encoding = "UTF-8";

    @Override
    public void init(String name, Properties config) throws Exception
    {
        // Cargamos inicial
        super.init(name, config);
        
        // Cargamos template
        template = config.getProperty("template");
        if (template == null)
        {
            System.err.println(name + " -> Debe especificar template ");
            System.exit(1);
        }
        
        // Cargamos in y out
        in = config.getProperty("in");
        out = config.getProperty("out");
        encoding = config.getProperty("encoding", encoding);
        extension = config.getProperty("extension", extension);
        model = PropertiesUtils.getSubproperties(config, "model.");
    }
    
    @Override
    public void execute(Map context) throws Exception
    {
        // Ejecutamos
        if (in == null)
        {
            // Validamos que exista out
            if (out == null) throw new IllegalArgumentException("in nullo Necesita propiedad out");
            String result = render(context, null, null);
            context.put(out, result);
            if (todir != null) FileUtils.writeStringToFile(new File(todir, out + '.' + extension), result, encoding); 
        }
        else
        {
            // Obtenemos in
            Object inObject = context.get(in);
            
            // Si es un mapa
            if (inObject != null && inObject instanceof Map)
            {
                // Creamos mapa de salida si es necesario
                Map outMap = null;
                if (out != null) outMap = new LinkedHashMap();
                
                // Vamos recorriendo insertando
                Map<String, Object> inMap = (Map) inObject;
                for (String key: inMap.keySet())
                {
                    String result = render(context, inMap.get(key), key);
                    if (outMap != null) outMap.put(key, result);
                    if (todir != null) FileUtils.writeStringToFile(new File(todir, key + '.' + extension), result, encoding); 
                }
                
                // Si hab�a salida la insertamos en contexto
                if (out != null) context.put(out, outMap);
            }
            else
            {
                // Validamos que exista out
                if (out == null) throw new IllegalArgumentException(inObject.getClass() + " necesita propiedad out");
                String result = render(context, inObject, null);
                context.put(out, result);
                if (todir != null) FileUtils.writeStringToFile(new File(todir, out + '.' + extension), result, encoding); 
            }
        }
    }

    private String render(Map context, Object inObject, String name) throws IOException
    {
        // Creamos context
        Map velocityContext = new HashMap();
        velocityContext.putAll(model);
        velocityContext.putAll(context);
        velocityContext.put("doc", inObject);
        velocityContext.put("doc_name", name);
        velocityContext.put("wiki", new WikiRender());
        velocityContext.put("utils", new Utils());
        
        // Obtenemos índice
        String lang = (String) ((Map) context.get("nav_lang")).get("lang");
        Map pages = (Map) context.get("pages_" + lang);
        Object index = pages.get("_index");
		velocityContext.put("index", index);
        System.out.println("Velocity: " + name + " -> " + lang);
        
        // Renderizamos
        return VelocityUtils.render(template, velocityContext);
    }
}
