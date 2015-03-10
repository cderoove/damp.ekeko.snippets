/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.IOException;

/** Service that builds a summary for a hit on a query. */
public interface HitSummarizer {
  /** Returns a summary for the given hit details.
   *
   * @param details the details of the hit to be summarized
   * @param query  indicates what should be higlighted in the summary text
   */
  String getSummary(HitDetails details, Query query) throws IOException;

  /** Returns summaries for a set of details.  Hook for parallel IPC calls.
   *
   * @param details the details of hits to be summarized
   * @param query  indicates what should be higlighted in the summary text
   */
  String[] getSummary(HitDetails[] details, Query query) throws IOException;
}
