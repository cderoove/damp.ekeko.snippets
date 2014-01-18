(ns 
  ^{:doc "Representation for operators."
    :author "Coen De Roover, Siltvani"}
   damp.ekeko.snippets.operatorsrep
  (:refer-clojure :exclude [== type])
  (:use [clojure.core.logic])
  (:use [damp.ekeko.snippets operators])
  (:require [damp.ekeko.snippets 
             [parsing :as parsing]
             [representation :as representation]]))



;; Informations for Operator
;; -------------------------

; operator-information 
;    {:operator-id1 [:operator-nodetype operator-function precondition-id
;                      :operator-type operator-name operator-description]
;     :operator-id2 ....
;     ...}
; :operator-nodetype -> node, property, snippet, and group
; :operator-type -> generalization, refinement, netral

(declare operator-information)
(declare operator-arguments)
(declare operator-arguments-with-precondition)
(declare operatortype-information)

(defn 
  operator-id 
  "Returns operator function of given map."
  [map]
  (key map))

(defn 
  operator-nodetype 
  "Returns operator node type of given operator id."
  [op-id]
  (first (get operator-information op-id)))

(defn 
  operator-function 
  "Returns operator function of given operator id."
  [op-id]
  (fnext (get operator-information op-id)))

(defn 
  precondition-id 
  "Returns precondition id of given operator id."
  [op-id]
  (first (nnext (get operator-information op-id))))

(defn 
  operator-arguments 
  "Returns operator arguments of given operator id."
  [op-id]
  (get operator-arguments op-id))

(defn 
  argument-precondition-id 
  "Returns argument precondition id of given operator id."
  [op-id]
  (fnext (get operator-arguments-with-precondition op-id)))

(defn 
  operator-argument-with-precondition
  "Returns operator argument with precondition of given operator id."
  [op-id]
  (first (get operator-arguments-with-precondition op-id)))

(defn 
  is-operator-argument-with-precondition?
  [op-id]
  (not (nil? (get operator-arguments-with-precondition op-id))))

(defn 
  operator-type 
  "Returns operator type of given operator id."
  [op-id]
  (fnext (nnext (get operator-information op-id))))

(defn 
  operator-name 
  "Returns operator name of given operator id."
  [op-id]
  (fnext (nnext (next (get operator-information op-id)))))

(defn 
  operator-description 
  "Returns operator description of given operator id."
  [op-id]
  (fnext (nnext (nnext (get operator-information op-id)))))

(defn 
  operatortype-name
  "Returns operator typename."
  [type]
  (get operatortype-information type))

(defn 
  operator-types
  "Returns all operator types."
  []
  (keys operatortype-information))

(defn 
  operator-ids
  "Returns all operator ids."
  []
  (keys operator-information))

(defn 
  operator-names
  "Returns all operator names."
  []
  (map operator-name (operator-ids)))

(defn 
  operator-functions
  "Returns all operator functions."
  []
  (map operator-function (operator-ids)))

(defn 
  operator-ids-with-type
  "Returns all operator names with given type."
  [type]
  (defn process-ids [operators selecteds]
    (if (empty? operators)
      selecteds
      (if (= type (operator-type (first operators)))
        (process-ids (rest operators) (cons (first operators) selecteds))
        (process-ids (rest operators) selecteds))))
  (process-ids (operator-ids) '()))
  
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
  (= (operator-type op-id) :transform)) 
  

;; Function apply-operator 
;; --------------------------------

(defn apply-operator
  "Apply operator to snippet, returns new snippet."
  [snippet op-id node args]
  (let [op-func (operator-function op-id)]
    (cond 
      (= op-id :add-node)
      (op-func snippet node (parsing/parse-string-ast (first args)) (Integer/parseInt (fnext args)))
      (= op-id :replace-node)
      (op-func snippet node (parsing/parse-string-ast (first args)))
      (= op-id :update-logic-conditions)
      (apply update-logic-conditions snippet args)
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
                  (representation/snippetgroup-snippet-for-node snippetgroup (first node))
                  (representation/snippetgroup-snippet-for-node snippetgroup node))
        op-func (operator-function op-id)]
    (if (is-operator-argument-with-precondition? op-id) 
      (apply op-func snippetgroup node args)
      (cond 
        (nil? snippet)
        (apply update-logic-conditions-to-snippetgroup snippetgroup args)
        (= op-id :introduce-logic-variables-to-group)
        (apply introduce-logic-variables-to-group snippetgroup node args)
        :else
        ;;apply operator to snippet
        (do
          (println "snippet" op-id (first args))
          (let [newsnippet (apply-operator snippet op-id node args)]
            (representation/snippetgroup-replace-snippet snippetgroup snippet newsnippet)))))))

