/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer;

import java.util.Date;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriter;

import net.nutch.util.LogFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

/** Creates an index for the output corresponding to a single fetcher run. */
public class IndexMerger {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.indexer.IndexMerger");

  public static final String DONE_NAME = "merge.done";

  private File indexDirectory;
  private File[] segments;

  public IndexMerger(File indexDirectory, File[] segments) {
    this.indexDirectory = indexDirectory;
    this.segments = segments;
  }

  private void merge() throws IOException {
    Directory[] dirs = new Directory[segments.length];
    for (int i = 0; i < segments.length; i++)
      dirs[i] = FSDirectory.getDirectory(new File(segments[i],"index"), false);

    IndexWriter writer = new IndexWriter(indexDirectory, null, true);
    writer.mergeFactor = 50;
    writer.infoStream = LogFormatter.getLogStream(LOG, Level.INFO);
    writer.setUseCompoundFile(false);
    writer.setSimilarity(new NutchSimilarity());

    writer.addIndexes(dirs);
    writer.close();
  }


  /** Create an index for the input files in the named directory. */
  public static void main(String[] args) throws Exception {
    File indexDirectory;
      
    String usage = "IndexMerger indexDirectory segments...";

    if (args.length < 2) {
      System.err.println("Usage: " + usage);
      return;
    }

    indexDirectory = new File(args[0]);

    File[] segments = new File[args.length - 1];
    for (int i = 1; i < args.length; i++) {
      segments[i-1] = new File(args[i]);
    }

    LOG.info("merging segment indexes to: " + indexDirectory);

    IndexMerger merger = new IndexMerger(indexDirectory, segments);
    merger.merge();

    LOG.info("done merging");
  }

}
