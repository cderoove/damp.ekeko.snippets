package damp.ekeko.snippets;

import java.util.Collection;

import clojure.lang.IFn;

public class BoundDirective {

	public static IFn FN_BOUNDDIRECTIVE_DESCRIPTION;
	public static IFn FN_BOUNDDIRECTIVE_STRING;

	public BoundDirective(Object directive, Collection<DirectiveOperandBinding> operandBindings) {
		this.directive = directive;
		this.operandBindings = operandBindings;
	}
	
	public Object directive;
	
	public Collection<DirectiveOperandBinding> operandBindings;
	
	public Collection<DirectiveOperandBinding> getOperandBindings() {
		return operandBindings;
	}
	
	public String getDescription() {
		return (String) FN_BOUNDDIRECTIVE_DESCRIPTION.invoke(this);
	}
	
	public String toString() {
		return (String) FN_BOUNDDIRECTIVE_STRING.invoke(this);
		
	}
	

}
