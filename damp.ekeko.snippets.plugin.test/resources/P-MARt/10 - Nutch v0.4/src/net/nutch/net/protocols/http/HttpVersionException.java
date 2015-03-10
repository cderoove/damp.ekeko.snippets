/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;


/** 
 * Superclass for exceptions which indicate errors specific to
 * Http versions (when these are caught, future attempts to fetch this page
 * should consider falling back to an earlier protocol version).
 */
public abstract class HttpVersionException extends HttpException {
  HttpVersionException(String msg) {
    super(msg);
  }

  HttpVersionException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
