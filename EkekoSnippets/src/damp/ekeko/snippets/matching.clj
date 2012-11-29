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
        `((has ~owner-property-keyw ~var-match-owner ~var-match)))))

(defn 
  gf-node-deep
  "Returns a function that will generate grounding conditions for the given AST node of a code snippet:
   For AST node is the value of a property: ((child+ ?var-for-owner-match ?var-for-node-match))"
  [snippet-ast]
  (fn [snippet] 
      (let [snippet-owner  (astnode/owner snippet-ast)
            var-match       (representation/snippet-var-for-node snippet snippet-ast) 
            var-match-owner (representation/snippet-var-for-node snippet snippet-owner)]
        `((child+ ~var-match-owner ~var-match)))))

 ;still not sure whether gf-node-exact & gf-node-deep is necessarry, because it maybe belongs to cf
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
  cf-node
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
                `(reification/has ~property-keyw ~var-match ~var-value))]
      (if 
        (= snippet-ast (:ast snippet)) 
        `(~@child-conditions) ;because snippet root is already ground to an ast of the right kind
        `((reification/ast ~snippet-keyw ~var-match)
           ~@child-conditions)))))

(defn 
  cf-list
    "Returns a function that will generate constraining conditions for the given property value of a code snippet.
     For Ekeko wrappers of ASTNode$NodeList instances: 
         (listvalue ?var-match)
         (fresh [?newly-generated-var]
           (value-raw ?var-match ?newly-generated-var)
     If type = :exact or type = :containswithsamesize :
           (equals snippet-list-size (.size ?newly-generated-var))
     If type = :exact     
           (equals ?var-el1 (.get ?newly-generated-var 1) ... (equals ?var-eln (.get ?newly-generated-var n))))
         else
           (contains ?newly-generated-var ?var-for-element1) ... (contains ?newly-generated-var ?var-for-elementn))"
  [snippet-val type]
  (let [lst (:value snippet-val)
        snippet-list-size (.size lst)]
    (fn [snippet]
      (let [var-match (representation/snippet-var-for-node snippet snippet-val)
            var-match-raw (util/gen-readable-lvar-for-value lst) ;freshly generated, not included in snippet datastructure ..
            size-conditions
            (if (or (= type :exact) (= type :containswithsamesize))
              `((el/equals ~snippet-list-size (.size ~var-match-raw)))
              `())
            element-conditions 
            (for [element lst
                  :let [idx-el (.indexOf lst element)
                        var-el (representation/snippet-var-for-node snippet element)]]
              (if (= type :exact) 
                `(el/equals ~var-el (.get ~var-match-raw ~idx-el))
                `(el/contains ~var-match-raw ~var-el)))]
        `((reification/listvalue ~var-match)
           (cl/fresh [~var-match-raw] 
                  (reification/value-raw ~var-match ~var-match-raw)
                  ~@size-conditions
                  ~@element-conditions))))))

(defn 
  cf-list-exact
  [snippet-val]
  (cf-list snippet-val :exact))

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
       `((cl/== ~var-userprovided ~var-match)))))

(defn 
  cf-list-contains-with-same-size
  [snippet-val]
  (cf-list snippet-val :containswithsamesize))

(defn 
  cf-list-contains
  [snippet-val]
  (cf-list snippet-val :contains))
  
(comment
  
;error : ClassCastException clojure.core.logic.LVar cannot be cast to clojure.lang.IFn  
(defn 
  cf-list-contains
    "Returns a function that will generate constraining conditions for the given AST node of a code snippet:
     (list-matches-list ?var-match ?var-match-raw snippet-list-size element-conditions)"
  [snippet-val]
  (let [lst (:value snippet-val)
        snippet-list-size (.size lst)]
    (fn [snippet]
      (let [var-match (representation/snippet-var-for-node snippet snippet-val)
            elements 
            (for [element lst
                :let [var-el (representation/snippet-var-for-node snippet element)]]
              `~var-el)]
        `((runtime/list-matches-list ~var-match ~snippet-list-size ~elements))))))
)

(defn 
  cf-exact
  [snippet-val]
  (cond
    (astnode/ast? snippet-val)
    (cf-node snippet-val)
    (astnode/lstvalue? snippet-val)
    (cf-list-exact snippet-val)
    (astnode/primitivevalue? snippet-val)
    (cf-primitive-exact snippet-val)
    (astnode/nilvalue? snippet-val)
    (cf-nil-exact snippet-val)))

(defn
  make-constraining-function
  [type]
  (cond
    (= type :exact)
    cf-exact
    (= type :list-exact)
    cf-list-exact
    (= type :list-contains-with-same-size)
    cf-list-contains-with-same-size
    (= type :list-contains)
    cf-list-contains
    (= type :variable)
    cf-variable
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
        `(org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/toKeyword ~(.toString primitive))
        (instance? org.eclipse.jdt.core.dom.PrimitiveType$Code primitive)
        `(org.eclipse.jdt.core.dom.PrimitiveType/toCode ~(.toString primitive))
        (instance? org.eclipse.jdt.core.dom.Assignment$Operator primitive)
        `(org.eclipse.jdt.core.dom.Assignment$Operator/toOperator ~(.toString primitive))
        (instance? org.eclipse.jdt.core.dom.InfixExpression$Operator primitive)
        `(org.eclipse.jdt.core.dom.InfixExpression$Operator/toOperator ~(.toString primitive))
        (nil? primitive) 
        (throw (Exception. (str "Encountered a null-valued property value that should have been wrapped by Ekeko.")))
        :else  (.toString primitive)))




