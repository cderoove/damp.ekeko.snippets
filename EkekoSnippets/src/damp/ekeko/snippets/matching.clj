(ns 
  ^{:doc "Matching strategies for snippet-driven querying."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.matching
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko.snippets 
             [util :as util]
             [representation :as representation]
             [runtime :as runtime]])
  (:require 
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt 
     [astnode :as astnode]
     [reification :as reification]]))

;; for each grounding or constraining function maker, there are additional arguments beside snippet-ast,
;; which can be accessed in snippet :ast2groundf or :ast2constrainf via getter function
;; snippet-grounder-args-for-node and snippet-constrainer-args-for-node (see representation.clj)

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
        (let [var-match (representation/snippet-var-for-node snippet snippet-ast)] 
          `((reification/ast ~snippet-ast-keyw ~var-match)))
        '()))))

  
(defn 
  gf-node-exact
  "Returns a function that will generate grounding conditions for the given AST node of a code snippet:
   For AST node is the value of a property: ((has :property ?var-for-owner-match ?var-for-node-match))"
  [snippet-ast]
  (fn [snippet] 
      (let [snippet-owner  (astnode/owner snippet-ast)
            var-match       (representation/snippet-var-for-node snippet snippet-ast) 
            var-match-owner (representation/snippet-var-for-node snippet snippet-owner)
            owner-property  (astnode/owner-property snippet-ast) 
            owner-property-keyw (astnode/ekeko-keyword-for-property-descriptor owner-property)]
        `((reification/has ~owner-property-keyw ~var-match-owner ~var-match)))))

(defn 
  gf-node-deep
  "Returns a function that will generate grounding conditions for the given AST node of a code snippet:
   For AST node is the value of a property: ((child+ ?var-for-owner-match ?var-for-node-match))"
  [snippet-ast]
  (fn [snippet] 
      (let [var-match       (representation/snippet-var-for-node snippet snippet-ast) 
            var-match-owner (first (representation/snippet-grounder-args-for-node snippet snippet-ast))]
        `((reification/child+ ~var-match-owner ~var-match)))))

(defn 
  make-grounding-function
  [type]
  (cond 
    (= type :minimalistic)
    gf-minimalistic
    (= type :node-exact)
    gf-node-exact
    (= type :node-deep)
    gf-node-deep
    (= type :epsilon)
    make-epsilon-function
    :default
    (throw (Exception. (str "Unknown grounding function type: " type)))))

;; Constraining Functions
;; ----------------------

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
          var-match          (representation/snippet-var-for-node snippet snippet-ast)
          child-conditions 
              (for [[property-keyw retrievalf] 
                    (seq snippet-properties)
                    :let [value     (retrievalf) 
                          var-value (representation/snippet-var-for-node snippet value)]]
                (if (not (is-ignored-property? property-keyw))
                  `(reification/has ~property-keyw ~var-match ~var-value)))
          filtered-child-conditions (filter (fn [x] (not (nil? x))) child-conditions)]
      (if 
        (= snippet-ast (:ast snippet)) 
        `(~@filtered-child-conditions) ;because snippet root is already ground to an ast of the right kind
        `((reification/ast ~snippet-keyw ~var-match)
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
  [snippet snippet-val type function-element-condition]
  (let [lst (representation/snippet-value-for-node snippet snippet-val)
        snippet-list-size (.size lst)
        var-match (representation/snippet-var-for-node snippet snippet-val)
        var-match-raw (representation/snippet-var-for-node snippet lst)
        size-condition
        (if (= type :samesize)
          `((el/equals ~snippet-list-size (.size ~var-match-raw)))
          `())
        element-conditions 
        (for [element lst
              :let [idx-el (.indexOf lst element)
                    var-el (representation/snippet-var-for-node snippet element)]]
          (function-element-condition var-match-raw var-el idx-el))]
    `((reification/listvalue ~var-match)
       (reification/value-raw ~var-match ~var-match-raw)
       ~@size-condition
       ~@element-conditions)))

(defn
  cf-list-exact
  "Returns a function that will generate constraining conditions for the given property value of a code snippet.
   Conditions : - size of list need to be the same
                - for each element (el/equals ?var-el (.get ?var-list ?idx-el))"
  [snippet-val]
  (fn [snippet] 
    (internal-cf-list snippet snippet-val :samesize 
                      (fn [var-list var-el idx-el] 
                        `(el/equals ~var-el (.get ~var-list ~idx-el))))))

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
             (representation/snippet-var-for-node snippet snippet-ast)]
       `((reification/primitivevalue ~var-match)
          (reification/value-raw ~var-match ~exp))))))

(defn
  cf-nil-exact
  "Returns a function that will generate constraining conditions for the given primitive property value of a code snippet.
   For Ekeko wrappers of Java null: 
      (nullvalue ?var-match)"
   [snippet-ast]
   (fn [snippet]
     (let [var-match 
           (representation/snippet-var-for-node snippet snippet-ast)]
       `((reification/nullvalue ~var-match)))))

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

