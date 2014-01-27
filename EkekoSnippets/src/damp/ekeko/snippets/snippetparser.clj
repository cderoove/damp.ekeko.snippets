(ns 
  ^{:doc "Parser for snippets."
   :author "Coen De Roover"}
  damp.ekeko.snippets.snippetparser
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko.snippets 
             [parsing :as parsing]
             [representation :as representation]]))

(defn
 string-as-snippet
 [string]
 (let [extracted (damp.ekeko.snippets.SnippetExtractor/extractSnippetBounds string)
       extracted-string (.snippet extracted)
       extracted-bounds (.bounds extracted) 
       ast (parsing/parse-string-ast extracted-string)
       snippet (representation/jdt-node-as-snippet ast)]
   snippet
   
   ;todo: convert directives to grounding/constraining functions
   ;todo: convert each meta-varibale to first java syntax that parses
 
   
   ))


