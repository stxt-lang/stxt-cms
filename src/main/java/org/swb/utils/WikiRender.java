package org.swb.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension; // Importar la extensión de tablas

import java.util.Arrays;
import java.util.List;

public class WikiRender
{
    public static String renderOld2(String wikiText)
    {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(wikiText);
        HtmlRenderer renderer = HtmlRenderer.builder().softbreak("<br>").build();
        String html = renderer.render(document);
        return html;
    }
    
    public static String renderOld(String markdown) {
        // Paso 1: Preprocesar el Markdown
        String processedMarkdown = markdown.replaceAll("\\\\\\n", "@@BR@@");  // Reemplaza `\n` con un marcador único

        // Crear el parser y renderer de Commonmark
        Parser parser = Parser.builder().build();
        Node document = parser.parse(processedMarkdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        // Convertir el documento Markdown a HTML
        String html = renderer.render(document);

        // Paso 2: Postprocesar el HTML para reemplazar el marcador por <br>
        String finalHtml = html.replace("@@BR@@", "<br>");

        return finalHtml;
    }
    public static String render(String markdown) {
        String processedMarkdown = markdown.replaceAll("\\\\\\n", "@@BR@@");  // Reemplaza `\n` con un marcador único
        
        // Crear una lista de extensiones con la extensión de tablas
        List extensions = Arrays.asList(TablesExtension.create());
        
        // Crear el parser y renderer con las extensiones habilitadas
        Parser parser = Parser.builder()
                .extensions(extensions)  // Agregar la extensión de tablas en el parser
                .build();
        
        Node document = parser.parse(processedMarkdown);  // Parsear el Markdown
        
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(extensions)  // Agregar la extensión de tablas en el renderer
                .build();

        // Renderizar el documento Markdown a HTML
        String html = renderer.render(document);
        
        // Paso 2: Postprocesar el HTML para reemplazar el marcador por <br>
        String finalHtml = html.replace("@@BR@@", "<br>");

        return finalHtml;
    }    
    
    public static String renderNoP(String markdown)
    {
    	String result = render(markdown).trim();
    	if (result.startsWith("<p>") && result.endsWith("</p>"))
    	{
    		result = result.substring(3, result.length()-4);
    	}
    	return result;
    }
    
    public static void main(String[] args)
    {
        String wikiText = "# Hello, World!\n Esto es un [enlace](https://www.google.es)\\\nDemo";
        System.out.println("WIKI: ");
        System.out.println(wikiText);
        System.out.println("RENDER: \n\n");
        String result = render(wikiText);
        System.out.println(result);
        
        // Ejemplo de texto con tabla en Markdown
        String table = "| Hora d'Inici | Hora de Final | Activitat                | Durada | Productivitat |\r\n"
                     + "|--------------|---------------|--------------------------|--------|---------------|\r\n"
                     + "| 08:00        | 08:30         | Esmorzar                 | 30 min | Mitjà         |\r\n"
                     + "| 08:30        | 09:00         | Revisió de correus       | 30 min | Baix          |\r\n"
                     + "| 09:00        | 11:00         | Treball en projecte X    | 2 h    | Alt           |\r\n"
                     + "| 11:00        | 11:15         | Pausa per cafè           | 15 min | Mitjà         |\r\n"
                     + "| 11:15        | 12:30         | Reunió d'equip           | 1 h 15 min | Mitjà     |\r\n";

        // Convertir la tabla a HTML usando el método render
        result = render(table);
        System.out.println(result);  // Imprimir el HTML resultante        
    }

}
