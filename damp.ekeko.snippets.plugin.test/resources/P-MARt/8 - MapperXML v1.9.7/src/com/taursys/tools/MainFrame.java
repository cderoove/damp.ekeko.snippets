/**
 * MainFrame - Main Frame for CodeGen Application
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.taursys.swing.*;
import javax.swing.filechooser.*;
import java.io.*;
import com.taursys.debug.Debug;

/**
 * MainFrame - Main Frame for CodeGen Application
 * @author Marty Phelan
 * @version 1.0
 */
public class MainFrame extends JFrame {
  Settings settings = null;
  CodeGenerator generator = null;
  Settings projectSettings;
  JPanel contentPane;
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenuItem jMenuFileExit = new JMenuItem();
  JMenu jMenuHelp = new JMenu();
  JMenuItem jMenuHelpAbout = new JMenuItem();
  JLabel statusBar = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel servletsPanel = new JPanel();
  JMenuItem jMenuFileUserSettings = new JMenuItem();
  JMenuItem jMenuFileNewProject = new JMenuItem();
  JMenuItem jMenuFileOpenProject = new JMenuItem();
  JMenu jMenuWizards = new JMenu();
  JMenuItem jMenuWizardsServletForm = new JMenuItem();
  JMenuItem jMenuWizardsServletApp = new JMenuItem();
  private JMenuItem jMenuWizardsValueObject = new JMenuItem();

  /**Construct the frame*/
  public MainFrame(Settings settings, CodeGenerator generator) {
    this.generator = generator;
    this.settings = settings;
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**Component initialization*/
  private void jbInit() throws Exception  {
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(400, 300));
    this.setTitle("Mapper CodeGen");
    statusBar.setText(" ");
    jMenuFile.setMnemonic('F');
    jMenuFile.setText("File");
    jMenuFileExit.setMnemonic('X');
    jMenuFileExit.setText("Exit");
    jMenuFileExit.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        jMenuFileExit_actionPerformed(e);
      }
    });
    jMenuHelp.setMnemonic('H');
    jMenuHelp.setText("Help");
    jMenuHelpAbout.setMnemonic('A');
    jMenuHelpAbout.setText("About");
    jMenuHelpAbout.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        jMenuHelpAbout_actionPerformed(e);
      }
    });
    jMenuFileUserSettings.setMnemonic('U');
    jMenuFileUserSettings.setText("User Settings");
    jMenuFileUserSettings.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuFileUserSettings_actionPerformed(e);
      }
    });
    jMenuFileNewProject.setMnemonic('N');
    jMenuFileNewProject.setText("New Project");
    jMenuFileNewProject.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuFileNewProject_actionPerformed(e);
      }
    });
    jMenuFileOpenProject.setMnemonic('O');
    jMenuFileOpenProject.setText("Open Project");
    jMenuFileOpenProject.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuFileOpenProject_actionPerformed(e);
      }
    });
    jMenuWizardsServletForm.setMnemonic('F');
    jMenuWizardsServletForm.setText("ServletForm");
    jMenuWizardsServletForm.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuWizardsServletForm_actionPerformed(e);
      }
    });
    jMenuWizardsServletApp.setMnemonic('A');
    jMenuWizardsServletApp.setText("ServletApp");
    jMenuWizardsServletApp.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuWizardsServletApp_actionPerformed(e);
      }
    });
    jMenuWizards.setEnabled(false);
    jMenuWizards.setMnemonic('Z');
    jMenuWizards.setText("Wizards");
    jMenuWizardsValueObject.setMnemonic('V');
    jMenuWizardsValueObject.setText("ValueObject");
    jMenuWizardsValueObject.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuWizardsValueObject_actionPerformed(e);
      }
    });
    jMenuFile.add(jMenuFileNewProject);
    jMenuFile.add(jMenuFileOpenProject);
    jMenuFile.addSeparator();
    jMenuFile.add(jMenuFileUserSettings);
    jMenuFile.addSeparator();
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuWizards);
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);
    contentPane.add(statusBar, BorderLayout.SOUTH);
    contentPane.add(servletsPanel, BorderLayout.CENTER);
    jMenuWizards.add(jMenuWizardsServletForm);
    jMenuWizards.add(jMenuWizardsServletApp);
    jMenuWizards.add(jMenuWizardsValueObject);
  }
  /**File | Exit action performed*/
  public void jMenuFileExit_actionPerformed(ActionEvent e) {
    System.exit(0);
  }
  /**Help | About action performed*/
  public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
    MainFrame_AboutBox dlg = new MainFrame_AboutBox(this);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.show();
  }
  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      jMenuFileExit_actionPerformed(null);
    }
  }

  void jButton1_actionPerformed(ActionEvent e) {
    ServletFormWizard servletFormWizard = new ServletFormWizard(settings, generator);
    servletFormWizard.invokeWizard();
  }

  void jMenuFileUserSettings_actionPerformed(ActionEvent e) {
    UserSettingsDialog dialog = new UserSettingsDialog(this, settings);
    dialog.show();
  }

  void jMenuFileNewProject_actionPerformed(ActionEvent e) {
    Settings pj = new ProjectSettings(settings);
    ProjectSettingsDialog dialog = new ProjectSettingsDialog(this, pj);
    int exitState = dialog.showDialog();
    if (exitState == JOptionPane.OK_OPTION) {
      projectSettings = pj;
      jMenuWizards.setEnabled(true);
    }
  }

  /**
   * Get the extension of a file.
   */
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');
    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i+1).toLowerCase();
    }
    return ext;
  }

  public static class MapperProjectFileFilter extends javax.swing.filechooser.FileFilter {
    public boolean accept(File f) {
      return (f.isDirectory() || ("mpj".equals(getExtension(f))));
    }

    public String getDescription() {
      return "Mapper Project";
    }
  }

  void jMenuFileOpenProject_actionPerformed(ActionEvent e) {
    JFileChooser chooser = new JFileChooser(
        settings.getProperty(UserSettings.DEFAULT_PROJECT_PATH));
    chooser.setFileFilter(new MapperProjectFileFilter());
    chooser.setDialogTitle("Mapper Project");
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selection = chooser.getSelectedFile();
      try {
        projectSettings = new ProjectSettings(settings);
        projectSettings.loadSettings(selection);
        jMenuWizards.setEnabled(true);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Problem during loading settings: "
            + ex.getMessage(), "Mapper CodeGen Error", JOptionPane.ERROR_MESSAGE);
        Debug.error("Problem during saving settings", ex);
      }
    }
  }


  void jMenuWizardsServletForm_actionPerformed(ActionEvent e) {
    WizardDialog wizard =
        new ServletFormWizard(projectSettings, generator);
    wizard.invokeWizard();
  }

  void jMenuWizardsServletApp_actionPerformed(ActionEvent e) {
    WizardDialog wizard =
        new ServletAppWizard(projectSettings, generator);
    wizard.invokeWizard();
  }

  void jMenuWizardsValueObject_actionPerformed(ActionEvent e) {
    WizardDialog wizard =
        new ValueObjectWizard(projectSettings, generator);
    wizard.invokeWizard();
  }

}
