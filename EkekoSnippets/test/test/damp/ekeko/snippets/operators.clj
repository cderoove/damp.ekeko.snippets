(ns 
  ^{:doc "Test suite for snippet operators."
    :author "Siltvani, Coen De Roover"}
  test.damp.ekeko.snippets.operators
  (:refer-clojure :exclude [== type declare])
  (:use [clojure.core.logic :exclude [is]] :reload)
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [representation :as representation]
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
      (let [snippet (representation/jdt-node-as-snippet node)
            generalized-snippet (operators/introduce-logic-variable snippet child '?childvar)
            solutions (snippets/query-by-snippet generalized-snippet)]
        (is (some #{[node child]} solutions))))))

(deftest
  ^{:doc "Introduce a logic variable that substitutes for the :name property of a :MethodDeclaration "}
  operator-variable-substitutes-methoddeclaration-name
  (let [node
        (parsing/parse-string-declaration "public void methodA() { this.methodM(); this.methodC();} ")
        snippet 
        (representation/jdt-node-as-snippet node)
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
        (representation/jdt-node-as-snippet node)
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
        (representation/jdt-node-as-snippet node)
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
        (representation/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (generelized-function
          generalized-snippet-with-lvar 
          (first (first (damp.ekeko/ekeko [?s]
                     (fresh [?b]
                     (reification/has :body node ?b)
                     (reification/has :statements ?b ?s))))))]
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
  (let [node
        (parsing/parse-string-declaration 
          "public int rmethodC() { int i = 0; int x = 0, y = 0; int z = x + y; return z;	}")
        snippet 
        (representation/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        generalized-snippet
        (operators/remove-node
          generalized-snippet-with-lvar 
          (first (.statements (.getBody node))))]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodB(){\\n  int x=0, y=0;\\n  int z=x + y;\\n  return z;\\n}\\n\" \"rmethodB\")}")))


;; Operator: split-variable-declaration-statement
;; ------------------------------------------

(deftest
  ^{:doc "Split given statement in given snippet, become multiple statements with one fragment in each."}
  operator-split-variable-declaration-statement
  (let [node
        (parsing/parse-string-declaration 
          "public int rmethodB() {int x = 0, y = 0; int z = x + y; return z; } ")
        snippet 
        (representation/jdt-node-as-snippet node)
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
  ^{:doc "Allow given lst (= list of statement) in given snippet, as part of one or more statements in target source code."}
  operator-contains-variable-declaration-statements
  (let [node
        (parsing/parse-string-declaration 
          "public int rmethodC() { int i = 0; int x = 0, y = 0; int z = x + y; return z;	}")
        snippet 
        (representation/jdt-node-as-snippet node)
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
        (representation/jdt-node-as-snippet node)
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
        (representation/jdt-node-as-snippet node)
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
        (representation/jdt-node-as-snippet node)
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
        (representation/jdt-node-as-snippet node)
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


;; Refinement Operators
;; --------------------

;; Operator: add-node
;; ------------------------------------------

(deftest
  ^{:doc "Add node to a nodelist :statements"}
  operator-add-node-to-statements
  (let [node
        (parsing/parse-string-declaration 
          "public int rmethodC() { int i = 0; int x = 0, y = 0; int z = x + y; return z;	}")
        snippet 
        (representation/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-logic-variable snippet (.getName node) '?m)
        new-node
        (parsing/parse-string-statement "i = 1;")
        generalized-snippet
        (operators/add-node
          generalized-snippet-with-lvar 
          (representation/snippet-node-with-value (.statements (.getBody node)))
          new-node
          1)]
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int rmethodD(){\\n  int i=0;\\n  i=1;\\n  int x=0, y=0;\\n  int z=x + y;\\n  return z;\\n}\\n\" \"rmethodD\")}")))


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

   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-remove-node-from-statements)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-add-node-to-statements)
   
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-split-variable-declaration-statement)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-contains-variable-declaration-statements)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-allow-ifstatement-with-else)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-allow-subtype-on-variable-declaration)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-allow-subtype-on-class-declaration-extends)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-allow-variable-declaration-with-initializer)
   
)

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

