/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;


/** 
 * Exception indicating that the Content-Length header could not be
 * parsed.
 */
public class ContentLengthParseException extends HttpException {
  ContentLengthParseException(String msg) {
    super(msg);
  }
}
