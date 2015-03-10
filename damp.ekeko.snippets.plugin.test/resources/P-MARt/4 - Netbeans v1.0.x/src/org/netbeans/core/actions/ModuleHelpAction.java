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

package org.netbeans.core.actions;

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;

import org.openide.TopManager;
import org.openide.awt.Actions;
import org.openide.awt.JInlineMenu;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

import org.netbeans.core.Help;

/** Shows a list of home help pages for modules.
*
* @author Jesse Glick
*/
public class ModuleHelpAction extends SystemAction implements Presenter.Menu, Presenter.Popup {
    /** Implementation of ActSubMenuInt */
    private static ActSubMenuModel model;

    //static final long serialVersionUID =2022674936562918639L;
    static final long serialVersionUID =2022674936562918639L;
    /** generated Serialized Version UID */
    // static final long serialVersionUID = ;

    /** Do nothing.
    */
    public void actionPerformed (ActionEvent e) {
    }

    /** URL to this action.
    * @return URL to the action icon
    */
    public String iconResource () {
        return "/org/netbeans/core/resources/actions/moduleHelp.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (ModuleHelpAction.class);
    }

    public String getName() {
        return NbBundle.getBundle (ModuleHelpAction.class).getString("ModuleHelp");
    }

    /* Returns a submenu that will presents this action in menu bar
    * @return the JMenuItem representation for this action (which is submenu)
    */
    public JMenuItem getMenuPresenter() {
        Help.Impl help = Help.getDefault ();
        if (help.getHomesByDisplay ().size () > 0 || help.getMasterID () != null)
            return new SpecialSubMenu(this, getModel(), false);
        else
            return new JInlineMenu ();
    }

    /* Returns a submneu that will present this action in a PopupMenu.
    * @return the JMenuItem representation for this action
    */
    public JMenuItem getPopupPresenter() {
        Help.Impl help = Help.getDefault ();
        if (help.getHomesByDisplay ().size () > 0 || help.getMasterID () != null)
            return new SpecialSubMenu(this, getModel(), true);
        else
            return new JInlineMenu ();
    }

    /** Convenience method for obtaining submenu model */
    static ActSubMenuModel getModel () {
        if (model == null)
            model = new ActSubMenuModel();
        return model;
    }

    /** SubMenu of this action - notifies data model about
    * ancestor changes (addNotify, removeNotify)
    */
    private static final class SpecialSubMenu extends Actions.SubMenu {

        static final long serialVersionUID =-4734612283802319986L;
        SpecialSubMenu (SystemAction aAction, Actions.SubMenuModel model,
                        boolean popup) {
            super(aAction, model, popup);
        }

        /** Notifies data model in adition to normal behaviour */
        public void addNotify () {
            ModuleHelpAction.model.addNotify();
            super.addNotify();
        }

        /** Notifies data model in adition to normal behaviour */
        public void removeNotify () {
            ModuleHelpAction.model.removeNotify();
            super.removeNotify();
        }

    } // end of SpecialSubMenu

    /** Implementation of SubMenuModel.
    */
    private static final class ActSubMenuModel implements Actions.SubMenuModel {
        /**
         * @associates String 
         */
        private List items; // List<{String homeID, String display}>

        public int getCount() {
            //System.err.println ("getCount");
            return items.size ();
        }

        public String getLabel (int index) {
            //System.err.println ("getLabel " + index);
            return ((String[]) items.get (index))[1];
        }

        public HelpCtx getHelpCtx (int index) {
            return new HelpCtx (((String[]) items.get (index))[0]);
        }

        public void performActionAt (int index) {
            //System.err.println ("performActionAt " + index);
            String homeID = ((String[]) items.get (index))[0];
            TopManager.getDefault ().showHelp (new HelpCtx (homeID));
        }

        public void addChangeListener (ChangeListener l) {
        }

        public void removeChangeListener (ChangeListener l) {
        }

        void addNotify () {
            //System.err.println ("addNotify called");
            Help.Impl help = Help.getDefault ();
            Map homesByDisplay = help.getHomesByDisplay ();
            List dists = new ArrayList ();
            List regs = new ArrayList ();
            Iterator it = homesByDisplay.entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry entry = (Map.Entry) it.next ();
                String display = (String) entry.getKey ();
                String home = (String) entry.getValue ();
                boolean dist = help.isDistinguished (home);
                (dist ? dists : regs).add (new String[] { home, display });
                //System.err.println("Help set: display=" + display + "; home=" + home + "; dist=" + dist);
            }
            Comparator c = new Comparator () {
                               public int compare (Object o1, Object o2) {
                                   return ((String[]) o1)[1].compareTo (((String[]) o2)[1]);
                               }
                           };
            Collections.sort (dists, c);
            Collections.sort (regs, c);
            items = new ArrayList (dists);
            if (dists.size () > 0 && regs.size () > 0)
                items.add (new String[] { null, null });
            items.addAll (regs);
            /*
            for (int i = 0; i < displayNames.size (); i++)
              System.err.println ("Pair: " + displayNames.get (i) + " " + homeIDs.get (i));
            */
            String id = help.getMasterID ();
            if (id != null) {
                if (! items.isEmpty ())
                    items.add (new String[] { null, null });
                items.add (new String[] { id, help.getMasterDisplayName () });
            }
        }

        void removeNotify () {
            /* Not safe to implement--called at inappropriate times.
            //System.err.println ("removeNotify called");
            displayNames = null;
            homeIDs = null;
            */
        }

    } // end of ActSubMenuModel

}

/*
 * Log
 *  9    Gandalf   1.8         1/12/00  Ales Novak      i18n
 *  8    Gandalf   1.7         12/21/99 Jesse Glick     Putting User's Guide off
 *       from the rest of the help menu items to visually distinguish it.
 *  7    Gandalf   1.6         12/20/99 Jesse Glick     Reorganized Help | 
 *       Features to be Help | Documentation, killing old UG browse action, 
 *       better labelling of master help set, etc.
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  4    Gandalf   1.3         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  3    Gandalf   1.2         7/19/99  Jesse Glick     Does not display when 
 *       there are no items for it.
 *  2    Gandalf   1.1         7/16/99  Jesse Glick     Actions.SubMenuModel.getHelpCtx
 *       
 *  1    Gandalf   1.0         7/9/99   Jesse Glick     
 * $
 * Beta Change History:
 */
