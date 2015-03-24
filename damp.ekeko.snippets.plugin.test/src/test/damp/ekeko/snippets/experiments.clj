(ns 
  ^{:doc "Suite containing experiments of the genetic search algorithm."
    :author "Tim Molderez"}
  test.damp.ekeko.snippets.experiments
  (:refer-clojure :exclude [== type declare record?])
  (:require  [clojure.core.logic :exclude [is] :as l])
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [matching :as matching]
             [parsing :as parsing]
             [operatorsrep :as operatorsrep]
             [util :as util]
             [persistence :as persistence]
             [transformation :as transformation]])
  (:require [damp.ekeko.snippets.geneticsearch
             [fitness :as fitness]
             [search :as search]
             [pmart :as pmart]])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]])
  (:require [damp.ekeko 
             [logic :as el]
             [snippets :as snippets]])
  (:import [damp.ekeko.snippets.geneticsearch PartialJavaProjectModel]) 
  (:use clojure.test))

(defn
  snippetgroup-from-resource
  [pathrelativetobundle]
  (persistence/slurp-snippet (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))

(def experiment-config-default
  {:max-generations 50
   :fitness-weights [19/20 1/20]
   :match-timeout 30000
   :selection-weight 1/4
   :mutation-weight 3/4
   :population-size 20
   :tournament-rounds 7})


(def
  atomic-operators
  (filter 
    (fn [op] 
      (some #{(operatorsrep/operator-id op)} 
            [
             "replace-by-variable"
             "replace-by-wildcard"
             "remove-node"
             "add-directive-equals"
             "add-directive-invokes"
             "add-directive-invokedby"
             "restrict-scope-to-child"
            ; "relax-scope-to-child+"
            ; "relax-scope-to-child*"
             "relax-size-to-atleast"
             "relax-scope-to-member"
             "consider-set|lst"
             "add-directive-type"
             "add-directive-type|qname"
             "add-directive-type|sname"
             "add-directive-refersto"
;             "replace-parent"
;             "erase-comments"
             "add-directive-constructs"
             "add-directive-constructedby"
             "add-directive-overrides"
             "generalize-directive"
             "remove-directive"
;             "generalize-references"
;             "generalize-types"
;             "generalize-types|qname"
;             "extract-template"
;             "generalize-invocations"
;             "generalize-constructorinvocations"
             ]))
    (operatorsrep/registered-operators)))

(def
  composite-operators
  (filter 
    (fn [op] 
      (some #{(operatorsrep/operator-id op)} 
            [
;             "replace-by-variable"
;             "replace-by-wildcard"
;             "remove-node"
;             "add-directive-equals"
;             "add-directive-invokes"
;             "add-directive-invokedby"
;             "restrict-scope-to-child"
            ; "relax-scope-to-child+"
            ; "relax-scope-to-child*"
;             "relax-size-to-atleast"
;             "relax-scope-to-member"
;             "consider-set|lst"
;             "add-directive-type"
;             "add-directive-type|qname"
;             "add-directive-type|sname"
;             "add-directive-refersto"
;             "replace-parent"
;             "erase-comments"
;             "add-directive-constructs"
;             "add-directive-constructedby"
;             "add-directive-overrides"
             "generalize-directive"
;             "remove-directive"
             "generalize-references"
             "generalize-types"
             "generalize-types|qname"
             "extract-template"
             "generalize-invocations"
             "generalize-constructorinvocations"
             ]))
    (operatorsrep/registered-operators)))

(defn run-experiment
  ([projects config verified]
    ; In this case, the verified matches are also used as initial population..
    (run-experiment projects config verified verified))
  ([projects config initial-population verified]
    (test/against-projects-named 
      projects false
      (fn []
        (let [merged-cfg (merge experiment-config-default config)
              merged-cfg2 (merge merged-cfg 
                                 {:initial-population 
                                  (search/population-from-templates
                                    initial-population
                                    (:population-size merged-cfg))})
              verifiedmatches (search/make-verified-matches
                                (mapcat (fn [x] (into [] (fitness/templategroup-matches x (:match-timeout merged-cfg))))
                                        verified)
                                [])]
          (apply search/evolve verifiedmatches (mapcat identity (vec merged-cfg2))))))))

(defn run-experiment-from-files
  ([projects config verifiedmatches-ekt]
    (run-experiment-from-files projects config verifiedmatches-ekt verifiedmatches-ekt))
  ([projects config initial-population-ekt verifiedmatches-ekt]
    (run-experiment projects config
                    (map snippetgroup-from-resource initial-population-ekt)
                    (map snippetgroup-from-resource verifiedmatches-ekt))))

(comment
  ; Sanity check
  (run-experiment-from-files
   ["TestCase-JDT-CompositeVisitor"]
   {:max-generations 50}
   ["/resources/EkekoX-Specifications/invokedby.ekt"])
  
  ; Singleton: From DesignPatterns to JHotDraw
  (run-experiment-from-files
   [(:jhotdraw pmart/projects)]
   {:max-generations 10
    :mutation-operators search/registered-operators|search}
   ["/resources/EkekoX-Specifications-DesignPatterns/Singleton_1.ekt"]
   ["/resources/EkekoX-Specifications-DesignPatterns/Singleton_JHotDraw_1_alt.ekt"])
  
  ; Singleton: Generalize all instances into one template
  (run-experiment-from-files
   ["DesignPatterns" (pmart/projects :jhotdraw)]
   {}
   ["/resources/EkekoX-Specifications-DesignPatterns/Singleton_JHotDraw_1a.ekt" 
    "/resources/EkekoX-Specifications-DesignPatterns/Singleton_1.ekt"])
  
  (run-experiment
    [(pmart/projects :uml)]
    {:max-generations 0
     :population-size 5}
    (pmart/pattern-instances-as-templategroups 
      (pmart/parse-pmart-xml)
      [(pmart/projects :uml)]
      "Observer"))
  )

;(deftest
;  ^{:doc "Try to infer the general template of the TemplateGroup pattern scam_demo1.ekx's left-hand-side template"}
;  singleton-experiment
;  ; Now mutating a singleton from one project into one from another project..
;  (let [singleton1 (snippetgroup-from-resource "/resources/EkekoX-Specifications-DesignPatterns/Singleton_1.ekt") 
;        singleton2 (snippetgroup-from-resource "/resources/EkekoX-Specifications-DesignPatterns/Singleton_JHotDraw_1a.ekt")
;        matches (concat 
;                  (into [] (fitness/templategroup-matches singleton1 10000))
;                  (into [] (fitness/templategroup-matches singleton2 10000))
;                  )
;        verifiedmatches (search/make-verified-matches matches [])]
;    (println "Starting...")
;    (search/evolve verifiedmatches
;                   :max-generations 50
;                   :initial-population (search/population-from-templates [singleton2] 10)
;                   :fitness-weights [18/20 2/20]
;                   :match-timeout 10000)))

;; Test suite
;; ----------

;(deftest
;   test-suite
;   (let [testproject "TestCase-JDT-CompositeVisitor"
;         metamodel "TestCase-TypeParameters"
;         matchproject "TestCase-EkekoX-Matching"
;         designpatterns "DesignPatterns"
;         jhotdraw "JHotDraw51"]
;     (test/against-projects-named [jhotdraw designpatterns] false singleton-experiment)))
;
;(defn test-ns-hook []
;  (test/with-ekeko-disabled test-suite))

(comment 
  (run-tests))