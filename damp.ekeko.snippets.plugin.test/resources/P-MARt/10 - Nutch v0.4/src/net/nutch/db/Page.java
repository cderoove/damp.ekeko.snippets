/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.db;

import java.io.*;
import java.net.*;
import java.util.*;

import net.nutch.io.*;
import net.nutch.util.*;
import net.nutch.net.UrlNormalizer;

/*********************************************
 * A row in the Page Database.
 * <pre>
 *   type   name    description
 * ---------------------------------------------------------------
 *   byte   VERSION  - A byte indicating the version of this entry.
 *   String URL      - The url of a page.  This is the primary key.
 *   128bit ID       - The MD5 hash of the contents of the page.
 *   64bit  DATE     - The date this page should be refetched.
 *   byte   RETRIES  - The number of times we've failed to fetch this page.
 *   byte   INTERVAL - Frequency, in days, this page should be refreshed.
 *   float  SCORE   - Multiplied into the score for hits on this page.
 *   float  NEXTSCORE   - Multiplied into the score for hits on this page.
 * </pre>
 *
 * @author Mike Cafarella
 * @author Doug Cutting
 *********************************************/
public class Page implements WritableComparable, Cloneable {
  private final static byte CUR_VERSION = 4;

  private static final byte DEFAULT_INTERVAL =
    (byte)NutchConf.getInt("db.default.fetch.interval", 30);

  private UTF8 url;
  private MD5Hash md5;
  private long nextFetch = System.currentTimeMillis();
  private byte retries;
  private byte fetchInterval = DEFAULT_INTERVAL;
  private int numOutlinks;
  private float score = 1.0f;
  private float nextScore = 1.0f;

  /** Construct a page ready to be read by {@link
   * #readFields(DataInput)}.*/
  public Page() {
    url = new UTF8();       // initialize for readFields()
    md5 = new MD5Hash();    // initialize for readFields()
  }

  /** Construct a new, default page, due to be fetched. */
  public Page(String urlString, MD5Hash md5) throws MalformedURLException {
    setURL(urlString);
    this.md5 = md5;
  }

  /** Construct a new, default page, due to be fetched. */
  public Page(String urlString, float score, float nextScore, long nextFetch)
    throws MalformedURLException {
    this(urlString, score, nextScore);
    this.nextFetch = nextFetch;
  }

  /** Construct a new, default page, due to be fetched. */
  public Page(String urlString, float score, float nextScore) throws MalformedURLException {
    setURL(urlString);
    this.md5 = MD5Hash.digest(url);               // hash url, by default
    this.score = score;
    this.nextScore = nextScore;
  }

  public void readFields(DataInput in) throws IOException {
    byte version = in.readByte();                 // read version
    if (version > CUR_VERSION)                    // check version
      throw new VersionMismatchException(CUR_VERSION, version);

    url.readFields(in);
    md5.readFields(in);
    nextFetch = in.readLong();
    retries = in.readByte();
    fetchInterval = in.readByte();
    numOutlinks = (version > 2) ? in.readInt() : 0; // added in Version 3
    score = (version>1) ? in.readFloat() : 1.0f;  // score added in version 2
    nextScore = (version>3) ? in.readFloat() : 1.0f;  // 2nd score added in V4
  }

  /** Copy the contents of another instance into this instance. */
  public void set(Page that) {
    this.url.set(that.url);
    this.md5.set(that.md5);
    this.nextFetch = that.nextFetch;
    this.retries = that.retries;
    this.fetchInterval = that.fetchInterval;
    this.numOutlinks = that.numOutlinks;
    this.score = that.score;
    this.nextScore = that.nextScore;
  }

  /**
   * Write the bytes out to the bytestream
   */
  public void write(DataOutput out) throws IOException {
    out.writeByte(CUR_VERSION);                   // store current version
    url.write(out);
    md5.write(out);
    out.writeLong(nextFetch);
    out.write(retries);
    out.write(fetchInterval);
    out.writeInt(numOutlinks);
    out.writeFloat(score);
    out.writeFloat(nextScore);
  }

    /**
     * Compare to another Page object
     */
    public int compareTo(Object o) {
        int md5Result = this.md5.compareTo(((Page) o).md5);
        if (md5Result != 0) {
            return md5Result;
        }
        return this.url.compareTo(((Page) o).url);
    }


  /** Compares pages by MD5, then by URL. */
  public static class Comparator extends WritableComparator {
    public Comparator() { super(Page.class); }
    
