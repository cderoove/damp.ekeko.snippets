/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.*;

/** Singleton Writable with no data. */
public class NullWritable implements Writable {

  private static final NullWritable THIS = new NullWritable();

  private NullWritable() {}                       // no public ctor

  /** Returns the single instance of this class. */
  public static NullWritable get() { return THIS; }

  public void readFields(DataInput in) throws IOException {}
  public void write(DataOutput out) throws IOException {}
}

