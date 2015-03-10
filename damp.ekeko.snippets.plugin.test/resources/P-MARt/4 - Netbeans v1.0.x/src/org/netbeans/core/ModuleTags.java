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

/** Interface contains XML Tag constants
 *
 * @author  Administrator
 * @version 
 */
interface ModuleTags {

    static final String XML_HEADER = "<?xml version='1.0'?>"; // NOI18N

    static final String MODULES_HEADER = "<modules>"; // NOI18N
    static final String MODULES_FOOTER = "</modules>"; // NOI18N

    static final String MODULE = "module"; // NOI18N
    static final String TEST_MODULE = "module_test"; // NOI18N
    static final String DELETED_MODULE = "module_deleted"; // NOI18N

    static final String CODENAMEBASE = "codenamebase"; // NOI18N
    static final String RELEASE = "release"; // NOI18N
    static final String SPECVERSION = "specversion"; // NOI18N
    static final String URL = "url"; // NOI18N
    static final String ENABLED = "enabled"; // NOI18N
    static final String DELETED = "deleted"; // NOI18N

    static final String BASE = "base"; // NOI18N
    static final String BASE_USER = "user"; // NOI18N
    static final String BASE_CENTRAL = "central"; // NOI18N
    static final String BASE_JAR = "jarfile"; // NOI18N

    static final String NEW_LINE = "\n"; // NOI18N

    static final String MANIFEST = "manifest"; // NOI18N

    static final String TAB = "  "; // NOI18N
    static final String TABx2 = TAB + TAB;

}