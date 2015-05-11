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
   :mutation-weight 2/4
   :crossover-weight 1/4
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
            ["generalize-references"
             "generalize-types"
             "generalize-types|qname"
             "extract-template"
             "generalize-invocations"
             "generalize-constructorinvocations"]))
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
                                  (search/population-from-templates initial-population (:population-size merged-cfg))})
              verifiedmatches (search/make-verified-matches
                                (mapcat (fn [x] (into [] (fitness/templategroup-matches x)))
                                        verified)
                                [])] 0
;          (apply search/evolve verifiedmatches (mapcat identity (vec merged-cfg2)))
          )))))

(defn run-experiment-from-files
  ([projects config verifiedmatches-ekt]
    (run-experiment-from-files projects config verifiedmatches-ekt verifiedmatches-ekt))
  ([projects config initial-population-ekt verifiedmatches-ekt]
    (run-experiment projects config
                    (map snippetgroup-from-resource initial-population-ekt)
                    (map snippetgroup-from-resource verifiedmatches-ekt))))

(defn run-pmart-experiment
  ([projects pattern config]
    (run-experiment projects config (pmart/pattern-instances-as-templategroups (pmart/parse-pmart-xml) projects pattern)))
  ([folder-name projects pattern config]
    (run-experiment
      projects
      config
      (pmart/slurp-pattern-instances-as-templategroups 
        folder-name
        (pmart/parse-pmart-xml)
        projects pattern (fn [x] true))))
  ([folder-name initial-pop projects pattern config]
    (run-experiment
      projects
      config
      (pmart/slurp-pattern-instances-as-templategroups 
        folder-name
        (pmart/parse-pmart-xml)
        projects pattern
        (fn [cls] 
          (some (fn [individual] (= individual (str (hash cls) "-" (last (clojure.string/split cls #"\.")) ".ekt"))) initial-pop))))))

(comment
  ; Sanity check
  (run-experiment-from-files
   ["TestCase-JDT-CompositeVisitor"]
   {:max-generations 50}
   ["/resources/EkekoX-Specifications/invokedby.ekt"])
  
  ; Singleton: From DesignPatterns to JHotDraw
  (run-experiment-from-files
   [(:jhotdraw pmart/projects)]
   {:max-generations 50
    :mutation-operators search/registered-operators|search}
   ["/resources/EkekoX-Specifications-DesignPatterns/Singleton_1.ekt"]
   ["/resources/EkekoX-Specifications-DesignPatterns/Singleton_JHotDraw_1_alt.ekt"])
  
  ; Singleton: Generalize all instances into one template
  (run-experiment-from-files
   ["DesignPatterns" (pmart/projects :jhotdraw)]
   {}
   ["/resources/EkekoX-Specifications-DesignPatterns/Singleton_JHotDraw_1a.ekt" 
    "/resources/EkekoX-Specifications-DesignPatterns/Singleton_1.ekt"])
  
  (run-pmart-experiment 
    [(pmart/projects :mapperxml)] "Singleton"
    {:max-generations 800
     :population-size 20})
  
  ; Singleton in MapperXML
  (def tg (new ThreadGroup "experiment"))
  (util/future-group 
    tg
    (run-pmart-experiment
      (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile "/resources/EkekoX-Specifications-DesignPatterns/singleton-mapperxml")
      [(pmart/projects :mapperxml)] "Singleton"
      {:max-generations 200
       :match-timeout 120000
       :fitness-weights [17/20 2/20 1/20]
       :population-size 10
       :selection-weight 1/8
       :mutation-weight 7/8
       :crossover-weight 0/8
       :tournament-rounds 2
       :thread-group tg}))
  (.interrupt tg)
  
  ; Template method in JHotDraw
  (def tg (new ThreadGroup "experiment"))
  (util/future-group 
    tg
    (run-pmart-experiment
      (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile "/resources/EkekoX-Specifications/dbg/templatemethod-jhotdraw")
      ["1655477502-ImageFigure.ekt" "-2090076616-PolygonFigure.ekt" "-1685183065-AbstractFigure.ekt" "2098468581-AttributeFigure.ekt"]
      [(pmart/projects :jhotdraw)] "Template Method"
      {:max-generations 200
       :match-timeout 120000
       :fitness-weights [17/20 2/20 1/20]
       :population-size 10
       :selection-weight 1/8
       :mutation-weight 7/8
       :crossover-weight 0/8
       :tournament-rounds 2
       :thread-group tg}))
  (.interrupt tg)
  
  
  )

;(deftest
;  ^{:doc "Try to infer the general template of the TemplateGroup pattern scam_demo1.ekx's left-hand-side template"}
;  singleton-experiment
;  ; Now mutating a singleton from one project into one from another project..
;  (let [singleton1 (snippetgroup-from-resource "/resources/EkekoX-Specifications-DesignPatterns/Singleton_1.ekt") 
;        singleton2 (snippetgroup-from-resource "/resources/EkekoX-Specifications-DesignPatterns/Singleton_JHotDraw_1a.ekt")
;        matches (concat 
;                  (into [] (fitness/templategroup-matches singleton1))
;                  (into [] (fitness/templategroup-matches singleton2))
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