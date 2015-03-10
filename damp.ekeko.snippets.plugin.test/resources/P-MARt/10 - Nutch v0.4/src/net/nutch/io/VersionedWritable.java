/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;

/** A base class for Writables that provides version checking.
 *
 * <p>This is useful when a class may evolve, so that instances written by the
 * old version of the class may still be processed by the new version.  To
 * handle this situation, {@link #readFields(DataInput)}
 * implementations should catch {@link VersionMismatchException}.
 *
 * @author Doug Cutting
 */
public abstract class VersionedWritable implements Writable {

  /** Return the version number of the current implementation. */
  public abstract byte getVersion();
    
  // javadoc from Writable
  public void write(DataOutput out) throws IOException {
    out.writeByte(getVersion());                  // store version
  }

  // javadoc from Writable
  public void readFields(DataInput in) throws IOException {
    byte version = in.readByte();                 // read version
    if (version != getVersion())
      throw new VersionMismatchException(getVersion(),version);
  }

    
}
