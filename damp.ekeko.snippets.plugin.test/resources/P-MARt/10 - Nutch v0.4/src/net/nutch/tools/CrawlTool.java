/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.tools;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.util.logging.*;

import net.nutch.io.*;
import net.nutch.db.*;
import net.nutch.util.*;
import net.nutch.fetcher.*;
import net.nutch.indexer.*;

/*
 */
public class CrawlTool {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.tools.CrawlTool");

  static {
    NutchConf.addConfResource("crawl-tool.xml");
  }

  /** Returns a string representing the current date and time that also sorts
   * lexicographically by date. */
  private static String getDate() {
    return new SimpleDateFormat("yyyyMMddHHmmss").format
      (new Date(System.currentTimeMillis()));
  }

  /** Returns the pathname of the latest segment in a segments directory. */
  private static String getLatestSegment(String segmentsDir) {
    String[] allSegments = new File(segmentsDir).list();
    Arrays.sort(allSegments);
    return segmentsDir + "/" + allSegments[allSegments.length-1];
  }

  /* Perform complete crawling and indexing given a set of root urls. */
  public static void main(String args[]) throws Exception {
    if (args.length < 1) {
      System.out.println
        ("Usage: CrawlTool <root_url_file> [-dir d] [-threads n] [-depth i] [-delay s] [-showThreadID]");
      return;
    }

    String rootUrlFile = args[0];

    String dir = "crawl-" + getDate();
    int threads = NutchConf.getInt("fetcher.threads.fetch", 10);
    int serverDelay = NutchConf.getInt("fetcher.server.delay", 1);
    int depth = 5;
    boolean showThreadID = false;

    for (int i = 1; i < args.length; i++) {
      if ("-dir".equals(args[i])) {
        dir = args[i+1];
        i++;
      } else if ("-threads".equals(args[i])) {
        threads = Integer.parseInt(args[i+1]);
        i++;
      } else if ("-depth".equals(args[i])) {
        depth = Integer.parseInt(args[i+1]);
        i++;
      } else if ("-delay".equals(args[i])) {
        serverDelay = Integer.parseInt(args[i+1]);
        i++;
      } else if ("-showThreadID".equals(args[i])) {
        showThreadID = true;
      }
    }

    if (new File(dir).exists())
      throw new RuntimeException(dir + " already exists.");

    LOG.info("crawl started in: " + dir);
    LOG.info("rootUrlFile = " + rootUrlFile);
    LOG.info("threads = " + threads);
    LOG.info("depth = " + depth);
    LOG.info("serverDelay = " + serverDelay);

    String db = dir + "/db";
    String segments = dir + "/segments";

    // initialize the web database
    WebDBAdminTool.main(new String[] { db, "-create" } );

    // inject the root urls into the database
    WebDBInjector.main(new String[] { db, "-urlfile", rootUrlFile } );
      
    for (int i = 0; i < depth; i++) {

      // generate a new segment
      FetchListTool.main(new String[] { db, segments } );
      String segment = getLatestSegment(segments);

      // fetch the new segment
      Fetcher.main(new String[] { "-threads", ""+threads,
                                  "-delay", ""+serverDelay,
                                  segment } );

      // update the database
      UpdateDatabaseTool.main(new String[] { db, segment } );
    }

    // Re-fetch everything to get the complete set of incoming anchor texts
    // associated with each page.  We should fix this, so that we can update
    // the previously fetched segments with the anchors that are now in the
    // database, but until that algorithm is written, we re-fetch.
    
    // delete all the old segment data
    FileUtil.fullyDelete(new File(segments));

    // generate a single segment containing all pages in the db
    FetchListTool.main(new String[] { db, segments,
                                      "-adddays", ""+Integer.MAX_VALUE } );
    String segment = getLatestSegment(segments);

    // re-fetch everything
    Fetcher.main(new String[] { "-threads", ""+threads,
                                "-delay", ""+serverDelay,
                                segment } );

    // index, dedup & merge
    IndexSegment.main(new String[] { segment } );
    DeleteDuplicates.main(new String[] { segments, dir + "/dedup" } );
    IndexMerger.main(new String[] { dir + "/index", segment } );

    LOG.info("crawl finished: " + dir);
  }
}
