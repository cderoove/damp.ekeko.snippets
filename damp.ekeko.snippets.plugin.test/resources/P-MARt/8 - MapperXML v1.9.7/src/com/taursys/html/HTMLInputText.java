/**
 * HTMLInputText - Peer component for the HTML INPUT type="text"
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
package com.taursys.html;

import com.taursys.xml.render.AttributeTextFieldRenderer;
import com.taursys.xml.render.TextFieldRenderer;
import com.taursys.xml.TextField;

/**
 * HTMLInputText is the peer component for an html INPUT type="text" component.
 * It extends the TextField component but uses an AttributeTextFieldRenderer
 * with an attribute name of "value".  It overrides the createDefaultRenderer
 * method to return an AttributeTextFieldRenderer.
 * @author Marty Phelan
 * @version 1.0
 */
public class HTMLInputText extends TextField {

  /**
   * Constructs a new HTMLInputText input component.
   */
  public HTMLInputText() {
    setAttributeName("value");
  }

  /**
   * Creates a new HTMLInputText with a DefaultTextModel and VariantValueHolder of the given TYPE_XXX.
   * @see com.taursys.util.DataTypes
   */
  public HTMLInputText(int javaDataType) {
    super(javaDataType);
    setAttributeName("value");
  }

  /**
   * Creates the default TextFieldRenderer for this component.
   * By default this method returns a new AttributeTextFieldRenderer.
   * Override this method to define your own TextFieldRenderer.
   */
  protected TextFieldRenderer createDefaultRenderer() {
    return new AttributeTextFieldRenderer(this);
  }
}
