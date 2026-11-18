package org.swb.processor;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.swb.Processor;
import org.swb.book.Book;
import org.swb.book.BookReader;

/**
 * Reads a book in the dev.stxt.pdf.book format (the text of stxt-pdf-book) with {@link BookReader}
 * and puts it in the context for the templates under templates/book/.
 * <p>
 * Configuration:
 * <ul>
 * <li>{@code file}: the index of one language, book/es/book.stxt.</li>
 * <li>{@code template}: the template of the format, .stxt/dev.stxt.pdf.book.stxt.</li>
 * <li>{@code out}: context key of the {@link Book} (default {@code book}).</li>
 * <li>{@code pages}: context key of the page map for the Velocity processor (default
 *     {@code book_pages}): {@code index} is the Book node (the table of contents), then each
 *     chapter by its page name, the file name without .stxt.</li>
 * </ul>
 */
public class ReadBook implements Processor
{
    private File file;
    private File template;
    private String out = "book";
    private String pages = "book_pages";

    @Override
    public void init(String name, Properties config) throws Exception
    {
        file = required(name, config, "file");
        template = required(name, config, "template");
        out = config.getProperty("out", out);
        pages = config.getProperty("pages", pages);
    }

    private static File required(String name, Properties config, String key)
    {
        String path = config.getProperty(key);
        if (path == null) throw new IllegalArgumentException(name + ": missing property " + key);
        File f = new File(path);
        if (!f.isFile()) throw new IllegalArgumentException(name + ": file not found: " + f.getAbsolutePath());
        return f;
    }

    @Override
    public void execute(Map<String, Object> context) throws Exception
    {
        System.out.println("Reading book: " + file.getCanonicalPath());
        Book book = BookReader.read(file, template);

        Map<String, Object> pageMap = new LinkedHashMap<>();
        pageMap.put(BookReader.INDEX, book.getNode());
        for (Book.Chapter chapter: book.getChapters()) pageMap.put(chapter.getName(), chapter.getNode());

        context.put(out, book);
        context.put(pages, pageMap);
        System.out.println("Book: " + book.getChapters().size() + " chapters in " + book.getParts().size() + " parts");
    }
}
