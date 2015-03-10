/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.IOException;
import java.io.File;

import java.util.Enumeration;
import java.util.ArrayList;

import org.apache.lucene.search.Searchable;
//import org.apache.lucene.search.Searcher;
//import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import net.nutch.io.*;
import net.nutch.db.*;
import net.nutch.fetcher.*;
import net.nutch.linkdb.*;
import net.nutch.indexer.*;
import net.nutch.analysis.NutchDocumentAnalyzer;

/** Implements {@link Searcher} and {@link HitDetailer} for either a single
 * merged index, or for a set of individual segment indexes. */
public class IndexSearcher implements Searcher, HitDetailer {

  private org.apache.lucene.search.Searcher luceneSearcher;

  private String[] segmentNames;                  // for back compat.

  /** Construct given a number of indexed segments. */
  public IndexSearcher(File[] segmentDirs) throws IOException {
    NutchSimilarity sim = new NutchSimilarity();
    Searchable[] searchables = new Searchable[segmentDirs.length];
    segmentNames = new String[segmentDirs.length];
    for (int i = 0; i < segmentDirs.length; i++) {
      org.apache.lucene.search.Searcher searcher =
        new org.apache.lucene.search.IndexSearcher
        (new File(segmentDirs[i], "index").toString());
      searcher.setSimilarity(sim);
      searchables[i] = searcher;
      segmentNames[i] = segmentDirs[i].getName();
    }
    this.luceneSearcher = new MultiSearcher(searchables);
    this.luceneSearcher.setSimilarity(sim);
  }

  /** Construct given a directory containing fetched segments, and a separate
   * directory naming their merged index. */
  public IndexSearcher(String index)
    throws IOException {
    this.luceneSearcher = new org.apache.lucene.search.IndexSearcher(index);
    this.luceneSearcher.setSimilarity(new NutchSimilarity());
  }

  public Hits search(Query query, int numHits) throws IOException {

    org.apache.lucene.search.Query luceneQuery =
      QueryTranslator.translate(query);
    
    return translateHits(luceneSearcher.search(luceneQuery, null, numHits));
  }

  public String getExplanation(Query query, Hit hit) throws IOException {
    return luceneSearcher.explain(QueryTranslator.translate(query),
                                  hit.getIndexDocNo()).toHtml();
  }

  public HitDetails getDetails(Hit hit) throws IOException {
    ArrayList fields = new ArrayList();
    ArrayList values = new ArrayList();

    Document doc = luceneSearcher.doc(hit.getIndexDocNo());

    Enumeration e = doc.fields();
    while (e.hasMoreElements()) {
      Field field = (Field)e.nextElement();
      fields.add(field.name());
      values.add(field.stringValue());
    }

    // for back-compatibility with old indexes
    String segment = doc.get("segment");
    if (segment == null) {
      MultiSearcher multi = (MultiSearcher)luceneSearcher;
      fields.add("segment");
      values.add(segmentNames[multi.subSearcher(hit.getIndexDocNo())]);
      fields.add("docNo");
      values.add(Integer.toString(multi.subDoc(hit.getIndexDocNo()), 16));
    }

    return new HitDetails((String[])fields.toArray(new String[fields.size()]),
                          (String[])values.toArray(new String[values.size()]));
  }

  public HitDetails[] getDetails(Hit[] hits) throws IOException {
    HitDetails[] results = new HitDetails[hits.length];
    for (int i = 0; i < hits.length; i++)
      results[i] = getDetails(hits[i]);
    return results;
  }

  private Hits translateHits(TopDocs topDocs) throws IOException {
    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
    int length = scoreDocs.length;
    Hit[] hits = new Hit[length];
    for (int i = 0; i < length; i++) {
      hits[i] = new Hit(scoreDocs[i].doc, scoreDocs[i].score);
    }
    return new Hits(topDocs.totalHits, hits);
  }

}
