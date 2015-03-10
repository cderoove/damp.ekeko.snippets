/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net;

/** Interface used to limit which URLs enter Nutch.  Used by the injector and
 * the db updater.*/

public interface URLFilter {

  /* Interface for a filter that transforms a URL: it can pass the
     original URL through or "delete" the URL by returning null */
  public String filter(String url);

}
