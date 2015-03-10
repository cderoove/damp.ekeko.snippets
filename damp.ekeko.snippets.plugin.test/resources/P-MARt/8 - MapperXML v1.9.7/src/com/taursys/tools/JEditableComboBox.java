/**
 * JEditableComboBox - JComboBox which text can be entered without hitting return
 *
 * Copyright (c) 2000, 2001
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
package com.taursys.tools;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.Component;

/**
 * JComboBox which text can be entered without hitting return.  This is a
 * temporary workaround.  Do not use this class. It is for internal use only
 * and may change or go away.
 */
public class JEditableComboBox extends JComboBox {

  public JEditableComboBox() {
    setEditable(true);
  }

  public String getText() {
    // Get Access to Current JTextField
    Component[] guts = this.getComponents();
    for(int i=0;i<guts.length;i++) {
      if (guts[i].getClass().getName().equals("javax.swing.JTextField")) {
        return ((JTextField)guts[i]).getText();
      }
    }
    System.err.println("com.taursys.tools.JEditableComboBox.getText "
        + "Debug warning - cannot access JTextField subcomponent");
    return "";
  }

//  class MyInputVerifyer extends javax.swing.InputVerifier {
//    public boolean verify(JComponent input) {
//      try {
//        if (getMDocument().isModified())
//          getMDocument().storeValue();
//        return true;
//      } catch (ModelException ex) {
//        /** @todo Popup error message */
//        JOptionPane.showMessageDialog(input, ex.getMessage(),
//            "Input Exception", JOptionPane.ERROR_MESSAGE);
//        return false;
//      }
//    }
//  }

  /**
   * Sets current selection to given text item or adds it to the list
   */
  public void setText(String text) {
    setSelectedItem(text);
  }
}
