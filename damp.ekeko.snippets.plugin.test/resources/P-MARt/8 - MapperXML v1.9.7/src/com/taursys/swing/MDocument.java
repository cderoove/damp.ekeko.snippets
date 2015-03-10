/**
 * MDocument - Document Implementation which binds to a value in a TextModel
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

import javax.swing.text.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.taursys.model.*;
import com.taursys.model.event.*;
import com.taursys.util.*;
import java.util.*;

/**
 * MDocument is a Document Implementation which binds to a value in a TextModel
 * @author Marty Phelan
 * @version 1.0
 */
public class MDocument extends PlainDocument implements ChangeListener {
  private TextModel model;
//  private com.taursys.model.ValueHolder valueHolder;
  private boolean ignoreChangeEvent = false;
  private boolean modified = false;
  private boolean isRetrieving = false;
  transient private Vector enableListeners;

  /**
   * Constructs a new MDocument and its default TextModel (not Document).
   * The default model, a DefaultTextModel, is created via the
   * createDefaultTextModel method.  By default, the DefaultTextModel creates and
   * uses a VariantValueHolder of type String.
   */
  public MDocument() {
    setModel(createDefaultTextModel());
    retrieveValue();
  }

  /**
   * Creates a new MDocument and a DefaultTextModel with a VariantValueHolder of the given type.
   * See com.taursys.util.DataTypes for defined data type constants TYPE_XXXXXX.
   * @throws UnsupportedDataTypeException if invalid javaDataType is given
   */
  public MDocument(int javaDataType) throws UnsupportedDataTypeException {
    setModel(new DefaultTextModel(javaDataType));
    retrieveValue();
  }

  /**
   * Returns the model for this component
   */
  public TextModel getModel() {
    return model;
  }

  /**
   * Sets the TextModel used by this Document.
   * If the given TextModel does not have a defined format, the format
   * and pattern are copied from the current TextModel.
   * Removes this Document as a change listener from the current
   * TextModel (if any) and adds this as a change listener to the
   * given TextModel.
   */
  public void setModel(TextModel newModel) {
    if (model != null) {
      if (newModel.getFormat() == null) {
        newModel.setFormat(model.getFormat());
        newModel.setFormatPattern(model.getFormatPattern());
      }
      model.removeChangeListener(this);
    }
    model = newModel;
    model.addChangeListener(this);
    fireEnableChange(
        new EnableEvent(this, isChangeable()));
  }

  /**
   * Creates the default model used by this component
   */
  protected TextModel createDefaultTextModel() {
    return new DefaultTextModel();
  }

  /**
   * Sets the Format of the TextModel.
   */
  public void setFormat(java.text.Format format) {
    model.setFormat(format);
  }

  /**
   * Returns the Format of the TextModel.
   */
  public java.text.Format getFormat() {
    return model.getFormat();
  }

  /**
   * Sets the Format patten of the TextModel.
   */
  public void setFormatPattern(String newPattern) {
    model.setFormatPattern(newPattern);
  }

  /**
   * Returns the Format pattern of the TextModel.
   */
  public String getFormatPattern() {
    return model.getFormatPattern();
  }

  /**
   * Sets the valueHolder for the model.  The valueHolder is the object
   * which holds the Object where the model stores the value.  The
   * default valueHolder is a VariantValueHolder with a javaDataType of String.
   */
  public void setValueHolder(com.taursys.model.ValueHolder newValueHolder) {
    model.setValueHolder(newValueHolder);
    fireEnableChange(
        new EnableEvent(this, isChangeable()));
  }

  /**
   * Returns the valueHolder for the model.  The valueHolder is the object
   * which holds the Object where the model stores the value.  The
   * default valueHolder is a VariantValueHolder with a javaDataType of String.
   */
  public com.taursys.model.ValueHolder getValueHolder() {
    return model.getValueHolder();
  }

