(ns 
  ^{:doc "Runtime predicates for snippet-driven querying."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.runtime
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko [logic :as el]]
            [damp.ekeko.jdt
             [basic :as basic]
             [reification :as reification]
             ;[soot :as soot]
             ])
  (:import 
    [org.eclipse.jdt.core.dom PrimitiveType Modifier$ModifierKeyword Assignment$Operator
     InfixExpression$Operator PrefixExpression$Operator PostfixExpression$Operator
     SimpleName VariableDeclarationFragment Type]))

(defn to-primitive-type-code
  [string]
  (PrimitiveType/toCode string)) 

(defn to-modifier-keyword
  [string]
  (Modifier$ModifierKeyword/toKeyword string)) 

(defn to-assignment-operator
  [string]
  (Assignment$Operator/toOperator string)) 

(defn to-infix-expression-operator
  [string]
  (InfixExpression$Operator/toOperator string)) 

(defn to-prefix-expression-operator
  [string]
  (PrefixExpression$Operator/toOperator string)) 

(defn to-postfix-expression-operator
  [string]
  (PostfixExpression$Operator/toOperator string)) 

(defn
  type-relaxmatch-subtype
   "Predicate to check type match ?ltype = ?type or ?ltype = subtype of ?type."
  [?keyword ?type ?ltype]
  (cl/conde [(el/equals ?type ?ltype)]
            [(cl/fresh [?itype ?iltype]
                       (reification/ast-type-type ?keyword ?type ?itype)
                       (reification/ast-type-type ?keyword ?ltype ?iltype)
                       (reification/type-super-type ?iltype ?itype))]))            
             
(defn
  assignment-relaxmatch-variable-declaration
  "Predicate to check relaxmatch of VariableDeclarationStatement (+ initializer) with Assignment."
  [?statement ?left ?right]
  (cl/conde [(cl/fresh [?assignment]
                       (reification/ast :ExpressionStatement ?statement)
                       (reification/has :expression ?statement ?assignment)                        
                       (reification/has :leftHandSide ?assignment ?left)
                       (reification/has :rightHandSide ?assignment ?right))]
            [(cl/fresh [?fragments ?fragmentsraw ?fragment ?fvalue ?avalue ?fname ?aname]
                       (reification/ast :VariableDeclarationStatement ?statement)
                       (reification/has :fragments ?statement ?fragments)
                       (reification/listvalue ?fragments)
                       (reification/value-raw ?fragments ?fragmentsraw)
                       (el/equals 1 (.size ?fragmentsraw))
                       (el/equals ?fragment (.get ?fragmentsraw 0))
                       (reification/ast :VariableDeclarationFragment ?fragment)
                       (reification/has :name ?fragment ?left)
                       (reification/has :initializer ?fragment ?right))]))

(defn
  ast-invocation-declaration
   "Relation between ASTNode invocation with it's declaration."
  [?inv ?dec]
  (cl/fresh [?k-inv ?k-dec ?b]
            (reification/ast-invocation-binding ?k-inv ?inv ?b)
            (reification/ast-declares-binding ?k-dec ?dec ?b)))

(defn
  ast-fieldaccess-samebinding
   "Relation between ASTNode var1 and var2 with the same resolveBinding."
  [?var1 ?var2]
  (cl/fresh [?k-var ?b]
            (reification/ast-fieldaccess-binding ?k-var ?var1 ?b)
            (reification/ast-fieldaccess-binding ?k-var ?var2 ?b)))

(defn
  ast-fieldaccess-declaration
   "Relation between ASTNode fieldaccess with it's declaration."
  [?var ?dec]
  (cl/fresh [?k-var ?k-dec ?b]
            (reification/ast-fieldaccess-binding ?k-var ?var ?b)
            (reification/ast-declares-binding ?k-dec ?dec ?b)))

(defn
  ast-variable-binding
  "Relation between an variable (SimpleName) instance ?ast,
   the keyword ?key representing its kind,
   and the IBinding ?binding for its type."
  [?key ?ast ?binding]
  (cl/all
    (reification/ast :SimpleName ?ast)
    (el/equals ?binding (.resolveBinding ^SimpleName ?ast))
    (cl/!= nil ?binding)
    (reification/ast ?key ?ast)))

(defn
  ast-variable-declaration-binding
  "Relation between a simple name ?ast (reside in variable declaration fragment),
   the keyword ?key representing its kind,
   and the IBinding ?binding for its type."
  [?key ?ast ?binding]
  (cl/fresh [?fragment]
    (reification/ast :SimpleName ?ast)
    (el/equals ?fragment (.getParent ?ast))
    (reification/ast :VariableDeclarationFragment ?fragment)
    (el/equals ?binding (.resolveBinding ^VariableDeclarationFragment ?fragment))
    (cl/!= nil ?binding)
    (reification/ast ?key ?ast)))

