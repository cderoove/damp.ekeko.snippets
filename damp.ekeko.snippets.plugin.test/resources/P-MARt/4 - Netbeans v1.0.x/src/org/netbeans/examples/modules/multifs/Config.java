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

package org.netbeans.examples.modules.multifs;

import org.openide.filesystems.FileSystem;

import java.io.Serializable;

final class Config implements Serializable {
    final FileSystem[] reads;
    final FileSystem defWrite;
    final Sieve[] writes;
    static final long serialVersionUID =-2331840986133610828L;
    Config (FileSystem[] reads, FileSystem defWrite, Sieve[] writes) {
        this.reads = reads;
        this.defWrite = defWrite;
        this.writes = writes;
    }
    static final class Sieve implements Serializable {
        final FileSystem fs;
        final String sieve;
        final boolean byExt;
        static final long serialVersionUID =-8027860651162502780L;
        Sieve (FileSystem fs, String sieve, boolean byExt) {
            this.fs = fs;
            this.sieve = sieve;
            this.byExt = byExt;
        }
    }
}
