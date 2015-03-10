/**
 * LoggerAdapter is an Adapter used by the Debug Singleton to log debug information.
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
 * LoggerAdapter is an Adapter used by the Debug Singleton to log debug information.
 * This interface is based on the features of Log4J, so it is easily used with
 * that system.  You can implement this interface for almost any logging system.
 * @author Marty Phelan
 * @version 1.0
 */
public interface LoggerAdapter {

  /**
   * Writes the given DEBUG message to the log if the DEBUG logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void debug(Object message);

  /**
   * Writes the given DEBUG message and stack trace to the log if the DEBUG logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void debug(Object message, Throwable t);

  /**
   * Writes the given INFO message to the log if the INFO logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void info(Object message);

  /**
   * Writes the given INFO message and stack trace to the log if the INFO logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void info(Object message, Throwable t);

  /**
   * Writes the given WARN message to the log if the WARN logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void warn(Object message);

  /**
   * Writes the given WARN message and stack trace to the log if the WARN logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void warn(Object message, Throwable t);

  /**
   * Writes the given ERROR message to the log if the ERROR logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void error(Object message);

  /**
   * Writes the given ERROR message and stack trace to the log if the ERROR logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void error(Object message, Throwable t);

  /**
   * Writes the given FATAL message to the log if the FATAL logging level is enabled
   * @param message is an object which contains the message to log
   */
  public void fatal(Object message);

  /**
   * Writes the given FATAL message and stack trace to the log if the FATAL logging level is enabled
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void fatal(Object message, Throwable t);

  /**
   * Indicates whether or not the DEBUG level is enabled for logging.
   * @return true if DEBUG level is enabled
   */
  public boolean isDebugEnabled();

  /**
   * Indicates whether or not the INFO level is enabled for logging.
   * @return true if INFO level is enabled
   */
  public boolean isInfoEnabled();

  /**
   * Indicates whether or not the given level is enabled for logging.
   * @param level to check if enabled
   * @return true if given level is enabled
   */
  public boolean isEnabledFor(int level);

  /**
   * Writes the given level message to the log if the given logging level is enabled
   * @param level of log message
   * @param message is an object which contains the message to log
   */
  public void log(int level, Object message);

  /**
   * Writes the given level message and stack trace to the log if the given logging level is enabled
   * @param level of log message
   * @param message is an object which contains the message to log
   * @param t throwable to be rendered using printStackTrace
   */
  public void log(int level, Object message, Throwable t);
}
