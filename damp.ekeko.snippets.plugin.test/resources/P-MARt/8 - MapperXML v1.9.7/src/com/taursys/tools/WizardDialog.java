/**
 * WizardDialog - Base class for all wizards
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
package com.taursys.tools;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import org.apache.velocity.VelocityContext;
import com.taursys.debug.Debug;

/**
 * WizardDialog is base class for all wizards.
 * @author Marty Phelan
 * @version 1.0
 */
public class WizardDialog extends JDialog {
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel wizardPanel = new JPanel();
  JPanel controlPanel = new JPanel();
  JButton jbBack = new JButton();
  JButton jbNext = new JButton();
  JButton jbFinish = new JButton();
  JButton jbCancel = new JButton();
  protected Settings settings;
  private CodeGenerator generator;
  private ArrayList pages = new ArrayList();
  private int currentPage = -1;
  private String templateName;
  private VelocityContext context = new VelocityContext();

  /**
   * Constructs a new WizardDialog
   */
  public WizardDialog(Settings settings, CodeGenerator generator) {
    this.settings = settings;
    this.generator = generator;
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jbBack.setMnemonic('B');
    jbBack.setText("< Back");
    jbBack.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbBack_actionPerformed(e);
      }
    });
    jbNext.setMnemonic('N');
    jbNext.setText("Next >");
    jbNext.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbNext_actionPerformed(e);
      }
    });
    jbFinish.setMnemonic('F');
    jbFinish.setText("Finish");
    jbFinish.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbFinish_actionPerformed(e);
      }
    });
    jbCancel.setMnemonic('C');
    jbCancel.setText("Cancel");
    jbCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbCancel_actionPerformed(e);
      }
    });
    this.getContentPane().add(wizardPanel, BorderLayout.CENTER);
    this.getContentPane().add(controlPanel, BorderLayout.SOUTH);
    controlPanel.add(jbBack, null);
    controlPanel.add(jbNext, null);
    controlPanel.add(jbFinish, null);
    controlPanel.add(jbCancel, null);
  }

  public void addPage(WizardPanel page) {
    page.setContext(context);
    pages.add(page);
  }

  public void invokeWizard() {
    currentPage = 0;
    setSize(500,500);
    wizardPanel.add((JPanel)pages.get(currentPage));
    jbBack.setEnabled(false);
    jbNext.setEnabled(pages.size() > 1);
    setVisible(true);
  }

  void jbNext_actionPerformed(ActionEvent e) {
    // invoke validate on current Wizard panel
    try {
      ((WizardPanel)pages.get(currentPage)).checkPage();
    } catch (Exception ex) {
      return;
    }
    wizardPanel.remove((JPanel)pages.get(currentPage));
    currentPage++;
    wizardPanel.add((JPanel)pages.get(currentPage));
    wizardPanel.revalidate();
    wizardPanel.repaint();
    jbBack.setEnabled(true);
    jbNext.setEnabled(pages.size() > currentPage + 1);
  }

  void jbBack_actionPerformed(ActionEvent e) {
    wizardPanel.remove((JPanel)pages.get(currentPage));
    currentPage--;
    wizardPanel.add((JPanel)pages.get(currentPage));
    wizardPanel.repaint();
    jbBack.setEnabled(currentPage > 0);
    jbNext.setEnabled(true);
  }

  protected void resetContext() {
    context = new VelocityContext();
    context.put("_author", settings.getProperty(UserSettings.AUTHOR));
    context.put("_copyright", settings.getProperty(UserSettings.COPYRIGHT));
  }

  void jbFinish_actionPerformed(ActionEvent e) {
    // invoke validate on current Wizard panel
    try {
      ((WizardPanel)pages.get(currentPage)).checkPage();
    } catch (Exception ex) {
      return;
    }
    try {
      String className = (String)context.get(ClassInfoPanel.CLASS_NAME);
      String packageName = (String)context.get(ClassInfoPanel.PACKAGE_NAME);
      String srcPath = settings.getProperty(ProjectSettings.SOURCE_PATH);
      generator.generateCode(
          templateName, context, srcPath, packageName, className);
      JOptionPane.showMessageDialog(this, "Complete");
      jbCancel_actionPerformed(e);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Problem during generating code: "
          + ex.getMessage(), "Mapper CodeGen Error", JOptionPane.ERROR_MESSAGE);
      Debug.error("Problem during generating code", ex);
    }
  }

  void jbCancel_actionPerformed(ActionEvent e) {
    pages.clear();
    currentPage = -1;
    setVisible(false);
  }

  public void setTemplateName(String newTemplateName) {
    templateName = newTemplateName;
  }

  public String getTemplateName() {
    return templateName;
  }

  public org.apache.velocity.VelocityContext getContext() {
    return context;
  }

  public void setContext(org.apache.velocity.VelocityContext newContext) {
    context = newContext;
  }

}
