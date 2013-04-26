(ns 
  ^{:doc "Conversion of snippets to Ekeko queries."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.querying
  (:require [clojure.core.logic :as cl]) 
  (:require [damp.ekeko.snippets 
             [representation :as representation]
             [operators :as operators]
             [util :as util]
             [matching :as matching] 
             ]))
  
;; Converting a snippet to a query
;; -------------------------------

(defn 
  snippet-conditions
  "Returns a list of logic conditions that will retrieve matches for the given snippet."
  [snippet]
  (defn 
    conditions
    [ast-or-list]
    (concat (((matching/make-grounding-function (representation/snippet-grounder-for-node snippet ast-or-list)) ast-or-list) snippet)
            (((matching/make-constraining-function (representation/snippet-constrainer-for-node snippet ast-or-list)) ast-or-list) snippet)))
  (let [ast (:ast snippet)
        query (atom '())]
    (util/walk-jdt-node 
      ast
      (fn [astval]  (swap! query concat (conditions astval)))
      (fn [lstval] (swap! query concat (conditions lstval)))
      (fn [primval] (swap! query concat (conditions primval)))
      (fn [nilval] (swap! query concat (conditions nilval))))
    @query))

(defn
  snippet-query-with-conditions
  [snippet ekekolaunchersymbol conditions userconditions]
  (let [root-var (representation/snippet-var-for-root snippet)
        uservars-exact (into #{} (representation/snippet-uservars-for-information snippet))
        uservars-var (into #{} (representation/snippet-uservars-for-variable snippet))
        vars (disj (into #{} (representation/snippet-vars snippet)) root-var)]
    `(~ekekolaunchersymbol 
       [~root-var ~@uservars-exact]
       (cl/fresh [~@vars ~@uservars-var]
                 ~@conditions
                 ~@userconditions))))
  
(defn
  snippet-query
  "Returns an Ekeko query that that will retrieve matches for the given snippet."
  [snippet ekekolaunchersymbol]
  (snippet-query-with-conditions 
    snippet ekekolaunchersymbol 
    (snippet-conditions snippet) 
    (representation/snippet-userqueries snippet)))
 
; Converting snippet group to query
;------------------------------------

(defn
  snippetgroup-conditions
  "Returns a list of logic conditions that will retrieve matches for the given snippet group."
  [snippetgroup]
  (representation/flat-map snippet-conditions (representation/snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-query-with-conditions
  [snippetgroup ekekolaunchersymbol conditions userconditions]
  (let [root-vars (representation/snippetgroup-rootvars snippetgroup)
        uservars-exact (into #{} (representation/snippetgroup-uservars-for-information snippetgroup))
        uservars-var (into #{} (representation/snippetgroup-uservars-for-variable snippetgroup))
        vars (into #{} (remove (set root-vars) (representation/snippetgroup-vars snippetgroup)))]
    `(~ekekolaunchersymbol 
       [~@root-vars ~@uservars-exact]
       (cl/fresh [~@vars ~@uservars-var]
                 ~@conditions
                 ~@userconditions))))

(defn
  snippetgroup-query
  "Returns an Ekeko query that that will retrieve matches for the given snippet group."
  [snippetgroup ekekolaunchersymbol]
  (snippetgroup-query-with-conditions 
    snippetgroup ekekolaunchersymbol 
    (snippetgroup-conditions snippetgroup) 
    (concat 
      (representation/snippetgroup-snippets-userqueries snippetgroup)
      (representation/snippetgroup-userqueries snippetgroup))))

    
; Converting snippet query depends on it's group
;-----------------------------------------------

(defn
  snippet-in-group-query-with-conditions
  "Query for snippet, depends on relation with other snippets in the group."
  [snippet snippetgroup ekekolaunchersymbol]
  (let [related-snippets (representation/snippetgroup-related-snippets-basedon-userqueries snippetgroup snippet)
        grp-related-snippets (update-in (representation/make-snippetgroup "") [:snippetlist] (fn [x] related-snippets))
        root-var (representation/snippet-var-for-root snippet)
        uservars-exact (into #{} (representation/snippet-uservars-for-information snippet))
        uservars-exact-grp (into #{} (representation/snippetgroup-uservars-for-information grp-related-snippets))
        uservars-var (into #{} (representation/snippet-uservars-for-variable snippet))
        uservars-var-grp (into #{} (representation/snippetgroup-uservars-for-variable grp-related-snippets))
        vars (into #{} (remove #{root-var} (representation/snippet-vars snippet)))
        vars-grp (into #{} (remove #{root-var} (representation/snippetgroup-vars grp-related-snippets)))
        conds (snippet-conditions snippet) 
        conds-grp (snippetgroup-conditions grp-related-snippets) 
        userconds (representation/snippet-userqueries snippet)
        userconds-grp (representation/snippetgroup-snippets-userqueries grp-related-snippets)]
    `(~ekekolaunchersymbol 
       [~root-var ~@uservars-exact]
       (cl/fresh [~@vars ~@vars-grp ~@uservars-exact-grp ~@uservars-var ~@uservars-var-grp]
                 ~@conds ~@conds-grp
                 ~@userconds ~@userconds-grp))))

(defn
  snippet-in-group-query
  "Returns an Ekeko query that that will retrieve matches for the given snippet depends on it's group conditions."
  [snippet snippetgroup ekekolaunchersymbol]
  (snippet-in-group-query-with-conditions 
    snippet snippetgroup ekekolaunchersymbol)) 

; tool to add query
; ------------------

(defn add-query
  [query additional-query]
  `(~@(concat 
        (butlast query)
        (list (concat
                (last query) ;;get query inside fresh and concat it with rewrite query
                additional-query))))) 

(defn add-query-on-top
  [query additional-query]
  (let [fresh-part (last query)]
  `(~@(concat 
        (butlast query)
        (list 
          (cons (first fresh-part)
                (cons (fnext fresh-part)
                      (concat additional-query
                              (rest (rest query)))))))))) 
    

