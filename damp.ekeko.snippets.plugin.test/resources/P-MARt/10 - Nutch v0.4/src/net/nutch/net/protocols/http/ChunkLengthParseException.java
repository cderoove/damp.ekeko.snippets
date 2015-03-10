/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;



/** 
 * Exception indicating that a Chunk-Length could not be parsed
 * as a hex integer.
 */ 
public class ChunkLengthParseException extends HttpVersionException {
  ChunkLengthParseException(String msg) {
    super(msg);
  }
}

