(ns 
  ^{:doc "Provides integration with the JMetal library, so we can do multi-objective genetic search
          in order to automatically find a template from a given set of code snippets"
    :author "Tim Molderez"}
damp.ekeko.snippets.jmetal
  (:refer-clojure :exclude [rand-nth rand-int rand])
  (:require [inspector-jay [core :as jay]])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [persistence :as persistence]
             [querying :as querying]
             [matching :as matching]
             [operators :as operators]
             [operatorsrep :as operatorsrep]
             [util :as util]
             [directives :as directives]
             [search :as search]])
  (:import [jmetal.core Solution SolutionSet Problem]
           [jmetal.util Ranking]
           [jmetal.metaheuristics.ibea IBEA]))

(defn make-problem []
  (proxy [Problem] []
    (getNumberOfObjectives [] 2)
    ; We won't be using this method..
    (evaluate [solution] nil)))

; Make a JMetal wrapper for an individual
(defn make-solution [individual]
  (proxy [Solution] [2]
    ; Hrm.. this is pretty gross.. can't add new methods using proxy, so just hijacking some unused method..
    (clone [] individual)))

(defn- population-to-solution-set
  "Wrap a population in a JMetal SolutionSet"
  [population verifiedmatches]
  (let [pop-size (count population)
        solution-set (new SolutionSet pop-size)]
    (doseq [i (range 0 pop-size)]
      (let [templategroup (nth population i)
            sol (make-solution templategroup)
            matches (search/templategroup-matches templategroup)]
        (.setObjective sol 0 (search/fmeasure matches verifiedmatches))
        (.setObjective sol 1 (search/directive-count-measure templategroup))
        (.add solution-set sol)))
    solution-set))

(defn- solution-set-to-population
  "Obtain the population encapsulated by a JMetal SolutionSet"
  [solution-set]
  (map (fn [x] (.clone x)) 
       (iterator-seq (.iterator solution-set))))

(defn ibea-algorithm [verifiedmatches population-size archive-size max-evalutions]
  (proxy [IBEA] [(make-problem)]
    (execute []
      (let [initial-population (search/population-from-tuples (:positives verifiedmatches))
            initial-solution-set (population-to-solution-set initial-population verifiedmatches)]
        
        ; Evolution loop
        (loop [evaluations population-size
               solution-set initial-solution-set 
               archive (new SolutionSet archive-size)]
          (if (< evaluations max-evalutions)
            ; Create the next generation
            (let [union (.union solution-set archive)]
              (.calculateFitness this union)
              (let [new-archive union]
                (while (> (.size new-archive) population-size)
                  (.removeWorst this new-archive))
                (let [current-population (solution-set-to-population new-archive)]
                  (recur 
                    (inc evaluations)
                    (population-to-solution-set 
                      (search/generate-new-population current-population)
                      verifiedmatches)
                    new-archive))))
            ; Create a ranking once we're all done
            (.getSubfront (new Ranking archive) 0)))))))

(defn ibea-evolve [verifiedmatches]
  (let [algo (ibea-algorithm verifiedmatches 10 10 20)]
    (.execute algo)))

(comment
  (def templategroup
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
  (def matches (search/templategroup-matches templategroup))
  (def verifiedmatches (search/make-verified-matches matches []))
  (ibea-evolve verifiedmatches)
)