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

package  org.netbeans.modules.web.wizards.beanjsp.model;

import org.netbeans.modules.web.wizards.beanjsp.ui.*;
import org.netbeans.modules.web.wizards.beanjsp.util.*;
import org.netbeans.modules.web.util.*;

import java.beans.*;
import java.util.*;

import org.openide.util.*;

public class HTMLElement {

    public static final int TEXTFIELD = 0;
    public static final int PASSWORD = 1;
    public static final int TEXTAREA = 2;
    public static final int CHECKBOX = 3;
    public static final int RADIOBUTTON = 4;
    public static final int LISTBOX = 5;
    public static final int CHOICE = 6;
    public static final int BUTTON = 7;
    public static final int TEXT = 8;	 //// for read only
    public static final int HIDDEN = 9;

    public static final HTMLElement TEXTFIELD_ELE = new HTMLElement(HTMLElement.TEXTFIELD);
    public static final HTMLElement PASSWORD_ELE = new HTMLElement(HTMLElement.PASSWORD);
    public static final HTMLElement TEXTAREA_ELE = new HTMLElement(HTMLElement.TEXTAREA);
    public static final HTMLElement CHECKBOX_ELE = new HTMLElement(HTMLElement.CHECKBOX);
    public static final HTMLElement RADIOBUTTON_ELE = new HTMLElement(HTMLElement.RADIOBUTTON);
    public static final HTMLElement LISTBOX_ELE = new HTMLElement(HTMLElement.LISTBOX);
    public static final HTMLElement CHOICE_ELE = new HTMLElement(HTMLElement.CHOICE);
    public static final HTMLElement BUTTON_ELE = new HTMLElement(HTMLElement.BUTTON);
    public static final HTMLElement TEXT_ELE = new HTMLElement(HTMLElement.TEXT);
    public static final HTMLElement HIDDEN_ELE = new HTMLElement(HTMLElement.HIDDEN);


    String htmlType;
    int htmlTypeID;

    String name=""; // NOI18N
    String value="";  //// this could be multiple value[]  // NOI18N

    public HTMLElement(int htmlTypeID) {
        this.htmlTypeID = htmlTypeID;
        updateHTMLType();
    }

    private void updateHTMLType() {
        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        switch(htmlTypeID) {
        case TEXTFIELD:
            htmlType = resBundle.getString("JBW_HTML_TEXTFIELD");				 // NOI18N
            break;
        case PASSWORD:
            htmlType = resBundle.getString("JBW_HTML_PASSWORD");				 // NOI18N
            break;
        case TEXTAREA:
            htmlType = resBundle.getString("JBW_HTML_TEXTAREA");				 // NOI18N
            break;
        case CHECKBOX:
            htmlType = resBundle.getString("JBW_HTML_CHECKBOX");				 // NOI18N
            break;
        case RADIOBUTTON:
            htmlType = resBundle.getString("JBW_HTML_RADIOBUTTON");				 // NOI18N
            break;
        case LISTBOX:
            htmlType = resBundle.getString("JBW_HTML_LISTBOX");					 // NOI18N
            break;
        case CHOICE:
            htmlType = resBundle.getString("JBW_HTML_CHOICE");					 // NOI18N
            break;
        case BUTTON:
            htmlType = resBundle.getString("JBW_HTML_BUTTON");					 // NOI18N
            break;
        case TEXT:
            htmlType = resBundle.getString("JBW_HTML_TEXT");					 // NOI18N
            break;
        case HIDDEN:
            htmlType = resBundle.getString("JBW_HTML_HIDDEN");					 // NOI18N
            break;
        default:
            htmlType = resBundle.getString("JBW_HTML_TEXTFIELD");				 // NOI18N
        }
    }

    public static HTMLElement getHTMLElement(int htmlElementTypeID) {
        switch(htmlElementTypeID) {
        case TEXTFIELD:
            return TEXTFIELD_ELE;
        case PASSWORD:
            return PASSWORD_ELE;
        case TEXTAREA:
            return TEXTAREA_ELE;
        case CHECKBOX:
            return CHECKBOX_ELE;
        case RADIOBUTTON:
            return RADIOBUTTON_ELE;
        case LISTBOX:
            return LISTBOX_ELE;
        case CHOICE:
            return CHOICE_ELE;
        case BUTTON:
            return BUTTON_ELE;
        case TEXT:
            return TEXT_ELE;
        case HIDDEN:
            return HIDDEN_ELE;
        default:
            return TEXTFIELD_ELE;
        }

    }

public String getHTMLType() { return htmlType; }

