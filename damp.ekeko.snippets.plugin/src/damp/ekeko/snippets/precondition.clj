(ns
  ^{:doc "Precondition check for operators."
    :author "Coen De Roover, Siltvani"}
   damp.ekeko.snippets.precondition
  (:refer-clojure :exclude [== type])
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko [logic :as el]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             ]))

;; Precondition Data
;; ----------------------


;(comment;
;
;  (defn
;  applicable-operators-for-transformation
;  "Returns list of applicable operator-id from given node."
;  [node]
;  (applicable-operators node (rep/operator-ids-for-transformation)))

;  )

;; Macro 
;; -------

(defmacro
  lstvalueraw?
  [x]
  `(instance? java.util.List ~x))

(defmacro
  type?
  [x]
  `(instance? org.eclipse.jdt.core.dom.Type ~x))


;; Precondition Function
;; -----------------------------

(defn
  listvalueraw
  "Relation of all list value-raw."
  [?raw]
  (cl/all 
    (el/succeeds (lstvalueraw? ?raw))))

(defn
  primitive-or-null-value
  "Relation of all value of ASTNode, but not listvalue."
  [?val]
  (cl/conde [(ast/value|primitive ?val)]
            [(ast/value|null ?val)]))

(defn
  is-ast?
  "Relation of all list ASTNode instances."
  [?node]
  (cl/all 
    (el/succeeds (astnode/ast? ?node))))

(defn
  is-type?
  "Relation of all list ASTNode instances (any subclass of Type)
   e.g :SimpleType, :PrimitiveType, etc."
  [?node]
  (cl/all
    (el/succeeds (type? ?node))))

(defn
  is-listmember?
  "Relation of all list ASTNode instances which is member of Nodelist."
  [?node]
  (cl/fresh [?key] 
    (cl/!= :CompilationUnit ?key)
    (ast/ast ?key ?node)
    (el/succeeds (astnode/property-descriptor-list? (.getLocationInParent ?node)))))

(defn
  is-variabledeclarationstatement?
  "Relation of all list ASTNode instances type :VariableDeclarationStatement."
  [?node]
  (cl/all 
    (ast/ast :VariableDeclarationStatement ?node)))

(defn
  is-ifstatement?
  "Relation of all list ASTNode instances type :IfStatement."
  [?node]
  (cl/all
    (ast/ast :IfStatement ?node)))

(defn
  is-assignmentstatement?
  "Relation of all list ASTNode instances type :ExpressionStatement with expression :Assignment."
  [?node]
  (cl/fresh [?exp] 
    (ast/ast :ExpressionStatement ?node)
    (ast/has :expression ?node ?exp)
    (ast/ast :Assignment ?exp)))

(defn
  is-methodinvocationstatement?
  "Relation of all list ASTNode instances type :ExpressionStatement with expression :MethodInvocation."
  [?node]
  (cl/fresh [?exp] 
    (ast/ast :ExpressionStatement ?node)
    (ast/has :expression ?node ?exp)
    (ast/ast :MethodInvocation ?exp)))

(defn
  is-methodinvocationexpression?
  "Relation of all list ASTNode instances type :MethodInvocation."
  [?node]
  (cl/all
    (ast/ast :MethodInvocation ?node)))

(defn
  is-methoddeclaration?
  "Relation of all list ASTNode instances type :MethodDeclaration."
  [?node]
  (cl/all
    (ast/ast :MethodDeclaration ?node)))

(defn
  is-simplename?
  "Relation of all list ASTNode instances type :SimpleName."
  [?node]
  (cl/all
    (ast/ast :SimpleName ?node)))

(defn
  is-variabledeclarationfragment?
  "Relation of all list ASTNode instances type :VariableDeclarationFragment."
  [?node]
  (cl/all
    (ast/ast :VariableDeclarationFragment ?node)))

(defn
  is-importlibrary?
  "Relation of all list ASTNode instances type :QualifiedName in :ImportDeclaration."
  [?node]
  (cl/fresh [?import]
         (ast/ast :QualifiedName ?node)
         (el/equals ?import (.getParent ?node))
         (ast/ast :ImportDeclaration ?import)))

(defn
  is-variabledeclaration?
  "Relation of all list ASTNode instances type :SimpleName as child of :VariableDeclarationFragment."
  [?node]
  (cl/fresh [?fragment]
         (ast/ast :SimpleName ?node)
         (el/equals ?fragment (.getParent ?node))
         (ast/ast :VariableDeclarationFragment ?fragment)))

(defn
  is-loop?
  "Relation of loop kind of statement."
  [?node]
  (cl/conde [(ast/ast :ForStatement ?node)]
            [(ast/ast :WhileStatement ?node)]
            [(ast/ast :DoStatement ?node)]))

(defn 
  epsilon
  []
  '())



(defn
  register-callbacks 
  []
  
  ;(set! (damp.ekeko.snippets.data.SnippetOperator/FN_APPLICABLE_OPERATORS_IN_CATEGORY) applicable-operators-in-category)
 ; (set! (damp.ekeko.snippets.data.SnippetOperator/FN_APPLICABLE_OPERATORS_FOR_TRANSFORMATION) applicable-operators-for-transformation)

  
  )


(register-callbacks)