;; Constraining Functions
;; Generalized
;; ----------------------

(defn
  cf-list-contains-with-same-size
  "Returns a function that will generate constraining conditions for the given property value of a code snippet.
   Conditions : - size of list need to be the same
                - for each element (el/contains ?var-list ?var-el)"
  [snippet-val]
  (fn [snippet] 
    (internal-cf-list snippet snippet-val :samesize 
                      (fn [var-list var-el idx-el] 
                        `(el/contains ~var-list ~var-el)))))

(defn
  cf-list-contains
  "Returns a function that will generate constraining conditions for the given property value of a code snippet.
   Conditions : - size of list no need to be the same
                - for each element (el/contains ?var-list ?var-el)"
  [snippet-val]
  (fn [snippet] 
    (internal-cf-list snippet snippet-val :notsamesize 
                      (fn [var-list var-el idx-el] 
                        `(el/contains ~var-list ~var-el)))))
  
(defn 
  internal-cf-list-contains-with-relative-order
  "Returns constraining conditions of relative elements order for the given nodelist of a code snippet.
   (succeeds (< (.indexOf alist el1) (.indexOf alist el2)))
   (succeeds (< (.indexOf alist el2) (.indexOf alist el3))) ... "
  [snippet var-list alist]
  (if (empty?  (next alist))
    '()
    (let [var-el1 (representation/snippet-var-for-node snippet (first alist))
          var-el2 (representation/snippet-var-for-node snippet (fnext alist))]
      (cons
        `(el/succeeds (< (.indexOf ~var-list ~var-el1) (.indexOf ~var-list ~var-el2)))
        (internal-cf-list-contains-with-relative-order snippet var-list (next alist))))))
    
(defn 
  cf-list-contains-with-relative-order
  [snippet-val]
  (fn [snippet]
    (let [lst (representation/snippet-value-for-node snippet snippet-val)
          var-match-raw (representation/snippet-var-for-node snippet lst)]
      (concat
        ((cf-list-contains snippet-val) snippet)
        (internal-cf-list-contains-with-relative-order snippet var-match-raw lst)))))

(defn 
  internal-cf-list-contains-with-repetition
  "Returns constraining conditions of list having repetition elements for the given nodelist of a code snippet.
   (fails (equals el1 el2)) 
   (fails (equals el1 el3)) ... "
  [snippet alist]
  (if (empty?  (next alist))
    '()
    (let [var-first-el (representation/snippet-var-for-node snippet (first alist))
          element-conditions 
          (for [element (next alist)
                :let [var-el (representation/snippet-var-for-node snippet element)]]
            `(el/fails (el/equals ~var-first-el ~var-el)))]
      (concat
        `(~@element-conditions)
        (internal-cf-list-contains-with-repetition snippet (next alist))))))
    
(defn 
  cf-list-contains-with-repetition
  [snippet-val]
  (fn [snippet]
    (let [lst (representation/snippet-value-for-node snippet snippet-val)]
      (concat
        ((cf-list-contains snippet-val) snippet)
        (internal-cf-list-contains-with-repetition snippet lst)))))

(defn
  cf-variable
  "Returns a function that will generate a condition that will unify the match for the
   given code snippet AST node with a user-provided logic variable:
      (== ?uservar ?var-match)"
  [snippet-ast]
   (fn [snippet]
     (let [var-match 
           (representation/snippet-var-for-node snippet snippet-ast)
           var-userprovided
           (representation/snippet-uservar-for-var snippet var-match)]
       (if (nil? var-userprovided)
         '() 
         `((cl/== ~var-userprovided ~var-match))))))

