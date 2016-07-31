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
             [parsing-template :as parsing-template]
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
           [org.eclipse.jdt.core.dom ASTNode Annotation MethodInvocation Expression Name
            FieldAccess SuperFieldAccess Statement BodyDeclaration CompilationUnit ImportDeclaration
            MethodDeclaration SuperMethodInvocation ClassInstanceCreation ConstructorInvocation
            SuperConstructorInvocation]))

(defn
  non-relation-directive?
  "Tests that a BoundDirective is not a directive which establishes a relation 
   between an AST node and a logic variable."
  [bd]
  (or
    (directives/bounddirective-for-directive [bd] matching/directive-exact) ; Not needed anymore
    (directives/bounddirective-for-directive [bd] matching/directive-protect) ; Does not add any constraints
    (directives/bounddirective-for-directive [bd] matching/directive-replacedbywildcard) ; Does not add any constraints
    ; Directives that only affect tree navigation
    (directives/bounddirective-for-directive [bd] matching/directive-multiplicity)
    (directives/bounddirective-for-directive [bd] matching/directive-consider-as-regexp|lst)
    (directives/bounddirective-for-directive [bd] matching/directive-ignore)
    (directives/bounddirective-for-directive [bd] matching/directive-child)
    (directives/bounddirective-for-directive [bd] matching/directive-child*)
    (directives/bounddirective-for-directive [bd] matching/directive-child+)
    (directives/bounddirective-for-directive [bd] matching/directive-consider-as-set|lst)))

(defn
  check-directives-only?
  "Does this template-node have any directives such that we should ignore the node's type and its children?"
  [template node]
  (let [bds (snippet/snippet-bounddirectives-for-node template node)]
    (or
      (directives/bounddirective-for-directive bds matching/directive-replacedbywildcard)
      (directives/bounddirective-for-directive bds matching/directive-replacedbyvariable))))

(defn has-directive? 
  "Does this template node have the given directive?"
  [template node directive]
  (directives/bounddirective-for-directive 
    (snippet/snippet-bounddirectives-for-node template node) 
    directive))

(defn
  pure-wildcard?
  "Is this node's only directive replace-by-wildcard? (aside from directive-child, directive-match, ..)
   (If so, we can safely skip this node .. it can match with anything, including nil)"
  [template node]
  (let [bds (snippet/snippet-bounddirectives-for-node template node)]
    (and
      (directives/bounddirective-for-directive bds matching/directive-replacedbywildcard)
      (every? non-relation-directive? (snippet/snippet-bounddirectives-for-node template node)))))

; Defines the properties associated with each current position
; - parent          The previous position (always an ancestor of the current position)
; - bindings-list   Logic variable bindings for the current position
;                   It's a list of maps. Each map maps a logic variable to its potential values.
(defrecord PositionProperties [parent bindings-list])

(defn- matchmap-create
  "Create a blank matchmap based on an initial list of potential matches
   @param matches           List of potential matches
   @param bindings-list     (Optional) Initial list of logic variable bindings"
  ([matches]
    (matchmap-create matches [{}]))
  ([matches bindings-list]
    (zipmap 
      matches
      (for [match matches]
        {match (PositionProperties. nil bindings-list) }))))

(defn- merge-bindings
  "Merges all lists of lvar bindings within a matchmap into one list of bindings
   Additionally, the matches of the template are now added as the values of a new logic variable templ-name.
   @param matchmap
   @param templ-name   A symbol representing the logic variable's name"
  [matchmap templ-name]
  (reduce
    (fn [bindings-list [match positionmap]]
      (let [merged 
            (apply concat 
                   (for [position (keys positionmap)]
                     (let [b-list (:bindings-list (get positionmap position))]
                       (for [bindings b-list]
                         (assoc bindings templ-name [match])))))]
        (concat bindings-list merged)))
    []
    matchmap))

(defn- lvarbindings-updatevalue
  "Update the value of a logic variable. (If it doesn't exist yet, it can be created now.)

   The updatefn has one argument, the current values of lvar (may be nil),
   and should returns lvar's new values.
   If it returns nil, the lvar is removed from the bindings map, while the others remain intact.
   If it returns an empty list, the entire bindings is removed from the bindings-list."
  [bindings-list lvar updatefn]
  (remove nil?
    (for [bindings bindings-list]
      (let [new-values (updatefn (get bindings lvar))]
        (cond 
          (nil? new-values) (dissoc bindings lvar)
          (empty? new-values) nil
          :rest (assoc bindings lvar new-values))))))

