(ns 
  ^{:doc "(Genetic) search for template specifications."
  :author "Coen De Roover"}
  damp.ekeko.snippets.search
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
             ]))


;; Problem representation
;; State corresponds to templategroup (can use sequence of operators later, problem is that there arguments cannot easily cross over)


(defrecord
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
  templategroup-matches
  "Given a templategroup, look for all of its matches in the code"
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
  "Return a fitness function that calculates the F-measure/F1-score
   which compares the matches found by a template
   against the matches we want (or don't want)"
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
  "Generate an initial population of templates based on the desired matches"
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
  (let [copiedgroup         
        (persistence/copy-snippetgroup snippetgroup)
        snippet ; Pick a random snippet from the group
        (rand-nth (snippetgroup/snippetgroup-snippetlist copiedgroup))
        value ; Pick a random node in the snippet
        (rand-nth (snippet/snippet-nodes snippet))]
    (let [operators ; Fetch all mutation operators
          (operatorsrep/applicable-operators snippetgroup snippet value registered-operators|search)
          operator ; Pick a random mutation
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

(defn- node-expected-class
  "Returns the expected type of an ASTnode, more specifically, the type that the parent node expects.
   This is done by looking at the child type in the property descriptor of the parent node."
  [node]
  (let [pd (astnode/owner-property node)]
    (cond 
      (astnode/property-descriptor-simple? pd) (astnode/property-descriptor-value-class pd)
      (astnode/property-descriptor-child? pd) (astnode/property-descriptor-child-node-class pd)
      (astnode/property-descriptor-list? pd) (astnode/property-descriptor-element-node-class pd)
      :else (type node))))

(defn rand-ast-node
  "Return a random AST node in a snippet"
  [snippet]
  (let [node (rand-nth (snippet/snippet-nodes snippet))]
    (if (astnode/ast? node) ; Some of these nodes appear to be wrappers? (produced by astnode/make-value) 
      node
      (recur snippet))))

(defn rand-typed-ast-node
  "Return a random AST node in a snippet that must be an instance of a certain type"
  [snippet cls]
  (let [node (rand-nth (snippet/snippet-nodes snippet))]
    (if (and (astnode/ast? node) (instance? cls node)) 
      node
      (recur snippet cls))))

(defn
  crossover
  "Performs a crossover between two snippets:
   This means that two AST nodes are chosen at random, and both nodes (and their children) will be swapped.
   (In case this operation leads to invalid syntax, we try again..)
   Returns a vector containing the two crossed-over snippets"
  [snippetgroup1 snippetgroup2]
  (let
    [snippet1 (rand-nth snippetgroup1)
     snippet2 (rand-nth snippetgroup2)
     ; Get two random AST nodes
     node1 (rand-ast-node snippet1)
     node2 (rand-typed-ast-node snippet2 (node-expected-class node1))]
    (println node1)
    (println node2)
    (println (:ast snippet1))
    (println (node-expected-class node1))
    ;(inspect (astnode/owner-property node1))
    [(operators/replace-node-with snippet1 node1 node2)
     (operators/replace-node-with snippet2 node2 node1)]
    ;(rewrites/replace-node (ASTRewrite/create (.getAST node1))  node1 node2)
    ))

;(use '(inspector-jay core))
;(let [] 
;  (inspect
;     (let [pop (population-from-tuples matches)
;           group1 (snippetgroup/snippetgroup-snippetlist (rand-nth pop))
;           group2 (snippetgroup/snippetgroup-snippetlist (rand-nth pop))
;           group1-copy (persistence/copy-snippetgroup group1)
;           group2-copy (persistence/copy-snippetgroup group1)]
;       (crossover group1-copy group2-copy)))
;  nil)

(defn
  sort-by-fitness
  "Sort the templates in a population by the fitness function"
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
        (when (< generation 10)
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
       (persistence/slurp-from-resource "/resources/EkekoX-Specifications/anymethod.ekt"))
  (def matches (templategroup-matches templategroup))
  (def verifiedmatches (make-verified-matches matches []))
  
  (= 1 (precision matches verifiedmatches))
  (= 1 (recall matches verifiedmatches))
  
  (pmap (make-fitness-function verifiedmatches) (inspect (population-from-tuples matches)))
  
  ;MethodDeclaration - MethodInvocation (vars sorted .. cannot compare otherwise)
  (map (fn [tuples] (map (fn [tuple] (map class tuple)) tuples))
       
        ; Test each generated template
        (map templategroup-matches (population-from-tuples matches)))
  
;  (evolve verifiedmatches)
  )