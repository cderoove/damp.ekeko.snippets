/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.IOException;
import java.io.File;

import java.util.HashMap;

import net.nutch.io.*;
import net.nutch.db.*;
import net.nutch.fetcher.*;
import net.nutch.pagedb.*;
import net.nutch.indexer.*;

/** Implements {@link HitSummarizer} and {@link HitContent} for a set of
 * fetched segments. */
public class FetchedSegments implements HitSummarizer, HitContent {

  private static class Segment {
    private ArrayFile.Reader fetcher;
    private ArrayFile.Reader content;
    private ArrayFile.Reader text;

    public Segment(File segmentDir) throws IOException {
      this.fetcher = new ArrayFile.Reader
        (new File(segmentDir, FetcherOutput.DIR_NAME).toString());
      this.content = new ArrayFile.Reader
        (new File(segmentDir, FetcherContent.DIR_NAME).toString());
      this.text = new ArrayFile.Reader
        (new File(segmentDir, FetcherText.DIR_NAME).toString());
    }

    public FetcherOutput getFetcherOutput(int docNo) throws IOException {
      FetcherOutput entry = new FetcherOutput();
      fetcher.get(docNo, entry);
      return entry;
    }

    public byte[] getContent(int docNo) throws IOException {
      FetcherContent entry = new FetcherContent();
      content.get(docNo, entry);
      return entry.getContent();
    }

    public String getText(int docNo) throws IOException {
      FetcherText entry = new FetcherText();
      text.get(docNo, entry);
      return entry.getText();
    }

  }

  private HashMap segments = new HashMap();

  /** Construct given a directory containing fetcher output. */
  public FetchedSegments(String segmentsDir) throws IOException {
    File[] segmentDirs = new File(segmentsDir).listFiles();

    if (segmentDirs != null) {
        for (int i = 0; i < segmentDirs.length; i++) {
            File segmentDir = segmentDirs[i];
            File indexdone = new File(segmentDir, IndexSegment.DONE_NAME);
            if(indexdone.exists() && indexdone.isFile()) {
            	segments.put(segmentDir.getName(), new Segment(segmentDir));
            }
        }
    }
  }

  public String[] getSegmentNames() {
    return (String[])segments.keySet().toArray(new String[segments.size()]);
  }

  public byte[] getContent(HitDetails details) throws IOException {
    return getSegment(details).getContent(getDocNo(details));
  }

  public String[] getAnchors(HitDetails details) throws IOException {
    return getSegment(details).getFetcherOutput(getDocNo(details))
      .getFetchListEntry().getAnchors();
  }

  public String getSummary(HitDetails details, Query query)
    throws IOException {

    String text = getSegment(details).getText(getDocNo(details));

    return new Summarizer().getSummary(text, query).toString();
  }
    
  public String[] getSummary(HitDetails[] details, Query query)
    throws IOException {
    String[] results = new String[details.length];
    for (int i = 0; i < details.length; i++)
      results[i] = getSummary(details[i], query);
    return results;
  }


  private Segment getSegment(HitDetails details) {
    return (Segment)segments.get(details.getValue("segment"));
  }

  private int getDocNo(HitDetails details) {
    return Integer.parseInt(details.getValue("docNo"), 16);
  }


}
