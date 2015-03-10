/**
 * CodeGen - Application to generate Mapper Source Code
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
 */
package com.taursys.tools;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import com.taursys.debug.Debug;

/**
 * Application to Generate Mapper Source Code
 * @author Marty Phelan
 * @version 1.0
 */
public class CodeGen {
  boolean packFrame = false;

  /**Construct the application*/
  public CodeGen() {
    // Load CodeGen Properties
    Settings settings = new UserSettings();
    try {
      settings.loadSettings();
    } catch (IOException ex) {
      Debug.error("Problem loading CodeGen settings", ex);
      JOptionPane.showMessageDialog(null,
          "Problem loading CodeGen properties: " + ex.getMessage(),
          "Mapper CodeGen Error", JOptionPane.ERROR_MESSAGE);
    }
    // Start Code Generator Engine
    CodeGenerator generator = CodeGenerator.getInstance();
    try {
      generator.setProperties(settings.getProperties());
      generator.initialize();
    } catch (Exception ex) {
      Debug.error("Problem loading CodeGen settings", ex);
      JOptionPane.showMessageDialog(null,
          "Problem initializing code generator: " + ex.getMessage(),
          "Mapper CodeGen Error", JOptionPane.ERROR_MESSAGE);
    }
    // Create MainFrame
    MainFrame frame = new MainFrame(settings, generator);
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  /**
   * Main method sets Look and Feel and Instantiates CodeGen application
   */
  public static void main(String[] args) {
    try {
      String laf = UIManager.getSystemLookAndFeelClassName();
      UIManager.setLookAndFeel(laf);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    new CodeGen();
  }
}
