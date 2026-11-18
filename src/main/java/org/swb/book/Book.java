package org.swb.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.stxt.InlineNode;
import dev.stxt.Node;

/**
 * A book in the dev.stxt.pdf.book format (the text of stxt-pdf-book), as the templates under
 * templates/book/ read it: its title, its parts and its chapters in reading order. Built by the
 * ReadBook processor; the chapters keep their parsed node, which the templates walk like a page.
 */
public final class Book
{
    private final InlineNode node;
    private final List<Part> parts = new ArrayList<>();
    private final List<Chapter> chapters = new ArrayList<>();

    Book(InlineNode node)
    {
        this.node = node;
    }

    void add(Part part)
    {
        parts.add(part);
        chapters.addAll(part.getChapters());
    }

    /** The Book node of the index (book.stxt). */
    public InlineNode getNode() { return node; }

    public String getTitle() { return value(node, "title"); }
    public String getSubtitle() { return value(node, "subtitle"); }
    public String getLanguage() { return value(node, "language"); }
    public String getReference() { return value(node, "reference"); }

    public List<Part> getParts() { return Collections.unmodifiableList(parts); }

    /** Every chapter of every part, in reading order. */
    public List<Chapter> getChapters() { return Collections.unmodifiableList(chapters); }

    /** The chapter with that page name, or null (the index page, for instance). */
    public Chapter getChapter(String name)
    {
        for (Chapter chapter: chapters) if (chapter.getName().equals(name)) return chapter;
        return null;
    }

    /** The chapter before this page in reading order; null for the index and the first chapter. */
    public Chapter previous(String name)
    {
        int i = indexOf(name);
        return i > 0 ? chapters.get(i - 1) : null;
    }

    /** The chapter after this page in reading order; the index is followed by the first chapter. */
    public Chapter next(String name)
    {
        int i = indexOf(name);
        if (i < 0) return chapters.isEmpty() ? null : chapters.get(0);
        return i + 1 < chapters.size() ? chapters.get(i + 1) : null;
    }

    private int indexOf(String name)
    {
        for (int i = 0; i < chapters.size(); i++) if (chapters.get(i).getName().equals(name)) return i;
        return -1;
    }

    static String value(InlineNode node, String child)
    {
        Node found = node.getChild(child);
        return found == null ? null : found.getText();
    }

    /** A part of the book: its title (Part: Parte I. De un texto a un PDF) and its chapters. */
    public static final class Part
    {
        private final String title;
        private final List<Chapter> chapters = new ArrayList<>();

        Part(String title)
        {
            this.title = title;
        }

        void add(Chapter chapter) { chapters.add(chapter); }

        public String getTitle() { return title; }
        public List<Chapter> getChapters() { return Collections.unmodifiableList(chapters); }
    }

    /**
     * A chapter: its page name (the file name without .stxt, 02-el-primer-pdf), its number (2, or
     * the letter of an appendix, A; none for the preface) and its title (the value of Chapter).
     */
    public static final class Chapter
    {
        private final String name;
        private final InlineNode node;
        private final Part part;

        Chapter(String name, InlineNode node, Part part)
        {
            this.name = name;
            this.node = node;
            this.part = part;
        }

        public String getName() { return name; }
        public InlineNode getNode() { return node; }
        public Part getPart() { return part; }
        public String getTitle() { return node.getText(); }
        public String getNumber() { return value(node, "number"); }

        /** An appendix is numbered with a letter (A, B...), a chapter with digits. */
        public boolean isAppendix()
        {
            String number = getNumber();
            return number != null && !number.isEmpty() && !Character.isDigit(number.charAt(0));
        }

        /** Not written yet: it carries the plan (Pending) instead of its text. */
        public boolean isPending() { return node.getChild("pending") != null; }
    }
}
