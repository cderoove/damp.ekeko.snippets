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

package org.netbeans.modules.autoupdate;

/** This class is used for performing long tasks which have to
 * show a modal dialog with cancel button.
 * @author  phrebejk 
 */
public interface ProgressDialog {

    static final int PARTIAL_GAUGE = 1;
    static final int OVERALL_GAUGE = 2;
    static final int EXTRA_GAUGE = 4;

    static final int PARTIAL_LABEL = 1;
    static final int OVERALL_LABEL = 2;
    static final int EXTRA_LABEL = 4;

    /** Indexed getter for property gaugeValue.
     *@param index Index of the property.
     *@return Value of the property at <CODE>index</CODE>.
     */
    public int getGaugeValue(int gauge);

    /** Indexed setter for property gaugeValue.
     *@param index Index of the property.
     *@param gaugeValue New value of the property at <CODE>index</CODE>.
     */
    public void setGaugeValue(int gauge, int gaugeValue);

    /** Indexed setter for property gaugeBounds.
     *@param index Index of the property.
     *@param gaugeBounds New value of the property at <CODE>index</CODE>.
     */
    public void setGaugeBounds(int gauge, int gaugeMin, int gaugeMax );

    /** Indexed getter for property labelText.
     *@param index Index of the property.
     *@return Value of the property at <CODE>index</CODE>.
     */ 
    public String getLabelText(int label);

    /** Indexed setter for property labelText.
     *@param index Index of the property.
     *@param labelText New value of the property at <CODE>index</CODE>.
     */
    public void setLabelText(int label, String labelText);

    /** Getter for property title.
     *@return Value of property title.
     */
    String getTitle();

    /** Setter for property title.
     *@param title New value of property title.
     */
    void setTitle(String title);
}

/*
 * Log
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/10/99 Petr Hrebejk    AutoUpdate made to 
 *       wizard
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $
 */
