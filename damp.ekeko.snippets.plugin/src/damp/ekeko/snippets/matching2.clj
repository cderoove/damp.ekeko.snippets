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
            [damp.ekeko 
             [logic :as el]
             [ekekomodel :as ekekomodel]]
            [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]
             [structure :as structure]
             [aststructure :as aststructure]
             [javaprojectmodel :as javaprojectmodel]])
  (:import [java.util List]
           [org.eclipse.jdt.core ITypeHierarchy]
           [org.eclipse.jdt.core.dom.rewrite ASTRewrite]
           [org.eclipse.jdt.core.dom ASTNode Annotation MethodInvocation Expression 
            FieldAccess SuperFieldAccess Statement BodyDeclaration CompilationUnit ImportDeclaration]))

(defn
  is-navigation-directive?
  "Is this a bound directive that alters how the template tree is explored?"
  [bd]
  (or
    (directives/bounddirective-for-directive [bd] matching/directive-exact) ; Not really a navigation directive, but we can safely ignore these
    (directives/bounddirective-for-directive [bd] matching/directive-replacedbywildcard) ; Can also be ignored..
    (directives/bounddirective-for-directive [bd] matching/directive-child)
    (directives/bounddirective-for-directive [bd] matching/directive-child*)
    (directives/bounddirective-for-directive [bd] matching/directive-child+)
    (directives/bounddirective-for-directive [bd] matching/directive-consider-as-set|lst)))

(defn
  check-directives-only?
  "Does this template node have any directives such that we should ignore the node's type and its children?"
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
   Returns nil if the constraint isn't satisfied by any value.
   @param bindings
   @param lvar            The logic variable involved in the constraint
   @param ast-node        The subject node to which the constraint is attached
   @param constraintfn    Two-parameter function that tests whether or not
                          a certain constraint holds for a concrete subject and logic variable value
   @param generatefn      If the logic variable has no values yet,
                          this function is used to produce all possible values
                          for which the constraint holds"
  [bindings ast-node lvar constraintfn generatefn]
  (let [values (get bindings lvar)
        new-values (if (empty? values)
                     (generatefn ast-node)
                     (filter (fn [val] (constraintfn ast-node val)) values))
        new-bindings (assoc bindings lvar new-values)]
    (if (empty? new-values)
      nil
      (assoc bindings lvar new-values))))

(defn- positionmap-checkconstraint
  [positionmap lvar constraintfn generatefn]
  (reduce 
    (fn [cur-positionmap [cur-position props]]
      (let [bindings-list (:bindings-list props) 
            new-bindings-list (for [bindings bindings-list]
                                (lvarbindings-checkconstraint bindings cur-position lvar constraintfn generatefn))
            new-props (assoc props :bindings-list (remove nil? new-bindings-list))]
        (if (empty? new-bindings-list) 
          cur-positionmap
          (assoc cur-positionmap cur-position new-props))))
    {}
    positionmap))

(defn- matchmap-checkconstraint 
  [matchmap lvar constraintfn generatefn]
  (reduce
    (fn [cur-matchmap [match positionmap]]
      (let [new-pmap (positionmap-checkconstraint positionmap lvar constraintfn generatefn)]
        (if (empty? new-pmap)
          cur-matchmap
          (assoc cur-matchmap match new-pmap))))
    {}
    matchmap))

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
        (if (empty? new-pmap) ; If new positions cannot be determined, there is no match!
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
      (matchmap-updatepositions 
        matchmap
        (fn [ast-node]
          (astnode/reachable-nodes-of-type ast-node (class ast-node))
          ))
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
                    (astnode/reachable-nodes-of-type ast-element (class template-element))
                    [ast-element])))
              )
            [(astnode/node-property-value ast-node owner-prop)]))))))

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