  /**
   * Sets the propertyName in the valueHolder where the model stores the value.
   * This name is ignored if you are using the default model (A DefaultTextModel
   * with a VariantValueHolder).
   */
  public void setPropertyName(String newPropertyName) {
    model.setPropertyName(newPropertyName);
  }

  /**
   * Returns the propertyName in the valueHolder where the model stores the value.
   * This name is ignored if you are using the default model (A DefaultTextModel
   * with a VariantValueHolder).
   */
  public String getPropertyName() {
    return model.getPropertyName();
  }

  /**
   * Retrieves text from TextModel and stores in this Document
   */
  public void retrieveValue() {
    if (!isRetrieving) {
      try {
        isRetrieving = true;
        remove(0, this.getLength());
        insertString(0, model.getText(), null);
        setModified(false);
      } catch (Exception ex) {
        /** @todo Invoke global exception handler */
        ex.printStackTrace();
      } finally {
        isRetrieving = false;
      }
    }
  }

  /**
   * Stores Document value in TextModel
   * @throws ModelException if value cannot be stored because of invalid format or other reasons
   */
  public void storeValue() throws ModelException {
    try {
      ignoreChangeEvent = true;
      model.setText(getText(0, getLength()));
      setModified(false);
    } catch (BadLocationException ex) {
      /** @todo Invoke global exception handler */
      ex.printStackTrace();
    } finally {
      ignoreChangeEvent = false;
    }
  }

  /**
   * Invoked whenever a change event occurs so the value will be retrieved from the TextModel.
   * Only responds if the event is a ContentChangeEvent and ignoreChangeEvent is
   * not true. The value retrieved from the text model will be stored in the
   * document.
   */
  public void stateChanged(ChangeEvent e) {
    if (!ignoreChangeEvent && e instanceof ContentChangeEvent) {
      retrieveValue();
      fireEnableChange(new EnableEvent(this,
        !((ContentChangeEvent)e).isContentNull()));
    }
  }

  /**
   * Set the modified flag which indicates whether or not this document is modified.
   * @param newModified the modified flag which indicates whether or not this document is modified.
   */
  public void setModified(boolean newModified) {
    modified = newModified;
  }

