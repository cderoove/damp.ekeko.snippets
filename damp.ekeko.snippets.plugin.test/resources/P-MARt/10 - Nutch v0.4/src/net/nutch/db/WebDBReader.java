/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.db;

import java.io.*;
import java.util.*;
import java.nio.channels.*;

import net.nutch.io.*;
import net.nutch.pagedb.*;
import net.nutch.linkdb.*;

/**********************************************
 * The WebDBReader implements all the read-only
 * parts of accessing our web database.
 * All the writing ones can be found in WebDBWriter.
 *
 * @author Mike Cafarella
 **********************************************/
public class WebDBReader implements IWebDBReader {
    static final Page[] PAGE_RECORDS = new Page[0];
    static final Link[] LINK_RECORDS = new Link[0];

    // filenames
    static final String PAGES_BY_URL = "pagesByURL";
    static final String PAGES_BY_MD5 = "pagesByMD5";
    static final String LINKS_BY_URL = "linksByURL";
    static final String LINKS_BY_MD5 = "linksByMD5";
    static final String STATS_FILE = "stats";

    File dbFile;
    MapFile.Reader pagesByURL, pagesByMD5, linksByURL, linksByMD5;
    long totalPages = 0, totalLinks = 0;
    Vector mapReaders = null, setReaders = null;
    FileInputStream dbReadLockData;
    FileLock dbReadLock;

    /**
     * Open a web db reader for the named directory.
     */    
    public WebDBReader(File dir) throws IOException, FileNotFoundException {
        this.dbFile = new File(dir, "webdb");

        // Obtain read lock on db so writers don't try to 
        // move it out from under us.  This obtains a non-exclusive
        // lock on the directory that holds the dbs (old and new)
        File readLockFile = new File(dir, "dbreadlock");
        readLockFile.createNewFile();
        this.dbReadLockData = new FileInputStream(readLockFile);
        this.dbReadLock = dbReadLockData.getChannel().lock(0L, Long.MAX_VALUE, true);

        // Create tables
        this.pagesByURL = new MapFile.Reader(new File(dbFile, PAGES_BY_URL).getPath(), new UTF8.Comparator());
        this.pagesByMD5 = new MapFile.Reader(new File(dbFile, PAGES_BY_MD5).getPath(), new Page.Comparator());

        this.linksByURL = new MapFile.Reader(new File(dbFile, LINKS_BY_URL).getPath(), new Link.UrlComparator());
        this.linksByMD5 = new MapFile.Reader(new File(dbFile, LINKS_BY_MD5).getPath(), new Link.MD5Comparator());

        // Load in statistics
        File stats = new File(dbFile, STATS_FILE);
        if (stats.exists()) {
            DataInputStream in = new DataInputStream(new FileInputStream(stats));
            try {
                int version = (byte) in.read();
                this.totalPages = in.readLong();
                this.totalLinks = in.readLong();
            } finally {
                in.close();
            }
        }

        // Create vectors so we can GC readers used by 
        // enum() calls.  We do this so we can have multiple
        // simultaneous enum users.  However, since we keep
        // a handle to each one, we're assuming that we don't
        // create too many before WebDBReader.close() is called.
        this.mapReaders = new Vector();
        this.setReaders = new Vector();
    }

    /**
     * Shutdown
     */
    public void close() throws IOException {
        pagesByURL.close();
        pagesByMD5.close();
        linksByURL.close();
        linksByMD5.close();

        for (Enumeration e = mapReaders.elements(); e.hasMoreElements(); ) {
            MapFile.Reader tmp = (MapFile.Reader) e.nextElement();
            tmp.close();
        }
        for (Enumeration e = setReaders.elements(); e.hasMoreElements(); ) {
            SetFile.Reader tmp = (SetFile.Reader) e.nextElement();
            tmp.close();
        }

        // release the lock
        dbReadLock.release();
        dbReadLockData.close();
    }

    /**
     * Get Page from the pagedb with the given URL
     */
    public Page getPage(String url) throws IOException {
        return (Page) pagesByURL.get(new UTF8(url), new Page());
    }

    /**
     * Get Pages from the pagedb according to their
     * content hash.
     */
    public Page[] getPages(MD5Hash md5) throws IOException {
        Vector records = new Vector(3);
        Page p = new Page();
        p.getMD5().set(md5);

        pagesByMD5.seek(p);
        while (pagesByMD5.next(p, NullWritable.get())) {
            if (p.getMD5().compareTo(md5) == 0) {
                records.add(p);
                p = new Page();
            } else {
                break;
            }
        }

        // Xfer from the vector into an array
        return (Page[]) records.toArray(PAGE_RECORDS);
    }

