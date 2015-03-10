/**
 * HTMLCheckboxFieldRenderer - Renders a CheckboxField to an HTML Checkbox.
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
package com.taursys.html.render;

import com.taursys.xml.render.CheckboxFieldRenderer;
import com.taursys.xml.CheckboxField;
import com.taursys.model.ModelException;
import com.taursys.model.CheckboxModel;
import com.taursys.dom.DocumentAdapter;
import com.taursys.xml.event.RenderException;
import com.taursys.xml.DocumentComponent;
import org.w3c.dom.*;

/**
 * Subcomponent which can render a value from a CheckboxField to an HTML Checkbox.
 * If the CheckboxModel isSelected returns true, then the "checked" attribute
 * is added to the element, otherwise it is removed.
 */
public class HTMLCheckboxFieldRenderer extends CheckboxFieldRenderer {

  /**
   * Constructs a new HTMLCheckboxFieldRenderer for given field.
   */
  public HTMLCheckboxFieldRenderer(CheckboxField field) {
    super(field);
  }

  /**
   * Renders the CheckboxField to a HTML Checkbox field.
   * If the CheckboxModel isSelected returns true, then the "checked" attribute
   * is added to the element, otherwise it is removed.
   * This component accesses the given field's parent to obtain the
   * DocumentAdapter.  It also accesses the given field's id and model.
   * @throws RenderException if any problem occurs during rendering
   */
  public void render() throws RenderException {
    init();
    if (getComponent().isVisible()) {
      restoreSelf();
      try {
        DocumentAdapter da = getComponent().getParent().getDocumentAdapter();
        Element element = (Element)da.getElementById(((DocumentComponent)getComponent()).getId());
        CheckboxField field = (CheckboxField)getComponent();
        CheckboxModel model = (CheckboxModel)field.getModel();
        // Store selected value
        element.setAttribute(field.getAttributeName(), field.getSelectedValue());
        // Set checked attribute
        if (model.isSelected()) {
          element.setAttribute("checked","checked");
        } else {
          element.removeAttribute("checked");
        }
      } catch (ModelException ex) {
        throw new RenderException(RenderException.REASON_MODEL_EXCEPTION, ex);
      }
    } else {
      removeSelf();
    }
  }
}
