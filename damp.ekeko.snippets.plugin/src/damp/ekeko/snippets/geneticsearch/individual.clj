(ns
  ^{:doc "Representation of an individual within a population, to be used in genetic search algorithms."
     :author "Tim Molderez"}
  damp.ekeko.snippets.geneticsearch.individual)

(defrecord 
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

(defn compute-fitness
  "Compute the fitness of an individual (if this hasn't been done before)
   and return the individual with its fitness values filled in."
  [individual fitness-func]
  (try  (if (nil? (:fitness-overall individual))
         (let [[overall components] (fitness-func (:templategroup individual))]
           (-> individual
             (assoc :fitness-overall overall)
             (assoc :fitness-components components)))
         individual)
    
    (catch Exception e
      (println "!!!")
      (inspector-jay.core/inspect individual)
      )
    ))

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