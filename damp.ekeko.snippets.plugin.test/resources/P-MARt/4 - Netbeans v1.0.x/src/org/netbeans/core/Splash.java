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

package org.netbeans.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.net.URL;
import javax.swing.*;

import org.openide.TopManager;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/** A class that encapsulates all the splash screen things.
*
* @author Ian Formanek, David Peroutka
*/
public class Splash  implements SwingConstants {

    /** The splash image */
    private static Image splash;

    private static Window splashWindow;
    private static SplashOutput splashOutput;

    private static SplashDialog splashDialog;

    static {
        URL u = NbBundle.getLocalizedFile (
                    "org.netbeans.core.resources.splash", // NOI18N
                    "gif" // NOI18N
                );

        splash = Toolkit.getDefaultToolkit ().getImage (u);
    }

    private static String getMainWindowTitle () {
        String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
        return java.text.MessageFormat.format (org.openide.util.NbBundle.getBundle(MainWindow.class).getString ("CTL_MainWindow_Title"),
                                               new Object[] { buildNumber });
    }

    public static void showSplashDialog () {
        if (splashDialog == null)
            splashDialog = new SplashDialog ();
        splashDialog.print (getMainWindowTitle ());
        splashDialog.show ();
    }

    public static SplashOutput showSplash () {
        if (Utilities.isWindows () ||
                (Utilities.getOperatingSystem () == Utilities.OS_SOLARIS) ||
                (Utilities.getOperatingSystem () == Utilities.OS_OS2)) {
            // only some systems supports non-frame windows
            splashWindow = new SplashWindow();
        } else {
            splashWindow = (Window)new SplashFrame();
        }

        splashOutput = (SplashOutput)splashWindow;
        // show splash
        splashWindow.show ();
        splashWindow.toFront ();
        return splashOutput;
    }

    public static void hideSplash () {
        splashWindow.setVisible (false);
        splashWindow.dispose ();
    }

    public void print (String s) {
        splashOutput.print (s);
    }

    interface SplashOutput {
        public void print (String s);
    }

    /**
     * Standard way how to place the window to the center of the screen.
     */
    public static final void center(Window c) {
        c.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = c.getSize();
        c.setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
    }

    /**
     * This class implements double-buffered splash screen component.
     */
    static class SplashComponent extends JComponent {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -1162806313274828742L;

        private static final Font font = new Font("SansSerif", Font.PLAIN, 12); // NOI18N
        private static final Rectangle view = new Rectangle(9, 52, 380, 73);

        private Image image;
        private Rectangle dirty = new Rectangle();

        /**
         * Creates a new splash screen component.
         */
        public SplashComponent() {
            image = new ImageIcon(splash).getImage(); // load!
        }

        /**
         * Defines the single line of text this component will display.
         */
        public void setText(String text) {
            // draw background
            image = createImage(image.getWidth(null), image.getHeight(null));
            Graphics graphics = image.getGraphics();
            graphics.drawImage(splash, 0, 0, null);
            // draw text
            graphics.setFont(font);
            graphics.setColor(Color.white);
            Rectangle rect = new Rectangle();
            FontMetrics metrics = graphics.getFontMetrics();
            SwingUtilities.layoutCompoundLabel(metrics, text, null,
                                               BOTTOM, LEFT, BOTTOM, LEFT,
                                               this.view, new Rectangle(), rect, 0);
            graphics.drawString(text, rect.x, rect.y + metrics.getAscent());

            // update screen (assume repaint manager optimizes unions;)
            repaint(this.dirty);
            repaint(rect);
            this.dirty = rect;
        }

        /**
         * Override update to *not* erase the background before painting.
         */
        public void update(Graphics g) {
            paint(g);
        }

        /**
         * Renders this component to the given graphics.
         */
        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }

        public Dimension getPreferredSize() {
            return new Dimension(image.getWidth (null), image.getHeight (null));
        }

