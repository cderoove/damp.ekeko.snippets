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
import javax.swing.JComponent;
import javax.swing.UIManager;

/** A class that produces a tab control component. It can be used
* separately or in a PageControl.
*
* <P>
* <TABLE BORDER COLS=3 WIDTH=100%>
* <TR><TH WIDTH=15%>Property<TH WIDTH=15%>Property Type<TH>Description
* <TR><TD> Direction      <TD> boolean <TD> Direction (upper or lower Tabcontrol)
* <TR><TD> SelectedIndex  <TD> int     <TD> Index of selected tab.
* </TABLE>
*
* TO DO:
*  1) Text is painted at wrong position.
*
* @version 2.21, May 22, 1998
* @author  Petr Hamernik, Ian Formanek
*/
public class TabControl extends JComponent implements Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 7374902493096688301L;

    /** The style of TabControl where the tabs are along the upper edge. */
    public static final boolean DIR_UP = true;

    /** The style of TabControl where the tabs are along the upper edge. */
    public static final boolean DIR_DOWN = false;

    /** The default style of TabControl. */
    private static final boolean DEFAULT_DIR = DIR_UP;

    /** Current style of TabControl. One of DIR_UP / DIR_DOWN. */
    protected boolean direction;

    /** Guarantee minimum width of each tab.
    * @see #setMinWidth
    * @see #getMinWidth
    */
    protected int minWidth;

    /** Default value of guarantee minimum width of each tab. */
    private static final int DEFAULT_MIN_WIDTH = 50;

    /** Currently selected tab.
    * @see #setSelectedIndex
    * @see #getSelectedIndex
    */
    private int selectedIndex;

    /** Vector of listeners which are invoked when the selction has changed.
     * @associates PropertyChangeListener*/
    transient private Vector indexListeners = new Vector();

    /** Vector of Strings with names of all tabs. 
     * @associates String*/
    protected Vector tabs;

    /** Indent of labels each tab. */
    protected static final int indent = 4;

    /** Width of lines among the tabs in pixels. */
    protected static final int margin = 1;

    /** Vector of sizes each tab. Depends on font size and the width
    *  of the label.
    * @associates Integer
    */
    protected Vector sizes;

    /** First visible tab in case that not all tabs are together
    *  visible. When the width of TabControl component is less than
    *  needed minimum width at the right side will appear small
    *  spin buttons for shifting visible tabs.
    * @see #lastVisible
    * @see #allVisible
    */
    protected int firstVisible;

    /** Last visible tab in case that not all tabs are together
    *  visible. When the width of TabControl component is less than
    *  needed minimum width at the right side will appear small
    *  spin buttons for shifting visible tabs.
    * @see #firstVisible
    * @see #allVisible
    */
    protected int lastVisible;

    /** Flag if all tabs are visible.
    * @see #firstVisible
    * @see #lastVisible
    */
    protected boolean allVisible;

    /** Size of small spin buttons for shifting of the visible
    *  tabs.
    * @see #allVisible
    */
    protected static final int posButtonSize = 10;

    /** Create new TabControl with all default values. */
    public TabControl() {
        tabs = new Vector();
        sizes = new Vector();
        minWidth = DEFAULT_MIN_WIDTH;
        direction = DIR_UP;
        Color defaultBackground = UIManager.getColor("TabbedPane.selected"); // NOI18N
        Color defaultForeground = UIManager.getColor("TabbedPane.tabForeground"); // NOI18N
        Font defaultFont = UIManager.getFont("TabbedPane.font"); // NOI18N

        /* "TabbedPane.font", dialogPlain12,
                "TabbedPane.tabBackground", table.get("controlShadow"),
                "TabbedPane.tabForeground", black,
                "TabbedPane.tabHighlight", table.get("controlHighlight"),
                "TabbedPane.tabShadow", table.get("controlShadow"),
                "TabbedPane.tabDarkShadow", table.get("controlDkShadow"),
                "TabbedPane.focus", getFocusColor(),
                "TabbedPane.selected", table.get("control"), */ // NOI18N

        setBackground(defaultBackground);
        setForeground(defaultForeground);
        setFont(defaultFont);
    }

    /** Remove all tabs and sets all variables into consistent state. */
    synchronized private void clearTabs() {
        tabs.removeAllElements();
        sizes.removeAllElements();
        firstVisible = 0;
        lastVisible = 0;
        allVisible = true;
        int old = selectedIndex;
        selectedIndex = -1;
        if (selectedIndex != old)
            fireIndexChange(old, selectedIndex);
    }

    /** Select new tab.
    * @param index Index of new selected tab.
    * @exception ArrayIndexOutOfBoundsException If the value was invalid.
    * @see #selectedIndex
    * @see #getSelectedIndex
    */
    synchronized public void setSelectedIndex(int index) throws ArrayIndexOutOfBoundsException {
        if ((index < 0) || (index >= tabs.size()))
            throw new ArrayIndexOutOfBoundsException();
        int old = selectedIndex;
        selectedIndex = index;
        fireIndexChange(old, selectedIndex);
        repaint();
    }

    /** Gets the index of currently selected tab.
    * @return Index of selected tab.
    * @see #selectedIndex
    * @see #setSelectedIndex
    */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /** Returns the number of tabs.
    * @return number of tabs.
    */
    public int getTabCount() {
        return tabs.size();
    }

    /** Converts point to index of a tab.
    * @param x x-coordinate on the component
    * @return index of the tab shown under the point
    */
    public int pointToIndex (int x) {
        int sum = 0;
        for (int i = firstVisible; i <= lastVisible; i++) {
            sum += ((Integer) sizes.elementAt(i)).intValue();
            if (x < sum) {
                return i;
            }
        }
        return lastVisible;
    }

    /** Gets the name of the tab with the specific index.
    * @param index Index of tab.
    * @exception ArrayIndexOutOfBoundsException If the value was invalid.
    * @return Name of tab.
    */
    public String getTabLabel(int index) throws ArrayIndexOutOfBoundsException {
        return (String) tabs.elementAt(index);
    }

    /** Add new tab.
    * @param newTab Name of new tab.
    */
    synchronized public void addTab(String newTab) {
        int index = tabs.size();
        addTabAt(newTab, index);
    }

    /** Add new tab at specific position.
    * @param newTab Name of new tab.
    * @param index Position of new tab.
    * @exception ArrayIndexOutOfBoundsException If the value was invalid.
    */
    synchronized public void addTabAt(String newTab, int index) throws
        ArrayIndexOutOfBoundsException {
        int w = stringWidth(getFontM(), newTab) + 2 * (indent + margin) + 3;
        if (w < minWidth)
            w = minWidth;
        tabs.insertElementAt(newTab, index);
        sizes.insertElementAt(new Integer(w), index);
        setSelectedIndex(0);
        checkWidth(true);
        repaint();
    }

    /** Removes the tab at the specific position.
     * @param index Position of tab.
     * @exception ArrayIndexOutOfBoundsException If the value was invalid.
     */
    synchronized public void removeTabAt(int index) throws
        ArrayIndexOutOfBoundsException {
        tabs.removeElementAt(index);
        sizes.removeElementAt(index);
        if (tabs.size() == 0) {
            clearTabs();
            return;
        }
        if (selectedIndex > index) {
            if (selectedIndex > 0)
                selectedIndex--;
        }
        else {
            if (selectedIndex == index) {
                if (selectedIndex == tabs.size())
                    selectedIndex--;
                fireIndexChange(index, selectedIndex);
            }
        }
        if (firstVisible > 0) firstVisible--;
        if (lastVisible > 0) lastVisible--;
        checkWidth(true);
        repaint();
    }

    /** Remove all tabs.
    * @see #removeTabAt
    */
    public void removeAllTabs() {
        clearTabs();
        repaint();
    }

    /** Sets the TabControl to be visible tab with the specific
    *  index.
    * @see #firstVisible
    * @see #lastVisible
    * @see #allVisible
    */
    synchronized public void makeVisible(int index) {
        if ((index < firstVisible) || (index > lastVisible)) {
            firstVisible = index;
            lastVisible = index;
            checkWidth(true);
            repaint();
        }
    }

    synchronized private void checkWidth(boolean dir) {
        Dimension d = getSize();
        int w = countTotalSize();
        if (w <= d.width) {
            allVisible = true;
            firstVisible = 0;
            lastVisible = tabs.size() - 1;
        }
        else {
            int wMax = d.width - (4 * margin + 2 * posButtonSize + 2 * indent);
            if (wMax < 0) {
                firstVisible = 0;
                lastVisible = 0;
                allVisible = false;
            }
            else {
                if (allVisible) {
                    allVisible = false;
                    if (countTotalSize(0, selectedIndex) < wMax) {
                        firstVisible = 0;
                        for (lastVisible = selectedIndex; lastVisible < sizes.size(); lastVisible++)
                            if (countTotalSize(0, lastVisible) > wMax) {
                                lastVisible--;
                                break;
                            }
                    }
                    else {
                        lastVisible = selectedIndex;
                        for (firstVisible = 0; firstVisible <= lastVisible; firstVisible++)
                            if (countTotalSize(firstVisible, lastVisible) < wMax)
                                break;
                    }
                }
                else {
                    if (countTotalSize(firstVisible, lastVisible) < wMax) {
                        for (firstVisible = 0; firstVisible < lastVisible; firstVisible++)
                            if (countTotalSize(firstVisible, lastVisible) < wMax) {
                                break;
                            }
                        for (; lastVisible < sizes.size() - 1; lastVisible++)
                            if (countTotalSize(firstVisible, lastVisible) > wMax) {
                                lastVisible--;
                                break;
                            }
                    }
                    else { // dir = true (reduce from left), dir = false (reduce from right)
                        if (dir) {
                            for (; firstVisible < lastVisible; firstVisible++)
                                if (countTotalSize(firstVisible, lastVisible) < wMax)
                                    break;
                        }
                        else {
                            for (; lastVisible > firstVisible; lastVisible--)
                                if (countTotalSize(firstVisible, lastVisible) < wMax)
                                    break;
                        }
                    }
                }
            }
        }
    }

    /** Sets a new direction of TabControl. If it is true,
    *  TabControl direction down otherwise up.
    * @param direct New direction.
    * @see #direction
    * @see #getDirection
    */
    public void setDirection(boolean direct) {
        if (direction != direct) {
            direction = direct;
            repaint();
        }
    }

    /** Gets the current direction of TabControl. If it is true,
    *  TabControl direction down otherwise up.
    * @return Current direction.
    * @see #direction
    * @see #setDirection
    */
    public boolean getDirection() {
        return direction;
    }

    /** Sets the minimum width of each tab.
    * @aMinWidth New value of minWidth variable.
    * @see #minWidth
    * @see #getMinWidth
    */
    public void setMinWidth(int aMinWidth) {
        minWidth = aMinWidth;
        countSizes();
        checkWidth(true);
        repaint();
    }

    /** Gets the minimum width of each tab.
    * @return Current value of minWidth variable.
    * @see #minWidth
    * @see #setMinWidth
    */
    public int getMinWidth() {
        return(minWidth);
    }

    public synchronized void setFont(Font f) {
        super.setFont(f);
        countSizes();
        checkWidth(true);
        repaint();
    }

    public void paint(Graphics g) {
        Dimension s = getSize();
        if (tabs.size() > 0) {
            int horizTrans = 2;
            int start = 0;
            FontMetrics f = getFontM();

            int fontYPos;
            if (s.height < f.getHeight())
                fontYPos = s.height;
            else
                fontYPos = (s.height + f.getHeight()) / 2 - 1;

            int focusStart = 0;
            int focusWidth = 0;
            if (allVisible) {
                firstVisible = 0;
                lastVisible = sizes.size() - 1;
            }

            for (int t = firstVisible; t <= lastVisible; t++) {
                int top = 0;
                int left = start;
                int height = s.height - 1;
                int width = ((Integer) sizes.elementAt(t)).intValue();
                int fontYPosDelta = 0;

                if (selectedIndex == t) {
                    focusStart = start;
                    focusWidth = width - 1;
                    if (!direction) {
                        fontYPosDelta -= horizTrans;
                        top -= horizTrans;
                    }
                    else {
                        height += margin;
                    }
                }
                else {
                    if (direction) {
                        top += horizTrans;
                        fontYPosDelta += horizTrans;
                    }
                }
                g.setColor(getForeground());
                String str = new String((String) tabs.elementAt(t));
                int delta = f.stringWidth(str);
                delta = ((width - delta - 2* (margin + indent)) / 2);
                g.drawString(str, start + margin + indent + delta, fontYPos + fontYPosDelta);

                start += width;

                width -=1;
                g.setColor(Color.black);
                g.drawLine(left + width, top, left + width, top + height);

                g.setColor(SystemColor.controlHighlight);
                width -=1;
                if ((selectedIndex == t) && (!direction)) {
                    top -= margin;
                    height += margin;
                }
                for(int i = 0; i < margin; i++) {
                    g.draw3DRect(left, top, width, height, true);
                    left++;
                    top++;
                    width = width - 2;
                    height = height - 2;
                }
            }
            if (!allVisible) {
                int top = s.height - indent - 2 * margin - posButtonSize;
                int height = posButtonSize + 2 * margin;
                int width = height;
                int left = s.width - indent - 4 * margin - 2 * posButtonSize;
                int[] xP = new int[3];
                int[] yP = new int[3];
                int os = left + 2 * margin + posButtonSize;
                int difr = posButtonSize / 10;
                for (int q = 0; q <= 1; q++) {
                    boolean b = true;
                    switch (q) {
                    case 0: xP[0] = os - margin - posButtonSize + difr;
                        xP[1] = os - margin - difr;
                        xP[2] = xP[1];
                        yP[0] = top + margin + posButtonSize / 2;
                        yP[1] = top + margin + difr;
                        yP[2] = top + margin + posButtonSize - difr;
                        if (firstVisible == 0)
                            b = false;
                        break;
                    case 1: for (int j = 0; j <= 2; j++)
                            xP[j] = 2 * os - xP[j];
                        if (lastVisible == tabs.size() - 1)
                            b = false;
                    }
                    if (b) {
                        g.setColor(SystemColor.controlHighlight);
                        for (int i = 0; i < margin; i++)
                            g.draw3DRect(left + i, top + i, width - 2*i, height - 2*i, true);
                        g.setColor(getForeground());
                        g.fillPolygon(xP, yP, 3);
                    }
                    left += 2 * margin + posButtonSize;
                }
            }
            if (direction) {
                g.setColor(SystemColor.controlHighlight);
                for (int i = 0; i < margin; i++) {
                    g.draw3DRect(0, s.height - i - 1, focusStart + margin, margin, true);
                    g.draw3DRect(focusStart + focusWidth - margin + i + 1, s.height - i - 1,
                                 s.width - focusStart - focusWidth + margin, margin, true);
                }
                g.setColor(getBackground());
                g.fillRect(focusStart + margin, s.height - margin - 1, 1, margin + 1);
            }
            else {
                g.setColor(SystemColor.controlHighlight);
                for (int i = 0; i < margin; i++) {
                    if (selectedIndex != firstVisible)
                        g.draw3DRect(margin - i - 1, -1, focusStart, 1 + i, true);
                    g.draw3DRect(focusStart + focusWidth - margin - 1, -1,
                                 s.width - focusStart - focusWidth + margin, 1 + i, true);
                }
                g.setColor(getBackground());
                g.fillRect(focusStart + focusWidth - margin - 1 , 0, 1, margin);
                g.setColor(Color.black);
                g.drawLine(focusStart, s.height - horizTrans, focusStart + focusWidth, s.height - horizTrans);
                g.drawLine(focusStart + focusWidth, margin, s.width, margin);
                if (selectedIndex != firstVisible)
                    g.drawLine(0, margin, focusStart - 1, margin);
            }
        }
        else {
            g.setColor(getBackground());
            g.fillRect(0, 0, s.width, s.height);
            g.setColor(getForeground());
            g.drawRect(0, 0, s.width - 1, s.height - 1);
        }
    }

    public Dimension getMinimumSize() {
        int w = countMaxSize();
        FontMetrics f = getFontM();

        if (sizes.size() > 1) {
            w += 2 * indent + 2 * posButtonSize + 4 * margin;
            w = Math.min(w, countTotalSize());
        }
        int h = 2 * margin + f.getHeight() * 4 / 3;
        return new Dimension(w, h);
    }

    public Dimension getPreferredSize() {
        int w = countTotalSize();
        if (w == 0)
            w = 2 * margin;
        Font font = getFont();
        FontMetrics f = getFontMetrics(
                            font==null ? new Font("Helvetica", Font.PLAIN, 12) : font); // NOI18N

        int h = 2 * margin + f.getHeight() * 4 / 3;
        return new Dimension(w, h);
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);
        int a = firstVisible;
        int b = lastVisible;
        checkWidth(false);
        if ((a != firstVisible) || (b != lastVisible))
            repaint();
    }

    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        int a = firstVisible;
        int b = lastVisible;
        checkWidth(false);
        if ((a != firstVisible) || (b != lastVisible))
            repaint();
    }

    private FontMetrics getFontM() {
        Font font = getFont();
        return getFontMetrics(
                   font==null ? new Font("Helvetica", Font.PLAIN, 12) : font); // NOI18N
    }

    private int stringWidth(FontMetrics f, String s) {
        return f.stringWidth(s);
    }

    private void countSizes() {
        int n = tabs.size();
        sizes.removeAllElements();
        if (n > 0) {
            FontMetrics f = getFontM();
            for (int i = 0; i < n; i++) {
                int w = stringWidth(getFontM(), (String) tabs.elementAt(i)) + 2 * (indent + 1) + 1;
                if (w < minWidth)
                    w = minWidth;
                sizes.addElement(new Integer(w));
            }
        }
    }

    private int countTotalSize(int from, int till) {
        int result = 0;
        for (int i = from; i <= till; i++)
            result += ((Integer) sizes.elementAt(i)).intValue();
        return result;
    }

    private int countTotalSize() {
        return countTotalSize(0, sizes.size() - 1);
    }

    private int countMaxSize() {
        int result = 0;
        for (int i = 0; i < sizes.size(); i++)
            result = Math.max(result, ((Integer) sizes.elementAt(i)).intValue());
        return result;
    }

    protected void processMouseEvent (MouseEvent e) {
        super.processMouseEvent (e);
        if (e.isConsumed () || e.getID () != MouseEvent.MOUSE_PRESSED) {
            return;
        }

        int x = e.getX();
        int y = e.getY();
        int sum = 0;
        int i;
        int old = selectedIndex;
        if (old == -1)
            return;
        for (i = firstVisible; i <= lastVisible; i++) {
            sum += ((Integer) sizes.elementAt(i)).intValue();
            if (x < sum)
                break;
        }
        if ((x < sum) && (selectedIndex != i))
            setSelectedIndex(i);
        else {
            if (!allVisible) {
                Dimension s = getSize();
                int tmp = s.height - indent;
                if ((y >= tmp - 2 * margin - posButtonSize) && (y < tmp)) {
                    tmp = s.width - indent - 4 * margin - 2 * posButtonSize;
                    if ((x >= tmp) && (x < tmp + 2 * margin + posButtonSize) &&
                            (firstVisible > 0)) {
                        firstVisible--;
                        checkWidth(false);
                        repaint();
                    }
                    if ((x >= tmp + 2 * margin + posButtonSize) &&
                            (x < tmp + 4 * margin + 2 * posButtonSize) && (lastVisible < tabs.size() - 1)) {
                        lastVisible++;
                        checkWidth(true);
                        repaint();
                    }
                }
            }
        }
    }

    public void addIndexChangeListener(PropertyChangeListener l) {
        if (indexListeners == null) indexListeners = new Vector ();
        indexListeners.addElement(l);
    }

    public void removeIndexChangeListener(PropertyChangeListener l) {
        if (indexListeners == null) indexListeners = new Vector ();
        indexListeners.removeElement(l);
    }

    protected void fireIndexChange(int oldValue, int newValue) {
        if (indexListeners == null) indexListeners = new Vector ();
        Vector newListeners = (Vector)indexListeners.clone();
        Enumeration en = newListeners.elements();
        PropertyChangeEvent evt = new PropertyChangeEvent(this, "selectedIndex", // NOI18N
                                  new Integer(oldValue), new Integer(newValue));
        while (en.hasMoreElements())
            ((PropertyChangeListener) en.nextElement()).propertyChange(evt);
    }
}

