(ns 
  damp.ekeko.snippets.rewrites
  ^{:doc "Functional interface to the Eclipse rewriting API.
          !!! This file is a copy of damp.ekeko.jdt.rewrites, with Ekeko/X-specific modifications to support nested transformations."
    :author "Coen De Roover, Reinout Stevens, Siltvani, Tim Molderez"}
(:import 
  [damp.ekeko JavaProjectModel]
  [org.eclipse.jface.text Document]
  [org.eclipse.text.edits TextEdit]
  [org.eclipse.jdt.core ICompilationUnit IJavaProject]
  [org.eclipse.jdt.core.dom BodyDeclaration Expression Statement ASTNode ASTParser AST CompilationUnit SimpleName ASTNode$NodeList]
  [org.eclipse.jdt.core.dom.rewrite ASTRewrite])
  (:require [damp.ekeko.jdt
             [astnode :as astnode]]
            [damp.ekeko.snippets
             [snippet :as snippet]
             [directives :as directives]]))

(declare reset-rewrite-root-map!)

(defn-
  make-rewrite-for-cu
  [cu]
  (ASTRewrite/create (.getAST cu)))

(def current-rewrites (atom {}))

(defn 
  reset-rewrites!
  []
  (reset! current-rewrites {}))

(defn
  current-rewrite-for-cu
  "Get the ASTRewrite instance for a compilation unit, containing all transformations that should occur on that unit"
  [cu]
  (if-let [rw (get @current-rewrites cu)]
    rw
    (let [nrw (make-rewrite-for-cu cu)]
      (swap! current-rewrites assoc cu nrw)
      nrw))) 

(defn- 
  apply-rewrite-to-node
  "Perform the rewrites related to node (a CompilationUnit)"
  [rw node]
  (JavaProjectModel/applyRewriteToNode rw node))

(defn- 
  apply-rewrites
  []
  (doseq [[cu rw] @current-rewrites]
    (apply-rewrite-to-node rw cu)))

(defn 
  apply-and-reset-rewrites
  []
  (do
    (apply-rewrites)
    (reset-rewrites!)
    (reset-rewrite-root-map!)))

(declare copy-astnode)

(defn-
  compatible
  "Create a copy (if needed) of astnodeorvalue such that it belongs to ast
   (This is needed when rewriting that ast.)"
  [ast astnodeorvalue]
  (cond 
    (instance? java.util.List astnodeorvalue)
    (map (partial compatible ast) astnodeorvalue)
    (astnode/ast? astnodeorvalue)
    (if
      (= ast (.getAST astnodeorvalue))
      astnodeorvalue
      (copy-astnode ast astnodeorvalue))
    :default
    astnodeorvalue))


"This map is necessary to support nested transformations, i.e. in cases where one RHS transforms another RHS.
 It maps an RHS tmplate's root to a compatible deep copy that must be used for rewriting.
 (Should be fine if multiple transformations happen in parellel.)"
(def rewrite-root-map (atom {}))

(defn reset-rewrite-root-map! []
  (swap! rewrite-root-map (fn [x] {})))

(defn determine-rewrite-cu 
  "Determine the CompilationUnit to be rewritten, based on the value of cu-var.. which must a node in that CU"
  [cu-var]
  (cond 
    (astnode/lstvalue? cu-var)
    (.getRoot (astnode/owner cu-var))
;    (.getRoot (first (astnode/value-unwrapped cu-var)))
    
    (astnode/ast? cu-var)
    (.getRoot cu-var)
    :else
    (.getRoot (astnode/value-unwrapped cu-var))))

(defn compatible-via-rewrite-map
  "Find a compatible node via the rewrite-root-map"
  [node]
  (if (instance? CompilationUnit (astnode/root node))
    ; If we're already in the right AST
    node
    ; Otherwise, go find the same node in the right AST using the rewrite-root-map
    (let [path (astnode/path-from-root node)
          corresponding-root (@rewrite-root-map (.getRoot node))]
      (astnode/node-from-path corresponding-root path))))

;;Operations

(defn
  remove-node
  "Remove a node from a list given its index."
  ([cu-var parent propertykey idx]
    (let [cu (determine-rewrite-cu cu-var)
          rewrite (current-rewrite-for-cu cu)]
      (remove-node rewrite cu-var parent propertykey idx)))
  ([rewrite cu-var parent propertykey idx]
    (let [property (astnode/node-property-descriptor-for-ekeko-keyword parent propertykey)
          parent-in-ctxt (compatible-via-rewrite-map parent)
          list-rewrite (.getListRewrite rewrite parent-in-ctxt property)
          compatible-removenode (nth (.getStructuralProperty parent property) idx)]
      (.remove list-rewrite compatible-removenode nil))))

