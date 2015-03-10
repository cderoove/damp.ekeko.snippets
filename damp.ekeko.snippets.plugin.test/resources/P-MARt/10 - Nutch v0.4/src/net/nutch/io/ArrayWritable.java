/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.*;
import java.lang.reflect.Array;

/** A Writable for arrays containing instances of a class. */
public class ArrayWritable implements Writable {
  private Class valueClass;
  private Writable[] values;

  public ArrayWritable(Class valueClass) {
    this.valueClass = valueClass;
  }

  public ArrayWritable(Class valueClass, Writable[] values) {
    this(valueClass);
    this.values = values;
  }

  public ArrayWritable(String[] strings) {
    this(UTF8.class, new Writable[strings.length]);
    for (int i = 0; i < strings.length; i++) {
      values[i] = new UTF8(strings[i]);
    }
  }

  public String[] toStrings() {
    String[] strings = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      strings[i] = values[i].toString();
    }
    return strings;
  }

  public Object toArray() {
    Object result = Array.newInstance(valueClass, values.length);
    for (int i = 0; i < values.length; i++) {
      Array.set(result, i, values[i]);
    }
    return result;
  }

  public void set(Writable[] values) { this.values = values; }

  public Writable[] get() { return values; }

  public void readFields(DataInput in) throws IOException {
    values = new Writable[in.readInt()];          // construct values
    for (int i = 0; i < values.length; i++) {
      Writable value;                             // construct value
      try {
        value = (Writable)valueClass.newInstance();
      } catch (InstantiationException e) {
        throw new RuntimeException(e.toString());
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e.toString());
      }
      value.readFields(in);                       // read a value
      values[i] = value;                          // store it in values
    }
  }

  public void write(DataOutput out) throws IOException {
    out.writeInt(values.length);                 // write values
    for (int i = 0; i < values.length; i++) {
      values[i].write(out);
    }
  }

}

