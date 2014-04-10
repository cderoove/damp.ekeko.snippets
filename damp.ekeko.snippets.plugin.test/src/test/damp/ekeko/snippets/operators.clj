(ns 
  ^{:doc "Test suite for snippet operators."
    :author "Siltvani, Coen De Roover"}
  test.damp.ekeko.snippets.operators
  (:refer-clojure :exclude [== type declare])
  (:use [clojure.core.logic :exclude [is]] :reload)
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [operators :as operators]
             [matching :as matching]
             [parsing :as parsing]
             [util :as util]])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [reification :as reification]
             ])
  (:require [damp.ekeko])
  (:require [damp.ekeko 
             [logic :as el]
             [snippets :as snippets]])
  (:use clojure.test))

;; Generalization Operators
;; ========================
      
;; Operator: introduce-logic-variable
;; ----------------------------------

;; In general: matches(snippet) is a subset of matches(generalized(snippet))

(deftest
  ^{:doc "For all nodes n, properties p of n, n should be included in the matches for introduce-logic-variable(snippet(n),p.n)."}
  operator-variable-substitutes-node-child
  (let [node-child
        (damp.ekeko/ekeko [?ast ?child]
               (fresh [?kind ?property] 
                      (reification/ast ?kind ?ast)
                      (reification/child ?property ?ast ?child)))]
    (doseq [[node child] node-child] 
      (let [snippet (snippet/jdt-node-as-snippet node)
            generalized-snippet (operators/introduce-logic-variable snippet child '?childvar)
            solutions (snippets/query-by-snippet generalized-snippet)]
        (is (some #{[node child]} solutions))))))

(deftest
  ^{:doc "Introduce a logic variable that substitutes for the :name property of a :MethodDeclaration "}
  operator-variable-substitutes-methoddeclaration-name
  (let [node
        (parsing/parse-string-declaration "public void methodA() { this.methodM(); this.methodC();} ")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet
        (operators/introduce-logic-variable snippet (.getName node) '?m)]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public void methodA1(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA1\") 
         (\"public void methodA(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA\")}")))


(deftest 
  ^{:doc "Introduce a logic variable (removing elements conditions) that substitutes for one of the :bodyDeclarations 
          (e.g., a MethodDeclaration) of a :TypeDeclaration. Tests introducing a logic variable
          in an ASTNode$NodeList."}
  operator-variable-substitutes-typedeclaration-bodydeclaration
  (let [node
        (parsing/parse-string-declaration "private class X {public Integer m() { return new Integer(111); } }")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet
        (operators/introduce-logic-variable snippet (first (.bodyDeclarations node)) '?bodydeclaration)]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"private class X {\\n  public Integer m(){\\n    return new Integer(111);\\n  }\\n}\\n\" 
          \"public Integer m(){\\n  return new Integer(111);\\n}\\n\")}")))
 
(deftest 
  ^{:doc "Introduce a logic variable that substitutes for one of the :bodyDeclarations 
          (e.g., a MethodDeclaration) of a :TypeDeclaration. Tests introducing a logic variable
          in an ASTNode$NodeList."}
  operator-variable-exact-substitutes-typedeclaration-bodydeclaration
  (let [node
        (parsing/parse-string-declaration "private class X {public Integer m() { return new Integer(111); } }")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet
        (operators/introduce-logic-variable-of-node-exact snippet (first (.bodyDeclarations node)) '?bodydeclaration)]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"private class X {\\n  public Integer m(){\\n    return new Integer(111);\\n  }\\n}\\n\" 
          \"public Integer m(){\\n  return new Integer(111);\\n}\\n\")}")))
      
;; Operator: generelized for the list
;; ------------------------------------------
;; note: to get the list, should use ekeko query, can not use .statements
;;       list = {:type :Value, :owner #<Block {..}>, :property #<ChildListPropertyDescriptor ..>
;;               :value #<NodeList [...]>}

(defn
  generelized-statements
  "Generelized list :statements of :MethodDeclaration with given generelized-function
   with preprocess : Introduce a logic variable that substitutes for the :name property of a :MethodDeclaration"
  [generelized-function snippet-string match-string]
  (let [node
        (parsing/parse-string-declaration snippet-string)
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (generelized-function
          generalized-snippet-with-lvar 
          (snippet/snippet-node-with-value snippet (.statements (.getBody node))))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      match-string)))

;; Operator: contains-elements-with-same-size
;; ------------------------------------------

(deftest
  ^{:doc "Contains elements in a nodelist :statements with the same size"}
  operator-contains-elements-with-same-size-of-statements
  (generelized-statements 
    operators/contains-elements-with-same-size 
    "public void methodA() { this.methodM(); this.methodC();} "
    "#{(\"public void methodA1(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA1\") 
       (\"public void methodA2(){\\n  this.methodC();\\n  this.methodM();\\n}\\n\" \"methodA2\") 
       (\"public void methodA(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA\")}"))

;; Operator: contains-elements
;; ------------------------------------------

(deftest
  ^{:doc "Contains elements in a nodelist :statements "}
  operator-contains-elements-of-statements
  (generelized-statements 
    operators/contains-elements 
    "public void methodA() { this.methodM(); this.methodC();} "
    "#{(\"public void methodA1(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA1\") 
       (\"public void methodA2(){\\n  this.methodC();\\n  this.methodM();\\n}\\n\" \"methodA2\") 
       (\"public void methodA(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA\") 
       (\"public void methodA3(){\\n  this.methodM();\\n  this.methodC();\\n  this.methodD();\\n  this.methodC();\\n  this.methodE();\\n}\\n\" \"methodA3\")}"))


;; Operator: contains-elements-with-relative-order
;; ------------------------------------------

(deftest
  ^{:doc "Contains elements in a nodelist :statements with realtive order"}
  operator-contains-elements-with-relative-order-of-statements
  (generelized-statements 
    operators/contains-elements-with-relative-order 
    "public void methodAA() { this.methodM(); this.methodD(); this.methodE; }"
    "#{(\"public void methodA3(){\\n  this.methodM();\\n  this.methodC();\\n  this.methodD();\\n  this.methodC();\\n  this.methodE();\\n}\\n\" \"methodA3\") 
       (\"public void methodAA(){\\n  this.methodM();\\n  this.methodD();\\n  this.methodE();\\n}\\n\" \"methodAA\")}"))

;; Operator: contains-elements-with-repetition
;; ------------------------------------------

(deftest
  ^{:doc "Contains elements in a nodelist :statements with repetition"}
  operator-contains-elements-with-repetition-of-statements
  (generelized-statements 
    operators/contains-elements-with-repetition 
    "public void methodAAA() { this.methodM(); this.methodD(); this.methodD; }"
    "#{(\"public void methodAAA(){\\n  this.methodM();\\n  this.methodD();\\n  this.methodD();\\n}\\n\" \"methodAAA\") 
       (\"public void methodA5(){\\n  this.methodM();\\n  this.methodE();\\n  this.methodD();\\n  this.methodD();\\n}\\n\" \"methodA5\")}"))

;; Operator: remove-node
;; ------------------------------------------

(deftest
  ^{:doc "Remove node from a nodelist :statements"}
  operator-remove-node-from-statements
  (let [doc
        (parsing/parse-string-to-document 
          "public int rmethodC() { int i = 0; int x = 0, y = 0; int z = x + y; return z;	}")
        snippet 
        (snippet/document-as-snippet doc)
        node 
        (:ast snippet) 
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/remove-node
          generalized-snippet-with-lvar 
          (first (.statements (.getBody node))))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodB(){\\n  int x=0, y=0;\\n  int z=x + y;\\n  return z;\\n}\\n\" \"rmethodB\")}")))

;; Operator: remove-logic-conditions
;; ------------------------------------------

(deftest
  ^{:doc "Remove logic conditions"}
  operator-remove-logic-conditions
  (let [node
        (parsing/parse-string-declaration 
          "public void methodA() {this.methodM(); this.methodC();	}")
        snippet 
        (snippet/jdt-node-as-snippet node)
        var-id     (snippet/snippet-var-for-node snippet (first (astnode/node-propertyvalues (.getName node))))
        var-name   (snippet/snippet-var-for-node snippet (.getName node))
        value      "methodA1"
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        ;;add conditions
        conditions  `((damp.ekeko.jdt.reification/has :identifier ~var-name ~var-id) 
                       (damp.ekeko.jdt.reification/value-raw ~var-id ~value))
        generalized-snippet-add
        (operators/add-logic-conditions
          generalized-snippet-with-lvar 
          conditions)
        ;;remove conditions
        generalized-snippet
        (operators/remove-logic-conditions
          generalized-snippet-add 
          conditions)]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public void methodA1(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA1\") 
         (\"public void methodA(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA\")}")))

;; Operator: split-variable-declaration-statement
;; ------------------------------------------

(deftest
  ^{:doc "Split given statement in given snippet, become multiple statements with one fragment in each."}
  operator-split-variable-declaration-statement
  (let [doc
        (parsing/parse-string-to-document 
          "public int rmethodB() {int x = 0, y = 0; int z = x + y; return z; } ")
        snippet 
        (snippet/document-as-snippet doc)
        node 
        (:ast snippet) 
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/split-variable-declaration-statement
          generalized-snippet-with-lvar 
          (first (.statements (.getBody node))))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodA(){\\n  int x=0;\\n  int y=0;\\n  int z=x + y;\\n  return z;\\n}\\n\" \"rmethodA\")}")))

;; Operator: contains-variable-declaration-statements
;; ------------------------------------------

(deftest
  operator-contains-variable-declaration-statement
  (let [doc
        (parsing/parse-string-to-document 
          "public int rmethodC() { int i = 0; int x = 0, y = 0; int z = x + y; return z;	}")
        snippet 
        (snippet/document-as-snippet doc)
        node 
        (:ast snippet) 
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet1
        (operators/contains-variable-declaration-statement
          generalized-snippet-with-lvar 
          (first (.statements (.getBody node))))
        node2 
        (:ast generalized-snippet1) 
        generalized-snippet
        (operators/contains-variable-declaration-statement
          generalized-snippet1 
          (fnext (.statements (.getBody node2))))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodC(){\\n  int i=0;\\n  int x=0, y=0;\\n  int z=x + y;\\n  return z;\\n}\\n\" \"rmethodC\") 
         (\"public int rmethodD(){\\n  int i=0;\\n  i=1;\\n  int x=0, y=0;\\n  int z=x + y;\\n  return z;\\n}\\n\" \"rmethodD\")}")))

(deftest
  ^{:doc "Allow given lst (= list of statement) in given snippet, as part of one or more statements in target source code."}
  operator-contains-variable-declaration-statements
  (let [doc
        (parsing/parse-string-to-document 
          "public int rmethodC() { int i = 0; int x = 0, y = 0; int z = x + y; return z;	}")
        snippet 
        (snippet/document-as-snippet doc)
        node 
        (:ast snippet) 
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/contains-variable-declaration-statements
          generalized-snippet-with-lvar 
          (list (first (.statements (.getBody node)))
                (fnext (.statements (.getBody node)))))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodC(){\\n  int i=0;\\n  int x=0, y=0;\\n  int z=x + y;\\n  return z;\\n}\\n\" \"rmethodC\") 
         (\"public int rmethodD(){\\n  int i=0;\\n  i=1;\\n  int x=0, y=0;\\n  int z=x + y;\\n  return z;\\n}\\n\" \"rmethodD\")}")))

;; Operator: allow-ifstatement-with-else
;; ------------------------------------------

(deftest
  ^{:doc "Allow match given node (= ifstatement without else) with node (= ifstatement with else)."}
  operator-allow-ifstatement-with-else
  (let [node
        (parsing/parse-string-declaration 
          "public int rmethodF(int val) {	int r = 0; if (val == 0) {	r = val;	} return r; }")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/allow-ifstatement-with-else
          generalized-snippet-with-lvar 
          (fnext (.statements (.getBody node))))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodF(int val){\\n  int r=0;\\n  if (val == 0) {\\n    r=val;\\n  }\\n  return r;\\n}\\n\" \"rmethodF\") 
         (\"public int rmethodE(int val){\\n  int r=0;\\n  if (val == 0) {\\n    r=val;\\n  }\\n else   if (val < 0) {\\n    r=val * -1;\\n  }\\n else {\\n    r=val;\\n  }\\n  return r;\\n}\\n\" \"rmethodE\") 
         (\"public int rmethodG(int val){\\n  int r=0;\\n  if (val == 0) {\\n    r=val;\\n  }\\n else   if (val < 0) {\\n    r=val * -1;\\n  }\\n  return r;\\n}\\n\" \"rmethodG\")}")))


;; Operator: allow-subtype-on-variable-declaration
;; ------------------------------------------
(defn method-with-name 
  [name]
  (first (first 
           (damp.ekeko/ekeko [?m]
                  (fresh [?n ?id]
                  (reification/ast :MethodDeclaration ?m)
                  (reification/has :name ?m ?n)
                  (reification/has :identifier ?n ?id)
                  (reification/value-raw ?id name))))))

(deftest
  ^{:doc "Allow match given node (= type) with node (= same type or subtype)."}
  operator-allow-subtype-on-variable-declaration
  (let [node
        (method-with-name "rmethodI")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/allow-subtype-on-variable-declaration
          generalized-snippet-with-lvar 
          (.getType (.get (.statements (.getBody node)) 0)))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodJ(){\\n  Integer o=1;\\n  Integer x=0;\\n  O test;\\n{\\n    int y=0;\\n    int z=x + y;\\n  }\\n  return x;\\n}\\n\" \"rmethodJ\") 
         (\"public int rmethodI(){\\n  Number o=1;\\n  Integer x=0;\\n  O test;\\n{\\n    int y=0;\\n    int z=x + y;\\n  }\\n  return x;\\n}\\n\" \"rmethodI\")}")))


;; Operator: allow-subtype-on-class-declaration-extends
;; ------------------------------------------
(defn class-with-name 
  [name]
  (first (first 
           (damp.ekeko/ekeko [?m]
                  (fresh [?n ?id]
                  (reification/ast :TypeDeclaration ?m)
                  (reification/has :name ?m ?n)
                  (reification/has :identifier ?n ?id)
                  (reification/value-raw ?id name))))))

(deftest
  ^{:doc "Allow match given node (= type (class extends)) with node (= same type or subtype)."}
  operator-allow-subtype-on-class-declaration-extends
  (let [node
        (class-with-name "Z")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/allow-subtype-on-class-declaration-extends
          generalized-snippet-with-lvar 
          (.getSuperclassType node))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"private class Z extends Object {\\n  private int i;\\n}\\n\" \"Z\") 
         (\"private class ZSub extends OSub {\\n  private int i;\\n}\\n\" \"ZSub\")}")))

;; Operator: allow-variable-declaration-with-initializer
;; ------------------------------------------
(deftest
  ^{:doc "Allow match given node (= assigment expression) with node (= variable declaration wth initializer)."}
  operator-allow-variable-declaration-with-initializer
  (let [node
        (method-with-name "myMethodK")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/allow-variable-declaration-with-initializer
          generalized-snippet-with-lvar 
          (.get (.statements (.getBody node)) 0))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public char rmethodK(){\\n  char s='m';\\n  return s;\\n}\\n\" \"rmethodK\") 
         (\"public char myMethodK(){\\n  s='m';\\n  return s;\\n}\\n\" \"myMethodK\")}")))

;; Operator: inline-method-invocation
;; ------------------------------------------
(deftest
  ^{:doc "Inline statement of method invocation in given snippet, with statements from called method.
          e.g: inline methodY1() in methodY().
               public void methodY() {methodA1(); methodY1(); methodA2(); methodA3(); }
               public void methodY1() {	myMethodX(); myMethodY();	}	"}
  operator-inline-method-invocation
  (let [node
        (method-with-name "methodY")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/inline-method-invocation
          generalized-snippet-with-lvar 
          (.get (.statements (.getBody node)) 1))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public void methodZ(){\\n  methodA1();\\n  myMethodX();\\n  myMethodY();\\n  methodA2();\\n  methodA3();\\n}\\n\" \"methodZ\")}")))


;; Operator: introduce-logic-variables
;; ------------------------------------------
(deftest
  ^{:doc "Introduce logic variable to a given node, and other nodes with the same ast kind and identifier.
          e.g: introduce logic variables for 'val' in rmethodE().
	             public int rmethodE(int val) { 
                      int r = 0;
		                  if (val == 0) { r = val; } else if (val < 0) { r = val * -1; } else { r = val; }
		                  return r;
	             } 
    !!This test is fail, but when test manually it's ok, the result are exactly the same as the string below."}
  operator-introduce-logic-variables
  (let [node
        (method-with-name "rmethodE")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/introduce-logic-variables
          generalized-snippet-with-lvar 
          (.getLeftOperand  (.getExpression (fnext (.statements (.getBody node))))))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodE2(int val2){\\n  int r=0;\\n  if (val2 == 0) {\\n    r=val2;\\n  }\\n else   if (val2 < 0) {\\n    r=val2 * -1;\\n  }\\n else {\\n    r=val2;\\n  }\\n  return r;\\n}\\n\" \"val2\" \"val2\" \"rmethodE2\" \"val2\" \"val2\" \"val2\" \"val2\") 
         (\"public int rmethodE(int val){\\n  int r=0;\\n  if (val == 0) {\\n    r=val;\\n  }\\n else   if (val < 0) {\\n    r=val * -1;\\n  }\\n else {\\n    r=val;\\n  }\\n  return r;\\n}\\n\" \"val\" \"val\" \"rmethodE\" \"val\" \"val\" \"val\" \"val\")}")))

;; Operator: introduce-logic-variables-with-condition
;; ------------------------------------------
(deftest
  ^{:doc "Introduce logic variable to a given node, and other nodes with the same ast kind and identifier.
          Logic variable for the nodes will be generated. Condition will be applied to all those nodes.
          e.g: see operator-introduce-logic-variables
               generated conditions: 
                 (clojure.core.logic/fresh [?v-id] 
                    (damp.ekeko.jdt.reification/has :identifier ??v15600 ?v-id) 
                    (damp.ekeko.jdt.reification/value-raw ?v-id \"val2\")) 
               ??v15600 -> new logic variables
               same conditions will be generated for all new logic variables.
    This test is error when eval query. But when run query manually, it is ok, and the result is correct."}
  operator-introduce-logic-variables-with-condition
  (let [node
        (method-with-name "rmethodE")
        var-val '?v-val
        var-id  '?v-id
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/introduce-logic-variables-with-condition
          generalized-snippet-with-lvar 
          (.getLeftOperand  (.getExpression (fnext (.statements (.getBody node)))))
          var-val
          `(clojure.core.logic/fresh [~var-id]
                                     (damp.ekeko.jdt.reification/has :identifier ~var-val ~var-id) 
                                     (damp.ekeko.jdt.reification/value-raw ~var-id "val2")))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodE2(int val2){\\n  int r=0;\\n  if (val2 == 0) {\\n    r=val2;\\n  }\\n else   if (val2 < 0) {\\n    r=val2 * -1;\\n  }\\n else {\\n    r=val2;\\n  }\\n  return r;\\n}\\n\" 
          \"val2\" \"val2\" \"rmethodE2\" \"val2\" \"val2\" \"val2\" \"val2\")}")))


(deftest
  ^{:doc "Match all kind of node except given node -> statement this.methodB() "}
  operator-negated-node
  (let [node
        (parsing/parse-string-declaration "public void myMethod() {this.methodA();	this.methodB();	this.methodC(); }")
        snippet 
        (snippet/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/negated-node
          generalized-snippet-with-lvar 
          (fnext (.statements (.getBody node))))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public void myMethodZ(){\\n  this.methodA();\\n  this.methodX();\\n  this.methodC();\\n}\\n\" \"myMethodZ\")}")))


;; Refinement Operators
;; --------------------

;; Operator: add-node
;; ------------------------------------------

(deftest
  ^{:doc "Add node to a nodelist :statements"}
  operator-add-node-to-statements
  (let [doc
        (parsing/parse-string-to-document 
          "public int rmethodC() { int i = 0; int x = 0, y = 0; int z = x + y; return z;	}")
        snippet 
        (snippet/document-as-snippet doc)
        node 
        (:ast snippet) 
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        new-node
        (parsing/parse-string-statement "i = 1;")
        generalized-snippet
        (operators/add-node
          generalized-snippet-with-lvar 
          (snippet/snippet-node-with-value snippet (.statements (.getBody node)))
          new-node
          1)]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodD(){\\n  int i=0;\\n  i=1;\\n  int x=0, y=0;\\n  int z=x + y;\\n  return z;\\n}\\n\" \"rmethodD\")}")))

;; Operator: add-logic-conditions
;; ------------------------------------------

(deftest
  ^{:doc "Add logic conditions"}
  operator-add-logic-conditions
  (let [node
        (parsing/parse-string-declaration 
          "public void methodA() {this.methodM(); this.methodC();	}")
        snippet 
        (snippet/jdt-node-as-snippet node)
        var-id     (snippet/snippet-var-for-node snippet (first (astnode/node-propertyvalues (.getName node))))
        var-name   (snippet/snippet-var-for-node snippet (.getName node))
        value      "methodA1"
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/add-logic-conditions
          generalized-snippet-with-lvar 
          `((damp.ekeko.jdt.reification/has :identifier ~var-name ~var-id) 
             (damp.ekeko.jdt.reification/value-raw ~var-id ~value)))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public void methodA1(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA1\")}")))

;; Operator: add-snippet
;; ------------------------------------------

(deftest
  ^{:doc "Add Snippet"}
  operator-add-snippet
  (let [node (parsing/parse-string-declaration 
               "public void methodX() {methodA1(); methodA2();	}")
        node2 (parsing/parse-string-statement "methodA3();")        
        snippet (snippet/jdt-node-as-snippet node)
        snippet2 (snippet/jdt-node-as-snippet node2)
        generalized-snippet-contains
        (operators/contains-elements snippet (.statements (.getBody node)))
        ;;add snippet1 and snippet2 to group
        group (snippetgroup/make-snippetgroup "group")
        added-group1 (operators/add-snippet group generalized-snippet-contains)
        added-group2 (operators/add-snippet added-group1 snippet2)
        ;;add logic conditions (list statement of node1 contains node2) 
        var-raw (snippet/snippet-var-for-node snippet (.statements (.getBody node)))
        var-stat (snippet/snippet-var-for-node snippet2 node2)
        generalized-group
        (operators/add-logic-conditions-to-snippetgroup
          added-group2 
          `((damp.ekeko.logic/contains ~var-raw ~var-stat)))]
    (test/tuples-correspond 
      (snippets/query-by-snippetgroup generalized-group)      
      "#{(\"methodA3();\\n\" 
          \"public void methodX(){\\n  methodA1();\\n  methodA2();\\n  methodA3();\\n}\\n\")}")))


;; Test suite
;; ----------
(deftest
   test-suite 
   
   ;(test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-substitutes-node-child)

   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-substitutes-methoddeclaration-name)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-substitutes-typedeclaration-bodydeclaration)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-exact-substitutes-typedeclaration-bodydeclaration)
   
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-contains-elements-with-same-size-of-statements)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-contains-elements-of-statements)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-contains-elements-with-relative-order-of-statements)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-contains-elements-with-repetition-of-statements)

   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-add-node-to-statements)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-remove-node-from-statements)   
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-add-logic-conditions)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-remove-logic-conditions)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-add-snippet)

   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-split-variable-declaration-statement)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-contains-variable-declaration-statement)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-allow-ifstatement-with-else)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-allow-subtype-on-variable-declaration)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-allow-subtype-on-class-declaration-extends)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-allow-variable-declaration-with-initializer)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-inline-method-invocation)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-negated-node)
   
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-introduce-logic-variables)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-introduce-logic-variables-with-condition)
)

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

