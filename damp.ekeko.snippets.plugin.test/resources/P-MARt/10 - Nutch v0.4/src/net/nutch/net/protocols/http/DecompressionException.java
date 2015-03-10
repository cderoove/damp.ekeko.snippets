/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;



/** 
 * Exception indicating that some data was recieved, but could not
 * be decompressed.
 */ 
public class DecompressionException extends HttpVersionException {
  DecompressionException(String msg) {
    super(msg);
  }
}
