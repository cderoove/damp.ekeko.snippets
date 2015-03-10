/**
 * ChainedException - Exception which contains source exception.
 *
 * Copyright (c) 2000, 2001, 2002
 *      Marty Phelan, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
// Based partly on example by Barry Mosher in Java Developers Journal (March 2000)
package com.taursys.util;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A ChainedException is a subclass of Exception which contains a Throwable cause.
 * This allows for the creation of a single linked list of exceptions and their
 * respective causes.  The printStackTrace() methods display the trace for this
 * exception and then invoke the method for the cause (which can in turn invoke
 * a trace for its ancestory).
 * <p>
 * The ChainedException also provides for diagnostic information.  This
 * information will be displayed in the stack trace, but not included in the
 * basic message. The diagnostic information is available through the
 * getLocalizedMessage method.
 */
public class ChainedException extends Exception {
  public static final int REASON_INVALID_REASON_CODE          = 0;
  public static final int REASON_UNSPECIFIED                  = 1;
  private static final String[] messages = new String[] {
    "Invalid reason code.",
    "Unspecified reason",
  };
  protected Throwable cause;
  protected String diagnosticInfo;
  protected int reason = REASON_UNSPECIFIED;
  protected String userFriendlyMessage = "A system exception has occurred";

  // ************************************************************************
  //                       Static Class Methods
  // ************************************************************************

  /**
   * Returns String for given reason code else String for REASON_INVALID_REASON_CODE.
   */
  public static String getReasonMessage(int reason) {
    if (reason >= 0 && reason < messages.length)
      return messages[reason];
    else
      return messages[REASON_INVALID_REASON_CODE];
  }

  /**
   * Returns String for given reason code else String for REASON_INVALID_REASON_CODE.
   */
  public static String getInvalidMessage() {
    return messages[REASON_INVALID_REASON_CODE];
  }

  // ************************************************************************
  //                        Public Constructors
  // ************************************************************************

  /**
   * Creates a ModelException with a reason code.
   * The reason code will also be used to set the message.
   */
  public ChainedException(int reason) {
    super(getReasonMessage(reason));
    this.reason = reason;
  }

  /**
   * Creates a ModelException with a reason code and cause.
   * The reason code will also be used to set the message.
   */
  public ChainedException(int reason, Throwable cause) {
    super(getReasonMessage(reason)+": "+cause.getMessage());
    this.reason = reason;
    this.cause = cause;
  }

  /**
   * Creates a ModelException with a reason code, cause and diagnostic information.
   * The reason code will also be used to set the message.
   */
  public ChainedException(int reason, Throwable cause, String diagnosticInfo) {
    this(reason, cause);
    this.diagnosticInfo = diagnosticInfo;
  }

  /**
   * Creates a ModelException with a reason code and diagnostic information.
   * The reason code will also be used to set the message.
   */
  public ChainedException(int reason, String diagnosticInfo) {
    this(reason);
    this.diagnosticInfo = diagnosticInfo;
  }

  // ************************************************************************
  //                       Protected Constructors
  // ************************************************************************

  /**
   * Creates a ChainedException with a message.
   * This constructor is only available to subclasses.
   */
  protected ChainedException(String message) {
    super(message);
  }

  /**
   * Creates a ChainedException with a message.
   * This constructor is only available to subclasses.
   * This constructor does not alter the message.  It simply stores the
   * message and reason.
   */
  protected ChainedException(String message, int reason) {
    super(message);
    this.reason = reason;
  }

  /**
   * Creates a ChainedException with a message.
   * This constructor is only available to subclasses.
   * This constructor appends the cause message to given message separated
   * by a ": ".  It then stores the message, reason code and cause.
   */
  protected ChainedException(String message, int reason, Throwable cause) {
    this(message + ": " + cause.getMessage(), reason);
    this.cause = cause;
  }

  // ************************************************************************
  //                       Property Accessors
  // ************************************************************************

  /**
   * Returns the reason code.
   */
  public int getReason() {
    return reason;
  }

  /**
   * Accesses the original cause of this ChainedException.
   */
  public Throwable getCause() {
    return cause;
  }

  /**
   * Returns the current userFriendlyMessage.
   */
  public String getUserFriendlyMessage() {
    return userFriendlyMessage;
  }

  /**
   * Override of superclass method to provide diagnostic information (if exists).
   * Provides original message plus diagnostic information.
   */
  public String getLocalizedMessage() {
    if (diagnosticInfo != null && diagnosticInfo.length() > 0)
      return getMessage() + "\n" + diagnosticInfo;
    else
      return getMessage();
  }

  /**
   * Returns the current diagnostic information.
   */
  public String getDiagnosticInfo() {
    return diagnosticInfo;
  }

  // ************************************************************************
  //                            Display Methods
  // ************************************************************************

  /**
   * Prints this ChainedException and its cause to the standard error stream.
   */
  public void printStackTrace() {
    super.printStackTrace();
    if (cause != null) {
      cause.printStackTrace();
    }
  }

  /**
   * Prints this ChainedException and its cause to the given print stream.
   */
  public void printStackTrace(PrintStream printStream) {
    super.printStackTrace(printStream);
    if (cause != null) {
      cause.printStackTrace(printStream);
    }
  }

  /**
   * Prints this ChainedException and its cause to the given print writer.
   */
  public void printStackTrace(PrintWriter printWriter) {
    super.printStackTrace(printWriter);
    if (cause != null) {
      cause.printStackTrace(printWriter);
    }
  }

  // ************************************************************************
  //                     Testing/Designing Methods
  // ************************************************************************

  /**
   * Main for designing/viewing
   */
  static public void main(String[] args) {
    try {
      throw new ChainedException(1, new Exception("Cause message"),"Trace x=1, y=2");
    } catch (Exception ex) {
      System.out.println("Exception toString:\n===========\n"+ex);
      System.out.println("==========================================================");
      System.out.println("Exception Message:\n===========\n"+ex.getMessage());
      System.out.println("==========================================================");
      System.out.println("Exception Localized Message:\n=================\n"+ex.getLocalizedMessage());
      System.out.println("==========================================================");
      System.out.println("Stack Trace:");
      System.out.println("==========================================================");
      ex.printStackTrace();
    }
  }
}
