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

package org.netbeans.editor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Hashtable;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;

/**
* Extended UI for the component. All the additional UI features
* like advanced scrolling, info about fonts, abbreviations,
* keyword matching are based on this class.
*
* @author Miloslav Metelka
* @version 1.00
*/
public class ExtUI implements ChangeListener, PropertyChangeListener, SettingsChangeListener {

    public static final String OVERWRITE_MODE_PROPERTY = "overwriteMode"; // NOI18N

    public static final String COMPONENT_PROPERTY = "component"; // NOI18N

    public static final String POPUP_MENU_PROPERTY = "popupMenu"; // NOI18N

    /** Default scrolling type is used for the standard
    * setDot() call. If the area is on the screen, it
    * jumps to it, otherwise it centers the requested area
    * vertically in the middle of the window and it uses
    * smallest covering on the right side.
    */
    public static final int SCROLL_DEFAULT = 0;

    /** Scrolling type used for regular caret moves.
    * The scrollJump is used when the caret requests area outside the screen.
    */
    public static final int SCROLL_MOVE = 1;

    /** Scrolling type where the smallest covering
    * for the requested rectangle is used. It's useful
    * for going to the end of the line for example.
    */
    public static final int SCROLL_SMALLEST = 2;

    /** Scrolling type for find operations, that can
    * request additional configurable area in each
    * direction, so the context around is visible too.
    */
    public static final int SCROLL_FIND = 3;