/*
 * Log
 *  6    src-jtulach1.5         1/12/00  Ales Novak      i18n
 *  5    src-jtulach1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    src-jtulach1.3         9/10/99  Ian Formanek    Removed deprecated code
 *  3    src-jtulach1.2         8/16/99  Ian Formanek    The TabControl height is
 *       smaller
 *  2    src-jtulach1.1         7/28/99  Jaroslav Tulach Popup menu for 
 *       workspaces.
 *  1    src-jtulach1.0         3/9/99   Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    2.04        --/--/98 Jan Formanek    added constants for direction (DIR_UP, DIR_DOWN), changed access rights
 *  0    Tuborg    2.04        --/--/98 Jan Formanek    of some class variables and initialization
 *  0    Tuborg    2.06        --/--/98 Petr Hamernik   small bug fixes.
 *  0    Tuborg    2.07        --/--/98 Petr Hamernik   small bug fixes.
 *  0    Tuborg    2.10        --/--/98 Petr Hamernik   changes in index listeners
 *  0    Tuborg    2.20        --/--/98 Jan Formanek    color is now taken from Swing's UIManager (same as TabbedPane)
 *  0    Tuborg    2.21        --/--/98 Jan Formanek    extends JComponewnt (==>> is lightweight, changes UI - colors)
 */