(defn directive-constraints 
  "Implements all directives (not relating to navigation)
   Returns a pair: [constraintfn generatorfn]"
  [directive]
  (let 
    [name (snippet/directive-name directive)
     
     get-type 
     (fn [ast-node]
       (.getJavaElement
         (cond 
           (instance? Expression ast-node)
           (.resolveTypeBinding ast-node)
           (instance? Annotation ast-node)
           (.resolveAnnotationBinding ast-node)
           :else
           (.resolveBinding ast-node))))
     
     get-all-ancestors
     (fn ancestors [itype]
       (let [hierarchy (.getTypeHierarchy ^damp.ekeko.EkekoModel (ekekomodel/ekeko-model) itype)
             supers (.getAllSupertypes ^ITypeHierarchy hierarchy itype)]
         (concat supers 
                 (apply concat 
                        (for [super supers] (ancestors super))))))
     
     directives
     {"replaced-by-variable"
      [(fn [ast-node val] (= ast-node val))
       (fn [ast-node] [ast-node])]
      
      "overrides"
      [(fn [ast-node val]
         (some 
           (fn [tgt] (= val tgt))
           (javaprojectmodel/method-overriders ast-node)))
       (fn [ast-node]
         (javaprojectmodel/method-overriders ast-node))]
      
      "invokes"
      [(fn [ast-node val]
         (some 
           (fn [tgt] (= val tgt))
           (javaprojectmodel/invocation-targets ast-node)))
       (fn [ast-node]
         (javaprojectmodel/invocation-targets ast-node))]
      
      "equals" ; Same as replaced-by-variable (only difference is that equals isn't mentioned in the check-directives-only? fn)
      [(fn [ast-node val] (= ast-node val))
       (fn [ast-node] [ast-node])]
      
      "type"
      [(fn [ast-node val] (= val (get-type ast-node) ))
       (fn [ast-node] [(get-type ast-node)])]
      
      "subtype*"
      [(fn [ast-node val]
         (let [itype (get-type ast-node)
               ancestors (get-all-ancestors itype)]
           (some
             (fn [subtype] (= val subtype))
             (conj itype ancestors))))
        (fn [ast-node]
          (let [itype (get-type ast-node)
                ancestors (get-all-ancestors itype)]
            (conj itype ancestors)))]
      
      "subtype+"
      [(fn [ast-node val]
         (let [itype (get-type ast-node)
               ancestors (get-all-ancestors itype)]
           (some (fn [subtype] (= val subtype)) ancestors)))
       (fn [ast-node]
         (let [itype (get-type ast-node)]
           (get-all-ancestors itype)))]
      
      "refers-to"
      [(fn [ast-node val]
         (let [binding (cond
                         (or (instance? FieldAccess ast-node) (instance? SuperFieldAccess ast-node))
                         (.resolveFieldBinding ast-node)
                         :rest
                         (.resolveBinding ast-node))]
           (= val (javaprojectmodel/binding-to-declaration binding))))
       (fn [ast-node]
         (let [binding (cond
                         (or (instance? FieldAccess ast-node) (instance? SuperFieldAccess ast-node))
                         (.resolveFieldBinding ast-node)
                         :rest
                         (.resolveBinding ast-node))
               local-decl (javaprojectmodel/binding-to-declaration binding)]
           [local-decl]))]
      
      "constructs" ; Same as invokes..
      [(fn [ast-node val]
         (some 
           (fn [tgt] (= val tgt))
           (javaprojectmodel/invocation-targets ast-node)))
       (fn [ast-node]
         (javaprojectmodel/invocation-targets ast-node))]
      }]
    (get directives name)))

; Some debugging fns
(def fst (atom true))
(def obj (atom nil))
(def counter (atom 0))

(defn rst
  "Reset"
  [] 
  (swap! fst (fn [x] true))
  (swap! counter (fn [x] 0)))

(defn store
  "Store the given object in @obj, but only at the nth call to this function"
  [n object]
  (if (= @counter n)
    (swap! obj (fn [x] object)))
  (swap! counter inc))

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
                           This can only be a plain AST node; this holds by construction!
                           (i.e. not a list node, nor a primitive value nor null)
   @param matchmap         Contains a map of all potential matches.
                           Each potential match maps to a map containing the current positions within that match.
                           Each of those current positions then maps to a list of logic variable bindings for that position.
                           Finally, each logic variable binding maps to a list of its potential values.
   @return                 The updated matchmap, after processing this node and its children"
  [template template-node matchmap]
;  (if (not= 0 (count (keys matchmap)))
;    (do
;      (println (count (keys matchmap)))
;      (println template-node)
;      (println (for [match (keys matchmap)] (for [curpos (keys (get matchmap match))] (str "$" (.toString curpos)))))
;      ))
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
        matchmap-2
        (reduce
          (fn [cur-matchmap bd]
            (let 
              [directive (snippet/bounddirective-directive bd)
               lvar (.getOperand (first (.getOperandBindings bd)))
               [constraintfn generatefn] (directive-constraints directive)]
              (matchmap-checkconstraint cur-matchmap lvar constraintfn generatefn)))
          matchmap-1
          bds)
        
        ; 3 - Check node children (taking into account navigation directives!)
        matchmap-3
        (if check-directives-only
          matchmap-2
          (reduce 
            (fn [cur-matchmap child]
              (cond
                ; For list nodes, process each element one after the other
                ; TODO must still check directives! hm.. a bit of duplication with primitive nodes
                (astnode/lstvalue? child)
                (if (check-directives-only? template child)
                  cur-matchmap
                  (reduce 
                    (fn [cur-mmap [index list-element]]
                      (let [pos-matchmap (determine-next-positions template child cur-mmap index)
                            new-mmap (process-node template list-element pos-matchmap)
                            final-mmap (return-to-previous-position new-mmap cur-mmap)]
                        final-mmap))
                    cur-matchmap
                    (let [elements (astnode/value-unwrapped child)]
                      (map-indexed (fn [idx elem] [idx elem]) elements ))))
                ; Regular nodes
                (astnode/ast? child)
                (let [pos-matchmap (determine-next-positions template child cur-matchmap 0)
                      new-mmap (process-node template child pos-matchmap)
                      final-mmap (return-to-previous-position new-mmap cur-matchmap)]
                  final-mmap)
                ; Primitive nodes ; TODO Should still check directives on primitives!
                (astnode/primitivevalue? child)
                (if (check-directives-only? template child)
                  cur-matchmap
                  (matchmap-filter 
                    cur-matchmap 
                    (fn [ast-node] 
                      (let [owner-prop (astnode/owner-property child)
                            ast-child (astnode/node-property-value ast-node owner-prop)]
                        (= ast-child (astnode/value-unwrapped child))))))
                ; Null values (are ignored)
                (astnode/nilvalue? child)
                cur-matchmap
                ; Anything else may not occur
                :rest
                (println "!!!286")))
            matchmap-2
            (snippet/snippet-node-children|conceptually-refs template template-node))
          )]
    matchmap-3
    ))

