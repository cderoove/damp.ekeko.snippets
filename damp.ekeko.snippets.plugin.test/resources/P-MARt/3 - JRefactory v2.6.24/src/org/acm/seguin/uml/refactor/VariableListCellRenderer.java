package org.acm.seguin.uml.refactor;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.acm.seguin.summary.VariableSummary;

class VariableListCellRenderer extends JLabel implements ListCellRenderer {
     public VariableListCellRenderer() {
         setOpaque(true);
     }
     public Component getListCellRendererComponent(
         JList list,
         Object value,
         int index,
         boolean isSelected,
         boolean cellHasFocus)
     {
     	if (value instanceof VariableSummary) {
     		VariableSummary varSummary = (VariableSummary) value;
     		setText(varSummary.getName() + " (" + varSummary.getType() + ")");
     	}
     	else {
         setText(value.toString());
     	}
         setBackground(isSelected ? Color.red : Color.white);
         setForeground(isSelected ? Color.white : Color.black);
         return this;
     }}
