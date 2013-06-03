(ns damp.ekeko.snippets.searchspace
  (:refer-clojure :exclude [== type])
  (:require [clojure.core.logic :as cl]) 
  (:require [damp.ekeko 
             [snippets :as snippets]])
  (:require [damp.ekeko.snippets 
             [representation :as representation]
             [operatorsrep :as operatorsrep]
             [operators :as operators]
             [precondition :as precondition]])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko])
  (:require [damp.ekeko.logic :as el]))    


(defn dfs
  [init-value goal-positive goal-negative 
   values map-operators 
   apply-function filter-function check-function opname-function]

  (defn nothing [x y] x)
  
  (let [nmap-operators (assoc map-operators :nothing nothing)
        operators (keys nmap-operators)]

    ;;operator function
    (defn operator-function [op] (get nmap-operators op)) 

    ;;todo --> list of [operator prev-result list-value path]
    (defn operator [todo] (first todo))
    (defn prev-result [todo] (fnext todo))
    (defn value [todo] (first (first (nnext todo))))
    (defn next-values [todo] (rest (first (nnext todo))))
    (defn path [todo] (last todo)) 

    ;;function to process todo, apply the operator to value, return the result
    (defn process
      [todo]
      (if (= (operator todo) :nothing)
        (prev-result todo)
        (apply-function (prev-result todo)  (operator todo) (value todo) nil)))

    ;;function to generate todo for the first time
    (defn generate-todo
      [init-value list-value result-check]
      (filter 
        (fn [todo] (filter-function (operator todo) (value todo) result-check))
        (map (fn [op] [op init-value list-value []]) operators)))

    ;;function to generate children of todo 
    ;;children of todo are --> apply all operators with next value
    (defn children
      [todo result-check]
      (let [curr-result (process todo)
            next-values (next-values todo)
            curr-path (if (= (operator todo) :nothing)
                        (path todo)
                        (conj (path todo) [(operator todo) (value todo)]))]
        (if (empty? next-values)
          '() 
          (filter 
            (fn [todo] (filter-function (operator todo) (value todo) result-check))
            (map (fn [op] [op curr-result next-values curr-path]) operators)))))

    ;;dfs algorithm
    (defn process-dfs
      [list-todo goal-positive goal-negative]
      (let [todo (first list-todo)]
        (if (empty? list-todo) 
          (do 
            (println "----------- FAIL ----------------------")
            [])
          (let [result-check (check-function (process todo) goal-positive goal-negative)]
            (if (= result-check :succeed)
              (do 
                (println "----------- SUCCEED ----------------------")
                (println "SUCCEED: " (conj (path todo) [(operator todo) (value todo)]))
                (conj (path todo) [(opname-function (operator todo)) (value todo)]))
              (do
                (println "----------- NEXT ----------------------")
                (println "Operator: " result-check) 
                (println (operator todo)) 
                (println (value todo)) 
                (process-dfs 
                  (concat (children todo result-check) (rest list-todo)) 
                  goal-positive 
                  goal-negative)))))))
    
    (process-dfs 
      (generate-todo init-value values (check-function init-value goal-positive goal-negative)) 
      goal-positive 
      goal-negative)))

(defn check-result
  ;return  :succeed, if result = goal-positive
  ;return  :refinement, if result contains negative examples but contains all positive examples (refine)
  ;return  :generalization, if result contains no negative examples but not contains all positive examples (generalize)
  ;return  :any, if result contains negative examples and not contains all positive examples
  [new-group goal-positive goal-negative]
  (let [result (snippets/query-by-snippetgroup new-group)]
    (cond 
      (= (test/tuples-to-stringsetstring result)
         (test/tuples-to-stringsetstring goal-positive))
      :succeed
      (test/tuples-aresubset goal-positive result)
      :refinement
      (test/tuples-aresubset result goal-positive)
      :generalization
      :else
      :any)))

(defn check-operator
  [op-id node result-check]
  (or
    (= op-id :nothing)
    (and 
      (precondition/safe-operator-for-node? op-id node)
      (= (operatorsrep/operator-type op-id) result-check))
    (and 
      (precondition/safe-operator-for-node? op-id node)
      (= result-check :any))))

