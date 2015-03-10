/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.IOException;

/** Service that returns the content of a hit. */
public interface HitContent {
  /** Returns the content of a hit document. */
  byte[] getContent(HitDetails details) throws IOException;

  /** Returns the anchors of a hit document. */
  String[] getAnchors(HitDetails details) throws IOException;
}