(defn- lvarbindings-checkconstraint
  "Check a given constraint on one logic variable, and update
   the list of logic variable bindings accordingly.
   Returns nil if the constraint isn't satisfied by any value.
   @param bindings
   @param lvar            The logic variable involved in the constraint
   @param ast-node        The subject node to which the constraint is attached
   @param typefn          One-parameter function that checks the type of the directive's subject.
                          Needed because directives can be attached to wildcards, which match with any type of node.
                          Similar to the applicability checks in operatorsrep.clj .. in this case
                          it's just used to prevent constraintfn/generatefn from throwing exceptions..
   @param constraintfn    Two-parameter function that tests whether or not
                          a certain constraint holds for a concrete subject and logic variable value
   @param generatefn      If the logic variable has no values yet,
                          this function is used to produce all possible values
                          for which the constraint holds"
  [bindings ast-node lvar typefn constraintfn generatefn]
  (let [values (get bindings lvar)
        new-values (if (empty? values)
                     (if (typefn ast-node) (generatefn ast-node) [])
                     (filter (fn [val] (and (typefn ast-node) (constraintfn ast-node val))) values))]
    (if (empty? new-values)
      nil
      (assoc bindings lvar new-values))))

(defn- positionmap-checkconstraint
  [positionmap lvar typefn constraintfn generatefn]
  (reduce 
    (fn [cur-positionmap [cur-position props]]
      (let [bindings-list (:bindings-list props) 
            new-bindings-list (for [bindings bindings-list]
                                (lvarbindings-checkconstraint bindings cur-position lvar typefn constraintfn generatefn))
            new-props (assoc props :bindings-list (remove nil? new-bindings-list))]
        (if (empty? new-bindings-list) 
          cur-positionmap
          (assoc cur-positionmap cur-position new-props))))
    {}
    positionmap))

