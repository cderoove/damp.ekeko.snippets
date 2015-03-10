/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.io;

import java.io.DataOutput;
import java.io.DataInput;
import java.io.IOException;

/** A simple, efficient, serialization protocol, based on {@link DataInput} and
 * {@link DataOutput}.
 *
 * <p>Implementations typically implement a static <code>read(DataInput)</code>
 * method which constructs a new instance, calls {@link
 * #readFields(DataInput)}, and returns the instance.
 *
 * @author Doug Cutting
 */
public interface Writable {
  /** Writes the fields of this object to <code>out</code>. */
  void write(DataOutput out) throws IOException;

  /** Reads the fields of this object from <code>in</code>.  For efficiency,
   * implementations should attempt to re-use storage in the existing object
   * where possible.
   */
  void readFields(DataInput in) throws IOException;
}