    private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);

    private static final Dimension NULL_DIMENSION = new Dimension(0, 0);

    private static final int STYLE_CNT = 4;

    /** Component this extended UI is related to. */
    private JTextComponent component;

    private JComponent extComponent;

    /** ID of the component in registry */
    int componentID = -1;

    /** Property change support for firing property changes */
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /** Document for the case ext ui is constructed without the component */
    private BaseDocument printDoc;

    /** Draw layer chain */
    DrawLayerList drawLayerList = new DrawLayerList();

    /** Map holding the [name, coloring] pairs */
    Map coloringMap;

    /** Character (or better line) height. Particular view can use a different
    * character height however most views will probably use this one.
    */
    int charHeight = 1; // prevent possible division by zero

    float lineHeightCorrection = 1.0f;

    /** Width of the space characters in all font style combinations. */
    int[] spaceWidths = new int[STYLE_CNT];

    /** Space width for default component font */
    int defaultSpaceWidth;

    /** Width of the digit for line numbering */
    int lineNumberDigitWidth;

    /** Ascent size of the font. This is useful for drawing the text */
    int[] ascents = new int[STYLE_CNT];

    /** Ascent of line numbers */
    int lineNumberAscent;

    /** Flag to initialize fonts */
    private boolean fontsInited;

    /** Have the font characters fixed width? It means whether the characters
    * of one of the font styles have the same width.
    */
    boolean fixedFont;

    /** Have the font characters of all font styles the same width? If this
    * flag is true then the fixedFont flag is also true.
    */
    boolean superFixedFont;

    /** Should the search words be colored? */
    boolean highlightSearch;

    /** Enable displaying line numbers. Both this flag and <tt>lineNumberVisibleSetting</tt>
    * must be true to have the line numbers visible in the window. This flag is false
    * by default. It's turned on automatically if the getExtComponent is called.
    */
    boolean lineNumberEnabled;

    /** This flag corresponds to the LINE_NUMBER_VISIBLE setting. */
    boolean lineNumberVisibleSetting;

    /** Whether to show line numbers or not. This flag is obtained using bitwise AND
    * operation on lineNumberEnabled flag and lineNumberVisibleSetting flag.
    */
    boolean lineNumberVisible;

    int baseLeftMargin;

    /** Line number total width with indentation */
    int lineNumberWidth;

    /** Current maximum count of digits in line number */
    int lineNumberMaxDigitCnt;

    /** Margin on the left side of the line number */
    Insets lineNumberMargin;

    /** This is the size of the editor as component while the real size
    * of the lines edited can be lower. The reason why to use this
    * virtual size is that each resizing of the component means
    * revalidating and therefore repainting of the whole component.
    */
    Rectangle virtualSize = new Rectangle();

    /** This is the increment by which the size of the component
    * is increased.
    */
    //  Rectangle virtualSizeIncrement = new Rectangle(); !!!

    Insets textMargin = NULL_INSETS;

    /** How much columns/lines to add when the scroll is performed
    * so that the component is not scrolled so often.
    * Negative number means portion of the extent width/height
    */
    Insets scrollJumpInsets;

    /** How much columns/lines to add when the scroll is performed
    * so that the component is not scrolled so often.
    * Negative number means portion of the extent width/height
    */
    Insets scrollFindInsets;

    /** Flag saying whether either the width or height in virtualSize
    * were updated.
    */
    boolean virtualSizeUpdated;

    /** Listener to changes in settings */
    private PropertyChangeListener settingsListener;

    /** ExtUI properties 
     * @associates Object*/
    Hashtable props = new Hashtable(11);

    boolean textLimitLineVisible;

    Color textLimitLineColor;

    int textLimitWidth;

    private Rectangle lastExtentBounds = new Rectangle();

    private Dimension componentSizeIncrement = new Dimension();

    private Abbrev abbrev;

    private WordMatch wordMatch;

    private ToolTipSupport toolTipSupport;

    /** Status bar */
    StatusBar statusBar;

    private FocusAdapter focusL;

    Map renderingHints;

    /** Construct extended UI for the use with a text component */
    public ExtUI() {
        Settings.addSettingsChangeListener(this);

        focusL = new FocusAdapter() {
                     public void focusGained(FocusEvent evt) {
                         Registry.activate(getComponent());
                     }
                 };

    }

    /** Construct extended UI for printing the given document */
    public ExtUI(BaseDocument printDoc) {
        this.printDoc = printDoc;

        settingsChange(null);

        fixedFont = true;
        superFixedFont = true;
        for (int i = 0; i < spaceWidths.length; i++) {
            spaceWidths[i] = 1;
            ascents[i] = 0;
        }

        updateLineNumberWidth();

        addLayer(new DrawLayerFactory.SyntaxLayer());
    }

    /** Called when the <tt>BaseTextUI</tt> is being installed
    * into the component.
    */
    protected void installUI(JTextComponent c) {
        this.component = c;

        // listen on component
        component.addPropertyChangeListener(this);
        component.addFocusListener(focusL);

        // listen on caret
        Caret caret = component.getCaret();
        if (caret != null) {
            caret.addChangeListener(this);
        }

        BaseDocument doc = getDocument();
        if (doc != null) {
            modelChanged(null, doc);
        }


        getToolTipSupport(); // cause tooltip support initialization

        settingsChange(null);

        putProperty(COMPONENT_PROPERTY, c);
    }

    /** Called when the <tt>BaseTextUI</tt> is being uninstalled
    * from the component.
    */
    protected void uninstallUI(JTextComponent c) {
        // stop listening on caret
        Caret caret = component.getCaret();
        if (caret != null) {
            caret.removeChangeListener(this);
        }

        // stop listening on component
        component.removePropertyChangeListener(this);
        component.removeFocusListener(focusL);

        BaseDocument doc = getDocument();
        if (doc != null) {
            modelChanged(doc, null);
        }

        component = null;
        putProperty(COMPONENT_PROPERTY, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, l);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void settingsChange(SettingsChangeEvent evt) {
        if (component != null) {
            if (Utilities.getKit(component) == null) {
                return; // prevent problems if not garbage collected and settings changed
            }
        }

        Class kitClass = getKitClass();
        String settingName = (evt != null) ? evt.getSettingName() : null;

        if (settingName == null || Settings.LINE_NUMBER_VISIBLE.equals(settingName)
                || Settings.PRINT_LINE_NUMBER_VISIBLE.equals(settingName)
           ) {
            lineNumberVisibleSetting = SettingsUtil.getBoolean(kitClass, (component != null)
                                       ? Settings.LINE_NUMBER_VISIBLE : Settings.PRINT_LINE_NUMBER_VISIBLE,
                                       false);
            lineNumberVisible = lineNumberEnabled && lineNumberVisibleSetting;
        }

        BaseDocument doc = getDocument();
        if (doc != null) {

            if (settingName == null
                    || settingName.endsWith(Settings.COLORING_NAME_SUFFIX)
                    || settingName.endsWith(Settings.COLORING_NAME_PRINT_SUFFIX)
               ) {
                coloringMap = null; // reset coloring map so it's lazily rebuilt
            }

            if (settingName == null || Settings.LINE_NUMBER_MARGIN.equals(settingName)) {
                Insets m = (Insets)Settings.getValue(kitClass, Settings.LINE_NUMBER_MARGIN);
                lineNumberMargin = (m != null) ? m : NULL_INSETS;
            }

            if (settingName == null || Settings.LINE_HEIGHT_CORRECTION.equals(settingName)) {
                Float f = (Float)Settings.getValue(kitClass, Settings.LINE_HEIGHT_CORRECTION);
                lineHeightCorrection = (f != null) ? f.floatValue() : 1.0f;
            }

            if (settingName == null || Settings.TEXT_LIMIT_LINE_VISIBLE.equals(settingName)) {
                textLimitLineVisible = SettingsUtil.getBoolean(kitClass,
                                       Settings.TEXT_LIMIT_LINE_VISIBLE, false);
            }

            if (settingName == null || Settings.TEXT_LIMIT_LINE_COLOR.equals(settingName)) {
                textLimitLineColor = (Color)Settings.getValue(kitClass,
                                     Settings.TEXT_LIMIT_LINE_COLOR);
            }

            if (settingName == null || Settings.TEXT_LIMIT_WIDTH.equals(settingName)) {
                textLimitWidth = SettingsUtil.getInteger(kitClass,
                                 Settings.TEXT_LIMIT_WIDTH,
                                 DefaultSettings.defaultTextLimitWidth);
            }

            // component only properties
            if (component != null) {
                if (settingName == null || Settings.SCROLL_JUMP_INSETS.equals(settingName)) {
                    scrollJumpInsets = (Insets)Settings.getValue(kitClass,
                                       Settings.SCROLL_JUMP_INSETS);
                    if (scrollJumpInsets == null) {
                        scrollJumpInsets = NULL_INSETS;
                    }
                }

                if (settingName == null || Settings.SCROLL_FIND_INSETS.equals(settingName)) {
                    scrollFindInsets = (Insets)Settings.getValue(kitClass,
                                       Settings.SCROLL_FIND_INSETS);
                    if (scrollFindInsets == null) {
                        scrollFindInsets = NULL_INSETS;
                    }
                }

                if (settingName == null || Settings.COMPONENT_SIZE_INCREMENT.equals(settingName)) {
                    componentSizeIncrement = (Dimension)Settings.getValue(kitClass,
                                             Settings.COMPONENT_SIZE_INCREMENT);
                    if (componentSizeIncrement == null) {
                        componentSizeIncrement = NULL_DIMENSION;
                    }
                }

                if (settingName == null || Settings.RENDERING_HINTS.equals(settingName)) {
                    renderingHints = (Map)Settings.getValue(kitClass, Settings.RENDERING_HINTS);
                }

                if (settingName == null || Settings.CARET_COLOR_INSERT_MODE.equals(settingName)
                        || Settings.CARET_COLOR_OVERWRITE_MODE.equals(settingName)
                   ) {
                    Boolean b = (Boolean)getProperty(OVERWRITE_MODE_PROPERTY);
                    Color caretColor;
                    if (b == null || !b.booleanValue()) {
                        caretColor = (Color)Settings.getValue(kitClass,
                                                              Settings.CARET_COLOR_INSERT_MODE);
                    } else {
                        caretColor = (Color)Settings.getValue(kitClass,
                                                              Settings.CARET_COLOR_OVERWRITE_MODE);
                    }

                    if (caretColor != null) {
                        component.setCaretColor(caretColor);
                    }
                }

                component.setKeymap(Utilities.getKit(component).getKeymap());

                fontsInited = false;
                BaseTextUI ui = (BaseTextUI)component.getUI();
                ui.updateHeight();

                component.repaint();
            }
        }
    }

    public void stateChanged(final ChangeEvent e) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    BaseKit kit = Utilities.getKit(component);
                    if (kit != null) {
                        boolean selectionVisible = ((Caret)e.getSource()).isSelectionVisible();
                        Action a = kit.getActionByName(BaseKit.cutAction);
                        if (a != null) {
                            a.setEnabled(selectionVisible);
                        }

                        a = kit.getActionByName(BaseKit.copyAction);
                        if (a != null) {
                            a.setEnabled(selectionVisible);
                        }

                        a = kit.getActionByName(BaseKit.removeSelectionAction);
                        if (a != null) {
                            a.setEnabled(selectionVisible);
                        }
                    }
                }
            }
        );
    }

    protected synchronized void modelChanged(BaseDocument oldDoc, BaseDocument newDoc) {
        if (oldDoc != null) {
            // remove all document layers
            drawLayerList.remove(oldDoc.getDrawLayerList());
        }

        if (newDoc != null) {
            settingsChange(null);

            // add all document layers
            drawLayerList.add(newDoc.getDrawLayerList());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if ("document".equals(propName)) {
            BaseDocument oldDoc = (evt.getOldValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getOldValue() : null;
            BaseDocument newDoc = (evt.getNewValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getNewValue() : null;
            modelChanged(oldDoc, newDoc);

        } else if ("margin".equals(propName)) { // NOI18N
            updateTextMargin();

        } else if ("caret".equals(propName)) { // NOI18N
            if (evt.getOldValue() instanceof Caret) {
                ((Caret)evt.getOldValue()).removeChangeListener(this);
            }
            if (evt.getNewValue() instanceof Caret) {
                ((Caret)evt.getNewValue()).addChangeListener(this);
            }

        } else if ("enabled".equals(propName)) { // NOI18N
            if (!component.isEnabled()) {
                component.getCaret().setVisible(false);
            }
        }
    }

    protected Map createColoringMap() {
        Map coloringMap = SettingsUtil.getColoringMap(getKitClass(), (component == null));
        // Test if there's a default coloring
        if (coloringMap.get(Settings.DEFAULT_COLORING) == null) {
            coloringMap.put(Settings.DEFAULT_COLORING, DefaultSettings.defaultColoring);
        }
        return coloringMap;
    }

    public Map getColoringMap() {
        if (coloringMap == null) {
            coloringMap = createColoringMap();
        }
        return coloringMap;
    }

    public Coloring getDefaultColoring() {
        return (Coloring)getColoringMap().get(Settings.DEFAULT_COLORING);
    }

    public Coloring getColoring(String coloringName) {
        return (Coloring)getColoringMap().get(coloringName);
    }

    private Font getUniformFont() {
        Map cm = getColoringMap();
        Iterator i = cm.entrySet().iterator();
        List exclusionList = (List)Settings.getValue(getKitClass(), Settings.UNIFORM_FONT_EXCLUSION_LIST);
        Font uniFont = null;
        String uniFontName = null;
        int uniFontSize = 0;
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            String coloringName = (String)me.getKey();
            Coloring c = (Coloring)me.getValue();
            if (c != null && (exclusionList == null || exclusionList.indexOf(coloringName) < 0)) {
                Font font = c.getFont();
                if (font != null) {
                    if (uniFont != null) {
                        if (!font.getName().equals(uniFontName) || font.getSize() != uniFontSize) {
                            return null;
                        }
                    } else {
                        uniFont = font;
                        uniFontName = uniFont.getName();
                        uniFontSize = uniFont.getSize();
                    }
                }
            }
        }
        return uniFont;
    }

    private int getMaximumFontHeight(Graphics g) {
        Map cm = getColoringMap();
        Iterator i = cm.entrySet().iterator();
        List exclusionList = (List)Settings.getValue(getKitClass(), Settings.UNIFORM_FONT_EXCLUSION_LIST);
        int maxHeight = 0;
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            String coloringName = (String)me.getKey();
            Coloring c = (Coloring)me.getValue();
            if (c != null && (exclusionList == null || exclusionList.indexOf(coloringName) < 0)) {
                Font font = c.getFont();
                if (font != null) {
                    maxHeight = Math.max(maxHeight, FontMetricsCache.getFontMetrics(font, g).getHeight());
                }
            }
        }
        return maxHeight;
    }

    protected void initFonts(Graphics g) {
        Class kitClass = Utilities.getKitClass(component);

        Insets m = (Insets)Settings.getValue(kitClass, Settings.MARGIN);
        if (kitClass != null && m != null) {
            component.setMargin(m);
        }

        // Apply the default coloring to the component
        getDefaultColoring().apply(component);

        if (renderingHints != null) {
            ((Graphics2D)g).setRenderingHints(renderingHints);
        }

        char[] chars = new char[] { 'i', 'W', 'm', ' ', 'm', 'm',
                                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
                                  };
        Coloring dc = getDefaultColoring();

        // Handle line number fonts and widths
        Coloring lnc = (Coloring)getColoringMap().get(Settings.LINE_NUMBER_COLORING);
        if (lnc != null) {
            Font lnFont = lnc.getFont();
            if (lnFont == null) {
                lnFont = dc.getFont();
            }
            FontMetrics lnFM = g.getFontMetrics(lnFont);
            int fs = lnFont.getStyle();
            int maxWidth = 1;
            for (int i = 5; i < 15; i++) {
                maxWidth = Math.max(maxWidth, lnFM.charsWidth(chars, i, 1));
            }
            lineNumberDigitWidth = maxWidth;
            lineNumberAscent = (int)(lnFM.getAscent() * lineHeightCorrection);
        }

        // Check whether the uniform font is used
        Font uniFont = getUniformFont();
        if (uniFont != null) {
            int[] testStarts = new int[] { 0, 1, 2, 2, 4,
                                           6, 7, 8, 9, 10, 11, 12, 13, 14, 15
                                         };
            int[] testLens = new int[] { 1, 1, 1, 3, 2,
                                         1, 1, 1, 1, 1, 1, 1, 1, 1, 1
                                       };
            int[][] results = new int[testStarts.length][];
            Font[] fonts = new Font[STYLE_CNT];
            FontMetrics[] fms = new FontMetrics[STYLE_CNT];
            charHeight = 1; // important for settings changes
            for (int i = 0; i < STYLE_CNT; i++) {
                fonts[i] = new Font(uniFont.getName(), i, uniFont.getSize());
                fms[i] = g.getFontMetrics(fonts[i]);
                charHeight = Math.max(charHeight, (int)(fms[i].getHeight() * lineHeightCorrection));
                ascents[i] = (int)(fms[i].getAscent() * lineHeightCorrection);
            }
            for (int i = 0; i < testStarts.length; i++) {
                results[i] = new int[STYLE_CNT];
                for (int j = 0; j < STYLE_CNT; j++) {
                    //          results[i][j] = (int)fonts[j].getStringBounds(chars,
                    //              testStarts[i], testLens[i], frc).getWidth();
                    results[i][j] = fms[j].charsWidth(chars,
                                                      testStarts[i], testLens[i]);
                }
            }
            // Test whether fonts are superFixed or fixed
            boolean wantSuperFixed = true;
            boolean wantFixed = true;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < STYLE_CNT; j++) {
                    if (results[i][j] != results[0][j]) {
                        wantFixed = false;
                    }
                    if (results[i][j] != results[i][0]) {
                        wantSuperFixed = false;
                    }
                }
            }
            if (!wantFixed) {
                wantSuperFixed = false;
            }
            fixedFont = wantFixed;
            superFixedFont = wantSuperFixed;
            //      System.out.println("ExtUI.java:590 fixedFont=" + fixedFont + ", superFixedFont=" + superFixedFont);
            fixedFont = false; // !!!
            superFixedFont = false; // !!!

            // Assign spaceWidths[]
            if (fixedFont) {
                for (int i = 0; i < STYLE_CNT; i++) {
                    spaceWidths[i] = results[0][i];
                }
            } else {
                for (int i = 0; i < STYLE_CNT; i++) {
                    spaceWidths[i] = results[3][i] - results[4][i];
                }
            }

            for (int i = 0; i < STYLE_CNT; i++) {
                //        System.out.println("ExtUI.java:366 numWidths[" + i + "]=" + numWidths[i]); // NOI18N
            }

        } else { // unifont is null

            // Get character height
            charHeight = getMaximumFontHeight(g);
            // compute space widths for the styles
            for (int i = 0; i < STYLE_CNT; i++) {
                spaceWidths[i] = charHeight / 3;
            }

        }

        //    defaultSpaceWidth = spaceWidths[0]; // !!! could be computed somehow?
        charHeight = (int)(getMaximumFontHeight(g) * lineHeightCorrection);
        defaultSpaceWidth = FontMetricsCache.getFontMetrics(dc.getFont(), g).stringWidth(" ");
        for (int i = 0; i < STYLE_CNT; i++) {
            spaceWidths[i] = defaultSpaceWidth;
        }

        // Update various sizes
        fontsInited = true;
        if (component != null) {
            ((BaseTextUI)component.getUI()).updateHeight();
            updateLineNumberWidth();
            checkLineLimit();
        }

        // Possibly display debugging info
        if (System.getProperty("netbeans.debug.editor.font") != null) {
            System.out.println("fixedFont=" + fixedFont + ", superFixedFont=" + superFixedFont);
        }

        /* JDK1.3 patch for the behavior that occurs when the line is wider
        * than the screen and the user first clicks End key to go to the end
        * and then goes back by (Ctrl+)Left. As the non-simple scrolling mode 
        * is used in JViewport in 1.3 the line number block appears shifted
        * to the right and gets repainted after 300ms which looks ugly.
        * The patch is to set the simple scrolling mode into JViewport.
        *
        * getParentViewport().setScrollMode(0); // 2 stands for SIMPLE_SCROLL_MODE
        *
        */
        try {
            JViewport vp = getParentViewport();
            if (vp != null) {
                java.lang.reflect.Method setScrollModeMethod = JViewport.class.getDeclaredMethod(
                            "setScrollMode", new Class[] { Integer.TYPE }); // NOI18N
                setScrollModeMethod.invoke(vp, new Object[] { new Integer(0) });
            }
        } catch (Throwable t) {
        }

    }

    public final JTextComponent getComponent() {
        return component;
    }

    /** Get the document to work on. Either component's document or printed document
    * is returned. It can return null in case the component's document is not instance
    * of BaseDocument.
    */
    public final BaseDocument getDocument() {
        return (component != null) ? Utilities.getDocument(component) : printDoc;
    }

    private Class getKitClass() {
        return (component != null) ? Utilities.getKitClass(component)
               : ((printDoc != null) ? printDoc.getKitClass() : null);
    }

    public Object getProperty(Object key) {
        return props.get(key);
    }

    public void putProperty(Object key, Object value) {
        Object oldValue;
        if (value != null) {
            oldValue = props.put(key, value);
        } else {
            oldValue = props.remove(key);
        }
        firePropertyChange(key.toString(), oldValue, value);
    }

    /** Create or get extended editor component */
    public JComponent getExtComponent() {
        if (extComponent == null) {
            if (component != null) {
                setLineNumberEnabled(true); // enable line numbering

                // Extended component is a panel
                extComponent = new JPanel(new BorderLayout());

                // Configure the scroll-pane with the component
                JScrollPane scroller = new JScrollPane(component);
                scroller.getViewport().setMinimumSize(new Dimension(4,4));
                extComponent.add(scroller);

                // Install the status-bar panel into extComponent in extUI
                extComponent.add(getStatusBar().getPanel(), BorderLayout.SOUTH);
            }
        }
        return extComponent;
    }

    public Abbrev getAbbrev() {
        if (abbrev == null) {
            abbrev = new Abbrev(this, true, true);
        }
        return abbrev;
    }

    public WordMatch getWordMatch() {
        if (wordMatch == null) {
            wordMatch = new WordMatch(this);
        }
        return wordMatch;
    }

    public ToolTipSupport getToolTipSupport() {
        if (toolTipSupport == null) {
            toolTipSupport = new ToolTipSupport(this);
        }
        return toolTipSupport;
    }

    public StatusBar getStatusBar() {
        if (statusBar == null) {
            if (extComponent != null) {
                statusBar = new StatusBar(this);
            }
        }
        return statusBar;
    }

    final DrawLayerList getDrawLayerList() {
        return drawLayerList;
    }

    /** Find the layer with some layer name in the layer hierarchy */
    public synchronized DrawLayer findLayer(String layerName) {
        return drawLayerList.findLayer(layerName);
    }

    /** Add new layer and use its priority to position it in the chain.
    * If there's the layer with same visibility then the inserted layer
    * will be placed after it.
    *
    * @param layer layer to insert into the chain
    */
    public synchronized boolean addLayer(DrawLayer layer) {
        return drawLayerList.add(layer);
    }

    public synchronized DrawLayer removeLayer(String layerName) {
        return drawLayerList.remove(layerName);
    }

    public void showPopupMenu(int x, int y) {
        BaseKit kit = Utilities.getKit(component);
        if (kit != null) {
            Action a = kit.getActionByName(
                           BaseKit.buildPopupMenuAction);
            if (a != null) {
                a.actionPerformed(new ActionEvent(component, 0, "")); // NOI18N
            }
            JPopupMenu pm = (JPopupMenu)getProperty(POPUP_MENU_PROPERTY);
            if (pm != null) {
                pm.show(component, x, y);
            }
        }
    }

    public void hidePopupMenu() {
        JPopupMenu pm = (JPopupMenu)getProperty(POPUP_MENU_PROPERTY);
        if (pm != null) {
            pm.setVisible(false);
        }
    }


    public void repaint(int startY) {
        repaint(startY, component.getHeight());
    }

    public void repaint(int startY, int height) {
        if (height <= 0) {
            return;
        }
        int width = Math.max(component.getWidth(), 0);
        startY = Math.max(startY, 0);
        component.repaint(0, startY, width, height);
    }

    public void repaintPos(int pos) throws BadLocationException {
        repaintBlock(pos, pos);
    }

    public void repaintBlock(int startPos, int endPos)
    throws BadLocationException {
        BaseTextUI ui = (BaseTextUI)component.getUI();
        if (startPos > endPos) { // swap
            int tmpPos = startPos;
            startPos = endPos;
            endPos = tmpPos;
        }
        try {
            int yFrom = ui.getYFromPos(startPos);
            int yTo = ui.getYFromPos(endPos);
            repaint(yFrom, (yTo - yFrom) + charHeight);
        } catch (BadLocationException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }
    }

    /** Is the parent of some editor component a viewport */
    private JViewport getParentViewport() {
        Component pc = component.getParent();
        return (pc instanceof JViewport) ? (JViewport)pc : null;
    }

    /** Finds the frame - parent of editor component */
    public static Frame getParentFrame(Component c) {
        do {
            c = c.getParent();
            if (c instanceof Frame) {
                return (Frame)c;
            }
        } while (c != null);
        return null;
    }

    /** Possibly update virtual width. If the width
    * is really updated, the method returns true.
    */
    public boolean updateVirtualWidth(int width) {
        boolean updated = false;
        if (width > virtualSize.width) {
            int widthInc = componentSizeIncrement.width;
            widthInc = (widthInc < 0) ? (lastExtentBounds.width * (-widthInc) / 100)
                       : widthInc * defaultSpaceWidth;

            virtualSize.width = width + widthInc;
            virtualSizeUpdated = true;
            updated = true;
        }

        return updated;
    }

    /** Possibly update virtual height. If the height
    * is really updated, the method returns true. There is
    * a slight difference against virtual width in that
    * if the height is shrinked too much the virtual height
    * is shrinked too.
    */
    public boolean updateVirtualHeight(int height) {
        boolean updated = false;
        updateLineNumberWidth();

        if (height > virtualSize.height) {
            int heightInc = componentSizeIncrement.height;
            heightInc = (heightInc < 0) ? (lastExtentBounds.height * (-heightInc) / 100)
                        : heightInc * charHeight;

            virtualSize.height = height + heightInc;
            virtualSizeUpdated = true;
            updated = true;
        }

        if (height < virtualSize.height - lastExtentBounds.height) {
            virtualSize.height = height;
            virtualSizeUpdated = true;
            updated = true;
        }

        return updated;
    }

    public boolean isLineNumberEnabled() {
        return lineNumberEnabled;
    }

    public void setLineNumberEnabled(boolean lineNumberEnabled) {
        this.lineNumberEnabled = lineNumberEnabled;
        lineNumberVisible = lineNumberEnabled && lineNumberVisibleSetting;
    }

    public void updateLineNumberWidth() {
        int oldWidth = lineNumberWidth;

        if (lineNumberVisible) {
            try {
                BaseDocument doc = getDocument();
                int lineCnt = Utilities.getLineOffset(doc, doc.getLength()) + 1;
                int digitCnt = Integer.toString(lineCnt).length();
                if (digitCnt > lineNumberMaxDigitCnt) {
                    lineNumberMaxDigitCnt = digitCnt;
                }
            } catch (BadLocationException e) {
                lineNumberMaxDigitCnt = 1;
            }
            lineNumberWidth = lineNumberMaxDigitCnt * lineNumberDigitWidth;
            if (lineNumberMargin != null) {
                lineNumberWidth += lineNumberMargin.left + lineNumberMargin.right;
            }
        } else {
            lineNumberWidth = 0;
        }

        updateTextMargin();
        if (oldWidth != lineNumberWidth) { // changed
            if (component != null) {
                component.repaint();
            }
        }
    }

    void checkLineLimit() {
        BaseDocument doc = getDocument();
        if (doc != null) {
            Integer lineLimit = (Integer)doc.getProperty(BaseDocument.LINE_LIMIT_PROP);
            if (lineLimit != null) {
                updateVirtualWidth(spaceWidths[0] * lineLimit.intValue()
                                   + lineNumberWidth);
            }
        }
    }

    public void updateTextMargin() {
        Insets orig = textMargin;
        Insets cm = (component != null) ? component.getMargin() : null;
        if (cm != null) {
            textMargin = new Insets(cm.top, cm.left + lineNumberWidth,
                                    cm.bottom, cm.right);
        } else {
            textMargin = new Insets(0, lineNumberWidth, 0, 0);
        }
        if (orig.top != textMargin.top || orig.bottom != textMargin.bottom) {
            ((BaseTextUI)component.getUI()).invalidateStartY();
        }
    }

    public Rectangle getExtentBounds() {
        return getExtentBounds(null);
    }

    /** Get position of the component extent. The (x, y) are set to (0, 0) if there's
    * no viewport or (-x, -y) if there's one.
    */
    public Rectangle getExtentBounds(Rectangle r) {
        if (r == null) {
            r = new Rectangle();
        }
        if (component != null) {
            JViewport port = getParentViewport();
            if (port != null) {
                Point p = port.getViewPosition();
                r.width = port.getWidth();
                r.height = port.getHeight();
                r.x = p.x;
                r.y = p.y;
            } else { // no viewport
                r.width = component.getWidth();
                r.height = component.getHeight();
                r.x = 0;
                r.y = 0;
            }
        }
        return r;
    }

    /** Get the begining of the area covered by text */
    public final Insets getTextMargin() {
        return new Insets(textMargin.top, textMargin.left,
                          textMargin.bottom, textMargin.right);
    }

    public void scrollRectToVisible(final Rectangle r, final int scrollPolicy) {
        Utilities.runInEventDispatchThread(
            new Runnable() {
                public void run() {
                    scrollRectToVisibleFragile(r, scrollPolicy);
                }
            }
        );
    }

    /** Must be called with EventDispatchThread */
    boolean scrollRectToVisibleFragile(Rectangle r, int scrollPolicy) {
        Insets margin = getTextMargin();
        Rectangle bounds = getExtentBounds();
        r = new Rectangle(r); // make copy of orig rect
        r.x -= margin.left;
        r.y -= margin.top;
        bounds.width -= margin.left + margin.right;
        bounds.height -= margin.top + margin.bottom;
        return scrollRectToVisibleImpl(r, scrollPolicy, bounds);
    }

    /** Scroll the view so that requested rectangle is best visible.
    * There are different scroll policies available.
    */
    private boolean scrollRectToVisibleImpl(Rectangle r, int scrollPolicy,
                                            Rectangle bounds) {
        if (bounds.width <= 0 || bounds.height <= 0) {
            return false;
        }

        // handle find scrolling specifically
        if (scrollPolicy == SCROLL_FIND) {
            int nx = Math.max(r.x - scrollFindInsets.left, 0);
            r.width += (r.x - nx) + scrollFindInsets.right;
            r.x = nx;
            int ny = Math.max(r.y - scrollFindInsets.top, 0);
            r.height += (r.y - ny) + scrollFindInsets.bottom;
            r.y = ny;
            return scrollRectToVisibleImpl(r, SCROLL_SMALLEST, bounds); // recall
        }
        // r must be within virtualSize's width
        if (r.x + r.width > virtualSize.width) {
            r.x = virtualSize.width - r.width;
            if (r.x < 0) {
                r.x = 0;
                r.width = virtualSize.width;
            }
            return scrollRectToVisibleImpl(r, scrollPolicy, bounds); // recall
        }
        // r must be within virtualSize's height
        if (r.y + r.height > virtualSize.height) {
            r.y = virtualSize.height - r.height;
            if (r.y < 0) {
                r.y = 0;
                r.height = virtualSize.height;
            }
            return scrollRectToVisibleImpl(r, scrollPolicy, bounds);
        }

        // if r extends bounds dimension it must be corrected now
        if (r.width > bounds.width || r.height > bounds.height) {
            Rectangle caretRect = new Rectangle((Rectangle)component.getCaret());
            if (caretRect.x >= r.x
                    && caretRect.x + caretRect.width <= r.x + r.width
                    && caretRect.y >= r.y
                    && caretRect.y + caretRect.height <= r.y + r.height
               ) { // caret inside requested rect
                // move scroll rect for best caret visibility
                int overX = r.width - bounds.width;
                int overY = r.height - bounds.height;
                if (overX > 0) {
                    r.x -= overX * (caretRect.x - r.x) / r.width;
                }
                if (overY > 0) {
                    r.y -= overY * (caretRect.y - r.y) / r.height;
                }
            }
            r.height = bounds.height;
            r.width = bounds.width; // could be different algorithm
            return scrollRectToVisibleImpl(r, scrollPolicy, bounds);
        }

        int newX = bounds.x;
        int newY = bounds.y;
        boolean move = false;
        // now the scroll rect is within bounds of the component
        // and can have size of the extent at maximum
        if (r.x < bounds.x) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_MOVE:
                newX = (scrollJumpInsets.left < 0)
                       ? (bounds.width * (-scrollJumpInsets.left) / 100)
                       : scrollJumpInsets.left * defaultSpaceWidth;
                newX = Math.min(newX, bounds.x + bounds.width - (r.x + r.width));
                newX = Math.max(r.x - newX, 0); // new bounds.x
                break;
            case SCROLL_DEFAULT:
            case SCROLL_SMALLEST:
            default:
                newX = r.x;
                break;
            }
            updateVirtualWidth(newX + bounds.width);
        } else if (r.x + r.width > bounds.x + bounds.width) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_SMALLEST:
                newX = r.x + r.width - bounds.width;
                break;
            default:
                newX = (scrollJumpInsets.right < 0)
                       ? (bounds.width * (-scrollJumpInsets.right) / 100 )
                       : scrollJumpInsets.right * defaultSpaceWidth;
                newX = Math.min(newX, bounds.width - r.width);
                newX = (r.x + r.width) + newX - bounds.width;
                break;
            }
            updateVirtualWidth(newX + bounds.width);
        }

        if (r.y < bounds.y) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_MOVE:
                newY = r.y;
                newY -= (scrollJumpInsets.top < 0)
                        ? (bounds.height * (-scrollJumpInsets.top) / 100 )
                        : scrollJumpInsets.top * charHeight;
                break;
            case SCROLL_SMALLEST:
                newY = r.y;
                break;
            case SCROLL_DEFAULT:
            default:
                newY = r.y - (bounds.height - r.height) / 2; // center
                break;
            }
            newY = Math.max(newY, 0);
        } else if (r.y + r.height > bounds.y + bounds.height) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_MOVE:
                newY = (r.y + r.height) - bounds.height;
                newY += (scrollJumpInsets.bottom < 0)
                        ? (bounds.height * (-scrollJumpInsets.bottom) / 100 )
                        : scrollJumpInsets.bottom * charHeight;
                break;
            case SCROLL_SMALLEST:
                newY = (r.y + r.height) - bounds.height;
                break;
            case SCROLL_DEFAULT:
            default:
                newY = r.y - (bounds.height - r.height) / 2; // center
                break;
            }
            newY = Math.max(newY, 0);
        }

        if (move) {
            setExtentPosition(newX, newY);
        }
        return move;
    }

    void setExtentPosition(int x, int y) {
        JViewport port = getParentViewport();
        if (port != null) {
            Point p = new Point(Math.max(x, 0), Math.max(y, 0));
            port.setViewPosition(p);
        }
    }

    public void adjustWindow(int caretPercentFromWindowTop) {
        final Rectangle bounds = getExtentBounds();
        if (component != null && (component.getCaret() instanceof Rectangle)) {
            Rectangle caretRect = (Rectangle)component.getCaret();
            bounds.y = caretRect.y - (caretPercentFromWindowTop * bounds.height) / 100
                       + (caretPercentFromWindowTop * charHeight) / 100;
            Utilities.runInEventDispatchThread(
                new Runnable() {
                    public void run() {
                        scrollRectToVisible(bounds, SCROLL_SMALLEST);
                    }
                }
            );
        }
    }

    /** Set the dot according to the currently visible screen window.
    * #param percentFromWindowTop percentage giving the distance of the caret
    *  from the top of the currently visible window.
    */
    public void adjustCaret(int percentFromWindowTop) {
        JTextComponent c = component;
        if (c != null) {
            ExtUI extUI = Utilities.getExtUI(c);
            Rectangle bounds = extUI.getExtentBounds();
            bounds.y += (percentFromWindowTop * bounds.height) / 100
                        - (percentFromWindowTop * extUI.charHeight) / 100;
            try {
                int pos = ((BaseTextUI)c.getUI()).getPosFromY(bounds.y);
                if (pos >= 0) {
                    caretSetDot(pos, null, SCROLL_SMALLEST);
                }
            } catch (BadLocationException e) {
            }
        }
    }

    public void caretSetDot(int pos, Rectangle scrollRect, int scrollPolicy) {
        if (component != null) {
            Caret caret = component.getCaret();
            if (caret instanceof BaseCaret) {
                ((BaseCaret)caret).setDot(pos, scrollRect, scrollPolicy);
            } else {
                caret.setDot(pos);
            }
        }
    }

    public void caretMoveDot(int pos, Rectangle scrollRect, int scrollPolicy) {
        if (component != null) {
            Caret caret = component.getCaret();
            if (caret instanceof BaseCaret) {
                ((BaseCaret)caret).moveDot(pos, scrollRect, scrollPolicy);
            } else {
                caret.moveDot(pos);
            }
        }
    }

    /** This method is called by textui to do the paint.
    * It is forwarded either to paint through the image
    * and then copy the image area to the screen or to
    * paint directly to this graphics. The real work occurs
    * in Drawer.
    */
    protected void paint(Graphics g) {
        if (component != null) { // component must be installed
            if (!fontsInited && g != null) {
                initFonts(g);
                getExtentBounds(lastExtentBounds);
            }
            ((BaseTextUI)component.getUI()).paintRegion(g);
        }
    }

}