    public int getHTMLTypeID() { return htmlTypeID; }
    public void setHTMLTypeID(int htmlTypeID) {
        this.htmlTypeID = htmlTypeID;
        updateHTMLType();
    }

    public String getName() { return this.name;}
    public void setName(String name) { this.name = name; }

    public String getValue() { return this.value;}
    public void setValue(String value) { this.value = value; }


    public static String toHTMLCheckBoxValueCode(String value) {
        boolean booleanTrue = (Boolean.valueOf(value)).booleanValue();

        if(booleanTrue) {
            return " \"true\" checked ";				 //NOI18N
        } else {
            return " \"false\" ";						 // NOI18N
        }
    }

    public static String toHTMLRadioButtonValueCode(String value) {
        boolean booleanTrue = (Boolean.valueOf(value)).booleanValue();

        if(booleanTrue) {
            return " \"true\" checked ";				 //NOI18N
        } else {
            return " \"false\" ";						 // NOI18N
        }
    }

    //// jspExpression is : beanVar.isTrue()
    public static String toHTMLCheckBoxValueCode(String value, String jspExpression) {
        return "<% if( "+jspExpression+ " ) { %> \"true\" checked <% } else { %> \"false\" <% } %>";	 // NOI18N
    }

    public static String toHTMLRadioButtonValueCode(String value, String jspExpression) {
        return "<% if( "+jspExpression+ " ) { %> \"true\" checked <% } else { %> \"false\" <% } %>";	 // NOI18N
    }

    public static String toHTMLElementCode(int elementType, String name, String value) {
        String elementCode="";	 // NOI18N

        if(elementType == TEXTFIELD ) {
            elementCode = " <input type=text size=20 name=\""+name+"\" "+		 // NOI18N
                          "value = \""+value +"\" >";										 // NOI18N
        }else if (elementType == PASSWORD ) {
            elementCode = " <input type=password size=20 name=\""+name+"\" "+	 // NOI18N
                          "value = \""+value +"\" >";										 // NOI18N
        }else if (elementType == CHECKBOX ) {
            // <INPUT type="checkbox" checked name="CheckB" value="CheckBValue"> Checkbox &nbsp;
            /// value passed is a special case string with quotes
            /// e.g  \"checkboxvalue\" <% if(bean.isTrue()) { %> checked <% } %>
            elementCode = " <input type=checkbox name=\""+name+"\" "+			 // NOI18N
                          "value = "+value+" >";												 // NOI18N
        }else if (elementType == RADIOBUTTON ) {
            // <INPUT type="radio" checked name="RadioB" value="RadioBValue"> RadioB
            /// value passed is a special case string with quotes
            /// e.g  \"radiovalue\" <% if(bean.isTrue()) { %> checked <% } %>
            elementCode = " <input type=radio name=\""+name+"\" "+				 // NOI18N
                          "value = "+value+" >";												 // NOI18N
        }else if (elementType == TEXTAREA ) {
            elementCode = " <textarea rows=\"3\" cols=\"30\" name=\""+name+"\">"+value+		 // NOI18N
                          "</textarea>";						 // NOI18N
        }else if (elementType == HIDDEN ) {
            elementCode = " <input type=hidden size=20 name=\""+name+"\" "+		 // NOI18N
                          "value = \""+value +"\" >";										 // NOI18N
        }else if (elementType == TEXT ) {
            elementCode = "<br>"+value+" ";										 // NOI18N
        }else if (elementType == CHOICE ) {
            elementCode = " <select name=\""+name+"\" >"+						 // NOI18N
                          "<option value=dummy1>Dummy1</option>"+				 // NOI18N
                          "<option selected value=dummy2>Dummy2</option>"+		 // NOI18N
                          "<option value=dummy3>Dummy3</option>"+				 // NOI18N
                          " </select>";											 // NOI18N
        }else if (elementType == LISTBOX ) {
            elementCode = " <select name=\""+name+"\" "+" size=\"3\" >"+		 // NOI18N
                          "<option value=dummy1>Dummy1</option>"+				 // NOI18N
                          "<option selected value=dummy2>Dummy2</option>"+		 // NOI18N
                          "<option value=dummy3>Dummy3</option>"+				 // NOI18N
                          " </select>";											 // NOI18N
        } else {
            elementCode = " <input type=text size=20 name=\""+name+"\" "+		 // NOI18N
                          "value = \""+value +"\" >";										 // NOI18N
        }

        return elementCode;
    }

    public String toString() { return this.getHTMLType();}

}

