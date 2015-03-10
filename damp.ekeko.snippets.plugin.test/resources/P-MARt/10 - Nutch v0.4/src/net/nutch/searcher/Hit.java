/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.nutch.io.Writable;

import java.util.logging.Logger;
import net.nutch.util.LogFormatter;

/** A document which matched a query in an index. */
public class Hit implements Writable, Comparable {
  private static final Logger LOG =
    LogFormatter.getLogger("net.nutch.searcher.Hit");

  private int indexNo;                            // index id
  private int indexDocNo;                         // index-relative id
  private float score;                            // its score

  public Hit() {}

  public Hit(int indexNo, int indexDocNo, float score) {
    this(indexDocNo, score);
    this.indexNo = indexNo;
  }
  public Hit(int indexDocNo, float score) {
    this.indexDocNo = indexDocNo;
    this.score = score;
  }

  /** Return the index number that this hit came from. */
  public int getIndexNo() { return indexNo; }
  public void setIndexNo(int indexNo) { this.indexNo = indexNo; }

  /** Return the document number of this hit within an index. */
  public int getIndexDocNo() { return indexDocNo; }

  /** Return the degree to which this document matched the query. */
  public float getScore() { return score; }

  public void write(DataOutput out) throws IOException {
    out.writeInt(indexDocNo);
    out.writeFloat(score);
  }

  public void readFields(DataInput in) throws IOException {
    indexDocNo = in.readInt();
    score = in.readFloat();
  }

  /** Display as a string. */
  public String toString() {
    return "#" + indexDocNo;
  }

  /** Compares this object with the specified object for order.*/
  public int compareTo(Object o) {
    Hit other = (Hit)o;
    if (other.score > this.score) {               // prefer higher scores
      return 1;
    } else if (other.score < this.score) {
      return -1;
    } else if (other.indexNo != this.indexNo) {
      return other.indexNo - this.indexNo;        // prefer later indexes
    } else {
      return other.indexDocNo - this.indexDocNo;  // prefer later docs
    }
  }
}