(defn- matchmap-checkconstraint 
  [matchmap lvar typefn constraintfn generatefn]
  (reduce
    (fn [cur-matchmap [match positionmap]]
      (let [new-pmap (positionmap-checkconstraint positionmap lvar typefn constraintfn generatefn)]
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
    positionmap))

(defn- matchmap-filter
  "Keeps only those matches that pass the given filter function
   @param matchmap  The matchmap to be filtered
   @param filterfn  Filter function"
  [matchmap filterfn]
  (reduce
    (fn [cur-matchmap [match positionmap]]
      (let [new-positionmap (positionmap-filter positionmap filterfn)]
        (if (empty? new-positionmap)
          cur-matchmap
          (assoc cur-matchmap match new-positionmap))))
    {}
    matchmap))

(defn- positionmap-updatepositions 
  "Updates each of the current positions within a positionmap
   according to an update-function.
   The update function takes a current position and its properties, 
   and returns a list of pairs (new position; and its properties).
   This implies the new positionmap can have an increased number of current positions."
  [positionmap match updatefn]
  (reduce 
    (fn [cur-positionmap [cur-position props]]
      (let [new-positions-and-props 
            (remove (fn [pair] (nil? (first pair))) (updatefn cur-position props))]
        (reduce ; Assoc each of the new positions
                (fn [cur-posmap [new-position new-props]]
                  (assoc cur-posmap new-position (assoc new-props :parent cur-position)))
                cur-positionmap
                new-positions-and-props)))
    {}
    positionmap))

(defn- matchmap-updatepositions
  "Updates each of the current positions within each potential match
   according to an update-function"
  [matchmap updatefn]
  (reduce
    (fn [cur-matchmap [match positionmap]]
      (let [new-pmap (positionmap-updatepositions positionmap match updatefn)]
        (if (empty? new-pmap) ; If new positions cannot be determined, there is no match!
          cur-matchmap
          (assoc cur-matchmap match new-pmap))))
    {}
    matchmap))

(defn- determine-next-positions
  "Given a template node, adjust all the current positions of the potential matches
   such that they correspond to that template node.
   @param 
   @param index           Only considered when cur-node is a list node; retrieves the item at the given index of the list"
  [ matchmap template cur-node child-node index]
  (let [bds (snippet/snippet-bounddirectives-for-node template cur-node)
        owner-prop (astnode/owner-property cur-node)
        
        ; If template-elem has an ignore directive, return its first child
        ; Otherwise, return template-elem
        ignore-test 
        (fn [template-elem]
          (if (and 
                (astnode/ast? template-elem)
                (directives/bounddirective-for-directive 
                  (snippet/snippet-bounddirectives-for-node template template-elem) 
                  matching/directive-ignore))
            (first (snippet/snippet-node-children template template-elem))
            template-elem))
        
        ; If template-node has a child* directive, return all nodes reachable from
        ; ast-node. If not, returns [ast-node].
        child*-test 
        (fn [template-node ast-node]
          (if (directives/bounddirective-for-directive 
                (snippet/snippet-bounddirectives-for-node template template-node) 
                matching/directive-child*)
            (if (check-directives-only? template template-node)
              (astnode/reachable-nodes ast-node) ; May only filter on node type if it's not a wildcard or replace-by-variable
              (filter 
                (fn [node] (= (class node) (class template-node)))
                (astnode/reachable-nodes ast-node)))
            [ast-node]))
        ]
    (if 
      ; Match|set (list nodes only)
      (directives/bounddirective-for-directive bds matching/directive-consider-as-set|lst)
      (matchmap-updatepositions
        matchmap
        (fn [ast-node props]
          (let [bindings-list (:bindings-list props)
                template-list (astnode/value-unwrapped cur-node)
                template-element (ignore-test child-node)
                ast-list (astnode/node-property-value ast-node owner-prop)
                bds-element (snippet/snippet-bounddirectives-for-node template template-element)
                elements-of-type (if (or
                                       (directives/bounddirective-for-directive bds-element matching/directive-child*)
                                       (check-directives-only? template template-element))
                                   ast-list ; May not filter on element type if it has a child*, wildcard or replace-by-variable
                                   (filter (fn [element] (= (class element) (class template-element))) ast-list))]
            (apply 
              concat 
              (for [element elements-of-type]
                (let [; We use a special logic variable, which has as its value(s) the list elements already matched
                      ; This is necessary to make sure multiple template list elements don't map to the same ast list element.
                      new-bindings-list-1
                      (lvarbindings-updatevalue bindings-list cur-node
                        (fn [vals]
                          (let [elements-seen-so-far-list (if (empty? vals) [[]] vals)]
                            (remove nil?
                                    (for [elements-seen-so-far elements-seen-so-far-list]
                                      (if (some (fn [x] (= x element)) elements-seen-so-far)
                                        nil ; If we've already seen this element before, it's an invalid new position
                                        (conj elements-seen-so-far element)))))))
                      
                      new-bindings-list-2 ; If we're at the end of the list, remove the special logic variable, as it's not needed anymore
                      (if (and (= index (dec (.size template-list))))
                        (lvarbindings-updatevalue new-bindings-list-1 cur-node (fn [vals] nil))
                        new-bindings-list-1)]
                  (if (empty? new-bindings-list-2)
                    [] ; In case there are no valid new positions
                    (for [new-position (child*-test template-element element)]
                      [new-position (assoc props :bindings-list new-bindings-list-2)]))))))))
      ; Normal navigation (normal and list nodes)
      (matchmap-updatepositions 
        matchmap
        (fn [ast-node props]
          (let [new-positions
                (if (snippet/snippet-value-list? template cur-node) 
                  (let [template-list (astnode/value-unwrapped cur-node)
                        ast-list (astnode/node-property-value ast-node owner-prop)]
                    (if (and
                          (not (has-directive? template cur-node matching/directive-consider-as-regexp|lst))
                          (not= (.size template-list) (.size ast-list)))
                      []
                      (let [ast-element (.get ast-list index)
                            template-element (ignore-test child-node)
                            bds-element (snippet/snippet-bounddirectives-for-node template template-element)]
                        (child*-test template-element ast-element))))
                  (child*-test cur-node (astnode/node-property-value ast-node owner-prop)))]
            (for [new-position new-positions]
              [new-position props])))))))

(defn- max-list-length-of-cur-positions 
  "Assuming the current position of a match map is a list node, 
   find the length of the longest list among potential matches"
  [mmap list-node]
  (let [prop (astnode/owner-property list-node)
        all-cur-positions (apply concat (for [pmap (vals mmap)] (keys pmap)))]
    (apply 
      max
      (for [cur-position all-cur-positions]
        (let [ast-list (astnode/node-property-value cur-position prop)]
          (.size ast-list))))))

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
    matchmap))

