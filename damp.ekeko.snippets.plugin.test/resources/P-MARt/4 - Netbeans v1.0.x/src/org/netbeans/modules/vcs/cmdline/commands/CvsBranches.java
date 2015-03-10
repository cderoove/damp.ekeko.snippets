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

package org.netbeans.modules.vcs.cmdline.commands;

import org.netbeans.modules.vcs.cmdline.*;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.exec.*;
import java.util.*;

/**
 *
 * @author  Martin Entlicher
 * @version 
 */
public class CvsBranches extends VcsAdditionalCommand {

    private String rootDir = null;
    private String dir = null;
    private String file = null;
    Hashtable vars = null;
    CvsLogInfo logInfo = new CvsLogInfo();
    private String[] diffArgs = null;
    private NoRegexListener stdoutNRListener = null;
    private NoRegexListener stderrNRListener = null;
    private RegexListener stdoutListener = null;
    private RegexListener stderrListener = null;
    private String dataRegex = null;
    private String errorRegex = null;

    /**
     * @associates Point 
     */
    Hashtable branchPositions = null;
    int graphHeight = 0;
    int graphWidth = 0;

    /**
     * @associates Vector 
     */
    private Hashtable itemPositionIntervals = null;

    /** Creates new CvsBranches */
    public CvsBranches() {
    }

    /**
     * Calculates the positions of branches and the number of elements along x and y axis.
     */
    private void computeBranchesPositions() {
        CvsRevisionGraphItem root = logInfo.getRevisionGraph().getRoot();
        int xPos = 0;
        int yPos = 0;
        graphHeight = 1;
        graphWidth = 1;
        branchPositions = new Hashtable();
        itemPositionIntervals = new Hashtable();
        computeBranchesPositions(root, xPos, yPos);
    }

    private void computeBranchesPositions(CvsRevisionGraphItem item, int xPos, int yPos) {
        if (xPos >= graphWidth) graphWidth = xPos + 1;
        if (yPos >= graphHeight) graphHeight = yPos + 1;
        if (item == null) return;
        addItemPosition(xPos, yPos);
        computeBranchesPositions(item.next, xPos, yPos + 1);
        item.setXPos(xPos);
        item.setYPos(yPos);
        Vector branches = item.getBranches();
        if (branches != null) {
            Enumeration enum = branches.elements();
            while (enum.hasMoreElements()) {
                CvsRevisionGraphItem branch = (CvsRevisionGraphItem) enum.nextElement();
                int xb = getItemFreePosition(xPos, yPos + 1);
                computeBranchesPositions(branch, xb, yPos + 1);
            }
        }
    }

    private void addItemPosition(int x, int y) {
        Vector intervals = (Vector) itemPositionIntervals.get(new Integer(x));
        if (intervals == null) {
            intervals = new Vector();
            intervals.add(new Integer(y));
            itemPositionIntervals.put(new Integer(x), intervals);
        } else {
            intervals.add(new Integer(y));
            //Enumeration enum = intervals.elements();
            //while(enum.hasMoreElements()) {
            // enum.nextElement();
        }
    }

    private int getItemFreePosition(int xPos, int yPos) {
        int x;
        for(x = xPos + 1; itemPositionIntervals.get(new Integer(x)) != null; x++);
        return x;
    }

    private void computeBranchesPositions_last(CvsRevisionGraphItem item, int xPos, int yPos) {
        if (xPos >= graphWidth) graphWidth = xPos + 1;
        if (yPos >= graphHeight) graphHeight = yPos + 1;
        if (item == null) return;
        Vector branches = item.getBranches();
        if (branches != null) {
            Enumeration enum = branches.elements();
            while (enum.hasMoreElements()) {
                CvsRevisionGraphItem branch = (CvsRevisionGraphItem) enum.nextElement();
                String branchName = branch.getRevision();
                xPos++;
                branchPositions.put(branchName, new java.awt.Point(xPos, yPos + 1));
                computeBranchesPositions(branch.next, xPos, yPos + 1);
            }
        }
        CvsRevisionGraphItem next = item.getNext();
        if (next != null) computeBranchesPositions(next, xPos, yPos + 1);
    }

    private void drawBranches() {
        //logInfo.getRevisionGraph();
        computeBranchesPositions();
        CvsBranchFrame branchFrame = new CvsBranchFrame(logInfo, this);
        MiscStuff.centerWindow(branchFrame);
        branchFrame.setPositions(graphWidth, graphHeight, branchPositions);
        branchFrame.setVisible(true);
        //logInfo.getRevisionGraph().getRoot().print();
    }

    public boolean doDiff(String revision1, String revision2) {
        CvsDiff diff = new CvsDiff();
        String args[] = null;
        if (revision1 != null) {
            if (revision2 != null) {
                args = new String[4];
                args[1] = revision2;
            } else {
                args = new String[3];
            }
            args[0] = revision1;
        } else args = new String[2];
        for(int i = 0; i < 2; i++) args[i + args.length - 2] = diffArgs[i];
        return diff.exec(vars, args, stdoutNRListener, stderrNRListener,
                         stdoutListener, dataRegex, stderrListener, errorRegex);
    }

    public void close() {
    }

    public boolean exec(Hashtable vars, String[] args,
                        NoRegexListener stdoutNRListener, NoRegexListener stderrNRListener,
                        RegexListener stdoutListener, String dataRegex,
                        RegexListener stderrListener, String errorRegex) {
        this.vars = vars;
        this.stdoutNRListener = stdoutNRListener;
        this.stderrNRListener = stderrNRListener;
        this.stdoutListener = stdoutListener;
        this.dataRegex = dataRegex;
        this.stderrListener = stderrListener;
        this.errorRegex = errorRegex;
        boolean success;
        if (args.length < 3) {
            String message = "Too few arguments to View Branches command !"; // NOI18N
            String[] elements = { message };
            if (stderrListener != null) stderrListener.match(elements);
            if (stderrNRListener != null) stderrNRListener.match(message);
            return false;
        }
        String[] logInfoArgs = new String[1];
        logInfoArgs[0] = args[0];
        diffArgs = new String[2];
        diffArgs[0] = args[1];
        diffArgs[1] = args[2];
        success = this.logInfo.updateLogInfo(vars, logInfoArgs, stdoutNRListener, stderrNRListener);
        if (success) {
            drawBranches();
        }
        return success;
    }
}