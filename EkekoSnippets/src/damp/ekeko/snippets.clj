(ns 
  ^{:doc "Snippet-driven querying of Java projects."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets
  (:refer-clojure :exclude [== type])
  (:require [clojure.core.logic :as cl]) 
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [representation :as representation]
             [matching :as matching]
             [parsing :as parsing]
             [util :as util]])
  (:require [damp.ekeko.jdt [astnode :as astnode]])
  (:require [damp.ekeko])
  (:require [damp.ekeko.logic :as el]))    
    
(defn
  query-by-snippet*
  "Queries the Ekeko projects for matches for the given snippet. Opens Eclipse view on results."
  [snippet]
  (eval (querying/snippet-query snippet 'damp.ekeko/ekeko*)))

(defn
  query-by-snippet
  "Queries the Ekeko projects for matches for the given snippet."
  [snippet]
  (eval (querying/snippet-query snippet 'damp.ekeko/ekeko)))

      
(defn 
  query-by-snippet-incrementally*
  [snippet]
  (loop [conditions (querying/snippet-conditions snippet)]
    (when (seq conditions)
      (eval (querying/snippet-query-with-conditions snippet 'damp.ekeko/ekeko* conditions))
      (recur (butlast conditions)))))

(comment 
  (use 'damp.ekeko.snippets)
  (in-ns 'damp.ekeko.snippets)
  
  ;;Example REPL session against JHotDraw51
  
  
  ;;Example 1: snippet linked to program it originates from
  ;;-------------------------------------------------------
  
  ;; select an AST node from the program as the starting point for the snippet
  (def selected (first (first 
                          (damp.ekeko/ekeko [?m] 
                                            (ast :MethodDeclaration ?m) 
                                            (fresh [?n]
                                                   (has :name ?m ?n)
                                                   (has :identifier ?n "LocatorHandle"))))))
  
  ;; create a snippet from the selected AST node 
  (def snippet (jdt-node-as-snippet selected))
  
  ;; convert the snippet to an Ekeko query
  (def query (snippet-query snippet))
  
  ;;Example 2: snippet originating from a string 
  ;;--------------------------------------------
 
  (snippet-query (jdt-node-as-snippet (parsing/parse-string-expression "fLocator.locate(owner())")))
    
  
  ;;Example 3: introduce logic variable 
  ;;--------------------------------------------

  (def astnode (parsing/parse-string-statement "return foo;"))
  (def snippet (jdt-node-as-snippet astnode))
  (def query (snippet-query snippet))
  
  ;(damp.ekeko.jdt.reification/ast :ReturnStatement ?ReturnStatement14017) --> still double
  ;(damp.ekeko.jdt.reification/ast :ReturnStatement ?ReturnStatement14017) 
  ;(damp.ekeko.jdt.reification/has :expression ?ReturnStatement14017 ?SimpleName14018) 
  ;(damp.ekeko.jdt.reification/ast :SimpleName ?SimpleName14018) 
  ;(damp.ekeko.jdt.reification/has :identifier ?SimpleName14018 "foo")
  
  (def snippet2 (introduce-logic-variable snippet (snippet-node-for-var snippet '?SimpleName14018) '?vfoo))
  (def query2 (snippet-query snippet2))
  ;(damp.ekeko.jdt.reification/has :identifier ?SimpleName14018 ?lvar)

  
  ;;Example 4: ignore elements sequence of the list
  ;;--------------------------------------------------------------------------------

  (def astnode (parsing/parse-string-statement "{int y; int x;}"))
  (def snippet (jdt-node-as-snippet astnode))
  (def query (snippet-query snippet))
  ;....
  ;(damp.ekeko.jdt.reification/has :statements ?Block14974 ?List14975) 
  ;(damp.ekeko.logic/equals 2 (.size ?List14975)) 
  ;(damp.ekeko.logic/equals ?VariableDeclarationStatement14976 (clojure.core/get ?List14975 0)) 
  ;(damp.ekeko.logic/equals ?VariableDeclarationStatement14982 (clojure.core/get ?List14975 1))
  ;.... 
  
  (def snippet2 (ignore-elements-sequence snippet (snippet-node-for-var snippet '?List14975)))  
  (def query2 (snippet-query snippet2))
  ;....
  ;(damp.ekeko.jdt.reification/has :statements ?Block14974 ?List14975) 
  ;(damp.ekeko.logic/equals 2 (.size ?List14975)) 
  ;(damp.ekeko.logic/contains ?List14975 ?VariableDeclarationStatement14976) 
  ;(damp.ekeko.logic/contains ?List14975 ?VariableDeclarationStatement14982)  
  ;....


  ;;Misc Examples
  ;;-------------
  
  
  (def s (jdt-node-as-snippet(parsing/parse-string-expression "x.m()")))             ;ok
  (def s (jdt-node-as-snippet(parsing/parse-string-statement "this.methodC();")))    ;ok
  (def s (jdt-node-as-snippet(parsing/parse-string-expression "o.f")))               ;ok
  (def s (jdt-node-as-snippet(parsing/parse-string-statement "o.f = x.m();")))       ;not ok
  
  (query-by-snippet s)
  
  (view-snippet s)

  
)
