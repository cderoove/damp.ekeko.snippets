/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.io.*;

/**
 * A collection of file-processing util methods
 */
public class FileUtil {
    /**
     * Delete a directory and all its contents.  If
     * we return false, the directory may be partially-deleted.
     */
    public static boolean fullyDelete(File dir) throws IOException {
        File contents[] = dir.listFiles();
        if (contents != null) {
            for (int i = 0; i < contents.length; i++) {
                if (contents[i].isFile()) {
                    if (! contents[i].delete()) {
                        throw new IOException("Could not delete " + contents[i].getPath());
                    }
                } else {
                    fullyDelete(contents[i]);
                }
            }
        }
        return dir.delete();
    }

    /**
     * Copy a file's contents to a new location.
     * Returns whether a target file was overwritten
     */
    public static boolean copyContents(File src, File dst, boolean overwrite) throws IOException {
        if (dst.exists() && !overwrite) {
            return false;
        }

        File dstParent = dst.getParentFile();
        if (! dstParent.exists()) {
            dstParent.mkdirs();
        }
        DataInputStream in = new DataInputStream(new FileInputStream(src));
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(dst));
            byte buf[] = new byte[2048];
            try {
                int readBytes = in.read(buf);

                while (readBytes >= 0) {
                    out.write(buf, 0, readBytes);
                    readBytes = in.read(buf);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
        return true;
    }

    /**
     * Copy a file and/or directory and all its contents (whether
     * data or other files/dirs)
     */
    public static void recursiveCopy(File src, File dst) throws IOException {
        //
        // Resolve the real target.
        //
        if (dst.exists() && dst.isDirectory()) {
            dst = new File(dst, src.getName());
        } else if (dst.exists()) {
            throw new IOException("Destination " + dst + " already exists");
        }

        //
        // Copy the items
        //
        if (! src.isDirectory()) {
            //
            // If the source is a file, then just copy the contents
            //
            copyContents(src, dst, true);
        } else {
            //
            // If the source is a dir, then we need to copy all the subfiles.
            //
            dst.mkdirs();
            File contents[] = src.listFiles();
            for (int i = 0; i < contents.length; i++) {
                recursiveCopy(contents[i], new File(dst, contents[i].getName()));
            }
        }
    }
}