        public boolean isOpaque () {
            return true;
        }
    }


    static class SplashWindow extends Window implements SplashOutput {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 4838519880359397841L;

        private final SplashComponent splashComponent = new SplashComponent();

        /** Creates a new SplashWindow */
        public SplashWindow () {
            super(new Frame());
            // add splash component
            setLayout (new java.awt.BorderLayout ());
            add(splashComponent, java.awt.BorderLayout.CENTER);
            Splash.center(this);
        }

        public java.awt.Dimension getPreferredSize () {
            return new java.awt.Dimension (400, 300);
        }

        /**
         * Prints the given progress message on the splash screen.
         * @param x specifies a string that is to be displayed
         */
        public void print(String x) {
            splashComponent.setText(x);
        }
    }

    static class SplashFrame extends JFrame implements SplashOutput {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 3200319077899134000L;

        private final SplashComponent splashComponent = new SplashComponent();

        /** Creates a new SplashFrame */
        public SplashFrame () {
            super (getMainWindowTitle ());
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
            // add splash component
            getContentPane().add(splashComponent);
            Splash.center(this);
        }

        /**
         * Prints the given progress message on the splash screen.
         * @param x specifies a string that is to be displayed
         */
        public void print(String x) {
            splashComponent.setText(x);
        }
    }

    static class SplashDialog extends JDialog implements SplashOutput {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 5185644855500178404L;

        private final SplashComponent splashComponent = new SplashComponent();

        /** Creates a new SplashDialog */
        public SplashDialog () {
            super (TopManager.getDefault ().getWindowManager ().getMainWindow (), true);
            setTitle (org.openide.util.NbBundle.getBundle(Splash.class).
                      getString ("CTL_About_Title"));
            setResizable (false);
            // add splash component
            getContentPane().add(splashComponent, "Center"); // NOI18N
            addKeyListener (new java.awt.event.KeyAdapter () {
                                public void keyPressed (java.awt.event.KeyEvent evt) {
                                    setVisible (false);
                                }
                            }
                           );
            Splash.center(this);
        }

        /**
         * Prints the given progress message on the splash screen.
         * @param x specifies a string that is to be displayed
         */
        public void print(String x) {
            splashComponent.setText(x);
        }
    }
}

/*
 * Log
 *  24   Gandalf-post-FCS1.22.1.0    4/7/00   Jaroslav Tulach Recognizes localized 
 *       splash screens. E.g. splash_ja.gif for japan version.
 *  23   Gandalf   1.22        1/25/00  Jesse Glick     #5454 (Splash window 
 *       title).
 *  22   Gandalf   1.21        1/18/00  Ian Formanek    Fixed text displayed in 
 *       Splash if used in About box
 *  21   Gandalf   1.20        1/13/00  Jaroslav Tulach I18N
 *  20   Gandalf   1.19        11/18/99 Pavel Buzek     
 *  19   Gandalf   1.18        11/15/99 Pavel Buzek     Name of SplashFrame 
 *       changed.
 *  18   Gandalf   1.17        11/12/99 Pavel Buzek     different color, 
 *       alignment and position of messages on splash
 *  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        10/9/99  Ian Formanek    Splash with Sun 
 *       copyright
 *  15   Gandalf   1.14        8/6/99   Ian Formanek    SplashDialog is not 
 *       resizable
 *  14   Gandalf   1.13        7/12/99  Ian Formanek    Fixed last change
 *  13   Gandalf   1.12        7/12/99  Ian Formanek    removed registration 
 *       code
 *  12   Gandalf   1.11        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   Gandalf   1.10        5/17/99  Ian Formanek    Updated text position 
 *       for the new splash
 *  10   Gandalf   1.9         4/14/99  Ian Formanek    Patched bug #1533 - The 
 *       Splash screen on Windows is one pixel smaller in vertical size
 *  9    Gandalf   1.8         4/8/99   Ian Formanek    Changed Object.class -> 
 *       getClass ()
 *  8    Gandalf   1.7         4/7/99   Ian Formanek    Updated for newer 
 *       version of splash image
 *  7    Gandalf   1.6         4/5/99   Ian Formanek    Positioning of printed 
 *       text in splash reflects changed splash image
 *  6    Gandalf   1.5         3/29/99  Ian Formanek    Removed obsoleted 
 *       imports of ButtonBar
 *  5    Gandalf   1.4         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  4    Gandalf   1.3         3/9/99   Jaroslav Tulach ButtonBar  
 *  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
