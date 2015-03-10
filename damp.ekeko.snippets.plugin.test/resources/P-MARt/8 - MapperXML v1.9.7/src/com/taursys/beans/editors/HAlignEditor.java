package com.taursys.beans.editors;

import java.beans.*;
import javax.swing.SwingConstants;

/**
 * Title:        Mapper
 * Description:  Presentation Framework for Web and GUI Applicaitons
 * Copyright:    Copyright (c) 2002
 * Company:      Taurus Systems
 * @author Marty Phelan
 * @version 2.0
 */

public class HAlignEditor extends PropertyEditorSupport {
  static private String[] resourceStrings = {
      "LEADING",
      "LEFT",
      "CENTER",
      "RIGHT",
      "TRAILING",
      };
  static private int[] intValues = {
      SwingConstants.LEADING,
      SwingConstants.LEFT,
      SwingConstants.CENTER,
      SwingConstants.RIGHT,
      SwingConstants.TRAILING,
      };
  static private String[] sourceCodeStrings = {
      "SwingConstants.LEADING",
      "SwingConstants.LEFT",
      "SwingConstants.CENTER",
      "SwingConstants.RIGHT",
      "SwingConstants.TRAILING",
      };

  public HAlignEditor() {
  }
  public String[] getTags() {
    return resourceStrings;
  }
  public String getJavaInitializationString() {
    Object value = getValue();
    for (int i = 0; i < intValues.length; i++) {
      if (value.equals(new Integer(intValues[i]))) {
        return sourceCodeStrings[i];
      }
    }
    return null;
  }
  public String getAsText() {
    Object value = getValue();
    for (int i = 0; i < intValues.length; i++) {
      if (value.equals(new Integer(intValues[i]))) {
        return resourceStrings[i];
      }
    }
    return null;
  }
  public void setAsText(String text) throws IllegalArgumentException {
    for (int i = 0; i < resourceStrings.length; i++) {
      if (text.equals(resourceStrings[i])) {
        setValue(new Integer(intValues[i]));
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
