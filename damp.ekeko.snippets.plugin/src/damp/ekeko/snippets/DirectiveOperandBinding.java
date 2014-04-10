package damp.ekeko.snippets;


public class DirectiveOperandBinding {

	public DirectiveOperandBinding(Object operand, Object value) {
		this.operand = operand;
		this.value = value;
	}
	
	public Object getOperand() {
		return operand;
	}

	public void setOperand(Object operand) {
		this.operand = operand;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}


	public Object operand;
	public Object value;


}
