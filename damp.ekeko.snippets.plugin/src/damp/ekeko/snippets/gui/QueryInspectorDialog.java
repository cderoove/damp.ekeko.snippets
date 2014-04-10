package damp.ekeko.snippets.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class QueryInspectorDialog extends Dialog {
	private String title;
	private String infoText;
	private String infoLabel;
	private Object[] args;
	private String[] inputs;
	
	private StyledText txtInfo;
	private Text[] txtInputs;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 * @wbp.parser.constructor
	 */
	public QueryInspectorDialog(Shell parentShell, String title, String infoText, String infoLabel, Object[] args, String[] inputs) {
		super(parentShell);
		setShellStyle(SWT.RESIZE);
		this.title = title;
		this.infoText = infoText;
		this.infoLabel = infoLabel;
		this.args = args;
		this.inputs = inputs;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 2;
		
		Label lblTitle = new Label(container, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		lblTitle.setText(title);
		
		txtInfo = new StyledText(container, SWT.BORDER);
		txtInfo.setEditable(false);
		GridData gd_txtInfo = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_txtInfo.heightHint = 173;
		txtInfo.setLayoutData(gd_txtInfo);
		txtInfo.setText(infoText);
		
		if (args != null) {
			Label lblTitle2 = new Label(container, SWT.NONE);
			lblTitle2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
			lblTitle2.setText("Corresponding query:");
		
			txtInputs = new Text[args.length];
			
			for (int i = 0; i < args.length; i++) {
				Label lblNewLabel = new Label(container, SWT.NONE);
				GridData gd_lblNewLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
				gd_lblNewLabel.heightHint = args[i].toString().split("\n").length * 15;
				lblNewLabel.setLayoutData(gd_lblNewLabel);
				lblNewLabel.setText(args[i].toString());
				
				txtInputs[i] = new Text(container, SWT.BORDER);
				GridData gd_txtInputs = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				gd_txtInputs.heightHint = args[i].toString().split("\n").length * 15;
				txtInputs[i].setLayoutData(gd_txtInputs);
				if (inputs != null && inputs[i] != null)
					txtInputs[i].setText(inputs[i]);
			}
		}

		Label lblInfo = new Label(container, SWT.NONE);
		GridData gd_lblInfo = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		gd_lblInfo.heightHint = 45;
		lblInfo.setLayoutData(gd_lblInfo);
		lblInfo.setText(infoLabel);
		
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, 
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(481, 498);
	}
	
	@Override
	protected void okPressed() {
		if (txtInputs != null) {
			inputs = new String[txtInputs.length];
			for (int i = 0; i < txtInputs.length; i++) {
				inputs[i] = txtInputs[i].getText();			
			}
		}
		infoText = txtInfo.getText();
	    super.okPressed();	
	}
	
	public String[] getInputs() {
		return inputs;
	}

	public String getInfo() {
		return infoText;
	}
}
