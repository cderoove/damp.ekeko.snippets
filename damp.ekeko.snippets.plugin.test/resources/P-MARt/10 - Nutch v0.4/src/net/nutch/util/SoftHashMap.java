/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Map which uses {@link SoftReference}s to keep track of values.
 *
 * <p>
 *
 * This class is suitable for use as a best-effort caching mechanism.
 * Under the hood, it is simply a {@link HashMap}, which wraps it's
 * values with SoftReference</code>s.  Unlike <code>HashMap</code>,
 * however, null values should not be used- they will be
 * indistinguisable from values which have been garbage collected.
 *
 * <p>
 *
 * Values should implement {@link FinalizationNotifier}, so keys can
 * be cleaned up as values are garbage collected.  If values are added
 * which do not support this interface, the associated keys will
 * continue to be referenced by this data structure until explicitly
 * removed.
 * 
 * <p>
 * 
 * Expiration policy is determined by the JVM's implementation of
 * <code>SoftReference</code>.  
 */
public class SoftHashMap extends AbstractMap implements Map {
  Map hashMap;
  ArrayList keysToDelete;

  /**
   * An interface for Object which accept notification when an another
   * Object is finalized.
   */
  public interface FinalizationListener {
    /**
     * This method will be called when a {@link FinalizationNotifier}
     * this Object is registered with is being finalized.
     * <em>Note</em> that this method is not passed a reference to the
     * Object which is undergoing finalization, since creating a new
     * reference to that object may block finalization.
     */
    public void finalizationOccurring();
  }

  /**
   * An interface for a Objects which can notify an object when they
   * are finalized.  Upon finalization, Objects which implement this
   * interface will call the <code>finalizationOccurring</code> method
   * of all {@link FinalizationListener}s that have registered with
   * it.
   */
  public interface FinalizationNotifier {
    /**
     * Registers a {@link FinalizationListener} for this object.
     */
    public void addFinalizationListener(FinalizationListener listener);
  }

  private class MyFinalizationListener implements FinalizationListener {
    Object key;

    MyFinalizationListener(Object key, FinalizationNotifier value) {
      this.key= key;
      value.addFinalizationListener(this);
    }

    public void finalizationOccurring() {
      SoftHashMap.this.queueKeyForDeletion(key);
    }

  }

  public SoftHashMap() {
    hashMap= Collections.synchronizedMap(new HashMap());
    keysToDelete= new ArrayList(128);
  }

  public void clear() {
    hashMap.clear();
  }

  // queues a key for deletion- called by FinalizationListener which
  // is listening for a value's expiration
  protected void queueKeyForDeletion(Object key) {
    synchronized (keysToDelete) {
      purgeQueuedKeys();
      keysToDelete.add(key);
    }
  }

  // purges keys listed in keysToDelete from map
  protected void purgeQueuedKeys() {
    synchronized (keysToDelete) {
      for (int i= keysToDelete.size() - 1; i >= 0 ; i--) {
        remove(keysToDelete.get(i));
      }
      keysToDelete.clear();
    }
  }

  /** 
   * Returns true if this map contains a mapping for the specified key.
   *
   * <em>Note</em> that this method can return true if the value has
   * been garbage collected, but the key has not been cleared.
   * Additionally, the finalizer may invalidate the result of this
   * operation before a subsequent <code>get()</code> can be issued.
   */
  public boolean containsKey(Object key) {
    return hashMap.containsKey(key);
  }

  /** 
   * Not Implemented 
   *
   * <em>Note</em> that the finalizer may invalidate the result an
   * implementation would return.
   */
  public boolean containsValue(Object value) 
    throws UnsupportedOperationException {
    throw new UnsupportedOperationException("SoftHashMap.containsValue is "
                                            + "not implemented");
  }

  /** 
   * Not Implemented 
   */
  public Set entrySet() throws UnsupportedOperationException {
    throw new
      UnsupportedOperationException("SoftHashMap.entrySet() not implemented");
  }

  public Object get(Object key) {
    SoftReference ref= (SoftReference) hashMap.get(key);
    if (ref == null) {
      return null;
    }
    return ref.get();
  }

  public boolean isEmpty() {
    purgeQueuedKeys();
    return hashMap.isEmpty();
  }

  public Set keySet() {
    purgeQueuedKeys();
    return hashMap.keySet();
  }


  /** 
   * Associates the specified value with the specified key in this
   * map. If the map previously contained a mapping for this key, the
   * old value is replaced.  
   *
   * <p>
   *
   * <em>Note</em>: <code>value<code> must implemnt FinalizationNotifier
   * for keys to be freed properly when values are garbage collected.
   */
  public Object put(Object key, Object value) {
    purgeQueuedKeys();
    SoftReference oldRef=
      (SoftReference) hashMap.put(key, new SoftReference(value));

    try {
      new MyFinalizationListener(key, (FinalizationNotifier) value);
    } catch (ClassCastException e) {
      // fixme: throw an exception?  warn?
    }
    
    if (oldRef == null)
      return null;
    return oldRef.get();
  }

  public Object remove(Object key) {
    SoftReference ref= (SoftReference) hashMap.remove(key);
    if (ref == null)
      return null;
    return ref.get();
  }

  public int size() {
    purgeQueuedKeys();
    return hashMap.size();
  }

  /** 
   * Not Implemented 
   */
  public Collection values() throws UnsupportedOperationException {
    throw new
      UnsupportedOperationException("SoftHashMap: values() not implemnted");
  }

}
