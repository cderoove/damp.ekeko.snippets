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
  non-relation-directive?
  "Tests that a BoundDirective is not a directive that establishes a relation 
   (between an AST node and a logic variable)."
  [bd]
  (or
    (directives/bounddirective-for-directive [bd] matching/directive-exact) 
    (directives/bounddirective-for-directive [bd] matching/directive-replacedbywildcard)
    (directives/bounddirective-for-directive [bd] matching/directive-ignore)
    (directives/bounddirective-for-directive [bd] matching/directive-protect)
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
  ([matches]
    (matchmap-create matches [{}]))
  ([matches bindings-list]
    (zipmap 
      matches
      (for [match matches]
        {match (PositionProperties. nil bindings-list) }))))

(defn- merge-bindings
  "Merges all lists of lvar bindings within a matchmap into one list of bindings
   Additionally, for each bindings map we also associate the corresponding match
   under the logic variable templ-name"
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
                     (filter (fn [val] (constraintfn ast-node val)) values))]
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
  [positionmap match updatefn]
  (reduce 
    (fn [cur-positionmap [cur-position props]]
      (let [new-positions (remove nil? (updatefn cur-position))
;            tmp (if (some (fn [pos] (nil? pos)) new-positions)
;                  (println "!!!!"))
            
            ]
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
      (let [new-pmap (positionmap-updatepositions positionmap match updatefn)]
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
  ; TODO Maybe this fn can be cleaned up a bit..
  (let [bds (snippet/snippet-bounddirectives-for-node template child-node)
        owner-prop (astnode/owner-property child-node)
        ignore-test (fn [template-elem]
                      (if (and 
                            (astnode/ast? template-elem)
                            (directives/bounddirective-for-directive 
                              (snippet/snippet-bounddirectives-for-node template template-elem) 
                              matching/directive-ignore))
                        (first (snippet/snippet-node-children template template-elem))
                        template-elem))
        ]
    (cond 
      ; Child* (normal nodes only)
      (directives/bounddirective-for-directive bds matching/directive-child*)
      (matchmap-updatepositions 
        matchmap
        (fn [ast-node]
          (let [ast-child (astnode/node-property-value ast-node owner-prop)]
            (astnode/reachable-nodes-of-type ast-child (class ast-child)))
          ))
      ; Match|set (list nodes only)
      (directives/bounddirective-for-directive bds matching/directive-consider-as-set|lst)
      (matchmap-updatepositions
        matchmap
        (fn [ast-node]
          (let [template-list (astnode/value-unwrapped child-node)
                template-element (ignore-test (.get template-list index))
                ast-list (astnode/node-property-value ast-node owner-prop)
                bds-element (snippet/snippet-bounddirectives-for-node template template-element)
                elements-of-type (if (directives/bounddirective-for-directive bds-element matching/directive-child*) ; May not filter on type if there's a child*
                                   ast-list
                                   (filter 
                                     (fn [element] (= (class element) (class template-element)))
                                     ast-list))
                ]
            (if (directives/bounddirective-for-directive bds-element matching/directive-child*)
              (apply concat
                     (for [ast-element elements-of-type] 
                       (astnode/reachable-nodes-of-type ast-element (class template-element))))
              
              elements-of-type))))
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
                      template-element (ignore-test (.get template-list index))
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
    matchmap))

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
             supers (if (nil? hierarchy)
                      []
                      (.getAllSupertypes ^ITypeHierarchy hierarchy itype))]
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
           (javaprojectmodel/ancestor-methods ast-node)
           ))
       (fn [ast-node]
         (javaprojectmodel/ancestor-methods ast-node)
         
;         (javaprojectmodel/method-overriders ast-node)
         )]
      
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
             (conj ancestors itype))))
        (fn [ast-node]
          (let [itype (get-type ast-node)
                ancestors (get-all-ancestors itype)]
            (conj ancestors itype)))]
      
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
;         (print "?")
         (let [binding (cond
                         (or (instance? FieldAccess ast-node) (instance? SuperFieldAccess ast-node))
                         (.resolveFieldBinding ast-node)
                         :rest
                         (.resolveBinding ast-node))]
           (= val (javaprojectmodel/binding-to-declaration binding))))
       (fn [ast-node]
;         (print "!")
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
           lvar (directives/directiveoperandbinding-value (second (directives/bounddirective-operandbindings bd))) ;(.getOperand (second (.getOperandBindings bd)))
           [constraintfn generatefn] (directive-constraints directive)]
          (matchmap-checkconstraint cur-matchmap lvar constraintfn generatefn)))
      matchmap
      bds)))

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

