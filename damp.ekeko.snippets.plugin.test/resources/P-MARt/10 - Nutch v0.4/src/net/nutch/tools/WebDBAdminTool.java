/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.tools;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import net.nutch.db.*;
import net.nutch.util.*;
import net.nutch.linkdb.*;
import net.nutch.pagedb.*;
import net.nutch.pagedb.*;

/******************************************
 * The WebDBAdminTool is for Nutch administrators
 * who need special access to the webdb.  It allows
 * for finer editing of the stored values.
 *
 * @author Mike Cafarella
 ******************************************/
public class WebDBAdminTool {
    public static final Logger LOG = LogFormatter.getLogger("net.nutch.tools.WebDBAdminTool");

    IWebDBReader reader;

    public WebDBAdminTool(IWebDBReader reader) {
        this.reader = reader;
    }

    /**
     * Emit the webdb to 2 text files.
     */
    public void textDump(String dumpName) throws IOException {
        //
        // First the pages
        //
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(dumpName + ".pages"))));
        try {
            for (Enumeration e = reader.pages(); e.hasMoreElements(); ) {
                Page p = (Page) e.nextElement();
                out.println(p.toTabbedString());
            }
        } finally {
            out.close();
        }

        //
        // Then the links
        //
        out = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(dumpName + ".links"))));
        try {
            for (Enumeration e = reader.links(); e.hasMoreElements(); ) {
                Link l = (Link) e.nextElement();
                out.println(l.toTabbedString());
            }
        } finally {
            out.close();
        }                                    
    }

    /**
     * Emit the top K-rated Pages.
     */
    public void emitTopK(int k) throws IOException {
        // Create a sorted list
        SortedSet topSet = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                Page p1 = (Page) o1;
                Page p2 = (Page) o2;
                if (p1.getScore() < p2.getScore()) {
                    return -1;
                } else if (p1.getScore() == p2.getScore()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }
            );

        // Find the top k elts
        Page lowestPage = null;
        for (Enumeration e = reader.pages(); e.hasMoreElements(); ) {
            Page curPage = (Page) e.nextElement();
                    
            if (topSet.size() < k) {
                topSet.add(curPage);
                lowestPage = (Page) topSet.first();
            } else if (lowestPage.getScore() < curPage.getScore()) {
                topSet.remove(lowestPage);
                topSet.add(curPage);
                lowestPage = (Page) topSet.first();
            }
        }
            
        // Print them out
        int i = 0;
        for (Iterator it = topSet.iterator(); it.hasNext(); i++) {
            LOG.info("Page " + i + ": " + (Page) it.next());
        }
    }

    /**
     * Emit each page's score and link data
     */
    public void scoreDump() throws IOException {
        for (Enumeration e = reader.pages(); e.hasMoreElements(); ) {
            Page p = (Page) e.nextElement();
            Link links[] = reader.getLinks(p.getURL());
            int numLinks = 0;
            if (links != null) {
                numLinks = links.length;
            }

            LOG.info(p.getURL() + "\t" + p.getScore() + "\t" + numLinks);
        }
    }

    /**
     * This tool performs a number of generic db management tasks.
     * Right now, it only emits the text-format database.
     */
    public static void main(String argv[]) throws FileNotFoundException, IOException {
        if (argv.length < 2) {
            System.out.println("Usage: java net.nutch.tools.WebDBAdminTool db [-create] [-textdump dumpPrefix] [-scoredump] [-top k]");
            return;
        }

        String dir = argv[0];
        boolean create = false;
        int k = 0;

        String command = null, dumpName = null;
        for (int i = 1; i < argv.length; i++) {
            if ("-create".equals(argv[i])) {
                command = argv[i];
                create = true;
            } else if ("-textdump".equals(argv[i])) {
                command = argv[i];
                i++;
                dumpName = argv[i];
            } else if ("-top".equals(argv[i])) {
                command = argv[i];
                i++;
                k = Integer.parseInt(argv[i]);
            } else if ("-scoredump".equals(argv[i])) {
                command = argv[i];
            }
        }

        //
        // For db creation
        //
        if ("-create".equals(command)) {
            WebDBWriter.createWebDB(new File(dir));
            LOG.info("Created webdb at " + dir);
            return;
        }

        //
        // For other functions
        //
        IWebDBReader reader = new WebDBReader(new File(dir));
        try {
            WebDBAdminTool admin = new WebDBAdminTool(reader);
            if ("-textdump".equals(command)) {
                admin.textDump(dumpName);
            } else if ("-top".equals(command)) {
                admin.emitTopK(k);
            } else if ("-scoredump".equals(command)) {
                admin.scoreDump();
            }
        } finally {
            reader.close();
        }
    }
}
