/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.fetcher;

import java.io.*;

import net.nutch.io.*;

/**********************************************************
 * Outlink is a field that holds the outlink from a single
 * HTML page.  It holds the target URL as well as anchor text.
 *
 * @author Doug Cutting
 **********************************************************/
public class Outlink implements Writable {

  private String toUrl;
  private String anchor;

  public Outlink() {}

  public Outlink(String toUrl, String anchor) {
    this.toUrl = toUrl;
    this.anchor = anchor;
  }

  public void readFields(DataInput in) throws IOException {
    toUrl = UTF8.readString(in);
    anchor = UTF8.readString(in);
  }

  /** Skips over one Outlink in the input. */
  public static void skip(DataInput in) throws IOException {
    UTF8.skip(in);                                // skip toUrl
    UTF8.skip(in);                                // skip anchor
  }

  public void write(DataOutput out) throws IOException {
    UTF8.writeString(out, toUrl);
    UTF8.writeString(out, anchor);
  }

  public static Outlink read(DataInput in) throws IOException {
    Outlink outlink = new Outlink();
    outlink.readFields(in);
    return outlink;
  }

  public String getToUrl() { return toUrl; }
  public String getAnchor() { return anchor; }


  public boolean equals(Object o) {
    if (!(o instanceof Outlink))
      return false;
    Outlink other = (Outlink)o;
    return
      this.toUrl.equals(other.toUrl) &&
      this.anchor.equals(other.anchor);
  }

  public String toString() {
    return "toUrl: " + toUrl + " anchor: " + anchor;  // removed "\n". toString, not printLine... WD.
  }

}
