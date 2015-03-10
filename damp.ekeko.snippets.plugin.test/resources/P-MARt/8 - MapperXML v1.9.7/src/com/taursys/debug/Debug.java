/**
 * Debug - Class with static methods to record debugging information
 *
 * Copyright (c) 2002
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
package com.taursys.debug;

/**
 * Debug is a singleton with static methods to log information.  Debug is proxy
 * to the actual logging class.  You use Debug with any logging system by
 * implementing a LoggerAdapter for the target system.  You can then assign
 * that loggin system to this Debug by using the setLoggerAdapter method.  By
 * default Debug will create its own SimpleLogger which outputs to the err
 * device.
 * @author Marty Phelan
 * @version 1.0
 * @see SimpleLogger
 */
public class Debug {
  private static Debug singleton = null;
  private LoggerAdapter loggerAdapter = null;
  /** DEBUG level is highest logging level */
  public static final int DEBUG   = 4;
  /** INFO level is second highest logging level */
  public static final int INFO    = 3;
  /** WARN level is third highest logging level */
  public static final int WARN    = 2;
  /** ERROR level is fourth highest logging level */
  public static final int ERROR   = 1;
  /** FATAL level is lowest logging level */
  public static final int FATAL   = 0;
  /** String array of logging level names */
  public static final String[] LEVEL_NAMES = {
      "FATAL",
      "ERROR",
      "WARN",
      "INFO",
      "DEBUG",
      "UNKNOWN",
      };

  /**
   * Private constructor to prevent direct instantiation
   */
  private Debug() {
    loggerAdapter = new SimpleLogger();
  }

  /**
   * Returns the singleton Debug instance.
   * @return singleton Debug instance.
   */
  public static Debug getInstance() {
    if (singleton == null)
      singleton = new Debug();
    return singleton;
  }

  /**
   * Sets the LoggerAdapter used by the singleton Debug.
   * @param loggerAdapter to by used by the singleton Debug.
   */
  public static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
    if (loggerAdapter == null)
      throw new IllegalArgumentException("LoggerAdapter cannot be null");
    getInstance().loggerAdapter = loggerAdapter;
  }

  /**
   * Gets the LoggerAdapter used by the singleton Debug.
   * @return loggerAdapter to by used by the singleton Debug.
   */
  public static LoggerAdapter getLoggerAdapter() {
    return getInstance().loggerAdapter;
  }

  /**
   * Writes the given DEBUG message to the log if the DEBUG logging level is enabled
   * @param message is an object which contains the message to log
   */
  public static void debug(Object message) {
    getInstance().loggerAdapter.debug(message);
  }

  /**
   * Writes the given DEBUG message and stack trace to the log if the DEBUG logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public static void debug(Object message, Throwable t) {
    getInstance().loggerAdapter.debug(message, t);
  }

  /**
   * Writes the given INFO message to the log if the INFO logging level is enabled
   * @param message is an object which contains the message to log
   */
  public static void info(Object message) {
    getInstance().loggerAdapter.info(message);
  }

  /**
   * Writes the given INFO message and stack trace to the log if the INFO logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public static void info(Object message, Throwable t) {
    getInstance().loggerAdapter.info(message, t);
  }

  /**
   * Writes the given WARN message to the log if the WARN logging level is enabled
   * @param message is an object which contains the message to log
   */
  public static void warn(Object message) {
    getInstance().loggerAdapter.warn(message);
  }

  /**
   * Writes the given WARN message and stack trace to the log if the WARN logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public static void warn(Object message, Throwable t) {
    getInstance().loggerAdapter.warn(message, t);
  }

  /**
   * Writes the given ERROR message to the log if the ERROR logging level is enabled
   * @param message is an object which contains the message to log
   */
  public static void error(Object message) {
    getInstance().loggerAdapter.error(message);
  }

  /**
   * Writes the given ERROR message and stack trace to the log if the ERROR logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public static void error(Object message, Throwable t) {
    getInstance().loggerAdapter.error(message, t);
  }

  /**
   * Writes the given FATAL message to the log if the FATAL logging level is enabled
   * @param message is an object which contains the message to log
   */
  public static void fatal(Object message) {
    getInstance().loggerAdapter.fatal(message);
  }

  /**
   * Writes the given FATAL message and stack trace to the log if the FATAL logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public static void fatal(Object message, Throwable t) {
    getInstance().loggerAdapter.fatal(message, t);
  }

  /**
   * Indicates whether or not the DEBUG level is enabled for logging.
   * @return true if DEBUG level is enabled
   */
  public static boolean isDebugEnabled() {
    return getInstance().loggerAdapter.isDebugEnabled();
  }

  /**
   * Indicates whether or not the INFO level is enabled for logging.
   * @return true if INFO level is enabled
   */
  public static boolean isInfoEnabled() {
    return getInstance().loggerAdapter.isInfoEnabled();
  }

  /**
   * Indicates whether or not the given level is enabled for logging.
   * @param level to check if enabled
   * @return true if given level is enabled
   */
  public boolean isEnabledFor(int level) {
    return getInstance().loggerAdapter.isEnabledFor(level);
  }

  /**
   * Writes the given level message to the log if the given logging level is enabled
   * @param level of log message
   * @param message is an object which contains the message to log
   */
  public static void log(int level, String message) {
    getInstance().loggerAdapter.log(level, message);
  }

  /**
   * Writes the given level message and stack trace to the log if the given logging level is enabled
   * @param level of log message
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public static void log(int level, String message, Throwable t) {
    getInstance().loggerAdapter.log(level, message, t);
  }
}
