/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.db;

import java.io.*;
import java.util.*;

import net.nutch.io.*;
import net.nutch.pagedb.*;
import net.nutch.linkdb.*;

/**********************************************
 * IWebDBReader is an interface to the consolidated
 * page/link database.  It permits all kind of read-only ops.
 *
 * This database may be implemented in several different
 * ways, which this interface hides from its user.
 *
 * @author Mike Cafarella
 **********************************************/
public interface IWebDBReader {
    /**
     * Done reading.  Release a handle on the db.
     */
    public void close() throws IOException;
    
    /**
     * Return a Page object with the given URL, if any.
     * Pages are guaranteed to be unique by URL, so there
     * can be max. 1 returned object.
     */
    public Page getPage(String url) throws IOException;

    /**
     * Return any Pages with the given MD5 checksum.  Pages
     * with different URLs often have identical checksums; this
     * can happen if the content has been copied, or a site
     * is available under several different URLs.
     */
    public Page[] getPages(MD5Hash md5) throws IOException;

    /**
     * Returns whether a Page with the given MD5 checksum is in the db.
     */
    public boolean pageExists(MD5Hash md5) throws IOException;

    /**
     * Obtain an Enumeration of all Page objects, sorted by URL
     */
    public Enumeration pages() throws IOException;

    /**
     * Obtain an Enumeration of all Page objects, sorted by MD5.
     */
    public Enumeration pagesByMD5() throws IOException;

    /**
     * Simple count of all Page objects in db.
     */
    public long numPages();

    /**
     * Return any Link objects that point to the given URL.  This
     * array can be very large if the given URL has lots of incoming
     * Links.  So large, in fact, that this method call will probably 
     * kill the process for certain URLs.
     */
    public Link[] getLinks(UTF8 url) throws IOException;

    /**
     * Return all the Link objects that originate from a document
     * with the given MD5 checksum.  These will be the outlinks for
     * the page of content described.
     */
    public Link[] getLinks(MD5Hash md5) throws IOException;

    /**
     * Obtain an Enumeration of all Link objects, sorted by target
     * URL.
     */
    public Enumeration links() throws IOException;

    /**
     * Simple count of all Link objects in db.
     */
    public long numLinks();
}
