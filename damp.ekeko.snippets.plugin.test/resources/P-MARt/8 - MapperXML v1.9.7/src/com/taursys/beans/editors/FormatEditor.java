/**
 * FormatterEditor - Bean Editor for choosing a Format
 *
 * Copyright (c) 2000
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
package com.taursys.beans.editors;

import java.beans.*;
import java.text.*;

public class FormatEditor extends PropertyEditorSupport {
  private static String[] resourceStrings = { "none", "Decimal Format", "Simple Date Format", "Message Format", };
  private static String[] sourceCodeStrings = { null, "java.text.DecimalFormat.getInstance()", "java.text.SimpleDateFormat.getInstance()", "new java.text.MessageFormat(\"\")", };
  private static String[] classNameStrings = { null, "java.text.DecimalFormat", "java.text.SimpleDateFormat", "java.text.MessageFormat", };

  public FormatEditor() {
  }

  public String[] getTags() {
    return resourceStrings;
  }

  public String getJavaInitializationString() {
    String className = getValue().getClass().getName();
    for (int i = 0; i < classNameStrings.length; i++) {
      if (className.equals(classNameStrings[i])) {
        return sourceCodeStrings[i];
      }
    }
    return null;
  }

  public String getAsText() {
    Object o = getValue();
    if (o==null)
      return "none";
    String className = o.getClass().getName();
    for (int i = 0; i < classNameStrings.length; i++) {
      if (className.equals(classNameStrings[i])) {
        return resourceStrings[i];
      }
    }
    return "unknown";
  }

  public void setAsText(String text) throws IllegalArgumentException {
    for (int i = 0; i < resourceStrings.length; i++) {
      if (text.equals(resourceStrings[i])) {
        Format form;
        if (classNameStrings[i]==null)
          form = null;
        else if (classNameStrings[i].equals("java.text.MessageFormat"))
          form = new MessageFormat("");
        else
          try {
            form = (Format)Class.forName(classNameStrings[i]).newInstance();
          } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot create class: " + classNameStrings[i]);
          }
        setValue(form);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
