(ns 
  ^{:doc "Representation for operators."
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
             [operators :as operators]
             [parsing :as parsing]
             [snippetgroup :as snippetgroup]
             [snippetgrouphistory :as snippetgrouphistory]
             [precondition :as precondition]

             ]))

;; Operator information

(defrecord 
  Operator
  [id operator category name scope description operands])

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
  [description scope])

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


;(defrecord 
;  Binding
;  [operand value])

(defn
  binding-operand
  [opval]
  (.operand opval))

(defn
  binding-value
  [opval]
  (.value opval))

(defn
  set-binding-value!
  [opval val]
  (set! (.value opval) val)) 
  
(defn
  operator-bindings-for-operands
  "Returns fresh, unitialized bindings for the operands of the given operator."
  [operator]
  (map (fn [operator]
         (damp.ekeko.snippets.OperandBinding. operator ""))
    (operator-operands operator)))

(defn
  binding-operand-description
  [opval]
  (operand-description (binding-operand opval)))

;; Registered operator types

(def
  categories
  {:generalization  "Generalization" 
     :refinement    "Refinement"
     :neutral       "Neutral"
     :other         "Other"
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
     :replace-by-variable  
     operators/replace-by-variable
     :generalization
     "Replace by variable"
     nil
     "Replaces selection by a variable."
     [(Operand. "Variable (e.g., ?v)" nil)])])


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


;; Operator Precondition
;; ---------------------

(def 
  operator-precondition
  {:listvalue                            [:property ast/value|list]					          
   :primitive-or-null-value              [:property precondition/primitive-or-null-value]					          
   :is-variabledeclarationstatement?     [:node precondition/is-variabledeclarationstatement?]
   :is-ifstatement?                      [:node precondition/is-ifstatement?]  
   :is-type?                             [:node precondition/is-type?]	
   :is-assignmentstatement?              [:node precondition/is-assignmentstatement?]    
   :is-methodinvocationstatement?        [:node precondition/is-methodinvocationstatement?] 
   :is-ast?                              [:node precondition/is-ast?]      
   :is-listmember?                       [:node precondition/is-listmember?]   
   :is-methodinvocationexpression?       [:node precondition/is-methodinvocationexpression?] 
   :is-methoddeclaration?                [:node precondition/is-methoddeclaration?]
   :is-variabledeclarationfragment?      [:node precondition/is-variabledeclarationfragment?]
   :is-variabledeclaration?              [:node precondition/is-variabledeclaration?]
   :is-simplename?                       [:node precondition/is-simplename?]
   :is-importlibrary?                    [:node precondition/is-importlibrary?]
   :is-loop?                             [:node precondition/is-loop?]
	})



(defn 
  precondition-function 
  "Returns precondition function of given precondition id."
  [pre-id]
  (fnext (get operator-precondition pre-id)))

