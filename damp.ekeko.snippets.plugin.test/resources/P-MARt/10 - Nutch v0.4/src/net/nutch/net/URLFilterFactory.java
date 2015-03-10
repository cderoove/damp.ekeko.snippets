/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net;

import net.nutch.util.*;
import java.util.logging.*;

/** Factory to create a URLFilter from "urlfilter.class" config property. */
public class URLFilterFactory {
  private static final Logger LOG =
    LogFormatter.getLogger("net.nutch.net.URLFilterFactory");

  private static final String URLFILTER_CLASS =
    NutchConf.get("urlfilter.class");

  private URLFilterFactory() {}                   // no public ctor

  private static URLFilter filter;

  /** Return the default URLFilter implementation. */
  public static URLFilter getFilter() {

    if (filter == null) {
      try {
        LOG.info("Using URL filter: " + URLFILTER_CLASS);
        Class filterClass = Class.forName(URLFILTER_CLASS);
        filter = (URLFilter)filterClass.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Couldn't create "+URLFILTER_CLASS, e);
      }
    }

    return filter;

  }

}