(defn apply-operator-to-snippetgrouphistory
  "Apply operator to group history and save the applied operator, returns new group history."
  [snippetgrouphistory op-id node args]
  (let [newgroup (apply-operator-to-snippetgroup
                   (representation/snippetgrouphistory-current snippetgrouphistory) 
                   op-id node args)
        newgrouphistory (representation/snippetgrouphistory-update-group snippetgrouphistory newgroup)
        var-node (representation/snippetgrouphistory-var-for-node snippetgrouphistory node)]
    (representation/snippetgrouphistory-add-history newgrouphistory op-id var-node args)))
     

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
            (representation/history-operator history)
            (representation/snippetgrouphistory-node-for-var grouphistory (representation/history-varnode history))
            (representation/history-args history))
          (rest op-histories)))))
  (let [op-histories (drop-last (representation/snippetgrouphistory-history grouphistory))
        undo-grouphistory (representation/snippetgrouphistory-add-undohistory grouphistory)
        new-grouphistory (representation/reset-snippetgrouphistory undo-grouphistory)]
    (undo-operator-rec new-grouphistory op-histories))) 

(defn redo-operator
  [grouphistory]
  "Redo last undo operator in given snippet group history." 
  (if (empty? (representation/snippetgrouphistory-undohistory grouphistory))
    grouphistory 
    (let [redo (representation/snippetgrouphistory-first-undohistory grouphistory)
          redo-grouphistory (representation/snippetgrouphistory-remove-undohistory grouphistory)]
      (apply-operator-to-snippetgrouphistory 
        redo-grouphistory
        (representation/history-operator redo)
        (representation/snippetgrouphistory-node-for-var redo-grouphistory (representation/history-varnode redo))
        (representation/history-args redo)))))
    

;; Operator Informations
;; -----------------------------

