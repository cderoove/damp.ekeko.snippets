(ns damp.ekeko.snippets.searchspace
  (:refer-clojure :exclude [== type])
  (:require [clojure.core.logic :as cl]) 
  (:require [damp.ekeko.snippets
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [querying :as querying]
             [operatorsrep :as operatorsrep]
             [operators :as operators]
             [precondition :as precondition]])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko])
  (:require [damp.ekeko.logic :as el]))    

  
  
  (def 
  searchspace-operators
  {:allow-relax-loop                                  operators/allow-relax-loop
   :allow-ifstatement-with-else                       operators/allow-ifstatement-with-else  
   :allow-subtype                                     operators/allow-subtype
       :relax-typeoftype                                  operators/relax-typeoftype
   :negated-node                                      operators/negated-node
	    })

  (defn searchspace-operator-ids [] (keys searchspace-operators))
  (defn searchspace-refinement-operator-ids [] 
    (keys (filter (fn [x] (= (operatorsrep/operator-category (first x)) :refinement)) searchspace-operators)))
(defn searchspace-generalization-operator-ids [] 
  (keys (filter (fn [x] (= (operatorsrep/operator-category (first x)) :generalization)) searchspace-operators)))





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
  (let [result 
        (eval (querying/snippetgroup-query new-group 'damp.ekeko/ekeko))]
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
      (operatorsrep/applicable? op-id node)
      (= (operatorsrep/operator-category op-id) result-check))
    (and 
      (operatorsrep/applicable? op-id node)
      (= result-check :any))))

;to test this, put negated-node precondition to is-simplename?
;negated-node generated queries that fails every part of selected node 
;time-consuming when execute the query

(defn dfs-snippet [group goal-positive goal-negative] 
  (let [group-nodes (snippetgroup/snippetgroup-nodes group)]
    (dfs 
      group 
      goal-positive
      goal-negative
      group-nodes
      searchspace-operators
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
  (let [group-nodes (snippetgroup/snippetgroup-nodes group)
        result-check (check-result group goal-positive goal-negative)
        operators (cond 
                    (= result-check :refinement) (searchspace-refinement-operator-ids)
                    (= result-check :generalization) (searchspace-generalization-operator-ids)
                    :else (searchspace-operator-ids))]
    (apply-operators group operators group-nodes)))

(defn
  register-callbacks 
  []
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_SEARCH) iterate-snippet))

(register-callbacks)



