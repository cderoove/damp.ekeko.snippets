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
             ])
  (:require 
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt [rewrites :as rewrites]]))


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


(defn
  registered-directive-for-name
  [name]
  (some
    (fn [directive]
      (when (= name (directives/directive-name directive))
        directive))
    (registered-directives)))
