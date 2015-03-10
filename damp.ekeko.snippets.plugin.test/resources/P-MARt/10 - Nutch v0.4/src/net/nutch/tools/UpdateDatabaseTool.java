/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.tools;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.*;

import net.nutch.db.*;
import net.nutch.net.*;
import net.nutch.io.*;
import net.nutch.linkdb.*;
import net.nutch.pagedb.*;
import net.nutch.fetcher.*;
import net.nutch.util.*;


/*****************************************************
 * This class takes the output of the fetcher and updates the page and link
 * DBs accordingly.  Eventually, as the database scales, this will broken into
 * several phases, each consuming and emitting batch files, but, for now, we're
 * doing it all here.
 *
 * @author Doug Cutting
 *****************************************************/
public class UpdateDatabaseTool {
    public static final float NEW_INTERNAL_LINK_FACTOR =
      NutchConf.getFloat("db.score.link.internal", 1.0f);
    public static final float NEW_EXTERNAL_LINK_FACTOR =
      NutchConf.getFloat("db.score.link.external", 1.0f);
    public static final int MAX_OUTLINKS_PER_PAGE =
      NutchConf.getInt("db.max.outlinks.per.page", 100);

    public static final boolean IGNORE_INTERNAL_LINKS =
      NutchConf.getBoolean("db.ignore.internal.links", true);


    public static final Logger LOG =
      LogFormatter.getLogger("net.nutch.tools.UpdateDatabaseTool");

    private static final int MAX_RETRIES = 2;
    private static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

    // back-compatibility hack for un-dated fetcher output
    // delete after 1 June 2003
    public static class FetcherOutputReader extends ArrayFile.Reader {
      private long lastModified;

      public FetcherOutputReader(String file) throws IOException {
        super(file);
        this.lastModified = new File(file).lastModified();
      }

      public Writable next(Writable value) throws IOException {
        return checkFetchDate((FetcherOutput)super.next(value));
      }

      public Writable get(long n, Writable value) throws IOException {
        return checkFetchDate((FetcherOutput)super.get(n, value));
      }

      private FetcherOutput checkFetchDate(FetcherOutput fo) {
        if (fo != null && fo.getFetchDate() == 0)
          // default fetchDate to file's lastModified
          fo.setFetchDate(lastModified);
        return fo;
      }
    }

    private IWebDBWriter webdb;
    private int maxCount = 0;
    private boolean additionsAllowed = true;
    private Set outlinkSet = new TreeSet(); // used in Page attr calculations

    /**
     * Take in the WebDBWriter, instantiated elsewhere.
     */
    public UpdateDatabaseTool(IWebDBWriter webdb, boolean additionsAllowed, int maxCount) {
        this.webdb = webdb;
        this.additionsAllowed = additionsAllowed;
        this.maxCount = maxCount;
    }

    /**
     * Iterate through items in the FetcherOutput.  For each one,
     * determine whether the pages need to be added to the webdb,
     * or what fields need to be changed.
     */
    public void updateForSegment(String directory)
        throws IOException {
        ArrayList deleteQueue = new ArrayList();
        String fetchDir=new File(directory, FetcherOutput.DIR_NAME).toString();
        ArrayFile.Reader table = null;
        int count = 0;
        try {
          table = new FetcherOutputReader(fetchDir);
          FetcherOutput fo = new FetcherOutput();
          while (table.next(fo) != null) {
            if ((maxCount >= 0) && (count >= maxCount)) {
              break;
            }

            FetchListEntry fle = fo.getFetchListEntry();
            Page page = fle.getPage();
            LOG.fine("Processing " + page.getURL());
            if (!fle.getFetch()) {                // didn't fetch
              pageContentsUnchanged(fo);          // treat as unchanged

            } else if (fo.getStatus() == fo.SUCCESS) { // fetch succeed
              if (fo.getMD5Hash().equals(page.getMD5())) {
                pageContentsUnchanged(fo);        // contents unchanged
              } else {
                pageContentsChanged(fo);          // contents changed
              }

            } else if (fo.getStatus() == fo.RETRY &&
                       page.getRetriesSinceFetch() < MAX_RETRIES) {

              pageRetry(fo);                      // retry later

            } else {
              pageGone(fo);                       // give up: page is gone
            }
            count++;
          }
        } catch (EOFException e) {
          LOG.warning("Unexpected EOF in: " + fetchDir +
                      " at entry #" + count + ".  Ignoring.");
        } finally {
          if (table != null)
            table.close();
        }
    }

