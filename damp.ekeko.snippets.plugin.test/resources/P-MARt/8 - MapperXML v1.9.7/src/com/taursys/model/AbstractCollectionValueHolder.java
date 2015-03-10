/**
 * AbstractCollectionValueHolder - A partial implementation of a CollectionValueHolder Interface
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

import java.util.Iterator;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Collections;
import com.taursys.debug.Debug;

/**
 * A partial implementation of a CollectionValueHolder Interface.
 * This class uses an internal ObjectValueHolder and Collection to
 * implement the required contract.
 * @author Marty Phelan
 * @version 2.0
 */
public class AbstractCollectionValueHolder implements CollectionValueHolder,
    ChangeListener {
  private Collection collection;
  private Iterator iterator;
  private ValueHolder parentValueHolder = null;
  private String parentPropertyName = null;
  private com.taursys.model.ObjectValueHolder objectValueHolder;

  /**
   * Constructs a new AbstractCollectionValueHolder
   */
  public AbstractCollectionValueHolder(ObjectValueHolder holder) {
    this(holder, new ArrayList());
  }

  /**
   * Constructs a new AbstractCollectionValueHolder for the given collection.
   */
  public AbstractCollectionValueHolder(ObjectValueHolder holder,
      Collection collection) {
    objectValueHolder = holder;
    setCollection(collection);
  }

  // ====================================================================
  //            PROXY METHODS TO INTERNAL OBJECT VALUE HOLDER
  //                   ValueHolder Interface Methods
  // ====================================================================

  /**
   * Get the internal ObjectValueHolder for this CollectionValueHolder.
   * The internal ObjectValueHolder is specified in the constructor.
   * @return the internal ObjectValueHolder for this CollectionValueHolder.
   */
  protected ObjectValueHolder getObjectValueHolder() {
    return objectValueHolder;
  }

  /**
   * Get the value of the given property in the valueObject.
   * @return the value of the given property in the valueObject.
   */
  public Object getPropertyValue(String propertyName) throws ModelException {
    return objectValueHolder.getPropertyValue(propertyName);
  }

  /**
   * Get the values for the given property names.
   * This method returns an empty Object array if the given propertyNames
   * is null or empty.
   * @param propertyNames array of property names
   * @return the values for the given property names.
   */
  public Object[] getPropertyValues(String[] propertyNames)
      throws ModelException {
    return objectValueHolder.getPropertyValues(propertyNames);
  }

  /**
   * Set the value of the given property in the valueObject.
   * Fires a StateChanged event to any listeners.
   * @param propertyName the property name to set
   * @param value the value to set the property to
   */
  public void setPropertyValue(String propertyName, Object value)
      throws ModelException {
    objectValueHolder.setPropertyValue(propertyName, value);
  }


  /**
   * Set the values for the given properties in the valueObject.
   * Fires a StateChanged event to any listeners.
   * @param propertyNames the property names to set
   * @param values the values to set the properties to
   */
  public void setPropertyValues(String[] propertyNames, Object[] values)
      throws ModelException {
    objectValueHolder.setPropertyValues(propertyNames, values);
  }

  /**
   * Get the java data type for the given property
   * @return the java data type for the given property
   */
  public int getJavaDataType(String propertyName) throws ModelException {
    return objectValueHolder.getJavaDataType(propertyName);
  }

  /**
   * Get the alias name for this ValueHolder.  This property is used by the
   * ComponentFactory to bind Components to ValueHolders by matching it to the
   * first part of the Component's ID property.
   * @return the alias name for this ValueHolder
   */
  public String getAlias() {
    return objectValueHolder.getAlias();
  }

  /**
   * Sets the alias name for this value holder.  This property is used by the
   * ComponentFactory to bind Components to value holders by matching it to the
   * first part of the Component's ID property.
   * @param newAlias the alias name for this value holder
   */
  public void setAlias(String newAlias) {
    objectValueHolder.setAlias(newAlias);
  }

  /**
   * Removes the specified change listener so that it no longer receives change
   * events from this value holder. Change events are generated whenever the
   * contents of the value holder change.
   * @param l the change listener to remove
   */
  public void removeChangeListener(ChangeListener l) {
    objectValueHolder.removeChangeListener(l);
  }

  /**
   * Adds the specified change listener to receive change events from this
   * value holder. Change events are generated whenever the contents of the
   * value holder change.
   * @param l the change listener to add
   */
  public void addChangeListener(ChangeListener l) {
    objectValueHolder.addChangeListener(l);
  }

  /**
   * Returns the object in the current position.  You should ensure that the
   * current position is valid before invoking this method.
   */
  public Object getObject() {
    return objectValueHolder.getObject();
  }

  /**
   * Replaces the object in the current position with the given one.  You should
   * ensure that the current position is valid before invoking this method.
   * The order of unordered collections may be disturbed by this operation.
   */
  public void setObject(Object obj) {
    Object old = getObject();
    objectValueHolder.setObject(obj);
    collection.remove(old);
    collection.add(obj);
  }

  // ***********************************************************************
  // *                         PARENT RELATED METHODS
  // ***********************************************************************

  /**
   * Set the parent ValueHolder for this VOCollectionValueHolder.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @param parentValueHolder the parent ValueHolder for this VOCollectionValueHolder.
   */
  public void setParentValueHolder(ValueHolder parentValueHolder) {
    if (parentValueHolder != null)
      parentValueHolder.removeChangeListener(this);
    this.parentValueHolder = parentValueHolder;
    if (parentValueHolder != null)
      parentValueHolder.addChangeListener(this);
  }

  /**
   * Get the parent ValueHolder for this VOCollectionValueHolder.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @return the parent ValueHolder for this VOCollectionValueHolder.
   */
  public ValueHolder getParentValueHolder() {
    return parentValueHolder;
  }

  /**
   * Set the property name of the Collection in the parentValueHolder for this
   * VOCollectionValueHolder.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @param parentValueHolder the property name of the Collection in the
   * parentValueHolder for this VOCollectionValueHolder.
   */
  public void setParentPropertyName(String parentPropertyName) {
    this.parentPropertyName = parentPropertyName;
  }

  /**
   * Get the property name of the Collection in the parentValueHolder for this
   * VOCollectionValueHolder.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @return the property name of the Collection in the parentValueHolder
   * for this VOCollectionValueHolder.
   */
  public String getParentPropertyName() {
    return parentPropertyName;
  }

  /**
   * Invoked by the parentValueHolder whenever there is a change in its value.
   * The parent ValueHolder will provide the Collection for this ValueHolder.
   * If the parent is also a CollectionValueHolder, then whenever the
   * parent moves to a new row, this VOCollectionValueHolder will receive
   * a notification and will retrieve its new collection from the parent.
   * @param e the ChangeEvent from the parentValueHolder
   */
  public void stateChanged(ChangeEvent e) {
    try {
      if (parentPropertyName != null && parentPropertyName.length() > 0) {
        Collection c = (Collection)parentValueHolder.getPropertyValue(
            parentPropertyName);
        setCollection(c);
      }
    } catch (Exception ex) {
      com.taursys.debug.Debug.error(
          "Problem getting new collection from parent",ex);
      setCollection(null);
    }
  }

  // ***********************************************************************
  // *                  POSITION RELATED METHODS
  // *              CollectionValueHolder Interface
  // ***********************************************************************

  /**
   * Indicates whether there is another (any) Objects in the collection.
   * If the internal iterator for the collection has not yet been created,
   * this method will invoke the getInternalIterator method.  This method
   * returns the results of the iterator's hasNext method.
   */
  public boolean hasNext() {
    if (getInternalIterator() != null)
      return iterator.hasNext();
    else
      return false;
  }

  /**
   * Makes the next object in the collection available.  You should invoke
   * the hasNext method BEFORE invoking this method to ensure that there IS
   * a next object.  The ValueObject is fetched from the collection and stored in
   * the valueObject property.
   */
  public void next() {
    if (getInternalIterator() != null)
      objectValueHolder.setObject(iterator.next());
    else
      objectValueHolder.setObject(null);
  }

  /**
   * Resets this holder so that you can iterate the collection from the beginning.
   * This sets the iterator and valueObject properties to null.  That ensures
   * that the iterator is recreated at the next invocation of hasNext,
   * next, or getInternalIterator.
   */
  public void reset() {
    iterator = null;
    objectValueHolder.setObject(null);
  }

  /**
   * Returns the current iterator, or creates one if the iterator is null.
   * Returns null if the collection is null.
   */
  private Iterator getInternalIterator() {
    if (iterator == null)
      if (getCollection() != null)
        iterator = getCollection().iterator();
    return iterator;
  }

  // ====================================================================
  //            PROXY METHODS TO INTERNAL COLLECTION
  // ====================================================================

  /**
   * Returns the current collection of this holder.
   */
  public Collection getCollection() {
    return collection;
  }

  /**
   * Sets the current collection for this holder and invokes reset method.
   * If the given collection is null, an Collections.EMPTY_LIST is stored
   * instead, otherwise the given collection is stored. This is done to prevent
   * NullPointerExceptions from occuring whenever any method is subsequently
   * called.
   */
  public void setCollection(Collection collection) {
    this.collection = collection == null ? Collections.EMPTY_LIST : collection;
    reset();
  }

  /**
   * Returns the number of elements in the underlying collection of this holder.
   * If the underlying collection of this holder
   * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
   * <tt>Integer.MAX_VALUE</tt>.
   *
   * @return the number of elements in the underlying collection of this holder
   */
  public int size() {
    return collection.size();
  };

  /**
   * Returns <tt>true</tt> if the underlying collection of this holder contains no elements.
   *
   * @return <tt>true</tt> if the underlying collection of this holder contains no elements
   */
  public boolean isEmpty() {
    return collection.isEmpty();
  }

  /**
   * Returns <tt>true</tt> if the underlying collection of this holder contains the specified
   * element.  More formally, returns <tt>true</tt> if and only if this
   * collection contains at least one element <tt>e</tt> such that
   * <tt>(o==null ? e==null : o.equals(e))</tt>.
   *
   * @param o element whose presence in the underlying collection of this holder is to be tested.
   * @return <tt>true</tt> if the underlying collection of this holder contains the specified
   *         element
   */
  public boolean contains(Object o) {
    return collection.contains(o);
  }

  /**
   * Returns an iterator over the elements in the underlying collection of this holder.
   * There are no guarantees concerning the order in which the elements are returned
   * (unless the underlying collection of this holder is an instance of some class that provides a
   * guarantee).
   *
   * @return an <tt>Iterator</tt> over the elements in the underlying collection of this holder
   */
  public Iterator iterator() {
    return collection.iterator();
  }

  /**
   * Returns an array containing all of the elements in the underlying collection
   * of this holder.  If the collection makes any guarantees as to what order
   * its elements are returned by its iterator, this method must return the
   * elements in the same order.<p>
   *
   * The returned array will be "safe" in that no references to it are
   * maintained by the underlying collection of this holder.  (In other words, this method must
   * allocate a new array even if the underlying collection of this holder is backed by an array).
   * The caller is thus free to modify the returned array.<p>
   *
   * This method acts as bridge between array-based and collection-based
   * APIs.
   *
   * @return an array containing all of the elements in the underlying collection of this holder
   */
  public Object[] toArray() {
    return collection.toArray();
  }

  /**
   * Returns an array containing all of the elements in the underlying collection of this holder
   * whose runtime type is that of the specified array.  If the collection
   * fits in the specified array, it is returned therein.  Otherwise, a new
   * array is allocated with the runtime type of the specified array and the
   * size of the underlying collection of this holder.<p>
   *
   * If the underlying collection of this holder fits in the specified array with room to spare
   * (i.e., the array has more elements than the underlying collection of this holder), the element
   * in the array immediately following the end of the collection is set to
   * <tt>null</tt>.  This is useful in determining the length of this
   * collection <i>only</i> if the caller knows that the underlying collection of this holder does
   * not contain any <tt>null</tt> elements.)<p>
   *
   * If the underlying collection of this holder makes any guarantees as to what order its elements
   * are returned by its iterator, this method must return the elements in
   * the same order.<p>
   *
   * Like the <tt>toArray</tt> method, this method acts as bridge between
   * array-based and collection-based APIs.  Further, this method allows
   * precise control over the runtime type of the output array, and may,
   * under certain circumstances, be used to save allocation costs<p>
   *
   * Suppose <tt>l</tt> is a <tt>List</tt> known to contain only strings.
   * The following code can be used to dump the list into a newly allocated
   * array of <tt>String</tt>:
   *
   * <pre>
   *     String[] x = (String[]) v.toArray(new String[0]);
   * </pre><p>
   *
   * Note that <tt>toArray(new Object[0])</tt> is identical in function to
   * <tt>toArray()</tt>.
   *
   * @param a the array into which the elements of the underlying collection of this holder are to be
   *        stored, if it is big enough; otherwise, a new array of the same
   *        runtime type is allocated for this purpose.
   * @return an array containing the elements of the underlying collection of this holder
   *
   * @throws ArrayStoreException the runtime type of the specified array is
   *         not a supertype of the runtime type of every element in this
   *         collection.
   */
  public Object[] toArray(Object[] a) {
    return collection.toArray(a);
  }

  /**
   * Ensures that the underlying collection of this holder contains the specified element (optional
   * operation).  Returns <tt>true</tt> if the underlying collection of this holder changed as a
   * result of the call.  (Returns <tt>false</tt> if the underlying collection of this holder does
   * not permit duplicates and already contains the specified element.)<p>
   *
   * Collections that support this operation may place limitations on what
   * elements may be added to the underlying collection of this holder.  In particular, some
   * collections will refuse to add <tt>null</tt> elements, and others will
   * impose restrictions on the type of elements that may be added.
   * Collection classes should clearly specify in their documentation any
   * restrictions on what elements may be added.<p>
   *
   * If a collection refuses to add a particular element for any reason
   * other than that it already contains the element, it <i>must</i> throw
   * an exception (rather than returning <tt>false</tt>).  This preserves
   * the invariant that a collection always contains the specified element
   * after this call returns.
   *
   * @param o element whose presence in the underlying collection of this holder is to be ensured.
   * @return <tt>true</tt> if the underlying collection of this holder changed as a result of the
   *         call
   *
   * @throws UnsupportedOperationException add is not supported by this
   *         collection.
   * @throws ClassCastException class of the specified element prevents it
   *         from being added to the underlying collection of this holder.
   * @throws IllegalArgumentException some aspect of this element prevents
   *          it from being added to the underlying collection of this holder.
   */
  public boolean add(Object o) {
    return collection.add(o);
  }

  /**
   * Removes a single instance of the specified element from this
   * collection, if it is present (optional operation).  More formally,
   * removes an element <tt>e</tt> such that <tt>(o==null ?  e==null :
   * o.equals(e))</tt>, if the underlying collection of this holder contains one or more such
   * elements.  Returns true if the underlying collection of this holder contained the specified
   * element (or equivalently, if the underlying collection of this holder changed as a result of the
   * call).
   *
   * @param o element to be removed from the underlying collection of this holder, if present.
   * @return <tt>true</tt> if the underlying collection of this holder changed as a result of the
   *         call
   *
   * @throws UnsupportedOperationException remove is not supported by this
   *         collection.
   */
  public boolean remove(Object o) {
    return collection.remove(o);
  }

  /**
   * Returns <tt>true</tt> if the underlying collection of this holder contains all of the elements
   * in the specified collection.
   *
   * @param c collection to be checked for containment in the underlying collection of this holder.
   * @return <tt>true</tt> if the underlying collection of this holder contains all of the elements
   *	       in the specified collection
   * @see #contains(Object)
   */
  public boolean containsAll(Collection c) {
    return collection.containsAll(c);
  }

  /**
   * Adds all of the elements in the specified collection to the underlying collection of this holder
   * (optional operation).  The behavior of this operation is undefined if
   * the specified collection is modified while the operation is in progress.
   * (This implies that the behavior of this call is undefined if the
   * specified collection is the underlying collection of this holder, and the underlying collection of this holder is
   * nonempty.)
   *
   * @param c elements to be inserted into the underlying collection of this holder.
   * @return <tt>true</tt> if the underlying collection of this holder changed as a result of the
   *         call
   *
   * @throws UnsupportedOperationException if the underlying collection of this holder does not
   *         support the <tt>addAll</tt> method.
   * @throws ClassCastException if the class of an element of the specified
   * 	       collection prevents it from being added to the underlying collection of this holder.
   * @throws IllegalArgumentException some aspect of an element of the
   *	       specified collection prevents it from being added to this
   *	       collection.
   *
   * @see #add(Object)
   */
  public boolean addAll(Collection c) {
    return collection.addAll(c);
  }

  /**
   *
   * Removes all the underlying collection of this holder's elements that are also contained in the
   * specified collection (optional operation).  After this call returns,
   * the underlying collection of this holder will contain no elements in common with the specified
   * collection.
   *
   * @param c elements to be removed from the underlying collection of this holder.
   * @return <tt>true</tt> if the underlying collection of this holder changed as a result of the
   *         call
   *
   * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
   * 	       is not supported by the underlying collection of this holder.
   *
   * @see #remove(Object)
   * @see #contains(Object)
   */
  public boolean removeAll(Collection c) {
    return collection.removeAll(c);
  }

  /**
   * Retains only the elements in the underlying collection of this holder that are contained in the
   * specified collection (optional operation).  In other words, removes from
   * the underlying collection of this holder all of its elements that are not contained in the
   * specified collection.
   *
   * @param c elements to be retained in the underlying collection of this holder.
   * @return <tt>true</tt> if the underlying collection of this holder changed as a result of the
   *         call
   *
   * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
   * 	       is not supported by this Collection.
   *
   * @see #remove(Object)
   * @see #contains(Object)
   */
  public boolean retainAll(Collection c) {
    return collection.retainAll(c);
  }

  /**
   * Removes all of the elements from the underlying collection of this holder (optional operation).
   * This collection will be empty after this method returns unless it
   * throws an exception.
   *
   * @throws UnsupportedOperationException if the <tt>clear</tt> method is
   *         not supported by the underlying collection of this holder.
   */
  public void clear() {
    collection.clear();
  }
}
