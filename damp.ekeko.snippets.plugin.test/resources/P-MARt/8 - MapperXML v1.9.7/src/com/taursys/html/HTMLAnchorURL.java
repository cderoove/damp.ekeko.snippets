/**
 * HTMLAnchorURL - a TextField which stores its value in the "href" attribute.
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
 * HTMLAnchorURL is a TextField which stores its value in the "href" attribute.
 * @author Marty Phelan
 * @version 1.0
 */
public class HTMLAnchorURL extends TextField {

  /**
   * Constructs a new HTMLAnchorURL
   */
  public HTMLAnchorURL() {
    setAttributeName("href");
  }

  /**
   * Creates a new HTMLAnchorURL with a DefaultTextModel and VariantValueHolder of the given TYPE_XXX.
   * @see com.taursys.util.DataTypes
   */
  public HTMLAnchorURL(int javaDataType) {
    super(javaDataType);
    setAttributeName("href");
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
