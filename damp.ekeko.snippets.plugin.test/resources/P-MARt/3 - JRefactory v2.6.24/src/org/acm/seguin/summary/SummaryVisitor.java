/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary;

/**
 *  All items that want to visit a summary tree should implement this 
 *  interface. 
 *
 *@author     Chris Seguin 
 *@created    May 12, 1999 
 */
public interface SummaryVisitor {
	/**
	 *  Visit a summary node. This is the default method. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(Summary node, Object data);


	/**
	 *  Visit a package summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(PackageSummary node, Object data);


	/**
	 *  Visit a file summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(FileSummary node, Object data);


	/**
	 *  Visit a import summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(ImportSummary node, Object data);


	/**
	 *  Visit a type summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(TypeSummary node, Object data);


	/**
	 *  Visit a method summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(MethodSummary node, Object data);


	/**
	 *  Visit a field summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(FieldSummary node, Object data);


	/**
	 *  Visit a parameter summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(ParameterSummary node, Object data);


	/**
	 *  Visit a local variable summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(LocalVariableSummary node, Object data);


	/**
	 *  Visit a variable summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(VariableSummary node, Object data);


	/**
	 *  Visit a type declaration summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(TypeDeclSummary node, Object data);


	/**
	 *  Visit a message send summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(MessageSendSummary node, Object data);


	/**
	 *  Visit a field access summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result 
	 */
	public Object visit(FieldAccessSummary node, Object data);
}
