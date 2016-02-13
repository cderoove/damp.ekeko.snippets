(ns 
  ^{:doc "Representation for operators and their operands."
    :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.operatorsrep
  (:refer-clojure :exclude [== type])
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko 
             [logic :as el]]
            [damp.ekeko.jdt
             [ast :as ast]
             [astnode :as astnode]])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [operators :as operators]
             [parsing :as parsing]
             [matching :as matching]
             [snippetgroup :as snippetgroup]
             [util :as util]])
  (:import 
    [java.util Map]
    [damp.ekeko JavaProjectModel]
    [damp.ekeko EkekoModel]
    [edu.cmu.cs.crystal.cfg.eclipse EclipseCFG EclipseCFGEdge EclipseCFGNode]
    [org.eclipse.core.runtime IProgressMonitor]
    [org.eclipse.jdt.core IJavaElement ITypeHierarchy IType IPackageFragment IClassFile ICompilationUnit
     IJavaProject WorkingCopyOwner IMethod]
    [org.eclipse.jdt.core.dom Expression IVariableBinding ASTParser AST IBinding Type TypeDeclaration 
     QualifiedName SimpleName ITypeBinding MethodDeclaration 
     MethodInvocation ClassInstanceCreation SuperConstructorInvocation SuperMethodInvocation
     SuperFieldAccess FieldAccess ConstructorInvocation ASTNode ASTNode$NodeList CompilationUnit
     Annotation IAnnotationBinding TypeLiteral Statement]))

;; Operator information

;scopes are necessary to communicate selection dialog type to gui
(def opscope-subject :subject) ;operand scope denoting the subject of the operator
(def opscope-variable :variable) ;operand/operator scope for logic variables

(def opscope-nodeclasskeyw :nodeclasskeyw) ;operand scope for ekeko keyword for node class 

(def opscope-directive :directive) ; operand scope denoting the id of a directive
(def opscope-string :string)
(def opscope-subjectlistidx :subjectlistidx) ;0 till size exclusive
(def opscope-incsubjectlistidx :incsubjectlistidx);0 till size inclusive
(def opscope-multiplicity :multiplicity) ;match multiplicity (int, *, +)


(defn-
  ekekokeyword-owningproperty?
  [value keyword]
  (when-let [ownerprop (astnode/owner-property value)]
   (and (= keyword (astnode/ekeko-keyword-for-property-descriptor ownerprop)))))

(defn has-directives?
  "Returns true if the given node has one of the given directives
   @param directives list of directive names to look for"
  [snippet node directives]
  (let [bds (snippet/snippet-bounddirectives-for-node snippet node)]
    (some (fn [bd]
            (.contains directives 
              (damp.ekeko.snippets.directives/directive-name (snippet/bounddirective-directive bd))))
          bds)))

(defn
  applicability|always
  [snippetgroup snippet value] 
  true)

(defn applicability|notprotected
  [snippetgroup snippet value] 
  (not (matching/node-protected? snippet value)))

(defn 
  applicability|node
  [snippetgroup snippet value]
  (astnode/ast? value))

