	(ns 
	  ^{:doc "Conversion of snippets to Ekeko queries."
	    :author "Coen De Roover, Siltvani"}
	  damp.ekeko.snippets.querying
	  (:require [clojure.core.logic :as cl]) 
	  (:require [damp.ekeko.snippets 
	             [snippet :as snippet]
               [snippetgroup :as snippetgroup]
	             [operators :as operators]
	             [util :as util]
	             [matching :as matching] 
	             [gui :as gui] 
	             [rewrite :as rewrite]]) 
   (:require 
     [damp.ekeko [logic :as el]]))
	  
	;; Converting a snippet to a query
	;; -------------------------------
	
	(defn 
	  snippet-conditions
	  "Returns a list of logic conditions that will retrieve matches for the given snippet."
	  [snippet]
	  (defn 
	    conditions
	    [ast-or-list]
	    (concat (((matching/make-grounding-function (snippet/snippet-grounder-for-node snippet ast-or-list)) ast-or-list) snippet)
	            (((matching/make-constraining-function (snippet/snippet-constrainer-for-node snippet ast-or-list)) ast-or-list) snippet)))
	  (let [ast (:ast snippet)
	        query (atom '())]
	    (util/walk-jdt-node 
	      ast
	      (fn [astval]  (swap! query concat (conditions astval)))
	      (fn [lstval] (swap! query concat (conditions lstval)))
	      (fn [primval] (swap! query concat (conditions primval)))
	      (fn [nilval] (swap! query concat (conditions nilval))))
	    (filter (fn[q] (not (nil? q))) @query)))
	
	(defn
	  snippet-query-with-conditions
	  [snippet ekekolaunchersymbol conditions userconditions]
	  (let [root-var (snippet/snippet-var-for-root snippet)
	        uservars-exact (into #{} (snippet/snippet-uservars-for-information snippet))
	        uservars-var (into #{} (snippet/snippet-uservars-for-variable snippet))
	        vars (disj (into #{} (snippet/snippet-vars snippet)) root-var)]
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
	    (snippet/snippet-userqueries snippet)))
	 
	; Converting snippet group to query
	;------------------------------------
	
	(defn
	  snippet-query-for-userfs
	  [snippet]
	  "Returns all user functions of the given snippet.
	  ((function var-match var-arg) ...)."
   (defn userfs-to-query [var-match userfs] 
     (map 
       (fn [userf] 
         (let [function (symbol (str "damp.ekeko.snippets.public/" (first userf)))
               var-arg (symbol (fnext userf))]
           (println function " " var-match " " var-arg)
           `(~function ~var-match ~var-arg)))
       userfs))
	  (snippetgroup/flat-map 
	    (fn [ast2userfs] 
	      (let [ast (key ast2userfs)
	            var-match (snippet/snippet-var-for-node snippet ast)
	            userfs (val ast2userfs)]
	        (userfs-to-query var-match userfs)))
	    (:ast2userfs snippet)))
	
	(defn
	  snippetgroup-query-for-userfs
	  [grp]
	  "Returns all user functions of the given grp.
	  ((function var-match var-arg) ...)."
	  (snippetgroup/flat-map (fn [s] (snippet-query-for-userfs s)) (:snippetlist grp)))
	
	(defn
	  snippetgroup-conditions
	  "Returns a list of logic conditions that will retrieve matches for the given snippet group."
	  [snippetgroup]
	  (snippetgroup/flat-map snippet-conditions (snippetgroup/snippetgroup-snippetlist snippetgroup)))
	
	(defn
	  snippetgroup-query-with-conditions
	  [snippetgroup ekekolaunchersymbol conditions userconditions]
	  (let [root-vars (snippetgroup/snippetgroup-rootvars snippetgroup)
	        uservars-exact (into #{} (snippetgroup/snippetgroup-uservars-for-information snippetgroup))
	        uservars-var (into #{} (snippetgroup/snippetgroup-uservars-for-variable snippetgroup))
	        vars (into #{} (remove (set root-vars) (snippetgroup/snippetgroup-vars snippetgroup)))]
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
	      (snippetgroup/snippetgroup-snippets-userqueries snippetgroup)
	      (snippetgroup/snippetgroup-userquery snippetgroup)
	      (snippetgroup-query-for-userfs snippetgroup))))
	
	    
	; Converting snippet group to rewrite query
	;------------------------------------------
	
	(defn
	  snippet-rewrite-query-for-userfs
	  [snippet]
	  "Returns all user rewrite functions of the given snippet.
	  ((function var-match string) ...)."
   (defn userfs-to-query [userfs node-str user-vars] 
     (map 
       (fn [userf] 
         (let [function (symbol (str "damp.ekeko.snippets.public/" (first userf)))
               var-match (symbol (fnext userf))]
           (println function " " var-match " " node-str " " user-vars)
           `(el/perform (~function ~var-match (rewrite/snippet-rewrite-string ~node-str [~@user-vars])))))
       userfs))
   (let [user-vars (rewrite/snippet-rewrite-uservar-pairs snippet)]
     (snippetgroup/flat-map 
       (fn [ast2userfs] 
         (let [ast (key ast2userfs)
               userfs (val ast2userfs)
               node-str (.replace (gui/print-plain-node snippet ast) "?" "*")]
           (userfs-to-query userfs node-str user-vars)))
       (:ast2userfs snippet))))
	
	(defn
	  snippetgroup-rewrite-query-for-userfs
	  [grp]
	  "Returns all user rewrite functions of the given grp.
	  ((function var-match string) ...)."
	  (snippetgroup/flat-map (fn [s] (snippet-rewrite-query-for-userfs s)) (:snippetlist grp)))
	
	(defn
	  snippetgroup-rewrite-query
	  "Returns an Ekeko rewrite query that will rewrite nodes for the given snippet group."
	  [snippetgroup snippetgrouprewrite ekekolaunchersymbol]
	  (snippetgroup-query-with-conditions 
	    snippetgroup ekekolaunchersymbol 
	    (snippetgroup-conditions snippetgroup) 
	    (concat 
	      (snippetgroup/snippetgroup-snippets-userqueries snippetgroup)
	      (snippetgroup/snippetgroup-userquery snippetgroup)
	      (snippetgroup-query-for-userfs snippetgroup)
        (snippetgroup-rewrite-query-for-userfs snippetgrouprewrite))))
	


	; Converting snippet query depends on it's group
	;-----------------------------------------------
	
	(defn
	  snippet-in-group-query-with-conditions
	  "Query for snippet, depends on relation with other snippets in the group."
	  [snippet snippetgroup ekekolaunchersymbol]
	  (let [related-snippets (snippetgroup/snippetgroup-related-snippets-basedon-mandatory-and-userqueries snippetgroup snippet)
	        grp-related-snippets (update-in (snippetgroup/make-snippetgroup "") [:snippetlist] (fn [x] related-snippets))
	        root-var (snippet/snippet-var-for-root snippet)
	        uservars-exact (into #{} (snippet/snippet-uservars-for-information snippet))
	        uservars-exact-grp (into #{} (snippetgroup/snippetgroup-uservars-for-information grp-related-snippets))
	        uservars-var (into #{} (snippet/snippet-uservars-for-variable snippet))
	        uservars-var-grp (into #{} (snippetgroup/snippetgroup-uservars-for-variable grp-related-snippets))
	        vars (into #{} (remove #{root-var} (snippet/snippet-vars snippet)))
	        vars-grp (into #{} (remove #{root-var} (snippetgroup/snippetgroup-vars grp-related-snippets)))
	        conds (snippet-conditions snippet) 
	        conds-grp (snippetgroup-conditions grp-related-snippets) 
	        userconds (snippet/snippet-userqueries snippet)
	        userconds-grp (snippetgroup/snippetgroup-snippets-userqueries grp-related-snippets)]
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
	    
	