/*
 * Log
 *  56   Gandalf-post-FCS1.48.1.6    4/18/00  Miloslav Metelka font height computing 
 *       fix
 *  55   Gandalf-post-FCS1.48.1.5    4/17/00  Miloslav Metelka printing fixed
 *  54   Gandalf-post-FCS1.48.1.4    4/5/00   Miloslav Metelka status bar patch2
 *  53   Gandalf-post-FCS1.48.1.3    4/5/00   Miloslav Metelka getExtComponent() patch
 *  52   Gandalf-post-FCS1.48.1.2    3/9/00   Miloslav Metelka ensure settingsChange() 
 *       is called
 *  51   Gandalf-post-FCS1.48.1.1    3/9/00   Miloslav Metelka removed debug msg
 *  50   Gandalf-post-FCS1.48.1.0    3/8/00   Miloslav Metelka 
 *  49   Gandalf   1.48        3/8/00   Miloslav Metelka project creating dedlok 
 *       fix
 *  48   Gandalf   1.47        2/18/00  Miloslav Metelka #5766 fix
 *  47   Gandalf   1.46        2/16/00  Miloslav Metelka obtaining font height 
 *       fixed
 *  46   Gandalf   1.45        1/26/00  Miloslav Metelka default coloring 
 *       applying moved
 *  45   Gandalf   1.44        1/16/00  Miloslav Metelka 
 *  44   Gandalf   1.43        1/15/00  Miloslav Metelka 
 *  43   Gandalf   1.42        1/14/00  Miloslav Metelka 
 *  42   Gandalf   1.41        1/13/00  Miloslav Metelka 
 *  41   Gandalf   1.40        1/10/00  Miloslav Metelka 
 *  40   Gandalf   1.39        1/6/00   Miloslav Metelka 
 *  39   Gandalf   1.38        1/4/00   Miloslav Metelka 
 *  38   Gandalf   1.37        12/28/99 Miloslav Metelka 
 *  37   Gandalf   1.36        11/24/99 Miloslav Metelka 
 *  36   Gandalf   1.35        11/14/99 Miloslav Metelka 
 *  35   Gandalf   1.34        11/11/99 Miloslav Metelka 
 *  34   Gandalf   1.33        11/10/99 Miloslav Metelka 
 *  33   Gandalf   1.32        11/8/99  Miloslav Metelka 
 *  32   Gandalf   1.31        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  31   Gandalf   1.30        10/10/99 Miloslav Metelka 
 *  30   Gandalf   1.29        10/8/99  Miloslav Metelka stability improvements
 *  29   Gandalf   1.28        10/6/99  Miloslav Metelka 
 *  28   Gandalf   1.27        10/4/99  Miloslav Metelka 
 *  27   Gandalf   1.26        9/30/99  Miloslav Metelka 
 *  26   Gandalf   1.25        9/16/99  Miloslav Metelka 
 *  25   Gandalf   1.24        9/15/99  Miloslav Metelka 
 *  24   Gandalf   1.23        9/10/99  Miloslav Metelka 
 *  23   Gandalf   1.22        8/20/99  Miloslav Metelka 
 *  22   Gandalf   1.21        8/19/99  Miloslav Metelka automatic view shrink
 *  21   Gandalf   1.20        8/19/99  Miloslav Metelka printing line nums
 *  20   Gandalf   1.19        8/17/99  Miloslav Metelka 
 *  19   Gandalf   1.18        8/9/99   Miloslav Metelka ClassCasts errors for 
 *       editor kit
 *  18   Gandalf   1.17        7/29/99  Miloslav Metelka 
 *  17   Gandalf   1.16        7/26/99  Miloslav Metelka 
 *  16   Gandalf   1.15        7/22/99  Miloslav Metelka 
 *  15   Gandalf   1.14        7/21/99  Miloslav Metelka 
 *  14   Gandalf   1.13        7/20/99  Miloslav Metelka 
 *  13   Gandalf   1.12        7/9/99   Miloslav Metelka 
 *  12   Gandalf   1.11        7/2/99   Miloslav Metelka 
 *  11   Gandalf   1.10        6/29/99  Miloslav Metelka Scrolling and patches
 *  10   Gandalf   1.9         6/25/99  Miloslav Metelka from floats back to ints
 *  9    Gandalf   1.8         6/24/99  Miloslav Metelka Drawing improved
 *  8    Gandalf   1.7         6/22/99  Miloslav Metelka 
 *  7    Gandalf   1.6         6/8/99   Miloslav Metelka 
 *  6    Gandalf   1.5         6/1/99   Miloslav Metelka 
 *  5    Gandalf   1.4         5/18/99  Miloslav Metelka patched printing
 *  4    Gandalf   1.3         5/15/99  Miloslav Metelka fixes
 *  3    Gandalf   1.2         5/13/99  Miloslav Metelka 
 *  2    Gandalf   1.1         5/7/99   Miloslav Metelka line numbering and fixes
 *  1    Gandalf   1.0         5/5/99   Miloslav Metelka 
 * $
 */