(defn
  remove-node-alt
  "Remove a given node from a list."
  ([cu-var parent propertykey removenode]
    (let [cu (determine-rewrite-cu cu-var)
          rewrite (current-rewrite-for-cu cu)]
      (remove-node rewrite cu-var parent propertykey removenode)))
  ([rewrite cu-var parent propertykey removenode]
    (let [property (astnode/node-property-descriptor-for-ekeko-keyword parent propertykey)
          parent-in-ctxt (compatible-via-rewrite-map parent)
          list-rewrite (.getListRewrite rewrite parent-in-ctxt property)
          compatible-removenode (some (fn [child] 
                                        ; If there are multiple equivalent children, the first one will be removed!
                                        (if (= (.toString child) (.toString removenode))
                                          child))
                                      (.getStructuralProperty parent property))]
      (.remove list-rewrite compatible-removenode nil))))

(defn 
  add-node 
  "Add newnode (or clone of newnode when ASTs are incompatible) to propertyList of the given parent at idx position."
  ([cu-var parent propertykey newnode idx] 
    (let [cu (determine-rewrite-cu cu-var)
          rewrite (current-rewrite-for-cu cu)]
      (add-node rewrite cu-var parent propertykey newnode idx)))
  ([rewrite cu-var parent propertykey newnode idx]
    (let [value (compatible (.getAST rewrite) newnode)
          x (swap! rewrite-root-map (fn [x] (assoc x newnode value)))
          property (astnode/node-property-descriptor-for-ekeko-keyword parent propertykey)
          parent-in-ctxt (compatible-via-rewrite-map parent)
          list-rewrite 
          (.getListRewrite rewrite parent-in-ctxt property)
          index 
          (if (instance? java.lang.String idx)
            (Integer/parseInt idx)
            idx)] 
      (.insertAt list-rewrite value index nil))))

(defn
  add-element
  "Add newnode to the given list at position idx."
  ([rewrite cu-var lst newnode idx]
    (let [owner (astnode/owner lst)
          ownerproperty (astnode/owner-property lst)]
      (add-node rewrite cu-var owner (astnode/ekeko-keyword-for-property-descriptor ownerproperty) newnode idx)))
  ([cu-var lst newnode idx]
(let [owner (astnode/owner lst)
      ownerproperty (astnode/owner-property lst)]
      (add-node cu-var owner (astnode/ekeko-keyword-for-property-descriptor ownerproperty) newnode idx))
    ))

(defn
  remove-element
  "Remove the element at index idx from the given list."
  ([rewrite cu-var lst idx]
    (let [owner (astnode/owner lst)
          ownerproperty (astnode/owner-property lst)]
      (remove-node rewrite cu-var owner (astnode/ekeko-keyword-for-property-descriptor ownerproperty) idx)))
  ([cu-var lst idx]
    (let [owner (astnode/owner lst)
          ownerproperty (astnode/owner-property lst)]
      (remove-node cu-var owner (astnode/ekeko-keyword-for-property-descriptor ownerproperty) idx))))

(defn
  remove-element-alt
  "Remove removenode from the given list."
  ([rewrite cu-var lst removenode]
    (let [owner (astnode/owner lst)
          ownerproperty (astnode/owner-property lst)]
      (remove-node-alt rewrite cu-var owner (astnode/ekeko-keyword-for-property-descriptor ownerproperty) removenode)))
  ([cu-var lst removenode]
    (let [owner (astnode/owner lst)
          ownerproperty (astnode/owner-property lst)]
      (remove-node-alt cu-var owner (astnode/ekeko-keyword-for-property-descriptor ownerproperty) removenode))
    ))

(defn
  move-element
  "Move source-elem from its list into target-list at idx."
  [src-cu-var tgt-cu-var source-elem target-list idx]
  (let [src-parent (astnode/owner source-elem)
        src-property (astnode/ekeko-keyword-for-property-descriptor (astnode/owner-property source-elem))
        target-parent (astnode/owner target-list)
        target-property (astnode/ekeko-keyword-for-property-descriptor (astnode/owner-property target-list))]
    (do
      (remove-node src-cu-var src-parent src-property source-elem)
      (add-node tgt-cu-var target-parent target-property source-elem idx))))