(defn
  applicability|name
  [snippetgroup snippet value]  
  (and 
    (applicability|node snippetgroup snippet value)
    (some #{(astnode/ekeko-keyword-for-class-of value)} [:SimpleName :QualifiedName])))

(defn
  applicability|fieldaccess
  "Value is a potential field acesss, not necessarily the case."
  [snippetgroup snippet value]
  (and 
    (applicability|node snippetgroup snippet value)
    (some #{(astnode/ekeko-keyword-for-class-of value)} 
          [:FieldAccess :SuperFieldAccess :SimpleName :QualifiedName])))

(defn 
  applicability|methoddeclaration
  [snippetgroup snippet value]
  (and
    (applicability|node snippetgroup snippet value)
    (= :MethodDeclaration (astnode/ekeko-keyword-for-class-of value))
    (not (.isConstructor value))))

(defn
  applicability|methoddeclarationorname
  [snippetgroup snippet value]
  (or 
    (applicability|methoddeclaration snippetgroup snippet value)
    (and
      (applicability|name snippetgroup snippet value)
      (applicability|methoddeclaration snippetgroup snippet (snippet/snippet-node-parent|conceptually snippet value))
      (ekekokeyword-owningproperty? value :name))))

(defn 
  applicability|methodinvocation
  [snippetgroup snippet value]
  (and (applicability|node snippetgroup snippet value)
       (some #{(astnode/ekeko-keyword-for-class-of value)}
             [:MethodInvocation :SuperMethodInvocation])))

(defn 
  applicability|methodinvocationorname
  [snippetgroup snippet value]
  (or 
    (applicability|methodinvocation snippetgroup snippet value)
    (and
      (applicability|name snippetgroup snippet value)
      (applicability|methodinvocation snippetgroup snippet (snippet/snippet-node-parent|conceptually snippet value))
      (ekekokeyword-owningproperty? value :name))))


(defn
  applicability|constructorinvocation 
  [snippetgroup snippet value]
  (and (applicability|node snippetgroup snippet value)
       (some #{(astnode/ekeko-keyword-for-class-of value)}
             [:ClassInstanceCreation :ConstructorInvocation :SuperConstructorInvocation])))

(defn
  applicability|constructor
  [snippetgroup snippet value]
  (and 
    (applicability|node snippetgroup snippet value)
    (= :MethodDeclaration (astnode/ekeko-keyword-for-class-of value))
    (.isConstructor value)))

(defn 
  applicability|constructororname
  [snippetgroup snippet value]
  (or 
    (applicability|constructor snippetgroup snippet value)
    (and
      (applicability|name snippetgroup snippet value)
      (applicability|constructor snippetgroup snippet (snippet/snippet-node-parent|conceptually snippet value))
      (ekekokeyword-owningproperty? value :name))))


(defn
  applicability|nonroot
  [snippetgroup snippet value]
  (not= (snippet/snippet-root snippet) value))

(defn 
  applicability|node|nonroot
  [snippetgroup snippet value]
  (and 
    (applicability|node snippetgroup snippet value)
    (applicability|nonroot snippetgroup snippet value)))

(defn
  applicability|expr
  [snippetgroup snippet value]
  (and 
    (applicability|node snippetgroup snippet value)
    (astnode/expression? value)))

(defn
  applicability|expr-in-body
  [snippetgroup snippet value]
  (and 
    (applicability|expr snippetgroup snippet value)
    ; One of the ancestors must be a block (which is part of a method body)
    (loop [node value]
      (let [parent (snippet/snippet-node-parent|conceptually snippet node)]
        (cond
          (nil? parent) false
          (astnode/block? parent) true
          :else (recur parent))
        )
      )))

(defn
  applicability|expressionstmt
  [snippetgroup snippet value]
  (and 
    (applicability|node snippetgroup snippet value)
    (astnode/expressionstmt? value)))

(defn applicability|replace-parent
  "The replace-parent operator can only be applied to Expression nodes, whose parent is an Expression as well."
  [snippetgroup snippet value]
  (and
    (applicability|expr snippetgroup snippet value)
    (applicability|nonroot snippetgroup snippet value)
    (astnode/expression? (snippet/snippet-node-parent|conceptually snippet value))
    (applicability|nonroot snippetgroup snippet (snippet/snippet-node-parent|conceptually snippet value))))

(defn applicability|stmt
  "The replace-parent-stmt operator can only be applied to Statements, whose grandparent isn't a MethodDeclaration."
  [snippetgroup snippet value]
  (and
    (applicability|node snippetgroup snippet value)
    (astnode/statement? value)))

(defn applicability|non-block-stmt
  [snippetgroup snippet value]
  (and 
    (applicability|stmt snippetgroup snippet value)
    (not (astnode/block? value))))

(defn applicability|replace-parent-stmt
  "The replace-parent-stmt operator can only be applied to Statements, whose grandparent isn't a MethodDeclaration."
  [snippetgroup snippet value]
  (and
    (applicability|stmt snippetgroup snippet value)
    
    (not (instance? MethodDeclaration ; go 3 parents up: value > list > Block > X
                    (snippet/snippet-node-ancestor|conceptually snippet value 3)))))

(defn 
  applicability|lst
  [snippetgroup snippet value]
  (astnode/lstvalue? value))

(defn 
  applicability|lst-nowildcard
  [snippetgroup snippet value]
  (and
    (applicability|lst snippetgroup snippet value)
    (not (has-directives? snippet value ["replaced-by-wildcard"]))))

(defn 
  applicability|block-or-nil
  [snippetgroup snippet value]
  (or 
    (astnode/nilvalue? value)
    (astnode/block? value)))

(defn
  applicability|simplepropertyvalue
  [snippetgroup snippet value] 
  (and 
    (not (nil? value))
    (when-let [property (astnode/owner-property value)]
      (astnode/property-descriptor-simple? property))))

(defn 
  applicability|lstelement
  [snippetgroup snippet value]
  (and 
    (not (nil? value))
    (not (astnode/lstvalue? value))
    (when-let [property (astnode/owner-property value)]
      (astnode/property-descriptor-list? property))))

(defn 
  applicability|lstelement|nonroot
  [snippetgroup snippet value]
  (and 
    (applicability|nonroot snippetgroup snippet value)
    (applicability|lstelement snippetgroup snippet value)))

(defn
  applicability|lstelement|node
  [snippetgroup snippet value]
  (and (applicability|node snippetgroup snippet value)
       (applicability|lstelement snippetgroup snippet value)))

(defn
  applicability|deleteable
  [snippetgroup snippet value]
  (and
    (astnode/ast? value) 
    (applicability|nonroot snippetgroup snippet value)
    (when-let [ownerprop (astnode/owner-property value)]
      (or 
        (astnode/property-descriptor-list? ownerprop)
        (not (.isMandatory ownerprop))))
    (not (matching/node-protected? snippet value))))

(defn
  applicability|multiplicity
  [snippetgroup snippet value]
  (matching/snippet-value-regexp-element snippet value))


(defn
  applicability|wildcard
  "Verifies that value is not a list with regexp matching enabled."
  [snippetgroup snippet value]
  (and
    (applicability|nonroot snippetgroup snippet value)
    (not (and
           (astnode/lstvalue? value)
           (matching/snippet-list-regexp? snippet value)))
;    (not (has-directives? snippet value ["match|set"])) ; No point in adding match|set .. plus it causes timeouts
    (not (matching/node-protected? snippet value))))

(defn
  applicability|child+*
  [snippetgroup snippet value]
  (and
    (applicability|nonroot snippetgroup snippet value)
    (not (snippet/snippet-value-primitive? snippet value)) ; Causes stack overflows for some reason?
    (complement applicability|lst)))


(defn
  applicability|regexplst
  "Verifies that value is a list, but has not been replaced by a wildcard."
  [snippetgroup snippet value]
  (and 
    (astnode/lstvalue? value)
    (not (matching/snippet-node-replaced-by-wilcard? snippet value))))

(defn
  applicability|regexpcfglst
  "As applicability|regexplst, but also verifies that lst is a statements list."
  [snippetgroup snippet value]
  (and 
    (applicability|regexplst snippetgroup snippet value)
    (ekekokeyword-owningproperty? value :statements)))
    
(defn
  applicability|receiver
  [snippetgroup snippet value]
  (and
    (applicability|node snippetgroup snippet value)
    (applicability|methodinvocation snippetgroup snippet (snippet/snippet-node-parent|conceptually snippet value))
    (ekekokeyword-owningproperty? value :expression)))      
         
(defn
  applicability|vardeclaration
  [snippetgroup snippet value]  
  (and
    (applicability|node snippetgroup snippet value)
    (or
      (some #{(astnode/ekeko-keyword-for-class-of value)}
            [:VariableDeclarationFragment :SingleVariableDeclaration])
      (and
        (applicability|name snippetgroup snippet value)
        (applicability|vardeclaration snippetgroup snippet (snippet/snippet-node-parent|conceptually snippet value))
        (ekekokeyword-owningproperty? value :name)
        ))))

  
(defn
  applicability|nullvalue
  [snippetgroup snippet node]
  (snippet/snippet-value-null? snippet node))
    
(defn
  applicability|absentvalue-classkeywords
  [snippetgroup snippet nullvalue classkeywords]
  (and 
    (applicability|nullvalue snippetgroup snippet nullvalue)
    (let [ownerproperty 
          (astnode/owner-property nullvalue)
          valueclass
          (astnode/property-descriptor-child-node-class ownerproperty)]
      (some #{(astnode/ekeko-keyword-for-class valueclass)} classkeywords))))      
      
      
(defn
  applicability|node-classkeywords
  [snippetgroup snippet node classkeywords]
  (and 
    (applicability|node snippetgroup snippet node)
    (some #{(astnode/ekeko-keyword-for-class-of node)} 
          classkeywords)))

(defn applicability|typedecl-bodydeclarations
  [snippetgroup snippet value]
  (and
    (applicability|lst snippetgroup snippet value)
    (let [parent (snippet/snippet-node-parent|conceptually snippet value)
          typeclasskeywords [:TypeDeclaration]]
      (or 
        (applicability|node-classkeywords snippetgroup snippet parent typeclasskeywords)
        (applicability|absentvalue-classkeywords snippetgroup snippet parent typeclasskeywords)))))

(defn
  applicability|type
  [snippetgroup snippet node]
  (let [typeclasskeywords 
        [:SimpleName :ArrayType :ParameterizedType :PrimitiveType :QualifiedType :SimpleType :UnionType :WildcardType :TypeParameter :Type]]
    (and
      (applicability|node snippetgroup snippet node)
      (if (= :SimpleName (astnode/ekeko-keyword-for-class-of node))
        (let [parent (snippet/snippet-node-parent|conceptually snippet node)]
          (if (not (nil? parent))
            (= :TypeDeclaration (astnode/ekeko-keyword-for-class-of parent))
            false
            ))
        true)
      (or 
       (applicability|node-classkeywords snippetgroup snippet node typeclasskeywords)
       (applicability|absentvalue-classkeywords snippetgroup snippet node  typeclasskeywords)))))
  
(defn
  applicability|typename
  [snippetgroup snippet node]
  (and
    (applicability|node snippetgroup snippet node)
    (applicability|name snippetgroup snippet node)
    ;(if-let [namebinding (snippet/snippet-node-resolvedbinding snippet node)]
    ;  (astnode/binding-type? namebinding))))
    (applicability|type snippetgroup snippet (snippet/snippet-node-parent|conceptually snippet node))
    (ekekokeyword-owningproperty? node :name)))
 
(defn
  applicability|typeortypename
  [snippetgroup snippet node]
  (or 
    (applicability|type snippetgroup snippet node)
    (applicability|typename snippetgroup snippet node)))
    
(defn
  applicability|qualifiedtypeorname
  [snippetgroup snippet value]
  (and 
    (applicability|node snippetgroup snippet value)
    (let [keyw (astnode/ekeko-keyword-for-class-of value)]
      (or (= :QualifiedType keyw)
          (and (= :QualifiedName keyw)
               (applicability|typename snippetgroup snippet value))))))
    
(defn
  validity|always
  [snippetgroup snippet subject operandvalue]
  true)

(defn
  validity|string
  [snippetgroup snippet subject operandvalue]
  (string? operandvalue))

(defn
  validity|integer
  [snippetgroup snippet subject operandvalue]
  (try (do
         (java.lang.Integer/parseInt operandvalue)
         true)
    (catch Exception e false))
  (string? operandvalue))

(defn
  validity|subject
  [snippetgroup snippet subject operandvalue]
  (= operandvalue subject))

(defn
  validity|node
  [snippetgroup snippet value operandvalue]
  (astnode/ast? operandvalue))

(defn
  validity|variable
  [snippetgroup snippet value operandvalue]
  (and
    (symbol operandvalue)
    (= (first (str operandvalue)) \?)))

; TODO unfinished; tries to infer the type of a metavariable..
;(defn
;  validity|variable-typed
;  [snippetgroup snippet value operandvalue expected-type]
;  (let
;    [; Like the map function, but removes nil results..
;     map-trim (fn [f coll]
;                (remove nil? (map f coll)))
;      
;     
;     inferred-type
;     (map-trim
;       
;       (fn [node]
;         (let [bds 
;               (snippet/snippet-bounddirectives-for-node snippet node)
;               
;               inferred-type 
;               (map-trim
;                 (fn [bd]
;                   (if (= (directives/directive-name (bounddirective-directive bd)) "equals")
;                     (let [uservar 
;                           (directives/directiveoperandbinding-value
;                             (first
;                               (directives/bounddirective-operandbindings bounddirective)))]
;                       (if (= operand-value uservar)
;                         (astnode/value-unwrapped node)
;                         
;                         
;                         ))))
;                 bds)
;               
;               ]
;           
;           ))
;       (snippet/snippet-nodes snippet))])
;  
;  
;  (and 
;    (string? operandvalue)
;    (= (first operandvalue) \?)))

(defn
  instance-of-classkeyword-assignable-to-property?
  [classkeyword propertydescriptor]
  (and 
    (keyword? classkeyword)
    (let [clazz 
          (astnode/class-for-ekeko-keyword classkeyword)
          propertyvaluetype
          (cond 
            (astnode/property-descriptor-child? propertydescriptor)
            (astnode/property-descriptor-child-node-class propertydescriptor)
            (astnode/property-descriptor-list? propertydescriptor)
            (astnode/property-descriptor-element-node-class propertydescriptor))]
      (.isAssignableFrom propertyvaluetype clazz))))

(defn
  validity|subjectowninglisttype
  [snippetgroup snippet value classkeyword]
  (let [propertydescriptor (astnode/owner-property value)]
    (and propertydescriptor
         (instance-of-classkeyword-assignable-to-property? classkeyword propertydescriptor))))

(def
  validity|subjectlisttype
  validity|subjectowninglisttype)


(defn
  validity|subjectlistidx
  [snippetgroup snippet value operandvalue]
  (and 
    (integer? operandvalue)
    (let [lst-raw (astnode/value-unwrapped value)]
      (and (>= operandvalue 0)
           (< operandvalue (.size lst-raw))))))

(defn
  validity|incsubjectlistidx
  [snippetgroup snippet value operandvalue]
  (and 
    (integer? operandvalue)
    (let [lst-raw (astnode/value-unwrapped value)]
      (and (>= operandvalue 0)
           (<= operandvalue (.size lst-raw))))))

(defn validity|directivename
  [snippetgroup snippet value operandvalue]  
  (some (fn [dir]
          (= operandvalue (damp.ekeko.snippets.directives/directive-name dir)))
        (matching/registered-directives)))

(def
  validity|subjectownerpropertytype 
  validity|subjectowninglisttype)

(defn
  validity|multiplicity
  [snippetgroup snippet value operandvalue]
  (or
    (= "1" operandvalue)
    (= "+" operandvalue)
    (= "*" operandvalue)))

(defrecord 
  Operator
  [id operator category name scope validation description operands appliestogroup])

(defn
  operator-id
  [operator]
  (:id operator))

(defn 
  operator-operator 
  "Returns function implementing given operator."
  [operator]
  (:operator operator))

(defn 
  operator-category 
  "Returns category of given operator."
  [operator]
  (:category operator))

(defn 
  operator-name 
  "Returns name of given operator."
  [operator]
  (:name operator))

(defn 
  operator-scope 
  "Returns scope of given operator."
  [operator]
  (:scope operator))

(defn 
  operator-validation 
  "Returns validation function to be used on implicit operand."
  [operator]
  (:validation operator))

(defn 
  operator-description 
  "Returns description of given operator."
  [operator]
  (:description operator))

(defn 
  operator-operands 
  "Returns operands for given operator."
  [operator]
  (:operands operator))

(defn
  operator-appliestogroup?
  "Returns whether the operator takes a snippetgroup or a single snippet as its first argument."
  [operator]
  (:appliestogroup operator))

(defn
  operator?
  "Checks whether a value is an Operator instance."
  [value]
  (instance? Operator value))

;; Operand information

(defrecord 
  Operand
  [description scope validation])

(defn
  make-operand 
  [description scope validation]
  (Operand. description scope validation))

(defn 
  operand-description
  "Returns description of operand."
  [operand]
  (:description operand))

(defn 
  operand-scope
  "Returns scope of operand."
  [operand]
  (:scope operand))

(defn
  operand-has-scope?
  [operand]
  (not= (operand-scope operand) nil))

(defn
  operand-validation
  "Returns predicate to be used for validating values it is bound to."
  [operand]
  (:validation operand))


;(defrecord 
;  Binding
;  [snippet operand value])
; replaced by damp.ekeko.snippets.OperandBinding class


(defn
  make-binding
  [operand group template value]
  (damp.ekeko.snippets.OperatorOperandBinding. operand group template value))

(defn
  binding-operand
  [binding]
  (.operand binding))

(defn
  binding-group
  [binding] 
  (.group binding))

(defn
  binding-template
  [binding] 
  (.template binding))

(defn
  binding-value
  [binding]
  (.value binding))

(defn
  set-binding-value!
  [binding val]
  (set! (.value binding) val))

(defn
  operator-bindings-for-operands
  "Returns fresh bindings for the operands of the given operator."
  [group template operator]
  (map (fn [operand]
         (make-binding operand group template  ""))
       (operator-operands operator)))

(defn
  make-implicit-operandbinding-for-operator-subject
  [group template subject-template-node operator]
  (make-binding
    (make-operand "Subject" 
                  opscope-subject
                  validity|subject)
    group
    template
    subject-template-node))

(defn
  operator-bindings-for-operands-and-subject
  "Returns fresh bindings for the subject of an operator and its additional operands."
  [group template subject-template-node operator]
  (cons 
    (make-implicit-operandbinding-for-operator-subject group template subject-template-node operator)
    (operator-bindings-for-operands group template operator)))

;; Registered operator types

(def
  categories
  {:generalization "Generalization" 
   :refinement    "Refinement"
   :neutral       "Neutral"
   :other         "Other"
   :destructive   "Destructive"
   :rewrite       "Rewriting"
   :contextual    "Contextual"
   })

(defn
  registered-categories
  "Returns collection of registered operator categories (symbols)."
  []
  (keys categories))

(defn
  category-description
  "Returns description of given operator type (symbol)"
  [category]
  (get categories category))



;; Registered operators


(def 
  operators
  [(Operator. 
     "replace-by-variable"  
     operators/replace-by-variable
     :generalization
     "Replace by meta-variable."
     opscope-subject
     applicability|notprotected
     "Replaces selection by a meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "replace-by-exp"  
     operators/replace-by-exp
     :rewrite
     "Replace by the result of an expression."
     opscope-subject
     (fn [snippetgroup snippet value]
       (and (applicability|simplepropertyvalue snippetgroup snippet value)
            (applicability|notprotected snippetgroup snippet value)))
     "Upon template instantiation, will replace selection by result of expression."
     [(make-operand "Expression (e.g., (str \"prefix\" ?string))" opscope-string validity|string)]
     false)
   
   (Operator. 
     "add-directive-equals"
     operators/add-directive-equals
     :neutral
     "Add directive equals."
     opscope-subject
     applicability|always
     "Requires match to unify with meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-equivalent"
     operators/add-directive-equivalent
     :neutral
     "Add directive equivalent."
     opscope-subject
     applicability|always
     "Requires match to be equivalent to the meta-variable's value. (i.e. they don't have to be the same node, but they should look the same)"
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-protect"
     operators/add-directive-protect
     :neutral
     "Add directive protect."
     opscope-subject
     applicability|always
     "Prevents this node or ancestor nodes from being removed/replaced."
     []
     false)
  
   (Operator. 
     "add-directive-invokes"
     operators/add-directive-invokes
     :refinement
     "Add directive invokes."
     opscope-subject
     applicability|methodinvocation ;applicability|methodinvocationorname
     "Requires matches to invoke the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)

   (Operator. 
     "add-directive-invokedby"
     operators/add-directive-invokedby
     :refinement
     "Add directive invoked-by."
     opscope-subject
     applicability|methoddeclaration ;applicability|methoddeclarationorname
     "Requires matches to be invoked by the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
     
   (Operator. 
     "add-directive-constructs"
     operators/add-directive-constructs
     :refinement
     "Add directive constructs."
     opscope-subject
     applicability|constructorinvocation ;note: no names exist here
     "Requires matches to invoke the constructor bound to the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
     
   (Operator. 
     "add-directive-constructedby"
     operators/add-directive-constructedby
     :refinement
     "Add directive constructed-by."
     opscope-subject
     applicability|constructororname
     "Requires matches to be constructors invoked by the meta-variable binding."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)

   (Operator. 
     "add-directive-overrides"
     operators/add-directive-overrides
     :refinement
     "Add directive overrides."
     opscope-subject
     applicability|methoddeclaration
     "Requires match to be a method, which overrides the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-refersto"
     operators/add-directive-refersto
     :refinement
     "Add directive refers-to."
     opscope-subject
     applicability|always
     "Requires matches to lexically refer to the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-referredby"
     operators/add-directive-referredby
     :refinement
     "Add directive referred-by."
     opscope-subject
     applicability|vardeclaration
     "Requires matches to be referred to lexically by the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-type"
     operators/add-directive-type
     :refinement
     "Add directive type."
     opscope-subject
     applicability|typeortypename
     "Match should resolve to a type, given by meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-type|qname"
     operators/add-directive-type|qname
     :refinement
     "Add directive type|qname."
     opscope-subject
     applicability|typeortypename
     "Match should resolve to a type with the given qualified name."
     [(make-operand "Qualified name (e.g., java.lang.Object)" opscope-string validity|string)]
     false)
   
   (Operator. 
     "add-directive-type|sname"
     operators/add-directive-type|sname
     :refinement
     "Add directive type|sname."
     opscope-subject
     applicability|typeortypename
     "Match should resolve to a type with the given simple name."
     [(make-operand "Simple name (e.g., Integer)" opscope-string validity|string)]
     false)
   
   (Operator. 
     "add-directive-subtype+"
     operators/add-directive-subtype+
     :refinement
     "Add directive subtype+."
     opscope-subject
     applicability|typeortypename
     "Match should resolve to a transitive subtype of the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-subtype+|qname"
     operators/add-directive-subtype+|qname
     :refinement
     "Add directive subtype+|qname."
     opscope-subject
     applicability|typeortypename
     "Match should resolve to a transitive subtype with the given string as qualified name."
     [(make-operand "Qualified name (e.g., java.lang.Object)" opscope-string validity|string)]
     false)
   
   (Operator. 
     "add-directive-subtype+|sname"
     operators/add-directive-subtype+|sname
     :refinement
     "Add directive subtype+|sname."
     opscope-subject
     applicability|typeortypename
     "Match should resolve to a transitive subtype with the given string as simlpe name."
     [(make-operand "Simple name (e.g., Integer)" opscope-string validity|string)]
     false)
   
   (Operator. 
     "add-directive-subtype*"
     operators/add-directive-subtype*
     :refinement
     "Add directive subtype*."
     opscope-subject
     applicability|typeortypename
     "Match should resolve to the type or a transitive subtype of the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-subtype*|qname"
     operators/add-directive-subtype*|qname
     :refinement
     "Add directive subtype*|qname."
     opscope-subject
     applicability|typeortypename
     "Match should resolve to the type or a transitive subtype with the given string as qualified name."
     [(make-operand "Qualified name (e.g., java.lang.Object)" opscope-string validity|string)]
     false)
   
   (Operator. 
     "add-directive-subtype*|sname"
     operators/add-directive-subtype*|sname
     :refinement
     "Add directive subtype*|sname."
     opscope-subject
     applicability|typeortypename
     "Match should resolve to the type or a transitive subtype with the given string as simlpe name."
     [(make-operand "Simple name (e.g., Integer)" opscope-string validity|string)]
     false)
   
   
   (Operator. 
     "restrict-scope-to-child"
     operators/restrict-scope-to-child
     :refinement
     "Use child as matching scope."
     opscope-subject
     applicability|always
     "Match is the corresponding child of the match for the parent."
     []
     false)
   
   (Operator. 
     "relax-scope-to-child+"
     operators/relax-scope-to-child+
     :generalization
     "Use child+ as matching scope."
     opscope-subject
     applicability|child+*
     "Match is nested within the corresponding child of the parent match."
     []
     false)
   
   (Operator. 
     "relax-scope-to-child*"
     operators/relax-scope-to-child*
     :generalization
     "Use child* as matching scope (child or child+)."
     opscope-subject
     applicability|child+*
     "Matches are the corresponding child of the parent match, or nested within it."
     []
     false)
   
   (Operator. 
     "generalize-directive"
     operators/generalize-directive
     :generalization
     "Generalize an existing directive"
     opscope-subject
     (complement applicability|lst)
     "Changes an existing directive into its more general variant. (e.g. type becomes type*)"
     [(make-operand "Directive name" opscope-directive validity|directivename)]
     false)
   
   (Operator. 
     "remove-directive"
     operators/remove-directive
     :destructive
     "Remove a directive"
     opscope-subject
     applicability|always
     "Removes a directive, given its name."
     [(make-operand "Directive name" opscope-directive validity|directivename)]
     false)
   
; Disabled because hardly ever used 
;   (Operator. 
;     "relax-size-to-atleast"
;     operators/relax-size-to-atleast
;     :generalization
;     "Add directive orlarger."
;     opscope-subject
;     applicability|lst-nowildcard
;     "Matches are lists with at least as many elements as the selection."
;     []
;     false)
   
   (Operator. 
     "empty-body"
     operators/empty-body
     :generalization
     "Add directive empty-body."
     opscope-subject
     applicability|block-or-nil
     "Matches empty lists (nil or 0 elements)."
     []
     false)
   
   (Operator. 
     "or-block"
     operators/or-block
     :generalization
     "Add directive or-block."
     opscope-subject
     applicability|child+*
     "Matches with a statement itself, or the statement wrapped in a block."
     []
     false)
   
   ;prefer set matching on owner of list element
   (Operator. 
     "relax-scope-to-member"
     operators/relax-scope-to-member
     :generalization
     "Restrict match to any index within the list match."
     opscope-subject
     applicability|lstelement
     "Matches can reside at any index within the parent list."
     []
     false)
   
   
   (Operator. 
     "add-directive-replace"
     operators/add-directive-replace
     :rewrite
     "Add directive replace."
     opscope-subject 
     applicability|node
     "Rewrites the operand by replacing it with the code corresponding to the template."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false) 
   
   (Operator. 
     "add-directive-replace-value"
     operators/add-directive-replace-value
     :rewrite
     "Add directive replace-value."
     opscope-subject 
     applicability|simplepropertyvalue
     "Rewrites the operand by replacing it with the code corresponding to the template."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false) 
   
   (Operator. 
     "add-directive-add-element"
     operators/add-directive-add-element
     :rewrite
     "Add directive add-element."
     opscope-subject 
     applicability|node
     "Adds the instantiated template to its list operand."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)
;      (make-operand "Index" opscope-string validity|integer)
      ]
     false)
   
   (Operator. 
     "add-directive-insert-before"
     operators/add-directive-insert-before
     :rewrite
     "Add directive insert-before."
     opscope-subject 
     applicability|node
     "Adds the instantiated template before the operand (a list element)."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-insert-after"
     operators/add-directive-insert-after
     :rewrite
     "Add directive insert-after."
     opscope-subject 
     applicability|node
     "Adds the instantiated template after the operand (a list element)."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-remove-element"
     operators/add-directive-remove-element
     :rewrite
     "Add directive remove-element."
     opscope-subject 
     applicability|node
     "Removes the element at the given index from the list in the subject."
     [(make-operand "Index" opscope-string validity|integer)]
     false)
   
   (Operator. 
     "add-directive-remove-element-alt"
     operators/add-directive-remove-element-alt
     :rewrite
     "Add directive remove-element-alt."
     opscope-subject 
     applicability|node
     "Removes the instantiated template from its list operand."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]
     false)
   
   (Operator. 
     "add-directive-copy-node"
     operators/add-directive-copy-node
     :rewrite
     "Add directive copy-node."
     opscope-subject 
     applicability|node
     "Copies the subject (only the node itself!) into the target list at a given index."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)
      (make-operand "Index" opscope-string validity|integer)]
     false)
   
   (Operator. 
     "add-directive-move-element"
     operators/add-directive-move-element
     :rewrite
     "Add directive move-element."
     opscope-subject 
     applicability|node
     "Moves the subject, a list element, into the target list at a given index."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)
      (make-operand "Index" opscope-string validity|integer)]
     false)
   
   (Operator. 
     "remove-node"
     operators/remove-node
     :destructive
     "Remove from template."
     opscope-subject
     applicability|deleteable 
     "Removes its selection from the template."
     []
     false)
   
   (Operator. 
     "replace-parent"
     operators/replace-parent
     :destructive
     "Replace parent node (expressions)."
     opscope-subject
     applicability|replace-parent 
     "Make this expression node replace its parent."
     []
     false)
   
   (Operator. 
     "replace-parent-stmt"
     operators/replace-parent-stmt
     :destructive
     "Replace parent node (statements)."
     opscope-subject
     applicability|replace-parent-stmt 
     "Make this statement replace its parent statement (e.g. a statement contained in a body of an if-statement)."
     []
     false)
   
   (Operator.
     "isolate-list-element"
     operators/isolate-list-element
     :generalization
     "Isolate list element."
     opscope-subject
     applicability|lstelement
     "Removes all other elements from the list, and adds set matching to the list."
     []
     false)
   
   (Operator.
     "isolate-stmt-in-block"
     operators/isolate-list-element
     :destructive
     "Isolate statement in block."
     opscope-subject
     applicability|non-block-stmt ; TODO Parent should be a block.. not the case in a single-stmt if/else-branch..
     "Removes all other statements in this block and adds set matching to the block."
     []
     false)
   
   (Operator.
     "isolate-stmt-in-method"
     operators/isolate-stmt-in-method
     :destructive
     "Isolate statement in method."
     opscope-subject
     applicability|non-block-stmt 
     "Replaces the entire method body by just this statement, adds set matching to the body, and child* to the selected statement."
     []
     false)
   
   (Operator.
     "isolate-expr-in-method"
     operators/isolate-expr-in-method
     :destructive
     "Isolate expression in method."
     opscope-subject
     applicability|expr-in-body 
     "Replaces the entire method body such that it matches any method body containing the selected expression."
     []
     false)
   
   (Operator. 
     "insert-node-before"
     operators/insert-newnodefromclasskeyw-before
     :destructive
     "Insert new node before."
     opscope-subject
     applicability|lstelement|nonroot
     "Creates a new node and inserts it before the selection."
     [(make-operand "Node type" opscope-nodeclasskeyw validity|subjectowninglisttype)]
     false)
   
   ;disabled because difficult to use
   ; (Operator. 
   ;  "insert-text-before"
   ;  operators/insert-newnodefromstring-before
   ;  :destructive
   ;  "Insert text before."
   ;  opscope-subject
   ;  applicability|lstelement|nonroot
   ;  "Parses a string and inserts the resulting node before the selection."
   ;  [(make-operand "New node type" opscope-nodeclasskeyw validity|subjectowninglisttype)
   ;   (make-operand "String" opscope-string validity|string)])
   
   (Operator. 
     "insert-node-after"
     operators/insert-newnodefromclasskeyw-after
     :destructive
     "Insert new node after."
     opscope-subject
     applicability|lstelement|nonroot
     "Creates a new node and inserts it after the selection."
     [(make-operand "Node type" opscope-nodeclasskeyw validity|subjectowninglisttype)]
     false)
   
   ;disabled because difficult to use
   ;(Operator. 
   ;  "insert-text-after"
   ;  operators/insert-newnodefromstring-after
   ;  :destructive
   ;  "Insert text after."
   ;  opscope-subject
   ;  applicability|lstelement|nonroot
   ;  "Parses a string and inserts the resulting node after the selection."
   ;  [(make-operand "New node type" opscope-nodeclasskeyw validity|subjectowninglisttype)
   ;   (make-operand "String" opscope-string validity|string)])
   
   (Operator. 
     "insert-node-at"
     operators/insert-newnodefromclasskeyw-atindex
     :destructive
     "Insert new node at index."
     opscope-subject
     applicability|lst 
     "Creates a new node and inserts it at the given index."
     [(make-operand "Node type" opscope-nodeclasskeyw validity|subjectlisttype)
      (make-operand "List index" opscope-incsubjectlistidx validity|incsubjectlistidx)]
     false)
   
   
   (Operator. 
     "replace-node"
     operators/replace-node
     :destructive
     "Replace by new node."
     opscope-subject
     (fn [snippetgroup snippet value]
       (and (applicability|node|nonroot snippetgroup snippet value)
            (applicability|notprotected snippetgroup snippet value)))
     "Replaces selection by newly created node."
     [(make-operand "Node type" opscope-nodeclasskeyw validity|subjectownerpropertytype)]
     false)
   
   (Operator. 
     "replace-value"
     operators/replace-value
     :destructive
     "Replace by value."
     opscope-subject
     (fn [snippetgroup snippet value]
       (and (applicability|simplepropertyvalue snippetgroup snippet value)
            (applicability|notprotected snippetgroup snippet value)))
     "Replaces selection by given textual value."
     [(make-operand "Value text" opscope-string validity|string)]
     false)
   
   (Operator. 
     "erase-list"
     operators/erase-list
     :destructive
     "Erase list."
     opscope-subject
     applicability|lst
     "Deletes all elements of the list."
     []
     false)
   
   
   (Operator. 
     "erase-comments"
     operators/erase-comments
     :destructive
     "Erase comments."
     opscope-subject
     applicability|node
     "Deletes all comments within the node (and the node itself)."
     []
     false)
   
; Hiding because it's never used
;   (Operator. 
;     "ignore-comments"
;     operators/ignore-comments
;     :generalization
;     "Ignore comments."
;     opscope-subject
;     applicability|node
;     "Replaces all comments within the node (and the node itself) by a wildcard."
;     []
;     false)
     
   (Operator. 
     "ignore-absentvalues"
     operators/ignore-absentvalues
     :generalization
     "Ignore absent children."
     opscope-subject
     applicability|node
     "Replaces absent children that are optional according to Java grammar by a wildcard."
     []
     false)

   
   (Operator. 
     "replace-by-wildcard"
     operators/replace-by-wildcard
     :generalization
     "Replace by wildcard."
     opscope-subject
     applicability|wildcard
     "Replaces selection by wildcard."
     []
     false)
   
; TODO Not fully implemented yet..
;   (Operator. 
;     "subs-src"
;     operators/add-directive-subs-src
;     :neutral
;     "Substitute - source."
;     opscope-subject
;     applicability|always
;     "This node will substitute for the parent node with @subs-tgt."
;     []
;     false)
;   
;   (Operator. 
;     "subs-tgt"
;     operators/add-directive-subs-tgt
;     :neutral
;     "Substitute - target."
;     opscope-subject
;     applicability|always
;     "This node will be substituted by the child node with @subs-src."
;     []
;     false)
   
; Hiding this one because it's rarely used up to now..
;   (Operator. 
;     "replace-by-checked-wildcard"
;     operators/replace-by-checked-wildcard
;     :generalization
;     "Replace by checked wildcard."
;     opscope-subject
;     applicability|wildcard
;     "Replaces selection by wildcard, but still checks for the AST node's type. (e.g. if applied to a MethodDeclaration, we still check that it's a MethodDeclaration)"
;     []
;     false)
   
   (Operator. 
     "consider-regexp|list"
     operators/consider-regexp|list
     :generalization
     "Use regexp matching for elements."
     opscope-subject
     applicability|regexplst
     "Considers list as regular expression when matching."
     []
     false)
   
   (Operator. 
     "consider-regexp|cfglist"
     operators/consider-regexp|cfglist
     :generalization
     "Use regexp matching over control flow graph for elements."
     opscope-subject
     applicability|regexpcfglst
     "Regular expression over control flow graph of method."
     []
     false)
   
   (Operator. 
     "update-multiplicity"
     operators/update-multiplicity
     :generalization
     "Update multiplicity."
     opscope-subject
     applicability|multiplicity
     "Updates multiplicity of match."
     [(make-operand "Multiplicity" opscope-multiplicity validity|multiplicity)]
     false)
   
   (Operator. 
     "consider-set|lst"
     operators/consider-set|list
     :generalization
     "Use set matching for elements."
     opscope-subject
     applicability|lst-nowildcard
     "Set matching will be used for elements of list."
     []
     false)
   
   (Operator.
     "include-inherited"
     operators/include-inherited
     :generalization
     "Include inherited members."
     opscope-subject
     applicability|typedecl-bodydeclarations
     "Inherited class members are also included in list matching."
     []
     false)

; TODO Not implemented yet
;   (Operator. 
;     "add-directive-or"
;     operators/add-directive-or
;     :generalization
;     "Add directive or."
;     opscope-subject
;     applicability|any
;     "The subject matches, or the template referred to by the operand."
;     []
;     true)
   
   (Operator. 
     "add-directive-orimplicit"
     operators/add-directive-orimplicit
     :generalization
     "Add directive orimplicit."
     opscope-subject
     applicability|receiver
     "Implicit this receiver will match."
     []
     false)
   
   (Operator. 
     "add-directive-notnil"
     operators/add-directive-notnil
     :refinement
     "Add directive notnil."
     opscope-subject
     applicability|always
     "Matches if the subject is not nil/null."
     []
     false)
   
   (Operator. 
     "add-directive-orsimple"
     operators/add-directive-orsimple
     :generalization
     "Add directive orsimple."
     opscope-subject
     applicability|qualifiedtypeorname
     "Simple types resolving to name of qualified type will match."
     []
     false)
   
   ; TODO Finish up the subs-src and subs-tgt directives, which allow any descendant to substitute for a node..
   (Operator. 
     "add-directive-ignore"
     operators/add-directive-ignore
     :generalization
     "Add directive ignore."
     opscope-subject
     applicability|expressionstmt
     "During matching, this node is substituted by its child."
     []
     false)
   
   (Operator. 
     "generalize-references"
     operators/generalize-references
     :generalization
     "Generalize variable references."
     opscope-subject
     applicability|vardeclaration
     "Generalizes all references to given variable declaration node in the template group."
     []
     true)
   
   (Operator. 
     "generalize-types"
     operators/generalize-types
     :generalization
     "Generalize type references."
     opscope-subject
     applicability|typeortypename
     "Generalizes all references to given type in template group."
     []
     true)
   
   (Operator. 
     "generalize-types|qname"
     operators/generalize-types|qname
     :generalization
     "Generalize type references, preserve qualified name."
     opscope-subject
     applicability|typeortypename
     "Generalizes all references to given type in the template group, while preserving its qualified name."
     []
     true)

   (Operator. 
     "extract-template"
     operators/extract-template
     :neutral
     "Extract node as new template."
     opscope-subject
     applicability|node|nonroot
     "Extracts selected node into an additional template for this node only."
     []
     true)
   
   (Operator. 
     "generalize-invocations"
     operators/generalize-invocations
     :generalization
     "Generalize method invocations."
     opscope-subject
     applicability|methoddeclarationorname
     "Generalizes invocations to selected method declaration."
     []
     true)

   (Operator. 
     "generalize-constructorinvocations"
     operators/generalize-constructorinvocations
     :generalization
     "Generalize constructor invocations."
     opscope-subject
     applicability|constructororname
     "Generalizes invocations of selected constructor declaration."
     []
     true)
     
     
   ])

(defn 
  registered-operators
  "Returns collection of registered operators."
  []
  operators)

(defn
  operator-from-id
  [id]
  (first 
    (filter (fn [operator]
              (= id (operator-id operator)))
            operators)))

(defn
  registered-operators-in-category
  [category]
  (filter (fn [operator]
            (= category (operator-category operator)))
          operators))

;(defn
;  validity|operatorid
;  [snippetgroup snippet subject operandvalue]
;  (some
;    (fn [x] (= x operandvalue))
;    (for [op (registered-operators)] (operator-id op))))



;; Operator applicability
;; ----------------------

(defn 
  applicable?
  "Returns true if preconditions for given operator are fulfilled by the node."
  [snippetgroup snippet node operator]
  (let [opscope (operator-scope operator)
        validationf (operator-validation operator)]
    (when (not= opscope opscope-subject)
      (throw (Exception. (str "Unexpected operator scope:" operator opscope))))
    (validationf snippetgroup snippet node)))


(defn
  applicable-operators
  "Filters operators that are applicable to the given node."
  ([snippetgroup snippet node operators] 
    (filter
      (fn [operator] 
        (applicable? snippetgroup snippet node operator))
      operators))
  ([snippetgroup snippet node]
    (applicable-operators snippetgroup snippet node (registered-operators))))

(defn
  applicable-operators-in-category
  [snippetgroup snippet node cat]
  (filter (fn [operator]
            (= (operator-category operator) cat))
          (applicable-operators  snippetgroup snippet node)))


;; Operand validation
;; ------------------

(defn
  validate-newvalue-for-operandbinding
  [snippetgroup snippet node operator binding value]
  (let [group (binding-group binding)  
        template (binding-template binding)
        operand (binding-operand binding)]
    ;should not happen if bindings created normally
    (when-not (= group snippetgroup)
      (throw (IllegalArgumentException. (str "Operand group is not the template group the operator is being applied to:" group snippetgroup))))
    ;should not happen if bindings created normally
    (when-not (= template snippet)
      (throw (IllegalArgumentException. (str "Operand template is not the template the operator is being applied to:" template snippet))))
    (let [opscope (operand-scope operand)
          opvalid (operand-validation operand)
          opdesc (operand-description operand)]      
      (when-not (opvalid group template node value)
        (throw (IllegalArgumentException. (str "Value \"" value "\" for operand \"" opdesc "\" is invalid according to \"" opvalid "\"")))))))


(defn
  validate-operandbinding
  [snippetgroup snippet node operator binding]
  (let [currentvalue (binding-value binding)]
    validate-newvalue-for-operandbinding snippetgroup snippet node operator binding currentvalue))

(defn
  validate-operandbindings
  [snippetgroup snippet node operator bindings]
  (doseq [binding bindings]
    (validate-operandbinding snippetgroup snippet node operator binding)))


(defn
  operandbinding|valid?
  [snippetgroup snippet node operator binding]
  (try 
    (validate-operandbinding snippetgroup snippet node operator binding)
    true
    (catch IllegalArgumentException e false)))

;; Applying operators
;; ------------------

(defn-
  apply-operator
  "Apply operator to snippet, returns new snippet (3 args) or new snippetgroup (4 args)."
  ([template operatorf subject args]
    (apply operatorf template subject args))
  ([templategroup template operatorf subject args]
    (apply operatorf templategroup template subject args)))

(defn 
  apply-operator-to-snippetgroup
  "Apply operator to snippetgroup."
  ([snippetgroup operator bindings] ;called by UI
                                    (let [subject-binding 
                                          (first bindings)
                                          subject-template
                                          (binding-template subject-binding)
                                          subject-node
                                          (binding-value subject-binding)] 
                                      (apply-operator-to-snippetgroup snippetgroup subject-template subject-node operator bindings)))
  ([snippetgroup snippet value operator bindings] ;regular call 
                                                  (validate-operandbindings snippetgroup snippet value operator bindings) 
                                                  (let [operatof (operator-operator operator)
                                                        args (map binding-value (rest bindings))]
                                                    (if 
                                                      (operator-appliestogroup? operator)
                                                      (if-let [newsnippetgroup (apply-operator snippetgroup snippet operatof value args)]
                                                        newsnippetgroup
                                                        snippetgroup)
                                                      (if-let [newsnippet (apply-operator snippet operatof value args)]
                                                        (snippetgroup/replace-snippet snippetgroup snippet newsnippet)
                                                        snippetgroup)))))


(defn apply-operator-to-root
  [templategroup snippet id]
  (let [operator (some 
                   (fn [op] (if (= (operator-id op) id) op))
                   (registered-operators))
        subject (snippet/snippet-root snippet)
        bindings (make-implicit-operandbinding-for-operator-subject templategroup snippet subject operator)]
    (apply-operator-to-snippetgroup templategroup snippet subject operator [bindings])))


(defn apply-operator-to-all-roots
  [templategroup operator-id]
  (loop [tg templategroup
         snippets (snippetgroup/snippetgroup-snippetlist templategroup)]
    (if (empty? snippets)
      tg
      (let [snippet (first snippets)]
        (recur 
          (apply-operator-to-root tg snippet operator-id)
          (rest snippets))))))

(defn preprocess-templategroup
  [templategroup]
  (-> templategroup
    (apply-operator-to-all-roots "erase-comments")
    (apply-operator-to-all-roots "ignore-absentvalues")))



;; Operand candidate values
;; ------------------------

(defmulti
  possible-operand-values
  (fn [snippetgroup snippet node operator operand]
    (operand-scope operand)))

(defmethod
  possible-operand-values
  opscope-subject
  [snippetgroup snippet node operator operand]
  ;(snippet/snippet-nodes snippet)
  [node]) 

(defmethod
  possible-operand-values
  opscope-multiplicity
  [snippetgroup snippet node operator operand]
  ["1" "+" "*"]) 

(defmethod
  possible-operand-values
  opscope-variable
  [snippetgroup snippet node operator operand]
  ; Pick among the existing uservars, or generate a new one
  (conj (apply concat (map 
                       matching/snippet-vars-among-directivebindings
                       (snippetgroup/snippetgroup-snippetlist snippetgroup)))
        (util/gen-readable-lvar-for-value node))
;  (if
;    (= (operator-id operator) "add-directive-equals")
;    [(util/gen-lvar)]
;    (let [uservars
;          (apply concat (map 
;                          matching/snippet-vars-among-directivebindings
;                          (snippetgroup/snippetgroup-snippetlist snippetgroup)))]
;      (if (empty? uservars)
;        [(util/gen-readable-lvar-for-value node)]
;        uservars)))
  )

(defmethod
  possible-operand-values
  opscope-nodeclasskeyw
  [snippetgroup snippet node operator operand]
  (map astnode/ekeko-keyword-for-class astnode/node-classes))

(defn- all-type-declarations []
  (for [x (damp.ekeko/ekeko [?type] (ast/ast :TypeDeclaration ?type))]
    (first x)))

(defmethod
  possible-operand-values
  opscope-string
  [snippetgroup snippet node operator operand]
  (case (operator-id operator)
    "add-directive-type|sname"
    (for [x (all-type-declarations)] ; Memoize this? But the cache'd have to be cleared when the project being queried changes..
      (-> x .getName .toString))
    "add-directive-type|qname"
    (for [x (all-type-declarations)]
      (-> x .resolveBinding .getQualifiedName))
    "remove-directive"
    (map damp.ekeko.snippets.directives/directive-name (matching/registered-directives))
    ; default case
    []))

(defmethod
  possible-operand-values
  opscope-directive
  [snippetgroup snippet node operator operand]
  (let [node-directives
        (distinct (for [bd (snippet/snippet-bounddirectives-for-node snippet node)]
                    (damp.ekeko.snippets.directives/directive-name 
                      (damp.ekeko.snippets.directives/bounddirective-directive bd))))]
    
    (case (operator-id operator)
     "remove-directive"
     (remove (fn [x] (or (= x "child") (= x "match"))) 
             node-directives)
     "generalize-directive"
     (clojure.set/intersection #{"child" "type" "type|sname" "type|qname"} (into #{} node-directives))
    ; default case
    [])))

(defmethod
  possible-operand-values
  opscope-subjectlistidx
  [snippetgroup snippet subject operator operand]
  (let [lst-raw (astnode/value-unwrapped subject)
        siz (.size lst-raw)]
    (range 0 (inc siz))))


(defmethod
  possible-operand-values
  opscope-incsubjectlistidx
  [snippetgroup snippet subject operator operand]
  (let [lst-raw (astnode/value-unwrapped subject)]
    (range 0 (+ 2 (.size lst-raw)))))


(defn
  possible-operand-values|valid
  [snippetgroup snippet node operator operand]
  (let [validationf 
        (operand-validation operand) 
        candidates
        (possible-operand-values snippetgroup snippet node operator operand)]
    (filter
      (fn [value]
        (validationf snippetgroup snippet node value))
      candidates)))



(defn
  register-callbacks 
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_APPLY_TO_SNIPPETGROUP) apply-operator-to-snippetgroup)
  
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_CATEGORIES) registered-categories)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATORCATEGORY_DESCRIPTION) category-description)
  
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_NAME) operator-name)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_FROM_ID) operator-from-id)
  
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_BINDINGS_FOR_OPERANDS) operator-bindings-for-operands-and-subject)
  
  ;    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_ARGUMENT_WITH_PRECONDITION) operator-argument-with-precondition)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_DESCRIPTION) operator-description)
  ;   (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_ISTRANSFORM) is-transform-operator?)
  
  
  ;(set! (damp.ekeko.snippets.data.SnippetOperator/FN_POSSIBLE_NODES_FOR_OPERATOR_ARGUMENT_IN_GROUP) possible-nodes-for-operator-argument-in-group)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_IS_OPERATOR) operator?)
  
  
  
  
  )


(register-callbacks)

