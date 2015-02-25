(ns 
  ^{:doc "Suite containing tests and experiments of the genetic search algorithm."
    :author "Tim Molderez"}
  test.damp.ekeko.snippets.geneticsearch
  (:refer-clojure :exclude [== type declare record?])
  (:require  [clojure.core.logic :exclude [is] :as l])
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [matching :as matching]
             [parsing :as parsing]
             [util :as util]
             [persistence :as persistence]
             ])
  (:require [damp.ekeko.snippets.geneticsearch
             [search :as search]])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]
             ])
  (:require [damp.ekeko 
             [logic :as el]
             [snippets :as snippets]])
  (:use clojure.test))


(defn
  snippetgroup-from-resource
  [pathrelativetobundle]
  (persistence/slurp-snippet (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))

;; Fitness function
;; ----------------

(deftest
  ^{:doc "Precision and recall should be 1 for the oracle template"}
  precision-recall 
  (let [templategroup (snippetgroup-from-resource "/resources/EkekoX-Specifications/invokedby.ekt")
        matches (search/templategroup-matches templategroup)
        verifiedmatches (search/make-verified-matches matches [])
        ]
    (is (= 1 (search/precision matches verifiedmatches)))
    (is (= 1 (search/recall matches verifiedmatches)))))


                    
;; Test suite
;; ----------

(deftest
   test-suite
   (let [testproject "TestCase-JDT-CompositeVisitor"
         matchproject "TestCase-EkekoX-Matching"]
     (test/against-project-named testproject false precision-recall)
    
     ))

(defn test-ns-hook []
  (test/with-ekeko-disabled test-suite))

(comment 
  (run-tests))