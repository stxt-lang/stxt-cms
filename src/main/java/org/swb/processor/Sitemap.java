package org.swb.processor;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.swb.Processor;

import dev.stxt.InlineNode;
import dev.stxt.Node;

public class Sitemap implements Processor
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    
    private File todir;
    private String domain;
    private String[] pages;
    private String[] prefix;
    
    // -------------
    // Configuraci�n
    // -------------
    
    @Override
    public void init(String name, Properties config) throws Exception
    {
        // Insertamos dirs
        todir = new File(config.getProperty("todir"));
        todir.mkdirs();
        
        pages = config.getProperty("pages", "").split(",");
        prefix = StringUtils.splitByWholeSeparator(config.getProperty("prefix", ""),",");
        domain = config.getProperty("domain");
    }
    
    // ---------
    // Ejecuci�n
    // ---------
    
    @Override
    public void execute(Map context) throws Exception
    {
        String result = generateSitemap(context);
        FileUtils.writeStringToFile(new File(todir, "sitemap.xml"), result, StandardCharsets.UTF_8.toString());
    }
    
    public String generateSitemap(Map context) throws IOException
    {
        List<Page> pages = new ArrayList<Page>();
        for(int i = 0; i<this.pages.length; i++)
        {
            // Obtenemos mapa de p�ginas a usar
            Map<String, Object> mapPages = (Map) context.get(this.pages[i]);
            String prefix = this.prefix[i];
            
            // Vamos iterando
            for (String page: mapPages.keySet())
            {
                Page p = createPage(mapPages.get(page), page, prefix);
                if (p!=null) pages.add(p);
            }
        }
        
        // Vamos recorriendo recursos insertando
        StringBuffer out = new StringBuffer();
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (Page page: pages)
        {
            String url = getUrl(page); 
            if (url!=null) out.append(url).append('\n');
        }
        out.append("</urlset>");
        
        return out.toString();
    }
    
    private Page createPage(Object object, String name, String prefix) throws IOException
    {
        // The _index pages are navigation, not public content: out of the sitemap
        if (name.equals("_index")) return null;

        // Creamos result
        Page result = new Page();

        // The home page is "/" and "/es/", never "/index" (a 301)
        if (name.equals("index")) result.setUrl("https://" + domain + prefix + '/');
        else result.setUrl("https://" + domain + prefix + '/' + name);

        // <lastmod> from Metadata/Last modif ("last-modif" is its canonical name)
        if (object instanceof InlineNode)
        {
            Node metadata = ((InlineNode) object).getChild("metadata");
            Node lastmodif = metadata instanceof InlineNode
                    ? ((InlineNode) metadata).getChild("last-modif") : null;
            if (lastmodif instanceof InlineNode)
            {
                try
                {
                    result.setLastModif(SDF.parse(((InlineNode) lastmodif).getValue()));
                }
                catch (Exception e)
                {
                    throw new IOException("Error parsing Last modif of: " + name, e);
                }
            }
        }

        // Retorno de resultado
        return result;
    }
    
    private String getUrl(Page page) throws UnsupportedEncodingException
    {
        // Creamos resultado
        StringBuffer result = new StringBuffer();
        
        result.append("<url><loc>").append(page.getUrl()).append("</loc>");
        if (page.getLastModif()!=null)
        {
            result.append("<lastmod>").append(SDF.format(page.getLastModif())).append("</lastmod>");
        }
        result.append("</url>");
        
        return result.toString();
    }

}

class Page
{
    private String url;
    private Date lastModif;

    public String getUrl()
    {
        return url;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }
    public Date getLastModif()
    {
        return lastModif;
    }
    public void setLastModif(Date lastModif)
    {
        this.lastModif = lastModif;
    }
}