(defn directive-constraints 
  "Implements all directives (not relating to navigation)
   Returns a triplet of functions that implement the requested directive: [typefn constraintfn generatefn]"
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
             supers (if (nil? hierarchy)
                      []
                      (.getAllSupertypes ^ITypeHierarchy hierarchy itype))]
         (concat supers 
                 (apply concat 
                        (for [super supers] (ancestors super))))))
     
     directives
     {"replaced-by-variable"
      [(fn [ast-node] true)
       (fn [ast-node val] (= ast-node val))
       (fn [ast-node] [ast-node])]
      
      "overrides"
      [(fn [ast-node] (instance? MethodDeclaration ast-node))
       (fn [ast-node val]
         (some 
           (fn [tgt] (= val tgt))
           (javaprojectmodel/method-ancestors ast-node)
           ))
       (fn [ast-node]
         (javaprojectmodel/method-ancestors ast-node))]
      
      "invokes"
      [(fn [ast-node] (or (instance? MethodInvocation ast-node) (instance? SuperMethodInvocation ast-node)))
       (fn [ast-node val]
         (some 
           (fn [tgt] (= val tgt))
           (javaprojectmodel/invocation-targets ast-node)))
       (fn [ast-node] (javaprojectmodel/invocation-targets ast-node))]
      
      "equals" ; Same as replaced-by-variable (only difference is that equals isn't mentioned in the check-directives-only? fn)
      [(fn [ast-node] true)
       (fn [ast-node val] (= ast-node val))
       (fn [ast-node] [ast-node])]
      
      "type"
      [(fn [ast-node] true)
       (fn [ast-node val] (= val (get-type ast-node) ))
       (fn [ast-node] [(get-type ast-node)])]
      
      "subtype*"
      [(fn [ast-node] true)
       (fn [ast-node val]
         (let [itype (get-type ast-node)]
           (if (nil? itype) ; Because ast-node may be a SimpleName that doesn't represent a type..
             false
             (some
               (fn [subtype] (= val subtype))
               (conj (get-all-ancestors itype) itype)))))
       (fn [ast-node]
         (let [itype (get-type ast-node)]
           (if (nil? itype)
             []
             (conj (get-all-ancestors itype) itype))))]
      
      "subtype+"
      [(fn [ast-node] true)
       (fn [ast-node val]
         (let [itype (get-type ast-node)]
           (if (nil? itype)
             false
             (some
               (fn [subtype] (= val subtype))
               (get-all-ancestors itype)))))
       (fn [ast-node]
         (let [itype (get-type ast-node)]
           (if (nil? itype) [] (get-all-ancestors itype))))]
      
      "refers-to"
      [(fn [ast-node] 
         (or 
           (instance? FieldAccess ast-node) 
           (instance? SuperFieldAccess ast-node)
           (instance? Name ast-node)))
       (fn [ast-node val]
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
      
      "constructs"
      [(fn [ast-node] 
         (or 
           (instance? ClassInstanceCreation ast-node)
           (instance? ConstructorInvocation ast-node) 
           (instance? SuperConstructorInvocation ast-node)))
       (fn [ast-node val]
         (some 
           (fn [tgt] (= val tgt))
           (javaprojectmodel/invocation-targets ast-node)))
       (fn [ast-node]
         (javaprojectmodel/invocation-targets ast-node))]
      }]
    (get directives name)))

