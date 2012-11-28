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
        ;useful when debugging:
        ;(println "node: " node) 
        ;(println "child: " child)
        (is (some #{node} (map first solutions))))))) ;TODO: also check whether ?childvar is bound to p.n, but ?chilvar has to 
      

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
      "#{(\"public void methodA1(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\") 
         (\"public void methodA(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\")}")))



;; test fails
;; reason: check generated query for
;(snippets/query-by-snippet* 
;      (let [node
;               (parsing/parse-string-declaration "private class X {	public Integer m() { return new Integer(111);	}	}" ")
;               snippet 
;               (representation/jdt-node-as-snippet node)
;               generalized-snippet
;               (operators/introduce-logic-variable snippet (first (.bodyDeclarations node)) '?bodydeclaration)]
;           generalized-snippet))
;-> there are still constraining conditions generate for the MethodDeclaration
;solution : add clear-cf-from-node in introduce-logic-variable
;           this function put :epsilon as cf and gf in all child of node
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
    (test/nonemptytuples-are ;assuming that class X has only one body declaration
      (snippets/query-by-snippet snippet)
      (snippets/query-by-snippet generalized-snippet)))) 
  
      

(comment

;; Test - List contains match
;; against methodA, methodA1 & methodA2

;; list (statements) of methodA
(defn selected-list [?m]
  (first (first 
           (damp.ekeko/ekeko [?l]
                    (fresh [?b]
                           (reification/has :body ?m ?b)
                           (reification/has :statements ?b ?l))))))

;; unit test
(defn list-contains-unittest []
  (let [selected    (selected-snippet)
        snippet     (snippets/jdt-node-as-snippet selected)
        newsnippet  (snippets/introduce-logic-variable snippet (selected-node selected) '?m)
        newsnippet  (snippets/ignore-elements-sequence newsnippet (selected-list selected))]
    (snippets/query-by-snippet newsnippet)))

;; test
(deftest
  list-contains-test
    (test/tuples-correspond 
      (list-contains-unittest)
      (test/tuples-to-stringsetstring 
          (query-for-get-methods ["methodA" "methodA1" "methodA2"]))))

)

;; Refinement Operators
;; --------------------


;; Test suite
;; ----------
(deftest
   test-suite 
   
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-substitutes-methoddeclaration-name)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-substitutes-node-child)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false  operator-variable-substitutes-typedeclaration-bodydeclaration)

   
   
   ;(test/against-project-named "TestCase-Snippets-BasicMatching" false list-contains-test)
   )

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

