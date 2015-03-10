/**
 * VelocitySimpleLogger -Logging System for Velocity when used with JBuilder IDE
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

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * VelocitySimpleLogger is Logging System for Velocity when used with JBuilder IDE
 * @author Marty Phelan
 * @version 1.0
 */
public class VelocitySimpleLogger implements LogSystem {

  /**
   * Constructs a new VelocitySimpleLogger
   */
  public VelocitySimpleLogger() {
  }

  /**
   * Initialize method - implementation does nothing.
   */
  public void init(RuntimeServices parm1) throws java.lang.Exception {
  }

  /**
   * Loggs the given message using the Debug logger
   */
  public void logVelocityMessage(int level, String msg) {
    switch (level) {
      case LogSystem.DEBUG_ID:
        Debug.log(Debug.DEBUG, msg);
        break;
      case LogSystem.INFO_ID:
        Debug.log(Debug.INFO, msg);
        break;
      case LogSystem.WARN_ID:
        Debug.log(Debug.WARN, msg);
        break;
      case LogSystem.ERROR_ID:
        Debug.log(Debug.ERROR, msg);
        break;
      default:
        Debug.log(Debug.INFO, msg);
        break;
    }
  }
}