(defn
  cf-exact-with-variable
  [snippet-val]
  (fn [snippet] 
    (concat 
      ((cf-exact snippet-val) snippet)
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
          var-match          (representation/snippet-var-for-node snippet snippet-ast)
          var-node           (util/gen-lvar)
          child-conditions 
              (for [[property-keyw retrievalf] 
                    (seq snippet-properties)
                    :let [value     (retrievalf) 
                          var-value (representation/snippet-var-for-node snippet value)]]
                `(reification/has ~property-keyw ~var-node ~var-value))]
      `((cl/fresh [~var-node]
         (reification/ast ~snippet-keyw ~var-node)
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
  cf-variable-declaration-with-initializer
    "Returns a function that will generate constraining conditions for the given property value of a code snippet:
     For ASTNode instances: (assignment-relaxmatch-variable-declaration ?var-for-node-match ?var-left ?var-right)"
  [snippet-ast]
  (fn [snippet]
    (let [var-match    (representation/snippet-var-for-node snippet snippet-ast)
          var-left     (representation/snippet-var-for-node snippet (.getLeftHandSide (.getExpression snippet-ast)))
          var-right    (representation/snippet-var-for-node snippet (.getRightHandSide (.getExpression snippet-ast)))]
      `((runtime/assignment-relaxmatch-variable-declaration ~var-match ~var-left ~var-right)))))

(defn 
  ast-conditions
  "Returns a list of logic conditions that will retrieve matches for the given snippet-ast in snippet."
  [snippet ast]
  (defn 
    conditions
    [ast-or-list]
    (concat ((gf-minimalistic ast-or-list) snippet)
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
  make-constraining-function
  [type]
  (cond
    (= type :exact)
    cf-exact
    (= type :list-exact)
    cf-list-exact
    (= type :same-size)
    cf-list-contains-with-same-size
    (= type :contains-elements)
    cf-list-contains
    (= type :relative-order)
    cf-list-contains-with-relative-order
    (= type :elements-repetition)
    cf-list-contains-with-repetition
    (= type :any-element)
    make-epsilon-function
    (= type :variable)
    cf-variable
    (= type :variable-info)
    cf-variable
    (= type :exact-variable)
    cf-exact-with-variable
    (= type :subtype)
    cf-subtype-with-variable
    (= type :dec-init)
    cf-variable-declaration-with-initializer
    (= type :relax-branch)
    cf-exact-with-variable
    (= type :negated)
    cf-negated
    (= type :method-dec)
    cf-exact-with-variable
    (= type :var-dec)
    cf-exact-with-variable
    (= type :var-binding)
    cf-exact-with-variable
    (= type :var-type)
    cf-exact-with-variable
    (= type :var-typename)
    cf-exact-with-variable
    (= type :type-qname)
    cf-exact-with-variable
    (= type :type-qnames)
    cf-exact-with-variable
    (= type :epsilon)
    make-epsilon-function
    :default
    (throw (Exception. (str "Unknown constraining function type: " type))))) 

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
        `(runtime/to-modifier-keyword ~(.toString primitive))
        (instance? org.eclipse.jdt.core.dom.PrimitiveType$Code primitive)
        `(runtime/to-primitive-type-code ~(.toString primitive))
        (instance? org.eclipse.jdt.core.dom.Assignment$Operator primitive)
        `(runtime/to-assignment-operator ~(.toString primitive))
        (instance? org.eclipse.jdt.core.dom.InfixExpression$Operator primitive)
        `(runtime/to-infix-expression-operator ~(.toString primitive))
        (instance? org.eclipse.jdt.core.dom.PrefixExpression$Operator primitive)
        `(runtime/to-prefix-expression-operator ~(.toString primitive))
        (instance? org.eclipse.jdt.core.dom.PostfixExpression$Operator primitive)
        `(runtime/to-postfix-expression-operator ~(.toString primitive))
        (nil? primitive) 
        (throw (Exception. (str "Encountered a null-valued property value that should have been wrapped by Ekeko.")))
        :else  (.toString primitive)))




