/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.io;

import java.io.File;

/**
 *  Traverses a directory structure and backups all java files found
 *
 *@author    Chris Seguin
 *@date      May 12, 1999
 */
public abstract class DirectoryTreeTraversal
{
	//  Instance Variables
	private File startingPoint;


	/**
	 *  Traverses a directory tree structure
	 *
	 *@param  init  the initial directory
	 */
	public DirectoryTreeTraversal(String init)
	{
		startingPoint = new File(init);
	}


	/**
	 *  Starts the tree traversal
	 */
	public void go()
	{
		if (startingPoint.exists())
		{
			traverse(startingPoint);
		}
	}


	/**
	 *  Determines if this file should be handled by this traversal
	 *
	 *@param  currentFile  the current file
	 *@return              true if the file should be handled
	 */
	protected abstract boolean isTarget(File currentFile);


	/**
	 *  Gets the Allowed attribute of the DirectoryTreeTraversal object
	 *
	 *@param  currentDirectory  Description of Parameter
	 *@return                   The Allowed value
	 */
	protected boolean isAllowed(File currentDirectory)
	{
		return true;
	}


	/**
	 *  Starts the tree traversal
	 *
	 *@param  current  Description of Parameter
	 */
	protected void traverse(File current)
	{
		if (current.isDirectory())
		{
			if (isAllowed(current))
			{
				arriveAtDir(current);
				String[] list = current.list();
				for (int ndx = 0; ndx < list.length; ndx++)
				{
					traverse(new File(current, list[ndx]));
				}
				leaveDir(current);
			}
		}
		else if (isTarget(current))
		{
			visit(current);
		}
	}


	/**
	 *  Visits the current file
	 *
	 *@param  currentFile  the current file
	 */
	protected abstract void visit(File currentFile);


	/**
	 *  Program called when we arrive at a directory
	 *
	 *@param  currentFile  the current file
	 */
	protected void arriveAtDir(File currentFile)
	{
	}


	/**
	 *  Program called when we arrive at a directory
	 *
	 *@param  currentFile  the current file
	 */
	protected void leaveDir(File currentFile)
	{
	}
}
