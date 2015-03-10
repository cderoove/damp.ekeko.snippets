/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.metrics;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 *  Metrics table cell renderer
 *
 *@author     Chris Seguin
 *@created    July 28, 1999
 */
class MetricsTableCellRenderer implements TableCellRenderer {
	Font headerFont;
	Font normalFont;


	/**
	 *  Constructor for the MetricsTableCellRenderer object
	 */
	MetricsTableCellRenderer() {
		headerFont = new Font("Serif", Font.BOLD, 14);
		normalFont = new Font("Serif", Font.PLAIN, 14);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  table       Description of Parameter
	 *@param  value       Description of Parameter
	 *@param  isSelected  Description of Parameter
	 *@param  hasFocus    Description of Parameter
	 *@param  row         Description of Parameter
	 *@param  column      Description of Parameter
	 *@return             Description of the Returned Value
	 */
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		JLabel label = new JLabel((String) value);
		label.setBorder(new EmptyBorder(4, 10, 4, 10));

		if (row == 0) {
			label.setFont(headerFont);
		}
		else {
			label.setFont(normalFont);
		}

		if (row == 0) {
			label.setHorizontalAlignment(SwingConstants.CENTER);
		}
		else if (column == 1) {
			label.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		else {
			label.setHorizontalAlignment(SwingConstants.LEFT);
		}

		return label;
	}
}
