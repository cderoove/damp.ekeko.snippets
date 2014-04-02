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
             [runtime :as runtime]])
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
        replacement-vars|symbols
        (map symbol replacement-vars|strings)
        ]
    `((el/equals ~var-match 
                 (runtime/template-to-string|projected
                    ;this might cause problems because template cannot be converted to a clojure value!
                    ;workaround: generate string of template with placeholders for vars (siltvani might have done something similar)
                    [~@replacement-vars|strings]
                    [~replacement-vars|symbols])))))
    

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