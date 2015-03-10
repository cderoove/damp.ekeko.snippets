/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols;

import java.io.Serializable;

/**
 * Base exception for all protocol handlers
 */
public class ProtocolException extends Exception implements Serializable {


  public ProtocolException() {
    super();
  }

  public ProtocolException(String message) {
    super(message);
  }

  public ProtocolException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProtocolException(Throwable cause) {
    super(cause);
  }

}
