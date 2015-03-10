/**
 * TemplateInputDispatcher - Dispatcher of InputEvents for Templates
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

import com.taursys.xml.Parameter;
import com.taursys.xml.Component;
import com.taursys.xml.Container;
import com.taursys.xml.Template;
import com.taursys.debug.Debug;
import java.util.Map;
import com.taursys.model.CollectionValueHolder;

/**
 * TemplateInputDispatcher is a Dispatcher for InputEvents for Templates
 * @author Marty Phelan
 * @version 1.0
 */
public class TemplateInputDispatcher extends InputDispatcher {
  private Template template;

  /**
   * Constructs a new TemplateInputDispatcher
   */
  public TemplateInputDispatcher(Template template) {
    this.template = template;
  }

  /**
   * Dispatches an Event to each registered component with the given key/value
   * <code>Map</code>. This method invokes the <code>dispatchToComponent</code>
   * for each registered component.
   * @param map a Map containing message key/values for dispatching
   */
  public void dispatch(Map map) throws Exception {
    CollectionValueHolder collectionValueHolder =
        template.getCollectionValueHolder();
    if (collectionValueHolder != null) {
      resetIndex();
      collectionValueHolder.reset();
      while (collectionValueHolder.hasNext()) {
        collectionValueHolder.next();
        super.dispatch(map);
        incrementIndex();
      }
    } else {
      Debug.warn("Template with id=" + template.getId()
          + " has a null CollectionValueHolder during input processing.");
    }
  }
}
