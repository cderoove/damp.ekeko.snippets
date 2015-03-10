/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.*;

/** A file-based set of keys. */
public class SetFile extends MapFile {

  protected SetFile() {}                            // no public ctor

  /** Write a new set file. */
  public static class Writer extends MapFile.Writer {

    /** Create the named set for keys of the named class. */
    public Writer(String dirName, Class keyClass) throws IOException {
      super(dirName, keyClass, NullWritable.class);
    }

    /** Create the named set using the named key comparator. */
    public Writer(String dirName, WritableComparator comparator)
      throws IOException {
      super(dirName, comparator, NullWritable.class);
    }

    /** Append a key to a set.  The key must be strictly greater than the
     * previous key added to the set. */
    public void append(WritableComparable key) throws IOException{
      append(key, NullWritable.get());
    }
  }

  /** Provide access to an existing set file. */
  public static class Reader extends MapFile.Reader {

    /** Construct a set reader for the named set.*/
    public Reader(String dirName) throws IOException {
      super(dirName);
    }

    /** Construct a set reader for the named set using the named comparator.*/
    public Reader(String dirName, WritableComparator comparator)
      throws IOException {
      super(dirName, comparator);
    }

    // javadoc inherited
    public boolean seek(WritableComparable key)
      throws IOException {
      return super.seek(key);
    }

    /** Read the next key in a set into <code>key</code>.  Returns
     * true if such a key exists and false when at the end of the set. */
    public boolean next(WritableComparable key)
      throws IOException {
      return next(key, NullWritable.get());
    }

    /** Read the matching key from a set into <code>key</code>.
     * Returns <code>key</code>, or null if no match exists. */
    public WritableComparable get(WritableComparable key)
      throws IOException {
      if (seek(key)) {
        next(key);
        return key;
      } else
        return null;
    }
  }

}