  /**
   * Get the modified flag which indicates whether or not this document is modified.
   * @return the modified flag which indicates whether or not this document is modified.
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * Updates document structure as a result of text insertion.  This
   * will happen within a write lock.  Since this document simply
   * maps out lines, we refresh the line map.
   * Also marks this document as modified.
   *
   * @param chng the change event describing the dit
   * @param attr the set of attributes for the inserted text
   */
  protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng,
      AttributeSet attr) {
    super.insertUpdate( chng,  attr);
  }

  /**
   * Updates any document structure as a result of text removal.
   * This will happen within a write lock. Since the structure
   * represents a line map, this just checks to see if the
   * removal spans lines.  If it does, the two lines outside
   * of the removal area are joined together.
   * Also marks this document as modified.
   *
   * @param chng the change event describing the edit
   */
  protected void removeUpdate(AbstractDocument.DefaultDocumentEvent chng) {
    setModified(true);
    super.removeUpdate(chng);
  }

  /**
   * Creates a document leaf element.
   * Hook through which elements are created to represent the
   * document structure.  Because this implementation keeps
   * structure and content seperate, elements grow automatically
   * when content is extended so splits of existing elements
   * follow.  The document itself gets to decide how to generate
   * elements to give flexibility in the type of elements used.
   * Also marks this document as modified.
   *
   * @param parent the parent element
   * @param a the attributes for the element
   * @param p0 the beginning of the range >= 0
   * @param p1 the end of the range >= p0
   * @return the new element
   */
  protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
    setModified(true);
    return super.createLeafElement( parent,  a,  p0,  p1);
  }

  /**
   * Removes some content from the document.
   * Removing content causes a write lock to be held while the
   * actual changes are taking place.  Observers are notified
   * of the change on the thread that called this method.
   * Also marks this document as modified.
   * <p>
   * This method is thread safe, although most Swing methods
   * are not. Please see
   * <A HREF="http://java.sun.com/products/jfc/swingdoc-archive/threads.html">Threads
   * and Swing</A> for more information.
   *
   * @param offs the starting offset >= 0
   * @param len the number of characters to remove >= 0
   * @exception BadLocationException  the given remove position is not a valid
   *   position within the document
   * @see Document#remove
   */
  public void remove(int offs, int len) throws BadLocationException {
    setModified(true);
    super.remove( offs,  len);
  }

  /**
   * Inserts some content into the document.
   * Inserting content causes a write lock to be held while the
   * actual changes are taking place, followed by notification
   * to the observers on the thread that grabbed the write lock.
   * Also marks this document as modified.
   * <p>
   * This method is thread safe, although most Swing methods
   * are not. Please see
   * <A HREF="http://java.sun.com/products/jfc/swingdoc-archive/threads.html">Threads
   * and Swing</A> for more information.
   *
   * @param offs the starting offset >= 0
   * @param str the string to insert; does nothing with null/empty strings
   * @param a the attributes for the inserted content
   * @exception BadLocationException  the given insert position is not a valid
   *   position within the document
   * @see Document#insertString
   */
  public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
    setModified(true);
    super.insertString( offs,  str,  a);
  }

  /**
   * Creates a document branch element, that can contain other elements.
   * Also marks this document as modified.
   *
   * @param parent the parent element
   * @param a the attributes
   * @return the element
   */
  protected Element createBranchElement(Element parent, AttributeSet a) {
    setModified(true);
    return super.createBranchElement( parent,  a);
  }

  /**
   * Get indicator whether or not the the property value of the ValueHolder
   * can be changed.  A VO type ValueHolder can only be changed if its object
   * is not null.  A Collection type ValueHolder can only be changes if it is
   * not empty.
   */
  private boolean isChangeable() {
    if (getValueHolder() == null)
      return false;
    if (getValueHolder() instanceof VOValueHolder)  {
      return ((VOValueHolder)getValueHolder()).getObject() != null;
    } else if (getValueHolder() instanceof CollectionValueHolder) {
      if (((CollectionValueHolder)getValueHolder()).isEmpty())
        return false;
      else if (getValueHolder() instanceof VOCollectionValueHolder ||
               getValueHolder() instanceof VOListValueHolder)
        return ((CollectionValueHolder)getValueHolder()).getObject() != null;
      else
        return true;
    } else {
      return true;
    }
  }

  /**
   * Removes the given listener from the list that is notified each time the
   * enabled state changes. EnableEvents are generated whenever
   * the contents of the ValueHolder change. They indicate whether or not a
   * component can modify the current contents of the ValueHolder (not-null).
   * @param l the EnableListener to remove from the notify list.
   */
  public synchronized void removeEnableListener(EnableListener l) {
    if (enableListeners != null && enableListeners.contains(l)) {
      Vector v = (Vector) enableListeners.clone();
      v.removeElement(l);
      enableListeners = v;
    }
  }

  /**
   * Adds the given listener to the list that is notified each time the
   * enabled state changes. EnableEvents are generated whenever the contents
   * of the ValueHolder change. They indicate whether or not a component
   * can modify the current contents of the ValueHolder (not-null).
   * @param l the EnableListener to add to the notify list.
   */
  public synchronized void addEnableListener(EnableListener l) {
    Vector v = enableListeners == null ? new Vector(2) : (Vector) enableListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      enableListeners = v;
    }
  }

  /**
   * Notify the listeners that the enabled state has changed. EnableEvents are
   * generated whenever the contents of the ValueHolder change. They indicate
   * whether or not a component can modify the current contents of the
   * ValueHolder (not-null).
   * @param e the EnableEvent to send to the listeners on the notify list.
   */
  protected void fireEnableChange(EnableEvent e) {
    if (enableListeners != null) {
      Vector listeners = enableListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((EnableListener) listeners.elementAt(i)).enableChange(e);
      }
    }
  }
}
