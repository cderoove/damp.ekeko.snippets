(ns
  ^{:doc "(Genetic) search for template specifications."
  :author "Coen De Roover, Tim Molderez"}
  damp.ekeko.snippets.geneticsearch.search
  (:refer-clojure :exclude [rand-nth rand-int rand])
  (:import 
    [damp.ekeko JavaProjectModel]
    [org.eclipse.jface.text Document]
    [org.eclipse.text.edits TextEdit]
    [org.eclipse.jdt.core ICompilationUnit IJavaProject]
    [org.eclipse.jdt.core.dom BodyDeclaration Expression Statement ASTNode ASTParser AST CompilationUnit]
    [org.eclipse.jdt.core.dom.rewrite ASTRewrite])
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko]
            [damp.ekeko.jdt
             [astnode :as astnode]
             [rewrites :as rewrites]])
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
  (:require [damp.ekeko.snippets.geneticsearch 
             [individual :as individual]
             [fitness :as fitness]])
  (:import [ec.util MersenneTwister]
           [damp.ekeko.snippets.geneticsearch PartialJavaProjectModel]
           [damp.ekeko.jdt.astnode EkekoAbsentValueWrapper]))

; Replace Java's pseudo-random number generator for MersenneTwister
(def ^:dynamic *twister* (MersenneTwister.)) ;TODO: use binding to rebind per-thread in different places of the search algo
(defn rand
  ([n] (.nextInt *twister* n)))
(defn rand-int [n]
  (int (rand n)))
(defn rand-nth [coll]
  (nth coll (rand-int (count coll))))

(def
  ^{:doc "List of all possible mutation operators"}
  registered-operators|search
  (filter 
    (fn [op] 
      (some #{(operatorsrep/operator-id op)} 
            [
             "replace-by-variable"
             "replace-by-wildcard"
;             "remove-node"
             "add-directive-equals"
             "add-directive-invokes"
;             "add-directive-invokedby"
;             "restrict-scope-to-child"
;             "relax-scope-to-child+"
;             "relax-scope-to-child*"
;             "relax-size-to-atleast"
;             "relax-scope-to-member"
             "consider-set|lst"
             "add-directive-type"
;             "add-directive-type|qname"
;             "add-directive-type|sname"
             "add-directive-refersto"
;             "erase-list"

;             "replace-parent"
;             "erase-comments"

             "add-directive-constructs"
;             "add-directive-constructedby"
;             "add-directive-overrides"
;             "generalize-directive"
             "remove-directive"
;             "extract-template" ; ! Don't use this for genetic search, as it expects a certain number of templates in a templategroup/individual
             "generalize-references"
             "generalize-types"
;             "generalize-types|qname"
             "generalize-invocations"
             "generalize-constructorinvocations"
             ]))
    (operatorsrep/registered-operators)))

(def
  ^{:doc "Default configuration options in the genetic search algorithm"}
  config-default
  {:max-generations 5
   :population-size 10
   :initial-population nil ; If nil, the initial population is generated from the verified matches
   
   :selection-weight 1/4
   :mutation-weight 3/4
   :crossover-weight 0/4
   
   :fitness-function fitness/make-fitness-function
   :fitness-weights [17/20 2/20 1/20]
   :fitness-threshold 0.9
   :fitness-filter-comp 0 ; This is the index of the fitness component that must be strictly positive; otherwise the individual will be filtered out. If -1, the overall fitness must be positive.
   
   :match-timeout 10000
   :thread-group (new ThreadGroup "Evolve")
   :tournament-rounds 7
   :mutation-operators registered-operators|search})