(defn
  copy-node
  "Copy source-elem (only the node itself) to target-list at idx."
  [src-cu-var tgt-cu-var source-elem target-list idx]
  (let [cu (determine-rewrite-cu tgt-cu-var)
        rewrite (current-rewrite-for-cu cu)
        
        newnode source-elem
        parent (astnode/owner target-list)
        propertykey (astnode/ekeko-keyword-for-property-descriptor (astnode/owner-property target-list))]
    
    (let [value (compatible (.getAST rewrite) newnode)
          minimized-value (astnode/minimize-node value)
          
          x (swap! rewrite-root-map (fn [x] (assoc x newnode value)))
          property (astnode/node-property-descriptor-for-ekeko-keyword parent propertykey)
          parent-in-ctxt (compatible-via-rewrite-map parent)
          list-rewrite 
          (.getListRewrite rewrite parent-in-ctxt property)
          index 
          (if (instance? java.lang.String idx)
            (Integer/parseInt idx)
            idx)] 
      (.insertAt list-rewrite minimized-value index nil))
    
    
    ))

(defn 
  replace-node 
  "Replace node with newnode (if ASTs are compatible) or clone of newnode (when ASTs are incompatible). "
  ([rewrite cu-var node newnode]
    (let [value (compatible (.getAST rewrite) newnode)]
      (swap! rewrite-root-map (fn [x] (assoc x newnode value)))
      (.replace rewrite (compatible-via-rewrite-map node) value nil)))
  ([cu-var node newnode]
    (let [cu (determine-rewrite-cu cu-var)
          rewrite (current-rewrite-for-cu cu)]
      (replace-node rewrite cu-var node newnode))))

(def 
  ast-for-newlycreatednodes
  (AST/newAST JavaProjectModel/JLS))

;todo: fix duplication

(defn-
  assignproperties
  [instance  propertykey2value]
  (doseq [[key value] propertykey2value]
      (let [desc (astnode/node-property-descriptor-for-ekeko-keyword instance key)
            val (compatible (.getAST instance) value)]
        (if 
          (astnode/property-descriptor-list? desc)
          (let [lstval (.getStructuralProperty instance desc)]
            (.addAll lstval val))
          (.setStructuralProperty instance desc val)))))

(defn
  newast-for-rewrite
  "Creates a new node of the given ekeko keyword (and optional property keyword to value pairs)."
  [rewrite key & {:as propertykey2value}]
  (let [clazz (astnode/class-for-ekeko-keyword key)
        instance (.createInstance (.getAST rewrite) clazz)]
    (assignproperties instance propertykey2value)
    instance))

(defn 
  newast
  "Creates a new node of the given ekeko keyword (and optional property keyword to value pairs)."
  [key & {:as propertykey2value}]
  (let [clazz (astnode/class-for-ekeko-keyword key)
        instance (.createInstance ast-for-newlycreatednodes clazz)]
      (assignproperties instance propertykey2value)
      instance))
    
    
(defn 
  change-property-node
  "Change property of node."
  ([rewrite node propertykey value]
    (let [property (astnode/node-property-descriptor-for-ekeko-keyword node propertykey)] 
      (.set rewrite node property value nil)))
  ([node propertykey value]
    (let [cu (.getRoot node)
          rewrite (current-rewrite-for-cu cu)]
      (change-property-node rewrite node propertykey value))))

(defn
  replace-value
  "Replaces a non-ASTNode primitive value by the given value."
  ([value newvalue]
    (let [owner (astnode/owner value)
          cu (.getRoot owner)
          rewrite (current-rewrite-for-cu cu)]
      (replace-value rewrite value newvalue)))
  ([rewrite value newvalue]
    (let [owner (astnode/owner value)
          ownerproperty (astnode/owner-property value)
          ekekopropertykeyword (astnode/ekeko-keyword-for-property-descriptor ownerproperty)]
      (change-property-node rewrite owner ekekopropertykeyword newvalue))))


(defn 
  copy-astnode 
  ([ast astnode]
    (ASTNode/copySubtree ast astnode))
  ([astnode]
    (copy-astnode (.getAST astnode) astnode)))

(defn 
  create-parameterized-type [type]
  (.newParameterizedType (.getAST type) type))

