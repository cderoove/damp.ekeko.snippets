/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;



/** 
 * Exception indicating that the server closed the socket in
 * mid-chunk.
 */ 
public class ChunkEOFException extends HttpVersionException {
  ChunkEOFException(String msg) {
    super(msg);
  }
}
