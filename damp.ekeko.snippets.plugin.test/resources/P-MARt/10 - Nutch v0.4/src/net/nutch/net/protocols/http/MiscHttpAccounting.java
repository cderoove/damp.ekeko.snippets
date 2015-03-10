/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;

import java.net.InetAddress;


/**
 *  Simple container for raw byte counts (sent and recieved) and
 *  HTTP version info, which can optionally be used for accounting.
 *  Also holds a cached InetAddress, so user can cache an address.
 */
public class MiscHttpAccounting {
  private long bytesRead= 0;
  private long bytesSent= 0;
  private int httpVersion= Http.HTTP_VER_NOTSET;
  private InetAddress addr;

  public MiscHttpAccounting() {
    bytesRead= 0;
    bytesSent= 0;
    httpVersion= Http.HTTP_VER_NOTSET;
    addr= null;
  }

  public long getBytesRead() {
    return bytesRead;
  }

  public void incrementBytesRead(long incr) {
    bytesRead+= incr;
  }

  public long getBytesSent() {
    return bytesSent;
  }

  public void incrementBytesSent(long incr) {
    bytesSent+= incr;
  }

  public int getServHttpVersion() {
    return httpVersion;
  }

  public void setServHttpVersion(int httpVersion) {
    this.httpVersion= httpVersion;
  }

  public InetAddress getAddr() {
    return addr;
  }

  public void setAddr(InetAddress addr) {
    this.addr= addr;
  }

  public void reset() {
    bytesRead= 0;
    bytesSent= 0;
    httpVersion= Http.HTTP_VER_NOTSET;
    addr= null;
  }

}
