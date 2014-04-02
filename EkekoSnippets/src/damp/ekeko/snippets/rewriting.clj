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
    `((cl/fresh [~runtime-template-var] 
                (cl/== ~runtime-template-var
                           (persistence/snippet-from-persistent-string ~stemplate))
                (cl/project [~runtime-template-var ~@replacement-vars|symbols]
                            (cl/== ~var-match 
                                   (parsing/parse-string-ast
                                     (runtime/template-to-string|projected 
                                       ~runtime-template-var
                                       [~@replacement-vars|quotedstrings]
                                       [~@replacement-vars|symbols])
                                     )))
                ))))

(defn
  rewrite-replace
  [root-ast replacement-var-string]
  (fn [snippet]
    (let [var-generatedcode (snippet/snippet-var-for-node snippet root-ast)
          var (symbol replacement-var-string)]
      `((el/perform 
          (rewrites/replace-node ~var ~var-generatedcode)
          )))))


(def
  directive-replace
  (directives/make-directive
    "replace"
    [(directives/make-directiveoperand "Replacement")]
    rewrite-replace 
    "Replaces its operand by the template."))


(def 
  directives-rewriting
  [directive-replace])


(defn 
  registered-directives
  []
  directives-rewriting)


(defn
  registered-rewriting-directive?
  [directive]
  (let [name (directives/directive-name directive)]
    (some #{name}
          (map directives/directive-name directives-rewriting))))