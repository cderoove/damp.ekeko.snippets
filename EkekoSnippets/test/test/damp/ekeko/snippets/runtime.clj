(ns 
  ^{:doc "Test suite for snippet runtime."
    :author "Siltvani, Coen De Roover"}
  test.damp.ekeko.snippets.runtime
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

(defn method-with-name 
  [name]
  (first (first 
           (damp.ekeko/ekeko [?m]
                  (fresh [?n ?id]
                  (reification/ast :MethodDeclaration ?m)
                  (reification/has :name ?m ?n)
                  (reification/has :identifier ?n ?id)
                  (reification/value-raw ?id name))))))

(defn class-with-name 
  [name]
  (first (first 
           (damp.ekeko/ekeko [?m]
                  (fresh [?n ?id]
                  (reification/ast :TypeDeclaration ?m)
                  (reification/has :name ?m ?n)
                  (reification/has :identifier ?n ?id)
                  (reification/value-raw ?id name))))))

;; Predicate: ast-variable-samebinding
;; ------------------------------------------

(deftest
  ^{:doc "Introduce logic var for two variable and add logic condition
          (ast-variable-samebinding ?var1 ?var2)
         
          Method:
          public int methodD() {
		          int foo = 0;     --> foo = ?var1
		          return foo;      --> foo = ?var2
         }
  "}
  predicate-ast-variable-samebinding
  (let [node      (method-with-name "methodD") 
        node-var1 (.getName (first (.fragments (first (.statements (.getBody node))))))
        node-var2 (.getExpression (fnext (.statements (.getBody node))))
        var1      '?var1   ;-> should do this, otherwise will become test.damp.ekeko.snippets.runtime/?var1
        var2      '?var2
        snippet   (representation/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-list-of-logic-variables 
          snippet 
          (list (.getName node) node-var1 node-var2)
          (list '?m var1 var2))
        generalized-snippet
        (operators/add-logic-conditions
          generalized-snippet-with-lvar
          `((damp.ekeko.snippets.runtime/ast-variable-samebinding ~var1 ~var2)))] 
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int methodD(){\\n  int foo=0;\\n  return foo;\\n}\\n\" \"foo\" \"foo\" \"methodD\") 
         (\"public int methodE(){\\n  int foo=0;\\n  return foo;\\n}\\n\" \"foo\" \"foo\" \"methodE\") 
         (\"public int methodF(){\\n  int foo2=0;\\n  return foo2;\\n}\\n\" \"foo2\" \"foo2\" \"methodF\")}")))


;; Predicate: ast-variable-declaration
;; ------------------------------------------

(deftest
  ^{:doc "Introduce logic var for two variable and add logic condition
          (ast-variable-declaration ?var1 ?var2)
         
          Method:
          public int methodD() {
		          int foo = 0;     --> foo = 0 --> ?var1
		          return foo;      --> foo         ?var2
         }
  "}
  predicate-ast-variable-declaration
  (let [node      (method-with-name "methodD") 
        node-var1 (first (.fragments (first (.statements (.getBody node)))))
        node-var2 (.getExpression (fnext (.statements (.getBody node))))
        var1      '?var1   ;-> should do this, otherwise will become test.damp.ekeko.snippets.runtime/?var1
        var2      '?var2
        snippet   (representation/jdt-node-as-snippet node)
        generalized-snippet-with-lvar
        (operators/introduce-list-of-logic-variables 
          snippet 
          (list (.getName node) node-var1 node-var2)
          (list '?m var1 var2))
        generalized-snippet
        (operators/add-logic-conditions
          generalized-snippet-with-lvar
          `((damp.ekeko.snippets.runtime/ast-variable-declaration ~var2 ~var1)))] 
    (test/tuples-correspond 
      (snippets/query-by-snippet generalized-snippet)
      "#{(\"public int methodF(){\\n  int foo2=0;\\n  return foo2;\\n}\\n\" \"foo2\" \"foo2=0\" \"methodF\") 
         (\"public int methodD(){\\n  int foo=0;\\n  return foo;\\n}\\n\" \"foo\" \"foo=0\" \"methodD\") 
         (\"public int methodE(){\\n  int foo=0;\\n  return foo;\\n}\\n\" \"foo\" \"foo=0\" \"methodE\")}")))


;; Predicate: ast-invocation-declaration
;; ------------------------------------------

(deftest
  ^{:doc "Introduce logic var for two variable and add logic condition
          (ast-invocation-declaration ?var1 ?var2)
         
          Method:
					public void myMethod() {
						this.methodA();
						this.methodB();    --> invocation ?var1
						this.methodC();
					}

          declaration of methodB  --> ?var2
  Still need to check!! the result should be not empty"}
  predicate-ast-invocation-declaration
  (let [;;snippet1
        node1     (method-with-name "myMethod") 
        node-var1 (.getExpression (fnext (.statements (.getBody node1))))
        var1      '?var1   ;-> should do this, otherwise will become test.damp.ekeko.snippets.runtime/?var1
        snippet1  (representation/jdt-node-as-snippet node1)
        generalized-snippet1
        (operators/introduce-list-of-logic-variables 
          snippet1 
          (list (.getName node1) node-var1)
          (list '?m var1))
        ;;snippet2
        node2     (method-with-name "methodB")
        var2      '?var2
        snippet2  (representation/jdt-node-as-snippet node2)
        generalized-snippet2
        (operators/introduce-logic-variable-of-node-exact
          snippet2 
          (.getName node2)
          var2)
        ;;add snippet1 and snippet2 to group
        group (representation/make-snippetgroup "group")
        added-group1 (operators/add-snippet group generalized-snippet1)
        added-group2 (operators/add-snippet added-group1 generalized-snippet2)
        generalized-snippet
        (operators/add-logic-conditions-to-snippetgroup
          added-group2
          `((damp.ekeko.snippets.runtime/ast-invocation-declaration ~var1 ~var2)))] 
    (test/tuples-correspond 
      (snippets/query-by-snippetgroup generalized-snippet)
      "#{}")))

;; Test suite
;; ----------
(deftest
   test-suite 
   
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  predicate-ast-variable-samebinding)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  predicate-ast-variable-declaration)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  predicate-ast-invocation-declaration)

)

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

