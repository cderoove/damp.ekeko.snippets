/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.awt;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;

import javax.swing.JPanel;

/**
* A class that produces a page control component.
*
* <P>
* <TABLE BORDER COLS=3 WIDTH=100%>
* <TR><TH WIDTH=15%>Property<TH WIDTH=15%>Property Type<TH>Description
* <TR><TD> Direction      <TD> boolean <TD> Direction (upper or lower Pagecontrol)
* <TR><TD> SelectedIndex  <TD> int     <TD> Index of selected tab.
* </TABLE>
*
* @version 2.20, Jan 22, 1998
* @author  Petr Hamernik, Ian Formanek
*/
public class PageControl extends Panel implements Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4648659103970803518L;

    /** Width of the 3D border around the component */
    protected final static int margin = 1;
    /** Vector of components inserted into PageControl. 
     * @associates Component*/
    protected Vector pages;
    /** TabControl component for switching among pages. */
    protected TabControl tab;
    /** Previous dimension of the PageControl */
    private Dimension dimLast = null;
    /** Vector of listeners which are invoked when the selction has changed.
     * @associates PropertyChangeListener*/
    transient private Vector indexListeners = new Vector();

    /** Focus listener for all components inserted in the PageControl.
    * When some component gains focus that it will be selected. */
    private class PageFocusListener extends Object implements FocusListener {
        public void focusGained(FocusEvent e) {
            Component comp = e.getComponent();
            int index = getIndexOfPage(comp);
            if (index != -1)
                setSelectedIndex(index);
        }
        public void focusLost(FocusEvent e) {
        }
    }

    /** Instance of focus listener */
    PageFocusListener focusListener = new PageFocusListener();

    /** Create new PageControl with all default values. */
    public PageControl() {
        setLayout(new BorderLayout());
        tab = new TabControl();
        add(tab, "North"); // NOI18N
        validate();
        pages = new Vector();
        dimLast = new Dimension(0,0);
        //    setBackground(SystemColor.control);
        //    setForeground(SystemColor.controlText);
        tab.addIndexChangeListener(new PropertyChangeListener() {
                                       public void propertyChange(PropertyChangeEvent evt) {
                                           if (evt.getPropertyName().equals("selectedIndex")) {
                                               int oldSel = ((Integer) evt.getOldValue()).intValue();
                                               int newSel = ((Integer) evt.getNewValue()).intValue();
                                               for (int i = 0; i < tab.getTabCount(); i++) {
                                                   if (i == newSel)
                                                       ((Component) (pages.elementAt(i))).setVisible(true);
                                                   else
                                                       ((Component) (pages.elementAt(i))).setVisible(false);
                                               }
                                               fireIndexChange(oldSel, newSel);
                                           }
                                       }
                                   });
    }

    /** Select new page.
    * @param index Index of new selected page.
    * @exception ArrayIndexOutOfBoundsException If the value was invalid.
    * @see #getSelectedIndex
    */
    synchronized public void setSelectedIndex(int index) throws ArrayIndexOutOfBoundsException {
        int old = tab.getSelectedIndex();
        if (old != index) {
            tab.setSelectedIndex(index);
            ((Component) (pages.elementAt(old))).setVisible(false);
            ((Component) (pages.elementAt(index))).setVisible(true);
        }
        fireIndexChange(old, index);
    }

    /** Gets the index of currently selected page.
    * @return Index of selected page.
    * @see #setSelectedIndex
    */
    public int getSelectedIndex() {
        return tab.getSelectedIndex();
    }

    /** Gets count of the pages.
    * @return Count of pages.
    */
    public int getTabCount() {
        return tab.getTabCount();
    }

    /** Gets the name of the page with the specific index.
    * @param index Index of page.
    * @exception ArrayIndexOutOfBoundsException If the value was invalid.
    * @return Name of page.
    */
    public String getTabLabel(int index) throws ArrayIndexOutOfBoundsException {
        return tab.getTabLabel(index);
    }

    /** Add new page.
    * @param newTab Name of new page.
    * @param comp Component which will be inserted into PageControl.
    */
    synchronized public void addPage(String newTab, Component comp) {
        int index = tab.getTabCount();
        addPageAt(newTab, comp, index);
    }

    /** Add new page at specific position.
    * @param newTab Name of new page.
    * @param comp Component which will be inserted into PageControl.
    * @param index Position of new page.
    * @exception ArrayIndexOutOfBoundsException If the value was invalid.
    */
    synchronized public void addPageAt(String newTab, Component comp, int index)
    throws ArrayIndexOutOfBoundsException {
        pages.insertElementAt(comp, index);
        add(comp, "Center"); // NOI18N
        tab.addTabAt(newTab, index);
        validate();
        countSizes();
        int j = getSelectedIndex();
        if (j == -1) {
            setSelectedIndex(index);
            j = index;
        }
        for (int i = 0; i < tab.getTabCount(); i++) {
            if (i != j)
                ((Component) pages.elementAt(i)).setVisible(false);
            else
                ((Component) pages.elementAt(i)).setVisible(true);
        }
        registerListeners(comp);
    }

    /** Gets index of page.
    * @param comp searching component
    * @return index or -1 if component is not inserted into PageControl
    */
    synchronized public int getIndexOfPage(Component comp) {
        for (int i = 0; i < tab.getTabCount(); i++) {
            if (comp.equals(pages.elementAt(i)))
                return i;
        }
        return -1;
    }

    /** Gets the component of specific page.
    * @param index Position of page.
    * @exception ArrayIndexOutOfBoundsException If the value was invalid.
    */
    public Component getPageAt(int index) throws ArrayIndexOutOfBoundsException {
        return (Component) pages.elementAt(index);
    }

    /** Removes the page at the specific position.
    * @param index Position of page.
    * @exception ArrayIndexOutOfBoundsException If the value was invalid.
    */
    synchronized public void removePageAt(int index) throws ArrayIndexOutOfBoundsException {
        boolean b = false;
        if (index == tab.getSelectedIndex())
            b = true;

        Component comp = (Component) pages.elementAt(index);
        remove(comp);
        unregisterListeners(comp);

        pages.removeElementAt(index);
        tab.removeTabAt(index);
        if (b) {
            int i = getSelectedIndex();
            if (i != -1)
                ((Component) (pages.elementAt(i))).setVisible(true);
        }
        repaint();
    }

    /** Remove all pages.
    * @see #removePageAt
    */
    synchronized public void removeAllPages() {
        tab.removeAllTabs();
        for (int i = 0; i < pages.size(); i++) {
            Component comp = (Component) pages.elementAt(i);
            remove(comp);
            unregisterListeners(comp);
        }
        pages.removeAllElements();
        repaint();
    }

    /** Sets a new direction of PageControl. If it is true,
    *  TabControl if on the top of PageControl otherwise
    *  at the bottom.
    * @param direct New direction.
    * @see #getDirection
    */
    public void setDirection(boolean direct) {
        if (tab.getDirection() != direct) {
            tab.setDirection (direct);
            remove(tab);
            if (tab.getDirection())
                add(tab,"North"); // NOI18N
            else
                add(tab,"South"); // NOI18N
            countSizes();
            repaint();
        }
    }

    /** Gets the current direction of PageControl. If it is true,
    *  TabControl if on the top of PageControl otherwise
    *  at the bottom.
    * @return Current direction.
    * @see #setDirection
    */
    public boolean getDirection() {
        return tab.getDirection();
    }

    private void countSizes() {
        Dimension d = tab.getPreferredSize();
        Dimension t = getSize();
        dimLast = t;
        Dimension ts = tab.getSize();
        if (ts != d)
            tab.setSize(t.width, d.height);
        Component comp;
        int x,y;
        if (tab.getDirection()) {
            if ((tab.getLocation().x != 0) || (tab.getLocation().y != 0))
                tab.setLocation(0,0);
            x = margin;
            y = d.height;
        }
        else {
            if ((tab.getLocation().x != 0) || (tab.getLocation().y != (t.height - d.height)))
                tab.setLocation(0, t.height - d.height);
            x = margin;
            y = margin;
        }
        int w = t.width - 2 * margin;
        int h = t.height - d.height - 2 * margin;
        for (int i = 0; i < getTabCount(); i++) {
            comp = (Component) pages.elementAt(i);
            comp.setBounds(x, y, w, h);
            comp.validate();
        }
    }

    public void paint(Graphics g) {
        Dimension s = getSize();
        if (s != dimLast)
            countSizes();
        if (tab.getTabCount() > 0) {
            int left = 0;
            int width = s.width - 1;
            int height = s.height - tab.getSize().height + margin - 1;
            int top = 0;
            if (tab.getDirection())
                top = tab.getSize().height - margin;

            g.setColor(SystemColor.controlHighlight);
            for(int i = 0; i < margin; i++) {
                g.draw3DRect(left, top, width, height, true);
                left++;
                top++;
                width = width - 2;
                height = height - 2;
            }
        }
        else {
            g.setColor(getBackground());
            g.fillRect(0, 0, s.width, s.height);
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        countSizes();
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);
        countSizes();
    }

    public void setSize(Dimension d) {
        super.setSize(d);
        countSizes();
    }

    public Dimension getMinimumSize() {
        return countSize(true);
    }

    public Dimension getPreferredSize() {
        return countSize(false);
    }

    private Dimension countSize(boolean min_pref) {
        Dimension d = (min_pref ? tab.getMinimumSize() : tab.getPreferredSize());
        Dimension e = new Dimension(0, 0);
        for (int i = 0; i < tab.getTabCount(); i++) {
            Component p = (Component) pages.elementAt(i);
            Dimension f = (min_pref ? p.getMinimumSize() : p.getPreferredSize());
            if (e.width < f.width)
                e.width = f.width;
            if (e.height < f.height)
                e.height = f.height;
        }
        return new Dimension(Math.max(d.width, e.width), d.height + e.height);
    }

    public void addIndexChangeListener(PropertyChangeListener l) {
        indexListeners.addElement(l);
    }

    public void removeIndexChangeListener(PropertyChangeListener l) {
        indexListeners.removeElement(l);
    }

    protected void fireIndexChange(int oldValue, int newValue) {
        Vector newListeners = (Vector)indexListeners.clone();
        Enumeration en = newListeners.elements();
        PropertyChangeEvent evt = new PropertyChangeEvent(this, "selectedIndex", // NOI18N
                                  new Integer(oldValue), new Integer(newValue));
        while (en.hasMoreElements())
            ((PropertyChangeListener) en.nextElement()).propertyChange(evt);
    }

    /** Register as focus listener at the added component */
    private void registerListeners(Component comp) {
        comp.addFocusListener(focusListener);
    }

    /** Unregister as focus listener from the removed component */
    private void unregisterListeners(Component comp) {
        comp.removeFocusListener(focusListener);
    }

    private void writeObject (ObjectOutputStream os) throws IOException{
        os.writeObject (pages);
        os.writeObject (tab);
        os.writeObject (dimLast);
    }

    private void readObject (ObjectInputStream is) throws IOException {
        try {
            pages = (Vector) is.readObject ();
            tab =  (TabControl) is.readObject ();
            dimLast = (Dimension) is.readObject ();
        } catch (ClassNotFoundException ex) {
            throw new IOException (ex.toString());
        }
        indexListeners = new Vector();
        setBackground(SystemColor.control);
        setForeground(SystemColor.controlText);
        tab.addIndexChangeListener(new PropertyChangeListener() {
                                       public void propertyChange(PropertyChangeEvent evt) {
                                           if (evt.getPropertyName().equals("selectedIndex")) {
                                               int oldSel = ((Integer) evt.getOldValue()).intValue();
                                               int newSel = ((Integer) evt.getNewValue()).intValue();
                                               for (int i = 0; i < tab.getTabCount(); i++) {
                                                   if (i == newSel)
                                                       ((Component) (pages.elementAt(i))).setVisible(true);
                                                   else
                                                       ((Component) (pages.elementAt(i))).setVisible(false);
                                               }
                                               fireIndexChange(oldSel, newSel);
                                           }
                                       }
                                   });
    }
}

/*
 * Log
 *  4    src-jtulach1.3         1/12/00  Ales Novak      i18n
 *  3    src-jtulach1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    src-jtulach1.1         4/16/99  Libor Martinek  
 *  1    src-jtulach1.0         3/9/99   Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    2.08        --/--/98 Jan Formanek    reflecting access rights changes in TabControl
 *  0    Tuborg    2.10        --/--/98 Petr Hamernik   focusListeners added
 *  0    Tuborg    2.12        --/--/98 Petr Hamernik   changes in index listeners
 *  0    Tuborg    2.13        --/--/98 Ales Novak      serializable
 *  0    Tuborg    2.20        --/--/98 Jan Formanek    made lightweight (tried, not succeeded)
 *  0    Tuborg    2.20        --/--/98 Jan Formanek    background color not set anymore
 */
