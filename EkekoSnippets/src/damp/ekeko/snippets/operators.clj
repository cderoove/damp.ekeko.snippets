(ns 
  ^{:doc "Operators for generalizing and refining snippets."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.operators
  (:require [damp.ekeko.snippets 
             [util :as util]
             [parsing :as parsing]
             [representation :as representation]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [reification :as reification]]))
  
(defn 
  update-groundf 
  "Update grounding function of a given node in a given snippet with new grounding function of given type
   Example: (update-groundf snippet node :node-deep)."
  [snippet node type]
  (update-in snippet [:ast2groundf node] (fn [x] (list type))))

(defn 
  update-constrainf 
  "Update constraining function of a given node in a given snippet with the new constraining function of given type
   Example: (update-constrainf snippet node :list-contains)."
  [snippet node type]
  (update-in snippet [:ast2constrainf node] (fn [x] (list type))))

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
  split-variable-declaration-fragments 
  "Split variable declaration fragments into multiple node with one fragment for each statement."
  [snippet listcontainer position fragments modifiers type]
  (if (empty? fragments)
    snippet
    (let [newnode         (parsing/make-variable-declaration-statement modifiers type (first fragments))
          newsnippet-node (add-node snippet listcontainer newnode position)
          newsnippet      (contains-elements newsnippet-node (first (astnode/node-propertyvalues newnode)))]
      (split-variable-declaration-fragments newsnippet listcontainer (+ position 1) (rest fragments) modifiers type))))

(defn
  split-variable-declaration-statement 
  "Split variable declaration statement with many fragments into multiple node with one fragment for each statement."
  [snippet statement]
  (let [listcontainer   (representation/snippet-node-with-member statement)
        position        (.indexOf (representation/snippet-value-for-node snippet listcontainer) statement)
        newsnippet      (remove-node snippet statement)]  
    (split-variable-declaration-fragments
      newsnippet 
      listcontainer
      position
      (.fragments statement) 
      (.modifiers statement) 
      (.getType statement))))

(defn
  contains-variable-declaration-statement 
  "Allow given variable declaration statement in given snippet, 
   as part of one or more variable declaration statements in target source code."
  [snippet statement]
  (let [listcontainer   (representation/snippet-node-with-member statement)
        position        (.indexOf (representation/snippet-value-for-node snippet listcontainer) statement)
        newsnippet-list (contains-elements snippet listcontainer)
        newsnippet      (remove-node newsnippet-list statement)]  
    (split-variable-declaration-fragments
      newsnippet 
      listcontainer
      position
      (.fragments statement) 
      (.modifiers statement) 
      (.getType statement))))

(defn
  contains-variable-declaration-statements 
  "Allow given lst (= list of statement) in given snippet, as part of one or more statements in target source code."
  [snippet lst]
  (if (empty? lst)
    snippet
    (contains-variable-declaration-statements 
      (contains-variable-declaration-statement snippet (first lst))
      (rest lst))))
  
(defn
  allow-ifstatement-with-else 
  "Allow match given node (= ifstatement without else) with node (= ifstatement with else)."
  [snippet node]
  (let [else-node (first (astnode/node-propertyvalues node))]
    (update-constrainf snippet else-node :epsilon)))

(defn
  allow-subtype-on-variable-declaration
  "Allow match given node (= field/variable type) with same type or its' subtype."
  [snippet node]
  (update-constrainf snippet node :subtype))
  
(defn
  allow-subtype-on-class-declaration-extends
  "Allow match given node (= class declaration extends type) with same type or its' subtype."
  [snippet node]
  (update-constrainf snippet node :subtype))

(defn
  allow-variable-declaration-with-initializer
  "Allow match given node (= assignment expression) with local variable declaration with initializer."
  [snippet node]
  (update-constrainf snippet node :variable-declaration-with-initializer))

(defn
  add-logic-conditions
  "Add user logic conditions to snippet. conditions should be in quote, '((...) (...))."
  [snippet conditions]
  (let [new-conditions `(~@(representation/snippet-userqueries snippet) ~@conditions)]
    (assoc snippet :userquery new-conditions)))

(defn
  remove-logic-conditions
  "Remove user logic conditions from snippet. conditions should be in quote, '((...) (...))."
  [snippet conditions]
  (let [new-conditions (remove (set conditions) (representation/snippet-userqueries snippet))]
    (assoc snippet :userquery new-conditions)))
  
(defn 
  add-snippet
  "Add snippet to snippetgroup."
  [snippetgroup snippet]
  (let [new-snippetlist (cons snippet (representation/snippetgroup-snippetlist snippetgroup))]
    (assoc snippetgroup :snippetlist new-snippetlist)))

(defn
  add-logic-conditions-to-snippetgroup
  "Add user logic conditions to snippet group. conditions should be in quote, '((...) (...))."
  [snippetgroup conditions]
  (let [new-conditions `(~@(representation/snippetgroup-userqueries snippetgroup) ~@conditions)]
    (assoc snippetgroup :userquery new-conditions)))

(defn
  remove-logic-conditions-from-snippetgroup
  "Remove user logic conditions from snippet group. conditions should be in quote, '((...) (...))."
  [snippetgroup conditions]
  (let [new-conditions (remove (set conditions) (representation/snippetgroup-userqueries snippetgroup))]
    (assoc snippetgroup :userquery new-conditions)))
