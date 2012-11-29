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
  ^{:doc "Introduce a logic variable that substitutes for one of the :bodyDeclarations 
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
 
      
;; Operator: generelized for the list
;; ------------------------------------------

(defn
  generelized-statements
  "Generelized list :statements of :MethodDeclaration with given generelized-function
   with preprocess : Introduce a logic variable that substitutes for the :name property of a :MethodDeclaration"
  [generelized-function match-string]
  (let [node
        (parsing/parse-string-declaration "public void methodA() { this.methodM(); this.methodC();} ")
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
    "#{(\"public void methodA1(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA1\") 
       (\"public void methodA2(){\\n  this.methodC();\\n  this.methodM();\\n}\\n\" \"methodA2\") 
       (\"public void methodA(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\" \"methodA\") 
       (\"public void methodA3(){\\n  this.methodC();\\n  this.methodM();\\n  this.methodD();\\n}\\n\" \"methodA3\")}"))



;; Refinement Operators
;; --------------------


;; Test suite
;; ----------
(deftest
   test-suite 
   
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-substitutes-methoddeclaration-name)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-substitutes-node-child)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-substitutes-typedeclaration-bodydeclaration)
   
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-contains-elements-with-same-size-of-statements)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-contains-elements-of-statements)
   )

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

