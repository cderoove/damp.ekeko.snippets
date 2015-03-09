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
             [transformation :as transformation]
             ])
  (:require [damp.ekeko.snippets.geneticsearch
             [fitness :as fitness]
             [search :as search]])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]
             ])
  (:require [damp.ekeko 
             [logic :as el]
             [snippets :as snippets]])
  (:import [damp.ekeko.snippets.geneticsearch PartialJavaProjectModel]) 
  (:use clojure.test))


(defn
  snippetgroup-from-resource
  [pathrelativetobundle]
  (persistence/slurp-snippet (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))

;; Fitness function
;; ----------------

(deftest
  ^{:doc "Precision, recall and F-score should be 1 for the oracle template"}
  precision-recall 
  (let [templategroup (snippetgroup-from-resource "/resources/EkekoX-Specifications/invokedby.ekt")
        matches (fitness/templategroup-matches templategroup)
        verifiedmatches (search/make-verified-matches matches [])
        ]
    (is (= 1 (fitness/precision matches verifiedmatches)))
    (is (= 1 (fitness/recall matches verifiedmatches)))
    (is (= 1 (fitness/fmeasure matches verifiedmatches)))))

(deftest
  ^{:doc "Filtered Ekeko query (used when determining partial matches),
          where we only query among a given set of AST nodes"}
  filtered-query
  (let [templategroup (snippetgroup-from-resource "/resources/EkekoX-Specifications/invokedby.ekt")
        matches (fitness/templategroup-matches templategroup)
        partialmodel (new PartialJavaProjectModel)]
    (.addExistingAST partialmodel (first (first matches)))
    (is (= 2 (count (binding [damp.ekeko.ekekomodel/*queried-project-models* (atom [partialmodel])]
                   (damp.ekeko/ekeko [?cu] (damp.ekeko.jdt.ast/ast :Statement ?cu))))))))


;; Crossover and mutations
;; -----------------------

; Testing crossover
;  (def m1 (persistence/slurp-from-resource "/resources/EkekoX-Specifications/m1.ekt"))
;  (def m2 (persistence/slurp-from-resource "/resources/EkekoX-Specifications/m2.ekt"))
;  (def match1 (templategroup-matches m1))
;  (def match2 (templategroup-matches m2))
;  
;  (doseq [x (range 0 1)]
;    (let [[x1 x2] (crossover m1 m2)
;          x1-match (templategroup-matches x1)]
;      (println "---" (meta x1))
;      
;      (if (not (empty? x1-match))
;        (do
;          (println x1-match)))))


;; SCAM 2014
;; ---------
; Given the concrete matches of a template (used in SCAM 2014),
; try to infer the template from the matches using genetic search

(deftest
  ^{:doc "Try to infer scam_demo1.ekx's left-hand-side template"}
  scam-demo 
  (let [templategroup (transformation/transformation-lhs (snippetgroup-from-resource "/resources/EkekoX-Specifications/scam_demo3.ekx"))
        matches (into [] (fitness/templategroup-matches templategroup 10000))
        cherry-picked-matches [(nth matches 0) ; Choose some of the shorter snippets to speed up the process..
                               (nth matches 1)
                               (nth matches 3)
                               (nth matches 7)]
        verifiedmatches (search/make-verified-matches cherry-picked-matches [])]
    (search/evolve verifiedmatches
                   :max-generations 50
                   :fitness-weights [20/20 0/20]
                   :match-timeout 10000)))

;; Design patterns experiments
;; ---------------------------

(deftest
  ^{:doc "Try to infer the general template of the TemplateGroup pattern scam_demo1.ekx's left-hand-side template"}
  singleton-experiment
  (let [singleton1 (snippetgroup-from-resource "/resources/EkekoX-Specifications-DesignPatterns/Singleton_1.ekt") 
        singleton2 (snippetgroup-from-resource "/resources/EkekoX-Specifications-DesignPatterns/Singleton_JHotDraw_1a.ekt")
        matches (concat 
                  (into [] (fitness/templategroup-matches singleton1 10000))
                  (into [] (fitness/templategroup-matches singleton2 10000)))
        verifiedmatches (search/make-verified-matches matches [])]
    (println "Starting...")
    (search/evolve verifiedmatches
                   :max-generations 5
                   :initial-population (search/population-from-templates [singleton1 singleton2] 10)
                   :fitness-weights [20/20 0/20]
                   :match-timeout 10000)
    ))

;; Test suite
;; ----------

(deftest
   test-suite
   (let [testproject "TestCase-JDT-CompositeVisitor"
         metamodel "TestCase-TypeParameters"
         matchproject "TestCase-EkekoX-Matching"
         designpatterns "DesignPatterns"
         jhotdraw "JHotDraw51"]
     (test/against-project-named testproject false precision-recall)
     (test/against-project-named testproject false filtered-query)
     (test/against-project-named metamodel false scam-demo)
     (test/against-projects-named [jhotdraw designpatterns] false singleton-experiment)))

(defn test-ns-hook []
  (test/with-ekeko-disabled test-suite))

(comment 
  (run-tests))