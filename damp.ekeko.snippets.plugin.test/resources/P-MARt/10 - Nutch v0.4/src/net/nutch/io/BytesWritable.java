/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

/** A Writable for byte arrays.
 * 
 * @author Doug Cutting
 */
public class BytesWritable implements Writable {
  private byte[] bytes;

  public BytesWritable() {}

  public BytesWritable(byte[] bytes) {
    this.bytes = bytes;
  }

  public byte[] get() {
    return bytes;
  }

  public void readFields(DataInput in) throws IOException {
    bytes = new byte[in.readInt()];
    in.readFully(bytes, 0, bytes.length);
  }

  public void write(DataOutput out) throws IOException {
    out.writeInt(bytes.length);
    out.write(bytes);
  }

}