(defn query-template 
  "Look for matches of a template
   @param template       The template to be matched
   @param lvar-bindings  A map, mapping logic variables to their potential values.
                         This is useful in case this template is part of a group, and a previous
                         template in the group narrowed down the potential values of logic variables
                         that may occur in this template as well."
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
    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/sysout-var.ekt")
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/sysout-wcard.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method-childstar3.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method-childstar2.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method-childstar.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method.ekt") ; OK! Also works correctly if too many list items
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/sysout.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/mini.ekt") ; OK! Also works correctly with diff prim values
;    (slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt")
    )

  (query-templategroup templategroup)
  (inspector-jay.core/inspect
    (query-template (first (snippetgroup/snippetgroup-snippetlist templategroup))))
  
  
  
  ; Tests for AST navigation..
  (def node (first (first (damp.ekeko/ekeko [?m] (ast/ast :MethodDeclaration ?m)))))
  (println (.toString node))
  (def props (astnode/node-property-descriptors node) )
  
  ; SimpleProperty
  (class (astnode/node-property-value node (nth props 2)))
  
  ; ChildProperty
  (def sn (class (astnode/node-property-value|reified node (nth props 5))))
  
  ; ChildListProperty
  (astnode/node-property-value node (nth props 1))
  
  (astnode/reachable-nodes-of-type node sn)
  )