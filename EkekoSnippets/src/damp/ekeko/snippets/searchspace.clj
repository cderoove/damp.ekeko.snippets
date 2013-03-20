(ns damp.ekeko.snippets.searchspace)


(defn dfs
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

    ;;dfs algorithm
    (defn process-dfs
      [list-todo goal]
      (let [todo (first list-todo)]
        (cond 
          (empty? list-todo) (println "fail")
          (check-function (process todo) goal) (println "succeed" (conj (path todo) [(operator todo) (value todo)]))
          :else (do
                  (println (operator todo)) 
                  (println (value todo)) 
                  (process-dfs (concat (children todo) (rest list-todo)) goal)))))
    
    (process-dfs (generate-todo init-value values) goal)))

