/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer;

import net.nutch.pagedb.*;
import net.nutch.linkdb.*;
import net.nutch.fetcher.*;
import net.nutch.analysis.NutchDocumentAnalyzer;
import net.nutch.db.*;
import net.nutch.io.*;
import net.nutch.util.*;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;
import java.io.File;
import java.io.EOFException;

/** Creates an index for the output corresponding to a single fetcher run. */
public class IndexSegment {
  public static final String DONE_NAME = "index.done";
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.index.IndexSegment");

  private boolean boostByLinkCount =
    NutchConf.getBoolean("indexer.boost.by.link.count", false);

  private float scorePower = NutchConf.getFloat("indexer.score.power", 0.5f);

  private int maxTitleLength =
    NutchConf.getInt("indexer.max.title.length", 100);

  private File directory = null;
  private int maxDocs = Integer.MAX_VALUE;

  /** Determines the power of link analyis scores.  Each pages's boost is
   * set to <i>score<sup>scorePower</sup></i> where <i>score</i> is its link
   * analysis score and <i>scorePower</i> is the value passed to this method.
   */
  public void setScorePower(float power) { scorePower = power; }

  private void indexPages() throws Exception {
    IndexWriter writer
      = new IndexWriter(new File(directory, "index"),
                        new NutchDocumentAnalyzer(), true);
    writer.mergeFactor = 50;
    writer.minMergeDocs = 50;
    writer.infoStream = LogFormatter.getLogStream(LOG, Level.INFO);
    writer.setUseCompoundFile(false);
    writer.setSimilarity(new NutchSimilarity());

    ArrayFile.Reader fetcher = null;
    ArrayFile.Reader text = null;

    int count = 0;
    try {
      fetcher = new ArrayFile.Reader(new File(directory, FetcherOutput.DIR_NAME).toString());
      text = new ArrayFile.Reader(new File(directory,FetcherText.DIR_NAME).toString());

      String segmentName = directory.getCanonicalFile().getName();
      FetcherOutput fetcherOutput = new FetcherOutput();
      FetcherText fetcherText = new FetcherText();

      while (fetcher.next(fetcherOutput) != null && count++ < maxDocs) {
        text.next(fetcherText);
        
        if (fetcherOutput.getStatus() != FetcherOutput.SUCCESS)
          continue;                               // don't index the page

        Document doc = makeDocument(segmentName, fetcher.key(),
                                    fetcherOutput, fetcherText);
        writer.addDocument(doc);
      }
    } catch (EOFException e) {
      LOG.warning("Unexpected EOF in: " + directory +
                  " at entry #" + count + ".  Ignoring.");
    } finally {
      if (fetcher != null)
        fetcher.close();
      if (text != null)
        text.close();
    }
    LOG.info("Optimizing index...");
    writer.optimize();
    writer.close();
  }

  private Document makeDocument(String segmentName, long docNo,
                                FetcherOutput fetcherOutput,
                                FetcherText fetcherText)
    throws Exception {

    FetchListEntry fle = fetcherOutput.getFetchListEntry();
    String url = fle.getPage().getURL().toString();
    String title = fetcherOutput.getTitle();

    if (title.length() > maxTitleLength) {        // truncate title if needed
      title = title.substring(0, maxTitleLength);
    }

    Document doc = new Document();

    // url is both stored and indexed, so it's both searchable and returned
    doc.add(Field.Text("url", url));

    // un-indexed fields: not searchable, but in hits and/or used by dedup
    doc.add(Field.UnIndexed("title", title));
    doc.add(Field.UnIndexed("digest", fetcherOutput.getMD5Hash().toString()));
    doc.add(Field.UnIndexed("docNo", Long.toString(docNo, 16)));
    doc.add(Field.UnIndexed("segment", segmentName));

    // content is indexed, so that it's searchable, but not stored in index
    doc.add(Field.UnStored("content", fetcherText.getText()));
    
    // anchors are indexed, so they're searchable, but not stored in index
    String[] anchors = fle.getAnchors();
    for (int i = 0; i < anchors.length; i++) {
      doc.add(Field.UnStored("anchor", anchors[i]));
    }

    // add title as anchor so it's searchable.  doesn't warrant its own field.
    doc.add(Field.UnStored("anchor", title));

    // compute boost
    // 1. Start with page's score from DB -- 1.0 if no link analysis.
    float boost = fle.getPage().getScore();
    // 2. Apply scorePower to this.
    boost = (float)Math.pow(boost, scorePower);
    // 3. Optionally boost by log of incoming anchor count.
    if (boostByLinkCount)
      boost *= (float)Math.log(Math.E + anchors.length);
    // 4. Apply boost to all indexed fields.
    doc.setBoost(boost);

    // store boost for use by explain and dedup
    doc.add(Field.UnIndexed("boost", Float.toString(boost)));
    
    return doc;
  }


  /** Create an index for the input files in the named directory. */
  public static void main(String[] args) throws Exception {
      
    String usage = "IndexSegment <segment_directory>";

    if (args.length == 0) {
      System.err.println("Usage: " + usage);
      return;
    }

    IndexSegment indexer = new IndexSegment();

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-max")) {        // parse -max option
        indexer.maxDocs = Integer.parseInt(args[++i]);
      } else if (i != args.length-1) {
        System.err.println("Usage: " + usage);
        return;
      } else {
        indexer.directory = new File(args[i]);
      }
    }

//     File fetcherDone = new File(indexer.directory, FetcherOutput.DONE_NAME);
//     if (!fetcherDone.exists())                    // check fetcher done file
//       throw new RuntimeException("can't index--not yet fetched: " +
//                                  fetcherDone + " does not exist");

    File doneFile = new File(indexer.directory, DONE_NAME);
    if (doneFile.exists())                        // check index done file
      throw new RuntimeException("already indexed: " + doneFile + " exists");

    LOG.info("indexing segment: " + indexer.directory);

    indexer.indexPages();
    doneFile.createNewFile();                     // create the done file

    LOG.info("done indexing");
  }

}
