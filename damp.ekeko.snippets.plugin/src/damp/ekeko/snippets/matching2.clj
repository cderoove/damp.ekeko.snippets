(ns
  ^{:doc "Imperative implementation of matching.clj
          .. in the hopes of improving performance and preventing out of memory exceptions :)"
    :author "Tim Molderez"}
  
  damp.ekeko.snippets.matching2
  (:require [clojure.core.logic :as cl]
            [clojure.zip :as zip]
            [damp.ekeko.snippets 
             [directives :as directives]
             [util :as util]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [parsing :as parsing]
             [persistence :as persistence]
             [matching :as matching]
             [runtime :as runtime]]
            [damp.ekeko [logic :as el]]
            [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]
             [structure :as structure]
             [aststructure :as aststructure]])
  (:import [java.util List]
           [org.eclipse.jdt.core.dom.rewrite ASTRewrite]
           [org.eclipse.jdt.core.dom ASTNode MethodInvocation Expression Statement BodyDeclaration CompilationUnit ImportDeclaration]))

(defn
  is-navigation-directive?
  "Is this a bound directive that alters how the template tree is explored?"
  [bd]
  (or
      (directives/bounddirective-for-directive [bd] matching/directive-child*)
      (directives/bounddirective-for-directive [bd] matching/directive-child+)
      (directives/bounddirective-for-directive [bd] matching/directive-consider-as-set|lst)))

(defn
  check-directives-only?
  "Does this template node have any directives such that we should ignore its type and/or properties?"
  [template node]
  (let [bds (snippet/snippet-bounddirectives-for-node template node)]
    (or
      (directives/bounddirective-for-directive bds matching/directive-replacedbywildcard)
      (directives/bounddirective-for-directive bds matching/directive-replacedbyvariable))))

; Defines the properties associated with each current position
; - parent          The parent AST node of the current position
; - bindings-list   Logic variable bindings for the current position
;                   It's a list of maps. Each map maps a logic variable to its potential values.
(defrecord PositionProperties [parent bindings-list])

(defn- matchmap-create 
  "Create a blank matchmap based on an initial list of potential matches"
  [matches]
  (zipmap 
    matches
    (for [match matches]
      {match (PositionProperties. nil [{}]) })))

(defn- lvarbindings-checkconstraint
  "Check a given constraint on one logic variable, and update
   the list of logic variable bindings accordingly.
   @param bindings-list
   @param lvar            The logic variable involved in the constraint
   @param constraintfn    Single-parameter function that tests whether or not
                          a certain constraint holds for a concrete value
   @param generatefn      If the logic variable has no values yet,
                          this function is used to produce all possible values
                          for which the constraint holds"
  [bindings-list lvar constraintfn generatefn]
  nil)

(defn- positionmap-filter
  "Keeps only those entries that pass the given filter function
   @param matchmap  The positionmap to be filtered
   @param filterfn  Filter function"
  [positionmap filterfn]
  (reduce 
    (fn [cur-positionmap [cur-position lvar-map]]
      (if (filterfn cur-position)
        (assoc cur-positionmap cur-position lvar-map)
        cur-positionmap))
    {}
    positionmap)
  )

(defn- matchmap-filter
  "Keeps only those matches that pass the given filter function
   @param matchmap  The matchmap to be filtered
   @param filterfn  Filter function"
  [matchmap filterfn]
  ; Iterate over each match in the matchmap
  (reduce
    (fn [cur-matchmap [match positionmap]]
      ; Iterate over each current position
      (let [new-positionmap (positionmap-filter positionmap filterfn)]
        (if (empty? new-positionmap)
          cur-matchmap
          (assoc cur-matchmap match new-positionmap)
          )
        )
      )
    {}
    matchmap))

(defn- positionmap-updatepositions 
  "Updates each of the current positions within a positionmap
   according to an update-function.
   The update function takes a current position, and returns a list of new positions.
   This implies the new positionmap can have an increased number of current positions."
  [positionmap updatefn]
  (reduce 
    (fn [cur-positionmap [cur-position props]]
      (let [new-positions (updatefn cur-position)]
        (reduce ; Assoc each of the new positions
                (fn [cur-posmap new-position]
                  (assoc cur-posmap new-position (assoc props :parent cur-position)))
                cur-positionmap
                new-positions)))
    {}
    positionmap))

(defn- matchmap-updatepositions
  "Updates each of the current positions within each potential match
   according to an update-function"
  [matchmap updatefn]
  (reduce
    (fn [cur-matchmap [match positionmap]]
      (let [new-pmap (positionmap-updatepositions positionmap updatefn)]
        (if (empty? new-pmap) ; If new positions cannot be determined, there is no match
          cur-matchmap
          (assoc cur-matchmap match new-pmap))))
    {}
    matchmap))

