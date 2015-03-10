/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;

import net.nutch.analysis.NutchDocumentAnalyzer;
import net.nutch.analysis.CommonGrams;

import net.nutch.searcher.Query.*;

import java.io.IOException;

/** Translation from Nutch queries to Lucene queries. */
public class QueryTranslator {
  private QueryTranslator() {}                    // can't construct

  private static float URL_BOOST = 4.0f;
  private static float ANCHOR_BOOST = 2.0f;

  private static int SLOP = Integer.MAX_VALUE;
  private static float PHRASE_BOOST = 1.0f;

  /** Set the boost factor for url matches, relative to content and anchor
   * matches */
  public static void setUrlBoost(float boost) { URL_BOOST = boost; }

  /** Set the boost factor for title/anchor matches, relative to url and
   * content matches. */
  public static void setAnchorBoost(float boost) { ANCHOR_BOOST = boost; }

  /** Set the boost factor for sloppy phrase matches relative to unordered term
   * matches. */
  public static void setPhraseBoost(float boost) { PHRASE_BOOST = boost; }

  /** Set the maximum number of terms permitted between matching terms in a
   * sloppy phrase match. */
  public static void setSlop(int slop) { SLOP = slop; }

  private static Interface DEFAULT_IMPL = new Default();

  /** Set the default query translation implementation. */
  public static void setDefaultTranslator(Interface impl) {
    DEFAULT_IMPL = impl;
  }

  /** Get the default query translation implementation. */
  public static Interface getDefaultTranslator() {
    return DEFAULT_IMPL;
  }

  /** Translate a Nutch query into a Lucene query using the default
   * translator. */
  public static org.apache.lucene.search.Query translate(Query input) {
    return DEFAULT_IMPL.translate(input);
  }

  /** Query translation interface. */
  public interface Interface {
    /** Translate a Nutch query into a Lucene query. */
    org.apache.lucene.search.Query translate(Query input);
  }

  /** The default query translator.  Searches all fields with both sloppy
   * phrases and individual terms.*/
  public static class Default implements Interface {
    public org.apache.lucene.search.Query translate(Query input) {
      BooleanQuery output = new BooleanQuery();
      addClauses(input, "url", output, URL_BOOST, SLOP);
      addClauses(input, "anchor", output, ANCHOR_BOOST,
                 NutchDocumentAnalyzer.INTER_ANCHOR_GAP);
      addClauses(input, "content", output, 1.0f, SLOP);
      //System.out.println(output.toString("content"));
      return output;
    }

    /** Add all terms from a Nutch query to a Lucene query, searching the named
     * field as a sloppy phrase and as individual terms.. */
    private static void addClauses(Query input, String field,
                                   BooleanQuery output,
                                   float boost, int slop) {
      BooleanQuery requirements = new BooleanQuery();
      PhraseQuery sloppyPhrase = new PhraseQuery();
      sloppyPhrase.setSlop(slop);
      sloppyPhrase.setBoost(boost * PHRASE_BOOST);
      int sloppyTerms = 0;

      Clause[] clauses = input.getClauses();
      for (int i = 0; i < clauses.length; i++) {
        Clause clause = clauses[i];

        if (clause.isPhrase()) {             // optimize phrase clauses
          String[] opt = CommonGrams.optimizePhrase(clause.getPhrase(), field);
          if (opt.length==1) {
            clause = new Clause(new Term(opt[0]), clause.isRequired(), clause.isProhibited());
          } else {
            clause = new Clause(new Phrase(opt), clause.isRequired(), clause.isProhibited());
          }
        }

        if (clause.isRequired()) {
          if (!clause.isPhrase()) {
            sloppyPhrase.add(luceneTerm(field, clause.getTerm()));
            sloppyTerms++;
            requirements.add(termQuery(field, clause.getTerm(), boost),
                             true, false);
          } else {
            requirements.add(exactPhrase(clause.getPhrase(), field, boost),
                             true, false);
          }

        } else if (clause.isProhibited()) {
          if (!clause.isPhrase()) {
            output.add(new TermQuery(luceneTerm(field, clause.getTerm())),
                       false, true);
                           
          } else {
            output.add(exactPhrase(clause.getPhrase(), field, boost),
                       false, true);
          }
        }
      }
      if (sloppyTerms > 1) {
        requirements.add(sloppyPhrase, true, false);
      }
      output.add(requirements, false, false);
    }

    private static TermQuery termQuery(String field, Term term, float boost) {
      TermQuery result = new TermQuery(luceneTerm(field, term));
      result.setBoost(boost);
      return result;
    }

    /** Utility to construct a Lucene exact phrase query for a Nutch phrase. */
    private static PhraseQuery exactPhrase(Phrase nutchPhrase,
                                           String field, float boost) {
      Term[] terms = nutchPhrase.getTerms();
      PhraseQuery exactPhrase = new PhraseQuery();
      for (int i = 0; i < terms.length; i++) {
        exactPhrase.add(luceneTerm(field, terms[i]));
      }
      exactPhrase.setBoost(boost);
      return exactPhrase;
    }

    /** Utility to construct a Lucene Term given a Nutch query term and field. */
    private static org.apache.lucene.index.Term luceneTerm(String field,
                                                           Term term) {
      return new org.apache.lucene.index.Term(field, term.toString());
    }
  }}
