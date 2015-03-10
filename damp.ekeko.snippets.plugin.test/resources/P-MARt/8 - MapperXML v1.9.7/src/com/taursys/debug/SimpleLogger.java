/**
 * SimpleLogger - an implementation of a LoggerAdapter that outputs log into to System.err
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

import java.util.Date;

/**
 * SimpleLogger is an implementation of a LoggerAdapter that outputs log into to System.err.
 * Its default level is WARN.  This is a very simple implementation.  The
 * message passed to the various logging methods is rendered using its toString
 * method.
 *
 * @author Marty Phelan
 * @version 1.0
 */
public class SimpleLogger implements LoggerAdapter {
  private int level = Debug.DEBUG;

  /**
   * Constructs a new SimpleLogger
   */
  public SimpleLogger() {
  }

  /**
   * Constructs a new SimpleLogger with a given level
   */
  public SimpleLogger(int level) {
    if (level < 0 || level > Debug.DEBUG)
      throw new IllegalArgumentException("Invalid logging level - " + level);
    this.level = level;
  }

  /**
   * Writes the given DEBUG message to the log if the DEBUG logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void debug(Object message) {
    log(Debug.DEBUG, message);
  }

  /**
   * Writes the given DEBUG message and stack trace to the log if the DEBUG logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void debug(Object message, Throwable t) {
    log(Debug.DEBUG, message, t);
  }

  /**
   * Writes the given INFO message to the log if the INFO logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void info(Object message) {
    log(Debug.INFO, message);
  }

  /**
   * Writes the given INFO message and stack trace to the log if the INFO logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void info(Object message, Throwable t) {
    log(Debug.INFO, message, t);
  }

  /**
   * Writes the given WARN message to the log if the WARN logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void warn(Object message) {
    log(Debug.WARN, message);
  }

  /**
   * Writes the given WARN message and stack trace to the log if the WARN logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void warn(Object message, Throwable t) {
    log(Debug.WARN, message, t);
  }

  /**
   * Writes the given ERROR message to the log if the ERROR logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void error(Object message) {
    log(Debug.ERROR, message);
  }

  /**
   * Writes the given ERROR message and stack trace to the log if the ERROR logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void error(Object message, Throwable t) {
    log(Debug.ERROR, message, t);
  }

  /**
   * Writes the given FATAL message to the log if the FATAL logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void fatal(Object message) {
    log(Debug.FATAL, message);
  }

  /**
   * Writes the given FATAL message and stack trace to the log if the FATAL logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void fatal(Object message, Throwable t) {
    log(Debug.FATAL, message, t);
  }

  /**
   * Indicates whether or not the DEBUG level is enabled for logging.
   * @return true if DEBUG level is enabled
   */
  public boolean isDebugEnabled() {
    return level == Debug.DEBUG;
  }

  /**
   * Indicates whether or not the INFO level is enabled for logging.
   * @return true if INFO level is enabled
   */
  public boolean isInfoEnabled() {
    return level >= Debug.WARN;
  }

  /**
   * Indicates whether or not the given level is enabled for logging.
   * @param level to check if enabled
   * @return true if given level is enabled
   */
  public boolean isEnabledFor(int level) {
    return this.level >= level;
  }

  /**
   * Writes the given level message to the log if the given logging level is enabled
   * @param level of log message
   * @param message is an object which contains the message to log
   */
  public void log(int level, Object message) {
    if (this.level >= level) {
      printMessage(level, message);
    }
  }

  /**
   * Writes the given level message and stack trace to the log if the given logging level is enabled
   * @param level of log message
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void log(int level, Object message, Throwable t) {
    if (this.level >= level) {
      printMessage(level, message);
      t.printStackTrace();
    }
  }

  /**
   * Sets the logging level of this SimpleLogger
   * @param level of logging for this SimppleLogger
   */
  public void setLevel(int level) {
    if (level < 0 || level > Debug.DEBUG)
      throw new IllegalArgumentException("Invalid logging level - " + level);
    this.level = level;
  }

  /**
   * Gets the logging level of this SimpleLogger
   * @return level of logging for this SimppleLogger
   */
  public int getLevel() {
    return level;
  }

  /**
   * Prints the given message to the System.err device if given level is enabled.
   * Prefixes a level name and timestamp to the message.
   */
  private void printMessage(int level, Object message) {
    // Adjust level if needed;
    if (level < 0)
      level = Debug.DEBUG + 1;
    // [DEBUG] 01/01/2002 00:00:00 - message
    System.err.println("[" + Debug.LEVEL_NAMES[level] + "] " + new Date()
        + " - " + message);
  }
}
