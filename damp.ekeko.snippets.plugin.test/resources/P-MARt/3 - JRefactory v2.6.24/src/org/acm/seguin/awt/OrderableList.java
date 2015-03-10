package org.acm.seguin.awt;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataListener;

public class OrderableList extends JPanel {
	private OrderableListModel olm;
	public OrderableList(Object[] data, ListCellRenderer render) {
		setLayout(null);

		olm = new OrderableListModel();
		olm.setData(data);
		JList list = new JList(olm);
		olm.setList(list);
		if (render != null)
			list.setCellRenderer(render);
		Dimension dim = list.getPreferredSize();
		list.setSize(dim);
		list.setLocation(10, 10);
		add(list);

		JButton upButton = new JButton("Up");
		upButton.addActionListener(new MoveItemAdapter(olm, list, -1));
		Dimension buttonSize = upButton.getPreferredSize();
		upButton.setSize(buttonSize);
		int top = Math.max(10, 10 + dim.height / 2 - 3 * buttonSize.height / 2);
		int bottom = top + buttonSize.height;
		upButton.setLocation(dim.width + 20, top);
		add(upButton);

		JButton downButton = new JButton("Down");
		downButton.addActionListener(new MoveItemAdapter(olm, list, 1));
		buttonSize = downButton.getPreferredSize();
		downButton.setSize(buttonSize);
		upButton.setSize(buttonSize);
		top = Math.max(bottom + 10, 10 + dim.height / 2 + buttonSize.height / 2);
		bottom = top + buttonSize.height;
		downButton.setLocation(dim.width + 20, top);
		add(downButton);

		Dimension panelSize = new Dimension(
			30 + dim.width + buttonSize.width,
			Math.max(10 + bottom, 20 + dim.height));

		setPreferredSize(panelSize);

		list.setLocation(10, (panelSize.height - dim.height) / 2);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Object[] data = {"one", "two", "three"};
		frame.getContentPane().add(new OrderableList(data, null));
		frame.pack();
		frame.show();
	}
	/**  Gets the correctly ordered data */
	public Object[] getData() { return olm.getData(); }
	public void addListDataListener(ListDataListener l) {
		olm.addListDataListener(l);
	}
	public void removeListDataListener(ListDataListener l) {
		olm.removeListDataListener(l);
	}
}