(defn- check-directives 
  "Check the non-navigation directives of the current template node"
  [template template-node matchmap]
  (let [bds (remove
              non-relation-directive?
              (snippet/snippet-bounddirectives-for-node template template-node))]
    (reduce 
      (fn [cur-matchmap bd]
        (let 
          [directive (snippet/bounddirective-directive bd)
           lvar (directives/directiveoperandbinding-value (second (directives/bounddirective-operandbindings bd)))
           [typefn constraintfn generatefn] (directive-constraints directive)]
          (matchmap-checkconstraint cur-matchmap lvar typefn constraintfn generatefn)))
      matchmap
      bds)))

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
  [template templ-node matchmap]
  (let [node-count (atom 0)
        nci ; Identity function; increases node-count as side-effect
        (fn [matchmap] 
          (swap! node-count 
                 (fn [n] 
                   (if (nil? (:node-count (meta matchmap))) ; Only the return value of process-node has :node-count meta-info
                     (if (= 0 (count matchmap)) n (inc n)) ; For primitive/null nodes
                     (+ n (:node-count (meta matchmap)))))) 
          matchmap)
        
        template-node ; If templ-node has an ignore directive, its first child is processed instead 
        (if (directives/bounddirective-for-directive 
              (snippet/snippet-bounddirectives-for-node template templ-node) 
              matching/directive-ignore)
          (first (snippet/snippet-node-children template templ-node))
          templ-node)
        
;        dbg
;        (if (not= 0 (count (keys matchmap)))
;          (do
;            (println "%%%" (count (keys matchmap)))
;            (println "%%%" template-node)
;            (println (for [match (keys matchmap)] (for [curpos (keys (get matchmap match))] (str "$" (.toString curpos)))))
;            ))
        
        check-directives-only (check-directives-only? template template-node)
        
        process-child ; "Move" to a child node, processes it, then move back to this node
        (fn [mmap pos-node child index]
          (if (pure-wildcard? template child)
            mmap
            (let [pos-mmap (determine-next-positions mmap template pos-node child index)
                  new-mmap (nci (process-node template child pos-mmap))
                  final-mmap (return-to-previous-position new-mmap mmap)]
              final-mmap)))
        
        process-list
        (fn [list-node mmap]
          (if (has-directive? template list-node matching/directive-consider-as-regexp|lst)
            
            ; Regex list
            (let [max-match-length (max-list-length-of-cur-positions mmap list-node)
                  process-elements
                  (fn process-elements [init-mmap init-index elements]
;                    (println (process-child init-mmap list-node (first elements) init-index))
                    
                    (cond
                      ; We've finished the list
                      (empty? elements) init-mmap
                      
                      ; Current element is a kleene-*
                      (has-directive? template (first elements) matching/directive-multiplicity)
                      (loop [match-length 0] ; See if the regex matches if the *-element appears 0 times.. If not, try 1, then 2, 3, 4,...
                        (let [after-star-mmap
                              (loop [cur-length 0
                                     cur-mmap init-mmap]
                                (if (= cur-length match-length)
                                  cur-mmap
                                  (recur 
                                    (inc cur-length) 
                                    (process-child cur-mmap list-node (first elements) (+ init-index cur-length)))))
                              
                              full-regex-mmap (process-elements after-star-mmap (+ init-index match-length 1) (rest elements))]
                          (if (and (= 0 (count full-regex-mmap)) (< match-length max-match-length))
                            (recur (inc match-length)) ; No matches, try again..
                            full-regex-mmap)))
                      
                      ; Current element has no regex quantifiers
                      :else
                      (recur 
                        (process-child init-mmap list-node (first elements) init-index)
                        (inc init-index)
                        (rest elements))))]
              
              (process-elements 
                (check-directives template list-node mmap) 
                0 
                (astnode/value-unwrapped list-node)))
            
            ; Normal (non-regex) list
            (reduce 
              (fn [cur-mmap [index list-element]]
                (process-child cur-mmap list-node list-element index))
              (check-directives template list-node mmap)
              (let [elements (astnode/value-unwrapped list-node)]
                (map-indexed (fn [idx elem] [idx elem]) elements )))))
        
        ; 1 - Check node type
        matchmap-1 
        (if check-directives-only
          matchmap
          (matchmap-filter matchmap (fn [ast-node] (= (class ast-node) (class template-node)))))
        
        ; 2 - Check directives (only considering directives that do not affect navigation)
        matchmap-2 (check-directives template template-node matchmap-1)
        
        ; 3 - Check node children (taking into account navigation directives)
        matchmap-3
        (if check-directives-only
          matchmap-2
          (reduce 
            (fn [cur-matchmap child]
              (cond
                ; For list nodes, process each element one after the other
                (astnode/lstvalue? child)
                (if (check-directives-only? template child)
                  (check-directives template child cur-matchmap)
                  (process-list child (check-directives template child cur-matchmap)))
                ; Regular nodes
                (astnode/ast? child)
                (process-child cur-matchmap child child 0)
                ; Primitive nodes
                (astnode/primitivevalue? child)
                (nci (if (check-directives-only? template child)
                       (check-directives template child cur-matchmap)
                       (check-directives template child
                                          (matchmap-filter 
                                            cur-matchmap 
                                            (fn [ast-node] 
                                              (let [owner-prop (astnode/owner-property child)
                                                    ast-child (astnode/node-property-value ast-node owner-prop)]
                                                (= ast-child (astnode/value-unwrapped child))))))))
                ; Null values
                (astnode/nilvalue? child)
                (nci (if (check-directives-only? template child)
                       cur-matchmap
                       (matchmap-filter 
                         cur-matchmap 
                         (fn [ast-node] 
                           (let [owner-prop (astnode/owner-property child)
                                 ast-child (astnode/node-property-value ast-node owner-prop)]
                             (nil? ast-child))))))
                ; Anything else may not occur
                :rest (throw (Exception. "Unknown node type"))))
            matchmap-2
            (snippet/snippet-node-children|conceptually-refs template template-node))
          )]
    (with-meta 
      matchmap-3 
      {:node-count (if (= 0 (count matchmap-3)) @node-count (inc @node-count))})))

