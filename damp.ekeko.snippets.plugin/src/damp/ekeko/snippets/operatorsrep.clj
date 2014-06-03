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
             [util :as util]
             ]))

;; Operator information

;scopes are necessary to communicate selection dialog type to gui
(def opscope-subject :subject) ;operand scope denoting the subject of the operator
(def opscope-variable :variable) ;operand/operator scope for logic variables

(def opscope-nodeclasskeyw :nodeclasskeyw) ;operand scope for ekeko keyword for node class 

(defn
  applicability|always
  [snippetgroup snippet value] 
  true)

(defn 
  applicability|node
  [snippetgroup snippet value]
  (astnode/ast? value))

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
  applicability|lst
  [snippetgroup snippet value]
  (astnode/lstvalue? value))

(defn 
  applicability|lstelement
  [snippetgroup snippet value]
  (and 
    (not (astnode/lstvalue? value))
    (astnode/property-descriptor-list? (astnode/owner-property value))))

(defn 
  applicability|lstelement|nonroot
  [snippetgroup snippet value]
  (and (applicability|node snippetgroup snippet value)
       (applicability|nonroot snippetgroup snippet value)))


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
    (let [ownerprop (astnode/owner-property value)]
      (or 
        (astnode/property-descriptor-list? ownerprop)
        (not (.isMandatory ownerprop))))))


(defn
  validity|always
  [snippetgroup snippet value operandvalue]
  true)


(defn
  validity|subject
  [snippetgroup snippet value operandvalue]
  (= operandvalue value))


(defn
  validity|node
  [snippetgroup snippet value operandvalue]
  (astnode/ast? operandvalue))

(defn
  validity|variable
  [snippetgroup snippet value operandvalue]
  (and 
    (string? operandvalue)
    (= (first operandvalue) \?)))

(defn
  validity|subjectowninglisttype
  [snippetgroup snippet value operandvalue]
  (and 
    (keyword? operandvalue)
    (let [clazz 
          (astnode/class-for-ekeko-keyword operandvalue)
          lstpropdesc 
          (astnode/owner-property value)
          lstelementtype
          (astnode/property-descriptor-element-node-class lstpropdesc)]
      (.isAssignableFrom lstelementtype clazz))))
        
(defrecord 
  Operator
  [id operator category name scope validation description operands])

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
  "Returns descroption of given operator."
  [operator]
  (:description operator))

(defn 
  operator-operands 
  "Returns operands for given operator."
  [operator]
  (:operands operator))

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
     "Replace by variable."
     opscope-subject
     applicability|always
     "Replaces selection by a variable."
     [(make-operand "Variable (e.g., ?v)" opscope-variable validity|variable)])
   
   (Operator. 
     "add-directive-equals"
     operators/add-directive-equals
     :neutral
     "Require match to unify with variable."
     opscope-subject
     applicability|always
     "Adds matching directive equals/1 to selection."
     [(make-operand "Variable (e.g., ?v)" opscope-variable validity|variable)])
   
   (Operator. 
     "relax-scope-to-child+"
     operators/relax-scope-to-offspring
     :generalization
     "Restrict match to any offspring of parent match."
     opscope-subject
     (complement applicability|lst)
     "Matches can reside at an arbitrary depth within the match for the parent."
     [])
   
   (Operator. 
     "relax-size-to-atleast"
     operators/relax-size-to-atleast
     :generalization
     "Allow matches to be larger."
     opscope-subject
     applicability|lst
     "Matches are lists with at least as many elements as the selection."
     [])
   
   (Operator. 
     "relax-scope-to-member"
     operators/relax-scope-to-member
     :generalization
     "Restrict match to any index within the list match."
     opscope-subject
     applicability|lstelement
     "Matches can reside at any index within the parent list."
     [])
   
   (Operator. 
     "restrict-scope-to-child"
     operators/restrict-scope-to-child
     :refinement
     "Restrict match to corresponding child of the parent match."
     opscope-subject
     applicability|always
     "Matches are the corresponding child of the match for the parent."
     [])
   
   (Operator. 
     "replace-operand-by-template"
     operators/replace-operand-by-template
     :rewrite
     "Replace operand by instantiated template."
     opscope-subject 
     (complement applicability|nonroot) 
     "Rewrites the operand by replacing it with the code corresponding to the template."
     [(make-operand "Variable (e.g., ?v)" opscope-variable validity|variable)]) ;todo: check var comes from lhs?
   
   (Operator. 
     "remove-node"
     operators/remove-node
     :destructive
     "Remove from template."
     opscope-subject
     applicability|deleteable 
     "Removes its selection from the template."
     [])
   
   (Operator. 
     "insert-node-before"
     operators/insert-newnode-before
     :destructive
     "Insert new node before."
     opscope-subject
     applicability|lstelement|nonroot
     "Creates a new node and inserts it before the selection."
     [(make-operand "New node type" opscope-nodeclasskeyw validity|subjectowninglisttype)])


   ])


(defn 
  registered-operators
  "Returns collection of registered operators."
  []
  operators)

(defn
  registered-operators-in-category
  [category]
  (filter (fn [operator]
            (= category (operator-category operator)))
          operators))



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
  [snippetgroup snippet node]
  (filter
    (fn [operator] 
      (applicable? snippetgroup snippet node operator))
    (registered-operators)))

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
  "Apply operator to snippet, returns new snippet."
  [template operatorf subject args]
  (apply operatorf template subject args))

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
   (let [newsnippet
         (apply-operator snippet (operator-operator operator) value (map binding-value (rest bindings)))]
     (snippetgroup/snippetgroup-replace-snippet snippetgroup snippet newsnippet))))





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
  opscope-variable
  [snippetgroup snippet node operator operand]
  (str 
    (if
      (= (operator-name operator) "add-directive-equals")
      (conj
        (map 
          matching/snippet-vars-among-directivebindings
          (snippetgroup/snippetgroup-snippetlist snippetgroup))
        (util/gen-lvar))
      [(util/gen-readable-lvar-for-value node)])))

(defmethod
  possible-operand-values
  opscope-nodeclasskeyw
  [snippetgroup snippet node operator operand]
  (map astnode/ekeko-keyword-for-class astnode/node-classes))


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
  
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_BINDINGS_FOR_OPERANDS) operator-bindings-for-operands-and-subject)
  
  ;    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_ARGUMENT_WITH_PRECONDITION) operator-argument-with-precondition)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_DESCRIPTION) operator-description)
  ;   (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_ISTRANSFORM) is-transform-operator?)
  
  
  ;(set! (damp.ekeko.snippets.data.SnippetOperator/FN_POSSIBLE_NODES_FOR_OPERATOR_ARGUMENT_IN_GROUP) possible-nodes-for-operator-argument-in-group)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_IS_OPERATOR) operator?)
  
  
  
  
  )


  (register-callbacks)

