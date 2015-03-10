/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;


/** Exception indicating that a header line could not be parsed. */
public class BadHeaderLineException extends HttpException {
  BadHeaderLineException(String msg) {
    super(msg);
  }
}
