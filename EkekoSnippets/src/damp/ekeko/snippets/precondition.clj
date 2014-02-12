(ns
  ^{:doc "Precondition check for operators."
    :author "Coen De Roover, Siltvani"}
   damp.ekeko.snippets.precondition
  (:refer-clojure :exclude [== type])
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko [logic :as el]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [operatorsrep :as rep]]))

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
  (let [pre-func (precondition-function (rep/precondition-id operator-id))
        op-type  (precondition-type (rep/precondition-id operator-id))]
    (if (contains? (set '(:node :property)) op-type) 
      (not (empty?
             (damp.ekeko/ekeko [?node] 
                               (el/equals node ?node)
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
  [ast operator-id]
  (possible-nodes-in-list ast (rep/precondition-id operator-id)))

(defn 
  possible-nodes-for-operator-argument
  "Returns list of possible nodes as argument of given operator."
  [ast operator-id]
  (possible-nodes-in-list ast (rep/argument-precondition-id operator-id)))

(defn 
  possible-nodes-for-operator-in-group
  [snippetgroup op-id]
  (possible-nodes-in-group snippetgroup (rep/precondition-id op-id)))

(defn 
  possible-nodes-for-operator-argument-in-group
  [snippetgroup op-id]
  (possible-nodes-in-group snippetgroup (rep/argument-precondition-id op-id)))

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
  (process-node (rep/operator-ids) {}))

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
  (applicable-operators-for-a-node-with-operators node (rep/operator-ids)))

(defn
  applicable-operators-with-type
  "Returns list of applicable operator-id from given operator-type and node."
  [op-type node]
  (applicable-operators-for-a-node-with-operators node (rep/operator-ids-with-type op-type)))

(defn
  applicable-operators-for-transformation
  "Returns list of applicable operator-id from given node."
  [node]
  (applicable-operators-for-a-node-with-operators node (rep/operator-ids-for-transformation)))

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
  (cl/all 
    (el/succeeds (lstvalueraw? ?raw))))

(defn
  primitive-or-null-value
  "Relation of all value of ASTNode, but not listvalue."
  [?val]
  (cl/conde [(ast/value|primitive ?val)]
            [(ast/value|null ?val)]))

(defn
  is-ast?
  "Relation of all list ASTNode instances."
  [?node]
  (cl/all 
    (el/succeeds (astnode/ast? ?node))))

(defn
  is-type?
  "Relation of all list ASTNode instances (any subclass of Type)
   e.g :SimpleType, :PrimitiveType, etc."
  [?node]
  (cl/all
    (el/succeeds (type? ?node))))

(defn
  is-listmember?
  "Relation of all list ASTNode instances which is member of Nodelist."
  [?node]
  (cl/fresh [?key] 
    (cl/!= :CompilationUnit ?key)
    (ast/ast ?key ?node)
    (el/succeeds (astnode/property-descriptor-list? (.getLocationInParent ?node)))))

(defn
  is-variabledeclarationstatement?
  "Relation of all list ASTNode instances type :VariableDeclarationStatement."
  [?node]
  (cl/all 
    (ast/ast :VariableDeclarationStatement ?node)))

(defn
  is-ifstatement?
  "Relation of all list ASTNode instances type :IfStatement."
  [?node]
  (cl/all
    (ast/ast :IfStatement ?node)))

(defn
  is-assignmentstatement?
  "Relation of all list ASTNode instances type :ExpressionStatement with expression :Assignment."
  [?node]
  (cl/fresh [?exp] 
    (ast/ast :ExpressionStatement ?node)
    (ast/has :expression ?node ?exp)
    (ast/ast :Assignment ?exp)))

(defn
  is-methodinvocationstatement?
  "Relation of all list ASTNode instances type :ExpressionStatement with expression :MethodInvocation."
  [?node]
  (cl/fresh [?exp] 
    (ast/ast :ExpressionStatement ?node)
    (ast/has :expression ?node ?exp)
    (ast/ast :MethodInvocation ?exp)))

(defn
  is-methodinvocationexpression?
  "Relation of all list ASTNode instances type :MethodInvocation."
  [?node]
  (cl/all
    (ast/ast :MethodInvocation ?node)))

(defn
  is-methoddeclaration?
  "Relation of all list ASTNode instances type :MethodDeclaration."
  [?node]
  (cl/all
    (ast/ast :MethodDeclaration ?node)))

(defn
  is-simplename?
  "Relation of all list ASTNode instances type :SimpleName."
  [?node]
  (cl/all
    (ast/ast :SimpleName ?node)))

(defn
  is-variabledeclarationfragment?
  "Relation of all list ASTNode instances type :VariableDeclarationFragment."
  [?node]
  (cl/all
    (ast/ast :VariableDeclarationFragment ?node)))

(defn
  is-importlibrary?
  "Relation of all list ASTNode instances type :QualifiedName in :ImportDeclaration."
  [?node]
  (cl/fresh [?import]
         (ast/ast :QualifiedName ?node)
         (el/equals ?import (.getParent ?node))
         (ast/ast :ImportDeclaration ?import)))

(defn
  is-variabledeclaration?
  "Relation of all list ASTNode instances type :SimpleName as child of :VariableDeclarationFragment."
  [?node]
  (cl/fresh [?fragment]
         (ast/ast :SimpleName ?node)
         (el/equals ?fragment (.getParent ?node))
         (ast/ast :VariableDeclarationFragment ?fragment)))

(defn
  is-loop?
  "Relation of loop kind of statement."
  [?node]
  (cl/conde [(ast/ast :ForStatement ?node)]
            [(ast/ast :WhileStatement ?node)]
            [(ast/ast :DoStatement ?node)]))

(defn 
  epsilon
  []
  '())


;; Operator Precondition
;; -----------------------------

(def 
  operator-precondition
  {:listvalue                            [:property ast/value|list]					          
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

(defn
  register-callbacks 
  []
  
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_APPLICABLE_OPERATORS_WITH_TYPE) applicable-operators-with-type)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_APPLICABLE_OPERATORS_FOR_TRANSFORMATION) applicable-operators-for-transformation)
  (set! (damp.ekeko.snippets.data.SnippetOperator/FN_POSSIBLE_NODES_FOR_OPERATOR_ARGUMENT_IN_GROUP) possible-nodes-for-operator-argument-in-group)

  
  )


(register-callbacks)

