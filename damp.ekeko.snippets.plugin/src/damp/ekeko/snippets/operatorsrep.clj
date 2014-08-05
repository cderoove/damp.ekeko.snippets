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

(def opscope-string :string)
(def opscope-subjectlistidx :subjectlistidx) ;0 till size exclusive
(def opscope-incsubjectlistidx :incsubjectlistidx);0 till size inclusive
(def opscope-multiplicity :multiplicity) ;match multiplicity (int, *, +)


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
        (not (.isMandatory ownerprop))))))

(defn
  applicability|multiplicity
  [snippetgroup snippet value]
  (matching/snippet-value-regexp-element? snippet value))


(defn
  applicability|wildcard
  "Verifies that value is not a list with regexp matching enabled."
  [snippetgroup snippet value]
  (not
    (and
      (astnode/lstvalue? value)
      (matching/snippet-list-regexp? snippet value))))

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
    (when-let [ownerprop (astnode/owner-property value)]
      (= "statements" (astnode/property-descriptor-id ownerprop)))))

(defn
  applicability|receiver
  [snippetgroup snippet value]
  (and
    (applicability|node snippetgroup snippet value)
    (when-let [owner (astnode/owner value)]
      (and (= :MethodInvocation (astnode/ekeko-keyword-for-class-of owner))
           (when-let [ownerprop (astnode/owner-property value)]
             (and (= :expression (astnode/ekeko-keyword-for-property-descriptor ownerprop))))))))


