(ns 
  ^{:doc "Matching strategies for snippet-driven querying."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.matching
  (:require [clojure.core.logic :as cl])
  
  (:require [damp.ekeko.snippets 
             [directives :as directives]
             [util :as util]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [parsing :as parsing]
             [runtime :as runtime]])
  (:require 
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt 
     [astnode :as astnode]
     [ast :as ast]])
  (:import  [org.eclipse.jdt.core.dom.rewrite ASTRewrite])
  )


(defn
  make-epsilon-function
  "Returns a function that does not generate any conditions for the given AST node of a code snippet."
  [snippet-val]
  (fn [snippet]
    '()))

(declare ast-primitive-as-expression)


;; Grounding Functions
;; -------------------

(defn
  gf-minimalistic
  "Only generates grounding conditions for the root node of the snippet."
  [snippet-ast]
  (let [snippet-ast-keyw (astnode/ekeko-keyword-for-class-of snippet-ast)]
    (fn [snippet] 
      (if 
        (= snippet-ast (:ast snippet))
        (let [var-match (snippet/snippet-var-for-node snippet snippet-ast)] 
          `((ast/ast ~snippet-ast-keyw ~var-match)))
        '()))))

(declare directive-parent)

(defn 
  gf-member-exact
  "Returns a function that will generate grounding conditions for the given AST node of a code snippet:
   For AST node is the member of the list : condition depends on cf of list owner
       cf = exact    -> ((el/equals ~var-el (.get ~var-list ~idx-el)))
       cf = contains -> ((el/contains ~var-list ~var-el))"
  [snippet-ast]
  (fn [snippet] 
    (if 
      (= snippet-ast (:ast snippet))
      (let [snippet-ast-keyw (astnode/ekeko-keyword-for-class-of snippet-ast)
            var-match (snippet/snippet-var-for-node snippet snippet-ast)] 
        `((ast/ast ~snippet-ast-keyw ~var-match)
           ))
      (let [bounddirectives (snippet/snippet-bounddirectives-for-node snippet snippet-ast)
            var-match       (snippet/snippet-var-for-node snippet snippet-ast) 

            ;todo: check for parent directives that might require different grounding
            list-owner      (snippet/snippet-list-containing snippet snippet-ast)
            list-owner-directives (snippet/snippet-bounddirectives-for-node snippet list-owner)
            
            list-match       (snippet/snippet-var-for-node snippet list-owner)
            list-raw         (:value list-owner)
            list-match-raw   (util/gen-readable-lvar-for-value list-raw)
            index-match     (.indexOf list-raw snippet-ast)
            
            cf-list-owner :toeliminate
            
            foo 
            (do 
              (println "bounddirectives " bounddirectives ) 
              (println "var-match " var-match)
              (println "list-owner " list-owner)
              (println "list-owner-directives " list-owner-directives)
              (println "list-match " list-match)
              (println "list-raw " list-raw)
              (println "list-match-raw " list-match-raw)
              (println "index-match " index-match))
              
            conditions 
            (cond
              ;todo: put at bottom of cond, check for parent directives first
              (directives/bounddirectives-include-directive? bounddirectives directive-parent)
              `((el/equals ~var-match (.get ~list-match-raw ~index-match)))
              
              ;todo: add variants of parent with arguments
              
	             (or (= cf-list-owner :contains) 
	                 (= cf-list-owner :contains-eq-size))
	             `((el/contains ~list-match-raw ~var-match))
              
	             (= cf-list-owner :contains-eq-order)
	             (if (> index-match 0)
	               (let [prev-member    (.get list-raw (- index-match 1))
	                     var-match-prev (snippet/snippet-var-for-node snippet prev-member)]
	                 `((el/contains ~list-match-raw ~var-match)
	                    (el/succeeds (> (.indexOf ~list-match-raw ~var-match) (.indexOf ~list-match-raw ~var-match-prev)))))
	               `((el/contains ~list-match-raw ~var-match)))
              
	             (= cf-list-owner :contains-repetition)
              (if (> index-match 0)
	               (let [element-conditions 
	                     (for [n  (take index-match (iterate inc 0))]
	                       (let [nth-member    (.get list-raw n)
	                             var-match-nth (snippet/snippet-var-for-node snippet nth-member)]
	                         `(el/fails (el/equals ~var-match ~var-match-nth))))]
	                 (println element-conditions)
	                 `((el/contains ~list-match-raw ~var-match)
                     ~@element-conditions))
	               `((el/contains ~list-match-raw ~var-match)))
              
              :default
              '())]
        `((cl/fresh [~list-match-raw] 
                    (ast/value-raw ~list-match ~list-match-raw)
                    ~@conditions))))))
    
(defn 
  gf-node-exact
  "Returns a function that will generate grounding conditions for the given AST node of a code snippet:
   For AST node is the value of a property: ((has :property ?var-for-owner-match ?var-for-node-match))"
  [snippet-ast]
  (fn [snippet] 
      (let [snippet-owner  (astnode/owner snippet-ast)
            var-match       (snippet/snippet-var-for-node snippet snippet-ast) 
            var-match-owner (snippet/snippet-var-for-node snippet snippet-owner)
            owner-property  (astnode/owner-property snippet-ast) 
            owner-property-keyw (astnode/ekeko-keyword-for-property-descriptor owner-property)]
        `((ast/has ~owner-property-keyw ~var-match-owner ~var-match)))))

(defn
  node|listmember? 
  "Checks whether value is an ASTNode that is the member of a list."
  [snippet-val]
  (and (instance? org.eclipse.jdt.core.dom.ASTNode snippet-val)  
         (not (instance? org.eclipse.jdt.core.dom.CompilationUnit snippet-val))  
         (astnode/property-descriptor-list? (astnode/owner-property snippet-val))))

(defn 
  gf-exact
  [snippet-val]
  (if
    (node|listmember? snippet-val)
    (gf-member-exact snippet-val)
    (gf-minimalistic snippet-val)))

(defn 
  gf-deep
  "Returns a function that will generate grounding conditions for the given AST node of a code snippet:
   For AST node is the value of a property: ((child+ ?var-for-owner-match ?var-for-node-match))"
  [snippet-ast]
  (fn [snippet] 
      (let [var-match       (snippet/snippet-var-for-node snippet snippet-ast)
            ;todo
            ;args            (snippet/snippet-grounder-args-for-node snippet snippet-ast)
            args []
            var-match-owner 
            (if (empty? args)
              (snippet/snippet-var-for-node snippet (astnode/owner snippet-ast)) ;for gf :child+
              (symbol (first args)))]                                                            ;for gf :deep
        `((ast/child+ ~var-match-owner ~var-match)))))

(comment
(defn 
  make-grounding-function
  [type]
  (cond 
    (= type :minimalistic)
    gf-minimalistic
    (= type :exact)
    gf-exact
    (= type :deep)
    gf-deep
    (= type :child+)
    gf-deep
    (= type :epsilon)
    make-epsilon-function
    :default
    (throw (Exception. (str "Unknown grounding function type: " type)))))
)

(def parent gf-exact)


;; Constraining Functions
;; ----------------------

;;TODO: not in sync with node-filtered-ekeko-properties (or something like that)


(defn 
  is-ignored-property?
  [property-keyw]
  (= property-keyw :javadoc))

(defn 
  cf-node-exact
    "Returns a function that will generate constraining conditions for the given property value of a code snippet:
     For ASTNode instances: ((ast :kind-of-node ?var-for-node-match)  
                               (has :property1 ?var-for-node-match ?var-for-child1-match)
                               (has :property2 ?var-for-node-match ''primitive-valued-child-as-string''))
                               ...."
  [snippet-ast]
  (fn [snippet]
    (let [snippet-keyw       (astnode/ekeko-keyword-for-class-of snippet-ast)
          snippet-properties (astnode/node-ekeko-properties snippet-ast)
          var-match          (snippet/snippet-var-for-node snippet snippet-ast)
          child-conditions 
              (for [[property-keyw retrievalf] 
                    (seq snippet-properties)
                    :let [value     (retrievalf) 
                          var-value (snippet/snippet-var-for-node snippet value)]]
                (if (not (is-ignored-property? property-keyw))
                  `(ast/has ~property-keyw ~var-match ~var-value)))
          filtered-child-conditions (filter (fn [x] (not (nil? x))) child-conditions)]
      (if 
        (= snippet-ast (:ast snippet)) 
        `(~@filtered-child-conditions) ;because snippet root is already ground to an ast of the right kind
        `((ast/ast ~snippet-keyw ~var-match)
           ~@child-conditions)))))

(defn 
  internal-cf-list
    "Returns constraining-conditions for the given property value of a code snippet.
     For Ekeko wrappers of ASTNode$NodeList instances: 
         (listvalue ?var-match)
         (fresh [?newly-generated-var]
           (value-raw ?var-match ?newly-generated-var)
           (equals snippet-list-size (.size ?newly-generated-var)) {If type = :samesize}
           (element-conditions)"
  [snippet snippet-val type] ; function-element-condition]
  (let [lst (:value snippet-val)
        snippet-list-size (.size lst)
        var-match (snippet/snippet-var-for-node snippet snippet-val)
        var-match-raw (util/gen-readable-lvar-for-value lst) ;freshly generated, not included in snippet datastructure ..
        size-condition
        (if (= type :samesize)
          `((el/equals ~snippet-list-size (.size ~var-match-raw)))
          `())]
        ;element-conditions -> moved to gf-member-exact
        ;(for [element lst
        ;      :let [idx-el (.indexOf lst element)
        ;            var-el (snippet/snippet-var-for-node snippet element)]]
        ;  (function-element-condition var-match-raw var-el idx-el))]
    `((ast/value|list ~var-match)
      (cl/fresh [~var-match-raw] 
         (ast/value-raw ~var-match ~var-match-raw)
         ~@size-condition))))
       ;~@element-conditions)))

(defn
  cf-list-exact
  "Returns a function that will generate constraining conditions for the given property value of a code snippet.
   Conditions : - size of list need to be the same"
  [snippet-val]
  (fn [snippet] 
    (internal-cf-list snippet snippet-val :samesize))) 

(defn
  cf-primitive-exact
  "Returns a function that will generate constraining conditions for the given primitive property value of a code snippet.
   For Ekeko wrappers of primitive values (int/string/...):
      (primitivevalue ?var-match)
      (value-raw ?var-match <clojure-exp-evaluating-to-raw-value>))"
   [snippet-ast]
   (let [exp 
         (ast-primitive-as-expression (:value snippet-ast))]
     (fn [snippet]
       (let [var-match 
             (snippet/snippet-var-for-node snippet snippet-ast)]
       `((ast/value|primitive ~var-match)
          (ast/value-raw ~var-match ~exp))))))

(defn
  cf-nil-exact
  "Returns a function that will generate constraining conditions for the given primitive property value of a code snippet.
   For Ekeko wrappers of Java null: 
      (nullvalue ?var-match)"
   [snippet-ast]
   (fn [snippet]
     (let [var-match 
           (snippet/snippet-var-for-node snippet snippet-ast)]
       `((ast/value|null ~var-match)))))

(defn 
  cf-exact
  [snippet-val]
  (cond
    (astnode/ast? snippet-val)
    (cf-node-exact snippet-val)
    (astnode/lstvalue? snippet-val)
    (cf-list-exact snippet-val)
    (astnode/primitivevalue? snippet-val)
    (cf-primitive-exact snippet-val)
    (astnode/nilvalue? snippet-val)
    (cf-nil-exact snippet-val)))



(def exact cf-exact)

;; Constraining Functions
;; Generalized
;; ----------------------

(defn
  cf-variable
  "Returns a function that will generate a condition that will unify the match for the
   given code snippet AST node with a user-provided logic variable:
      (== ?uservar ?var-match)"
  [snippet-ast]
   (fn [snippet]
     (let [var-match 
           (snippet/snippet-var-for-node snippet snippet-ast)
           var-userprovided
           (snippet/snippet-uservar-for-var snippet var-match)]
       (if (nil? var-userprovided)
         '() 
         `((cl/== ~var-userprovided ~var-match))))))

(defn
  cf-epsilon-with-variable
  [snippet-val]
  (fn [snippet] 
     (let [var-match 
           (snippet/snippet-var-for-node snippet snippet-val)
           var-userprovided
           (snippet/snippet-uservar-for-var snippet var-match)]
       (if (nil? var-userprovided)
         ((make-epsilon-function snippet-val) snippet)
         (concat 
           ((gf-node-exact snippet-val) snippet)
           ((cf-variable snippet-val) snippet))))))

(defn
  cf-exact-with-variable
  [snippet-val]
  (fn [snippet] 
    (concat 
      ((cf-exact snippet-val) snippet)
      ((cf-variable snippet-val) snippet))))

(defn
  cf-list-relax-size
  "Returns a function that will generate constraining conditions for the given property value of a code snippet.
   Conditions : - size of list no need to be the same"
  [snippet-val]
  (fn [snippet] 
    (internal-cf-list snippet snippet-val :notsamesize))) 
  
(defn
  cf-list-relax-size-with-variable
  [snippet-val]
  (fn [snippet] 
    (concat 
      ((cf-list-relax-size snippet-val) snippet)
      ((cf-variable snippet-val) snippet))))

(defn 
  cf-subtype
    "Returns a function that will generate constraining conditions for the given property value of a code snippet:
     For ASTNode instances: ((type-subtypematch-logictype :kind-of-node ?var-for-node ?var-for-node-match)
                               (ast :kind-of-node ?var-for-node)  
                               (has :property1 ?var-for-node ''primitive-valued-child-as-string''))
                               ....
     ?var-for-node is new logic variable."
  [snippet-ast]
  (fn [snippet]
    (let [snippet-keyw       (astnode/ekeko-keyword-for-class-of snippet-ast)
          snippet-properties (astnode/node-ekeko-properties snippet-ast)
          var-match          (snippet/snippet-var-for-node snippet snippet-ast)
          var-node           (util/gen-lvar)
          child-conditions 
              (for [[property-keyw retrievalf] 
                    (seq snippet-properties)
                    :let [value     (retrievalf) 
                          var-value (snippet/snippet-var-for-node snippet value)]]
                `(ast/has ~property-keyw ~var-node ~var-value))]
      `((cl/fresh [~var-node]
         (ast/ast ~snippet-keyw ~var-node)
           ~@child-conditions
           (runtime/type-relaxmatch-subtype ~snippet-keyw ~var-node ~var-match))))))

(defn
  cf-subtype-with-variable
  [snippet-val]
  (fn [snippet] 
    (concat 
      ((cf-subtype snippet-val) snippet)
      ((cf-variable snippet-val) snippet))))

(defn
  cf-relax-typeoftype
  [snippet-val]
  (fn [snippet]
    (let [cond-exact ((cf-exact snippet-val) snippet)
          snippet-var (snippet/snippet-var-for-node snippet snippet-val)
          type-var (snippet/snippet-var-for-node snippet (.getType snippet-val))]
      `((cl/conde 
          [~@cond-exact]
          [(el/equals ~snippet-var ~type-var)])))))

(defn 
  cf-variable-declaration-with-initializer
    "Returns a function that will generate constraining conditions for the given property value of a code snippet:
     For ASTNode instances: (assignment-relaxmatch-variable-declaration ?var-for-node-match ?var-left ?var-right)"
  [snippet-ast]
  (fn [snippet]
    (let [var-match    (snippet/snippet-var-for-node snippet snippet-ast)
          var-left     (snippet/snippet-var-for-node snippet (.getLeftHandSide (.getExpression snippet-ast)))
          var-right    (snippet/snippet-var-for-node snippet (.getRightHandSide (.getExpression snippet-ast)))]
      `((runtime/assignment-relaxmatch-variable-declaration ~var-match ~var-left ~var-right)))))

(defn
  cf-variable-declaration-with-variable
  [snippet-val]
  (fn [snippet] 
    (concat 
      ((cf-variable-declaration-with-initializer snippet-val) snippet)
      ((cf-variable snippet-val) snippet))))

(defn 
  ast-conditions
  "Returns a list of logic conditions that will retrieve matches for the given snippet-ast in snippet."
  [snippet ast]
  (defn 
    conditions
    [ast-or-list]
    (concat ((gf-exact ast-or-list) snippet)
            ((cf-exact ast-or-list) snippet)))
  (let [query (atom '())]
    (util/walk-jdt-node 
      ast
      (fn [astval]  (swap! query concat (conditions astval)))
      (fn [lstval] (swap! query concat (conditions lstval)))
      (fn [primval] (swap! query concat (conditions primval)))
      (fn [nilval] (swap! query concat (conditions nilval))))
    @query))

(defn 
  cf-negated
    "Returns a function that will generate constraining conditions for the given property value of a code snippet:
     For ASTNode instances: (fails (all (node-conditions) (child-conditions)))."
  [snippet-ast]
  (fn [snippet]
    (let [conditions-of-ast (ast-conditions snippet snippet-ast)]
      `((el/fails (cl/all ~@conditions-of-ast))))))

(defn
  cf-negated-with-variable
  [snippet-val]
  (fn [snippet] 
    (concat 
      ((cf-negated snippet-val) snippet)
      ((cf-variable snippet-val) snippet))))

(defn 
  cf-relax-loop
    "Returns a function that will generate constraining conditions for the given property value of a code snippet:
     For ASTNode instances: ((ast :ForStatement/WhileStatement/DoStatement ?var-for-node-match)  
                               (has :property1 ?var-for-node-match ?var-for-child1-match)
                               (has :property2 ?var-for-node-match ''primitive-valued-child-as-string''))
                               ...."
  [snippet-ast]
  (fn [snippet]
    (let [snippet-keyw       (astnode/ekeko-keyword-for-class-of snippet-ast)
          snippet-properties (astnode/node-ekeko-properties snippet-ast)
          var-match          (snippet/snippet-var-for-node snippet snippet-ast)
          child-conditions 
              (for [[property-keyw retrievalf] 
                    (seq snippet-properties)
                    :let [value     (retrievalf) 
                          var-value (snippet/snippet-var-for-node snippet value)]]
                (if (and (not (is-ignored-property? property-keyw))
                         (not (= property-keyw :updaters))
                         (not (= property-keyw :initializers)))
                  `(ast/has ~property-keyw ~var-match ~var-value)))]
      `((cl/conde [(ast/ast :ForStatement ~var-match)]
                  [(ast/ast :WhileStatement ~var-match)]
                  [(ast/ast :DoStatement ~var-match)])
         ~@child-conditions))))

(defn
  cf-relax-loop-with-variable
  [snippet-val]
  (fn [snippet] 
    (concat 
      ((cf-relax-loop snippet-val) snippet)
      ((cf-variable snippet-val) snippet))))

(comment

(defn
  make-constraining-function
  [type]
  (cond
    (= type :variable)
    cf-variable
    
    ;below has not been checked
    (= type :exact)
    cf-exact-with-variable
    (= type :any)
    cf-epsilon-with-variable
    (= type :child+)
    cf-list-relax-size-with-variable
    (= type :contains)
    cf-list-relax-size-with-variable
    (= type :contains-eq-size)
    cf-exact-with-variable
    (= type :contains-eq-order)
    cf-list-relax-size-with-variable
    (= type :contains-repetition)
    cf-list-relax-size-with-variable
    (= type :exact-variable)
    cf-exact-with-variable
    (= type :relax-type)
    cf-subtype-with-variable
    (= type :relax-typeoftype)
    cf-relax-typeoftype
    (= type :relax-assign)
    cf-variable-declaration-with-variable
    (= type :relax-branch)
    cf-exact-with-variable
    (= type :negated)
    cf-negated-with-variable
    (= type :relax-loop)
    cf-relax-loop-with-variable
    (= type :method-dec)
    cf-exact-with-variable
    (= type :var-dec)
    cf-exact-with-variable
    (= type :var-binding)
    cf-exact-with-variable
    (= type :var-type)
    cf-exact-with-variable
    (= type :var-qname)
    cf-exact-with-variable
    (= type :type-qname)
    cf-exact-with-variable
    (= type :epsilon)
    cf-epsilon-with-variable
    :default
    (throw (Exception. (str "Unknown constraining function type: " type))))) 

)




(defn
  to-literal-string
  [value]
  (let [qstr (.toString value)]
    `~qstr))

(defn 
  ast-primitive-as-expression
  "Returns the string representation of a primitive-valued JDT node (e.g., instances of Modifier.ModifierKeyword)."
  [primitive]
  ;could dispatch on this as well
  (cond (or (true? primitive) (false? primitive))
        primitive
        (number? primitive)
        primitive
        (instance? org.eclipse.jdt.core.dom.Modifier$ModifierKeyword primitive)
        `(runtime/to-modifier-keyword ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.PrimitiveType$Code primitive)
        `(runtime/to-primitive-type-code ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.Assignment$Operator primitive)
        `(runtime/to-assignment-operator ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.InfixExpression$Operator primitive)
        `(runtime/to-infix-expression-operator ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.PrefixExpression$Operator primitive)
        `(runtime/to-prefix-expression-operator ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.PostfixExpression$Operator primitive)
        `(runtime/to-postfix-expression-operator ~(to-literal-string primitive))
        (nil? primitive) 
        (throw (Exception. (str "Encountered a null-valued property value that should have been wrapped by Ekeko.")))
        :else (to-literal-string primitive)))


;;Registering directives
;;----------------------

(def
  directive-exact
  (damp.ekeko.snippets.directives.Directive. 
    "Node type and properties match type and properties of match."
    []
    exact 
    ))

(def 
  directive-parent
  (damp.ekeko.snippets.directives.Directive.
    "Node parent matches match parent."
    []
    parent
    ))

(def 
  directives-constraining
  [directive-exact])

(def
  directives-grounding
  [directive-parent])
  
(defn 
  registered-constraining-directives
  "Returns collection of registered constraining directives."
  []
  directives-constraining)

(defn 
  registered-grounding-directives
  "Returns collection of registered grounding directives."
  []
  directives-grounding)

(defn
  registered-grounding-directive?
  "Succeeds for registered directives that are grounding."
  [directive]
  (some #{directive} directives-grounding))

(defn
  registered-constraining-directive?
  "Succeeds for registered directives that are constraining."
  [directive]
  (some #{directive} directives-constraining))


;; Constructing Snippet instances with default matching directives
;; ---------------------------------------------------------------

(defn 
  bind-nullary-directive
  [directive snippet value]
  (directives/make-bounddirective 
    directive
    (directives/directive-bindings-for-directiveoperands-and-match
      snippet
      value
      directive)))

(defn
  default-directives
  "Returns default matching directives for given snippet and element of the snippet element."
  [snippet value]
  (list 
    (bind-nullary-directive directive-exact snippet value)
    (bind-nullary-directive directive-parent snippet value)))

(defn 
  jdt-node-as-snippet
  "Interpretes the given JDT ASTNode as a snippet with default matching 
   strategies (i.e., grounding=:exact, constaining=:exact)
   for the values of its properties.
   note: Only used to test operators related binding."
  [n]
  (defn 
    assoc-snippet-value
    [snippet value]
    (let [lvar (util/gen-readable-lvar-for-value value)]
      (->
        snippet
        (assoc-in [:ast2var value] lvar)
        (assoc-in [:ast2bounddirectives value] 
                  (default-directives snippet value))
        (assoc-in [:var2ast lvar] value))))
  
  (let [snippet (atom (damp.ekeko.snippets.snippet.Snippet. n {} {} {} {} '() nil nil {} {}))]
    (util/walk-jdt-node 
      n
      (fn [astval] (swap! snippet assoc-snippet-value astval))
      (fn [lstval] 
        (swap! snippet assoc-snippet-value lstval))
      (fn [primval]  (swap! snippet assoc-snippet-value primval))
      (fn [nilval] (swap! snippet assoc-snippet-value nilval)))
    @snippet))
  


;;TODO: is tracking really necessary?
;;seems to complicate things, need to reduce duplicated code
(defn 
  document-as-snippet
  "Parse Document doc as a snippet with default matching strategies 
   (i.e., grounding=:exact, constaining=:exact)
   for the values of its properties.
   Function ASTRewrite/track is called for each ASTNode to activate the Node Tracking in ASTRewrite." 
  [doc]
  
  (defn 
    make-astrewrite
    [node]
    (ASTRewrite/create (.getAST node)))
  
  (defn assoc-snippet-value [snippet value track]
    (let [lvar (util/gen-readable-lvar-for-value value)
          arrTrack [(util/class-simplename (class value))
                    (snippet/snippet-property-for-node snippet value) 
                    (.getStartPosition track) 
                    (.getLength track)]]
      (->
        snippet
        (assoc-in [:ast2var value] lvar)
        (assoc-in [:ast2bounddirectives value] 
                  (default-directives snippet value))
        (assoc-in [:var2ast lvar] value)
        (assoc-in [:track2ast arrTrack] value)
        (assoc-in [:ast2track value] arrTrack))))
  
  (let [n (parsing/parse-document doc)
        rw (make-astrewrite n)
        snippet (atom (damp.ekeko.snippets.snippet.Snippet. n {} {} {} {} '() doc rw {} {}))]
    (util/walk-jdt-node 
      n
      (fn [astval] 
        (swap! snippet assoc-snippet-value astval (.track rw astval)))
      (fn [lstval] 
        (swap! snippet assoc-snippet-value lstval (.track rw (:owner lstval))))
      (fn [primval]  (swap! snippet assoc-snippet-value primval (.track rw (:owner primval))))
      (fn [nilval] (swap! snippet assoc-snippet-value nilval (.track rw (:owner nilval)))))
    @snippet))


(defn 
  apply-rewrite 
  "Apply rewrite to snippet."
  [snippet]
  (let [rewrite (snippet/snippet-rewrite snippet)
        document (snippet/snippet-document snippet)]
    (.apply (.rewriteAST rewrite document nil) document)
    (let [newsnippet (document-as-snippet document)]
      (snippet/copy-snippet snippet newsnippet)))) 


(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_FROMDOCUMENT) document-as-snippet))

(register-callbacks)