(defn- determine-next-positions
  "Given a child template node, adjust all the current positions of the potential matches
   such that they correspond to that child template node.
   @param index           Only considered when child-node is a list node; retrieves the item at the given index of the list"
  [template child-node matchmap index]
  (let [bds (snippet/snippet-bounddirectives-for-node template child-node)
        owner-prop (astnode/owner-property child-node)]
    (cond 
      ; Child* (normal nodes only)
      (directives/bounddirective-for-directive bds matching/directive-child*)
      (let []
        (println "!!!")
        (matchmap-updatepositions 
         matchmap
         (fn [ast-node]
           (astnode/reachable-nodes-of-type ast-node (class ast-node))
           )))
      ; Match|set (list nodes only)
      (directives/bounddirective-for-directive bds matching/directive-consider-as-set|lst)
      (matchmap-updatepositions
        matchmap
        (fn [ast-node] nil) ; TODO Should first find the very first list item of the template node, which may appear anywhere
        ; Any subsequent items should follow right behind it
        )
      ; Normal navigation (normal and list nodes)
      :else
      (matchmap-updatepositions 
        matchmap
        (fn [ast-node]
          (if (snippet/snippet-value-list? template child-node) 
            (let [template-list (astnode/value-unwrapped child-node)
                  ast-list (astnode/node-property-value ast-node owner-prop)]
              (if (not= (.size template-list) (.size ast-list))
                []
                (let [ast-element (.get ast-list index)
                      template-element (.get template-list index)
                      bds-element (snippet/snippet-bounddirectives-for-node template template-element)]
                  (if (directives/bounddirective-for-directive bds-element matching/directive-child*)
                    (let [] 
                      (println "???" (class ast-element))
                      (astnode/reachable-nodes-of-type ast-element (class template-element)))
                    [ast-element])))
              )
            [(astnode/node-property-value ast-node owner-prop)])))
      )))

(defn- positionmap-return-to-previous-position
  [positionmap match old-matchmap]
  (reduce 
    (fn [cur-positionmap [cur-position props]]
      (let [bindings (:bindings-list props)
            parent (:parent props)
            cur-bindings (:bindings-list (get cur-positionmap parent)) ; Can be nil, but that's normal
            new-bindings (concat bindings cur-bindings)
            grandparent (:parent (get (get old-matchmap match) parent))]
        (assoc cur-positionmap parent (PositionProperties. grandparent new-bindings))))
    {}
    positionmap)
  )

(defn- return-to-previous-position
  "After processing a(n indirect) child node, we need to take
   its logic variable bindings and move/merge them back to the parent node,
   from where we can proceed to the next node."
  [matchmap old-matchmap]
  (reduce
    (fn [cur-mm [match positionmap]]
      (assoc cur-mm match (positionmap-return-to-previous-position positionmap match old-matchmap)))
    {}
    matchmap)
  )

(def fst (atom true))
(defn rst [] (swap! fst (fn [x] true)))

; Println, but only on the first call to prf
(defn- prf [txt]
  (if @fst
    (do
      (swap! fst (fn [x] false))
      (println txt))))

(defn- inf [obj]
  (if @fst
    (do
      (swap! fst (fn [x] false))
      (inspector-jay.core/inspect obj))))

