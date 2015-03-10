/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols;

import java.net.URL;


/** A response inteface.  Makes all protocols model HTTP. */

public interface Response {

  /** Returns the URL used to retrieve this response. */
  public URL getUrl();

  /** Returns the response code. */
  public int getCode();

  /** Returns the value of a named header. */
  public String getHeader(String name);

  /** Returns the full content of the response. */
  public byte[] getContent();

  /** 
   * Returns the compressed version of the content if the server
   * transmitted a compressed version, or <code>null</code>
   * otherwise. 
   */
  public byte[] getCompressedContent();

  /**
   * Returns the number of 100/Continue headers encountered 
   */
  public int getNumContinues();

}
