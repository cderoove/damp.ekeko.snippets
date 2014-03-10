package damp.ekeko.snippets;

import java.util.Collection;
import java.util.List;

public class BoundDirective {
	
	public BoundDirective(Object directive, Collection<DirectiveOperandBinding> operandBindings) {
		this.directive = directive;
		this.operandBindings = operandBindings;
	}
	
	public Object directive;
	
	public Collection<DirectiveOperandBinding> operandBindings;
	

}
