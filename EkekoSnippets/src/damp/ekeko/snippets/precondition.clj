(ns
  ^{:doc "Precondition check for operators."
    :author "Coen De Roover, Siltvani"}
   damp.ekeko.snippets.precondition
  (:refer-clojure :exclude [== type])
  (:use [clojure.core.logic])
  (:use [damp.ekeko logic])
  (:use [damp.ekeko.jdt astnode basic reification])
  (:use [damp.ekeko.snippets representation operatorsrep]))

;; Precondition Data
;; ----------------------

(declare operator-precondition)

(defn 
  precondition-function 
  "Returns precondition function of given operator id."
  [op-id]
  (get operator-precondition (precondition-id op-id)))



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


;; Operator Precondition
;; -----------------------------

(def 
  operator-precondition
  {:listvalue                            listvalue					          
   :is-variabledeclarationstatement?     is-variabledeclarationstatement?
   :is-ifstatement?                      is-ifstatement?  
   :is-type?                             is-type?	
   :is-assignmentexpression?             is-assignmentexpression?    
   :is-methodinvocationexpression?       is-methodinvocationexpression? 
   :is-ast?                              is-ast?      
   :is-listmember?                       is-listmember?   
	})