(defn 
  precondition-type 
  "Returns precondition type of given precondition id."
  [pre-id]
  (first (get operator-precondition pre-id)))

  
  
  (comment 

  (defn 
  operator-maps
  "Returns map {name function} of all operator."
  []
  (zipmap (operator-ids) (operator-functions)))

  (defn 
  operator-ids-for-transformation
  "Returns all operator ids for transformation."
    []
    (concat 
      (list :introduce-logic-variable
            :add-user-defined-condition)
      (operator-ids-with-type :transform)))

  (defn 
    is-transform-operator?
    [op-id]
    (= (operator-category op-id) :transform)) 
  
  )

  ;; Function apply-operator 
  ;; --------------------------------

  (defn apply-operator
    "Apply operator to snippet, returns new snippet."
    [snippet op-id node args]
    (let [op-func (operator-operator op-id)]
      (cond 
        (= op-id :add-node)
        (op-func snippet node (parsing/parse-string-ast (first args)) (Integer/parseInt (fnext args)))
        (= op-id :replace-node)
      (op-func snippet node (parsing/parse-string-ast (first args)))
      (= op-id :update-logic-conditions)
      (apply operators/update-logic-conditions snippet args)
      (= op-id :introduce-logic-variables-for-snippet)
        (do
          (println "snippet" op-id (first args))
        (apply op-func snippet args))
      :else
        (do
        (println "snippet" op-id (first args))
          (apply op-func snippet node args)))))

  (defn apply-operator-to-snippetgroup
  "Apply operator to group and related snippet inside group, returns new group.
   node can be many, but should be in one snippet."
  [snippetgroup op-id node args]
    (let [snippet (if (or (sequential? node) (.isArray (.getClass node)))
                    (snippetgroup/snippetgroup-snippet-for-node snippetgroup (first node))
                  (snippetgroup/snippetgroup-snippet-for-node snippetgroup node))
          op-func (operator-operator op-id)]
      (if (operand-has-scope? (first (operator-operands op-id))) ;todo: meer argumenten
        (apply op-func snippetgroup node args)
        (cond 
        (nil? snippet)
        (apply operators/update-logic-conditions-to-snippetgroup snippetgroup args)
        (= op-id :introduce-logic-variables-to-group)
        (apply operators/introduce-logic-variables-to-group snippetgroup node args)
        :else
        ;;apply operator to snippet
        (do
          (println "snippet" op-id (first args))
          (let [newsnippet (apply-operator snippet op-id node args)]
            (snippetgroup/snippetgroup-replace-snippet snippetgroup snippet newsnippet)))))))

  (defn apply-operator-to-snippetgrouphistory
  "Apply operator to group history and save the applied operator, returns new group history."
  [snippetgrouphistory op-id node args]
  (let [newgroup (apply-operator-to-snippetgroup
                     (snippetgrouphistory/snippetgrouphistory-current snippetgrouphistory) 
                     op-id node args)
          newgrouphistory (snippetgrouphistory/snippetgrouphistory-update-group snippetgrouphistory newgroup)
          var-node (snippetgrouphistory/snippetgrouphistory-var-for-node snippetgrouphistory node)]
      (snippetgrouphistory/snippetgrouphistory-add-history newgrouphistory op-id var-node args)))
     
  

  ;; Function undo and redo 
  ;; --------------------------------

  (defn undo-operator
  [grouphistory]
  "Undo last applied operator in given snippet group history." 
    (defn undo-operator-rec [grouphistory op-histories]
      (if (empty? op-histories)
        grouphistory
        (let [history (first op-histories)]
          (undo-operator-rec
            (apply-operator-to-snippetgrouphistory 
            grouphistory
            (snippetgrouphistory/history-operator history)
            (snippetgrouphistory/snippetgrouphistory-node-for-var grouphistory (snippetgrouphistory/history-varnode history))
            (snippetgrouphistory/history-args history))
            (rest op-histories)))))
  (let [op-histories (drop-last (snippetgrouphistory/snippetgrouphistory-history grouphistory))
          undo-grouphistory (snippetgrouphistory/snippetgrouphistory-add-undohistory grouphistory)
          new-grouphistory (snippetgrouphistory/reset-snippetgrouphistory undo-grouphistory)]
    (undo-operator-rec new-grouphistory op-histories))) 

  (defn redo-operator
  [grouphistory]
  "Redo last undo operator in given snippet group history." 
    (if (empty? (snippetgrouphistory/snippetgrouphistory-undohistory grouphistory))
      grouphistory 
    (let [redo (snippetgrouphistory/snippetgrouphistory-first-undohistory grouphistory)
          redo-grouphistory (snippetgrouphistory/snippetgrouphistory-remove-undohistory grouphistory)]
      (apply-operator-to-snippetgrouphistory 
          redo-grouphistory
          (snippetgrouphistory/history-operator redo)
          (snippetgrouphistory/snippetgrouphistory-node-for-var redo-grouphistory (snippetgrouphistory/history-varnode redo))
        (snippetgrouphistory/history-args redo)))))
    

  
  
  ;; Operator Information
  ;; --------------------


  (comment
     ;; Following have not been checked


  (def 
  operator-information
  {
     
     :node-deep                                        [:node   
                                                      operators/node-deep
                                                      :is-ast?					          
                                                      :generalization 
                                                      "Allow deep path"
                                                      "Operator with matching strategy :deep \nAllows node as child or nested child of its parent."]
     
   :any-element                                      [:property   
                                                      operators/contains-any-elements
                                                      :none					          
                                                      :generalization 
                                                      "Allow list with any element"
                                                      "Operator with matching strategy  :any\nMatch node with any element."]
   
     :contains-deep                                    [:property   
                                                      operators/contains-deep
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with child+"
                                                      "Operator with matching strategy :child+\nMatch nodelist which contains all elements of snippet nodelist as its childs or nested childs"]
   
     :contains-elements                                [:property   
                                                      operators/contains-elements
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with minimum elements"
                                                      "Operator with matching strategy :contains\nMatch nodelist which contains all elements of snippet nodelist"]
   :contains-elements-with-same-size                 [:property       
                                                      operators/contains-elements-with-same-size                  
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with any element order"
                                                      "Operator with matching strategy :contains-eq-size\nMatch nodelist which contains all elements and has same size with snippet nodelist"]
   :contains-elements-with-relative-order            [:property
                                                      operators/contains-elements-with-relative-order     
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with minimum elements but same order"
                                                      "Operator with matching strategy :contains-eq-order\nMatch nodelist which contains all elements in same order with snippet nodelist"]
   :contains-elements-with-repetition                [:property 
                                                      operators/contains-elements-with-repetition    
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with minimum repetitive elements"
                                                      "Operator with matching strategy :contains-repetition\nMatch nodelist which contains all elements (possible has repetitive elements) with snippet nodelist"]
   :contains-variable-declaration-statement          [:node  
                                                      operators/contains-variable-declaration-statement     
                                                      :is-variabledeclarationstatement?
                                                      :generalization 
                                                      "Allow relax variable declaration statement"
                                                      "Operator with matching strategy :relax-var-dec\nMatch statements which covers all variable declaration fragments in single statement in snippet"]
   :allow-ifstatement-with-else                      [:node   
                                                      operators/allow-ifstatement-with-else  
                                                      :is-ifstatement?  
                                                      :generalization 
                                                      "Allow relax branch"
                                                      "Operator with matching strategy :relax-branch\nMatch ifstatement node with or without else"]
   :allow-subtype                                    [:node
                                                      operators/allow-subtype     
                                                      :is-type?	
                                                      :generalization 
                                                      "Allow relax type"
                                                      "Operator with matching strategy :relax-type\nMatch node with same type or subtype of snippet node"]
   :relax-typeoftype                                 [:node
                                                      operators/relax-typeoftype     
                                                      :is-type?	
                                                      :generalization 
                                                      "Allow relax type of type"
                                                      "Operator with matching strategy :relax-typeoftype\nMatch node (=type) to simple type or parameterized type"]
   :allow-variable-declaration-with-initializer      [:node     
                                                      operators/allow-variable-declaration-with-initializer 
                                                      :is-assignmentstatement?    
                                                      :generalization 
                                                      "Allow relax assignment"
                                                      "Operator with matching strategy :relax-assign\nMatch variable declaration node with same initializer with assignment node in snippet"]
   :allow-relax-loop                                 [:node
                                                      operators/allow-relax-loop
                                                      :is-loop?	
                                                      :generalization 
                                                      "Allow relax loop"
                                                      "Operator with matching strategy :relax-loop\nAllow loop node as for, while or do statement."]
      
   :introduce-logic-variable-of-node-exact           [:node   
                                                      operators/introduce-logic-variable-of-node-exact  
                                                      :none      
                                                      :neutral 
                                                      "Bind logic variable"
                                                      "Operator to introduce new logic variable without removing any it's property values"]
     
   :introduce-logic-variables                        [:node 
                                                      operators/introduce-logic-variables  
                                                      :is-simplename?					
                                                      :generalization 
                                                      "Introduce logic variables"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node"]
   :introduce-logic-variables-to-group               [:node 
                                                      operators/introduce-logic-variables-to-group  
                                                      :is-simplename?					
                                                      :generalization 
                                                      "Introduce logic variables to group"
                                                      "Operator to introduce new logic variables to all nodes in group with same binding with given snippet node"]
   :introduce-logic-variables-with-condition         [:node     
                                                     operators/introduce-logic-variables-with-condition 
                                                      :is-simplename?       	
                                                      :generalization 
                                                      "Introduce logic variables with condition"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node, with additional user logic condition on it"]
   :split-variable-declaration-statement             [:node 
                                                      operators/split-variable-declaration-statement        
                                                      :is-variabledeclarationstatement?
                                                      :refinement 
                                                      "Split variable declaration statement"
                                                      "Operator to split variable declaration fragments into one fragment for each statement"] 
   :inline-method-invocation                         [:node  
                                                      operators/inline-method-invocation          
                                                      :is-methodinvocationstatement? 
                                                      :refinement 
                                                      "Inline method invocation"
                                                      "Operator to replace method invocation with all statements in it's method declaration body"]
   :negated-node                                     [:node   
                                                      operators/negated-node        
                                                      :is-simplename?      
                                                      :refinement 
                                                      "Match negated node"
                                                      "Operator with matching strategy :negated\nMatch all node except given snippet node"]
   :add-node                                         [:property   
                                                      operators/add-node                                          
                                                      :listvalue					          
                                                      :refinement 
                                                      "Add new node"
                                                      "Operator to add new node"]
   :remove-node                                      [:node       
                                                      operators/remove-node                                       
                                                      :is-listmember?   
                                                      :generalization 
                                                      "Remove node"
                                                      "Operator to remove node"]
   :remove-nodes                                     [:node       
                                                      operators/remove-nodes                                       
                                                      :is-listmember?   
                                                      :none 
                                                      "Remove nodes"
                                                      "Operator to remove nodes"]
   :replace-node                                     [:node       
                                                      operators/replace-node                                       
                                                      :is-ast?   
                                                      :refinement 
                                                      "Replace node"
                                                      "Operator to replace node with new node"]
   :change-property-node                             [:property       
                                                      operators/change-property-node                                       
                                                      :primitive-or-null-value   
                                                      :refinement 
                                                      "Change property node"
                                                      "Operator to change value of property node"]
   :match-invocation-declaration                     [:node  
                                                      operators/match-invocation-declaration          
                                                      :is-methodinvocationexpression? 
                                                      :refinement 
                                                      "Refer to method declaration"
                                                      "Operator with matching strategy :method-dec\nMatch method invocation node which has same reference to given method declaration"]
   :match-variable-declaration                       [:node  
                                                      operators/match-variable-declaration          
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Refer to variable declaration"
                                                      "Operator with matching strategy :var-dec\nMatch variable node which has same reference to given variable declaration"]
   :match-variable-samebinding                       [:node  
                                                      operators/match-variable-samebinding    
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Bind to variable"
                                                      "Operator with matching strategy :var-binding\nMatch variable node which has same binding with given snippet node"]
   :match-variable-type                             [:node  
                                                      operators/match-variable-type    
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Match variable type"
                                                      "Operator with matching strategy :var-type\nMatch type of variable node to the given type"]
   :match-variable-typequalifiedname                 [:node  
                                                      operators/match-variable-typequalifiedname
                                                      :is-simplename? 
                                                      :none 
                                                      "Match var-type qualified name"
                                                      "Operator with matching strategy :var-type\nMatch variable with its type qualified name"]
   :match-variable-typequalifiednamestring           [:node  
                                                      operators/match-variable-typequalifiednamestring
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Match variable type with qualified name"
                                                      "Operator with matching strategy :var-qname\nMatch variable with its type qualified name"]
   :match-type-qualifiedname                         [:node  
                                                      operators/match-type-qualifiedname
                                                      :is-type? 
                                                      :none 
                                                      "Match type qualified name"
                                                      "Operator with matching strategy :type-qname\nMatch type with its qualified name"]
   :match-type-qualifiednamestring                   [:node  
                                                      operators/match-type-qualifiednamestring
                                                      :is-type? 
                                                      :refinement 
                                                      "Match type with qualified name"
                                                      "Operator with matching strategy :type-qname\nMatch type with its qualified name"]
   :bind-variables                                   [:node 
                                                      operators/bind-variables  
                                                      :is-simplename?					
                                                      :generalization 
                                                      "Bind variables"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node and bind those variables"]
   :refer-variables-to-variable-declaration          [:node 
                                                      operators/refer-variables-to-variable-declaration
                                                      :is-variabledeclaration?					
                                                      :generalization 
                                                      "Refer variables to variable declarations"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node and refer those variables to its declaration"]
   :add-user-defined-condition                       [:none       
                                                      operators/add-user-defined-condition                                       
                                                      :none   
                                                      :other 
                                                      "Add user-defined condition"
                                                      "Operator to add user-defined condition."]
   :remove-user-defined-condition                    [:none       
                                                      operators/remove-user-defined-condition                                       
                                                      :none   
                                                      :other 
                                                      "Remove user-defined condition"
                                                      "Operator to remove user-defined condition."]
   :introduce-logic-variables-for-snippet            [:node     
                                                      operators/introduce-logic-variables-for-snippet 
                                                      :is-ast?       	
                                                      :none 
                                                      "Introduce logic variables for snippet"
                                                      "Operator to introduce new logic variables to all nodes based on logic variables in the template snippet"]
   :change-name                                      [:node       
                                                      operators/change-name                                       
                                                      :is-simplename?   
                                                      :transform 
                                                      "Change name with rule"
                                                      "Operator to change name with rule.\n Example: \"prefix[part-of-name]suffix\" -> \"add[Name]s\""]
   :t-add-node-after                                 [:node       
                                                      operators/t-add-node-after                                       
                                                      :is-ast?   
                                                      :transform 
                                                      "Add node after"
                                                      "Operator to add node after selected original node."]
   :t-add-node-before                                [:node       
                                                      operators/t-add-node-before                                       
                                                      :is-ast?   
                                                      :transform 
                                                      "Add node before"
                                                      "Operator to add node before selected original node."]
   :t-add-member-node                                [:node       
                                                      operators/t-add-member-node                                       
                                                      :is-ast?   
                                                      :transform 
                                                      "Add node as member"
                                                      "Operator to add node as member of selected original node."]
   :t-replace-node                                   [:node       
                                                      operators/t-replace-node
                                                      :is-ast?   
                                                      :transform 
                                                      "Replace node"
                                                      "Operator to replace node."]
	})


  ;;Operators Arguments
  ;;------------------------------

  (def 
  operator-arguments
  {:introduce-logic-variable                         ["Variable (e.g., ?v)"]
   :introduce-logic-variable-with-info               ["Variable (e.g., ?v)"]
   :introduce-logic-variable-of-node-exact           ["Variable (e.g., ?v)"]
   :introduce-logic-variables                        ["Variable (e.g., ?v)"]
   :introduce-logic-variables-to-group               ["Variable (e.g., ?v)"]
   :introduce-logic-variables-with-condition         ["Variable (e.g., ?v)" 
                                                      "Conditions \n(eg. ((damp.ekeko.jdt.ast/has :identifier ?name ?id)\n      (damp.ekeko.jdt.ast/value-raw ?id \"methodX\"))"]
   :add-node                                         ["New Node (eg. int x = 5;)"
                                                      "Index (eg. 1)"]
   :replace-node                                     ["New Node (eg. int x = 5;)"]
   :change-property-node                             ["New Value (eg. \"methodA\")"]
   :add-logic-conditions                             ["Conditions \n(eg. ((damp.ekeko.jdt.ast/has :identifier ?name ?id)\n      (damp.ekeko.jdt.ast/value-raw ?id \"methodX\"))"]
   :remove-logic-conditions                          ["Conditions \n(eg. ((damp.ekeko.jdt.ast/has :identifier ?name ?id)\n      (damp.ekeko.jdt.ast/value-raw ?id \"methodX\"))"]
   :add-logic-conditions-to-snippetgroup             ["Conditions \n(eg. ((damp.ekeko.jdt.ast/has :identifier ?name ?id)\n      (damp.ekeko.jdt.ast/value-raw ?id \"methodX\"))"]
   :remove-logic-conditions-from-snippetgroup        ["Conditions \n(eg. ((damp.ekeko.jdt.ast/has :identifier ?name ?id)\n      (damp.ekeko.jdt.ast/value-raw ?id \"methodX\"))"]
   :update-logic-conditions                          ["Conditions \n(eg. (damp.ekeko.jdt.ast/has :identifier ?name ?id)\n      (damp.ekeko.jdt.ast/value-raw ?id \"methodX\")"]
   :update-logic-conditions-to-snippetgroup          ["Conditions \n(eg. (damp.ekeko.jdt.ast/has :identifier ?name ?id)\n      (damp.ekeko.jdt.ast/value-raw ?id \"methodX\")"]
   :match-variable-typequalifiednamestring           ["QualifiedName (eg. \"java.util.LinkedList\")"]
   :match-type-qualifiednamestring                   ["QualifiedName (eg. \"java.util.LinkedList\")"]
   :change-name                                      ["Change Rule \n(eg. \"prefix[part-of-name]suffix\""]
   :add-user-defined-condition                       ["Condition \n(eg. (enclosing-class ?class))"]
	})

(def 
  operator-arguments-with-precondition
  {:match-invocation-declaration                     ["Declaration Node"       :is-methoddeclaration?]
     :match-variable-declaration                       ["Declaration Node"       :is-variabledeclaration?]
   :match-variable-samebinding                       ["Variable Node"          :is-simplename?]
   :match-variable-type                              ["Type Node  "            :is-type?]
   :match-variable-typequalifiedname                 ["Qualified Name Node"    :is-importlibrary?]
   :match-type-qualifiedname                         ["Qualified Name Node"    :is-importlibrary?]
   :node-deep                                        ["Parent Node"            :is-ast?]
  })




) ;end of comment


  
;; Operator applicability
;; ----------------------

(defn 
  applicable?
  "Returns true if preconditions for given operator are fulfilled by the node."
  [operator node]
  (let [id (operator-scope operator)
        pre-func (precondition-function id)
        op-type  (precondition-type id)]
    (if (or (= op-type :node)
            (= op-type :property))
      (not (empty?
             (cl/run-nc 1 [?node] 
                       (el/equals node ?node)
                       (pre-func ?node))))
    true)))

(defn
  applicable-operators
  "Filters operators that are applicable to the given node."
  [node]
  (filter
    (fn [operator] 
      (applicable? operator node))
    (registered-operators)))

(defn
  applicable-operators-in-category
  [node cat]
  (filter (fn [operator]
            (= (operator-category operator) cat))
          (applicable-operators node)))
    

;; Operand candidates
;; ------------------


(defn 
  node-possible-nodes
  "Returns list of possible nodes from given precondition and ast root (ASTNode)."
  [ast precondition-id]
  (let [root ast
        pre-func (precondition-function precondition-id)
        op-type  (precondition-type precondition-id)]
    (case op-type 
        ;check astnode : the root itself and all childs
        :node     (concat (damp.ekeko/ekeko [?node] 
                                            (el/equals root ?node)
                                            (pre-func ?node))
                          (damp.ekeko/ekeko [?node] 
                                            (ast/child+ root ?node)
                                            (pre-func ?node)))
        ;check property value-raw of the root and all childs
        :property (concat (damp.ekeko/ekeko [?property] 
                                            (cl/fresh [?node ?keyword] 
                                                   (el/equals root ?node)
                                                   (ast/has ?keyword ?node ?property)
                                                   (pre-func ?property)))
                          (damp.ekeko/ekeko [?property] 
                                            (cl/fresh [?node ?keyword] 
                                                   (ast/child+ root ?node)
                                                   (ast/has ?keyword ?node ?property)
                                                   (pre-func ?property))))
        ;others, return empty list
        '()))) 
    
(defn 
  nodelist-possible-nodes
  "Returns list of possible nodes from given precondition and ast root (nodelist)."
  [ast precondition-id]
  (let [list     (:value ast)
        pre-func (precondition-function precondition-id)
        op-type  (precondition-type precondition-id)]
    (case op-type 
        ;check astnode : the member of root and member of all childs
        :node     (concat (damp.ekeko/ekeko [?node] 
                                            (el/contains list ?node)
                                            (pre-func ?node))
                          (damp.ekeko/ekeko [?node] 
                                            (cl/fresh [?member] 
                                                   (el/contains list ?member)
                                                   (ast/child+ ?member ?node)
                                                   (pre-func ?node))))
        ;check property value-raw of root it self, of property members and of property all childs
        :property (concat (damp.ekeko/ekeko [?property] 
                                            (el/equals ast ?property)
                                            (pre-func ?property))
                          (damp.ekeko/ekeko [?property] 
                                            (cl/fresh [?node ?keyword] 
                                                   (el/contains list ?node)
                                                   (ast/has ?keyword ?node ?property)
                                                   (pre-func ?property)))
                          (damp.ekeko/ekeko [?property] 
                                            (cl/fresh [?node ?keyword ?member] 
                                                   (el/contains list ?member)
                                                   (ast/child+ ?member ?node)
                                                   (ast/has ?keyword ?node ?property)
                                                   (pre-func ?property))))
        ;others, return empty list
        '()))) 

(defn 
  possible-nodes
  "Returns list of possible nodes from given precondition."
  [ast precondition-id]
  (cond 
    (astnode/ast? ast) (node-possible-nodes ast precondition-id)
    (astnode/lstvalue? ast) (nodelist-possible-nodes ast precondition-id) 
    :else nil))

(defn 
  possible-nodes-in-list
  [ast precondition-id]
  (map first (possible-nodes ast precondition-id)))

(defn 
  possible-nodes-in-group
  [snippetgroup pre-id]
  (mapcat (fn [x] (possible-nodes-in-list (:ast x) pre-id)) (snippetgroup/snippetgroup-snippetlist snippetgroup)))

(defn 
  possible-nodes-for-operator
  "Returns list of possible nodes to be applied on to given operator."
  [ast op]
  (possible-nodes-in-list ast (operator-scope op)))

(defn 
  possible-nodes-for-operator-argument
  "Returns list of possible nodes as argument of given operator."
  [ast op]
  (possible-nodes-in-list ast (operand-scope (first (operator-operands op)))))

(defn 
  possible-nodes-for-operator-in-group
  [snippetgroup op]
  (possible-nodes-in-group snippetgroup (operand-scope (first (operator-operands op)))))

(defn 
  possible-nodes-for-operator-argument-in-group
  [snippetgroup op]
  (possible-nodes-in-group snippetgroup (operand-scope (first (operator-operands op)))))




  
  
  
  
  (defn
    register-callbacks 
    []
    (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_APPLY_TO_SNIPPETGROUPHISTORY) apply-operator-to-snippetgrouphistory)
    (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_UNDO) undo-operator)
    (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_REDO) redo-operator)
  
    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_CATEGORIES) registered-categories)
    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATORCATEGORY_DESCRIPTION) category-description)
    
    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_NAME) operator-name)
    
    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_BINDINGS_FOR_OPERANDS) operator-bindings-for-operands)

;    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_ARGUMENT_WITH_PRECONDITION) operator-argument-with-precondition)
    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_DESCRIPTION) operator-description)
 ;   (set! (damp.ekeko.snippets.data.SnippetOperator/FN_OPERATOR_ISTRANSFORM) is-transform-operator?)
 
 
    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_POSSIBLE_NODES_FOR_OPERATOR_ARGUMENT_IN_GROUP) possible-nodes-for-operator-argument-in-group)
    (set! (damp.ekeko.snippets.data.SnippetOperator/FN_IS_OPERATOR) operator?)
    
    
    (set! (damp.ekeko.snippets.gui.OperandBindingDescriptionLabelProvider/FN_BINDING_OPERAND_DESCRIPTION) binding-operand-description)
    (set! (damp.ekeko.snippets.gui.OperandBindingEditingSupport/FN_UPDATE_OPERANDBINDING_VALUE) set-binding-value!)
    (set! (damp.ekeko.snippets.gui.OperandBindingEditingSupport/FN_OPERANDBINDING_VALUE) binding-value)
    

   
 
    )


  (register-callbacks)