    /**
     * There's been no change: update date & retries only
     */
    private void pageContentsUnchanged(FetcherOutput fetcherOutput)
        throws IOException {
        Page oldPage = fetcherOutput.getFetchListEntry().getPage();
        Page newPage = (Page)oldPage.clone();

        LOG.fine("unchanged");

        newPage.setNextFetchTime(nextFetch(fetcherOutput)); // set next fetch
        newPage.setRetriesSinceFetch(0);              // zero retries

        webdb.addPage(newPage);                       // update record in db
    }
    
    /**
     * We've encountered new content, so update md5, etc.
     * Also insert the new outlinks into the link DB
     */
    private void pageContentsChanged(FetcherOutput fetcherOutput)
        throws IOException {
      Page oldPage = fetcherOutput.getFetchListEntry().getPage();
      Page newPage = (Page)oldPage.clone();

      LOG.fine("new contents");

      newPage.setNextFetchTime(nextFetch(fetcherOutput)); // set next fetch
      newPage.setMD5(fetcherOutput.getMD5Hash());   // update md5
      newPage.setRetriesSinceFetch(0);              // zero retries

      // Go through all the outlinks from this page, and add to
      // the LinkDB.
      //
      // If the replaced page is the last ref to its MD5, then
      // its outlinks must be removed.  The WebDBWriter will
      // handle that, upon page-replacement.
      //
      Outlink[] outlinks = fetcherOutput.getOutlinks();
      String sourceHost = getHost(oldPage.getURL().toString());
      long sourceDomainID = newPage.computeDomainID();
      long nextFetch = nextFetch(fetcherOutput, 0);
      outlinkSet.clear();  // Use a hashtable to uniquify the links
      int end = Math.min(outlinks.length, MAX_OUTLINKS_PER_PAGE);
      for (int i = 0; i < end; i++) {
        Outlink link = outlinks[i];
        String url = link.getToUrl();

        url = URLFilterFactory.getFilter().filter(url);
        if (url == null)
          continue;

        outlinkSet.add(url);        
        
        if (additionsAllowed) {
            String destHost = getHost(url);
            boolean internal = destHost == null || destHost.equals(sourceHost);

            try {
                //
                // If it is an in-site link, then we only add a Link if
                // the Page is also added.  So we pass it to addPageIfNotPresent().
                //
                // If it is not an in-site link, then we always add the link.
                // We then conditionally add the Page with addPageIfNotPresent().
                //
                Link newLink = new Link(newPage.getMD5(), sourceDomainID, url, link.getAnchor());

                float newScore = oldPage.getScore();
                float newNextScore = oldPage.getNextScore();

                if (internal) {
                  newScore *= NEW_INTERNAL_LINK_FACTOR;
                  newNextScore *= NEW_INTERNAL_LINK_FACTOR;
                } else {
                  newScore *= NEW_EXTERNAL_LINK_FACTOR;
                  newNextScore *= NEW_EXTERNAL_LINK_FACTOR;
                }

                Page linkedPage = new Page(url, newScore, newNextScore, nextFetch);

                if (internal && IGNORE_INTERNAL_LINKS) {
                  webdb.addPageIfNotPresent(linkedPage, newLink);
                } else {
                  webdb.addLink(newLink);
                  webdb.addPageIfNotPresent(linkedPage);
                }

            } catch (MalformedURLException e) {
                LOG.fine("skipping " + url + ":" + e);
            }
        }
      }

      // Calculate the number of different outlinks here.
      // We use the outlinkSet TreeSet so that we count only
      // the unique links leaving the Page.  The WebDB will
      // only store one value for each (fromID,toURL) pair
      //
      // Store the value with the Page, to speed up later
      // Link Analysis computation.
      //
      // NOTE: This value won't necessarily even match what's
      // in the linkdb!  That's OK!  It's more important that
      // this number be a "true count" of the outlinks from
      // the page in question, than the value reflect what's
      // actually in our db.  (There are a number of reasons,
      // mainly space economy, to avoid placing URLs in our db.
      // These reasons slightly pervert the "true out count".)
      // 
      newPage.setNumOutlinks(outlinkSet.size());  // Store # outlinks

      webdb.addPage(newPage);                     // update record in db
    }

