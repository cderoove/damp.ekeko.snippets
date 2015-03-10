/**
 * ServletFormWizard - Wizard for building a new ServletForm
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

import org.apache.velocity.VelocityContext;

/**
 * ServletFormWizard is a wizard for building a new ServletForm
 * @author Marty Phelan
 * @version 1.0
 */
public class ValueObjectWizard extends com.taursys.tools.WizardDialog {
  private static String
      TEMPLATE_NAME = "com/taursys/tools/templates/ValueObject.template";

  /**
   * Constructs a new ServletFormWizard
   */
  public ValueObjectWizard(Settings projectSettings, CodeGenerator generator) {
    super(projectSettings, generator);
  }

  /**
   * Invokes the Wizard.  The wizard is
   */
  public void invokeWizard() {
    resetContext();
    wizardPanel.removeAll();
    setTemplateName(TEMPLATE_NAME);
    addPage(new ClassInfoPanel());
    addPage(new ValueObjectPropertyInfoPanel());
    super.invokeWizard();
  }

  /**
   * For testing/designing only
   */
  static public void main(String[] args) {
    try {
      // Load CodeGen Properties
      Settings settings = new UserSettings();
      settings.setProperty(ProjectSettings.SOURCE_PATH,
          System.getProperty("user.home") + "/tmp");
      // Start Code Generator Engine
      CodeGenerator generator = CodeGenerator.getInstance();
      generator.setProperties(settings.getProperties());
      generator.initialize();
      WizardDialog wizard =
        new ValueObjectWizard(settings, generator);
      wizard.invokeWizard();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
