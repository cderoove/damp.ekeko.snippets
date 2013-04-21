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
  (list :add-node :remove-node 
        :replace-node :change-property-node 
        :introduce-logic-variable
        :introduce-logic-variables-for-snippet)) 

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
      (apply op-func snippet args)
      :else
      (apply op-func snippet node args))))

(defn apply-operator-to-snippetgroup
  "Apply operator to group and related snippet inside group, returns new group."
  [snippetgroup op-id node args]
  (let [snippet (representation/snippetgroup-snippet-for-node snippetgroup node)
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
  {:contains-elements                                [:property   
                                                      contains-elements
                                                      :listvalue					          
                                                      :generalization 
                                                      "Match list with contains elements"
                                                      "Operator with matching strategy :list contains-elements\nMatch nodelist which contains all elements of snippet nodelist"]
   :contains-elements-with-same-size                 [:property       
                                                      contains-elements-with-same-size                  
                                                      :listvalue					          
                                                      :generalization 
                                                      "Match list with same size"
                                                      "Operator with matching strategy :list same-size\nMatch nodelist which contains all elements and has same size with snippet nodelist"]
   :contains-elements-with-relative-order            [:property
                                                      contains-elements-with-relative-order     
                                                      :listvalue					          
                                                      :generalization 
                                                      "Match list with relative order"
                                                      "Operator with matching strategy :list relative-order\nMatch nodelist which contains all elements in same order with snippet nodelist"]
   :contains-elements-with-repetition                [:property 
                                                      contains-elements-with-repetition    
                                                      :listvalue					          
                                                      :generalization 
                                                      "Match list with elements repetition"
                                                      "Operator with matching strategy :list elements-repetition\nMatch nodelist which contains all elements (possible has repetitive elements) with snippet nodelist"]
   :split-variable-declaration-statement             [:node 
                                                      split-variable-declaration-statement        
                                                      :is-variabledeclarationstatement?
                                                      :refinement 
                                                      "Split variable declaration fragments"
                                                      "Operator to split variable declaration fragments into one fragment for each statement"] 
   :contains-variable-declaration-statement          [:node  
                                                      contains-variable-declaration-statement     
                                                      :is-variabledeclarationstatement?
                                                      :generalization 
                                                      "Match variable declaration statements"
                                                      "Operator with matching strategy :dec-stat\nMatch statements which covers all variable declaration fragments in single statement in snippet"]
   :allow-ifstatement-with-else                      [:node   
                                                      allow-ifstatement-with-else  
                                                      :is-ifstatement?  
                                                      :generalization 
                                                      "Match branch with/without else"
                                                      "Operator with matching strategy :relax-branch\nMatch ifstatement node with or without else"]
   :allow-subtype-on-variable-declaration            [:node
                                                      allow-subtype-on-variable-declaration     
                                                      :is-type?	
                                                      :generalization 
                                                      "Match subtype on variable declaration"
                                                      "Operator with matching strategy :subtype\nMatch node with same type or subtype of snippet node"]
   :allow-subtype-on-class-declaration-extends       [:node   
                                                      allow-subtype-on-class-declaration-extends   
                                                      :is-type?     	
                                                      :generalization 
                                                      "Match subtype on class declaration extends"
                                                      "Operator with matching strategy :subtype\nMatch node with same type or subtype of snippet node"]
   :allow-variable-declaration-with-initializer      [:node     
                                                      allow-variable-declaration-with-initializer 
                                                      :is-assignmentstatement?    
                                                      :generalization 
                                                      "Match variable declaration with initializer"
                                                      "Operator with matching strategy :dec-init\nMatch variable declaration node with same initializer with assignment node in snippet"]
   :inline-method-invocation                         [:node  
                                                      inline-method-invocation          
                                                      :is-methodinvocationstatement? 
                                                      :refinement 
                                                      "Inline method invocation"
                                                      "Operator to replace method invocation with all statements in it's method declaration body"]
   :negated-node                                     [:node   
                                                      negated-node        
                                                      :is-ast?      
                                                      :refinement 
                                                      "Match negated node"
                                                      "Operator with matching strategy :negated\nMatch all node except given snippet node"]
   :introduce-logic-variable                         [:node  
                                                      introduce-logic-variable  
                                                      :is-ast?                 
                                                      :generalization 
                                                      "Introduce logic variable"
                                                      "Operator to introduce new logic variable and remove all it's property values"]
   :introduce-logic-variable-with-info               [:node  
                                                      introduce-logic-variable-with-info  
                                                      :is-ast?                 
                                                      :generalization 
                                                      "Introduce logic variable with information"
                                                      "Operator to introduce new logic variable and remove all it's property values and add it as result in the query"]
   :introduce-logic-variable-of-node-exact           [:node   
                                                      introduce-logic-variable-of-node-exact  
                                                      :is-ast?      
                                                      :netral 
                                                      "Introduce logic variable as information"
                                                      "Operator to introduce new logic variable without removing any it's property values"]
   :introduce-logic-variables                        [:node 
                                                      introduce-logic-variables  
                                                      :is-ast?					
                                                      :generalization 
                                                      "Introduce logic variables"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node"]
   :introduce-logic-variables-to-group               [:node 
                                                      introduce-logic-variables-to-group  
                                                      :is-ast?					
                                                      :generalization 
                                                      "Introduce logic variables to group"
                                                      "Operator to introduce new logic variables to all nodes in group with same binding with given snippet node"]
   :introduce-logic-variables-with-condition         [:node     
                                                      introduce-logic-variables-with-condition 
                                                      :is-ast?       	
                                                      :generalization 
                                                      "Introduce logic variables with condition"
                                                      "Operator to introduce new logic variables to all nodes with same binding with given snippet node, with additional user logic condition on it"]
   :introduce-logic-variables-for-snippet            [:node     
                                                      introduce-logic-variables-for-snippet 
                                                      :is-ast?       	
                                                      :none 
                                                      "Introduce logic variables for snippet"
                                                      "Operator to introduce new logic variables to all nodes based on logic variables in the template snippet"]
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
                                                      "Match invocation declaration"
                                                      "Operator with matching strategy :method-dec\nMatch method invocation node which has same reference to given method declaration"]
   :match-variable-declaration                       [:node  
                                                      match-variable-declaration          
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Match variable declaration"
                                                      "Operator with matching strategy :var-dec\nMatch variable node which has same reference to given variable declaration"]
   :match-variable-samebinding                       [:node  
                                                      match-variable-samebinding    
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Match variable binding"
                                                      "Operator with matching strategy :var-binding\nMatch variable node which has same binding with given snippet node"]
   :match-variable-typequalifiedname                 [:node  
                                                      match-variable-typequalifiedname
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Match var-type qualified name"
                                                      "Operator with matching strategy :var-type\nMatch variable with its type qualified name"]
   :match-variable-typequalifiednamestring           [:node  
                                                      match-variable-typequalifiednamestring
                                                      :is-simplename? 
                                                      :refinement 
                                                      "Match var-type qualified name string"
                                                      "Operator with matching strategy :var-typename\nMatch variable with its type qualified name"]
   :match-type-qualifiedname                         [:node  
                                                      match-type-qualifiedname
                                                      :is-type? 
                                                      :refinement 
                                                      "Match type qualified name"
                                                      "Operator with matching strategy :type-qname\nMatch type with its qualified name"]
   :match-type-qualifiednamestring                   [:node  
                                                      match-type-qualifiednamestring
                                                      :is-type? 
                                                      :refinement 
                                                      "Match type qualified name string"
                                                      "Operator with matching strategy :type-qnames\nMatch type with its qualified name"]
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
                                                      "Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :add-node                                         ["New Node (eg. int x = 5;)"
                                                      "Index (eg. 1)"]
   :replace-node                                     ["New Node (eg. int x = 5;)"]
   :change-property-node                             ["New Value (eg. \"methodA\")"]
   :add-logic-conditions                             ["Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :remove-logic-conditions                          ["Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :add-logic-conditions-to-snippetgroup             ["Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :remove-logic-conditions-from-snippetgroup        ["Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :update-logic-conditions                          ["Conditions \n(eg. (damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\")"]
   :update-logic-conditions-to-snippetgroup          ["Conditions \n(eg. (damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\")"]
   :match-variable-typequalifiednamestring           ["QualifiedName (eg. \"java.util.LinkedList\")"]
   :match-type-qualifiednamestring                   ["QualifiedName (eg. \"java.util.LinkedList\")"]
	})

(def 
  operator-arguments-with-precondition
  {:match-invocation-declaration                     ["Declaration Node"       :is-methoddeclaration?]
   :match-variable-declaration                       ["Declaration Node"       :is-variabledeclarationfragment?]
   :match-variable-samebinding                       ["Variable Node"          :is-simplename?]
   :match-variable-typequalifiedname                 ["Qualified Name Node"    :is-importlibrary?]
   :match-type-qualifiedname                         ["Qualified Name Node"    :is-importlibrary?]
  })

;;Operators type
;;------------------------------
(def
  operatortype-information
  {:generalization  "Generalization" 
   :refinement      "Refinement"
   :netral          "Netral"
  })


;;Operators for searchspace
;;------------------------------

(def 
  searchspace-operators
  {:contains-elements-with-same-size                  contains-elements-with-same-size                  
   :contains-elements                                 contains-elements
   :contains-elements-with-relative-order             contains-elements-with-relative-order     
   :contains-elements-with-repetition                 contains-elements-with-repetition    
   :split-variable-declaration-statement              split-variable-declaration-statement        
   :contains-variable-declaration-statement           contains-variable-declaration-statement     
   :allow-ifstatement-with-else                       allow-ifstatement-with-else  
   :allow-subtype-on-variable-declaration             allow-subtype-on-variable-declaration     
   :allow-subtype-on-class-declaration-extends        allow-subtype-on-class-declaration-extends   
   :allow-variable-declaration-with-initializer       allow-variable-declaration-with-initializer 
   :inline-method-invocation                          inline-method-invocation          
   :negated-node                                      negated-node        
	})
