/**
 * WizardPanel - A basic wizard panel which gathers info for a WizardDialog
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

import javax.swing.JPanel;
import org.apache.velocity.VelocityContext;

/**
 * WizardPanel is a basic wizard panel which gathers info for a WizardDialog.
 * You will normally subclass this and override the checkPage method.
 * @author Marty Phelan
 * @version 1.0
 */
public class WizardPanel extends JPanel {
  private VelocityContext context;

  /**
   * Constructs a new WizardPanel
   */
  public WizardPanel() {
  }

  /**
   * Check page for errors and stores values in context.
   * @throws Exception if problems found
   */
  public void checkPage() throws Exception {
  }

  /**
   * Set the VelocityContext for this WizardPanel.
   * All values will be stored and retrieved from the VelocityContext.
   * @param context the VelocityContext for this WizardPanel.
   */
  public void setContext(VelocityContext context) {
    this.context = context;
  }

  /**
   * Get the VelocityContext for this WizardPanel.
   * All values will be stored and retrieved from the VelocityContext.
   * @return the VelocityContext for this WizardPanel.
   */
  public VelocityContext getContext() {
    return context;
  }

  /**
   * Put a value into the VelocityContext under the given key name.
   */
  public void putContext(String key, Object value) {
    context.put(key, value);
  }
}
