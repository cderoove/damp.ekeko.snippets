/**
 * CollectionValueHolder - ValueHolder which manages a collection of values.
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

import java.util.Collection;
import java.util.Iterator;

/**
 * This ValueHolder manages a collection of values.  It provides access to
 * a current object via an internal iterator.  The next and reset
 * methods control the position in the collection.  The hasNext indicates
 * whether there is another object in the collection (used before invoking next).
 */
public interface CollectionValueHolder extends ValueHolder, Collection {

  /**
   * Indicates whether there is another (any) Objects in the collection.
   */
  public boolean hasNext();

  /**
   * Makes the next object in the collection available.  You should invoke
   * the hasNext method BEFORE invoking this method to ensure that there IS
   * a next object.
   */
  public void next();

  /**
   * Resets this holder so that you can iterate the collection from the beginning.
   */
  public void reset();

  /**
   * Returns the object in the current position.  You should ensure that the
   * current position is valid before invoking this method.
   */
  public Object getObject();

  /**
   * Sets (replace/copy) the object in the current position.  You should ensure
   * that the current position is valid before invoking this method.  Depending
   * on the specific implementation, the given object may either replace the
   * current object in the list, or the property values of the given object may
   * be copied to the current object in the list.
   */
  public void setObject(Object obj);

  // ====================================================================
  //            PROXY METHODS TO UNDERLYING COLLECTION
  // ====================================================================

  /**
   * Returns the number of elements in the underlying collection of this holder.
   * If the underlying collection of this holder
   * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
   * <tt>Integer.MAX_VALUE</tt>.
   *
   * @return the number of elements in the underlying collection of this holder
   */
  public int size();

  /**
   * Returns <tt>true</tt> if the underlying collection of this holder contains no elements.
   *
   * @return <tt>true</tt> if the underlying collection of this holder contains no elements
   */
  public boolean isEmpty();

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
  public boolean contains(Object o);

  /**
   * Returns an iterator over the elements in the underlying collection of this holder.
   * There are no guarantees concerning the order in which the elements are returned
   * (unless the underlying collection of this holder is an instance of some class that provides a
   * guarantee).
   *
   * @return an <tt>Iterator</tt> over the elements in the underlying collection of this holder
   */
  public Iterator iterator();

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
  public Object[] toArray();

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
  public Object[] toArray(Object[] a);

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
  public boolean add(Object o);

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
  public boolean remove(Object o);

  /**
   * Returns <tt>true</tt> if the underlying collection of this holder contains all of the elements
   * in the specified collection.
   *
   * @param c collection to be checked for containment in the underlying collection of this holder.
   * @return <tt>true</tt> if the underlying collection of this holder contains all of the elements
   *	       in the specified collection
   * @see #contains(Object)
   */
  public boolean containsAll(Collection c);

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
  public boolean addAll(Collection c);

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
  public boolean removeAll(Collection c);

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
  public boolean retainAll(Collection c);

  /**
   * Removes all of the elements from the underlying collection of this holder (optional operation).
   * This collection will be empty after this method returns unless it
   * throws an exception.
   *
   * @throws UnsupportedOperationException if the <tt>clear</tt> method is
   *         not supported by the underlying collection of this holder.
   */
  public void clear();

  /**
   * Compares the specified object with the underlying collection of this holder for equality. <p>
   *
   * While the <tt>Collection</tt> interface adds no stipulations to the
   * general contract for the <tt>Object.equals</tt>, programmers who
   * implement the <tt>Collection</tt> interface "directly" (in other words,
   * create a class that is a <tt>Collection</tt> but is not a <tt>Set</tt>
   * or a <tt>List</tt>) must exercise care if they choose to override the
   * <tt>Object.equals</tt>.  It is not necessary to do so, and the simplest
   * course of action is to rely on <tt>Object</tt>'s implementation, but
   * the implementer may wish to implement a "value comparison" in place of
   * the default "reference comparison."  (The <tt>List</tt> and
   * <tt>Set</tt> interfaces mandate such value comparisons.)<p>
   *
   * The general contract for the <tt>Object.equals</tt> method states that
   * equals must be symmetric (in other words, <tt>a.equals(b)</tt> if and
   * only if <tt>b.equals(a)</tt>).  The contracts for <tt>List.equals</tt>
   * and <tt>Set.equals</tt> state that lists are only equal to other lists,
   * and sets to other sets.  Thus, a custom <tt>equals</tt> method for a
   * collection class that implements neither the <tt>List</tt> nor
   * <tt>Set</tt> interface must return <tt>false</tt> when the underlying collection of this holder
   * is compared to any list or set.  (By the same logic, it is not possible
   * to write a class that correctly implements both the <tt>Set</tt> and
   * <tt>List</tt> interfaces.)
   *
   * @param o Object to be compared for equality with the underlying collection of this holder.
   * @return <tt>true</tt> if the specified object is equal to this
   * collection
   *
   * @see Object#equals(Object)
   * @see Set#equals(Object)
   * @see List#equals(Object)
   */
  public boolean equals(Object o);

  /**
   *
   * Returns the hash code value for the underlying collection of this holder.  While the
   * <tt>Collection</tt> interface adds no stipulations to the general
   * contract for the <tt>Object.hashCode</tt> method, programmers should
   * take note that any class that overrides the <tt>Object.equals</tt>
   * method must also override the <tt>Object.hashCode</tt> method in order
   * to satisfy the general contract for the <tt>Object.hashCode</tt>method.
   * In particular, <tt>c1.equals(c2)</tt> implies that
   * <tt>c1.hashCode()==c2.hashCode()</tt>.
   *
   * @return the hash code value for the underlying collection of this holder
   *
   * @see Object#hashCode()
   * @see Object#equals(Object)
   */
  public int hashCode();
}
