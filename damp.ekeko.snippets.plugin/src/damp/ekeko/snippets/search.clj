(ns 
  ^{:doc "(Genetic) search for template specifications."
  :author "Coen De Roover"}
  damp.ekeko.snippets.search
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [persistence :as persistence]
             [querying :as querying]
             [matching :as matching]
             [operatorsrep :as operatorsrep]
             ]))


;; Problem representation
;; State corresponds to templategroup (can use sequence of operators later, problem is that there arguments cannot easily cross over)


(defrecord
  VerifiedMatches
  [positives negatives])

(defn
  make-verified-matches
  [positives negatives]
  (VerifiedMatches. (into #{} positives)
                    (into #{} negatives)))


(defn
  templategroup-matches
  [templategroup]
  (into #{} (eval (querying/snippetgroup-query|usingpredicates templategroup 'damp.ekeko/ekeko true)))) 

  
(defn 
  truep
  [matches verifiedmatches]
  (clojure.set/intersection matches (:positives verifiedmatches)))

(defn 
  falsep
  [matches verifiedmatches]
  (clojure.set/difference matches (:positives verifiedmatches)))

(defn
  falsen
  [matches verifiedmatches]
  (clojure.set/difference (:positives verifiedmatches) matches))
  
(defn 
  precision
  [matches verifiedmatches]
  (let [ctp (count (truep matches verifiedmatches))
        cfp (count (falsep matches verifiedmatches))]
    (if (= 0 (+ cfp ctp))
      0
      (/ ctp (+ cfp ctp))
      )))
  
(defn
  recall
  [matches verifiedmatches]
  (let [ctp (count (truep matches verifiedmatches))
        cfn (count (falsen matches verifiedmatches))]
    (/ ctp (+ ctp cfn))))
  
(defn
  fmeasure
  [matches verifiedmatches]
  (let [p (precision matches verifiedmatches)
        r (recall matches verifiedmatches)]
    (if 
      (= (+ p r) 0)
      0
      (* 2 (/ (* p r) (+ p r))))))


(defn
  make-fitness-function
  [verifiedmatches]
  (fn [templategroup]
    (let [matches (templategroup-matches templategroup)]
      (fmeasure matches verifiedmatches))))


;; Search


;;assumes: arity of tuple corresponds to number of templategroups required
(defn
  templategroup-from-tuple
  ([tuple]
    (templategroup-from-tuple tuple "A population member"))
  ([tuple name]
    (snippetgroup/make-snippetgroup name
                                    (map matching/snippet-from-node tuple))))

(defn
  population-from-tuples
  [matches] 
  (map-indexed
    (fn [idx tuple] 
      (templategroup-from-tuple tuple (str "Offspring of tuple " idx)))
    matches))


(def
  registered-operators|search
  (filter (fn [op] 
            (let [id (operatorsrep/operator-id op)]
              (some #{id} 
                    ["replace-by-variable"
                     "add-directive-equals"
                     "replace-by-wildcard"
                     "add-directive-invokes"
                     "add-directive-invokedby"
                     "restrict-scope-to-child"
                     "relax-scope-to-child+"
                     "relax-scope-to-child*"
                     "relax-size-to-atleast"
                     "relax-scope-to-member"
                     "consider-set|lst"
                     ]
                    )))
          (operatorsrep/registered-operators)
          ))

;note:clone snippetgroup best wanneer destructieve operatoren toegelaten worden op asts 
(defn
  mutate
  [snippetgroup]
  (let [snippet 
        (rand-nth (snippetgroup/snippetgroup-snippetlist snippetgroup))
        value
        (rand-nth (snippet/snippet-nodes snippet))]
    (let [operators
          (operatorsrep/applicable-operators snippetgroup snippet value registered-operators|search)
          operator
          (rand-nth operators)
          operands
          (operatorsrep/operator-operands operator)]
      (let [operandvalues
            (map 
              (fn [operand]
                (rand-nth
                  (operatorsrep/possible-operand-values|valid
                    snippetgroup snippet value operator operand)))
              operands)
            bindings
            (cons
              (operatorsrep/make-implicit-operandbinding-for-operator-subject snippetgroup snippet value operator)
              (map (fn [operand operandval]
                     (operatorsrep/make-binding operand snippetgroup snippet operandval))
                   operands
                   operandvalues))]
        (operatorsrep/apply-operator-to-snippetgroup snippetgroup 
                                                     snippet
                                                     value 
                                                     operator 
                                                     bindings)))))

(defn
  sort-by-fitness
  [population fitnessf]
  (sort-by (fn [templategroup]
             (fitnessf templategroup))
           population))


(defn 
  select
  [population tournament-size]
  (let [size (count population)]
    (nth population
         (apply min (repeatedly tournament-size #(rand-int size))))))





(defn
  evolve 
  [verifiedmatches]
  (let [fitness (make-fitness-function verifiedmatches)]
    (loop 
      [generation 0
       population (sort-by-fitness (population-from-tuples (:positives verifiedmatches)) fitness)]
      (let [best (last population)
            best-fitness (fitness best)]
        (println "Generation:" generation)
        (println "Highest fitness:" best-fitness)
        (println "Best specification:" (persistence/snippetgroup-string best))
        (when (< generation 1000)
          (if
            (> best-fitness 0.9)
            (println "Success:" (persistence/snippetgroup-string best))
            (recur 
              (inc generation)
              (sort-by-fitness
                (concat
                  (repeatedly (* 1/2 (count population)) #(mutate (select population 7)))
                  (repeatedly (* 1/2 (count population)) #(select population 7)))
                fitness))))))))          

;; todo: applicable for equals: bestaande vars (of slechts 1 nieuwe)
;; todo: gewone a* search
  
  

(comment
  
  (def templategroup
       (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokes.ekt"))
  
  (def matches (templategroup-matches templategroup))
  
  (def verifiedmatches (make-verified-matches matches []))
    
  (= 1 (precision matches verifiedmatches))
  
  (= 1 (recall matches verifiedmatches))
    
  (pmap (make-fitness-function verifiedmatches) (population-from-tuples matches))
  
  ;MethodDeclaration - MethodInvocation (vars sorted .. cannot compare otherwise)
  (map (fn [tuples] (map (fn [tuple] (map class tuple)) tuples))
        (map templategroup-matches (population-from-tuples matches)))
  
  

  )


