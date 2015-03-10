package org.acm.seguin.refactor.method;

import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.summary.FieldAccessSummary;
import org.acm.seguin.summary.LocalVariableSummary;
import org.acm.seguin.summary.MessageSendSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.Summary;

/**
 *  This class contains a static method to determine if the method in question
 *  makes any references to the local object
 *
 *@author    Chris Seguin
 */
class ObjectReference {
	/**
	 *  Determines if this object is referenced
	 *
	 *@param  methodSummary  the method summary
	 *@return                true if the object is referenced
	 */
	public static boolean isReferenced(MethodSummary methodSummary) {
		LinkedList locals = new LinkedList();
		Iterator iter = methodSummary.getDependencies();
		if (iter != null) {
			while (iter.hasNext()) {
				Summary next = (Summary) iter.next();
				if (next instanceof LocalVariableSummary) {
					locals.add(next.getName());
				}
				else if (next instanceof FieldAccessSummary) {
					FieldAccessSummary fas = (FieldAccessSummary) next;
					if ((fas.getPackageName() == null) &&
							(fas.getObjectName() == null) &&
							(!locals.contains(fas.getFieldName()))) {
						return true;
					}
				}
				else if (next instanceof MessageSendSummary) {
					MessageSendSummary mss = (MessageSendSummary) next;
					if ((mss.getPackageName() == null) &&
							((mss.getObjectName() == null) ||
							(mss.getObjectName().equals("this")))) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
