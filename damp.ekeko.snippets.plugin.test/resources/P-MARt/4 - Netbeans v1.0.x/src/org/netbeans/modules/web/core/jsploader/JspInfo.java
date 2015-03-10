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

package org.netbeans.modules.web.core.jsploader;

/** High-level information about a JSP file.
* Implementation may be created with various level of error checking, 
* so reliability of this information may differ for each instance.
*/
public interface JspInfo {

    /** Files included by include directive,
    * resolved to absolute URL within the context. */
    public String[] getIncludedFiles();

    //  deferred until we have JSP 1.1
    /** TagLibaryInfo-s for used tag libraries. */
    //  public TagLibraryInfo[] getTagLibraries();

    /** Class names of the bean used by this page. */
    public String[] getBeans();

    /** File used as the error page,
    * resolved to absolute URL within the context. */
    public String[] getErrorPage();

    /** Files referenced by include and forward actions.
    * These are either absolute URLs within the context, or
    * JSP expressions. Distrinction may be made using JspUtil.isExpression(). */
    public String[] getReferencedPages();

    /** Returns whether this page is an error page. */
    public boolean isErrorPage();

}