package org.swb;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.swb.utils.PropertiesLoader;
import org.swb.utils.PropertiesUtils;

public class Executor
{
    public static void main(String[] args) throws Exception
    {
        // Obtenemos file
        String file = "processor.properties";
        String command = "main";
        if (args.length>0) file = args[0];
        if (args.length>1) command = args[1];
        
        // Miramos que exista
        File f = new File(file);
        if (!f.exists()||!f.isFile())
        {
            System.out.println("No se ha encontrado el fichero de ejecuci�n: " + f.getAbsolutePath());
            System.exit(1);
        }
        
        // Cargamos propiedades
        Properties p = PropertiesLoader.loadProperties(f);
        
        // Ejecutamos commando
        exec(p, command);
    }

    private static void exec(Properties p, String commands) throws Exception
    {
        // Ejecutamos command
        System.out.println("Execute: " + commands);
        
        // Buscamos comandos
        String commandsList = p.getProperty(commands);
        if (commandsList == null) throw new IllegalArgumentException("Lista de comandos no encontrada: " + commands);
        
        // Dividimos commandos
        String[] commandsArray = commandsList.split(",");
        List<Processor> processors = new ArrayList<>();
        long time = System.currentTimeMillis();
        for (String cmd: commandsArray)
        {
            Processor processor = createProcessor(cmd.trim(), p);
            if (processor != null) processors.add(processor);
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Time creation: " + time + " ms");
        
        // Creamos contexto y ejecutamos
        Map<String, Object> context = new LinkedHashMap<>();
        time = System.currentTimeMillis();
        for (Processor processor: processors) processor.execute(context);
        time = System.currentTimeMillis() - time;
        System.out.println("Time execution: " + time + " ms");
        
        // Fin ejecuci�n
        System.out.println("End execution: " + commands);
    }

    private static Processor createProcessor(String cmd, Properties p) throws Exception
    {
        // Validamos no nulo
        if (cmd.equals("")) return null;
        
        // Obtenemos subproperties
        String type = p.getProperty(cmd);
        p = PropertiesUtils.getSubproperties(p, cmd + ".");
        
        // Creamos objeto
        Processor result = (Processor) Executor.class.getClassLoader().loadClass("org.swb.processor." + type).newInstance();
        System.out.println("Init: " + cmd);
        result.init(cmd, p);
        return result;
    }
}