    /**
     * Keep the page, but never re-fetch it.
     */
    private void pageGone(FetcherOutput fetcherOutput)
        throws IOException {
        Page oldPage = fetcherOutput.getFetchListEntry().getPage();
        Page newPage = (Page)oldPage.clone();

        LOG.fine("retry never");

        newPage.setNextFetchTime(Long.MAX_VALUE); // never refetch
        webdb.addPage(newPage);                   // update record in db
    }

    /**
     * Update with new retry count and date
     */
    private void pageRetry(FetcherOutput fetcherOutput)
        throws IOException {
        Page oldPage = fetcherOutput.getFetchListEntry().getPage();
        Page newPage = (Page)oldPage.clone();

        LOG.fine("retry later");

        newPage.setNextFetchTime(nextFetch(fetcherOutput,1)); // wait a day
        newPage.setRetriesSinceFetch
            (oldPage.getRetriesSinceFetch()+1);         // increment retries

        webdb.addPage(newPage);                       // update record in db
    }

    /**
     * Compute the next fetchtime for the Page.
     */
    private long nextFetch(FetcherOutput fo) {
        return nextFetch(fo,
                         fo.getFetchListEntry().getPage().getFetchInterval());
    }

    /**
     * Compute the next fetchtime, from this moment, with the given
     * number of days.
     */
    private long nextFetch(FetcherOutput fetcherOutput, int days) {
      return fetcherOutput.getFetchDate() + (MILLISECONDS_PER_DAY * days);
    }

    /**
     * Parse the hostname from a URL and return it.
     */
    private String getHost(String url) {
      try {
        return new URL(url).getHost().toLowerCase();
      } catch (MalformedURLException e) {
        return null;
      }
    }

    /**
     * Shut everything down.
     */
    public void close() throws IOException {
        webdb.close();
    }

    /**
     * Create the UpdateDatabaseTool, and pass in a WebDBWriter.
     */
    public static void main(String args[]) throws Exception {
      File dbDir = null;
      int segDirStart = -1;
      int max = -1;
      boolean additionsAllowed = true;

      String usage = "UpdateDatabaseTool [-max N] [-noAdditions] db_dir seg_dir [ seg_dir ... ]";

      for (int i = 0; i < args.length; i++) {     // parse command line
        if (args[i].equals("-max")) {      // found -max option
          max = Integer.parseInt(args[++i]);
        } else if (args[i].equals("-noAdditions")) {
          additionsAllowed = false;
        } else if (dbDir == null) {
          dbDir = new File(args[i]);
        } else {
          segDirStart = i;
          break;
        }
      }

      if (segDirStart == -1) {
        System.err.println(usage);
        System.exit(-1);
      }
      
      LOG.info("Updating " + dbDir);

      IWebDBWriter webdb = new WebDBWriter(dbDir);

      UpdateDatabaseTool tool = new UpdateDatabaseTool(webdb, additionsAllowed, max);

      for (int i = segDirStart; i < args.length; i++) {
        String segDir = args[i];
        LOG.info("Updating for " + segDir);
        tool.updateForSegment(segDir);
      }

      LOG.info("Finishing update");
      tool.close();
      LOG.info("Update finished");
    }
}
