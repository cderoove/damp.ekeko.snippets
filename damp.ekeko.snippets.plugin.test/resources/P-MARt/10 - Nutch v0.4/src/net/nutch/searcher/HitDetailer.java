/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.IOException;

/** Service that returns details of a hit within an index. */
public interface HitDetailer {
  /** Returns the details for a hit document. */
  HitDetails getDetails(Hit hit) throws IOException;
  
  /** Returns the details for a set of hits.  Hook for parallel IPC calls. */
  HitDetails[] getDetails(Hit[] hits) throws IOException;

}
