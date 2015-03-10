/**
 * OpenFormListener - is a EventListener interface for OpenFormEvents
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
package com.taursys.xml.event;

import java.util.*;

/**
 * OpenFormListener is an EventListener interface for OpenFormEvents
 * @author Marty Phelan
 * @version 1.0
 */
public interface OpenFormListener extends EventListener {

  /**
   * Message to tell a Component to render itself to the output system.
   * @param e the RenderEvent descriptor
   */
  public void openForm(OpenFormEvent e) throws Exception;
}