(defn
  ast-variable-samebinding
   "Relation between ASTNode var1 and var2 with the same resolveBinding."
  [?var1 ?var2]
  (cl/fresh [?k-var ?b]
            (ast-variable-binding ?k-var ?var1 ?b)
            (ast-variable-binding ?k-var ?var2 ?b)))

(defn
  ast-variable-declaration
   "Relation between ASTNode variable with it's declaration."
  [?var ?dec]
  (cl/fresh [?k-var ?k-dec ?b]
            (ast-variable-binding ?k-var ?var ?b)
            (ast-variable-declaration-binding ?k-dec ?dec ?b)))

;(defn
;  ast-may-alias
;   "Relation between ASTNode ast1 may alias ast2."
;  [?ast1 ?ast2]
;  (cl/fresh [?model]
;            (soot/ast-references-soot-model-may-alias ?ast1 ?ast2 ?model)))
  
(defn
  ast-variable-sameidentifier
   "Relation between ASTNode var1 and var2 with the same identifier."
  [?var1 ?var2]
  (cl/fresh [?id1 ?id2 ?value]
            (reification/ast :SimpleName ?var1)
            (reification/ast :SimpleName ?var2)
            (reification/has :identifier ?var1 ?id1) 
            (reification/has :identifier ?var2 ?id2)
            (reification/value-raw ?id1 ?value) 
            (reification/value-raw ?id2 ?value))) 

(defn
  ast-samekind-sameidentifier
   "Relation between ASTNode var1 and var2 with the same identifier."
  [?var1 ?var2]
      (cl/fresh [?key ?key-id ?var1-id ?var2-id  ?identifier]
                (reification/ast ?key ?var1)
                (reification/ast ?key ?var2)
                (reification/has ?key-id ?var1 ?var1-id)
                (reification/has ?key-id ?var2 ?var2-id)
                (reification/value-raw ?var1-id ?identifier)
                (reification/value-raw ?var2-id ?identifier)))

(defn
  ast-variable-type
   "Relation between ASTNode var with it's type."
  [?var ?type]
  (cl/conde [(cl/fresh [?dec ?stat ?frag]
                       (ast-variable-declaration ?var ?dec)
                       (reification/ast :SimpleName ?dec)
                       (el/equals ?frag (.getParent ?dec))
                       (reification/ast :VariableDeclarationFragment ?frag)
                       (el/equals ?stat (.getParent ?frag))
                       (reification/ast :VariableDeclarationStatement ?stat)
                       (reification/has :type ?stat ?type))]
            [(cl/fresh [?dec ?stat ?frag]
                       (ast-variable-declaration ?var ?dec)
                       (reification/ast :SimpleName ?dec)
                       (el/equals ?frag (.getParent ?dec))
                       (reification/ast :VariableDeclarationFragment ?frag)
                       (el/equals ?stat (.getParent ?frag))
                       (reification/ast :FieldDeclaration ?stat)
                       (reification/has :type ?stat ?type))]))
                                    
(defn 
  ast-type-qualifiednamestring
   "Relation between ASTNode var with it's qualified name (in string)."
  [?type ?string]
  (cl/fresh [?binding]
            (reification/ast :Type ?type)
            (el/equals ?binding (.resolveBinding ^Type ?type))
            (cl/!= nil ?binding)
            (el/equals ?string (.getQualifiedName ?binding))))

(defn 
  ast-type-qualifiednamecontain
   "Relation between ASTNode var with it's qualified name (contain the string)."
  [?type ?string]
  (cl/fresh [?qname]
            (ast-type-qualifiednamestring ?type ?qname)
            (el/succeeds (.contains ?qname ?string))))

(defn 
  ast-type-qualifiedname
   "Relation between ASTNode var with it's qualified name."
  [?type ?qname]
  (cl/fresh [?string] 
            (ast-type-qualifiednamestring ?type ?string)
            (reification/ast :QualifiedName ?qname)
            (el/succeeds (.contains ?string (str ?qname)))))
  
(defn
  ast-variable-typequalifiednamestring
   "Relation between ASTNode var with it's type qualified name (in string)."
  [?var ?string]
  (cl/fresh [?type]
            (ast-variable-type ?var ?type)
            (ast-type-qualifiednamecontain ?type ?string)))

(defn
  ast-variable-typequalifiedname
   "Relation between ASTNode var with it's type qualified name."
  [?var ?qname]
  (cl/fresh [?type]
            (ast-variable-type ?var ?type)
            (ast-type-qualifiedname ?type ?qname)))

