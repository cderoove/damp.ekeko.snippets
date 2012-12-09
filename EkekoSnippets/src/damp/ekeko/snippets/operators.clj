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
        snippet-with-epsilon (representation/remove-node-from-snippet snippet-with-uservar node)
        snippet-with-gf (update-groundf snippet-with-epsilon node :minimalistic)]
    (update-constrainf snippet-with-gf node :variable)))

(defn
  listrewrite-for-node
  "Return listrewrite of the given node (= list wrapper) or new listrewrite if it doesn't have."
  [snippet node]
  (let [list-rewrite (representation/snippet-usernode-for-node snippet node)]
    (if (nil? list-rewrite)
      (util/create-listrewrite node)
      list-rewrite)))
  
(defn
  update-listrewrite-for-node
  "Update or write :node2usernode for listrewrite of the given node (= list wrapper).
   Write :ast2var for new list with the same logic variable of old list."
  [snippet node new-list-rewrite]
  (let [list-rewrite (representation/snippet-usernode-for-node snippet node)
        new-snippet 
        (if (nil? list-rewrite)
          (assoc-in snippet [:node2usernode node] new-list-rewrite)
          (update-in snippet [:node2usernode node] (fn [x] new-list-rewrite)))
        new-list (util/rewritten-list-from-listrewrite new-list-rewrite)
        var-match-raw (representation/snippet-var-for-node new-snippet (:value node))]
    (if (nil? (representation/snippet-var-for-node new-snippet new-list))
      (assoc-in new-snippet [:ast2var new-list] var-match-raw)
      new-snippet)))

(defn 
  remove-node 
  "Remove a given node from snippet. Add new listrewrite to snippet node2usernode."
  [snippet node]
  (let [list-container (representation/snippet-node-with-member node)
        list-rewrite (listrewrite-for-node snippet list-container)
        new-snippet (representation/remove-node-from-snippet snippet node)]
    (util/remove-node-from-listrewrite list-rewrite node)
    (update-listrewrite-for-node new-snippet list-container list-rewrite)))

(defn 
  add-node 
  "Add a given node in given idx inside the lst in snippet."
  [snippet lst node idx]
  (let [list-container lst
        list-rewrite (listrewrite-for-node snippet list-container)
        new-snippet (representation/add-node-to-snippet snippet node)]
    (util/add-node-to-listrewrite list-rewrite node idx)
    (update-listrewrite-for-node new-snippet list-container list-rewrite)))

(defn
  contains-variable-declaration-fragments 
  "Allow given fragments in given snippet, as part of one or more fragments in target source code."
  [snippet fragments]
  (let [listcontainer (representation/snippet-node-with-member (:owner fragments))
        newsnippet    (contains-elements snippet listcontainer)]  
    (contains-elements newsnippet fragments)))

(defn
  contains-list-of-variable-declaration-fragments 
  "Allow given lst (= list of fragments) in given snippet, as part of one or more fragments in target source code."
  [snippet lst]
  (if (empty? lst)
    snippet
    (contains-list-of-variable-declaration-fragments 
      (contains-variable-declaration-fragments snippet (first lst))
      (rest lst))))
  



