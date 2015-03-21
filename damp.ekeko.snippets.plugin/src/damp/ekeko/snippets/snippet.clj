(ns 
  ^{:doc "Core functionality related to the Snippet datatype used in snippet-driven queries."
    :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.snippet
  (:require [damp.ekeko.snippets 
             [util :as util]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]])
  (:import 
    [damp.ekeko.jdt.astnode RelativeListElementIdentifier RelativePropertyValueIdentifier]
    [java.io Writer] 
    [java.util List]
    [org.eclipse.jdt.core.dom.rewrite ASTRewrite]
    [org.eclipse.jdt.core.dom ASTNode ASTNode$NodeList CompilationUnit]))


;; Snippet Datatype
;; ----------------

; Datatype representing a code snippet that can be matched against Java projects.

; For each AST node of the code snippet, a matching AST node has to be found in the Java project.
; To this end, an Ekeko query is generated that can be launched against the project. 
; The logic variables in the query correspond to invididual AST nodes of the snippet. 
; They will be bound to matching AST nodes in the project. 
; The actual conditions that are generated for the query depend on how each AST node of the snippet
; is to be matched (e.g., more lenient, or rather strict).

; members of the Snippet datatype:
; - ast: complete AST for the original code snippet
; - ast2var: map from an AST node of the snippet to the logic variable that is to be bound to its match
; - var2ast: map from a logic variable to the an AST node which is bound to its match
; - ast2bounddirectives: map from AST node to its matching/rewriting directives
; - ast2meta: map from AST node to map with misc. information about that node 
; - userquery: TODO

(defrecord 
  Snippet
  [ast ast2var ast2bounddirectives var2ast ast2meta userquery anchor]
  clojure.core.logic.protocols/IUninitialized ;otherwise cannot be bound to logic var
  (-uninitialized [_]
    (Snippet. 
      nil nil nil nil nil nil nil)))


