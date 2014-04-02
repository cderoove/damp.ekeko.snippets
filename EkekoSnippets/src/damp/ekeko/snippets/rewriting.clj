(ns 
  ^{:doc "Rewriting directives for template-based program transformations."
    :author "Coen De Roover"}
  damp.ekeko.snippets.rewriting
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko.snippets 
             [matching :as matching]
             [snippet :as snippet]
             [directives :as directives]
             [util :as util]
             [parsing :as parsing]
             [runtime :as runtime]
             [persistence :as persistence]
             
             ])
  (:require 
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt [rewrites :as rewrites]]))


(defn
  newnode-from-template
  [template]
  (let [root
        (snippet/snippet-root template)
        
        var-match 
        (snippet/snippet-var-for-node template root)
        
        replacement-vars|strings
        (matching/snippet-replacement-vars template) 
        
        replacement-vars|quotedstrings
        (map matching/to-literal-string replacement-vars|strings)
        
        replacement-vars|symbols
        (map symbol replacement-vars|strings)
        stemplate
        (persistence/snippet-as-persistent-string template)
        runtime-template-var  
        (util/gen-readable-lvar-for-value root)
        ]
    `((cl/fresh [~runtime-template-var ~var-match] 
                (cl/== ~runtime-template-var
                           (persistence/snippet-from-persistent-string ~stemplate))
                
                
                (cl/project [~runtime-template-var ~@replacement-vars|symbols]
                            (cl/== ~var-match 
                                   (runtime/template-to-string|projected 
                                     ~runtime-template-var
                                     [~@replacement-vars|quotedstrings]
                                     [~@replacement-vars|symbols]
                )))
                
                ))))

(defn
  rewrite-replace
  [template]
  (fn [template-ast]))


(def
  directive-replace
  (directives/make-directive
    "replace"
    []
    rewrite-replace 
    "Replace the operand the template."))


(def 
  directives-rewriting
  [directive-replace])


(defn 
  registered-directives
  []
  directives-rewriting)