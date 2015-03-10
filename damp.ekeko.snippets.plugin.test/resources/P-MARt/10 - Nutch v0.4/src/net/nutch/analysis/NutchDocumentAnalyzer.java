/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;
import java.io.Reader;
import java.io.IOException;

/** The analyzer used for Nutch documents.  Uses the JavaCC-defined lexical
 * analyzer {@link NutchDocumentTokenizer}, with no stop list.  This keeps it
 * consistent with query parsing. */
public class NutchDocumentAnalyzer extends Analyzer {

  /** Analyzer used to index textual content. */
  private static class ContentAnalyzer extends Analyzer {
    /** Constructs a {@link NutchDocumentTokenizer}. */
    public TokenStream tokenStream(String field, Reader reader) {
      return CommonGrams.getFilter(new NutchDocumentTokenizer(reader), field);
    }
  }

  /** Analyzer used to index textual content. */
  public static final Analyzer CONTENT_ANALYZER = new ContentAnalyzer();

  // Anchor Analysis
  // Like content analysis, but leave gap between anchors to inhibit
  // cross-anchor phrase matching.

  /** The number of unused term positions between anchors in the anchor
   * field. */
  public static final int INTER_ANCHOR_GAP = 4;

  private static class AnchorFilter extends TokenFilter {
    public AnchorFilter(TokenStream input) {
      super(input);
    }

    private boolean first = true;
    public final Token next() throws IOException {
      Token result = input.next();
      if (result == null)
        return result;
      if (first) {
        result.setPositionIncrement(INTER_ANCHOR_GAP);
        first = false;
      }
      return result;
    }
  }

  private static class AnchorAnalyzer extends Analyzer {
    public final TokenStream tokenStream(String fieldName, Reader reader) {
      return new AnchorFilter(CONTENT_ANALYZER.tokenStream(fieldName, reader));
    }
  }

  /** Analyzer used to analyze anchors. */
  public static final Analyzer ANCHOR_ANALYZER = new AnchorAnalyzer();

  /** Returns a new token stream for text from the named field. */
  public TokenStream tokenStream(String fieldName, Reader reader) {
    Analyzer analyzer;
    if ("url".equals(fieldName) || ("anchor".equals(fieldName)))
      analyzer = ANCHOR_ANALYZER;
    else
      analyzer = CONTENT_ANALYZER;

    return analyzer.tokenStream(fieldName, reader);
  }

}
