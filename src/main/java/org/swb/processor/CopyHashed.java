package org.swb.processor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.swb.utils.Utils;

/**
 * Copies each matching file to "todir" with its content hash in the name ("css/site.css"
 * is written as "css/site.1be47509cb.css", see Utils.assetPath) and deletes the copies
 * left by earlier builds of the same file ("css/site.[other hash].css"), so the output
 * holds exactly one version of every hashed asset. The templates get the same name from
 * $utils.assetPath(); both sides use Utils.hashFile.
 */
public class CopyHashed extends AbstractDirProcessor
{
    private int lengthBaseDir = 0;

    @Override
    public void init(String name, Properties config) throws Exception
    {
        super.init(name, config);
        if (dir == null || todir == null) throw new IllegalArgumentException(name + ": 'dir' and 'todir' are required");
        lengthBaseDir = dir.getCanonicalPath().length();
    }

    @Override
    protected void startProcess(Map<String, Object> context) throws IOException
    {
        System.out.println("Copy hashed start: " + dir.getCanonicalPath() + " to " + todir.getCanonicalPath());
    }

    @Override
    protected void endProcess(Map<String, Object> context) throws IOException
    {
        System.out.println("Copy hashed end.");
    }

    @Override
    protected void process(Map<String, Object> context, File srcFile) throws IOException, Exception
    {
        String relative = srcFile.getCanonicalPath().substring(lengthBaseDir).replace('\\', '/');
        String hash = Utils.hashFile(srcFile);
        File dstFile = new File(todir, Utils.hashedName(relative, hash));
        deleteStale(dstFile, srcFile.getName());

        System.out.println((dstFile.exists() ? "Overwrite: " : "Copy: ") + srcFile.getCanonicalPath() + " to " + dstFile.getCanonicalPath());
        FileUtils.copyFile(srcFile, dstFile);
    }

    /** Deletes the "name.[hash].ext" siblings of dstFile whose hash is not the current one. */
    private static void deleteStale(File dstFile, String plainName)
    {
        File parent = dstFile.getParentFile();
        File[] siblings = parent == null ? null : parent.listFiles();
        if (siblings == null) return;

        int dot = plainName.lastIndexOf('.');
        String base = dot <= 0 ? plainName : plainName.substring(0, dot);
        String ext = dot <= 0 ? "" : plainName.substring(dot);
        Pattern stale = Pattern.compile(Pattern.quote(base) + "\\.[0-9a-f]{" + Utils.HASH_LENGTH + "}" + Pattern.quote(ext));

        for (File sibling: siblings)
        {
            if (sibling.isFile() && !sibling.equals(dstFile) && stale.matcher(sibling.getName()).matches())
            {
                System.out.println("Delete stale: " + sibling.getAbsolutePath());
                if (!sibling.delete()) System.err.println("Could not delete: " + sibling.getAbsolutePath());
            }
        }
    }
}
