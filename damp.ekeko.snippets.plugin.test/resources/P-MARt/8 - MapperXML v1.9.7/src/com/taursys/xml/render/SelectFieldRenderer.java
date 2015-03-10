/**
 * SelectFieldRenderer - Subcomponent which can render a SelectField value to a DOM Element.
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
package com.taursys.xml.render;

import com.taursys.xml.SelectField;
import com.taursys.model.ModelException;
import com.taursys.dom.DocumentAdapter;
import com.taursys.xml.event.RenderException;
import com.taursys.xml.DocumentComponent;

/**
 * Subcomponent which can render a value from a SelectField to a DOM Element as a TextNode.
 */
public class SelectFieldRenderer extends AbstractRenderer {

  /**
   * Constructs a new SelectFieldRender.
   * @deprecated - use constructor that takes a SelectField as parameter
   */
  public SelectFieldRenderer() {
    super(null);
  }

  /**
   * Constructs a new SelectFieldRender.
   */
  public SelectFieldRenderer(SelectField c) {
    super(c);
  }

  /**
   * Renders the given SelectField to a Text Node of Element in a Document.
   * This component accesses the given textField's parent to obtain the
   * DocumentAdapter.  It also accesses the given field's id and model.
   * It uses the DocumentAdapter's setElementText to render the model's text
   * value to the Element indicated by the id.
   * @throws RenderException if any problem occurs during rendering
   * @deprecated - use constructor that takes a SelectField as parameter and
   * render that takes no parameters.
   */
  public void render(SelectField textField) throws RenderException {
    try {
      DocumentAdapter da = textField.getParent().getDocumentAdapter();
      da.setElementText(textField.getId(), textField.getModel().getText());
    } catch (ModelException ex) {
      throw new RenderException(RenderException.REASON_MODEL_EXCEPTION, ex);
    }
  }

  /**
   * Renders the SelectField to a Text Node of Element in a Document.
   * This component accesses the given textField's parent to obtain the
   * DocumentAdapter.  It also accesses the given field's id and model.
   * It uses the DocumentAdapter's setElementText to render the model's text
   * value to the Element indicated by the id.
   * @throws RenderException if any problem occurs during rendering
   */
  public void render() throws RenderException {
    init();
    if (getComponent().isVisible()) {
      restoreSelf();
      try {
        DocumentAdapter da = getComponent().getParent().getDocumentAdapter();
        da.setElementText(((DocumentComponent)getComponent()).getId(),
            ((SelectField)getComponent()).getModel().getText());
      } catch (ModelException ex) {
        throw new RenderException(RenderException.REASON_MODEL_EXCEPTION, ex);
      }
    } else {
      removeSelf();
    }
  }
}
