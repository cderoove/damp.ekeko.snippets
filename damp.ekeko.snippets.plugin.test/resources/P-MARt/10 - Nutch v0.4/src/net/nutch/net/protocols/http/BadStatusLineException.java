/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;


/** Exception indicating that the status line could not be parsed. */
public class BadStatusLineException extends HttpException {
  BadStatusLineException(String msg, Throwable cause) {
    super( msg, cause);
  }
}
