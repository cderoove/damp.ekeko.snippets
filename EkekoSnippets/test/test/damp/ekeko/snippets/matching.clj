(ns 
  ^{:doc "Test suite for matching strategies of snippets."
    :author "Siltvani, Coen De Roover"}
  matching
  (:refer-clojure :exclude [== type declare])
  (:use [clojure.core.logic :exclude [is]] :reload)
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [representation :as representation]
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


  
;more reliable to use: basic/typedeclaration-identifier-methoddeclaration-identifier
;; General queries
;(defn query-for-get-methods [methods]
;  "methods -> vector of methods name eg. [\"methodA\" \"methodA1\"]"
;          (damp.ekeko/ekeko [?m] 
;                    (reification/ast :MethodDeclaration ?m) 
;                    (fresh [?n ?id]
;                           (reification/has :name ?m ?n)
;                           (reification/has :identifier ?n ?id)
;                           (contains methods ?id))))


;; Matching Strategy: Exact
;; ------------------------

;; ASTNodes in general

(deftest
  ^{:doc "For all nodes n, n should be included in the matches for snippet(n)."}
  exactmatch-node 
  (let [nodes 
        (map first (damp.ekeko/ekeko [?ast] (fresh [?kind] (reification/ast ?kind ?ast))))
        snippets
        (map representation/jdt-node-as-snippet nodes)]
    (doseq [[node snippet] (map vector nodes snippets)]
      (is (some #{node} (map first (snippets/query-by-snippet snippet)))))))
    
;; Type Declarations

(deftest
  ^{:doc "Exact matching of type declaration snippet."}
  exactmatch-typedeclaration
  (test/tuples-correspond 
    (snippets/query-by-snippet 
      (representation/jdt-node-as-snippet
        (parsing/parse-string-declaration "private class X { public Integer m() { return new Integer(111); } }")))
    "#{(\"private class X {\\n  public Integer m(){\\n    return new Integer(111);\\n  }\\n}\\n\")}"))

;; Method Declarations

(deftest
  ^{:doc "Exact matching of method declaration snippet."}
  exactmatch-methoddeclaration
  (test/tuples-correspond 
    (snippets/query-by-snippet 
      (representation/jdt-node-as-snippet
        (parsing/parse-string-declaration 
          "public void methodA() {
               this.methodM(); 
               this.methodC();
            } ")))
    "#{(\"public void methodA(){\\n  this.methodM();\\n  this.methodC();\\n}\\n\")}"))
  
;; Expressions

(deftest
  exactmatch-methodinvocation
  (test/tuples-correspond 
    (snippets/query-by-snippet (representation/jdt-node-as-snippet (parsing/parse-string-expression "x.m()")))
    "#{(\"x.m()\")}")) ;string obtained by evaluating (test/tuples-to-stringsetstring (snippets/query-by-snippet .....
     
;; Statements
;; ----------

;; If Statements

(deftest
  ^{:doc "Exact matching of if statement snippet."}
  exactmatch-ifstatement
  (test/tuples-correspond 
    (snippets/query-by-snippet 
      (representation/jdt-node-as-snippet
        (parsing/parse-string-statement "if (getInput() % 2 == 0) return o; else return new MayAliasLeaf();")))
    "#{(\"if (getInput() % 2 == 0) return o;\\n else return new MayAliasLeaf();\\n\")}"))



;; Test suite
;; ----------

(deftest
   test-suite 
   
   ;this test discovers some shortcomings!
   (test/against-project-named "TestCase-JDT-CompositeVisitor" false exactmatch-node)

   ;fails for some of the bigger nodes (e.g., compilation units) because the query gets too big 
   ;(could work on this later, for instance by splitting the query into two)
   ;(test/against-project-named "TestCase-Snippets-BasicMatching" false exactmatch-node)
   
   ;(test/against-project-named "TestCase-Snippets-BasicMatching" false exactmatch-typedeclaration)
   ;(test/against-project-named "TestCase-Snippets-BasicMatching" false exactmatch-methoddeclaration)
   ;(test/against-project-named "TestCase-Snippets-BasicMatching" false exactmatch-methodinvocation)
   ;(test/against-project-named "TestCase-JDT-CompositeVisitor" false exactmatch-ifstatement)

   )

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

