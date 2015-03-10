/**
 * TextModel - Used by components to store/retrieve a text value.
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
package com.taursys.model;

import java.text.Format;
import com.taursys.model.ValueHolder;
import javax.swing.event.ChangeListener;

/**
 * <p>The <code>TextModel</code> is a foundation model for MapperXML. The
 * <code>TextModel</code> provides access to the internal "state" or value
 * via the <code>getText</code> and <code>setText</code> methods. It is the
 * responsibility of theses methods to provide any required parsing or
 * formatting as they transform the value between its <code>String</code>
 * representation and actual internal representation (<code>int</code>,
 * <code>Date</code>, <code>BigDecimal</code>, etc). There are 2 properties
 * which govern the parse/format process: <code>format</code> and
 * <code>formatPattern</code>. These use the standard
 * <code>java.text.Format</code> objects and patterns.
 *
 * <p>The <code>TextModel</code> uses a <code>ValueHolder</code> to hold the
 * actual "state" or value. A <code>ValueHolder</code> can provide storage for
 * any data type. Implementations of the <code>TextModel</code> can create and
 * use their own internal <code>ValueHolder</code>. They can also be assigned
 * an external <code>ValueHolder</code> to use. Multiple <code>TextModels</code>
 * can share the same <code>ValueHolder</code>. A specific property of the
 * <code>ValueHolder</code> host object can also be targeted. The target property
 * can be specified by setting the <code>propertyName</code> property. (Note:
 * the <code>ValueHolder</code> itself must be one that actually supports
 * access to individual properties for this feature to work, otherwise the
 * <code>propertyName</code> setting is ignored).</p>
 *
 * <p>The <code>TextModel</code> also provides notification to
 * <code>ChangeListeners</code> whenever the "state" or value changes. Interested
 * listeners can register or unregister to receive notification of
 * <code>ChangeEvents</code> by using the <code>addChangeListener</code> or
 * <code>removeChangeListener</code> methods.</p>
 */
public interface TextModel {

  /**
   * Returns the model value as a String (using Format if defined)
   */
  public String getText() throws ModelException;

  /**
   * Parses the given String (using Format if defined) and sets model value.
   */
  public void setText(String text) throws ModelException;

  /**
   * Returns the Format to be used by the model or null if undefined.
   */
  public Format getFormat();

  /**
   * Sets the Format to be used by the model or null if undefined.
   */
  public void setFormat(Format format);

  /**
   * Returns the pattern used by the format
   */
  public String getFormatPattern();

  /**
   * Sets the pattern used by the format
   */
  public void setFormatPattern(String newPattern);

  /**
   * Sets the valueHolder used by the model for storing the value.
   */
  public void setValueHolder(ValueHolder newValueHolder);

  /**
   * Returns the valueHolder used by the model for storing the value.
   */
  public com.taursys.model.ValueHolder getValueHolder();

  /**
   * Sets the ValueObject's propertyName where the model value is stored.
   */
  public void setPropertyName(String newPropertyName);

  /**
   * Returns the ValueObject's propertyName where the model value is stored.
   */
  public String getPropertyName();

  /**
   * Removes the specified change listener so that it no longer receives change events from this text model.
   * Change events are generated whenever the contents of the value holder change.
   */
  public void removeChangeListener(ChangeListener l);

  /**
   * Adds the specified change listener to receive change events from this text model.
   * Change events are generated whenever the contents of the value holder change.
   */
  public void addChangeListener(ChangeListener l);
}
