package org.swb.book;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.stxt.InlineNode;
import dev.stxt.Node;
import dev.stxt.Parser;
import dev.stxt.exceptions.ParseException;
import dev.stxt.schema.SchemaValidator;
import dev.stxt.template.TemplateSchemaProviderMemory;

/**
 * Reads a book in the dev.stxt.pdf.book format: the index (book.stxt, a Book with its Part and
 * Include nodes) and every chapter it includes, in order, each validated against the template of
 * the format. Any problem is an {@link IllegalArgumentException} with the file and the line.
 * <p>
 * The web edition writes the pages flat into one folder and the images next to them, so on top of
 * the template it checks that a chapter name and an image name are unique in the whole book, that
 * no chapter is called {@code index} (the page of the table of contents), and that an Image names
 * an existing file of the chapter's own folder.
 */
public final class BookReader
{
    public static final String NAMESPACE = "dev.stxt.pdf.book";
    public static final String INDEX = "index";

    private final TemplateSchemaProviderMemory schemas = new TemplateSchemaProviderMemory();
    private final Map<String, File> chapterFiles = new HashMap<>();
    private final Map<String, File> images = new HashMap<>();

    private BookReader(File template)
    {
        try
        {
            schemas.addTemplate(read(template));
        }
        catch (ParseException e)
        {
            throw error(template, e.getLine(), e.getMessage());
        }
    }

    /**
     * @param index    the index of one language, book/es/book.stxt
     * @param template the template of the format, .stxt/dev.stxt.pdf.book.stxt
     */
    public static Book read(File index, File template)
    {
        return new BookReader(template).readBook(index);
    }

    private Book readBook(File index)
    {
        Book book = new Book(root(index, "book"));
        for (Node partNode: book.getNode().getChildren("part"))
        {
            Book.Part part = new Book.Part(partNode.getText());
            for (Node include: ((InlineNode) partNode).getChildren("include"))
            {
                part.add(chapter(index, include, part));
            }
            book.add(part);
        }
        return book;
    }

    private Book.Chapter chapter(File index, Node include, Book.Part part)
    {
        File file = canonical(new File(index.getParentFile(), include.getText()));
        if (!file.isFile()) throw error(index, include.getLine(), "chapter not found: " + include.getText());
        String name = file.getName();
        if (!name.endsWith(".stxt")) throw error(index, include.getLine(), "a chapter is a .stxt file: " + include.getText());
        name = name.substring(0, name.length() - ".stxt".length());
        if (name.equals(INDEX)) throw error(index, include.getLine(), "a chapter cannot be called " + INDEX);
        if (chapterFiles.put(name, file) != null)
        {
            throw error(index, include.getLine(), "two chapters with the same name: " + name);
        }

        InlineNode node = root(file, "chapter");
        for (Node image: node.getChildren("image")) checkImage(file, image);
        return new Book.Chapter(name, node, part);
    }

    private void checkImage(File chapter, Node image)
    {
        String ref = image.getText();
        if (ref.contains("/") || ref.contains("\\"))
        {
            throw error(chapter, image.getLine(), "an image must be in the folder of its chapter: " + ref);
        }
        File file = canonical(new File(chapter.getParentFile(), ref));
        if (!file.isFile()) throw error(chapter, image.getLine(), "image not found: " + ref);
        File previous = images.put(ref, file);
        if (previous != null && !previous.equals(file))
        {
            throw error(chapter, image.getLine(), "two images with the same name in the book: " + ref);
        }
    }

    /** The only root node of a file, which must be {@code name} in the namespace of the book. */
    private InlineNode root(File file, String name)
    {
        Parser parser = new Parser();
        parser.registerValidator(new SchemaValidator(schemas));
        List<Node> roots;
        try
        {
            roots = parser.parse(read(file));
        }
        catch (ParseException e)
        {
            throw error(file, e.getLine(), e.getCode() + ": " + e.getMessage());
        }
        if (roots.size() != 1 || !(roots.get(0) instanceof InlineNode)
                || !NAMESPACE.equals(roots.get(0).getNamespace()) || !roots.get(0).getCanonicalName().equals(name))
        {
            throw error(file, 1, "the file must have a single root node, " + name + " (" + NAMESPACE + ")");
        }
        return (InlineNode) roots.get(0);
    }

    private static String read(File file)
    {
        try
        {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    private static File canonical(File file)
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            return file.getAbsoluteFile();
        }
    }

    private static IllegalArgumentException error(File file, int line, String message)
    {
        return new IllegalArgumentException(canonical(file) + (line > 0 ? ":" + line : "") + ": " + message);
    }
}