    /** Optimized comparator. */
    public int compare(byte[] b1, int s1, int l1,
                       byte[] b2, int s2, int l2) {
      int urlLen1 = readUnsignedShort(b1, s1+1);  // skip version byte
      int urlLen2 = readUnsignedShort(b2, s2+1);
      int urlStart1 = s1+1+2;
      int urlStart2 = s2+1+2;
      int md5Start1 = urlStart1 + urlLen1;
      int md5Start2 = urlStart2 + urlLen2;
      int c = compareBytes(b1, md5Start1, MD5Hash.MD5_LEN, // compare md5
                           b2, md5Start2, MD5Hash.MD5_LEN);
      if (c != 0)
        return c;
      return compareBytes(b1, urlStart1, urlLen1, b2, urlStart2, urlLen2);
    }
  }

  /** Compares pages by URL only. */
  public static class UrlComparator extends WritableComparator {
    public UrlComparator() { super(Page.class); }
    
    public int compare(WritableComparable a, WritableComparable b) {
      Page pageA = (Page)a;
      Page pageB = (Page)b;
      
      return pageA.getURL().compareTo(pageB.getURL());
    }


    /** Optimized comparator. */
    public int compare(byte[] b1, int s1, int l1,
                       byte[] b2, int s2, int l2) {
      int urlLen1 = readUnsignedShort(b1, s1+1);  // skip version byte
      int urlLen2 = readUnsignedShort(b2, s2+1);
      int urlStart1 = s1+1+2;
      int urlStart2 = s2+1+2;
      return compareBytes(b1, urlStart1, urlLen1, b2, urlStart2, urlLen2);
    }
  }

  public static Page read(DataInput in) throws IOException {
    Page page = new Page();
    page.readFields(in);
    return page;
  }

  //
  // Accessor methods
  //
  public UTF8 getURL() { return url; }
  public void setURL(String url) throws MalformedURLException {
    this.url = new UTF8(UrlNormalizer.normalize(url));
  }

  public MD5Hash getMD5() { return md5; }
  public void setMD5(MD5Hash md5) { this.md5 = md5; }

  public long getNextFetchTime() { return nextFetch; }
  public void setNextFetchTime(long nextFetch) { this.nextFetch = nextFetch; }

  public byte getRetriesSinceFetch() { return retries; }
  public void setRetriesSinceFetch(int retries) {this.retries = (byte)retries;}

  public byte getFetchInterval() { return fetchInterval; }
  public void setFetchInterval(byte fetchInterval) {
    this.fetchInterval = fetchInterval;
  }

  public int getNumOutlinks() { return numOutlinks; }
  public void setNumOutlinks(int numOutlinks) { 
    this.numOutlinks = numOutlinks;
  }

  public float getScore() { return score; }
  public float getNextScore() { return nextScore; }
  public void setScore(float score, float nextScore) { 
    this.score = score; 
    this.nextScore = nextScore;
  }

  /**
   * Compute domain ID from URL
   */
  public long computeDomainID() throws MalformedURLException {
    return MD5Hash.digest(new URL(url.toString()).getHost()).halfDigest();
  }


  /**
   * Print out the Page
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Version: " + CUR_VERSION + "\n");
    buf.append("URL: " + getURL() + "\n");
    buf.append("ID: " + getMD5() + "\n");
    buf.append("Next fetch: " + new Date(getNextFetchTime()) + "\n");
    buf.append("Retries since fetch: " + getRetriesSinceFetch() + "\n");
    buf.append("Retry interval: " + getFetchInterval() + " days\n");
    buf.append("Num outlinks: " + getNumOutlinks() + "\n");
    buf.append("Score: " + getScore() + "\n");
    buf.append("NextScore: " + getNextScore() + "\n");
    return buf.toString();
  }

  /**
   * A tab-delimited text version of the Page's data.
   */
  public String toTabbedString() {
      StringBuffer buf = new StringBuffer();
      buf.append(CUR_VERSION); buf.append("\t");
      buf.append(getURL()); buf.append("\t");
      buf.append(getMD5()); buf.append("\t");
      buf.append(getNextFetchTime()); buf.append("\t");
      buf.append(getRetriesSinceFetch()); buf.append("\t");
      buf.append(getFetchInterval()); buf.append("\t");
      buf.append(getNumOutlinks()); buf.append("\t");
      buf.append(getScore()); buf.append("\t");
      buf.append(getNextScore()); buf.append("\t");
      return buf.toString();
  }

  public boolean equals(Object o) {
    if (!(o instanceof Page))
      return false;
    Page other = (Page)o;
    return
      this.url.equals(other.url) &&
      this.md5.equals(other.md5) &&
      (this.nextFetch == other.nextFetch) &&
      (this.retries == other.retries) &&
      (this.fetchInterval == other.fetchInterval) &&
      (this.score == other.score) &&
      (this.nextScore == other.nextScore);
  }

  public int hashCode() {
    return
      url.hashCode() ^
      md5.hashCode() ^
      ((int)nextFetch) ^
      retries ^
      fetchInterval ^
      Float.floatToIntBits(score) ^
      Float.floatToIntBits(nextScore);
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

}