(def 
  operator-information
  {:node-deep                                        [:node   
                                                      node-deep
                                                      :is-ast?					          
                                                      :generalization 
                                                      "Allow deep path"
                                                      "Operator with matching strategy :deep \nAllows node as child or nested child of its parent."]
   :any-element                                      [:property   
                                                      contains-any-elements
                                                      :none					          
                                                      :generalization 
                                                      "Allow list with any element"
                                                      "Operator with matching strategy  :any\nMatch node with any element."]
   :contains-deep                                    [:property   
                                                      contains-deep
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with child+"
                                                      "Operator with matching strategy :child+\nMatch nodelist which contains all elements of snippet nodelist as its childs or nested childs"]
   :contains-elements                                [:property   
                                                      contains-elements
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with minimum elements"
                                                      "Operator with matching strategy :contains\nMatch nodelist which contains all elements of snippet nodelist"]
   :contains-elements-with-same-size                 [:property       
                                                      contains-elements-with-same-size                  
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with any element order"
                                                      "Operator with matching strategy :contains-eq-size\nMatch nodelist which contains all elements and has same size with snippet nodelist"]
   :contains-elements-with-relative-order            [:property
                                                      contains-elements-with-relative-order     
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with minimum elements but same order"
                                                      "Operator with matching strategy :contains-eq-order\nMatch nodelist which contains all elements in same order with snippet nodelist"]
   :contains-elements-with-repetition                [:property 
                                                      contains-elements-with-repetition    
                                                      :listvalue					          
                                                      :generalization 
                                                      "Allow list with minimum repetitive elements"
                                                      "Operator with matching strategy :contains-repetition\nMatch nodelist which contains all elements (possible has repetitive elements) with snippet nodelist"]
   :contains-variable-declaration-statement          [:node  
                                                      contains-variable-declaration-statement     
                                                      :is-variabledeclarationstatement?
                                                      :generalization 
                                                      "Allow relax variable declaration statement"
                                                      "Operator with matching strategy :relax-var-dec\nMatch statements which covers all variable declaration fragments in single statement in snippet"]
   :allow-ifstatement-with-else                      [:node   
                                                      allow-ifstatement-with-else  
                                                      :is-ifstatement?  
                                                      :generalization 
                                                      "Allow relax branch"
                                                      "Operator with matching strategy :relax-branch\nMatch ifstatement node with or without else"]
   :allow-subtype                                    [:node
                                                      allow-subtype     
                                                      :is-type?	
                                                      :generalization 
                                                      "Allow relax type"
                                                      "Operator with matching strategy :relax-type\nMatch node with same type or subtype of snippet node"]
   :relax-typeoftype                                 [:node
                                                      relax-typeoftype     
                                                      :is-type?	
                                                      :generalization 
                                                      "Allow relax type of type"
                                                      "Operator with matching strategy :relax-typeoftype\nMatch node (=type) to simple type or parameterized type"]
   :allow-variable-declaration-with-initializer      [:node     
                                                      allow-variable-declaration-with-initializer 
                                                      :is-assignmentstatement?    
                                                      :generalization 
                                                      "Allow relax assignment"
                                                      "Operator with matching strategy :relax-assign\nMatch variable declaration node with same initializer with assignment node in snippet"]
   :allow-relax-loop                                 [:node
                                                      allow-relax-loop
                                                      :is-loop?	
                                                      :generalization 
                                                      "Allow relax loop"
                                                      "Operator with matching strategy :relax-loop\nAllow loop node as for, while or do statement."]
   :introduce-logic-variable                         [:node  
                                                      introduce-logic-variable  
                                                      :none                 
                                                      :generalization 
                                                      "Introduce logic variable"
                                                      "Operator to introduce new logic variable and remove all it's property values"]
   :introduce-logic-variable-by-random-var           [:node  
                                                      introduce-logic-variable-by-random-var  
                                                      :is-simplename?                 
                                                      :generalization 
                                                      "Introduce logic variable by random variable"
                                                      "Operator to introduce new logic variable and remove all it's property values"]
   :introduce-logic-variable-with-info               [:node  
                                                      introduce-logic-variable-with-info  
                                                      :none                 
                                                      :generalization 
                                                      "Introduce logic variable with information"
                                                      "Operator to introduce new logic variable and remove all it's property values and add it as result in the query"]
   :introduce-logic-variable-of-node-exact           [:node   
                                                      introduce-logic-variable-of-node-exact  
                                                      :none      
                                                      :netral 
                                                      "Bind logic variable"
                                                      "Operator to introduce new logic variable without removing any it's property values"]
   :introduce-logic-variables                        [:node 
                                                      introduce-logic-variables  
                                                      :is-simplename?					
                                                      :generalization 
                                                      "Introduce logic variables"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node"]
   :introduce-logic-variables-to-group               [:node 
                                                      introduce-logic-variables-to-group  
                                                      :is-simplename?					
                                                      :generalization 
                                                      "Introduce logic variables to group"
                                                      "Operator to introduce new logic variables to all nodes in group with same binding with given snippet node"]
   :introduce-logic-variables-with-condition         [:node     
                                                      introduce-logic-variables-with-condition 
                                                      :is-simplename?       	
                                                      :generalization 
                                                      "Introduce logic variables with condition"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node, with additional user logic condition on it"]
   :split-variable-declaration-statement             [:node 
                                                      split-variable-declaration-statement        
                                                      :is-variabledeclarationstatement?
                                                      :refinement 
                                                      "Split variable declaration statement"
                                                      "Operator to split variable declaration fragments into one fragment for each statement"] 
   :inline-method-invocation                         [:node  
                                                      inline-method-invocation          
                                                      :is-methodinvocationstatement? 
                                                      :refinement 
                                                      "Inline method invocation"
                                                      "Operator to replace method invocation with all statements in it's method declaration body"]
   :negated-node                                     [:node   
                                                      negated-node        
                                                      :is-simplename?      
                                                      :refinement 
                                                      "Match negated node"
                                                      "Operator with matching strategy :negated\nMatch all node except given snippet node"]
   :add-node                                         [:property   
                                                      add-node                                          
                                                      :listvalue					          
                                                      :refinement 
                                                      "Add new node"
                                                      "Operator to add new node"]
   :remove-node                                      [:node       
                                                      remove-node                                       
                                                      :is-listmember?   
                                                      :generalization 
                                                      "Remove node"
                                                      "Operator to remove node"]
   :remove-nodes                                     [:node       
                                                      remove-nodes                                       
                                                      :is-listmember?   
                                                      :none 
                                                      "Remove nodes"
                                                      "Operator to remove nodes"]
   :replace-node                                     [:node       
                                                      replace-node                                       
                                                      :is-ast?   
                                                      :refinement 
                                                      "Replace node"
                                                      "Operator to replace node with new node"]
   :change-property-node                             [:property       
                                                      change-property-node                                       
                                                      :primitive-or-null-value   
                                                      :refinement 
                                                      "Change property node"
                                                      "Operator to change value of property node"]
   :match-invocation-declaration                     [:node  
                                                      match-invocation-declaration          
                                                      :is-methodinvocationexpression? 
                                                      :refinement 
                                                      "Refer to method declaration"
                                                      "Operator with matching strategy :method-dec\nMatch method invocation node which has same reference to given method declaration"]
   :match-variable-declaration                       [:node  
                                                      match-variable-declaration          
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Refer to variable declaration"
                                                      "Operator with matching strategy :var-dec\nMatch variable node which has same reference to given variable declaration"]
   :match-variable-samebinding                       [:node  
                                                      match-variable-samebinding    
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Bind to variable"
                                                      "Operator with matching strategy :var-binding\nMatch variable node which has same binding with given snippet node"]
   :match-variable-type                             [:node  
                                                      match-variable-type    
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Match variable type"
                                                      "Operator with matching strategy :var-type\nMatch type of variable node to the given type"]
   :match-variable-typequalifiedname                 [:node  
                                                      match-variable-typequalifiedname
                                                      :is-simplename? 
                                                      :none 
                                                      "Match var-type qualified name"
                                                      "Operator with matching strategy :var-type\nMatch variable with its type qualified name"]
   :match-variable-typequalifiednamestring           [:node  
                                                      match-variable-typequalifiednamestring
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Match variable type with qualified name"
                                                      "Operator with matching strategy :var-qname\nMatch variable with its type qualified name"]
   :match-type-qualifiedname                         [:node  
                                                      match-type-qualifiedname
                                                      :is-type? 
                                                      :none 
                                                      "Match type qualified name"
                                                      "Operator with matching strategy :type-qname\nMatch type with its qualified name"]
   :match-type-qualifiednamestring                   [:node  
                                                      match-type-qualifiednamestring
                                                      :is-type? 
                                                      :refinement 
                                                      "Match type with qualified name"
                                                      "Operator with matching strategy :type-qname\nMatch type with its qualified name"]
   :bind-variables                                   [:node 
                                                      bind-variables  
                                                      :is-simplename?					
                                                      :generalization 
                                                      "Bind variables"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node and bind those variables"]
   :refer-variables-to-variable-declaration          [:node 
                                                      refer-variables-to-variable-declaration
                                                      :is-variabledeclaration?					
                                                      :generalization 
                                                      "Refer variables to variable declarations"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node and refer those variables to its declaration"]
   :add-user-defined-condition                       [:none       
                                                      add-user-defined-condition                                       
                                                      :none   
                                                      :other 
                                                      "Add user-defined condition"
                                                      "Operator to add user-defined condition."]
   :remove-user-defined-condition                    [:none       
                                                      remove-user-defined-condition                                       
                                                      :none   
                                                      :other 
                                                      "Remove user-defined condition"
                                                      "Operator to remove user-defined condition."]
   :introduce-logic-variables-for-snippet            [:node     
                                                      introduce-logic-variables-for-snippet 
                                                      :is-ast?       	
                                                      :none 
                                                      "Introduce logic variables for snippet"
                                                      "Operator to introduce new logic variables to all nodes based on logic variables in the template snippet"]
   :change-name                                      [:node       
                                                      change-name                                       
                                                      :is-simplename?   
                                                      :transform 
                                                      "Change name with rule"
                                                      "Operator to change name with rule.\n Example: \"prefix[part-of-name]suffix\" -> \"add[Name]s\""]
   :t-add-node-after                                 [:node       
                                                      t-add-node-after                                       
                                                      :is-ast?   
                                                      :transform 
                                                      "Add node after"
                                                      "Operator to add node after selected original node."]
   :t-add-node-before                                [:node       
                                                      t-add-node-before                                       
                                                      :is-ast?   
                                                      :transform 
                                                      "Add node before"
                                                      "Operator to add node before selected original node."]
   :t-add-member-node                                [:node       
                                                      t-add-member-node                                       
                                                      :is-ast?   
                                                      :transform 
                                                      "Add node as member"
                                                      "Operator to add node as member of selected original node."]
   :t-replace-node                                   [:node       
                                                      t-replace-node
                                                      :is-ast?   
                                                      :transform 
                                                      "Replace node"
                                                      "Operator to replace node."]
	})


;;Operators Arguments
;;------------------------------

(def 
  operator-arguments
  {:introduce-logic-variable                         ["Logic Variable (eg. ?v)"]
   :introduce-logic-variable-with-info               ["Logic Variable (eg. ?v)"]
   :introduce-logic-variable-of-node-exact           ["Logic Variable (eg. ?v)"]
   :introduce-logic-variables                        ["Logic Variable (eg. ?v)"]
   :introduce-logic-variables-to-group               ["Logic Variable (eg. ?v)"]
   :introduce-logic-variables-with-condition         ["Logic Variable (eg. ?v)" 
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

;;Operators type
;;------------------------------
(def
  operatortype-information
  {:generalization  "Generalization" 
   :refinement      "Refinement"
   :netral          "Neutral"
   :other           "Other"
   })


;;Operators for searchspace
;;------------------------------

(def 
  searchspace-operators
  {:allow-relax-loop                                  allow-relax-loop
   :allow-ifstatement-with-else                       allow-ifstatement-with-else  
   :allow-subtype                                     allow-subtype
   :relax-typeoftype                                  relax-typeoftype
   :negated-node                                      negated-node
	})

(defn searchspace-operator-ids [] (keys searchspace-operators))
(defn searchspace-refinement-operator-ids [] 
  (keys (filter (fn [x] (= (operator-type (first x)) :refinement)) searchspace-operators)))
(defn searchspace-generalization-operator-ids [] 
  (keys (filter (fn [x] (= (operator-type (first x)) :generalization)) searchspace-operators)))
