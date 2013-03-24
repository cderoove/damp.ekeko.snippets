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

;; Function apply-operator 
;; --------------------------------

(defn apply-operator
  "Apply operator to snippet, returns new snippet."
  [snippet op-id node args]
  (let [op-func (operator-function op-id)]
    (cond 
      (= op-id :add-node)
      (op-func snippet node (parsing/parse-string-ast (first args)) (Integer/parseInt (fnext args)))
      (= op-id :update-logic-conditions)
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
      (if (nil? snippet)  
        ;;apply operator (update-logic-conditions-to-snippetgroup) to group
        (apply update-logic-conditions-to-snippetgroup snippetgroup args)
        ;;apply operator to snippet
        (do
          (let [newsnippet (apply-operator snippet op-id node args)]
            (representation/snippetgroup-replace-snippet snippetgroup snippet newsnippet)))))))

(defn apply-operator-to-snippetgrouphistory
  "Apply operator to group history and save the applied operator, returns new group history."
  [snippetgrouphistory op-id node args]
  (let [newgroup (apply-operator-to-snippetgroup
                   (representation/snippetgrouphistory-current snippetgrouphistory) 
                   op-id node args)
        newgrouphistory (representation/snippetgrouphistory-update-group snippetgrouphistory newgroup)]
    (representation/snippetgrouphistory-add-history newgrouphistory op-id node args)))
     

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
            (representation/history-node history)
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
        (representation/history-node redo)
        (representation/history-args redo)))))
    

;; Operator Informations
;; -----------------------------

(def 
  operator-information
  {:contains-elements-with-same-size                 [:property       
                                                      contains-elements-with-same-size                  
                                                      :listvalue					          
                                                      :generalization 
                                                      "contains-elements-with-same-size"
                                                      "Desc"]
   :contains-elements                                [:property   
                                                      contains-elements
                                                      :listvalue					          
                                                      :generalization 
                                                      "contains-elements"
                                                      "Desc"]
   :contains-elements-with-relative-order            [:property
                                                      contains-elements-with-relative-order     
                                                      :listvalue					          
                                                      :generalization 
                                                      "contains-elements-with-relative-order"
                                                      "Desc"]
   :contains-elements-with-repetition                [:property 
                                                      contains-elements-with-repetition    
                                                      :listvalue					          
                                                      :generalization 
                                                      "contains-elements-with-repetition"
                                                      "Desc"]
   :split-variable-declaration-statement             [:node 
                                                      split-variable-declaration-statement        
                                                      :is-variabledeclarationstatement?
                                                      :refinement 
                                                      "split-variable-declaration-statement"
                                                      "Desc"]
   :contains-variable-declaration-statement          [:node  
                                                      contains-variable-declaration-statement     
                                                      :is-variabledeclarationstatement?
                                                      :generalization 
                                                      "contains-variable-declaration-statement"
                                                      "Desc"]
   :allow-ifstatement-with-else                      [:node   
                                                      allow-ifstatement-with-else  
                                                      :is-ifstatement?  
                                                      :generalization 
                                                      "allow-ifstatement-with-else"
                                                      "Desc"]
   :allow-subtype-on-variable-declaration            [:node
                                                      allow-subtype-on-variable-declaration     
                                                      :is-type?	
                                                      :generalization 
                                                      "allow-subtype-on-variable-declaration"
                                                      "Desc"]
   :allow-subtype-on-class-declaration-extends       [:node   
                                                      allow-subtype-on-class-declaration-extends   
                                                      :is-type?     	
                                                      :generalization 
                                                      "allow-subtype-on-class-declaration-extends"
                                                      "Desc"]
   :allow-variable-declaration-with-initializer      [:node     
                                                      allow-variable-declaration-with-initializer 
                                                      :is-assignmentstatement?    
                                                      :generalization 
                                                      "allow-variable-declaration-with-initializer"
                                                      "Desc"]
   :inline-method-invocation                         [:node  
                                                      inline-method-invocation          
                                                      :is-methodinvocationstatement? 
                                                      :refinement 
                                                      "inline-method-invocation"
                                                      "Desc"]
   :negated-node                                     [:node   
                                                      negated-node        
                                                      :is-ast?      
                                                      :refinement 
                                                      "negated-node"
                                                      "Desc"]
   :introduce-logic-variable                         [:node  
                                                      introduce-logic-variable  
                                                      :is-ast?                 
                                                      :generalization 
                                                      "introduce-logic-variable"
                                                      "Desc"]
   :introduce-logic-variable-of-node-exact           [:node   
                                                      introduce-logic-variable-of-node-exact  
                                                      :is-ast?      
                                                      :netral 
                                                      "introduce-logic-variable-of-node-exact"
                                                      "Desc"]
   :introduce-logic-variables                        [:node 
                                                      introduce-logic-variables  
                                                      :is-ast?					
                                                      :generalization 
                                                      "introduce-logic-variables"
                                                      "Desc"]
   :introduce-logic-variables-with-condition         [:node     
                                                      introduce-logic-variables-with-condition 
                                                      :is-ast?       	
                                                      :generalization 
                                                      "introduce-logic-variables-with-condition"
                                                      "Desc"]
   :add-node                                         [:property   
                                                      add-node                                          
                                                      :listvalue					          
                                                      :refinement 
                                                      "add-node"
                                                      "Desc"]
   :remove-node                                      [:node       
                                                      remove-node                                       
                                                      :is-listmember?   
                                                      :generalization 
                                                      "remove-node"
                                                      "Desc"]
   :match-invocation-declaration                     [:node  
                                                      match-invocation-declaration          
                                                      :is-methodinvocationexpression? 
                                                      :refinement 
                                                      "match-invocation-declaration"
                                                      "Desc"]
   :match-variable-declaration                       [:node  
                                                      match-variable-declaration          
                                                      :is-simplename? 
                                                      :refinement 
                                                      "match-variable-declaration"
                                                      "Desc"]
   :match-variable-samebinding                       [:node  
                                                      match-variable-samebinding    
                                                      :is-simplename? 
                                                      :refinement 
                                                      "match-variable-samebinding"
                                                      "Desc"]
	})


;;Operators Arguments
;;------------------------------

(def 
  operator-arguments
  {:introduce-logic-variable                         ["Logic Variable (eg. ?v)"]
   :introduce-logic-variable-of-node-exact           ["Logic Variable (eg. ?v)"]
   :introduce-logic-variables-with-condition         ["Logic Variable (eg. ?v)" 
                                                      "Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :add-node                                         ["New Node (eg. int x = 5;)"
                                                      "Index (eg. 1)"]
   :add-logic-conditions                             ["Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :remove-logic-conditions                          ["Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :add-logic-conditions-to-snippetgroup             ["Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :remove-logic-conditions-from-snippetgroup        ["Conditions \n(eg. ((damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\"))"]
   :update-logic-conditions                          ["Conditions \n(eg. (damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\")"]
   :update-logic-conditions-to-snippetgroup          ["Conditions \n(eg. (damp.ekeko.jdt.reification/has :identifier ?name ?id)\n      (damp.ekeko.jdt.reification/value-raw ?id \"methodX\")"]
	})

(def 
  operator-arguments-with-precondition
  {:match-invocation-declaration                     ["Declaration Node" :is-methoddeclaration?]
   :match-variable-declaration                       ["Declaration Node" :is-variabledeclarationfragment?]
   :match-variable-samebinding                       ["Variable Node"    :is-simplename?]
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
