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
   Example: (update-groundf snippet node :node-deep)."
  [snippet node type]
  (update-in snippet [:ast2groundf node] (fn [x] type)))

(defn 
  update-constrainf 
  "Update constraining function of a given node in a given snippet with the new constraining function of given type
   Example: (update-constrainf snippet node :list-contains)."
  [snippet node type]
  (update-in snippet [:ast2constrainf node] (fn [x] type)))

(defn
  contains-elements-with-same-size 
  "Contains all elements in a given node (list), and list has to be the same size."
  [snippet node]
  (update-constrainf snippet node :list-contains-with-same-size))

(defn
  contains-elements
  "Contains all elements in a given node (list), and list does not have to be the same size."
  [snippet node]
  (update-constrainf snippet node :list-contains))

(defn
  contains-elements-with-relative-order 
  "Contains all elements in a given node (list), with relative order."
  [snippet node]
  (update-constrainf snippet node :list-contains-with-relative-order))

(defn
  contains-elements-with-repetition 
  "Contains all elements in a given node (list), with repetition."
  [snippet node]
  (update-constrainf snippet node :list-contains-with-repetition))

(declare clear-cf-for-node)

(defn 
  introduce-logic-variable-of-node-exact 
  "Introduce logic variable to a given node, without removing any condition."
  [snippet node uservar]
  (let [snippet-with-uservar (assoc-in snippet [:var2uservar (representation/snippet-var-for-node snippet node)] uservar)]
    (update-constrainf snippet-with-uservar node :exact-with-variable)))

(defn 
  introduce-logic-variable 
  "Introduce logic variable to a given node."
  [snippet node uservar]
  (let [snippet-with-uservar (assoc-in snippet [:var2uservar (representation/snippet-var-for-node snippet node)] uservar)
        snippet-with-epsilon (clear-cf-for-node snippet-with-uservar node)
        snippet-with-gf (update-groundf snippet-with-epsilon node :minimalistic)]
    (update-constrainf snippet-with-gf node :variable)))

(defn 
  clear-cf-for-node
  "Clear constraining function for all child of a given node in snippet."
  [snippet node]
  (defn update-snippet-value [snippet value]
    (update-in snippet [:ast2groundf value] (fn [x] :epsilon))
    (update-in snippet [:ast2constrainf value] (fn [x] :epsilon)))
  (let [snippet (atom snippet)]
    (util/walk-jdt-node 
      node
      (fn [astval] (swap! snippet update-snippet-value astval))
      (fn [lstval] (swap! snippet update-snippet-value lstval))
      (fn [primval]  (swap! snippet update-snippet-value primval))
      (fn [nilval] (swap! snippet update-snippet-value nilval)))
    @snippet))


