(ns
  ^{:doc "Precondition check for operators."
    :author "Coen De Roover, Siltvani"}
   damp.ekeko.snippets.precondition
  (:refer-clojure :exclude [== type])
  (:use [clojure.core.logic])
  (:use [damp.ekeko logic])
  (:use [damp.ekeko.jdt astnode ast])
  (:use [damp.ekeko.snippets representation operatorsrep]))

;; Precondition Data
;; ----------------------

(declare operator-precondition)

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


;; Applicable Operator for snippet
;; ----------------------------------

(defn 
  safe-operator-for-node?
  "Returns true if precondition of given operator is fulfilled by the node."
  [operator-id node]
  (let [pre-func (precondition-function (precondition-id operator-id))
        op-type  (precondition-type (precondition-id operator-id))]
    (if (contains? (set '(:node :property)) op-type) 
      (not (empty?
             (damp.ekeko/ekeko [?node] 
                               (equals node ?node)
                               (pre-func ?node))))
      true)))

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
  nodelist-possible-nodes
  "Returns list of possible nodes from given precondition and ast root (nodelist)."
  [ast precondition-id]
  (let [list     (:value ast)
        pre-func (precondition-function precondition-id)
        op-type  (precondition-type precondition-id)]
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
  possible-nodes
  "Returns list of possible nodes from given precondition."
  [ast precondition-id]
  (cond 
    (ast? ast) (node-possible-nodes ast precondition-id)
    (lstvalue? ast) (nodelist-possible-nodes ast precondition-id) 
    :else nil))

(defn 
  possible-nodes-in-list
  [ast precondition-id]
  (map first (possible-nodes ast precondition-id)))

(defn 
  possible-nodes-in-group
  [snippetgroup pre-id]
  (flat-map (fn [x] (possible-nodes-in-list (:ast x) pre-id)) (snippetgroup-snippetlist snippetgroup)))

(defn 
  possible-nodes-for-operator
  "Returns list of possible nodes to be applied on to given operator."
  [ast operator-id]
  (possible-nodes-in-list ast (precondition-id operator-id)))

(defn 
  possible-nodes-for-operator-argument
  "Returns list of possible nodes as argument of given operator."
  [ast operator-id]
  (possible-nodes-in-list ast (argument-precondition-id operator-id)))

(defn 
  possible-nodes-for-operator-in-group
  [snippetgroup op-id]
  (possible-nodes-in-group snippetgroup (precondition-id operator-id)))

(defn 
  possible-nodes-for-operator-argument-in-group
  [snippetgroup op-id]
  (possible-nodes-in-group snippetgroup (argument-precondition-id op-id)))

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
  applicable-operators-for-a-node-with-operators
  "Returns list of applicable operator-id from given node and operators-ids."
  [node op-ids]
  (defn process-a-node [operators app-operators]
    (if (empty? operators)
      app-operators
      (let [op-id (first operators)]
        (if (safe-operator-for-node? op-id node)
          (process-a-node (rest operators) (cons op-id app-operators))
          (process-a-node (rest operators) app-operators)))))
  (process-a-node op-ids '()))

(defn
  applicable-operators-for-a-node
  "Returns list of applicable operator-id from given node."
  [node]
  (applicable-operators-for-a-node-with-operators node (operator-ids)))

(defn
  applicable-operators-with-type
  "Returns list of applicable operator-id from given operator-type and node."
  [op-type node]
  (applicable-operators-for-a-node-with-operators node (operator-ids-with-type op-type)))

(defn
  applicable-operators-for-transformation
  "Returns list of applicable operator-id from given node."
  [node]
  (applicable-operators-for-a-node-with-operators node (operator-ids-for-transformation)))

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
  primitive-or-null-value
  "Relation of all value of ASTNode, but not listvalue."
  [?val]
  (conde [(value|primitive ?val)]
         [(value|null ?val)]))

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
    (!= :CompilationUnit ?key)
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
  is-assignmentstatement?
  "Relation of all list ASTNode instances type :ExpressionStatement with expression :Assignment."
  [?node]
  (fresh [?exp] 
    (ast :ExpressionStatement ?node)
    (has :expression ?node ?exp)
    (ast :Assignment ?exp)))

(defn
  is-methodinvocationstatement?
  "Relation of all list ASTNode instances type :ExpressionStatement with expression :MethodInvocation."
  [?node]
  (fresh [?exp] 
    (ast :ExpressionStatement ?node)
    (has :expression ?node ?exp)
    (ast :MethodInvocation ?exp)))

(defn
  is-methodinvocationexpression?
  "Relation of all list ASTNode instances type :MethodInvocation."
  [?node]
  (ast :MethodInvocation ?node))

(defn
  is-methoddeclaration?
  "Relation of all list ASTNode instances type :MethodDeclaration."
  [?node]
  (ast :MethodDeclaration ?node))

(defn
  is-simplename?
  "Relation of all list ASTNode instances type :SimpleName."
  [?node]
  (ast :SimpleName ?node))

(defn
  is-variabledeclarationfragment?
  "Relation of all list ASTNode instances type :VariableDeclarationFragment."
  [?node]
  (ast :VariableDeclarationFragment ?node))

(defn
  is-importlibrary?
  "Relation of all list ASTNode instances type :QualifiedName in :ImportDeclaration."
  [?node]
  (fresh [?import]
         (ast :QualifiedName ?node)
         (equals ?import (.getParent ?node))
         (ast :ImportDeclaration ?import)))

(defn
  is-variabledeclaration?
  "Relation of all list ASTNode instances type :SimpleName as child of :VariableDeclarationFragment."
  [?node]
  (fresh [?fragment]
         (ast :SimpleName ?node)
         (equals ?fragment (.getParent ?node))
         (ast :VariableDeclarationFragment ?fragment)))

(defn
  is-loop?
  "Relation of loop kind of statement."
  [?node]
  (conde [(ast :ForStatement ?node)]
         [(ast :WhileStatement ?node)]
         [(ast :DoStatement ?node)]))

(defn 
  epsilon
  []
  '())


;; Operator Precondition
;; -----------------------------

(def 
  operator-precondition
  {:listvalue                            [:property value|list]					          
   :primitive-or-null-value              [:property primitive-or-null-value]					          
   :is-variabledeclarationstatement?     [:node is-variabledeclarationstatement?]
   :is-ifstatement?                      [:node is-ifstatement?]  
   :is-type?                             [:node is-type?]	
   :is-assignmentstatement?              [:node is-assignmentstatement?]    
   :is-methodinvocationstatement?        [:node is-methodinvocationstatement?] 
   :is-ast?                              [:node is-ast?]      
   :is-listmember?                       [:node is-listmember?]   
   :is-methodinvocationexpression?       [:node is-methodinvocationexpression?] 
   :is-methoddeclaration?                [:node is-methoddeclaration?]
   :is-variabledeclarationfragment?      [:node is-variabledeclarationfragment?]
   :is-variabledeclaration?              [:node is-variabledeclaration?]
   :is-simplename?                       [:node is-simplename?]
   :is-importlibrary?                    [:node is-importlibrary?]
   :is-loop?                             [:node is-loop?]
	})

