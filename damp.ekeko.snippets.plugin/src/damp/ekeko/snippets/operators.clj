(ns 
  ^{:doc "Operators for generalizing and refining snippets."
    :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.operators
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [matching :as matching]
             [directives :as directives]
             [rewriting :as rewriting]
             ])
  ;(:require [damp.ekeko.jdt 
  ;           [astnode :as astnode]
  ;           [ast :as ast]])
  )


;; Operators for Snippet
;; ---------------------

;remove-all-directives+ (on node and all children)
;remove-directives: constraining (fron node itsel_
;add-constraining directive: ignore, ignore-variable, maybe enought to have an entry in ast2uservar, is ast2uservar still needed? 
;could just use operandbindings of directive instead

(defn
  make-directiveoperand-for-match
  []
  (directives/make-directiveoperand "Match for template node"))

(defn
  make-directiveoperandbinding-for-match
  [node]
  (directives/make-directiveoperand-binding
    (make-directiveoperand-for-match)
    node))

(defn 
  replace-by-variable 
  "Replace snippet AST node by a logic variable."
  [snippet node uservar]
  (let [purged-of-children ;remove all directives for children
        (matching/remove-all-directives+ snippet node)
        purged 
        ;remove constraining directives for node, keep grounding directives
        ;(TODO: perhaps only remove the default constraining directive?)
        (matching/remove-directives purged-of-children node (matching/registered-constraining-directives))]
    (snippet/add-bounddirective purged
                                node 
                                (directives/make-bounddirective 
                                  matching/directive-replacedbyvariable
                                  [(make-directiveoperandbinding-for-match node)
                                   (directives/make-directiveoperand-binding
                                     (directives/make-directiveoperand "Variable replacing template node")
                                     uservar)]))))

(defn 
  add-directive-equals 
  "Adds directive-equals to node."
  [snippet node uservar]
  (snippet/add-bounddirective snippet
                              node 
                              (directives/make-bounddirective 
                                matching/directive-equals
                                [(make-directiveoperandbinding-for-match node)
                                 (directives/make-directiveoperand-binding
                                   (directives/make-directiveoperand "Variable equals to template node")
                                   uservar)])))



(defn
  relax-size-to-atleast
  "Uses match-size|atleast/0 for constraining match candidates for a template list."
  [template lst]
  (snippet/add-bounddirective 
    (matching/remove-directives template lst (matching/registered-constraining-directives))
    lst
    (directives/make-bounddirective matching/directive-size|atleast
                                    [(make-directiveoperandbinding-for-match lst)])))
  

(defn
  relax-scope-to-member
  "Uses member/0 for grounding of list members."
  [template value]
  (snippet/add-bounddirective 
    (matching/remove-directives template value (matching/registered-grounding-directives))
    value
    (directives/make-bounddirective matching/directive-member
                                    [(make-directiveoperandbinding-for-match value)])))

(defn
  relax-scope-to-offspring
  "Uses scope|offspring/0 for grounding."
  [template value]
  (snippet/add-bounddirective 
    (matching/remove-directives 
      ; No longer doing: "If member of list, list should relax it's size requirement."
      ; because prefer to keep all operators as atomic as possible
      ; in order to derive mutation patterns
      ;(if 
      ;  (matching/value|listmember? value)
      ;  (let [lst (snippet/snippet-list-containing template value)]
      ;    (relax-size-to-atleast template lst))
      ;  template)
      template
      value 
      (matching/registered-grounding-directives))
    value
    (directives/make-bounddirective matching/directive-offspring
                                    [(make-directiveoperandbinding-for-match value)])))


(defn
  restrict-scope-to-child
  "Uses child/0 for grounding."
  [template value]
  (snippet/add-bounddirective 
    (matching/remove-directives template value (matching/registered-grounding-directives))
    value
    (directives/make-bounddirective matching/directive-child
                                    [(make-directiveoperandbinding-for-match value)])))


  
    
(defn 
  replace-operand-by-template
  ""
  [snippet value-to-be-ignored uservar]
  (let
    [root (snippet/snippet-root snippet)]
    (snippet/add-bounddirective
      snippet
      root 
      (directives/make-bounddirective 
        rewriting/directive-replace
        [(make-directiveoperandbinding-for-match root)
         (directives/make-directiveoperand-binding
           (directives/make-directiveoperand "Variable to be replaced by template")
           uservar)]))))

    

(defn
  remove-node
  "Removes node from snippet."
  [template value]
  (snippet/remove-node
    template
    value))
  
  

(comment
  
  (defn
    allow-relax-loop 
    "Allow relax loop (for, do, while)."
    [snippet node]
    (if (instance? org.eclipse.jdt.core.dom.ForStatement node)
      (snippet/update-cf 
        (snippet/remove-gf+
          (snippet/remove-gfcf+
            snippet 
            (astnode/make-value node (astnode/node-property-descriptor-for-ekeko-keyword node :initializers) (.initializers node)))
          (astnode/make-value node (astnode/node-property-descriptor-for-ekeko-keyword node :updaters) (.updaters node)))
        node 
        :relax-loop)
      (snippet/update-cf snippet node :relax-loop)))
  
  (defn
    contains-deep 
    "Allows elements of given node as child+ of given node."
    [snippet node]
    (defn change-gf-elements [snippet alist]
      (if (empty? alist)
        snippet
        (let [snippet-node (snippet/update-gf snippet (first alist) :child+)]
          (change-gf-elements snippet-node (next alist)))))
    (let [list-raw (:value node)
          new-gf-snippet (change-gf-elements snippet list-raw)]
      (snippet/update-cf new-gf-snippet node :child+)))
  
  (defn
    contains-elements-with-same-size 
    "Contains all elements in a given nodelist (listval), and list has to be the same size."
    [snippet node]
    (snippet/update-cf snippet node :contains-eq-size))
  
  (defn
    contains-any-elements
    "Contains any elements or none in a given nodelist (listval)."
    [snippet node]
    (let [snippet-with-epsilon (snippet/remove-gfcf+ snippet node)]
      (snippet/update-cf snippet-with-epsilon node :any)))
  
  (defn
    contains-elements
    "Contains all elements in a given nodelist (listval), and list does not have to be the same size."
    [snippet node]
    (snippet/update-cf snippet node :contains))
  
  (defn
    contains-elements-with-relative-order 
    "Contains all elements in a given nodelist (listval), with relative order."
    [snippet node]
    (snippet/update-cf snippet node :contains-eq-order))
  
  (defn
    contains-elements-with-repetition 
    "Contains all elements in a given nodelist (listval), with repetition."
    [snippet node]
    (snippet/update-cf snippet node :contains-repetition))
  
  (defn 
    replace-node-no-apply-rewrite 
    "Replace a node with new node in snippet."
    [snippet node newnode]
    (let [rewrite (snippet/snippet-rewrite snippet)]
      (.replace rewrite node newnode (new org.eclipse.text.edits.TextEditGroup "snippet"))
      snippet))
  
  (defn 
    change-property-node-no-apply-rewrite 
    "Change property of given node in snippet."
    [snippet node value]
    (let [rewrite (snippet/snippet-rewrite snippet)]
      (.set rewrite (:owner node) (:property node) value)
      snippet))
  
  (defn 
    change-property-node 
    "Change property of given node in snippet."
    [snippet node value]
    (change-property-node-no-apply-rewrite snippet node value)
    (snippet/apply-rewrite snippet)) 
  
  (defn 
    replace-node 
    "Replace a node with new node in snippet."
    [snippet node newnode]
    (replace-node-no-apply-rewrite snippet node newnode)
    (snippet/apply-rewrite snippet)) 
  
  (defn 
    remove-node-no-apply-rewrite 
    "Remove a given node from snippet, without apply rewrite."
    [snippet node]
    (let [list-container (snippet/snippet-list-containing snippet node)
          rewrite (snippet/snippet-rewrite snippet)
          list-rewrite (.getListRewrite rewrite (:owner list-container) (:property list-container))]
      (.remove list-rewrite node (new org.eclipse.text.edits.TextEditGroup "snippet"))
      snippet))
  
  (defn 
    remove-node 
    "Remove a given node from snippet."
    [snippet node]
    (remove-node-no-apply-rewrite snippet node)
    (snippet/apply-rewrite snippet)) 
  
  (defn
    remove-nodes
    "Remove given nodes from snippet."
    [snippet nodes]
    (defn remove-nodes-rec [snippet nodes]
      (if (empty? nodes)
        snippet
        (let [new-snippet (remove-node-no-apply-rewrite snippet (first nodes))]
          (remove-nodes-rec
            new-snippet
            (rest nodes)))))
    (snippet/apply-rewrite
      (remove-nodes-rec snippet nodes)))
  
  (defn 
    add-node-no-apply-rewrite 
    "Add a given node in given idx inside the lst (listval) in snippet."
    [snippet list-container node idx]
    (let [rewrite (snippet/snippet-rewrite snippet)
          list-rewrite (.getListRewrite rewrite (:owner list-container) (:property list-container))]
      (.insertAt list-rewrite node idx (new org.eclipse.text.edits.TextEditGroup "snippet"))
      snippet))
  
  (defn 
    add-node 
    "Add a given node in given idx inside the lst (listval) in snippet."
    [snippet list-container node idx]
    (add-node-no-apply-rewrite snippet list-container node idx)
    (snippet/apply-rewrite snippet)) 
  
  (defn
    add-nodes 
    "Add nodes (list of node) to list lst (listval) with index starting from idx in the given snippet."
    [snippet lst nodes idx]
    (defn add-nodes-rec [snippet lst nodes idx]
      (if (empty? nodes)
        snippet
        (let [newsnippet (add-node-no-apply-rewrite snippet lst (first nodes) idx)]
          (add-nodes-rec newsnippet lst (rest nodes) (+ idx 1)))))
    (snippet/apply-rewrite (add-nodes-rec snippet lst nodes idx))) 
  
  (defn
    split-variable-declaration-fragments 
    "Split variable declaration fragments into multiple node with one fragment for each statement.
   List listcontainer (lstval) is the list which fragments resided."
    [snippet listcontainer position fragments modifiers type]
    (defn split-variable-declaration-fragments-rec 
      [snippet listcontainer position fragments modifiers type]
      (if (empty? fragments)
        snippet
        (let [newnode         (parsing/make-variable-declaration-statement modifiers type (first fragments))
              newsnippet-node (add-node-no-apply-rewrite snippet listcontainer newnode position)
              newsnippet      (contains-elements newsnippet-node (first (astnode/node-propertyvalues newnode)))]
          (split-variable-declaration-fragments-rec newsnippet listcontainer (+ position 1) (rest fragments) modifiers type))))
    (snippet/apply-rewrite 
      (split-variable-declaration-fragments-rec snippet listcontainer position fragments modifiers type))) 
  
  (defn
    split-variable-declaration-statement 
    "Split variable declaration statement with many fragments into multiple node with one fragment for each statement."
    [snippet statement]
    (let [listcontainer   (snippet/snippet-list-containing snippet statement)
          position        (.indexOf (:value listcontainer) statement)
          newsnippet      (remove-node-no-apply-rewrite snippet statement)]  
      (split-variable-declaration-fragments
        newsnippet 
        listcontainer
        position
        (.fragments statement) 
        (.modifiers statement) 
        (.getType statement))))
  
  (defn
    contains-variable-declaration-statement 
    "Allow given variable declaration statement in given snippet, 
   as part of one or more variable declaration statements in target source code."
    [snippet statement]
    (let [listcontainer   (snippet/snippet-list-containing snippet statement)
          position        (.indexOf (:value listcontainer) statement)
          newsnippet-list (contains-elements snippet (:value listcontainer))
          newsnippet      (remove-node-no-apply-rewrite newsnippet-list statement)]  
      (split-variable-declaration-fragments
        newsnippet 
        listcontainer
        position
        (.fragments statement) 
        (.modifiers statement) 
        (.getType statement))))
  
  (defn
    contains-variable-declaration-statements 
    "Allow given lst (= list of statement) in given snippet, as part of one or more statements in target source code."
    [snippet lst]
    (if (empty? lst)
      snippet
      (contains-variable-declaration-statements 
        (contains-variable-declaration-statement snippet (first lst))
        (rest lst))))
  
  (defn
    allow-ifstatement-with-else 
    "Allow match given node (= ifstatement without else) with node (= ifstatement with else)."
    [snippet node]
    (let [else-node (first (astnode/node-propertyvalues node))
          snippet-without-else (snippet/update-cf snippet else-node :epsilon)]
      (snippet/update-cf snippet-without-else node :relax-branch)))
  
  (defn
    allow-subtype
    "Allow match given node (= field/variable type) with same type or its' subtype."
    [snippet node]
    (snippet/update-cf snippet node :relax-type))
  
  (defn
    relax-typeoftype
    "Allow match given node (= type) as simple type or parameterized type."
    [snippet node]
    (let [snippet-epsilon (snippet/remove-gfcf+ snippet node)
          snippet-type    (snippet/update-cf snippet-epsilon (.getType node) :exact)]
      (snippet/update-cf snippet-type node :relax-typeoftype)))
  
  (defn
    allow-variable-declaration-with-initializer
    "Allow match given node (= assignment expression) with local variable declaration with initializer."
    [snippet node]
    (let [snippet-without-assignment 
          (snippet/update-cf snippet (.getExpression node) :epsilon)]
      (snippet/update-cf snippet-without-assignment node :relax-assign)))
  
  (defn
    update-logic-conditions
    "Update user logic conditions to snippet. conditions should be in quote, '(...) (...) or string."
    [snippet conditions]
    (assoc snippet :userquery (list (symbol conditions))))
  
  (defn
    add-logic-conditions
    "Add user logic conditions to snippet. conditions should be in quote, '((...) (...))."
    [snippet conditions]
    (if (not (empty? conditions)) 
      (let [new-conditions `(~@(snippet/snippet-userquery snippet) ~@conditions)]
        (assoc snippet :userquery new-conditions))
      snippet))
  
  (defn
    remove-logic-conditions
    "Remove user logic conditions from snippet. conditions should be in quote, '((...) (...))."
    [snippet conditions]
    (if (not (empty? conditions)) 
      (let [new-conditions (remove (set conditions) (snippet/snippet-userquery snippet))]
        (assoc snippet :userquery new-conditions))
      snippet))
  
  (defn
    inline-method-invocation 
    "Inline statement of method invocation in given snippet, with statements from called method."
    [snippet statement]
    (defn declaration-of-invocation [inv] ;returns MethodDeclaration from the given invocation 
      (first (first 
               (damp.ekeko/ekeko [?dec] 
                                 (runtime/ast-invocation-declaration inv ?dec)))))
    (let [listcontainer   (snippet/snippet-list-containing snippet statement)
          position        (.indexOf (:value listcontainer) statement)
          inlined-statements (.statements (.getBody (declaration-of-invocation (.getExpression statement))))
          newsnippet      (remove-node-no-apply-rewrite snippet statement)]  
      (add-nodes newsnippet listcontainer inlined-statements position)))
  
  (defn 
    introduce-logic-variable-of-node-exact 
    "Introduce logic variable to a given node, without removing any condition."
    [snippet node uservar]
    (let [snippet-with-uservar (assoc-in snippet [:var2uservar (snippet/snippet-var-for-node snippet node)] (symbol uservar))]
      (snippet/update-cf snippet-with-uservar node :exact-variable)))
  
  
  (defn 
    internal-introduce-logic-variables-with-condition
    "Introduce logic variable to a given node, and other nodes with the same ast kind and identifier.
   Logic variable for the nodes will be generated. Condition will be applied to all those nodes."
    [snippet node newuservar newcondition count]
    (let [uservar (symbol newuservar)
          condition newcondition]
      (defn make-condition [condition uservar newvar]
        (if (not (empty? condition))
          (list (symbol (clojure.string/replace condition (str uservar) (str newvar))))
          condition))
      (defn update-snippet-var-cond [snippet value counter]
        (let [newvar     (symbol (str uservar counter))
              newsnippet (replace-by-variable snippet value newvar)]
          (add-logic-conditions newsnippet (make-condition condition uservar newvar))))
      (defn get-binding-variables [root node] ;returns list of nodes (variables) with the same binding as node 
        (damp.ekeko/ekeko [?var] 
                          (ast/child+ root ?var)
                          (runtime/ast-samekind-sameidentifier node ?var))) ;shud be ast-variable-samebinding
      (defn process-binding-variables [snippet nodes counter]
        (if (empty? nodes)
          snippet
          (let [new-snippet (update-snippet-var-cond snippet (first (first nodes)) counter)]
            (process-binding-variables new-snippet (rest nodes) (+ counter 1)))))
      (process-binding-variables snippet (get-binding-variables (:ast snippet) node) count)))
  
  (defn 
    introduce-logic-variables-with-condition
    "Introduce logic variable to a given node, and other nodes with the same ast kind and identifier.
   Logic variable for the nodes will be generated. Condition will be applied to all those nodes."
    [snippet node newuservar newcondition]
    (internal-introduce-logic-variables-with-condition
      snippet node newuservar newcondition 1))
  
  (defn 
    introduce-logic-variables
    "Introduce logic variable to a given node, and other nodes with the same ast kind and identifier.
   Logic variable for the nodes will be generated."
    [snippet node uservar]
    (introduce-logic-variables-with-condition snippet node uservar '()))
  
  (defn
    introduce-list-of-logic-variables
    [snippet nodes uservars]
    (if (empty? nodes)
      snippet
      (let [new-snippet (replace-by-variable snippet (first nodes) (first uservars))]
        (introduce-list-of-logic-variables
          new-snippet
          (rest nodes)
          (rest uservars)))))
  
  (defn
    introduce-logic-variables-with-condition-to-group
    [snippetgroup node newuservar newcondition]
    (defn process-binding-snippets [snippetlist counter resultlist]
      (if (empty? snippetlist)
        resultlist
        (let [new-snippet   (internal-introduce-logic-variables-with-condition
                              (first snippetlist) node newuservar newcondition counter)]
          (process-binding-snippets 
            (rest snippetlist) (+ counter 100) (concat resultlist (list new-snippet))))))
    (assoc snippetgroup :snippetlist 
           (process-binding-snippets (:snippetlist snippetgroup) 1 '())))
  
  (defn 
    introduce-logic-variables-to-group
    [snippetgroup node uservar]
    (introduce-logic-variables-with-condition-to-group snippetgroup node uservar '()))
  
  (defn 
    negated-node 
    "Match all kind of node, except the given node."
    [snippet node]
    (let [snippet-with-epsilon (snippet/remove-gfcf+ snippet node)
          snippet-with-gf (snippet/update-gf snippet-with-epsilon node :exact)]
      (snippet/update-cf snippet-with-gf node :negated)))
  
  (defn
    match-variable-typequalifiednamestring
    "Match Relation between ASTNode variable with it's type qualified name."
    [snippet node-var string]
    (let [var-node (snippet/snippet-lvar-for-node snippet node-var)
          new-snippet-var (snippet/update-cf-with-args snippet node-var :var-qname (list string))]
      (add-logic-conditions
        new-snippet-var
        `((damp.ekeko.snippets.runtime/ast-variable-typequalifiednamestring ~var-node ~string)))))
  
  (defn
    match-type-qualifiednamestring
    "Match Relation between ASTNode type with it's type qualified name."
    [snippet node-var string]
    (let [var-node (snippet/snippet-lvar-for-node snippet node-var)
          new-snippet-var (snippet/update-cf-with-args snippet node-var :type-qname (list string))]
      (add-logic-conditions
        new-snippet-var
        `((damp.ekeko.snippets.runtime/ast-type-qualifiednamecontain ~var-node ~string)))))
  
  ;todo
  (defn
    add-user-defined-condition
    "Add user defined condition to snippet.
   string format : (function-name var-arg)"
    [snippet node-var string]
    ;(let [arr-str (.split (.replace (.replace string ")" "") "(" "") " ")
    ;      f-name (first arr-str)
    ;      var-arg (fnext arr-str)
    ;      
    ;     ;;todo
    ;     new-conditions (cons (list f-name var-arg)
    ;                          (snippet/snippet-userfs-for-node snippet node-var))
    ;      
    ;     ]
    ;  (update-in snippet [:ast2userfs node-var] (fn [x] new-conditions)))
    snippet
    )
  
  (defn
    remove-user-defined-condition
    "Remove user defined condition for a given node-var."
    [snippet node-var]
    (update-in snippet [:ast2userfs node-var] (fn [x] '())))
  
  
  ;; Operators for SnippetGroup
  ;; --------------------------
  
  (defn
    node-deep 
    "Allows node as child+ of its parent."
    [snippetgroup node parent]
    (defn change-cf-parent [snippet node]
      (if (= node parent)
        ;(snippet/update-cf snippet (snippet/snippet-node-with-member snippet node) :epsilon)
        snippet
        (let [snippet-node (snippet/update-cf snippet node :epsilon)
              snippet-nodelist (snippet/update-cf snippet-node (snippet/snippet-list-containing snippet-node node) :epsilon)]
          (change-cf-parent snippet-nodelist (.getParent node)))))
    (let [snippet (snippetgroup/snippetgroup-snippet-for-node snippetgroup node)
          var-parent (snippet/snippet-lvar-for-node snippet parent)
          new-gf-snippet (snippet/update-gf-with-args (change-cf-parent snippet node) node :deep (list var-parent))
          new-snippet (snippet/update-cf new-gf-snippet node :exact)]
      (snippetgroup/snippetgroup-replace-snippet snippetgroup snippet new-snippet)))
  
  
  (defn
    internal-user-defined-condition
    "Internal function to add user-defined-condition of given snippetgroup node-var and node-arg."
    [snippetgroup node-var node-arg function-string]
    (let [snippet (snippetgroup/snippetgroup-snippet-for-node snippetgroup node-var)
          snippet-arg (snippetgroup/snippetgroup-snippet-for-node snippetgroup node-arg)
          var-arg (snippet/snippet-lvar-for-node snippet-arg node-arg)
          new-snippet (add-user-defined-condition snippet node-var (str function-string " " var-arg))]
      (snippetgroup/snippetgroup-replace-snippet snippetgroup snippet new-snippet)))
  
  (defn
    match-invocation-declaration
    "Match Relation between ASTNode invocation with it's declaration."
    [snippetgroup node-var node-arg]
    (internal-user-defined-condition snippetgroup node-var node-arg "method-dec"))
  
  (defn
    match-variable-declaration
    "Match Relation between ASTNode variable with it's declaration."
    [snippetgroup node-var node-arg]
    (internal-user-defined-condition snippetgroup node-var node-arg "var-dec"))
  
  (defn
    match-variable-samebinding
    "Match Relation between ASTNode variable with other variable with same binding."
    [snippetgroup node-var node-arg]
    (internal-user-defined-condition snippetgroup node-var node-arg "var-binding"))
  
  (defn
    match-variable-type
    "Match Relation between ASTNode invocation with it's type."
    [snippetgroup node-var node-arg]
    (internal-user-defined-condition snippetgroup node-var node-arg "var-type"))
  
  (defn 
    internal-bind-variables
    "Introduce logic variable to a given node, and other nodes with the same ast kind and identifier.
   Logic variable for the nodes will be generated. Then bind the variables."
    [snippet node newuservar count function-string]
    (let [uservar (symbol newuservar)]
      (defn update-snippet-var-cond [snippet value counter]
        (let [newvar     (symbol (str uservar counter))
              newsnippet (replace-by-variable snippet value newvar)]
          (if (= counter 1)
            newsnippet
            (add-user-defined-condition newsnippet value (str function-string " " uservar "1")))))
      (defn get-binding-variables [root node] ;returns list of nodes (variables) with the same binding as node 
        (damp.ekeko/ekeko [?var] 
                          (ast/child+ root ?var)
                          (runtime/ast-samekind-sameidentifier node ?var))) ;shud be ast-variable-samebinding
      (defn process-binding-variables [snippet nodes counter]
        (if (empty? nodes)
          snippet
          (let [new-snippet (update-snippet-var-cond snippet (first (first nodes)) counter)]
            (process-binding-variables new-snippet (rest nodes) (+ counter 1)))))
      (process-binding-variables snippet (get-binding-variables (:ast snippet) node) count)))
  
  (defn
    bind-variables
    [snippetgroup node newuservar]
    (defn process-binding-snippets [snippetlist counter resultlist]
      (if (empty? snippetlist)
        resultlist
        (let [new-snippet   (internal-bind-variables
                              (first snippetlist) node newuservar counter "var-binding")]
          (process-binding-snippets 
            (rest snippetlist) (+ counter 100) (concat resultlist (list new-snippet))))))
    (assoc snippetgroup :snippetlist 
           (process-binding-snippets (:snippetlist snippetgroup) 1 '())))
  
  (defn
    refer-variables-to-variable-declaration
    [snippetgroup node newuservar]
    (defn process-binding-snippets [snippetlist counter resultlist]
      (if (empty? snippetlist)
        resultlist
        (let [new-snippet   (internal-bind-variables
                              (first snippetlist) node newuservar counter "var-dec")]
          (process-binding-snippets 
            (rest snippetlist) (+ counter 100) (concat resultlist (list new-snippet))))))
    (assoc snippetgroup :snippetlist 
           (process-binding-snippets (:snippetlist snippetgroup) 1 '())))
  
  ;;not used
  (defn
    match-variable-typequalifiedname
    "Match Relation between ASTNode variable with it's type qualified name."
    [snippetgroup node-var node-type]
    (let [snippet-var (snippetgroup/snippetgroup-snippet-for-node snippetgroup node-var)
          snippet-type (snippetgroup/snippetgroup-snippet-for-node snippetgroup node-type)
          var-node (snippet/snippet-lvar-for-node snippet-var node-var)
          var-type (snippet/snippet-lvar-for-node snippet-type node-type)
          new-snippet-var (snippet/update-cf-with-args snippet-var node-var :var-type (list var-type))
          new-snippet-var-with-cond
          (add-logic-conditions
            new-snippet-var
            `((damp.ekeko.snippets.runtime/ast-variable-typequalifiedname ~var-node ~var-type)))
          new-group (snippetgroup/snippetgroup-replace-snippet snippetgroup snippet-var new-snippet-var-with-cond)]
      new-group))
  
  ;;not used
  (defn
    match-type-qualifiedname
    "Match Relation between ASTNode type with it's type qualified name."
    [snippetgroup node-var node-type]
    (let [snippet-var (snippetgroup/snippetgroup-snippet-for-node snippetgroup node-var)
          snippet-type (snippetgroup/snippetgroup-snippet-for-node snippetgroup node-type)
          var-node (snippet/snippet-lvar-for-node snippet-var node-var)
          var-type (snippet/snippet-lvar-for-node snippet-type node-type)
          new-snippet-var (snippet/update-cf-with-args snippet-var node-var :type-qname (list var-type))
          new-snippet-var-with-cond
          (add-logic-conditions
            new-snippet-var
            `((damp.ekeko.snippets.runtime/ast-type-qualifiedname ~var-node ~var-type)))
          new-group (snippetgroup/snippetgroup-replace-snippet snippetgroup snippet-var new-snippet-var-with-cond)]
      new-group))
  
  
  ;; Operator for Transformation
  ;; ---------------------------
  
  (defn 
    introduce-logic-variables-for-node
    "Introduce logic variable to all nodes with the same identifier as a given node.
   The given node is not in the snippet."
    [snippet node uservar]
    (defn get-nodes [root node]
      (damp.ekeko/ekeko [?var] 
                        (ast/child+ root ?var)
                        (runtime/ast-variable-sameidentifier node ?var))) 
    (defn process-introduce-variables [snippet nodes]
      (if (empty? nodes)
        snippet
        (let [new-snippet (replace-by-variable snippet (first (first nodes)) uservar)]
          (process-introduce-variables new-snippet (rest nodes)))))
    (process-introduce-variables snippet (get-nodes (:ast snippet) node)))
  
  (defn 
    introduce-logic-variables-for-snippet
    "Introduce logic variable to all nodes based on all user vars of a given template snippet."
    [snippet template-snippet]
    (defn process-introduce-variables-rec [snippet var2uservar]
      (if (empty? var2uservar)
        snippet
        (let [first-var (first var2uservar)
              new-snippet (introduce-logic-variables-for-node 
                            snippet 
                            (snippet/snippet-node-for-var template-snippet (key first-var))
                            (val first-var))]
          (process-introduce-variables-rec new-snippet (dissoc var2uservar (key first-var))))))
    (process-introduce-variables-rec snippet (:var2uservar template-snippet)))
  
  (defn
    change-name
    "Operator to change name with rule.
   Example: \"add[part-of-name]s\"."
    [snippet node template-snippet template-node string]
    (let [user-var (snippet/snippet-lvar-for-node template-snippet template-node)
          new-snippet (replace-by-variable snippet node user-var)
          rule (util/convert-string-to-rule string (str template-node) user-var)]
      (snippet/update-cf-with-args new-snippet node :change-name (list rule (str template-node)))))
  
  (defn
    t-internal-user-defined-condition
    [snippet node template-snippet template-node function-string]
    (let [bound-snippet (introduce-logic-variables-for-snippet snippet template-snippet)
          user-var (snippet/snippet-lvar-for-node template-snippet template-node)]
      (add-user-defined-condition bound-snippet node (str function-string " " user-var))))
  
  (defn
    t-add-node-after
    "Operator to add node after original node."
    [snippet node template-snippet template-node]
    (t-internal-user-defined-condition snippet node template-snippet template-node "add-node-after"))
  
  (defn
    t-add-node-before
    "Operator to add node before original node."
    [snippet node template-snippet template-node]
    (t-internal-user-defined-condition snippet node template-snippet template-node "add-node-before"))
  
  (defn
    t-add-member-node
    "Operator to replace node."
    [snippet node template-snippet template-node]
    (t-internal-user-defined-condition snippet node template-snippet template-node "add-member-node"))
  
  (defn
    t-replace-node
    "Operator to replace node."
    [snippet node template-snippet template-node]
    (t-internal-user-defined-condition snippet node template-snippet template-node "replace-node"))
  
  
  )

(defn
  register-callbacks 
  []
  
  
  )

(register-callbacks)


