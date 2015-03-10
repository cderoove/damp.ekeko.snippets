/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer;

import org.apache.lucene.util.PriorityQueue;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import java.io.OutputStreamWriter;

/** Lists the most frequent terms in an index. */
public class HighFreqTerms {
  public static int numTerms = 100;

  private static class TermFreq {
    TermFreq(Term t, int df) {
      term = t;
      docFreq = df;
    }
    int docFreq;
    Term term;
  }

  private static class TermFreqQueue extends PriorityQueue {
    TermFreqQueue(int size) {
      initialize(size);
    }

    protected final boolean lessThan(Object a, Object b) {
      TermFreq termInfoA = (TermFreq)a;
      TermFreq termInfoB = (TermFreq)b;
      return termInfoA.docFreq < termInfoB.docFreq;
    }
  }

  public static void main(String[] args) throws Exception {
    IndexReader reader = null;
    boolean noFreqs = false;
    int count = 100;
    String usage = "HighFreqTerms [-count <n>] [-nofreqs] <index dir>";

    if (args.length == 0) {
      System.err.println(usage);
      System.exit(-1);
    }

    for (int i = 0; i < args.length; i++) {       // parse command line
      if (args[i].equals("-count")) {		  // found -count option
        count = Integer.parseInt(args[++i]);
      } else if (args[i].equals("-nofreqs")) {    // found -nofreqs option
        noFreqs = true;
      } else {
        reader = IndexReader.open(args[i]);
      }
    }

    TermFreqQueue tiq = new TermFreqQueue(count);
    TermEnum terms = reader.terms();
      
    int minFreq = 0;
    while (terms.next()) {
      if (terms.docFreq() > minFreq) {
        tiq.put(new TermFreq(terms.term(), terms.docFreq()));
        if (tiq.size() > count) {                 // if tiq overfull
          tiq.pop();                              // remove lowest in tiq
          minFreq = ((TermFreq)tiq.top()).docFreq; // reset minFreq
        }
      }
    }

    OutputStreamWriter out = new OutputStreamWriter(System.out, "UTF-8");
    while (tiq.size() != 0) {
      TermFreq termInfo = (TermFreq)tiq.pop();
      out.write(termInfo.term.toString());
      if (!noFreqs) {
        out.write(" ");
        out.write(Integer.toString(termInfo.docFreq));
      }
      out.write("\n");
    }

    out.flush();
    reader.close();
  }

}