(defn
  applicability|qualifiedtypeorname
  [snippetgroup snippet value]
  (and 
    (applicability|node snippetgroup snippet value)
    (> (count (clojure.string/split (.toString value) #"\.")) 1)))
  


(defn
  validity|always
  [snippetgroup snippet subject operandvalue]
  true)

(defn
  validity|string
  [snippetgroup snippet subject operandvalue]
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
    (string? operandvalue)
    (= (first operandvalue) \?)))


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
    (instance-of-classkeyword-assignable-to-property? classkeyword propertydescriptor))) 

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
     "Replace by meta-variable."
     opscope-subject
     applicability|always
     "Replaces selection by a meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)])
   
   (Operator. 
     "replace-by-exp"  
     operators/replace-by-exp
     :rewrite
     "Replace by the result of an expression."
     opscope-subject
     applicability|simplepropertyvalue
     "Upon template instantiation, will replace selection by result of expression."
     [(make-operand "Expression (e.g., (str \"prefix\" ?string))" opscope-string validity|string)])
   
   (Operator. 
     "add-directive-equals"
     operators/add-directive-equals
     :neutral
     "Add directive equals."
     opscope-subject
     applicability|always
     "Requires match to unify with meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)])
   
   (Operator. 
     "add-directive-refersto"
     operators/add-directive-refersto
     :refinement
     "Add directive refers-to."
     opscope-subject
     applicability|always
     "Requires matches to lexically refer to the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)])
   
   (Operator. 
     "add-directive-referredby"
     operators/add-directive-referredby
     :refinement
     "Add directive referred-by."
     opscope-subject
     applicability|always
     "Requires matches to be referred to lexically by the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)])
   
   (Operator. 
     "add-directive-type"
     operators/add-directive-type
     :refinement
     "Add directive type."
     opscope-subject
     applicability|always
     "Match should resolve to a type, given by meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)])
   
   (Operator. 
     "add-directive-type|qname"
     operators/add-directive-type|qname
     :refinement
     "Add directive type|qname."
     opscope-subject
     applicability|always
     "Match should resolve to a type with the given qualified name."
     [(make-operand "Qualified name (e.g., java.lang.Object)" opscope-string validity|string)])
   
   (Operator. 
     "add-directive-type|sname"
     operators/add-directive-type|sname
     :refinement
     "Add directive type|sname."
     opscope-subject
     applicability|always
     "Match should resolve to a type with the given simple name."
     [(make-operand "Simple name (e.g., Integer)" opscope-string validity|string)])
   
   
   (Operator. 
     "add-directive-subtype+"
     operators/add-directive-subtype+
     :refinement
     "Add directive subtype+."
     opscope-subject
     applicability|always
     "Match should resolve to a transitive subtype of the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)])
   
   (Operator. 
     "add-directive-subtype+|qname"
     operators/add-directive-subtype+|qname
     :refinement
     "Add directive subtype+|qname."
     opscope-subject
     applicability|always
     "Match should resolve to a transitive subtype with the given string as qualified name."
     [(make-operand "Qualified name (e.g., java.lang.Object)" opscope-string validity|string)])
   
   (Operator. 
     "add-directive-subtype+|sname"
     operators/add-directive-subtype+|sname
     :refinement
     "Add directive subtype+|sname."
     opscope-subject
     applicability|always
     "Match should resolve to a transitive subtype with the given string as simlpe name."
     [(make-operand "Simple name (e.g., Integer)" opscope-string validity|string)])
   
   (Operator. 
     "add-directive-subtype*"
     operators/add-directive-subtype*
     :refinement
     "Add directive subtype*."
     opscope-subject
     applicability|always
     "Match should resolve to the type or a transitive subtype of the binding for the meta-variable."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)])
   
   (Operator. 
     "add-directive-subtype*|qname"
     operators/add-directive-subtype*|qname
     :refinement
     "Add directive subtype*|qname."
     opscope-subject
     applicability|always
     "Match should resolve to the type or a transitive subtype with the given string as qualified name."
     [(make-operand "Qualified name (e.g., java.lang.Object)" opscope-string validity|string)])
   
   (Operator. 
     "add-directive-subtype*|sname"
     operators/add-directive-subtype*|sname
     :refinement
     "Add directive subtype*|sname."
     opscope-subject
     applicability|always
     "Match should resolve to the type or a transitive subtype with the given string as simlpe name."
     [(make-operand "Simple name (e.g., Integer)" opscope-string validity|string)])
   
   
   (Operator. 
     "restrict-scope-to-child"
     operators/restrict-scope-to-child
     :refinement
     "Use child as matching scope."
     opscope-subject
     applicability|always
     "Match is the corresponding child of the match for the parent."
     [])
   
   (Operator. 
     "relax-scope-to-child+"
     operators/relax-scope-to-child+
     :generalization
     "Use child as matching scope."
     opscope-subject
     (complement applicability|lst)
     "Match is nested within the corresponding child of the parent match."
     [])
   
   (Operator. 
     "relax-scope-to-child*"
     operators/relax-scope-to-child*
     :generalization
     "Use child as matching scope (child or child+)."
     opscope-subject
     (complement applicability|lst)
     "Matches are the corresponding child of the parent match, or nested within it."
     [])
   
   
   (Operator. 
     "relax-size-to-atleast"
     operators/relax-size-to-atleast
     :generalization
     "Add directive orlarger."
     opscope-subject
     applicability|lst
     "Matches are lists with at least as many elements as the selection."
     [])
   
   ;prefer set matching on owner of list element
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
     "add-directive-replace"
     operators/add-directive-replace
     :rewrite
     "Add directive replace."
     opscope-subject 
     ;(complement applicability|nonroot) 
     applicability|node
     "Rewrites the operand by replacing it with the code corresponding to the template."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]) 
   
   (Operator. 
     "add-directive-replace-value"
     operators/add-directive-replace-value
     :rewrite
     "Add directive replace-value."
     opscope-subject 
     ;(complement applicability|nonroot) 
     applicability|simplepropertyvalue
     "Rewrites the operand by replacing it with the code corresponding to the template."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]) 
   
   
   
   (Operator. 
     "add-directive-add-element"
     operators/add-directive-add-element
     :rewrite
     "Add directive add-element."
     opscope-subject 
     ;(complement applicability|nonroot) 
     applicability|node
     "Adds the instantiated template to its list operand."
     [(make-operand "Meta-variable (e.g., ?v)" opscope-variable validity|variable)]) 
   
   
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
     operators/insert-newnodefromclasskeyw-before
     :destructive
     "Insert new node before."
     opscope-subject
     applicability|lstelement|nonroot
     "Creates a new node and inserts it before the selection."
     [(make-operand "Node type" opscope-nodeclasskeyw validity|subjectowninglisttype)])
   
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
     [(make-operand "Node type" opscope-nodeclasskeyw validity|subjectowninglisttype)])
   
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
     )
   
   
   (Operator. 
     "replace-node"
     operators/replace-node
     :destructive
     "Replace by new node."
     opscope-subject
     applicability|node|nonroot
     "Replaces selection by newly created node."
     [(make-operand "Node type" opscope-nodeclasskeyw validity|subjectownerpropertytype)]
     )
   
   (Operator. 
     "replace-value"
     operators/replace-value
     :destructive
     "Replace by value."
     opscope-subject
     applicability|simplepropertyvalue
     "Replaces selection by given textual value."
     [(make-operand "Value text" opscope-string validity|string)]
     )
   
   (Operator. 
     "erase-list"
     operators/erase-list
     :destructive
     "Erase list."
     opscope-subject
     applicability|lst
     "Deletes all elements of the list."
     []
     )
   
   (Operator. 
     "replace-by-wildcard"
     operators/replace-by-wildcard
     :generalization
     "Replace by wildcard."
     opscope-subject
     applicability|wildcard
     "Replaces selection by wildcard."
     []
     )
   
   ;todo: operator to rever to normal constraining of list elements
   ;needs to re-add ground-relative-to-parent
   
   (Operator. 
     "consider-regexp|list"
     operators/consider-regexp|list
     :generalization
     "Use regexp matching for elements."
     opscope-subject
     applicability|regexplst
     "Considers list as regular expression when matching."
     []
     )
   
   (Operator. 
     "consider-regexp|cfglist"
     operators/consider-regexp|cfglist
     :generalization
     "Use regexp matching over control flow graph for elements."
     opscope-subject
     applicability|regexpcfglst
     "Regular expression over control flow graph of method."
     []
     )
   
   (Operator. 
     "update-multiplicity"
     operators/update-multiplicity
     :generalization
     "Update multiplicity."
     opscope-subject
     applicability|multiplicity
     "Updates multiplicity of match."
     [(make-operand "Multiplicity" opscope-multiplicity validity|multiplicity)]
     )
   
   (Operator. 
     "consider-set|lst"
     operators/consider-set|list
     :generalization
     "Use set matching for elements."
     opscope-subject
     applicability|lst
     "Set matching will be used for elements of list."
     []
     )
   
   (Operator. 
     "add-directive-orimplicit"
     operators/add-directive-orimplicit
     :generalization
     "Add directive orimplicit."
     opscope-subject
     applicability|receiver
     "Implicit this receiver will match."
     []
     )
   
   (Operator. 
     "add-directive-orsimple"
     operators/add-directive-orsimple
     :generalization
     "Add directive orsimple."
     opscope-subject
     applicability|qualifiedtypeorname
     "Simple types resolving to name of qualified type will match."
     []
     )
   
   
   
   
   
   
   
   
   
   
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
  opscope-multiplicity
  [snippetgroup snippet node operator operand]
  ["1" "+" "*"]) 