(defn
  make-snippet
  "For internal use only. 
   Consider matching/snippet-from-string or matching/snippet-from-node instead."
  [node]
  (damp.ekeko.snippets.snippet.Snippet. node {} {} {} {} '() nil))

(defn 
  snippet-root 
  [snippet]
  (:ast snippet))

(defn
  snippet-anchor
  "Returns the anchor of the snippet's root (i.e., its unique identifier in the workspace it originates from)."
  [snippet]
  (:anchor snippet))


(defn
  snippet-anchor|resolved
  [snippet]
  (if-let [anchor (snippet-anchor snippet)]
    (astnode/corresponding-project-value anchor)))

(defn 
  snippet-var-for-node 
  "For the given AST node of the given snippet, returns the name of the logic
   variable that will be bound to a matching AST node from the Java project."
  [snippet snippet-node]
  (get-in snippet [:ast2var snippet-node]))

(defn
  snippet-var-for-root
  "Returns the logic variable associated with the root of the snippet
   (i.e., the JDT node the snippet originated from)."
  [snippet]
  (snippet-var-for-node snippet (:ast snippet)))

(defn 
  snippet-node-for-var 
  "For the logic variable of the given snippet, returns the AST node  
   that is bound to a matching logic variable."
  [snippet snippet-var]
  (get-in snippet [:var2ast snippet-var]))


(defn
  snippet-bounddirectives-for-node
  "For the given AST node of the given snippet, returns the seq of bound directives used to generate conditions to find a match for the node."
  [snippet snippet-node]
  (get-in snippet [:ast2bounddirectives snippet-node]))

(defn
  snippet-bounddirectives
  "Returns all bounddirectives for all nodes of the snippet."
  [snippet]
  (vals (:ast2bounddirectives snippet)))

(defn
  snippet-meta-for-node
  "Retrieve a piece of meta-data about a node"
  [snippet node meta-key]
  (get-in snippet [:ast2meta node meta-key]))

(defn 
  snippet-nodes
  "Returns all AST-originating values of the given snippet."
  [snippet]
  (keys (:ast2var snippet)))

(defn
  snippet-contains?
  [snippet value]
  (let [result (contains? (:ast2var snippet) value)]
    ;(assert result (str "Snippet does not contain value: " value))
    result))

(defn-
  snippet-value 
  "Returns the equivalent snippet value if there exists a match variable for it in the snippet, returns nil otherwise.
   Stems from period in which reifying same object twice resulted in different values."
  [snippet val]
  ;(some #{val} (snippet-nodes snippet)))
  (when 
    (snippet-contains? snippet val)
    val))

(defn
  snippet-list-containing
  [snippet mbr]
  (when (astnode/ast? mbr)
    (if-let [owner (astnode/owner mbr)]
      (let [property (astnode/owner-property mbr)]
        (when (astnode/property-descriptor-list? property)
          (let [wrappedlst (astnode/node-property-value|reified owner property)]
            (snippet-value snippet wrappedlst)))))))
 
(defn
  snippet-node-owner
  "Returns representation in snippet for owner of given node."
  [snippet node]
  (let [owner (astnode/owner node)]
    ;finds value equal to, but not identitical to owner .. should not make a difference in practice (see note in make-value, and see jdt-node-as-snippet)
    ;(some #{owner} (snippet-nodes snippet)))) 
    (snippet-value snippet owner)))

(defn
  snippet-node-children
  "Returns representations in snippet for children of given node. 
   Corresponds to JDT children: other nodes, simple values, and lists.
   Use snippet-node-children|conceptually for variant that also accepts lists, and returns their elements."
  [snippet node]
  ;;finds all values in snippet whose owner is equal to the given node.
  ;;note that using astnode/node-property-values would create new wrappersfor primitive values.
  ;;which the JFace treeviewer might not like
  ;(filter
  ;  (fn [value] 
  ;    (= (astnode/owner value) node))
  ;  (snippet-nodes snippet)))
  (astnode/node-propertyvalues node))


(defn 
  snippet-vars
  "Returns the logic variables that correspond to the AST nodes
   of the given snippet. These variables will be bound to matching
   AST nodes from the queried Java project."
  [snippet]
  (vals (:ast2var snippet)))


(defn 
  snippet-userquery
  "Returns the logic conditions defined by users of the given snippet."
  [snippet]
  (let [query (:userquery snippet)]
    (if (nil? query)
      '()
      query)))


(defn
  snippet-value-list?
  "Returns true for snippet values that are wrapped lists."
  [snippet val]
  (boolean 
    ;(if-let [value (snippet-value snippet val)] ;;too expensive
    (astnode/lstvalue? val))) 

(defn
  snippet-value-list-unwrapped
  "Returns the ASTNode$NodeList wrapped by this template value."
  [snippet val]
  (when
    (snippet-value-list? snippet val)
    (astnode/value-unwrapped val)))

(defn
  snippet-value-primitive?
  "Returns true for snippet values that are wrapped primitives."
  [snippet val]
  (boolean 
    (if-let [value (snippet-value snippet val)]
      (astnode/primitivevalue? value))))

(defn
  snippet-value-primitive-unwrapped
  "Returns the primitive wrapped by this template value."
  [snippet val]
  (when
    (snippet-value-primitive? snippet val)
    (astnode/value-unwrapped val)))

(defn
  snippet-value-node?
  "Returns true for snippet values that are AST nodes."
  [snippet val]
  (boolean 
    (if-let [value (snippet-value snippet val)]
      (astnode/ast? value))))

(defn
  snippet-value-node-unwrapped
  "Returns the node 'wrapped' by this template value."
  [snippet val]
  (when
    (snippet-value-node? snippet val)
    val))


(defn
  snippet-value-null?
  "Returns true for snippet values that are wrapped nulls."  
  [snippet val]
  (boolean 
    (if-let [value (snippet-value snippet val)]
      (astnode/nilvalue? value))))


;; Manipulating snippets
;; ---------------------

(defn
  copy-jdt-node
  [^ASTNode node]
  (.copySubtree (.getAST node) node))

(defn
  update-bounddirectives
  [snippet node bounddirectives]
  (update-in snippet
             [:ast2bounddirectives node]
             (fn [oldbounddirectives] 
               bounddirectives)))

(defn
  update-anchor 
  [snippet anchor]
  (assoc snippet :anchor anchor))

(defn
  add-bounddirective
  [snippet node bounddirective]
  (update-in snippet
             [:ast2bounddirectives node]
             (fn [oldbounddirectives] (conj oldbounddirectives bounddirective))))

(defn
  remove-bounddirective
  [snippet node bounddirective]
  (update-in snippet
             [:ast2bounddirectives node]
             (fn [oldbounddirectives] (remove #{bounddirective} oldbounddirectives))))

(defn
  update-meta
  [snippet node meta-key meta-value]
  (update-in snippet
             [:ast2meta node]
             (fn [oldmeta] (assoc oldmeta meta-key meta-value))))

(defn
  update-uservar
  "Adds a mapping from the match for the given snippet node to a user-defined variable."
  [snippet node uservar]
  (assoc-in snippet
            [:var2uservar (snippet-var-for-node snippet node)]
            (symbol uservar)))

(defn
  snippet-node-parent|conceptually
  "Returns conceptual parent of this snippet element 
   (as it would be displayed in the tree viewer of the template editor)."
  [snippet c]
  (if-let [ownerproperty (astnode/owner-property c)] 
    ;owner of compilationunit = nil, parent = nil
    ;owner of list = node
    ;owner of node = parent
    ;owner of list element = node ... should look for list containing value insted
    (if
      (and 
        (astnode/property-descriptor-list? ownerproperty)
        (not (astnode/lstvalue? c)))
      (snippet-list-containing snippet c)
      (snippet-node-owner snippet c))))


(defn
  snippet-node-children|conceptually
  "Returns conceptual children of this snippet element 
   (as they would be displayed in the tree viewer of the template editor)."
  [snippet node]
  (cond 
    (snippet-value-list? snippet node)
    (snippet-value-list-unwrapped snippet node)
    (snippet-value-node? snippet node)
    (snippet-node-children snippet node)
    :default
    []))

;  (filter 
;    (fn [child]
;      (=  node (snippet-node-parent|conceptually snippet child)))
;    (snippet-nodes snippet)))



(defn 
  walk-snippet-element
  "Performs a recursive descent through a particular snippet element.
   Takes snippet changes into account that might not be reflected 
   in the snippets' root ASTNode. Parent and children of AST nodes are
   looked up in snippet, rather than taken from node itself."
  ([snippet element f]
    (walk-snippet-element snippet element f f f f))
  ([snippet element node-f list-f primitive-f null-f]
    (loop
      [nodes (list element)]
      (when-not (empty? nodes)
        (let [val (first nodes)
              others (rest nodes)]
          (cond 
            (astnode/ast? val)
            (do
              (node-f val)
              (recur 
                (concat 
                  (snippet-node-children|conceptually snippet val)
                  others)))
            (astnode/lstvalue? val)
            (do 
              (list-f val)
              (recur (concat 
                       (snippet-node-children|conceptually snippet val)
                       others)))
            (astnode/primitivevalue? val)
            (do
              (primitive-f val)
              (recur others))
            (astnode/nilvalue? val)
            (do
              (null-f val)
              (recur others))
            :default
            (throw (Exception. (str "Don't know how to walk this value:" val)))
            ))))))

(defn 
  walk-snippet-element-track-depth
  "@see walk-snippet-element
   Also passes the depth of each node on the function that is called on all nodes in the snippet.
   That is, f takes two arguments: the node itself and its depth in the tree. (where level 0 is the snippet root)"
  ([snippet element f]
    (walk-snippet-element-track-depth snippet element f f f f))
  ([snippet element node-f list-f primitive-f null-f]
    (loop
      [nodes (list element) ; worklist
       depths (list 0)]
      (when-not (empty? nodes)
        (let [val (first nodes)
              others (rest nodes)
              cur-depth (first depths)
              other-depths (rest depths)]
          (cond 
            (astnode/ast? val)
            (do
              (node-f val cur-depth)
              (let [children (snippet-node-children|conceptually snippet val)
                    child-depths (for [x children] (inc cur-depth))]
                (recur 
                  (concat children others)
                  (concat child-depths other-depths))))
            
            (astnode/lstvalue? val)
            (do 
              (list-f val cur-depth)
              (let [children (snippet-node-children|conceptually snippet val)
                    child-depths (for [x children] (inc cur-depth))]
                (recur 
                  (concat children others)
                  (concat child-depths other-depths))))
            
            (astnode/primitivevalue? val)
            (do
              (primitive-f val cur-depth)
              (recur others other-depths))
            
            (astnode/nilvalue? val)
            (do
              (null-f val cur-depth)
              (recur others other-depths))
            :default
            (throw (Exception. (str "Don't know how to walk this value:" val)))
            ))))))

;(defn add-depth-info 
;  "Walks over a snippet to add meta-info about the tree depth of each node
;   (available via (snippet-meta-for-node node :depth) )"
;  [snippet]
;  (let [newsnippet (atom snippet)]
;    (walk-snippet-element-track-depth
;      @newsnippet
;      (snippet-root snippet)
;      (fn [node depth]
;        (swap! newsnippet update-meta node :depth depth)))
;    @newsnippet))

(defn 
  walk-snippets-elements
  "See walk-snippet-element, but walks two corresponding elements from different snippets simultaneously.
   Function arguments therefore take pairs of elements, rather than a single element."
  ([s1 e1 s2 e2 f]
    (walk-snippets-elements s1 e1 s2 e2 f f f f))
  ([s1 e1 s2 e2 node-f list-f primitive-f null-f]
    (loop
      [nodes (list [e1 e2])]
      (when-not (empty? nodes)
        (let [[v1 v2 :as v] (first nodes)
              others (rest nodes)]
          (cond 
            (astnode/ast? v1)
            ;;;todo: check v2 is an astnode as well, otherwise throw friendly exception
            (do
              (node-f v)
              (recur 
                (concat 
                  (map vector 
                       (snippet-node-children|conceptually s1 v1)
                       (snippet-node-children|conceptually s2 v2))
                  others)))
            (astnode/lstvalue? v1)
            (do 
              (list-f v)
              (recur (concat
                       (map vector 
                            (snippet-node-children|conceptually s1 v1)
                            (snippet-node-children|conceptually s2 v2))
                       others)))
            (astnode/primitivevalue? v1)
            (do
              (primitive-f v)
              (recur others))
            (astnode/nilvalue? v1)
            (do
              (null-f v)
              (recur others))
            :default
            (throw (Exception. (str "Don't know how to walk this value:" val)))
            ))))))



;; Snippet value identifiers (see also astnode.clj of Ekeko for identifiers of JDT values)
;; ---------------------------------------------------------------------------------------

(defrecord 
  RootIdentifier []) 

(defn
  make-root-identifier
  []
  (RootIdentifier.))

(defmethod 
  clojure.core/print-dup 
  RootIdentifier
  [identifier w]
  (.write ^Writer w (str  "#=" `(make-root-identifier))))

(defn
  snippet-value-identifier
  [snippet value]
  (let [owner (astnode/owner value) ;owner of list = node, owner of list element = node (never list)
        property (astnode/owner-property value)]
    (cond 
      ;root
      (= value (snippet-root snippet))
      (make-root-identifier)
      
      ;lists (keep before next clause, do not merge with before-last clause)
      (astnode/lstvalue? value)
      (astnode/make-property-value-identifier 
        (snippet-value-identifier snippet owner)
        property)
      
      ;list members
      (astnode/property-descriptor-list? property)
      (let [lst (snippet-list-containing snippet value)
            lst-raw (astnode/value-unwrapped lst)]
        (assert lst (str "Could not find list in snippet containing list member:" value))
        (astnode/make-list-element-identifier 
          (snippet-value-identifier 
            snippet
            lst)
          (.indexOf ^List lst-raw value)))
      
      ;non-list members
      (or 
        (astnode/ast? value)
        (astnode/nilvalue? value)
        (astnode/primitivevalue? value))
      
      
      
      (astnode/make-property-value-identifier
        (snippet-value-identifier snippet owner)
        property)
      
      :else
      (throw (Exception. (str "Unknown snippet value to create identifier for:" value))))))



(defn-
  find-snippet-value-corresponding-to-identifier
  [snippet identifier]
  (some 
    (fn [value] 
      (let [value-id (snippet-value-identifier snippet value)]
        (when (= value-id identifier)
          value)))
    (snippet-nodes snippet)))


(defn
  snippet-value-corresponding-to-identifier
  [snippet identifier]
  (let [found (find-snippet-value-corresponding-to-identifier snippet identifier)]
    (if
      (nil? found)
      (throw (Exception. (str "While deserializing snippet, could not locate node for identifier in snippet:" identifier snippet)))
      found))) 


(defprotocol 
  IIdentifiesProjectValueForSnippetValue
  (corresponding-projectvalue-for-snippetvalue [identifier snippetrootinproject]
                                               "See snippet-corresponding-projectvalue-for-snippetvalue."))

(extend-protocol 
  IIdentifiesProjectValueForSnippetValue
  RootIdentifier
  (corresponding-projectvalue-for-snippetvalue [id snippetrootinproject]
    snippetrootinproject) ;difference with astnode/corresponding-project-value: recursion stops with given astnode, does not retrieve CU for handle
  RelativePropertyValueIdentifier
  (corresponding-projectvalue-for-snippetvalue [id snippetrootinproject]
    (let [ownerid (:ownerid id)
          property (:property id)
          owner (corresponding-projectvalue-for-snippetvalue ownerid snippetrootinproject)]
      (astnode/node-property-value|reified owner property)))
  RelativeListElementIdentifier
  (corresponding-projectvalue-for-snippetvalue [id snippetrootinproject]
    (let [listid (:listid id)
          idx (:index id)
          lst (corresponding-projectvalue-for-snippetvalue listid snippetrootinproject)
          lst-raw (astnode/value-unwrapped lst)]
      (.get ^List lst-raw idx))))


(def
  snippet-corresponding-projectvalue-for-snippetvalue
  "Returns the JDT value from the snippet's project anchor that corresponds to the given snippet value, if it still exists."
  (memoize 
    (fn [snippet value]
      (if-let [rootinproject 
               (snippet-anchor|resolved snippet)]
        (let [valueid 
              (snippet-value-identifier snippet value)]
          (corresponding-projectvalue-for-snippetvalue valueid rootinproject))))))


(defn
  snippet-node-resolvedbinding
  "Resolves binding for given node using the snippet's project anchor."
  ([snippet node bindingproducingfn]
    (if-let [projectnode (snippet-corresponding-projectvalue-for-snippetvalue snippet node)]
      (bindingproducingfn projectnode)))
  ([snippet node]  
    (snippet-node-resolvedbinding 
      snippet node
      (fn [projectnode]
        (let [nodetype (astnode/ekeko-keyword-for-class-of projectnode)]
          (when (some #{nodetype} astnode/ekeko-keywords-for-resolveable-ast-classes)
            (.resolveBinding projectnode)))))))


(defn
  snippet-children-resolvingto
  "Recursive descent through a snippet that is anchored to a project,
   starting from root (inclusively), 
   in search for nodes that resolve to a binding for which the given predicate succeeds."
  ([snippet root bindingpredicate]
    (snippet-children-resolvingto snippet root bindingpredicate snippet-node-resolvedbinding))
  ([snippet root bindingpredicate snippetnodebindingfn]
    (let [children (atom '())] 
      (walk-snippet-element 
        snippet
        root 
        (fn [node] 
          (if-let [nodebinding (snippetnodebindingfn snippet node)]
            (when (bindingpredicate nodebinding) 
              (swap! children conj node))))
        (fn [list])
        (fn [prim])
        (fn [null]))
      @children)))






(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_ROOT) snippet-root)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_USERQUERY) snippet-userquery)
  
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_LIST_CONTAINING) snippet-list-containing)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_ISLIST) snippet-value-list?)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_LIST) snippet-value-list-unwrapped)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_ISVALUE)  snippet-value-primitive?)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_VALUE)  snippet-value-primitive-unwrapped)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_ISNODE) snippet-value-node?)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_NODE) snippet-value-node-unwrapped)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_ISNULL) snippet-value-null?)
  
  (set! (damp.ekeko.snippets.gui.BoundDirectivesViewer/FN_BOUNDDIRECTIVES_FOR_NODE) snippet-bounddirectives-for-node)
  
  (set! (damp.ekeko.snippets.gui.TemplateEditor/FN_SNIPPET_ANCHOR) snippet-anchor)
  (set! (damp.ekeko.snippets.gui.TemplateEditor/FN_SNIPPET_VALUE_ANCHOR_RESOLVED)  snippet-corresponding-projectvalue-for-snippetvalue)
  
  
  )

(register-callbacks)