;(defn- inf [obj]
;  (if @fst
;    (do
;      (swap! fst (fn [x] false))
;      (inspector-jay.core/inspect obj))))

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
  (let [template-node 
        (if (and 
              (astnode/ast? templ-node)
              (directives/bounddirective-for-directive 
                (snippet/snippet-bounddirectives-for-node template templ-node) 
                matching/directive-ignore))
          (first (snippet/snippet-node-children template templ-node))
          templ-node)
        
;        dbg
;        (if (not= 0 (count (keys matchmap)))
;          (do
;            (println "%%%" (count (keys matchmap)))
;            (println template-node)
;            (println (for [match (keys matchmap)] (for [curpos (keys (get matchmap match))] (str "$" (.toString curpos)))))
;            ))
;        
;        dbg2
;        (let [f (get (first (keys matchmap)) matchmap )
;              p (get (first (keys f)) f)] 
;          (if (empty? (:list-bindings p))
;            (println "#" (:list-bindings p))))

        check-directives-only (check-directives-only? template template-node)
        node-count (atom 0)
        nci (fn [matchmap] 
              (swap! node-count (fn [n] 
                                  (if (nil? (:node-count (meta matchmap))) ; Only the return value of process-node has :node-count
                                    (if (= 0 (count matchmap)) n (inc n)) ; For primitive/null nodes
                                    (+ n (:node-count (meta matchmap)))))) 
              matchmap)
        
        ; 1 - Check node type
        matchmap-1 
        (if check-directives-only
          matchmap
          (matchmap-filter matchmap (fn [ast-node] (= (class ast-node) (class template-node)))))
        
        
        ; 2 - Check directives (only considering directives that do not affect navigation!)
        matchmap-2 (check-directives template template-node matchmap-1)
        
        ; 3 - Check node children (taking into account navigation directives!)
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
                  (reduce 
                    (fn [cur-mmap [index list-element]]
                      (let [pos-matchmap (determine-next-positions template child cur-mmap index)
                            new-mmap (nci (process-node template list-element pos-matchmap))
                            final-mmap (return-to-previous-position new-mmap cur-mmap)]
                        final-mmap))
                    (check-directives template child cur-matchmap)
                    (let [elements (astnode/value-unwrapped child)]
                      (map-indexed (fn [idx elem] [idx elem]) elements ))))
                ; Regular nodes
                (astnode/ast? child)
                (let [pos-matchmap (determine-next-positions template child cur-matchmap 0)
                      new-mmap (nci (process-node template child pos-matchmap))
                      final-mmap (return-to-previous-position new-mmap cur-matchmap)]
                  final-mmap)
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
                :rest (println "!!!")))
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
    (rst)
    (let [root (snippet/snippet-root template)
         root-type (astnode/ekeko-keyword-for-class-of root)
         matches (ast/nodes-of-type root-type) ; (damp.ekeko/ekeko [?m] (ast/ast root-type ?m))
         matchmap (with-meta 
                    (matchmap-create matches bindings-list)
                    {:node-count 0})]
     (process-node template root matchmap))))

(defn template-node-count [template]
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
        process-template (fn [[template & rest-templates] bindings-list]
                           (let [matchmap (query-template template bindings-list)
                                 new-bindings-list (with-meta 
                                                     (merge-bindings 
                                                      matchmap 
                                                      (util/gen-readable-lvar-for-value|classbased (snippet/snippet-root template)))
                                                     {:node-count (+ (:node-count (meta bindings-list)) (:node-count (meta matchmap)))})] 
                             (if (nil? rest-templates)
                               new-bindings-list
                               (recur rest-templates new-bindings-list))))]
    (process-template templates (with-meta [{}] {:node-count 0}))))

(comment ; All tests use the CompositeVisitor project
  
  (defn slurp-from-resource [pathrelativetobundle]
    (persistence/slurp-snippetgroup (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))
  (def templategroup
;    (slurp-from-resource "/resources/EkekoX-Specifications/experiments/factorymethod-jhotdraw/solution_take4-reorder.ekt")
;    (slurp-from-resource "/resources/EkekoX-Specifications/experiments/strategy-jhotdraw/solution3.ekt")
;    (slurp-from-resource "/resources/EkekoX-Specifications/experiments/observer-jhotdraw/solution.ekt")
;    (slurp-from-resource "/resources/EkekoX-Specifications/experiments/prototype-jhotdraw/solution.ekt") ; OK
;    (slurp-from-resource "/resources/EkekoX-Specifications/experiments/templatemethod-jhotdraw/solution.ekt")
    (slurp-from-resource "/resources/EkekoX-Specifications/invokes.ekt")
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method-combo.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls-refersto.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls-subtype.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls-type.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls-overrides.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls-isolateexpr.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method-isolateexpr.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls-setmatching.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/jhot.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls-constructs.ekt") ; OK! (Artificial example.. must add constructor call in original source..)
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls-invokes2.ekt") ; OK! (Should not produce any matches)
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/cls-invokes.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/sysout-metavar.ekt") ; OK!
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/method-metavar.ekt") ; OK! replace-by-metavar on diff types of nodes
;    (slurp-from-resource "/resources/EkekoX-Specifications/matching2/sysout-var.ekt") ; OK!
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
  (def templategroup 
;    (:lhs (slurp-from-resource "/resources/EkekoX-Specifications/scam-demo/scam_demo0.ekx")) ; OK!
;    (:lhs (slurp-from-resource "/resources/EkekoX-Specifications/scam-demo/scam_demo1.ekx")) ; OK!
;    (:lhs (slurp-from-resource "/resources/EkekoX-Specifications/scam-demo/scam_demo2.ekx")) ; OK!
    (:lhs (slurp-from-resource "/resources/EkekoX-Specifications/scam-demo/scam_demo3.ekx"))
    )
  
  (inspector-jay.core/inspect
    (time (query-templategroup templategroup)))
  (inspector-jay.core/inspect
    (query-template (first (snippetgroup/snippetgroup-snippetlist templategroup))))
  
  (template-node-count (first (snippetgroup/snippetgroup-snippetlist templategroup)))
  (templategroup-node-count templategroup)
  (meta (query-template (first (snippetgroup/snippetgroup-snippetlist templategroup))))
  (meta (query-templategroup templategroup))
  
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