(defn- process-node 
  "Process a template node and its children to perform template matching
   @param template         The template being processed
   @param template-node    The current template node to be processed
   @param matchmap         Contains a map of all potential matches.
                           Each potential match maps to a map containing the current positions within that match.
                           
                           Each of those current positions then maps to a list of logic variable bindings for that position.
                           Finally, each logic variable binding maps to a list of its potential values.
   @return                 The updated matchmap, after processing this node and its children"
  [template template-node matchmap]
  ; Dbg: The last entry printed is the problem-causing node
;  (if (not= 0 (count (keys matchmap)))
;    (do
;      (println (count (keys matchmap)))
;      (println template-node)))
  
  (cond
    ; Regular AST nodes
    (astnode/ast? template-node)
    (let [check-directives-only (check-directives-only? template template-node)
          
          ; 1 - Check node type
          matchmap-1 
          (if check-directives-only
            matchmap
            (matchmap-filter matchmap (fn [ast-node] (= (class ast-node) (class template-node)))))
          
          
          ; 2 - Check directives (only considering directives that do not affect navigation!)
          bds (remove
                is-navigation-directive?
                (snippet/snippet-bounddirectives-for-node template template-node))
          matchmap-2 matchmap-1 ; TODO
          
;          tmp (println (count (keys matchmap-2)))
          
          ; 3 - Check node children (taking into account navigation directives!)
          matchmap-3
          (if check-directives-only
            matchmap-2
            (reduce 
              (fn [cur-matchmap child]
                (cond
                  ; For list nodes, process each element one after the other
                  (astnode/lstvalue? child)
                  (reduce
                    (fn [cur-mmap [index list-element]]
                      (let [pos-matchmap (determine-next-positions template child cur-mmap index)
                            new-mmap (process-node template list-element pos-matchmap)
                            final-mmap (return-to-previous-position new-mmap cur-mmap)]
                        final-mmap))
                    cur-matchmap
                    (let [elements (astnode/value-unwrapped child)]
                      (map-indexed (fn [idx elem] [idx elem]) elements )))
                  ; Regular nodes
                  (or 
                    (astnode/ast? child)
                    (astnode/primitivevalue? child))
                  (let [pos-matchmap (determine-next-positions template child cur-matchmap 0)
;                        tmp (prf child)
                        new-mmap (process-node template child pos-matchmap)
                        ;tmp (inspector-jay.core/inspect new-mmap)
                        final-mmap (return-to-previous-position new-mmap cur-matchmap)]
                    final-mmap
                    )
                  (astnode/nilvalue? child)
                  cur-matchmap ; Ignoring these
                  ; Shouldn't happen!
                  :rest (println "!!! 286")))
              matchmap-2
              (snippet/snippet-node-children|conceptually-refs template template-node))
            )
          
; Redundant; already checked by determine-new-positions..          
;          properties (astnode/node-ekeko-properties template-node)
;          matchmap-3__
;          (if check-directives-only
;            matchmap-2
;            (reduce 
;              (fn [cur-matchmap [property-keyw retrievalfn]]
;                (let [prop-descriptor (astnode/node-property-descriptor-for-ekeko-keyword template-node property-keyw)]
;                  (matchmap-filter 
;                    cur-matchmap
;                    (fn [ast-node]
;                      (or
;                        (not (astnode/property-descriptor-simple? prop-descriptor))
;                        (not (nil? (retrievalfn ast-node) )))))))
;              matchmap-2
;              properties))
          ]
      matchmap-3
      )
    
    ; List nodes
    (astnode/lstvalue? template-node)
    (println "314 May not occur!") ; May never occur!! Process-node is called directly with the list elements
    
    ; Primitive values (leafs)
    (astnode/primitivevalue? template-node)
    (let [;exp (matching/ast-primitive-as-expression (astnode/value-unwrapped template-node))
          val (astnode/value-unwrapped template-node)]
      (matchmap-filter matchmap 
                       (fn [ast-node] (= ast-node val))))
    
    ; Null values
    (astnode/nilvalue? template-node)
    (matchmap-filter matchmap 
                     (fn [ast-node] (astnode/nilvalue? ast-node)))))

(defn query-template 
  "Look for matches of a template
   @param template       The template to be matched
   @param lvar-bindings  A map, mapping logic variables to their potential values.
                         This is useful in case this template is part of a group, and a previous
                         template in the group narrowed down the potential values of logic variables
                         that may occur in this template as well.
   @return               A pair consisting of:
                         - a list of matches
                         - an updated map of logic variable bindings"
  ([template]
    (query-template template {}))
  ([template lvar-bindings]
    (rst)
    (let [root (snippet/snippet-root template)
          root-type (astnode/ekeko-keyword-for-class-of root)
          matches (for [result-vector (damp.ekeko/ekeko [?m] (ast/ast root-type ?m))]
                    (first result-vector))
          matchmap (matchmap-create matches)]
      (process-node template root matchmap))))

(defn query-templategroup
  "Look for matches of a template group
   @param templategroup  The template group to be matched
   @return               A pair consisting of:
                         - a list of matches
                         - an updated map of logic variable bindings"
  [templategroup]
  (let [templates (snippetgroup/snippetgroup-snippetlist templategroup)
        process-template (fn [[template & rest-templates] prev-matches lvar-bindings]
                           ; TODO still need to merge lvar-bindings from matches..
                           (let [[matches new-lvar-bindings] (query-template template lvar-bindings)
                                 new-matches (conj prev-matches matches)] 
                             (if (nil? rest-templates)
                               [new-matches new-lvar-bindings]
                               (recur rest-templates new-matches new-lvar-bindings))))]
    (process-template templates [] {})))

(comment ; All tests use the CompositeVisitor project
  
  (defn slurp-from-resource [pathrelativetobundle]
    (persistence/slurp-snippetgroup (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))
  (def templategroup
    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method-childstar.ekt")
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method.ekt") ; OK! Also works correctly if too many list items
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/sysout.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/mini.ekt") ; OK! Also works correctly with diff prim values
;    (slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt")
    )

  (query-templategroup templategroup)
  (first (query-template (first (snippetgroup/snippetgroup-snippetlist templategroup))))
  
  
  
  ; Tests for AST navigation..
  (def node (first (first (damp.ekeko/ekeko [?m] (ast/ast :MethodDeclaration ?m)))))
  (def props (astnode/node-property-descriptors node) )
  
  ; SimpleProperty
  (class (astnode/node-property-value node (nth props 2)))
  
  ; ChildProperty
  (def sn (class (astnode/node-property-value|reified node (nth props 5))))
  
  ; ChildListProperty
  (astnode/node-property-value node (nth props 1))
  
  (astnode/reachable-nodes-of-type node sn)
  )