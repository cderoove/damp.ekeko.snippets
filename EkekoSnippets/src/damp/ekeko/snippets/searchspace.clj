(ns damp.ekeko.snippets.searchspace)


(defn bfs
  [init-value goal values map-operators filter-function check-function]

  (defn nothing [x y] x)
  
  (let [nmap-operators (assoc map-operators 'nothing nothing)
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
      ((operator-function (operator todo)) (prev-result todo) (value todo)))

    ;;function to generate todo for the first time
    (defn generate-todo
      [init-value list-value]
      (filter 
        (fn [todo] (filter-function (operator todo) (value todo)))
        (map (fn [op] [op init-value list-value []]) operators)))

    ;;function to generate children of todo 
    ;;children of todo are --> apply all operators with next value
    (defn children
      [todo]
      (let [curr-result (process todo)
            next-values (next-values todo)
            curr-path (if (= (operator todo) 'nothing)
                        (path todo)
                        (conj (path todo) [(operator todo) (value todo)]))]
        (if (empty? next-values)
          '() 
          (filter 
            (fn [todo] (filter-function (operator todo) (value todo)))
            (map (fn [op] [op curr-result next-values curr-path]) operators)))))

    ;;bfs algorithm
    (defn process-bfs
      [list-todo goal]
      (let [todo (first list-todo)]
        (cond 
          (empty? list-todo) (print "fail")
          (check-function (process todo) goal) (print "succeed" (conj (path todo) [(operator todo) (value todo)]))
          :else (do
                  (println (operator todo)) 
                  (println (value todo)) 
                  (process-bfs (concat (rest list-todo) (children todo)) goal)))))
    
    (process-bfs (generate-todo init-value values) goal)))

(comment
  
(def init-value 0)
(def goal (/ 105 6))
(def values '(100 0 10 5 6))
(def map-operators {'+ + '- - '* * '/ /})
(defn safe-operator? [op value]
  (not (and (= op '/) (= value 0))))

(bfs init-value goal values map-operators safe-operator? =)

(def node
  (damp.ekeko.snippets.parsing/parse-string-declaration 
    "public int myMethodF(int val) {	r = 0; if (val == 0) {	r = val;	} return r; }"))
(def snippet 
  (damp.ekeko.snippets.representation/jdt-node-as-snippet node))
(def resnippet 
  (damp.ekeko.snippets.operators/allow-ifstatement-with-else
    snippet
    (fnext (.statements (.getBody node)))))
(def strsnippet 
  (test.damp.ekeko/tuples-to-stringsetstring 
    (damp.ekeko.snippets/query-by-snippet resnippet)))

(defn result-equal 
  [snippet string]
  (= (test.damp.ekeko/tuples-to-stringsetstring 
       (damp.ekeko.snippets/query-by-snippet snippet))
     string))

(.start 
  (Thread. 
    (fn [] 
      (damp.ekeko.snippets.searchspace/bfs 
        snippet 
        strsnippet 
        (damp.ekeko.snippets.representation/snippet-nodes snippet)
        damp.ekeko.snippets.precondition/searchspace-operators
        damp.ekeko.snippets.precondition/safe-operator-for-node? 
        result-equal))))

)

