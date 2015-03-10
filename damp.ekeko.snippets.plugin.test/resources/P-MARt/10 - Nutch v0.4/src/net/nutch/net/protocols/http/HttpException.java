/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;

import net.nutch.net.protocols.ProtocolException;


/** Superclass for non-IO exceptions thrown during HTTP requests
 *  or parsing of responses
 */
public class HttpException extends ProtocolException {

  public HttpException() {
    super();
  }

  public HttpException(String message) {
    super(message);
  }

  public HttpException(String message, Throwable cause) {
    super(message, cause);
  }

  public HttpException(Throwable cause) {
    super(cause);
  }

}
