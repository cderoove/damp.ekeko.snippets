/**
 * TestServletInputStream - Extension of ServletInputStream for testing
 *
 * Copyright (c) 2001
 *      Marty Phelan, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package com.taursys.servlet.test;

import javax.servlet.ServletInputStream;
import java.io.*;

/**
 * Extension of ServletInputStream for testing
 */
public class TestServletInputStream extends ServletInputStream {
  private int position = -1;
  private ByteArrayInputStream stream;

  public TestServletInputStream() {
  }

  public void setData(byte[] newData) {
    stream = new ByteArrayInputStream(newData);
  }

  public int read() throws java.io.IOException {
    return stream.read();
  }

  public long skip(long n) throws java.io.IOException {
    return stream.skip( n);
  }

  public synchronized void mark(int readlimit) {
    stream.mark( readlimit);
  }

  public boolean markSupported() {
    return stream.markSupported();
  }

  public int read(byte[] parm1, int parm2, int parm3) throws java.io.IOException {
    return stream.read( parm1,  parm2,  parm3);
  }

  public int available() throws java.io.IOException {
    return stream.available();
  }

  public synchronized void reset() throws java.io.IOException {
    stream.reset();
  }

  public void close() throws java.io.IOException {
    stream.close();
  }

  public int read(byte[] parm1) throws java.io.IOException {
    return stream.read( parm1);
  }
}
