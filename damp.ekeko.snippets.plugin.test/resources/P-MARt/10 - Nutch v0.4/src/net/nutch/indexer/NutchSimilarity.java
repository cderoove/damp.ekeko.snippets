/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.indexer;

import org.apache.lucene.search.DefaultSimilarity;

/** Similarity implementatation used by Nutch indexing and search. */
public class NutchSimilarity extends DefaultSimilarity  {
  private static final int MIN_CONTENT_LENGTH = 1000;

  /** Normalize field by length.  Called at index time. */
  public float lengthNorm(String fieldName, int numTokens) {
    if ("url".equals(fieldName)) {                // URL: prefer short
      return 1.0f / numTokens;                    // use linear normalization
      
    } else if ("anchor".equals(fieldName)) {      // Anchor: prefer more
      return (float)(1.0/Math.log(Math.E+numTokens)); // use log

    } else if ("content".equals(fieldName)) {     // Content: penalize short
      return super.lengthNorm(fieldName,          // treat short as longer
                              Math.max(numTokens, MIN_CONTENT_LENGTH));

    } else {                                      // use default
      return super.lengthNorm(fieldName, numTokens);
    }
  }

  public float coord(int overlap, int maxOverlap) {
    return 1.0f;
  }

}