(defrecord
  ^{:doc "Specifies the 'oracle' for testing the fitness of templates produced by our genetic search algorithm
          The entries in :positives are snippets that a template must match, whereas
          the entries in :negatives may not match."}
  VerifiedMatches
  [positives negatives])

(defn make-verified-matches
  [positives negatives]
  (VerifiedMatches. (into #{} positives)
                    (into #{} negatives)))

(defn
  individual-from-snippet
  ([snippet]
    (individual-from-snippet snippet "A population member"))
  ([snippet name]
    (individual/make-individual
      (snippetgroup/make-snippetgroup 
       name (map matching/snippet-from-node snippet)))))

(defn
  population-from-snippets
  "Generate an initial population of individuals based on the desired matches
   (In case population-size is larger than the number of matches,
    we cycle through the matches again until the population is filled..)"
  [matches population-size]
  (assert (not (empty? matches)) "Empty initial population! Is Ekeko enabled on the right project?")
  (let [id-templates 
        (map-indexed
          (fn [idx snippet] 
            (individual-from-snippet snippet (str "Offspring of snippet " idx)))
          matches)]
    (for [x (range 0 population-size)]
      (nth id-templates (mod x (count matches))))))

(defn
  population-from-templates
  "Generate an initial population of individuals based on the desired templates
   (In case population-size is larger than the number of matches,
    we cycle through the matches again until the population is filled..)"
  [templates population-size]
  (let [id-templates (map individual/make-individual templates)]
    (for [x (range 0 population-size)]
      (nth id-templates (mod x (count templates))))))

(defn- rand-snippet [snippetgroup]
  (-> snippetgroup
    snippetgroup/snippetgroup-snippetlist
    rand-nth))

(defn operator-directive
  "Retrieve the directive that will be created by an operator"
  [operator-id]
  (case operator-id
    ; these exceptions don't follow the "add-directive-" naming convention
    "relax-scope-to-child+" matching/directive-child+
    "relax-scope-to-child*" matching/directive-child*
    "restrict-scope-to-child" matching/directive-child
    "relax-size-to-atleast" matching/directive-size|atleast
    "empty-body" matching/directive-emptybody
    "relax-scope-to-member" matching/directive-member
    "consider-set|lst" matching/directive-consider-as-set|lst
    
    ; Gross hack; FIXME later!!
    "generalize-types" matching/directive-type
    "generalize-references" matching/directive-replacedbyvariable
    "generalize-constructorinvocations" matching/directive-constructs
    ; default case; we rely on the naming convention that operator id starts with "add-directive-"
    (try
      (let [directive-func-name (subs operator-id 4)]
        (eval (read-string (str "matching/" directive-func-name))))
      (catch Exception e nil))))

(defn
  mutate
  "Perform a mutation operation on a template group. A random node is chosen among the snippets,
   and a random operation is applied to it, in order to mutate the snippet."
  [individual operators]
  (let [snippetgroup (individual/individual-templategroup individual)
        group-copy (persistence/copy-snippetgroup snippetgroup)
        operator-bias (nth (individual/individual-fitness-components individual) 3)
        snippet (rand-snippet group-copy)
        
        pick-operator
        (fn []
          (let [
                refining-ops (filter (fn [x] (= :refinement (operatorsrep/operator-category x)) ) operators)
                generalizing-ops (filter (fn [x] (= :generalization (operatorsrep/operator-category x)) ) operators)
                other-ops (filter (fn [x] (and (not= :refinement (operatorsrep/operator-category x))
                                               (not= :generalization (operatorsrep/operator-category x))) ) operators)
                
;                operator (let [rand-num (rand 100)]
;                           (if (pos? operator-bias)
;                             (cond
;                               (<= rand-num 20) (rand-nth generalizing-ops)
;                               (>= rand-num 60) (rand-nth refining-ops)
;                               :else (rand-nth other-ops))
;                             (cond
;                               (<= rand-num 40) (rand-nth generalizing-ops)
;                               (>= rand-num 80) (rand-nth refining-ops)
;                               :else (rand-nth other-ops))))
                
                operator (rand-nth operators)

                ; Pick an AST node that the chosen operator can be applied to
                all-valid-nodes (filter
                                  (fn [node]
                                    (and
                                      (operatorsrep/applicable? snippetgroup snippet node operator)
                                      ; In case of an operator that adds a directive, check that the directive isn't already there..
                                      (not (boolean
                                             (directives/bounddirective-for-directive
                                               (snippet/snippet-bounddirectives-for-node snippet node)
                                               (operator-directive (operatorsrep/operator-id operator)))))))
                                  (matching/reachable-nodes snippet (snippet/snippet-root snippet)))
;                all-valid-nodes (case (operatorsrep/operator-id operator)
;                                  "generalize-types" (filter (fn [x] (or 
;                                                                       (= "Debug" (.toString x))
;                                                                       (= "CodeGenerator" (.toString x))
;                                                                       (= "HTMLComponentFactory" (.toString x))
;                                                                       )) all-valid-nodes1)
;                                  "generalize-references" (filter (fn [x] (or 
;                                                                            (= "factory" (.toString x))
;                                                                            (= "singleton" (.toString x))
;                                                                            (= "singleton=null" (.toString x)))) all-valid-nodes1)
;                                  "replace-by-wildcard" (filter (fn [x]
;                                                                  (and
;                                                                    (or
;                                                                      true
;                                                                      (= "private" (.toString x)))
;                                                                    (not= EkekoAbsentValueWrapper (class x)))) all-valid-nodes1)
;                                  all-valid-nodes1 
;                                  )
                ]
            (if (empty? all-valid-nodes)
              (recur) ; Try again if there are no valid subjects..
              (let [subject (rand-nth all-valid-nodes)
                    operands (operatorsrep/operator-operands operator)
                    possiblevalues (for [operand operands]
                                     (operatorsrep/possible-operand-values|valid snippetgroup snippet subject operator operand))]
                (if (every? (fn [x] (not (empty? x))) possiblevalues)
                [operator subject operands (for [vals possiblevalues] (rand-nth vals))]
                (recur)))))) ; Try again if there are operands with no possible values..
        
        [operator value operands operandvalues]
        (pick-operator)
        
        bindings
        (cons
          (operatorsrep/make-implicit-operandbinding-for-operator-subject group-copy snippet value operator)
          (map (fn [operand operandval]
                 (operatorsrep/make-binding operand group-copy snippet operandval))
               operands
               operandvalues))]
    (individual/make-individual
      (operatorsrep/apply-operator-to-snippetgroup group-copy snippet value operator bindings)
      {:mutation-operator (conj (individual/individual-info individual :mutation-operator) (operatorsrep/operator-id operator))
       :mutation-node (conj (individual/individual-info individual :mutation-node) value)
       :mutation-opvals (conj (individual/individual-info individual :mutation-opvals) operandvalues)}
      )))

(defn- node-expected-class
  "Returns the expected type of an ASTnode, more specifically, the type that the parent node expects.
   This is done by looking at the child type in the property descriptor of the parent node."
  [node]
  (let [pd (astnode/owner-property node)]
    (cond 
      (astnode/property-descriptor-simple? pd) (astnode/property-descriptor-value-class pd)
      (astnode/property-descriptor-child? pd) (astnode/property-descriptor-child-node-class pd)
      (astnode/property-descriptor-list? pd) (astnode/property-descriptor-element-node-class pd)
      :else (throw (Exception. "Should not happen.")))))

(defn- non-root-ast-node?
  [node snippet]
  (and 
    (astnode/ast? node) ; Because some nodes aren't AST nodes but property descriptors..
    (not= node (snippet/snippet-root snippet))))

(defn- rand-ast-node
  "Return a random AST node (that is not the root) in a snippet"
  [snippet]
  (let [node (rand-nth (snippet/snippet-nodes snippet))]
    (if (non-root-ast-node? node snippet)
      node
      (recur snippet))))

(defn- find-compatible-ast-pair
  "Given two snippets, find a pair of nodes in each snippet's AST
   such that they can be safely swapped without causing syntax issues"
  [snippet1 snippet2]
  (let [node1 (rand-ast-node snippet1)
        cls1 (node-expected-class node1)
        ; Look for all compatible nodes in snippet2 that could be swapped with node1 
        compatible-node2s (filter
                            (fn [node2]
                              (and
                                (non-root-ast-node? node2 snippet2)
                                (instance? cls1 node2)
                                (instance? (node-expected-class node2) node1)))
                            (snippet/snippet-nodes snippet2))]
    (if (empty? compatible-node2s)
      (recur snippet1 snippet2)
      [node1 (rand-nth compatible-node2s)])))

(defn
  crossover
  "Performs a crossover between two templategroups:
   This means that two AST nodes are chosen at random, and both nodes (and their children) will be swapped.
   (In case this operation leads to invalid syntax, we try again..)
   Returns a vector containing the two crossed-over snippets"
  [ind1 ind2]
  (let
    [snippetgroup1 (individual/individual-templategroup ind1)
     snippetgroup2 (individual/individual-templategroup ind2)
     group-copy1 (persistence/copy-snippetgroup snippetgroup1)
     group-copy2 (persistence/copy-snippetgroup snippetgroup2)
     ; Get two random snippets
     snippet1 (rand-snippet group-copy1)
     snippet2 (rand-snippet group-copy2)
     ; Get two random AST nodes
     node-pair (find-compatible-ast-pair snippet1 snippet2)
     node1 (first node-pair)
     node2 (second node-pair) 
     new-snippet1 (operators/replace-node-with snippet1 node1 snippet2 node2)
     new-snippet2 (operators/replace-node-with snippet2 node2 snippet1 node1)]
    [(individual/make-individual
       (snippetgroup/replace-snippet group-copy1 snippet1 new-snippet1)
       {:crossover-old node1 :crossover-new node2})
     (individual/make-individual
       (snippetgroup/replace-snippet group-copy2 snippet2 new-snippet2)
       {:crossover-old node2 :crossover-new node1})]))

(defn 
  select
  "Do tournament selection in a population:
   Given that the population is sorted from best fitness to worst, we pick a number 
   of random entries in the population, then return the best one from those entries.
   @param tournament-size  The number of random entries to pick"
  [population tournament-size]
  (let [size (count population)]
    (nth population 
         (apply max (repeatedly tournament-size #(rand-int size))))))

(defn
  evolve
  "Look for a template that is able to match with a number of snippets, using genetic search
   @param verifiedmatches  There are the snippets we want to match with (@see make-verified-matches)
   @param conf             Configuration keyword arguments; see config-default for all default values"
  [verifiedmatches & {:as conf}]
  (let
    [output-dir (str "evolve-" (util/current-date))
     csv-name (str output-dir "/results.csv")
     csv-columns ["Generation" "Total time" "Generation time"
                  "Best fitness" "Worst fitness" "Average fitness"
                  "Best fscore" "Worst fscore" "Average fscore"
                  "Best partial" "Worst partial" "Average partial"
                  "Best dirscore" "Worst dirscore" "Average dirscore"
                  "Average op-bias"]
     start-time (. System (nanoTime))
     
     config (merge config-default conf)
     fitness ((:fitness-function config) verifiedmatches config)
     sort-by-fitness (fn [population]
                       (sort-by
                         (fn [x] (individual/individual-fitness x))
                         (map (fn [ind] (individual/compute-fitness ind fitness)) 
                              population)))
     initial-pop (sort-by-fitness (if (nil? (:initial-population config))
                                    (population-from-snippets (:positives verifiedmatches) (:population-size config))
                                    (:initial-population config))) 
     tournament-size (:tournament-rounds config)]
    (util/make-dir output-dir)
    (util/append-csv csv-name csv-columns)
    
    (loop
      [generation 0
       generation-start-time start-time
       population initial-pop
       history #{}]
      (let [new-history (atom history)
            history-hash (fn [individual] 
                           (hash (individual/individual-templategroup individual)))
            in-history (fn [individual]
                         (contains? @new-history (history-hash individual)))
            preprocess (fn [individual]
                         (if (not (in-history individual))
                           (let
                             [ind (individual/compute-fitness individual fitness)
                              filter-score (if ( = -1 (:fitness-filter-comp config))
                                             (individual/individual-fitness ind)
                                             (nth (individual/individual-fitness-components ind) (:fitness-filter-comp config)))]
                             (swap! new-history
                                    (fn [x] (clojure.set/union x #{(history-hash individual)})))
                             (if (pos? filter-score) ind))))
            best-fitness (individual/individual-fitness (last population))]
        (println "Generation:" generation)
        (println "Highest fitness:" (individual/individual-fitness (last population)))
        (println "Fitnesses:" (map individual/individual-fitness-components population))
        (println "Best specification:" (persistence/snippetgroup-string (individual/individual-templategroup (last population))))
        (util/append-csv csv-name [generation (util/time-elapsed start-time) (util/time-elapsed generation-start-time) 
                                   best-fitness ; Fitness 
                                   (individual/individual-fitness (first population))
                                   (util/average (map (fn [ind] (individual/individual-fitness ind)) population))
                                   (first (individual/individual-fitness-components (last population))) ; F-score
                                   (first (individual/individual-fitness-components (first population)))
                                   (util/average (map (fn [ind] (first (individual/individual-fitness-components ind))) population))
                                   (second (individual/individual-fitness-components (last population))) ; Partial score
                                   (second (individual/individual-fitness-components (first population)))
                                   (util/average (map (fn [ind] (second (individual/individual-fitness-components ind))) population))
                                   (nth (individual/individual-fitness-components (last population)) 2) ; Dirscore
                                   (nth (individual/individual-fitness-components (first population)) 2)
                                   (util/average (map (fn [ind] (nth (individual/individual-fitness-components ind) 2)) population))
                                   (util/average (map (fn [ind] (nth (individual/individual-fitness-components ind) 3)) population)) ; Average op-bias
                                   ])
        (util/make-dir (str output-dir "/" generation))
        (doall (map-indexed
                 (fn [idx individual]
                   (persistence/spit-snippetgroup (str output-dir "/" generation "/individual-" idx ".ekt") 
                                                  (individual/individual-templategroup individual))) 
                 population))
;        (doseq [x population]
;          (println "OP:" (individual/individual-info x :mutation-operator)))
        (when (< generation (:max-generations config))
          (if
            (> best-fitness (:fitness-threshold config))
            (do
              (println "Success:" (persistence/snippetgroup-string (individual/individual-templategroup (last population))))
              (persistence/spit-snippetgroup (str output-dir "/success.ekt") 
                                             (individual/individual-templategroup (last population))))
            (recur
              (inc generation)
              (. System (nanoTime))
              (sort-by-fitness
                ; Produce the next generation using mutation, crossover and tournament selection
                (concat
                  ; Mutation
                  (util/parallel-viable-repeat
                    (* (:mutation-weight config) (count population))
                    #(preprocess (mutate (select population tournament-size) (:mutation-operators config)))
                    (fn [x] (not (nil? x)))
                    (:thread-group config))
                  
                  ; Crossover (Note that each crossover operation produces a pair)
                  (apply concat
                         (util/parallel-viable-repeat 
                           (* (/ (:crossover-weight config) 2) (count population))
                           #(map preprocess
                                 (crossover
                                   (select population tournament-size)
                                   (select population tournament-size)))
                           (fn [x]
                             (not-any? nil? x))
                           (:thread-group config)))
                  
                  ; Selection
                  (util/parallel-viable-repeat 
                    (* (:selection-weight config) (count population)) 
                    #(select population tournament-size) 
                    (fn [ind] (pos? (individual/individual-fitness ind)))
                    (:thread-group config))))
              @new-history)))))))

(defn run-example []
  (def tg (new ThreadGroup "invokedby"))
  (def templategroup (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
  (def matches (into [] (fitness/templategroup-matches templategroup)))
  (def verifiedmatches (make-verified-matches matches []))
  (util/future-group tg (evolve verifiedmatches
                                :selection-weight 1/4
                                :mutation-weight 3/4
                                :crossover-weight 0/4
                                :max-generations 5
                                :match-timeout 12000
                                :thread-group tg
                                :population-size 10
                                :tournament-rounds 5
                                )))

(comment
  (run-example) ; To start
  (.interrupt tg) ; To stop
  
  (def templategroup
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
  
  (def matches (into [] (fitness/templategroup-matches templategroup)))
  (inspector-jay.core/inspect (nth (population-from-snippets matches 7) 6))
  
  (def templategroup
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/dbg/good-Debug.ekt"))
  (time (util/with-timeout 15000 (fitness/templategroup-matches templategroup)))
  (querying/print-snippetgroup templategroup 'damp.ekeko/ekeko)
  
  
  
  (clojure.pprint/pprint (querying/query-by-snippetgroup templategroup 'damp.ekeko/ekeko))
  (fitness/templategroup-matches templategroup) 
  @damp.ekeko.snippets.geneticsearch.fitness/matched-nodes
  (count (snippetgroup/snippetgroup-nodes templategroup))
  
  (damp.ekeko.snippets.geneticsearch.fitness/reset-matched-nodes)
  (fitness/templategroup-matches (individual/individual-templategroup (first (population-from-snippets (:positives verifiedmatches) 2))))
  
  ; Test a particular mutation operator (on a random subject)
  (do
    (def templategroup
      (persistence/slurp-from-resource "/resources/EkekoX-Specifications/singleton-mapperxml/-1228449125.ekt"))
    (def mutant
      (mutate (damp.ekeko.snippets.geneticsearch.individual/make-individual templategroup)
              (filter (fn [op] (= (operatorsrep/operator-id op) "generalize-constructorinvocations")) (operatorsrep/registered-operators))
              ))
    
    
    (persistence/snippetgroup-string (individual/individual-templategroup mutant))
    (individual/compute-fitness (individual/make-individual templategroup) (fitness/make-fitness-function
                                                                             (make-verified-matches 
                                                                               (map
                                                                                 (fn [x] (first (fitness/templategroup-matches x)))
                                                                                 [(persistence/slurp-from-resource "/resources/EkekoX-Specifications/singleton-mapperxml/-1228449125.ekt")
                                                                                 (persistence/slurp-from-resource "/resources/EkekoX-Specifications/singleton-mapperxml/-907843851.ekt")
                                                                                 (persistence/slurp-from-resource "/resources/EkekoX-Specifications/singleton-mapperxml/29895102.ekt")])
                                                                               [])
                                                                             config-default))
    (fitness/templategroup-matches (individual/individual-templategroup mutant))
    nil)
  
  ; Selection 
  (inspector-jay.core/inspect (select (population-from-snippets matches 10) 2))
  )