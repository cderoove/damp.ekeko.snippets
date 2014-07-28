package damp.ekeko.jdt;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;


public class NaiveASTFlattener extends ASTVisitor {

		protected StringBuffer buffer;

		private int indent = 0;

		public NaiveASTFlattener() {
			this.buffer = new StringBuffer();
		}

		public String getResult() {
			return this.buffer.toString();
		}

		void printIndent() {
			for (int i = 0; i < this.indent; i++)
				this.buffer.append("  "); 
		}

		void printModifiers(List<?> ext) {
			for(Object name : ext) {
				ASTNode p = (ASTNode) name;
				p.accept(this);
				this.buffer.append(" "); 
			}
		}

		/**
		 * reference node helper function that is common to all
		 * the difference reference nodes.
		 * 
		 * @param typeArguments list of type arguments 
		 */
		private void visitReferenceTypeArguments(List<?> typeArguments) {
			this.buffer.append("::"); 
			if (!typeArguments.isEmpty()) {
				this.buffer.append('<');
				for (Iterator<?> it = typeArguments.iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(',');
					}
				}
				this.buffer.append('>');
			}
		}
		
		private void visitTypeAnnotations(AnnotatableType node) {
			visitAnnotationsList(node.annotations());
		}

		private void visitAnnotationsList(List<?> annotations) {
			for (Iterator<?> it = annotations.iterator(); it.hasNext(); ) {
				Annotation annotation = (Annotation) it.next();
				annotation.accept(this);
				this.buffer.append(' ');
			}
		}
		
		/**
		 * Resets this printer so that it can be used again.
		 */
		public void reset() {
			this.buffer.setLength(0);
		}


		public boolean visit(AnnotationTypeDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			printModifiers(node.modifiers());
			this.buffer.append("@interface "); 
			node.getName().accept(this);
			this.buffer.append(" {"); 
			for (Iterator<?> it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
				BodyDeclaration d = (BodyDeclaration) it.next();
				d.accept(this);
			}
			this.buffer.append("}\n"); 
			return false;
		}

		public boolean visit(AnnotationTypeMemberDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			printModifiers(node.modifiers());
			node.getType().accept(this);
			this.buffer.append(" "); 
			node.getName().accept(this);
			this.buffer.append("()"); 
			if (node.getDefault() != null) {
				this.buffer.append(" default "); 
				node.getDefault().accept(this);
			}
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(AnonymousClassDeclaration node) {
			this.buffer.append("{\n"); 
			this.indent++;
			for (Iterator<?> it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
				BodyDeclaration b = (BodyDeclaration) it.next();
				b.accept(this);
			}
			this.indent--;
			printIndent();
			this.buffer.append("}\n"); 
			return false;
		}

		public boolean visit(ArrayAccess node) {
			node.getArray().accept(this);
			this.buffer.append("["); 
			node.getIndex().accept(this);
			this.buffer.append("]"); 
			return false;
		}

		public boolean visit(ArrayCreation node) {
			this.buffer.append("new "); 
			ArrayType at = node.getType();
			int dims = at.getDimensions();
			Type elementType = at.getElementType();
			elementType.accept(this);
			for (Iterator<?> it = node.dimensions().iterator(); it.hasNext(); ) {
				this.buffer.append("["); 
				Expression e = (Expression) it.next();
				e.accept(this);
				this.buffer.append("]"); 
				dims--;
			}
			// add empty "[]" for each extra array dimension
			for (int i= 0; i < dims; i++) {
				this.buffer.append("[]"); 
			}
			if (node.getInitializer() != null) {
				node.getInitializer().accept(this);
			}
			return false;
		}

		public boolean visit(ArrayInitializer node) {
			this.buffer.append("{"); 
			for (Iterator<?> it = node.expressions().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append("}"); 
			return false;
		}

		public boolean visit(ArrayType node) {
			node.getElementType().accept(this);
			List<?> dimensions = node.dimensions();
			int size = dimensions.size();
			for (int i = 0; i < size; i++) {
				Dimension aDimension = (Dimension) dimensions.get(i);
				aDimension.accept(this);
			}
			return false;
		}

		public boolean visit(AssertStatement node) {
			printIndent();
			this.buffer.append("assert "); 
			node.getExpression().accept(this);
			if (node.getMessage() != null) {
				this.buffer.append(" : "); 
				node.getMessage().accept(this);
			}
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(Assignment node) {
			node.getLeftHandSide().accept(this);
			this.buffer.append(node.getOperator().toString());
			node.getRightHandSide().accept(this);
			return false;
		}

		public boolean visit(Block node) {
			this.buffer.append("{\n"); 
			this.indent++;
			for (Iterator<?> it = node.statements().iterator(); it.hasNext(); ) {
				Statement s = (Statement) it.next();
				s.accept(this);
			}
			this.indent--;
			printIndent();
			this.buffer.append("}\n"); 
			return false;
		}

		public boolean visit(BlockComment node) {
			printIndent();
			this.buffer.append("/* */"); 
			return false;
		}

		public boolean visit(BooleanLiteral node) {
			if (node.booleanValue() == true) {
				this.buffer.append("true"); 
			} else {
				this.buffer.append("false"); 
			}
			return false;
		}

		public boolean visit(BreakStatement node) {
			printIndent();
			this.buffer.append("break"); 
			if (node.getLabel() != null) {
				this.buffer.append(" "); 
				node.getLabel().accept(this);
			}
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(CastExpression node) {
			this.buffer.append("("); 
			node.getType().accept(this);
			this.buffer.append(")"); 
			node.getExpression().accept(this);
			return false;
		}

		public boolean visit(CatchClause node) {
			this.buffer.append("catch ("); 
			node.getException().accept(this);
			this.buffer.append(") "); 
			node.getBody().accept(this);
			return false;
		}

		public boolean visit(CharacterLiteral node) {
			this.buffer.append(node.getEscapedValue());
			return false;
		}

		public boolean visit(ClassInstanceCreation node) {
			if (node.getExpression() != null) {
				node.getExpression().accept(this);
				this.buffer.append("."); 
			}
			this.buffer.append("new "); 
				if (!node.typeArguments().isEmpty()) {
					this.buffer.append("<"); 
					for (Iterator<?> it = node.typeArguments().iterator(); it.hasNext(); ) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							this.buffer.append(","); 
						}
					}
					this.buffer.append(">"); 
				}
				node.getType().accept(this);
			
			this.buffer.append("("); 
			for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append(")"); 
			if (node.getAnonymousClassDeclaration() != null) {
				node.getAnonymousClassDeclaration().accept(this);
			}
			return false;
		}

		public boolean visit(CompilationUnit node) {
			if (node.getPackage() != null) {
				node.getPackage().accept(this);
			}
			for (Iterator<?> it = node.imports().iterator(); it.hasNext(); ) {
				ImportDeclaration d = (ImportDeclaration) it.next();
				d.accept(this);
			}
			for (Iterator<?> it = node.types().iterator(); it.hasNext(); ) {
				AbstractTypeDeclaration d = (AbstractTypeDeclaration) it.next();
				d.accept(this);
			}
			return false;
		}

		public boolean visit(ConditionalExpression node) {
			node.getExpression().accept(this);
			this.buffer.append(" ? "); 
			node.getThenExpression().accept(this);
			this.buffer.append(" : "); 
			node.getElseExpression().accept(this);
			return false;
		}

		public boolean visit(ConstructorInvocation node) {
			printIndent();
				if (!node.typeArguments().isEmpty()) {
					this.buffer.append("<"); 
					for (Iterator<?> it = node.typeArguments().iterator(); it.hasNext(); ) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							this.buffer.append(","); 
						}
					}
					this.buffer.append(">"); 
				}
			
			this.buffer.append("this("); 
			for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append(");\n"); 
			return false;
		}

		public boolean visit(ContinueStatement node) {
			printIndent();
			this.buffer.append("continue"); 
			if (node.getLabel() != null) {
				this.buffer.append(" "); 
				node.getLabel().accept(this);
			}
			this.buffer.append(";\n"); 
			return false;
		}
		
		public boolean visit(CreationReference node) {
			node.getType().accept(this);
			visitReferenceTypeArguments(node.typeArguments());
			this.buffer.append("new"); 
			return false;
		}

		public boolean visit(Dimension node) {
			List<?> annotations = node.annotations();
			if (annotations.size() > 0)
				this.buffer.append(' ');
			visitAnnotationsList(annotations);
			this.buffer.append("[]");  
			return false;
		}

		public boolean visit(DoStatement node) {
			printIndent();
			this.buffer.append("do "); 
			node.getBody().accept(this);
			this.buffer.append(" while ("); 
			node.getExpression().accept(this);
			this.buffer.append(");\n"); 
			return false;
		}

		public boolean visit(EmptyStatement node) {
			printIndent();
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(EnhancedForStatement node) {
			printIndent();
			this.buffer.append("for ("); 
			node.getParameter().accept(this);
			this.buffer.append(" : "); 
			node.getExpression().accept(this);
			this.buffer.append(") "); 
			node.getBody().accept(this);
			return false;
		}

		public boolean visit(EnumConstantDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			printModifiers(node.modifiers());
			node.getName().accept(this);
			if (!node.arguments().isEmpty()) {
				this.buffer.append("("); 
				for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
					Expression e = (Expression) it.next();
					e.accept(this);
					if (it.hasNext()) {
						this.buffer.append(","); 
					}
				}
				this.buffer.append(")"); 
			}
			if (node.getAnonymousClassDeclaration() != null) {
				node.getAnonymousClassDeclaration().accept(this);
			}
			return false;
		}

		public boolean visit(EnumDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			printModifiers(node.modifiers());
			this.buffer.append("enum "); 
			node.getName().accept(this);
			this.buffer.append(" "); 
			if (!node.superInterfaceTypes().isEmpty()) {
				this.buffer.append("implements "); 
				for (Iterator<?> it = node.superInterfaceTypes().iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(", "); 
					}
				}
				this.buffer.append(" "); 
			}
			this.buffer.append("{"); 
			for (Iterator<?> it = node.enumConstants().iterator(); it.hasNext(); ) {
				EnumConstantDeclaration d = (EnumConstantDeclaration) it.next();
				d.accept(this);
				// enum constant declarations do not include punctuation
				if (it.hasNext()) {
					// enum constant declarations are separated by commas
					this.buffer.append(", "); 
				}
			}
			if (!node.bodyDeclarations().isEmpty()) {
				this.buffer.append("; "); 
				for (Iterator<?> it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
					BodyDeclaration d = (BodyDeclaration) it.next();
					d.accept(this);
					// other body declarations include trailing punctuation
				}
			}
			this.buffer.append("}\n"); 
			return false;
		}

		public boolean visit(ExpressionMethodReference node) {
			node.getExpression().accept(this);
			visitReferenceTypeArguments(node.typeArguments());
			node.getName().accept(this);
			return false;
		}	

		public boolean visit(ExpressionStatement node) {
			printIndent();
			node.getExpression().accept(this);
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(FieldAccess node) {
			node.getExpression().accept(this);
			this.buffer.append("."); 
			node.getName().accept(this);
			return false;
		}

		public boolean visit(FieldDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			printModifiers(node.modifiers());
			node.getType().accept(this);
			this.buffer.append(" "); 
			for (Iterator<?> it = node.fragments().iterator(); it.hasNext(); ) {
				VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
				f.accept(this);
				if (it.hasNext()) {
					this.buffer.append(", "); 
				}
			}
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(ForStatement node) {
			printIndent();
			this.buffer.append("for ("); 
			for (Iterator<?> it = node.initializers().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) this.buffer.append(", "); 
			}
			this.buffer.append("; "); 
			if (node.getExpression() != null) {
				node.getExpression().accept(this);
			}
			this.buffer.append("; "); 
			for (Iterator<?> it = node.updaters().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) this.buffer.append(", "); 
			}
			this.buffer.append(") "); 
			node.getBody().accept(this);
			return false;
		}

		public boolean visit(IfStatement node) {
			printIndent();
			this.buffer.append("if ("); 
			node.getExpression().accept(this);
			this.buffer.append(") "); 
			node.getThenStatement().accept(this);
			if (node.getElseStatement() != null) {
				this.buffer.append(" else "); 
				node.getElseStatement().accept(this);
			}
			return false;
		}

		public boolean visit(ImportDeclaration node) {
			printIndent();
			this.buffer.append("import "); 
			if (node.isStatic()) {
				this.buffer.append("static "); 
			}
			node.getName().accept(this);
			if (node.isOnDemand()) {
				this.buffer.append(".*"); 
			}
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(InfixExpression node) {
			node.getLeftOperand().accept(this);
			this.buffer.append(' ');  // for cases like x= i - -1; or x= i++ + ++i;
			this.buffer.append(node.getOperator().toString());
			this.buffer.append(' ');
			node.getRightOperand().accept(this);
			final List<?> extendedOperands = node.extendedOperands();
			if (extendedOperands.size() != 0) {
				this.buffer.append(' ');
				for (Iterator<?> it = extendedOperands.iterator(); it.hasNext(); ) {
					this.buffer.append(node.getOperator().toString()).append(' ');
					Expression e = (Expression) it.next();
					e.accept(this);
				}
			}
			return false;
		}

		public boolean visit(Initializer node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printModifiers(node.modifiers());
			node.getBody().accept(this);
			return false;
		}

		public boolean visit(InstanceofExpression node) {
			node.getLeftOperand().accept(this);
			this.buffer.append(" instanceof "); 
			node.getRightOperand().accept(this);
			return false;
		}

		public boolean visit(IntersectionType node) {
			for (Iterator<?> it = node.types().iterator(); it.hasNext(); ) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					this.buffer.append(" & ");  
				}
			}
			return false;
		}

		
		public boolean visit(Javadoc node) {
			printIndent();
			this.buffer.append("/** "); 
			for (Iterator<?> it = node.tags().iterator(); it.hasNext(); ) {
				ASTNode e = (ASTNode) it.next();
				e.accept(this);
			}
			this.buffer.append("\n */\n"); 
			return false;
		}

		public boolean visit(LabeledStatement node) {
			printIndent();
			node.getLabel().accept(this);
			this.buffer.append(": "); 
			node.getBody().accept(this);
			return false;
		}

		
		public boolean visit(LambdaExpression node) {
			boolean hasParentheses = node.hasParentheses();
			if (hasParentheses)
				this.buffer.append('(');
			for (Iterator<?> it = node.parameters().iterator(); it.hasNext(); ) {
				VariableDeclaration v = (VariableDeclaration) it.next();
				v.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			if (hasParentheses)
				this.buffer.append(')');
			this.buffer.append(" -> ");  
			node.getBody().accept(this);
			return false;
		}

		public boolean visit(LineComment node) {
			this.buffer.append("//\n"); 
			return false;
		}

		public boolean visit(MarkerAnnotation node) {
			this.buffer.append("@"); 
			node.getTypeName().accept(this);
			return false;
		}
		
		public boolean visit(MemberRef node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
			}
			this.buffer.append("#"); 
			node.getName().accept(this);
			return false;
		}

		public boolean visit(MemberValuePair node) {
			node.getName().accept(this);
			this.buffer.append("="); 
			node.getValue().accept(this);
			return false;
		}

		public boolean visit(MethodDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printIndent();
			
				printModifiers(node.modifiers());
				if (!node.typeParameters().isEmpty()) {
					this.buffer.append("<"); 
					for (Iterator<?> it = node.typeParameters().iterator(); it.hasNext(); ) {
						TypeParameter t = (TypeParameter) it.next();
						t.accept(this);
						if (it.hasNext()) {
							this.buffer.append(","); 
						}
					}
					this.buffer.append(">"); 
				}
			if (!node.isConstructor()) {
					if (node.getReturnType2() != null) {
						node.getReturnType2().accept(this);
					} else {
						// methods really ought to have a return type
						this.buffer.append("void"); 
					}
				}
				this.buffer.append(" "); 
			
			node.getName().accept(this);
			this.buffer.append("("); 
			if (node.getAST().apiLevel() >= AST.JLS8) {
				Type receiverType = node.getReceiverType();
				if (receiverType != null) {
					receiverType.accept(this);
					this.buffer.append(' ');
					SimpleName qualifier = node.getReceiverQualifier();
					if (qualifier != null) {
						qualifier.accept(this);
						this.buffer.append('.');
					}
					this.buffer.append("this");  
					if (node.parameters().size() > 0) {
						this.buffer.append(',');
					}
				}
			}
			for (Iterator<?> it = node.parameters().iterator(); it.hasNext(); ) {
				SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
				v.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append(")"); 
			int size = node.getExtraDimensions();
			List<?> dimensions = node.extraDimensions();
			for (int i = 0; i < size; i++) {
				visit((Dimension) dimensions.get(i));
			}
			if (!node.thrownExceptionTypes().isEmpty()) {				
					this.buffer.append(" throws "); 
					for (Iterator<?> it = node.thrownExceptionTypes().iterator(); it.hasNext(); ) {
						Type n = (Type) it.next();
						n.accept(this);
						if (it.hasNext()) {
							this.buffer.append(", "); 
						}
					}	
					this.buffer.append(" "); 				
			}
			
			if (node.getBody() == null) {
				this.buffer.append(";\n"); 
			} else {
				node.getBody().accept(this);
			}
			
			return false;
		}

		/*
		 * @see ASTVisitor#visit(MethodInvocation)
		 */
		public boolean visit(MethodInvocation node) {
			if (node.getExpression() != null) {
				node.getExpression().accept(this);
				this.buffer.append("."); 
			}
				if (!node.typeArguments().isEmpty()) {
					this.buffer.append("<"); 
					for (Iterator<?> it = node.typeArguments().iterator(); it.hasNext(); ) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							this.buffer.append(","); 
						}
					}
					this.buffer.append(">"); 
				}
			
			node.getName().accept(this);
			this.buffer.append("("); 
			for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append(")"); 
			return false;
		}

		public boolean visit(MethodRef node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
			}
			this.buffer.append("#"); 
			node.getName().accept(this);
			this.buffer.append("("); 
			for (Iterator<?> it = node.parameters().iterator(); it.hasNext(); ) {
				MethodRefParameter e = (MethodRefParameter) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append(")"); 
			return false;
		}

		public boolean visit(MethodRefParameter node) {
			node.getType().accept(this);
				if (node.isVarargs()) {
					this.buffer.append("..."); 
				}
			if (node.getName() != null) {
				this.buffer.append(" "); 
				node.getName().accept(this);
			}
			return false;
		}

		public boolean visit(Modifier node) {
			this.buffer.append(node.getKeyword().toString());
			return false;
		}

		public boolean visit(NameQualifiedType node) {
			node.getQualifier().accept(this);
			this.buffer.append('.');
			visitTypeAnnotations(node);
			node.getName().accept(this);
			return false;
		}
		
		public boolean visit(NormalAnnotation node) {
			this.buffer.append("@"); 
			node.getTypeName().accept(this);
			this.buffer.append("("); 
			for (Iterator<?> it = node.values().iterator(); it.hasNext(); ) {
				MemberValuePair p = (MemberValuePair) it.next();
				p.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append(")"); 
			return false;
		}

		public boolean visit(NullLiteral node) {
			this.buffer.append("null"); 
			return false;
		}

		
		public boolean visit(NumberLiteral node) {
			this.buffer.append(node.getToken());
			return false;
		}

		public boolean visit(PackageDeclaration node) {
				if (node.getJavadoc() != null) {
					node.getJavadoc().accept(this);
				}
				for (Iterator<?> it = node.annotations().iterator(); it.hasNext(); ) {
					Annotation p = (Annotation) it.next();
					p.accept(this);
					this.buffer.append(" "); 
				}
			printIndent();
			this.buffer.append("package "); 
			node.getName().accept(this);
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(ParameterizedType node) {
			node.getType().accept(this);
			this.buffer.append("<"); 
			for (Iterator<?> it = node.typeArguments().iterator(); it.hasNext(); ) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append(">"); 
			return false;
		}

		public boolean visit(ParenthesizedExpression node) {
			this.buffer.append("("); 
			node.getExpression().accept(this);
			this.buffer.append(")"); 
			return false;
		}

		public boolean visit(PostfixExpression node) {
			node.getOperand().accept(this);
			this.buffer.append(node.getOperator().toString());
			return false;
		}

		public boolean visit(PrefixExpression node) {
			this.buffer.append(node.getOperator().toString());
			node.getOperand().accept(this);
			return false;
		}

		public boolean visit(PrimitiveType node) {
			visitTypeAnnotations(node);
			this.buffer.append(node.getPrimitiveTypeCode().toString());
			return false;
		}

		public boolean visit(QualifiedName node) {
			node.getQualifier().accept(this);
			this.buffer.append("."); 
			node.getName().accept(this);
			return false;
		}

		public boolean visit(QualifiedType node) {
			node.getQualifier().accept(this);
			this.buffer.append("."); 
			visitTypeAnnotations(node);
			node.getName().accept(this);
			return false;
		}

		public boolean visit(ReturnStatement node) {
			printIndent();
			this.buffer.append("return"); 
			if (node.getExpression() != null) {
				this.buffer.append(" "); 
				node.getExpression().accept(this);
			}
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(SimpleName node) {
			this.buffer.append(node.getIdentifier());
			return false;
		}

		public boolean visit(SimpleType node) {
			visitTypeAnnotations(node);
			node.getName().accept(this);
			return false;
		}

		public boolean visit(SingleMemberAnnotation node) {
			this.buffer.append("@"); 
			node.getTypeName().accept(this);
			this.buffer.append("("); 
			node.getValue().accept(this);
			this.buffer.append(")"); 
			return false;
		}

		public boolean visit(SingleVariableDeclaration node) {
			printIndent();
				printModifiers(node.modifiers());
			node.getType().accept(this);
				if (node.isVarargs()) {
						List<?> annotations = node.varargsAnnotations();
						if (annotations.size() > 0) {
							this.buffer.append(' ');
						}
						visitAnnotationsList(annotations);
					this.buffer.append("..."); 
				}
			this.buffer.append(" "); 
			node.getName().accept(this);
			int size = node.getExtraDimensions();
				List<?> dimensions = node.extraDimensions();
				for (int i = 0; i < size; i++) {
					visit((Dimension) dimensions.get(i));
				}
			
			if (node.getInitializer() != null) {
				this.buffer.append("="); 
				node.getInitializer().accept(this);
			}
			return false;
		}

		public boolean visit(StringLiteral node) {
			this.buffer.append(node.getEscapedValue());
			return false;
		}

		public boolean visit(SuperConstructorInvocation node) {
			printIndent();
			if (node.getExpression() != null) {
				node.getExpression().accept(this);
				this.buffer.append("."); 
			}
			if (!node.typeArguments().isEmpty()) {
					this.buffer.append("<"); 
					for (Iterator<?> it = node.typeArguments().iterator(); it.hasNext(); ) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							this.buffer.append(","); 
						}
					}
					this.buffer.append(">"); 
				}
			this.buffer.append("super("); 
			for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append(");\n"); 
			return false;
		}

		public boolean visit(SuperFieldAccess node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
				this.buffer.append("."); 
			}
			this.buffer.append("super."); 
			node.getName().accept(this);
			return false;
		}

		public boolean visit(SuperMethodInvocation node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
				this.buffer.append("."); 
			}
			this.buffer.append("super."); 
				if (!node.typeArguments().isEmpty()) {
					this.buffer.append("<"); 
					for (Iterator<?> it = node.typeArguments().iterator(); it.hasNext(); ) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							this.buffer.append(","); 
						}
					}
					this.buffer.append(">"); 
				}
			node.getName().accept(this);
			this.buffer.append("("); 
			for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.buffer.append(","); 
				}
			}
			this.buffer.append(")"); 
			return false;
		}

		public boolean visit(SuperMethodReference node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
				this.buffer.append('.');
			}
			this.buffer.append("super"); 
			visitReferenceTypeArguments(node.typeArguments());
			node.getName().accept(this);
			return false;
		}

		public boolean visit(SwitchCase node) {
			if (node.isDefault()) {
				this.buffer.append("default :\n"); 
			} else {
				this.buffer.append("case "); 
				node.getExpression().accept(this);
				this.buffer.append(":\n"); 
			}
			this.indent++; //decremented in visit(SwitchStatement)
			return false;
		}

		public boolean visit(SwitchStatement node) {
			this.buffer.append("switch ("); 
			node.getExpression().accept(this);
			this.buffer.append(") "); 
			this.buffer.append("{\n"); 
			this.indent++;
			for (Iterator<?> it = node.statements().iterator(); it.hasNext(); ) {
				Statement s = (Statement) it.next();
				s.accept(this);
				this.indent--; // incremented in visit(SwitchCase)
			}
			this.indent--;
			printIndent();
			this.buffer.append("}\n"); 
			return false;
		}

		public boolean visit(SynchronizedStatement node) {
			this.buffer.append("synchronized ("); 
			node.getExpression().accept(this);
			this.buffer.append(") "); 
			node.getBody().accept(this);
			return false;
		}

		public boolean visit(TagElement node) {
			if (node.isNested()) {
				// nested tags are always enclosed in braces
				this.buffer.append("{"); 
			} else {
				// top-level tags always begin on a new line
				this.buffer.append("\n * "); 
			}
			boolean previousRequiresWhiteSpace = false;
			if (node.getTagName() != null) {
				this.buffer.append(node.getTagName());
				previousRequiresWhiteSpace = true;
			}
			boolean previousRequiresNewLine = false;
			for (Iterator<?> it = node.fragments().iterator(); it.hasNext(); ) {
				ASTNode e = (ASTNode) it.next();
				// Name, MemberRef, MethodRef, and nested TagElement do not include white space.
				// TextElements don't always include whitespace, see <https://bugs.eclipse.org/206518>.
				boolean currentIncludesWhiteSpace = false;
				if (e instanceof TextElement) {
					String text = ((TextElement) e).getText();
					if (text.length() > 0 && ScannerHelper.isWhitespace(text.charAt(0))) {
						currentIncludesWhiteSpace = true; // workaround for https://bugs.eclipse.org/403735
					}
				}
				if (previousRequiresNewLine && currentIncludesWhiteSpace) {
					this.buffer.append("\n * "); 
				}
				previousRequiresNewLine = currentIncludesWhiteSpace;
				// add space if required to separate
				if (previousRequiresWhiteSpace && !currentIncludesWhiteSpace) {
					this.buffer.append(" ");  
				}
				e.accept(this);
				previousRequiresWhiteSpace = !currentIncludesWhiteSpace && !(e instanceof TagElement);
			}
			if (node.isNested()) {
				this.buffer.append("}"); 
			}
			return false;
		}

		public boolean visit(TextElement node) {
			this.buffer.append(node.getText());
			return false;
		}

		public boolean visit(ThisExpression node) {
			if (node.getQualifier() != null) {
				node.getQualifier().accept(this);
				this.buffer.append("."); 
			}
			this.buffer.append("this"); 
			return false;
		}

		public boolean visit(ThrowStatement node) {
			printIndent();
			this.buffer.append("throw "); 
			node.getExpression().accept(this);
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(TryStatement node) {
			printIndent();
			this.buffer.append("try "); 
			List<?> resources = node.resources();
			if (!resources.isEmpty()) {
				this.buffer.append('(');
				for (Iterator<?> it = resources.iterator(); it.hasNext(); ) {
					VariableDeclarationExpression variable = (VariableDeclarationExpression) it.next();
					variable.accept(this);
					if (it.hasNext()) {
						this.buffer.append(';');
					}
				}
				this.buffer.append(')');
			}
			node.getBody().accept(this);
			this.buffer.append(" "); 
			for (Iterator<?> it = node.catchClauses().iterator(); it.hasNext(); ) {
				CatchClause cc = (CatchClause) it.next();
				cc.accept(this);
			}
			if (node.getFinally() != null) {
				this.buffer.append(" finally "); 
				node.getFinally().accept(this);
			}
			return false;
		}

		public boolean visit(TypeDeclaration node) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			printModifiers(node.modifiers());
			this.buffer.append(node.isInterface() ? "interface " : "class ");//$NON-NLS-2$ 
			node.getName().accept(this);
			if (!node.typeParameters().isEmpty()) {
				this.buffer.append("<"); 
					for (Iterator<?> it = node.typeParameters().iterator(); it.hasNext(); ) {
						TypeParameter t = (TypeParameter) it.next();
							t.accept(this);
						if (	it.hasNext()) {
							this.buffer.append(","); 
						}
					}
					this.buffer.append(">"); 
				}
			
			this.buffer.append(" "); 
				if (node.getSuperclassType() != null) {
					this.buffer.append("extends "); 
					node.getSuperclassType().accept(this);
					this.buffer.append(" "); 
				}
				if (!node.superInterfaceTypes().isEmpty()) {
					this.buffer.append(node.isInterface() ? "extends " : "implements ");//$NON-NLS-2$ 
					for (Iterator<?> it = node.superInterfaceTypes().iterator(); it.hasNext(); ) {
						Type t = (Type) it.next();
						t.accept(this);
						if (it.hasNext()) {
							this.buffer.append(", "); 
						}
					}
					this.buffer.append(" "); 
				}
			this.buffer.append("{\n"); 
			this.indent++;
			for (Iterator<?> it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
				BodyDeclaration d = (BodyDeclaration) it.next();
				d.accept(this);
			}
			this.indent--;
			printIndent();
			this.buffer.append("}\n"); 
			return false;
		}

		public boolean visit(TypeDeclarationStatement node) {
			node.getDeclaration().accept(this);
			return false;
		}

		public boolean visit(TypeLiteral node) {
			node.getType().accept(this);
			this.buffer.append(".class"); 
			return false;
		}

		public boolean visit(TypeMethodReference node) {
			node.getType().accept(this);
			visitReferenceTypeArguments(node.typeArguments());
			node.getName().accept(this);
			return false;
		}

		public boolean visit(TypeParameter node) {
			printModifiers(node.modifiers());
			node.getName().accept(this);
			if (!node.typeBounds().isEmpty()) {
				this.buffer.append(" extends "); 
				for (Iterator<?> it = node.typeBounds().iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(" & "); 
					}
				}
			}
			return false;
		}

		public boolean visit(UnionType node) {
			for (Iterator<?> it = node.types().iterator(); it.hasNext(); ) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					this.buffer.append('|');
				}
			}
			return false;
		}

		public boolean visit(VariableDeclarationExpression node) {
			printModifiers(node.modifiers());
			node.getType().accept(this);
			this.buffer.append(" "); 
			for (Iterator<?> it = node.fragments().iterator(); it.hasNext(); ) {
				VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
				f.accept(this);
				if (it.hasNext()) {
					this.buffer.append(", "); 
				}
			}
			return false;
		}

		public boolean visit(VariableDeclarationFragment node) {
			node.getName().accept(this);
			int size = node.getExtraDimensions();
			List<?> dimensions = node.extraDimensions();
				for (int i = 0; i < size; i++) {
					visit((Dimension) dimensions.get(i));
				}	
			if (node.getInitializer() != null) {
				this.buffer.append("="); 
				node.getInitializer().accept(this);
			}
			return false;
		}

		public boolean visit(VariableDeclarationStatement node) {
			printIndent();
			printModifiers(node.modifiers());
			node.getType().accept(this);
			this.buffer.append(" "); 
			for (Iterator<?> it = node.fragments().iterator(); it.hasNext(); ) {
				VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
				f.accept(this);
				if (it.hasNext()) {
					this.buffer.append(", "); 
				}
			}
			this.buffer.append(";\n"); 
			return false;
		}

		public boolean visit(WhileStatement node) {
			printIndent();
			this.buffer.append("while ("); 
			node.getExpression().accept(this);
			this.buffer.append(") "); 
			node.getBody().accept(this);
			return false;
		}

		public boolean visit(WildcardType node) {
			visitTypeAnnotations(node);
			this.buffer.append("?"); 
			Type bound = node.getBound();
			if (bound != null) {
				if (node.isUpperBound()) {
					this.buffer.append(" extends "); 
				} else {
					this.buffer.append(" super "); 
				}
				bound.accept(this);
			}
			return false;
		}

	}

	
	
