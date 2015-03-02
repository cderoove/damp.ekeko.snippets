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
             [transformation :as transformation]])
  (:require [damp.ekeko.snippets.geneticsearch 
             [individual :as individual]
             [fitness :as fitness]])
  (:import [ec.util MersenneTwister]
           [damp.ekeko.snippets.geneticsearch PartialJavaProjectModel]))

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
             ;untested:
;             "replace-parent"
;             "erase-comments"
             ]))
    (operatorsrep/registered-operators)))

(def
  ^{:doc "Default configuration options in the genetic search algorithm"}
  config-default
  {:max-generations 5
   :population-size 100
   
   :selection-weight 1/4
   :mutation-weight 3/4
   :crossover-weight 0/4
   
   :fitness-function fitness/make-fitness-function
   :fitness-weights [19/20 1/20]
   :fitness-threshold 0.95
   
   :match-timeout 10000
   :tournament-rounds 7
   :mutation-operators registered-operators|search})

(defrecord
  ^{:doc "Specifies the 'oracle' for testing the fitness of templates produced by our genetic search algorithm
          The entries in :positives are snippets that a template must match, whereas
          the entries in :negatives may not match."}
  VerifiedMatches
  [positives negatives])

(defn
  make-verified-matches
  "Create a record of verified matches, consisting of a number of positive and negative matches.
   The positive ones are those that the resulting template must match;
   the negative one are those it may not match."
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
  "Generate an initial population of individuals based on the desired matches"
  [matches]
  (let [id-templates 
        (map-indexed
          (fn [idx snippet] 
            (individual-from-snippet snippet (str "Offspring of snippet " idx)))
          matches)]
    (mapcat identity (repeat 1 id-templates))))

(defn- rand-snippet [snippetgroup]
  (-> snippetgroup
    snippetgroup/snippetgroup-snippetlist
    rand-nth))

(def operator-directive
  "Retrieve the directive that will be created by an operator
   !! For now, this relies on the naming convention that the operator's id starts with 'add-'
   , followed by the directive's name.."
  (clojure.core.memoize/memo
    (fn [operator-id] 
      (try
        (let [directive-func-name (subs operator-id 4)]
         (eval (read-string (str "matching/" directive-func-name))))
        (catch Exception e nil))))
  ;  (cond
  ;    "replace-by-variable" matching/directive-replacedbyvariable
  ;    "replace-by-exp" matching/directive-replacedbyexp
  ;    "add-directive-equals" matching/directive-equals
  ;    "add-directive-invokes" matching/directive-invokes
  ;    
  ;    :rest nil)
  )

(defn
  mutate
  "Perform a mutation operation on a template group. A random node is chosen among the snippets,
   and a random operation is applied to it, in order to mutate the snippet."
  [individual operators]
  (let [snippetgroup (individual/individual-templategroup individual)
        ;group-copy (persistence/copy-snippetgroup snippetgroup)
        group-copy snippetgroup
        snippet (rand-snippet group-copy)
        
        pick-operator
        (fn []
          (let [operator (rand-nth operators)
                ; Pick an AST node that the chosen operator can be applied to
                all-valid-nodes (filter
                                  (fn [node] 
                                    (and (operatorsrep/applicable? snippetgroup snippet node operator)
                                         ; Check that you haven't already applied this operation to this node..
                                         (not (boolean
                                                (directives/bounddirective-for-directive
                                                  (snippet/snippet-bounddirectives-for-node snippet node)
                                                  (operator-directive (operatorsrep/operator-id operator)))))))
                                  (snippet/snippet-nodes snippet))]
            (if (empty? all-valid-nodes)
              (recur)
              [operator (rand-nth all-valid-nodes)])))
        
        [operator value] (pick-operator)
        operands (operatorsrep/operator-operands operator)
        
        operandvalues
        (map
          (fn [operand]
            (rand-nth
              (operatorsrep/possible-operand-values|valid
                    group-copy snippet value operator operand)))
          operands)
        
        bindings
        (cons
          (operatorsrep/make-implicit-operandbinding-for-operator-subject group-copy snippet value operator)
          (map (fn [operand operandval]
                 (operatorsrep/make-binding operand group-copy snippet operandval))
               operands
               operandvalues))]
    (individual/make-individual
      (operatorsrep/apply-operator-to-snippetgroup group-copy snippet value operator bindings)
      {:mutation-operator (operatorsrep/operator-id operator)
       :mutation-node value
       :mutation-opvals operandvalues}
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
       (snippetgroup/snippetgroup-replace-snippet group-copy1 snippet1 new-snippet1)
       {:crossover-old node1 :crossover-new node2})
     (individual/make-individual
       (snippetgroup/snippetgroup-replace-snippet group-copy2 snippet2 new-snippet2)
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

;(defn- correct-implicit-operands?
;  [snippetgroup]
;  (reduce (fn [sofar node]
;            (and 
;              sofar
;              (every? (fn [bd]
;                        (let [implOp (first (.getOperandBindings bd))]
;                          (= (.getValue implOp) node)))
;                      (snippet/snippet-bounddirectives-for-node node))))
;          true
;          (mapcat snippet/snippet-nodes snippetgroup)))

(defn
  evolve
  "Look for a template that is able to match with a number of snippets, using genetic search
   @param verifiedmatches  There are the snippets we want to match with (@see make-verified-matches)
   @param conf             Configuration keyword arguments; see config-default for all default values"
  [verifiedmatches & {:as conf}]
  (let
    [config (merge config-default conf)
     fitness ((:fitness-function config) verifiedmatches config)
     set-fit (fn [individual] (individual/compute-fitness individual fitness))
     sort-by-fitness (fn [population]
                       (sort-by
                         (fn [x] (individual/individual-fitness x))
                         (map (fn [ind] (individual/compute-fitness ind fitness)) 
                              population)))
     initial-pop (sort-by-fitness (population-from-snippets (:positives verifiedmatches))) 
     tournament-size (:tournament-rounds config)] 
    (loop
      [generation 0
       population initial-pop
       history #{}]
      (let [new-history (atom history)
            best (last population)
            best-fitness (individual/individual-fitness best)
            history-hash (fn [individual] 
                           (hash (individual/individual-templategroup individual)))
            in-history (fn [individual]
                         (contains? @new-history (history-hash individual)))
            
            preprocess (fn [individual]
                         (if (not (in-history individual))
                           (let
                             [ind (individual/compute-fitness individual fitness)]
                             (swap! new-history
                                    (fn [x] (clojure.set/union x #{(history-hash individual)})))
                             (if (pos? (individual/individual-fitness ind)) 
                               ind))))
            
            ;            is-viable (fn [individual]
            ;                        (and
            ;                          ; We ignore the individuals we've seen before
            ;                          (not (contains? history (history-hash individual)))
            ;                          ; .. and those with fitness 0
            ;                          (util/dbg (pos? (individual/individual-fitness individual)))
            ;                          ))
            ;            
            ;            new-history (clojure.set/union
            ;                          history
            ;                          (set (map history-hash population)))
            ]
        (println "Generation:" generation)
        (println "Highest fitness:" best-fitness)
        (println "Fitnesses:" (map individual/individual-fitness population))
        (println "Best specification:" (persistence/snippetgroup-string (individual/individual-templategroup best)))
        
        (when (< generation (:max-generations config))
          (if
            (> best-fitness (:fitness-threshold config))
            (println "Success:" (persistence/snippetgroup-string (individual/individual-templategroup best)))
            (recur
              (inc generation)
              (sort-by-fitness
                ; Produce the next generation using mutation, crossover and tournament selection
                (concat
                  ; Mutation
                  (util/viable-repeat
                    (* (:mutation-weight config) (count population))
                    #(preprocess (mutate (select population tournament-size) (:mutation-operators config)))
                    (fn [x] (not (nil? x))))
                  
                  ; Crossover (Note that each crossover operation produces a pair)
                  (apply concat
                         (util/viable-repeat 
                           (* (/ (:crossover-weight config) 2) (count population))
                           #(map preprocess
                                 (crossover
                                   (select population tournament-size)
                                   (select population tournament-size)))
                           (fn [x] (not (some? nil? x)))))
                  
                  ; Selection
                  (util/viable-repeat 
                    (* (:selection-weight config) (count population)) 
                    #(select population tournament-size) 
                    (fn [x] true))))
              @new-history)))))))

(comment
  (def templategroup
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
  (def matches (fitness/templategroup-matches templategroup 10000))
  (def verifiedmatches (make-verified-matches matches []))
  (evolve verifiedmatches
          :max-generations 25
          :fitness-weights [20/20 0/20]
          :match-timeout 2000)
  
  (damp.ekeko.snippets.matching/reset-matched-nodes)
  (def templategroup
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
;  (clojure.pprint/pprint (querying/snippetgroup-query|usingpredicates templategroup 'damp.ekeko/ekeko true))
  (templategroup-matches templategroup 10000)  
  (count @damp.ekeko.snippets.matching/matched-nodes)
  )

;; todo: applicable for equals: bestaande vars (of slechts 1 nieuwe)
;; todo: gewone a* search  