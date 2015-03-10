/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.*;

/** A WritableComparable for longs. */
public class LongWritable implements WritableComparable {
  private long value;

  public LongWritable() {}

  public LongWritable(long value) { set(value); }

  /** Set the value of this LongWritable. */
  public void set(long value) { this.value = value; }

  /** Return the value of this LongWritable. */
  public long get() { return value; }

  public void readFields(DataInput in) throws IOException {
    value = in.readLong();
  }

  public void write(DataOutput out) throws IOException {
    out.writeLong(value);
  }

  /** Returns true iff <code>o</code> is a LongWritable with the same value. */
  public boolean equals(Object o) {
    if (!(o instanceof LongWritable))
      return false;
    LongWritable other = (LongWritable)o;
    return this.value == other.value;
  }

  public int hashCode() {
    return (int)value;
  }

  /** Compares two LongWritables. */
  public int compareTo(Object o) {
    long thisValue = this.value;
    long thatValue = ((LongWritable)o).value;
    return (thisValue<thatValue ? -1 : (thisValue==thatValue ? 0 : 1));
  }

  public String toString() {
    return Long.toString(value);
  }

  /** A Comparator optimized for LongWritable. */ 
  public static class Comparator extends WritableComparator {
    public Comparator() {
      super(LongWritable.class);
    }

    public int compare(byte[] b1, int s1, int l1,
                       byte[] b2, int s2, int l2) {
      long thisValue = readLong(b1, s1);
      long thatValue = readLong(b2, s2);
      return (thisValue<thatValue ? -1 : (thisValue==thatValue ? 0 : 1));
    }
  }

}