    /**
     * Test whether a certain piece of content is in the
     * database, but don't bother returning the Page(s) itself.
     */
    public boolean pageExists(MD5Hash md5) throws IOException {
        Page p = new Page();
        p.getMD5().set(md5);
        pagesByMD5.seek(p);
        if (pagesByMD5.next(p, NullWritable.get()) && p.getMD5().compareTo(md5) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Iterate through all the Pages, sorted by URL
     */
    public Enumeration pages() throws IOException {
        MapFile.Reader tmpReader = new MapFile.Reader(new File(dbFile, "pagesByURL").getPath());
        mapReaders.add(tmpReader);
        return new TableEnumerator(tmpReader);
    }

    //
    // The TableEnumerator goes through all the entries
    // in the Table (which is a MapFile).
    //
    class TableEnumerator implements Enumeration {
        MapFile.Reader reader;
        Page nextItem;

        /**
         * Start the cursor and find the first item.
         * Store it for later return.
         */
        public TableEnumerator(MapFile.Reader reader) {
            this.reader = reader;
            this.nextItem = new Page();
            try {
                if (! reader.next(new UTF8(), this.nextItem)) {
                    this.nextItem = null;
                }
            } catch (IOException ie) {
                this.nextItem = null;
            }
        }

        /**
         * If there's no item left in store, we've hit the end.
         */
        public boolean hasMoreElements() {
            return (nextItem != null);
        }

        /**
         * Set aside the item we have in store.  Then retrieve
         * another for the next time we're called.  Finally, return
         * the set-aside item.
         */
        public Object nextElement() {
            if (nextItem == null) {
                throw new NoSuchElementException("PageDB Enumeration");
            }
            Page toReturn = nextItem;
            this.nextItem = new Page();
            try {
                if (! reader.next(new UTF8(), nextItem)) {
                    this.nextItem = null;
                }
            } catch (IOException ie) {
                this.nextItem = null;
            }
            return toReturn;
        }
    }


    /**
     * Iterate through all the Pages, sorted by MD5
     */
    public Enumeration pagesByMD5() throws IOException {
        SetFile.Reader tmpReader = new SetFile.Reader(new File(dbFile, "pagesByMD5").getPath());
        setReaders.add(tmpReader);
        return new IndexEnumerator(tmpReader);
    }

    /**
     * Return the number of pages we're dealing with
     */
    public long numPages() {
        return totalPages;
    }

    //
    // The IndexEnumerator goes through all the entries
    // in the index (which is a SequenceFile).
    //
    class IndexEnumerator implements Enumeration {
        SetFile.Reader reader;
        Page nextItem;

        /**
         * Start the cursor and find the first item.
         * Store it for later return.
         */
        public IndexEnumerator(SetFile.Reader reader) {
            this.reader = reader;
            this.nextItem = new Page();
            try {
                if (! reader.next(nextItem)) {
                    this.nextItem = null;
                }
            } catch (IOException ie) {
                this.nextItem = null;
            }
        }

        /**
         * If there's no item left in store, we've hit the end.
         */
        public boolean hasMoreElements() {
            return (nextItem != null);
        }

        /**
         * Set aside the item we have in store.  Then retrieve
         * another for the next time we're called.  Finally, return
         * the set-aside item.
         */
        public Object nextElement() {
            if (nextItem == null) {
                throw new NoSuchElementException("PageDB Enumeration");
            }

            Page toReturn = nextItem;
            this.nextItem = new Page();
            try {
                if (! reader.next(nextItem)) {
                    this.nextItem = null;
                }
            } catch (IOException ie) {
                this.nextItem = null;
            }
            return toReturn;
        }
    }

    /**
     * Get all the hyperlinks that link TO the indicated URL.
     */     
    public Link[] getLinks(UTF8 url) throws IOException {
        Vector records = new Vector(3);
        Link l = new Link();
        l.getURL().set(url);

        linksByURL.seek(l);
        while (linksByURL.next(l, NullWritable.get())) {
            if (url.equals(l.getURL())) {
                records.add(l);
                l = new Link();
            } else {
                break;
            }
        }
        
        // Xfer from the vector into an array
        return (Link[]) records.toArray(LINK_RECORDS);
    }

    /**
     * Grab all the links from the given MD5 hash.
     */
    public Link[] getLinks(MD5Hash md5) throws IOException {
        Vector records = new Vector(3);
        Link l = new Link();
        l.getFromID().set(md5);

        linksByMD5.seek(l);
        while (linksByMD5.next(l, NullWritable.get())) {
            if (md5.equals(l.getFromID())) {
                records.add(l);
                l = new Link();
            } else {
                break;
            }
        }
        
        // Xfer from the vector into an array
        return (Link[]) records.toArray(LINK_RECORDS);
    }

    /**
     * Return all the links, by target URL
     */
    public Enumeration links() {
        return new MapEnumerator(linksByURL);
    }

    /**
     * Return the number of links in our db.
     */
    public long numLinks() {
        return totalLinks;
    }

    //
    // Here's the class for the above function
    //
    class MapEnumerator implements Enumeration {
        MapFile.Reader reader;
        Link nextItem;

        /**
         * Start the cursor and find the first item.
         * Store it for later return.
         */
        public MapEnumerator(MapFile.Reader reader) {
            this.reader = reader;
            this.nextItem = new Link();
            try {
                if (! reader.next(this.nextItem, NullWritable.get())) {
                    this.nextItem = null;
                }
            } catch (IOException ie) {
                this.nextItem = null;
            }
        }

        /**
         * If there's no item left in store, we've hit the end.
         */
        public boolean hasMoreElements() {
            return (nextItem != null);
        }

        /**
         * Set aside the item we have in store.  Then retrieve
         * another for the next time we're called.  Finally, return
         * the set-aside item.
         */
        public Object nextElement() {
            if (nextItem == null) {
                throw new NoSuchElementException("PageDB Enumeration");
            }

            Link toReturn = nextItem;
            this.nextItem = new Link();
            try {
                if (! reader.next(nextItem, NullWritable.get())) {
                    this.nextItem = null;
                }
            } catch (IOException ie) {
                this.nextItem = null;
            }
            return toReturn;
        }
    }

    /**
     * The WebDBReader.main() provides some handy utility methods
     * for looking through the contents of the webdb.  Hoo-boy!
     */
    public static void main(String argv[]) throws FileNotFoundException, IOException {
        if (argv.length < 2) {
            System.out.println("Usage: java net.nutch.db.WebDBReader <db> [-pageurl url] | [-pagemd5 md5] | [-dumppageurl] | [-dumppagemd5] | [-toppages <k>] | [-linkurl url] | [-linkmd5 md5] | [-dumplinks] | [-stats]");
            return;

        }

        WebDBReader reader = new WebDBReader(new File(argv[0]));
        try {
            if ("-pageurl".equals(argv[1])) {
                String url = argv[2];
                System.out.println(reader.getPage(url.trim()));
            } else if ("-pagemd5".equals(argv[1])) {
                MD5Hash md5 = new MD5Hash(argv[2]);
                Page pages[] = reader.getPages(md5);
                System.out.println("Found " + pages.length + " pages.");
                for (int i = 0; i < pages.length; i++) {
                    System.out.println("Page " + i + ": " + pages[i]);
                }
            } else if ("-dumppageurl".equals(argv[1])) {
                System.out.println(reader);
                System.out.println();
                int i = 1;
                for (Enumeration e = reader.pages(); e.hasMoreElements(); i++) {
                    Page page = (Page) e.nextElement();
                    System.out.println("Page " + i + ": " + page);
                    System.out.println();
                }
            } else if ("-dumppagemd5".equals(argv[1])) {
                System.out.println(reader);
                System.out.println();
                int i = 1;
                for (Enumeration e = reader.pagesByMD5(); e.hasMoreElements(); i++) {
                    Page page = (Page) e.nextElement();
                    System.out.println("Page " + i + ": " + page);
                    System.out.println();
                }
            } else if ("-toppages".equals(argv[1])) {
                int topSize = Integer.parseInt(argv[2]);

                // Create a sorted list
                SortedSet topSet = new TreeSet(new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Page p1 = (Page) o1;
                        Page p2 = (Page) o2;
                        if (p1.getScore() < p2.getScore()) {
                            return -1;
                        } else if (p1.getScore() == p2.getScore()) {
                            // If two scores are equal, we will
                            // use regular Page comparison (which
                            // uses URL as the primary key).  We
                            // don't want to uniquify by score!
                            return p1.compareTo(p2);
                        } else {
                            return 1;
                        }
                    }
                }
                    );

                // Find the top "topSize" elts
                Page lowestPage = null;
                for (Enumeration e = reader.pages(); e.hasMoreElements(); ) {
                    Page curPage = (Page) e.nextElement();
                    if (topSet.size() < topSize) {
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
                    System.out.println("Page " + i + ": " + (Page) it.next());
                    System.out.println();
                }
            } else if ("-linkurl".equals(argv[1])) {
                String url = argv[2];
                Link links[] = reader.getLinks(new UTF8(url.trim()));
                System.out.println("Found " + links.length + " links.");
                for (int i = 0; i < links.length; i++) {
                    System.out.println("Link " + i + ": " + links[i]);
                }
            } else if ("-linkmd5".equals(argv[1])) {
                MD5Hash fromID = new MD5Hash(argv[2]);
                Link links[] = reader.getLinks(fromID);
                System.out.println("Found " + links.length + " links.");
                for (int i = 0; i < links.length; i++) {
                    System.out.println("Link " + i + ": " + links[i]);
                }
            } else if ("-dumplinks".equals(argv[1])) {
                System.out.println(reader);
                System.out.println();
                Enumeration e = reader.pagesByMD5();
                while (e.hasMoreElements()) {
                  Page page = (Page) e.nextElement();
                  Link[] links = reader.getLinks(page.getMD5());
                  if (links.length > 0) {
                    System.out.println("from " + page.getURL());
                    for (int i = 0; i < links.length; i++) {
                      System.out.println(" to " + links[i].getURL());
                    }
                    System.out.println();
                  }
                }
            } else if ("-stats".equals(argv[1])) {
                System.out.println("Stats for " + reader);
                System.out.println("-------------------------------");
                System.out.println("Number of pages: " + reader.numPages());
                System.out.println("Number of links: " + reader.numLinks());
            } else {
                System.out.println("Sorry, no command with name " + argv[1]);
            }
        } finally {
            reader.close();
        }
    }
}
