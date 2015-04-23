(ns
  ^{:doc "Representation of an individual within a population, to be used in genetic search algorithms."
     :author "Tim Molderez"}
  damp.ekeko.snippets.geneticsearch.individual
  (:require [damp.ekeko]
            [damp.ekeko.jdt
             [astnode :as astnode]])
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
             [transformation :as transformation]])
  (:import [damp.ekeko JavaProjectModel]
           [java.util.concurrent TimeoutException]
           [org.eclipse.jdt.core.dom AST]))

;(def errors (new java.util.Vector))
;(defn clear-errors [] 
;  (-> errors .clear))

(defrecord 
  ^{:doc "An individual in a population"}
  Individual
  [templategroup
   fitness-overall    ; Overall fitness value
   fitness-components ; List of fitness component values (The overall fitness is composed of these values..)
   info               ; Map of extra info about the individual (e.g. mutation operator applied to produce this individual)
   ])

(defn make-individual
  "Construct a new individual from a templategroup"
  ([template]
    (make-individual template nil))
  ([template info-map]
    (Individual. template nil nil info-map)))

(defn individual-templategroup 
  [individual]
  (:templategroup individual))

(defn individual-fitness
  [individual]
  (:fitness-overall individual))

(defn individual-fitness-components
  [individual]
  (:fitness-components individual))

(defn individual-all-info
  [individual]
  (:info individual))

(defn individual-info
  [individual key]
  (key (:info individual)))

(defn individual-add-info
  [individual info-map]
  (let [old-info (:info individual)
        new-info (merge old-info info-map)]
    (assoc individual :info new-info)))

(defn compute-fitness
  "Compute the fitness of an individual (if this hasn't been done before)
   and return the individual with its fitness values filled in."
  [individual fitness-func]
  (if (nil? (:fitness-overall individual))
    (let [[overall components] 
          (try
            (fitness-func (:templategroup individual))
            
            (catch TimeoutException e
              (println "!(timeout)")
              (persistence/spit-snippetgroup 
                (str "timeout" (util/current-time) ".ekt")
                (individual-templategroup individual))
              [0 [0 0]])
            (catch java.lang.OutOfMemoryError e
              (throw e))
            (catch clojure.lang.Compiler$CompilerException e ; This may be a wrapper to an OutOfMemoryError!
              (throw e))
            (catch Exception e
              (let [id (util/current-time)
                    tg (individual-templategroup individual)]
                (println "!")
;                (println "-" (individual-info individual :mutation-node))

;                (inspector-jay.core/inspect [id individual e])
;                (-> errors (.add [id individual e]))

;                (damp.ekeko.snippets/open-editor-on-snippetgroup tg)
                (persistence/spit-snippetgroup (str "error" id ".ekt") tg)
                (util/log "error"
                          (str 
                            "!!! " id " --- " (.getMessage e)
                            (snippetgroup/snippetgroup-name tg) 
                            "\n--- Mutation operator\n"
                            (individual-info individual :mutation-operator)
                            "\n--- Mutation subject\n"
                            (individual-info individual :mutation-node)
                            "\n--- Mutation operand values\n"
                            (individual-info individual :mutation-opvals)
                            "\n--- Template\n"
                            (persistence/snippetgroup-string tg)
                            "\n--- Stacktrace\n"
                            (if (nil? (.getCause e))
                              "No cause available.."
                              (util/stacktrace-to-string (.getCause e))) ; Because the future in util/with-timeout will wrap the exception..
                            "\n################\n\n"))
                (if (instance? java.lang.UnsupportedOperationException (.getCause e))
                  (throw e))
                [0 [0 0]]
                )))]
      (-> individual
        (assoc :fitness-overall overall)
        (assoc :fitness-components components)))
    individual))