;to test this, put negated-node precondition to is-simplename?
;negated-node generated queries that fails every part of selected node 
;time-consuming when execute the query

(defn dfs-snippet [group goal-positive goal-negative] 
  (let [group-nodes (representation/snippetgroup-nodes group)]
    (dfs 
      group 
      goal-positive
      goal-negative
      group-nodes
      operatorsrep/searchspace-operators
      operatorsrep/apply-operator-to-snippetgroup
      check-operator 
      check-result
      operatorsrep/operator-name)))

(defn iterate-snippet [group goal-positive goal-negative] 
  (defn apply-operator-to-nodes
    [group operator nodes]
    (println "next operator")
    (if (empty? nodes) 
      []
      (if (check-operator operator (first nodes) :any)
        (let [new-group (operatorsrep/apply-operator-to-snippetgroup group operator (first nodes) nil)
              result-check (check-result new-group goal-positive goal-negative)]
          (println "process" operator (first nodes))
          (if (= result-check :succeed)
            (do 
              (println "succeed" operator (first nodes))
              [[(operatorsrep/operator-name operator) (first nodes)]])
            (apply-operator-to-nodes group operator (rest nodes))))
        (apply-operator-to-nodes group operator (rest nodes)))))
  (defn apply-operators
    [group operators nodes]
    (println "start")
    (if (empty? operators) 
      []
      (let [result (apply-operator-to-nodes group (first operators) nodes)]
        (if (empty? result)
          (apply-operators group (rest operators) nodes)
          result))))
  (let [group-nodes (representation/snippetgroup-nodes group)
        result-check (check-result group goal-positive goal-negative)
        operators (cond 
                    (= result-check :refinement) (operatorsrep/searchspace-refinement-operator-ids)
                    (= result-check :generalization) (operatorsrep/searchspace-generalization-operator-ids)
                    :else (operatorsrep/searchspace-operator-ids))]
    (apply-operators group operators group-nodes)))



(comment 
(defn dfs-tree-snippet-old [group snippet goal] 
  (defn result-equal 
    [new-snippet goal]
    (= (test/tuples-to-stringsetstring 
         (snippets/query-by-snippet-in-group new-snippet (operators/remove-snippet group snippet)))
       (test/tuples-to-stringsetstring goal)))
  (let [snippet-nodes
        (remove  (fn [x] (= (:ast snippet) x)) (representation/snippet-nodes snippet))]
    (dfs 
      snippet 
      goal 
      snippet-nodes
      operatorsrep/searchspace-operators
      precondition/safe-operator-for-node? 
      result-equal
      operatorsrep/operator-name)))

(defn dfs-snippet-old [group snippet goal] 
  (defn result-equal 
    [new-snippet goal]
    (= (test/tuples-to-stringsetstring 
         (snippets/query-by-snippet-in-group new-snippet (operators/remove-snippet group snippet)))
       (test/tuples-to-stringsetstring goal)))
  (defn apply-operator-to-nodes
    [snippet operator nodes]
    (println "next operator")
    (if (empty? nodes) 
      []
      (if (precondition/safe-operator-for-node? operator (first nodes))
        (let [new-snippet (operatorsrep/apply-operator snippet operator (first nodes) nil)]
          (println "process" operator (first nodes))
          (if (result-equal new-snippet goal)
            (do 
              (println "succeed" operator (first nodes))
              [[(operatorsrep/operator-name operator) (first nodes)]])
            (apply-operator-to-nodes snippet operator (rest nodes))))
        (apply-operator-to-nodes snippet operator (rest nodes)))))
  (defn apply-operators
    [snippet operators nodes]
    (println "start")
    (if (empty? operators) 
      []
      (let [result (apply-operator-to-nodes snippet (first operators) nodes)]
        (if (empty? result)
          (apply-operators snippet (rest operators) nodes)
          result))))
  (let [snippet-nodes
        (remove  (fn [x] (= (:ast snippet) x)) (representation/snippet-nodes snippet))]
    (apply-operators snippet (operatorsrep/searchspace-operator-ids) snippet-nodes)))
)