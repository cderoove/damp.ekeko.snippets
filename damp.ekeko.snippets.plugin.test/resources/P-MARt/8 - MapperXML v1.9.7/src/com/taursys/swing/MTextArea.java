/**
 * MTextArea - A TextArea which which is bound to a MDocument.
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
package com.taursys.swing;

import javax.swing.*;
import com.taursys.model.*;
import com.taursys.util.*;
import javax.swing.text.*;

/**
 * MTextArea is a TextArea which which is bound to a MDocument.
 * @author Marty Phelan
 * @version 1.0
 */
public class MTextArea extends JTextArea  implements EnableListener {
  private InputVerifier secondaryInputVerifier = null;

  /**
   * Constructs a new MTextArea and underlying MDocument as a String type field.
   */
  public MTextArea() {
    super(new MDocument(),null,0,0);
    super.setInputVerifier(new MyInputVerifyer());
  }

  /**
   * Constructs a new MTextField and underlying MDocument as the given javaDataType field.
   * @throws UnsupportedDataTypeException if invalid javaDataType is given
   * @see com.taursys.util.DataTypes for defined data type constants TYPE_XXXXXX.
   */
  public MTextArea(int javaDataType) throws UnsupportedDataTypeException {
    super(new MDocument(javaDataType), null, 0, 0);
    super.setInputVerifier(new MyInputVerifyer());
  }

  /**
   * Set the secondary InputVerifier for this component.
   * This MTextField also contains its own internal InputVerifier which is invoked
   * for handling text to object parsing (Date, Number, etc). The given
   * InputVerifier is invoked after the internal InputVerifier has successfully
   * verified the input.
   * @param newInputVerifier the secondary InputVerifier for this component.
   */
  public void setSecondaryInputVerifier(InputVerifier newInputVerifier) {
    secondaryInputVerifier = newInputVerifier;
  }

  /**
   * Get the secondary InputVerifier for this component.
   * This MTextField also contains its own internal InputVerifier which is invoked
   * for handling text to object parsing (Date, Number, etc). The given
   * InputVerifier is invoked after the internal InputVerifier has successfully
   * verified the input.
   * @return the secondary InputVerifier for this component.
   */
  public InputVerifier getSecondaryInputVerifier() {
    return secondaryInputVerifier;
  }

  /**
   * The internal InputVerifier which is invoked
   * for handling text to object parsing (Date, Number, etc). The secondary
   * InputVerifier is invoked after the internal InputVerifier has successfully
   * verified the input.
   */
  private class MyInputVerifyer extends javax.swing.InputVerifier {
    private String errorMessage;

    public boolean verify(JComponent input) {
      errorMessage = "";
      try {
        if (getMDocument().isModified()) {
          getMDocument().storeValue();
        }
        if (secondaryInputVerifier == null)
          return true;
        else
          return secondaryInputVerifier.shouldYieldFocus(input);
      } catch (ModelException ex) {
        /** @todo Allow cancellation of changes - show tooltip text */
        errorMessage = ex.getUserFriendlyMessage();
//        JOptionPane.showMessageDialog(input, ex.getMessage(),
//            "Input Exception", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }

    /**
     * This method is overridden to work around a JDK Bug # 4532517
     * Descr: shouldYieldFocus does not allow side effects such as popping
     *        up a OptionPane.
     *
     * This problem first appeared in:  merlin-beta3, 1.4
     */
    public boolean shouldYieldFocus(JComponent input) {
      if (verify(input)) {
        return true;
      }

      // According to the documentation should yield focus is allowed to cause
      // side effects.  So temporarily remove the input verifier on the text
      // field.
      input.setInputVerifier(null);

      // Pop up the message dialog.
      JOptionPane.showMessageDialog(input, errorMessage,
          "Input Exception", JOptionPane.ERROR_MESSAGE);

      // Reinstall the input verifier.
      input.setInputVerifier(this);

      // Tell whoever called us that we don't want to yeild focus.
      return false;
    }
  }


  /**
   * Gets and casts the Document as a MDocument
   */
  private MDocument getMDocument() {
    return (MDocument)getDocument();
  }

  /**
   * Sets the Format of the TextModel.
   */
  public void setFormat(java.text.Format format) {
    getMDocument().setFormat(format);
  }

  /**
   * Returns the Format of the TextModel.
   */
  public java.text.Format getFormat() {
    return getMDocument().getFormat();
  }

  /**
   * Sets the Format patten of the TextModel.
   */
  public void setFormatPattern(String newPattern) {
    getMDocument().setFormatPattern(newPattern);
  }

  /**
   * Returns the Format pattern of the TextModel.
   */
  public String getFormatPattern() {
    return getMDocument().getFormatPattern();
  }

  /**
   * Sets the valueHolder for the model.  The valueHolder is the object
   * which holds the Object where the model stores the value.  The
   * default valueHolder is a VariantValueHolder with a javaDataType of String.
   */
  public void setValueHolder(com.taursys.model.ValueHolder newValueHolder) {
    getMDocument().setValueHolder(newValueHolder);
  }

  /**
   * Returns the valueHolder for the model.  The valueHolder is the object
   * which holds the Object where the model stores the value.  The
   * default valueHolder is a VariantValueHolder with a javaDataType of String.
   */
  public com.taursys.model.ValueHolder getValueHolder() {
    return getMDocument().getValueHolder();
  }

  /**
   * Sets the propertyName in the valueHolder where the model stores the value.
   * This name is ignored if you are using the default model (A DefaultTextModel
   * with a VariantValueHolder).
   */
  public void setPropertyName(String newPropertyName) {
    getMDocument().setPropertyName(newPropertyName);
  }

  /**
   * Returns the propertyName in the valueHolder where the model stores the value.
   * This name is ignored if you are using the default model (A DefaultTextModel
   * with a VariantValueHolder).
   */
  public String getPropertyName() {
    return getMDocument().getPropertyName();
  }

  /**
   * Binds this MTextField to the given Document. The given Document should
   * be an instance of MDocument for this component to function properly.
   * This method registers this component as an EnableListener with the model.
   * @param newDoc  the document to display/edit
   */
  public void setDocument(Document newDoc) {
    if (getDocument() instanceof MDocument)
      ((MDocument)getDocument()).removeEnableListener(this);
    super.setDocument(newDoc);
    if (newDoc instanceof MDocument)
      ((MDocument)newDoc).addEnableListener(this);
  }

  /**
   * Invoked whenever an EnableChange event is generated by the model.
   * The model will issue the EnableChange event to indicate whether or
   * not this control should allow edits. The enabled and editable properties
   * are set based on this.
   */
  public void enableChange(EnableEvent e) {
    setEnabled(e.isEnable());
    setEditable(e.isEnable());
  }
}


