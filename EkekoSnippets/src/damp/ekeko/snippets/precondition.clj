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
; operator-precondition 
;    {:operator-name1 [:operator-type operator-function1 precondition-fuction1]
;     :operator-name2 [:operator-type operator-function2 precondition-fuction2]
;     ...}
; :operator-type -> node, property, snippet, and group

(declare operator-precondition)

(defn 
  operator-name 
  "Returns operator function of given map."
  [map]
  (key map))

(defn 
  operator-type 
  "Returns operator type of given operator name."
  [op-name]
  (first (get operator-precondition op-name)))

(defn 
  operator-function 
  "Returns operator function of given operator name."
  [op-name]
  (fnext (get operator-precondition op-name)))

(defn 
  precondition-function 
  "Returns precondition function of given operator name."
  [op-name]
  (first (nnext (get operator-precondition op-name))))

(defn 
  operator-names
  "Returns all operator names."
  []
  (map (fn [x] (key x)) operator-precondition))

(defn 
  operator-functions
  "Returns all operator functions."
  []
  (map (fn [x] (fnext (val x))) operator-precondition))


;; Applicable Operator for snippet
;; ----------------------------------

(defn 
  safe-operator-for-node?
  "Returns true if precondition of given operator is fulfilled by the node."
  [operator-name node]
  (let [pre-func (precondition-function operator-name)
        op-type  (operator-type operator-name)]
    (if (contains? (set '(:node :property)) op-type) 
      (not (empty?
             (damp.ekeko/ekeko [?node] 
                               (equals node ?node)
                               (pre-func ?node))))
      true)))

(defn 
  possible-nodes-for-operator
  "Returns list of possible nodes to be applied on."
  [root operator-name]
  (let [pre-func (precondition-function operator-name)
        op-type  (operator-type operator-name)]
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
                                          (fresh [?node ?keyword ?raw] 
                                                 (equals root ?node)
                                                 (has ?keyword ?node ?property)
                                                 (value-raw ?property ?raw) 
                                                 (pre-func ?raw)))
                        (damp.ekeko/ekeko [?property] 
                                          (fresh [?node ?keyword ?raw] 
                                                 (child+ root ?node)
                                                 (has ?keyword ?node ?property)
                                                 (value-raw ?property ?raw) 
                                                 (pre-func ?raw))))
      ;others, return empty list
      (lazy-seq)))) 
    
(defn 
  possible-nodes-for-operator-in-group
  [snippetgroup op-name]
  (flat-map (fn [x] (possible-nodes-for-operator (:ast x) op-name)) (snippetgroup-snippetlist snippetgroup)))

(defn
  applicable-operators-for-node
  "Returns set of operator-name and corresponding list of possible nodes,
   in the form {op-name (list of nodes)
                op-name (list of nodes)
                ....}."
  [node]
  (defn process-node [operators app-operators]
    (if (empty? operators)
      app-operators
      (let [op-name (first operators)
            list-of-nodes (possible-nodes-for-operator node op-name)]
        (if (empty? list-of-nodes)
          (process-node (rest operators) app-operators)
          (process-node (rest operators) (assoc app-operators op-name list-of-nodes))))))
  (process-node (operator-names) {}))

(defn
  applicable-operators-for-snippet
  [snippet]
  (applicable-operators-for-node (:ast snippet)))


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
  operator-precondition
  {:contains-elements-with-same-size                 [:property   contains-elements-with-same-size                  listvalueraw					          ]
   :contains-elements                                [:property   contains-elements                                 listvalueraw			          		]
   :contains-elements-with-relative-order            [:property   contains-elements-with-relative-order             listvalueraw			             	]
   :contains-elements-with-repetition                [:property   contains-elements-with-repetition                 listvalueraw			          	 	]
   :split-variable-declaration-statement             [:node       split-variable-declaration-statement              is-variabledeclarationstatement?]
   :contains-variable-declaration-statement          [:node       contains-variable-declaration-statement           is-variabledeclarationstatement?]
   :allow-ifstatement-with-else                      [:node       allow-ifstatement-with-else                       is-ifstatement?               	]
   :allow-subtype-on-variable-declaration            [:node       allow-subtype-on-variable-declaration             is-type?			              		]
   :allow-subtype-on-class-declaration-extends       [:node       allow-subtype-on-class-declaration-extends        is-type?     		          			]
   :allow-variable-declaration-with-initializer      [:node       allow-variable-declaration-with-initializer       is-assignmentexpression?       	]
   :inline-method-invocation                         [:node       inline-method-invocation                          is-methodinvocationexpression?  ]
   :negated-node                                     [:node       negated-node                                      is-ast?                         ]
   :introduce-logic-variable                         [:node       introduce-logic-variable                          is-ast?                       	]
   :introduce-logic-variable-of-node-exact           [:node       introduce-logic-variable-of-node-exact            is-ast?         	        			]
   :introduce-logic-variables                        [:node       introduce-logic-variables                         is-ast?						            	]
   :introduce-logic-variables-with-condition         [:node       introduce-logic-variables-with-condition          is-ast?       	        				]
   :add-node                                         [:property   add-node                                          listvalueraw		          			]
   :remove-node                                      [:node       remove-node                                       is-ast?   		          				]	
   :add-logic-conditions                             [:snippet    add-logic-conditions                              epsilon			            				]
   :remove-logic-conditions                          [:snippet    remove-logic-conditions                           epsilon			          			    ]
   :add-logic-conditions-to-snippetgroup             [:group      add-logic-conditions-to-snippetgroup              epsilon                 				]
   :remove-logic-conditions-from-snippetgroup        [:group      remove-logic-conditions-from-snippetgroup         epsilon               					]
   :add-snippet                                      [:snippet    add-snippet                                       epsilon                        	]
	})