(defn query-template 
  "Look for matches of a template
   @param template       The template to be matched
   @param lvar-bindings  A map, mapping logic variables to their potential values.
                         This is useful in case this template is part of a group, and a previous
                         template in the group narrowed down the potential values of logic variables
                         that may occur in this template as well."
  ([template]
    (query-template template [{}]))
  ([template bindings-list]
    (let [root (snippet/snippet-root template)
         root-type (astnode/ekeko-keyword-for-class-of root)
         matches (ast/nodes-of-type root-type)
         matchmap (with-meta 
                    (matchmap-create matches bindings-list)
                    {:node-count 0})]
     (process-node template root matchmap))))

(defn template-node-count
  "Count the total number of nodes in template (to be able to measure matching progress, partial score, ..)
   (Count includes ast nodes, null nodes, primitive nodes
    ; excluding list nodes as they don't exist in plain ASTs)"
  [template]
  (let [visit-node 
        (fn visit [node]
          (let [node-count (atom 0)
                ncp! (fn [n] (swap! node-count (fn [x] (+ x n))))]
            (if (not (check-directives-only? template node))
              (doseq [child (snippet/snippet-node-children|conceptually-refs template node)]
                (cond 
                  (astnode/lstvalue? child)
                  (if (not (check-directives-only? template child)) 
                    (doseq [element (astnode/value-unwrapped child)]
                      (ncp! (visit element))))
                  (astnode/ast? child)
                  (ncp! (visit child))
                  (astnode/primitivevalue? child)
                  (ncp! 1)
                  (astnode/nilvalue? child)
                  (ncp! 1)
                  )))
            (inc @node-count)))]
    (visit-node (snippet/snippet-root template))))

(defn templategroup-node-count [templategroup]
  (let [templates (snippetgroup/snippetgroup-snippetlist templategroup)]
    (reduce + (map template-node-count templates))))

(defn query-templategroup
  "Look for matches of a template group
   @param templategroup  The template group to be matched"
  [templategroup]
  (let [templates (snippetgroup/snippetgroup-snippetlist templategroup)
        process-template (fn [[template & rest-templates] bindings-list index]
                           (let [matchmap (query-template template bindings-list)
                                 new-bindings-list (with-meta 
                                                     (merge-bindings 
                                                      matchmap 
                                                      ; Generate a unique variable name for the root, but should be the same every time this template is queried
                                                      (str "?" (util/classname (snippet/snippet-root template)) index) 
                                                      )
                                                     {:node-count (+ (:node-count (meta bindings-list)) (:node-count (meta matchmap)))})] 
                             (if (nil? rest-templates)
                               new-bindings-list
                               (recur rest-templates new-bindings-list (inc index)))))]
    (process-template templates (with-meta [{}] {:node-count 0}) 0)))

(defn query-templategroup-txt
  "Look for matches of a template group, in its textual form"
  [templategroup-string]
  (let [[var-map templategroup-string-novars] (parsing-template/parse-var-defs templategroup-string)
        value-combinations (if (empty? var-map) [[]] (util/combinations var-map))
        
        inline-values ; Inline concrete values for all predetermined metavariables 
        (fn [values]
          (reduce 
            (fn [inlined-string [idx value]]
              (let [var (nth (keys var-map) idx)]
                (if (coll? value)
                  ; Is this variable a list type?
                  (let [list-size (count value)]
                    (reduce
                      (fn [inlined-string [idx element]]
                        (clojure.string/replace 
                          inlined-string 
                          (re-pattern (str "\\" var "_" idx)) ; TODO Don't replace occurences that appear in a directives list!
                          (str "[" element "]@[equals " var "_" idx "]")))
                      inlined-string
                      (map-indexed (fn [idx element] [idx element]) value)))
                  ; Non-list variables
                  (clojure.string/replace 
                    inlined-string 
                    (re-pattern (str "\\" var)) ; TODO Don't replace occurences that appear in a directives list!
                    (str "[" value "]@[equals " var "]"))))) ; Also attach an equals directive, such that it becomes a normal metavariable
                
                
            templategroup-string-novars
            (map-indexed (fn [idx value] [idx value]) values)))]
    (reduce 
      (fn [matches values]
        (let [inlined-templategroup-string (inline-values values)
              inlined-templategroup (parsing-template/parse-templategroup inlined-templategroup-string)]
          (clojure.set/union matches (into #{} (query-templategroup inlined-templategroup))))) ; Should make a custom union that ignores variable names.. cause the roots' names are gen-symmed
      #{}
      value-combinations)
    ))

(comment
  
  (defn slurp-from-resource [pathrelativetobundle]
    (persistence/slurp-snippetgroup (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))
  
  (defn run-test-batch [path-testfn-triples]
    (doseq [[path queryfn testfn] path-testfn-triples]
      (let [input (if (or (.endsWith path ".ekt") (.endsWith path ".ekx"))
                    (slurp-from-resource path)
                    (slurp (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile path))
                    )
            start (. System (nanoTime))
            matches (queryfn input)
            end (/ (double (- (. System (nanoTime)) start)) 1000000.0)
            test-result (testfn matches)]
        (if test-result
          (println "pass (" (count matches) "matches -" end "ms -" path ")")
          (println "FAIL (" (count matches) "matches -" end "ms -"  path ")")))))
  
  ; Matching2 tests - composite visitor project
  (run-test-batch
    [["/resources/EkekoX-Specifications/matching2/mini.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/sysout.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/method.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/method-childstar.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/method-childstar2.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/method-childstar3.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/sysout-wcard.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/sysout-var.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/method-metavar.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-invokes.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-invokes2.ekt" query-templategroup empty?]
     ["/resources/EkekoX-Specifications/matching2/cls-constructs.ekt" query-templategroup empty?]
     ["/resources/EkekoX-Specifications/matching2/cls-setmatching.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-setmatching2.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-setmatching3.ekt" query-templategroup empty?]
     ["/resources/EkekoX-Specifications/matching2/cls-nestedsetmatching.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-nestedsetmatching2.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/method-isolateexpr.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-isolateexpr.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-overrides.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-type.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-subtype.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-refersto.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/method-combo.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/matching2/cls-regex.ekt" query-templategroup not-empty]])
  
  ; Text parser tests - composite visitor project
  (run-test-batch
    [["/resources/EkekoX-Specifications/parsing/sysout.ekt.txt" query-templategroup-txt not-empty]
      ["/resources/EkekoX-Specifications/parsing/sysout_wcard.ekt.txt" query-templategroup-txt not-empty]
      ["/resources/EkekoX-Specifications/parsing/sysout_predef.ekt.txt" query-templategroup-txt not-empty]
      ["/resources/EkekoX-Specifications/parsing/sysout_predeflist.ekt.txt" query-templategroup-txt not-empty]])
  
  (run-test-batch ; JHotDraw project
    [["/resources/EkekoX-Specifications/experiments/templatemethod-jhotdraw/solution.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/experiments/prototype-jhotdraw/solution.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/experiments/observer-jhotdraw/solution.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/experiments/strategy-jhotdraw/solution3.ekt" query-templategroup not-empty]
     ["/resources/EkekoX-Specifications/experiments/factorymethod-jhotdraw/solution_take4-reorder2.ekt" query-templategroup not-empty]])
  
  (def templategroup
    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method.ekt")
;    (:lhs (slurp-from-resource "/resources/EkekoX-Specifications/scam-demo/scam_demo3.ekx"))
    )
  
  (def templ-txt (slurp (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile 
                         "/resources/EkekoX-Specifications/parsing/sysout_predef.ekt.txt")))
  (inspector-jay.core/inspect (query-templategroup-txt templ-txt))
  )