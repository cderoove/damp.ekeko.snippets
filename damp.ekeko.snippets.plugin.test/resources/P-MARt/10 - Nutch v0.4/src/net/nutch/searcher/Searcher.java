/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.IOException;

/** Service that searches. */
public interface Searcher {
  /** Return the top-scoring hits for a query. */
  Hits search(Query query, int numHits) throws IOException;

  /** Return an HTML-formatted explanation of how a query scored. */
  String getExplanation(Query query, Hit hit) throws IOException;
}
