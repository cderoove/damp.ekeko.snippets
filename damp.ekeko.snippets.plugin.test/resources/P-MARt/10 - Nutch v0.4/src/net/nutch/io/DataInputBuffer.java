/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.*;


/** A reusable {@link DataInput} implementation that reads from an in-memory
 * buffer.
 *
 * <p>This saves memory over creating a new DataInputStream and
 * ByteArrayInputStream each time data is read.
 *
 * <p>Typical usage is something like the following:<pre>
 *
 * DataInputBuffer buffer = new DataInputBuffer();
 * while (... loop condition ...) {
 *   byte[] data = ... get data ...;
 *   int dataLength = ... get data length ...;
 *   buffer.reset(data, dataLength);
 *   ... read buffer using DataInput methods ...
 * }
 * </pre>
 *  
 * @author Doug Cutting
 */
public class DataInputBuffer extends DataInputStream {

  private static class Buffer extends ByteArrayInputStream {
    public Buffer() {
      super(new byte[] {});
    }

    public void reset(byte[] input, int start, int length) {
      this.buf = input;
      this.count = start+length;
      this.mark = start;
      this.pos = start;
    }

    public int getPosition() { return pos; }
  }

  private Buffer buffer;
  
  /** Constructs a new empty buffer. */
  public DataInputBuffer() {
    this(new Buffer());
  }

  private DataInputBuffer(Buffer buffer) {
    super(buffer);
    this.buffer = buffer;
  }

  /** Resets the data that the buffer reads. */
  public void reset(byte[] input, int length) {
    buffer.reset(input, 0, length);
  }

  /** Resets the data that the buffer reads. */
  public void reset(byte[] input, int start, int length) {
    buffer.reset(input, start, length);
  }

  /** Returns the current position in the input. */
  public int getPosition() { return buffer.getPosition(); }

}
