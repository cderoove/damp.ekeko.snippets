(ns 
  ^{:doc "Test suite for persistence of snippets."
    :author "Coen De Roover"}
  test.damp.ekeko.snippets.persistence
  (:refer-clojure :exclude [== type declare record?])
  (:require  [clojure.core.logic :exclude [is] :as l])
  (:require [damp.ekeko.snippets 
             [persistence :as persistence]
             ])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [javaprojectmodel :as javaprojectmodel]
              ])
  (:import [org.eclipse.jdt.core.dom CompilationUnit])
  (:use clojure.test))




;; Persisting JDT AST nodes
;; ------------------------

(deftest
  ^{:doc "For all nodes compilation units cu, cu has to be persistable."}
   persist-compilationunits
  (let [cus
        (mapcat (fn [jpm] (.getCompilationUnits jpm))
                (javaprojectmodel/java-project-models))]
    (doseq [cu cus]
      (let [serialized (persistence/snippet-as-persistent-string cu)
            deserialized (persistence/snippet-from-persistent-string serialized)]
        (is (instance? CompilationUnit deserialized))))))



;; Looking up AST nodes in project by identifier
;; ---------------------------------------------

(deftest
  ^{:doc "For all AST nodes returned by Ekeko, an equivalent AST node should be found in a workspace project."}
   lookup-equivalent-nodes
   (is (reduce (fn [sofar t] 
             (let [exp (first t)
                   expid (persistence/project-value-identifier exp)
                   equivalent (persistence/corresponding-project-value expid)]
               (and sofar (= (str exp) (str equivalent)))))
           (damp.ekeko/ekeko [?e ?key] (damp.ekeko.jdt.ast/ast ?key ?e)))))

;; Test suite
;; ----------

(deftest
   test-suite 
   (let [testproject "TestCase-JDT-CompositeVisitor"]
     (test/against-project-named testproject false persist-compilationunits)
     (test/against-project-named testproject false lookup-equivalent-nodes)
     
     )
   )

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