(defmethod
  possible-operand-values
  opscope-variable
  [snippetgroup snippet node operator operand]
  (if
    (= (operator-name operator) "add-directive-equals")
    (conj
      (map 
        matching/snippet-vars-among-directivebindings
        (snippetgroup/snippetgroup-snippetlist snippetgroup))
      (str (util/gen-lvar)))
    [(str (util/gen-readable-lvar-for-value node))]))

(defmethod
  possible-operand-values
  opscope-nodeclasskeyw
  [snippetgroup snippet node operator operand]
  (map astnode/ekeko-keyword-for-class astnode/node-classes))

(defmethod
  possible-operand-values
  opscope-string
  [snippetgroup snippet node operator operand]
  [""])


(defmethod
  possible-operand-values
  opscope-string
  [snippetgroup snippet node operator operand]
  [""])

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
  
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_BINDINGS_FOR_OPERANDS) operator-bindings-for-operands-and-subject)
  
  ;    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_ARGUMENT_WITH_PRECONDITION) operator-argument-with-precondition)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_DESCRIPTION) operator-description)
  ;   (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_ISTRANSFORM) is-transform-operator?)
  
  
  ;(set! (damp.ekeko.snippets.data.SnippetOperator/FN_POSSIBLE_NODES_FOR_OPERATOR_ARGUMENT_IN_GROUP) possible-nodes-for-operator-argument-in-group)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_IS_OPERATOR) operator?)
  
  
  
  
  )


(register-callbacks)

