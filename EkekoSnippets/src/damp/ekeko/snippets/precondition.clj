(ns
  ^{:doc "Precondition check for operators."
    :author "Coen De Roover, Siltvani"}
   damp.ekeko.snippets.precondition
  (:refer-clojure :exclude [== type])
  (:use [clojure.core.logic])
  (:use [damp.ekeko logic])
  (:use [damp.ekeko.jdt astnode basic reification])
  (:use [damp.ekeko.snippets operators representation]))


;; Precondition for Operator
;; -------------------------

; Datatype representing relation between operator and precondition function.
; operator-information 
;    {:operator-id1 [:operator-nodetype operator-function precondition-fuction
;                      :operator-nodetype operator-id operator-description]
;     :operator-id2 ....
;     ...}
; :operator-nodetype -> node, property, snippet, and group
; :operator-nodetype -> generalization, refinement, netral

(declare operator-information)
(declare operator-arguments)
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
  operator-arguments 
  "Returns operator arguments of given operator id."
  [op-id]
  (get operator-arguments op-id))

(defn 
  precondition-function 
  "Returns precondition function of given operator id."
  [op-id]
  (first (nnext (get operator-information op-id))))

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

;; Applicable Operator for snippet
;; ----------------------------------

(defn 
  safe-operator-for-node?
  "Returns true if precondition of given operator is fulfilled by the node."
  [operator-id node]
  (let [pre-func (precondition-function operator-id)
        op-type  (operator-nodetype operator-id)]
    (if (contains? (set '(:node :property)) op-type) 
      (not (empty?
             (damp.ekeko/ekeko [?node] 
                               (equals node ?node)
                               (pre-func ?node))))
      true)))

(defn 
  node-possible-nodes-for-operator
  "Returns list of possible nodes to be applied on, from given ast root (ASTNode)."
  [ast operator-id]
  (let [root ast
        pre-func (precondition-function operator-id)
        op-type  (operator-nodetype operator-id)]
    (case op-type 
        ;check astnode : the root itself and all childs
        :node     (concat (damp.ekeko/ekeko [?node] 
                                            (equals root ?node)
                                            (pre-func ?node))
                          (damp.ekeko/ekeko [?node] 
                                            (child+ root ?node)
                                            (pre-func ?node)))
        ;check property value-raw of the root and all childs
        :property (concat (damp.ekeko/ekeko [?property] 
                                            (fresh [?node ?keyword] 
                                                   (equals root ?node)
                                                   (has ?keyword ?node ?property)
                                                   (pre-func ?property)))
                          (damp.ekeko/ekeko [?property] 
                                            (fresh [?node ?keyword] 
                                                   (child+ root ?node)
                                                   (has ?keyword ?node ?property)
                                                   (pre-func ?property))))
        ;others, return empty list
        '()))) 
    
(defn 
  nodelist-possible-nodes-for-operator
  "Returns list of possible nodes to be applied on from given ast root (nodelist)."
  [ast operator-id]
  (let [list     (:value ast)
        pre-func (precondition-function operator-id)
        op-type  (operator-nodetype operator-id)]
    (case op-type 
        ;check astnode : the member of root and member of all childs
        :node     (concat (damp.ekeko/ekeko [?node] 
                                            (contains list ?node)
                                            (pre-func ?node))
                          (damp.ekeko/ekeko [?node] 
                                            (fresh [?member] 
                                                   (contains list ?member)
                                                   (child+ ?member ?node)
                                                   (pre-func ?node))))
        ;check property value-raw of root it self, of property members and of property all childs
        :property (concat (damp.ekeko/ekeko [?property] 
                                            (equals ast ?property)
                                            (pre-func ?property))
                          (damp.ekeko/ekeko [?property] 
                                            (fresh [?node ?keyword] 
                                                   (contains list ?node)
                                                   (has ?keyword ?node ?property)
                                                   (pre-func ?property)))
                          (damp.ekeko/ekeko [?property] 
                                            (fresh [?node ?keyword ?member] 
                                                   (contains list ?member)
                                                   (child+ ?member ?node)
                                                   (has ?keyword ?node ?property)
                                                   (pre-func ?property))))
        ;others, return empty list
        '()))) 

(defn 
  possible-nodes-for-operator
  "Returns list of possible nodes to be applied on."
  [ast operator-id]
  (cond 
    (ast? ast) (node-possible-nodes-for-operator ast operator-id)
    (lstvalue? ast) (nodelist-possible-nodes-for-operator ast operator-id) 
    :else nil))

(defn 
  possible-nodes-for-operator-in-group
  [snippetgroup op-id]
  (flat-map (fn [x] (possible-nodes-for-operator (:ast x) op-id)) (snippetgroup-snippetlist snippetgroup)))

(defn
  applicable-operators-for-node
  "Returns set of operator-id and corresponding list of possible nodes from given rootnode,
   in the form {op-id (list of nodes)
                op-id (list of nodes)
                ....}."
  [node]
  (defn process-node [operators app-operators]
    (if (empty? operators)
      app-operators
      (let [op-id (first operators)
            list-of-nodes (possible-nodes-for-operator node op-id)]
        (if (empty? list-of-nodes)
          (process-node (rest operators) app-operators)
          (process-node (rest operators) (assoc app-operators op-id list-of-nodes))))))
  (process-node (operator-ids) {}))

(defn
  applicable-operators-for-snippet
  [snippet]
  (applicable-operators-for-node (:ast snippet)))

(defn
  applicable-operators-for-a-node
  "Returns list of applicable operator-id from given node."
  [node]
  (defn process-a-node [operators app-operators]
    (if (empty? operators)
      app-operators
      (let [op-id (first operators)]
        (if (safe-operator-for-node? op-id node)
          (process-a-node (rest operators) (cons op-id app-operators))
          (process-a-node (rest operators) app-operators)))))
  (if (or (ast? node) (lstvalue? node))
    (process-a-node (operator-ids) '())
    '()))

(defn
  applicable-operators-with-type
  "Returns list of applicable operator-id from given operator-type and node."
  [op-type node]
  (defn process-a-node [operators app-operators]
    (if (empty? operators)
      app-operators
      (let [op-id (first operators)]
        (if (safe-operator-for-node? op-id node)
          (process-a-node (rest operators) (cons op-id app-operators))
          (process-a-node (rest operators) app-operators)))))
  (if (or (ast? node) (lstvalue? node))
    (process-a-node (operator-ids-with-type op-type) '())
    '()))

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
  (all 
    (succeeds (lstvalueraw? ?raw))))

(defn
  is-ast?
  "Relation of all list ASTNode instances."
  [?node]
  (all 
    (succeeds (ast? ?node))))

(defn
  is-type?
  "Relation of all list ASTNode instances (any subclass of Type)
   e.g :SimpleType, :PrimitiveType, etc."
  [?node]
  (all
    (succeeds (type? ?node))))

(defn
  is-listmember?
  "Relation of all list ASTNode instances which is member of Nodelist."
  [?node]
  (fresh [?key] 
    (ast ?key ?node)
    (succeeds (property-descriptor-list? (.getLocationInParent ?node)))))

(defn
  is-variabledeclarationstatement?
  "Relation of all list ASTNode instances type :VariableDeclarationStatement."
  [?node]
  (all 
    (ast :VariableDeclarationStatement ?node)))

(defn
  is-ifstatement?
  "Relation of all list ASTNode instances type :IfStatement."
  [?node]
  (all
    (ast :IfStatement ?node)))

(defn
  is-assignmentexpression?
  "Relation of all list ASTNode instances type :ExpressionStatement with expression :Assignment."
  [?node]
  (fresh [?exp] 
    (ast :ExpressionStatement ?node)
    (has :expression ?node ?exp)
    (ast :Assignment ?exp)))

(defn
  is-methodinvocationexpression?
  "Relation of all list ASTNode instances type :ExpressionStatement with expression :MethodInvocation."
  [?node]
  (fresh [?exp] 
    (ast :ExpressionStatement ?node)
    (has :expression ?node ?exp)
    (ast :MethodInvocation ?exp)))

(defn 
  epsilon
  []
  '())

;; Operator Precondition Data
;; -----------------------------

(def 
  operator-information
  {:contains-elements-with-same-size                 [:property       
                                                      contains-elements-with-same-size                  
                                                      listvalue					          
                                                      :generalization 
                                                      "contains-elements-with-same-size"
                                                      "Desc"]
   :contains-elements                                [:property   
                                                      contains-elements
                                                      listvalue					          
                                                      :generalization 
                                                      "contains-elements"
                                                      "Desc"]
   :contains-elements-with-relative-order            [:property
                                                      contains-elements-with-relative-order     
                                                      listvalue					          
                                                      :generalization 
                                                      "contains-elements-with-relative-order"
                                                      "Desc"]
   :contains-elements-with-repetition                [:property 
                                                      contains-elements-with-repetition    
                                                      listvalue					          
                                                      :generalization 
                                                      "contains-elements-with-repetition"
                                                      "Desc"]
   :split-variable-declaration-statement             [:node 
                                                      split-variable-declaration-statement        
                                                      is-variabledeclarationstatement?
                                                      :refinement 
                                                      "split-variable-declaration-statement"
                                                      "Desc"]
   :contains-variable-declaration-statement          [:node  
                                                      contains-variable-declaration-statement     
                                                      is-variabledeclarationstatement?
                                                      :generalization 
                                                      "contains-variable-declaration-statement"
                                                      "Desc"]
   :allow-ifstatement-with-else                      [:node   
                                                      allow-ifstatement-with-else  
                                                      is-ifstatement?  
                                                      :generalization 
                                                      "allow-ifstatement-with-else"
                                                      "Desc"]
   :allow-subtype-on-variable-declaration            [:node
                                                      allow-subtype-on-variable-declaration     
                                                      is-type?	
                                                      :generalization 
                                                      "allow-subtype-on-variable-declaration"
                                                      "Desc"]
   :allow-subtype-on-class-declaration-extends       [:node   
                                                      allow-subtype-on-class-declaration-extends   
                                                      is-type?     	
                                                      :generalization 
                                                      "allow-subtype-on-class-declaration-extends"
                                                      "Desc"]
   :allow-variable-declaration-with-initializer      [:node     
                                                      allow-variable-declaration-with-initializer 
                                                      is-assignmentexpression?    
                                                      :generalization 
                                                      "allow-variable-declaration-with-initializer"
                                                      "Desc"]
   :inline-method-invocation                         [:node  
                                                      inline-method-invocation          
                                                      is-methodinvocationexpression? 
                                                      :refinement 
                                                      "inline-method-invocation"
                                                      "Desc"]
   :negated-node                                     [:node   
                                                      negated-node        
                                                      is-ast?      
                                                      :refinement 
                                                      "negated-node"
                                                      "Desc"]
   :introduce-logic-variable                         [:node  
                                                      introduce-logic-variable  
                                                      is-ast?                 
                                                      :generalization 
                                                      "introduce-logic-variable"
                                                      "Desc"]
   :introduce-logic-variable-of-node-exact           [:node   
                                                      introduce-logic-variable-of-node-exact  
                                                      is-ast?      
                                                      :netral 
                                                      "introduce-logic-variable-of-node-exact"
                                                      "Desc"]
   :introduce-logic-variables                        [:node 
                                                      introduce-logic-variables  
                                                      is-ast?					
                                                      :generalization 
                                                      "introduce-logic-variables"
                                                      "Desc"]
   :introduce-logic-variables-with-condition         [:node     
                                                      introduce-logic-variables-with-condition 
                                                      is-ast?       	
                                                      :generalization 
                                                      "introduce-logic-variables-with-condition"
                                                      "Desc"]
   :add-node                                         [:property   
                                                      add-node                                          
                                                      listvalue					          
                                                      :refinement 
                                                      "add-node"
                                                      "Desc"]
   :remove-node                                      [:node       
                                                      remove-node                                       
                                                      is-listmember?   
                                                      :generalization 
                                                      "remove-node"
                                                      "Desc"]
	})

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
