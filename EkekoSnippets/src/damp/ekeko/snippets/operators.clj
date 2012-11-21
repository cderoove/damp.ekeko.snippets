(ns 
  ^{:doc "Operators for generalizing and refining snippets."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.operators
  (:require [damp.ekeko.snippets 
             [util :as util]
             [representation :as representation]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [reification :as reification]]))
  
(defn 
  update-groundf 
  "Update grounding function of a given node in a given snippet with new grounding function of given type
   Example: (update-groundf snippet node :node-deep)"
  [snippet node type]
  (update-in snippet [:ast2groundf node] (fn [x] type)))

(defn 
  update-constrainf 
  "Update constraining function of a given node in a given snippet with the new constraining function of given type
   Example: (update-constrainf snippet node :list-contains)"
  [snippet node type]
  (update-in snippet [:ast2constrainf node] (fn [x] type)))

(defn
  ignore-elements-sequence 
  "Ignore elements sequence in a given node(list)"
  [snippet node]
  (update-constrainf snippet node :list-contains))

(defn 
  introduce-logic-variable 
  "Introduce logic variable to a given node"
  [snippet node uservar]
  (let [newsnippet (assoc-in snippet [:var2uservar (representation/snippet-var-for-node snippet node)] uservar)]
    (update-constrainf newsnippet node :variable)))



