(ns 
  ^{:doc "Snippet-driven querying of Java projects."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets
  (:refer-clojure :exclude [== type])
  (:use clojure.core.logic)
  (:import [org.eclipse.jdt.core.dom ASTParser AST ASTNode ASTNode$NodeList CompilationUnit]
           [org.eclipse.jface.viewers TreeViewerColumn]
           [org.eclipse.swt SWT]
           [org.eclipse.ui IWorkbench PlatformUI IWorkbenchPage IWorkingSet IWorkingSetManager]
           [org.eclipse.swt.widgets Display])
  (:use [damp ekeko])
  (:use [damp.ekeko logic gui])
  (:use [damp.ekeko.jdt reification astnode]))


; Parsing strings as Java code
; ----------------------------

(defn 
  jdt-node-malformed?
  "Returns whether a JDT ASTNode has its MALFORMED bit set or is a CU which has an IProblem which is an error."
  [^ASTNode n]
   (or (not= 0 (bit-and (.getFlags n) (ASTNode/MALFORMED)))
       (and (instance? CompilationUnit n)
            (some (fn [p] (.isError p))
                  (.getProblems n)))))

(defn 
  jdt-node-valid? 
  "Returns whether a JDT ASTNode is valid (i.e., is not malformed)."
  [n]
  (not (jdt-node-malformed? n)))

(declare jdt-parse-string)

(defn 
  parse-string-statements
  "Parses the given string as a sequence of Java statements."
  [string]
  (jdt-parse-string string (ASTParser/K_STATEMENTS)))

(defn 
  parse-string-statement
  "Parses the given string as a sequence of Java statements and return first statement."
  [string]
  (let [ast-node (parse-string-statements string)]
    (first (first (damp.ekeko/ekeko [?stat]
                                  (fresh [?stats]
                                  (ast :Block ast-node)
                                  (has :statements ast-node ?stats)
                                  (equals ?stat (.get ?stats 0))))))))

(defn 
  parse-string-expression 
  "Parses the given string as a Java expression."
  [string]
  (jdt-parse-string string (ASTParser/K_EXPRESSION)))

(defn 
  parse-string-unit 
  "Parses the given string as a Java compilation unit."
  [string]
  (jdt-parse-string string (ASTParser/K_COMPILATION_UNIT)))

(defn 
  parse-string-declarations 
  "Parses the given string as a sequence of Java class body declarations."
  [string]
  (jdt-parse-string string (ASTParser/K_CLASS_BODY_DECLARATIONS)))

(defn 
  jdt-parse-string 
  "Parses the given string as a Java construct of the given kind
   (expression, statements, class body declarations, compilation unit),
   or as the first kind for which the JDT parser returns a valid ASTNode."
  ([^String string string-kind]
    (let [parser (ASTParser/newParser AST/JLS3)]                
      (.setSource parser (.toCharArray string))
      (.setKind parser string-kind)
      (.createAST parser nil)))
  ([string]
    (let [kinds (list (ASTParser/K_EXPRESSION) (ASTParser/K_STATEMENTS) (ASTParser/K_CLASS_BODY_DECLARATIONS) (ASTParser/K_COMPILATION_UNIT))]
      (some (fn [k] 
              (let [result (jdt-parse-string string k)]
                (when (jdt-node-valid? result)
                  result)))
            kinds))))


; Actual snippets
; ---------------


(defn 
  gen-lvar
  "Generates a unique symbol starting with ?v
   (i.e., a symbol to be used as the name for a logic variable)."
  ([] (gen-lvar "v"))
  ([prefix] (gensym (str "?" prefix))))


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
; - ast2groundf: map from an AST node of the snippet to a function that generates "grounding" conditions. 
;   These will ground the corresponding logic variable to an AST node of the Java project
; - ast2constrainf:  map from an AST node of the snippet to a function that generates "constraining" conditions. 
;   These will constrain the bindings for the corresponding logic variable to an AST node of the Java project
;   that actually matches the AST node of the snippet. 
; - var2ast: map from a logic variable to the an AST node which is bound to its match
; - var2uservar: map from logic variable to the user defined logic variable 

(defrecord 
  Snippet
  [ast ast2var ast2groundf ast2constrainf var2ast var2uservar])

(defn 
  snippet-var-for-node 
  "For the given AST node of the given snippet, returns the name of the logic
   variable that will be bound to a matching AST node from the Java project."
  [snippet snippet-node]
  (get-in snippet [:ast2var snippet-node]))

(defn 
  snippet-grounder-for-node
  "For the given AST node of the given snippet, returns the function
   that will generate grounding conditions for the corresponding logic variable."
  [snippet template-ast]
  (get-in snippet [:ast2groundf template-ast]))

(defn 
  snippet-constrainer-for-node
  "For the given AST node of the given snippet, returns the function
   that will generate constraining conditions for the corresponding logic variable."
  [snippet template-ast]
  (get-in snippet [:ast2constrainf template-ast]))

(defn 
  snippet-node-for-var 
  "For the logic variable of the given snippet, returns the AST node  
   that is bound to a matching logic variable."
  [snippet snippet-var]
  (get-in snippet [:var2ast snippet-var]))

(defn 
  snippet-uservar-for-var 
  "For the givenlogis var of the given snippet, returns the name of the user defined logic variable."
  [snippet snippet-var]
  (get-in snippet [:var2uservar snippet-var]))

(defn 
  snippet-nodes
  "Returns all AST nodes of the given snippet."
  [snippet]
  (keys (:ast2var snippet)))

(defn 
  snippet-vars
  "Returns the logic variables that correspond to the AST nodes
   of the given snippet. These variables will be bound to matching
   AST nodes from the queried Java project."
  [snippet]
  (vals (:ast2var snippet)))


(defn
  make-epsilon-function
  "Returns a function that does not generate any conditions for the given AST node of a code snippet."
  [template-ast]
  (fn [template-owner]
    '()))


(comment

(defn 
  make-grounding-function
  "Returns a function that will generate grounding conditions for the given AST node of a code snippet:
   - if the AST node is a root node: ((ast :kind-of-node ?var-for-node-match)) 
   - if the AST node is the value of a property: ((has :property ?var-for-owner-match ?var-for-node-match))
   - it its AST node is the element of a list-valued property at index idx:  
     ((has :property ?var-for-owner-match ?var-for-list)
      (equals ?var-for-node-match (.get ?var-for-list idx))"
  [template-ast]
  (let [template-owner 
        ;owner (i.e., parent) of the AST node 
        (owner template-ast) 
        ;Ekeko keyword for AST nodes of the same kind (e.g., :MethodDeclaration)
        template-keyw
        (ekeko-keyword-for-class-of template-ast)  
        ]
    ;returned grounding function takes a Snippet datastructure as its argument
    ;note that it has an AST node of the snippet in its closure (template-ast)
    (fn [snippet] 
      (let [var-match
            ;logic variable to be bound to a matching ast node from the Java project
            (snippet-var-for-node snippet template-ast)] 
          (if 
            (nil? template-owner)
            ;root nodes of the AST have no owner, so candidate matches are all AST nodes of the same kind
            ;generates a list  with a single condition such as (ast :MethodDeclaration ?var-match)
            `((ast ~template-keyw ~var-match)) 
            ;;otherwise, a candidate match is the corresponding child of the match for the owner
            (let [;logic variable that will be bound to the match for the owner
                  var-match-owner
                  (snippet-var-for-node snippet template-owner)
                  ;property of the owner that has template-ast as its value
                  owner-property 
                  (owner-property template-ast) 
                  ;Ekeko keyword of the owner's property that has template-ast as its value 
                  ;to be used in conditions such as (has :name ?var-match-owner ?var-match)
                  owner-property-keyw 
                  (ekeko-keyword-for-property-descriptor owner-property)]
              ;dispatch over the property kind
              (cond
                ;property of owner has an ASTNode as its value 
                ;generate singleton list (has :owner-property-keyword ?var-match-owner ?var-match)
                (property-descriptor-child? owner-property) 
                `((has ~owner-property-keyw ~var-match-owner ~var-match))
                ;property of owner has a ASTNode$NodeList as its value
                (property-descriptor-list? owner-property) 
                (if 
                  ;snippet's node is the list itself
                  ;(note: could not use ASTNode$NodeList here, gives rise to following exception:
                  ;IllegalAccessError tried to access class org.eclipse.jdt.core.dom.ASTNode$NodeList from class damp.ekeko.snippets$make_grounding_function$fn__13774)
                  (instance? java.util.AbstractList template-ast)
                  `((has ~owner-property-keyw ~var-match-owner ~var-match))
                  ;;snippet's node is an element from the list
                  (let [;list in the snippet of which the node is an element
                        template-list 
                        (node-property-value template-owner owner-property)
                        ;logic variable that will be bound to a matching list from the Java project
                        var-list
                        (snippet-var-for-node snippet template-list)
                        ;index of template-ast in the list
                        template-position 
                        (.indexOf template-list template-ast)]
                    ;the actual match for the list element 
                    `((has ~owner-property-keyw ~var-match-owner ~var-list)
                       (equals ~var-match (.get ~var-list ~template-position)))))
                :else 
                (throw (Exception. "make-grounding-function should only be called for NodeLists and Nodes. Not simple values.")))))))))

  
(defn 
  make-constraining-function-exact
    "Returns a function that will generate constraining conditions for the given AST node of a code snippet:
     - for ASTNode$NodeList instances: ((equals size-of-snippet-node (.size ?var-for-node-match))
                                        (equals ?var-for-element0-match (get ?var-for-node-match 0))
                                        (equals ?var-for-element1-match ''primitive-valued-element-as-string''))
                                        ....
                                        (equals ?var-for-elementn-match (get ?var-for-node-match n))

     - for ASTNode instances: ((ast :kind-of-node ?var-for-node-match)  
                               (has :property1 ?var-for-node-match ?var-for-child1-match)
                               (has :property2 ?var-for-node-match ''primitive-valued-child-as-string''))
                               ....
                               (has :propertyn ?var-for-node-match ?var-for-childn-match))"
  [template-ast]
  (if 
    (instance? ASTNode template-ast)
    (let [;ekeko keyword for the node's kind (e.g., :MethodDeclaration)
          template-keyw 
          (ekeko-keyword-for-class-of template-ast)
          ;a map from keywords to functions, 
          ;keyword is the name of an AST node property (e.g., :name), 
          ;while the corrresponding function retrieves the value of this property
          template-properties
          (node-ekeko-properties template-ast)]
      (fn [snippet]
        (let [;logic variable that will be bound to a matching node from the Java project
              var-match
              (snippet-var-for-node snippet template-ast)
              ;logic conditions that constrain the match through its children
              ;the children of the match have to match the children of the snippet's node
              child-conditions 
              (for [[property-keyw retrievalf] 
                    (seq template-properties)
                    :let [;one child node of snippet's node
                          child 
                          (retrievalf) 
                          ;variable that will be bound to the corresponding child of the match
                          ;or the string representation of a primitive-valued child
                          var-child (or 
                                      (snippet-var-for-node snippet child)
                                      (ast-primitive-as-string child))]]
                `(has ~property-keyw ~var-match ~var-child))]
          `((ast ~template-keyw ~var-match)
             ~@child-conditions))))
    (let [template-list-size (.size template-ast)]
      (fn [snippet]
        (let [;logic variable that will be bound to a matching list from the Java project
              var-match 
              (snippet-var-for-node snippet template-ast)
              ;logic conditions that constrain the match through the list's elements
              ;the elements of the snippet's list and the project's list have to correspond one-to-one
              element-conditions 
              (for [element
                    template-ast
                    :let [;index of the element in the snippet's list
                          idx-el 
                          (.indexOf template-ast element)
                          ;variable that will be bound to the corresponding element of the match
                          ;or the string representation of a primitive-valued element
                          var-el (or 
                                   (snippet-var-for-node snippet element)
                                   (ast-primitive-as-string element))]]
                `(equals ~var-el (get ~var-match ~idx-el)))]
          `((equals ~template-list-size (.size ~var-match))
             ~@element-conditions))))))

)


(declare ast-primitive-as-string)

;grounding function

(defn
  gf-minimalistic
  "Only generates grounding conditions for the root node of the snippet."
  [snippet-ast]
  (let [snippet-ast-keyw (ekeko-keyword-for-class-of snippet-ast) ]
    (fn [snippet] 
      (if 
        (= snippet-ast (:ast snippet))
        (let [var-match (snippet-var-for-node snippet snippet-ast)] 
          `((ast ~snippet-ast-keyw ~var-match)))
        '()))))
  
(defn 
  gf-node-exact
  "Returns a function that will generate grounding conditions for the given AST node of a code snippet:
   For AST node is the value of a property: ((has :property ?var-for-owner-match ?var-for-node-match))"
  [snippet-ast]
  (fn [snippet] 
      (let [snippet-owner  (owner snippet-ast)
            var-match       (snippet-var-for-node snippet snippet-ast) 
            var-match-owner (snippet-var-for-node snippet snippet-owner)
            owner-property  (owner-property snippet-ast) 
            owner-property-keyw (ekeko-keyword-for-property-descriptor owner-property)]
        `((has ~owner-property-keyw ~var-match-owner ~var-match)))))

(defn 
  gf-node-deep
  "Returns a function that will generate grounding conditions for the given AST node of a code snippet:
   For AST node is the value of a property: ((child+ ?var-for-owner-match ?var-for-node-match))"
  [snippet-ast]
  (fn [snippet] 
      (let [snippet-owner  (owner snippet-ast)
            var-match       (snippet-var-for-node snippet snippet-ast) 
            var-match-owner (snippet-var-for-node snippet snippet-owner)]
        `((child+ ~var-match-owner ~var-match)))))

 ;still not sure whether gf-node-exact & gf-node-deep is necessarry, because it maybe belongs to cf
(defn 
  make-grounding-function
  [type]
  (cond 
    (= type :minimalistic)
    gf-minimalistic
    (= type :node-exact)
    gf-node-exact
    (= type :node-deep)
    gf-node-deep
    (= type :epsilon)
    make-epsilon-function
    :default
    (throw (Exception. (str "Unknown grounding function type: " type)))))

;constraining function

(defn 
  cf-node
    "Returns a function that will generate constraining conditions for the given AST node of a code snippet:
     For   ASTNode instances: ((ast :kind-of-node ?var-for-node-match)  
                               (has :property1 ?var-for-node-match ?var-for-child1-match)
                               (has :property2 ?var-for-node-match ''primitive-valued-child-as-string''))
                               ....
     If ?lvar exist then       (has :property2 ?var-for-node-match ?lvar))"
  [snippet-ast & [?lvar]]
  (fn [snippet]
    (let [snippet-keyw       (ekeko-keyword-for-class-of snippet-ast)
          snippet-properties (node-ekeko-properties snippet-ast)
          var-match          (snippet-var-for-node snippet snippet-ast)
          child-conditions 
              (for [[property-keyw retrievalf] 
                    (seq snippet-properties)
                    :let [child     (retrievalf) 
                          var-child (or 
                                      (snippet-var-for-node snippet child)
                                      (let [lvar (snippet-uservar-for-var snippet var-match)]
                                        (if (nil? lvar)
                                          (ast-primitive-as-string child)
                                          lvar)))]]
                `(has ~property-keyw ~var-match ~var-child))]
      (if 
        (= snippet-ast (:ast snippet))
        `(~@child-conditions)
        `((ast ~snippet-keyw ~var-match)
           ~@child-conditions)))))


(defn 
  cf-list-exact
    "Returns a function that will generate constraining conditions for the given AST node of a code snippet:
     For ASTNode$NodeList instances: (equals size-of-snippet-node (.size ?var-for-node-match))"
  [snippet-ast]
  (fn [snippet]
    (let [snippet-list-size (.size snippet-ast)
          var-match (snippet-var-for-node snippet snippet-ast)
          element-conditions 
          (for [element
                snippet-ast
                :let [idx-el (.indexOf snippet-ast element)
                      var-el (or 
                               (snippet-var-for-node snippet element)
                               (ast-primitive-as-string element))]]
            `(equals ~var-el (get ~var-match ~idx-el)))]
      `((equals ~snippet-list-size (.size ~var-match))
         ~@element-conditions))))

(defn 
  cf-list-contains
    "Returns a function that will generate constraining conditions for the given AST node of a code snippet:
     For ASTNode$NodeList instances: for each element in the list (contains ?var-for-node-match ?var-for-element)"
  [snippet-ast]
  (fn [snippet]
    (let [snippet-list-size (.size snippet-ast)
          var-match (snippet-var-for-node snippet snippet-ast)
          element-conditions 
          (for [element
                snippet-ast
                :let [var-el (or 
                               (snippet-var-for-node snippet element)
                               (ast-primitive-as-string element))]]
            `(contains ~var-match ~var-el))]
      `((equals ~snippet-list-size (.size ~var-match))
         ~@element-conditions))))

(defn 
  cf-exact
  [snippet-ast]
  (if 
    (instance? ASTNode snippet-ast)
    (cf-node snippet-ast)
    (cf-list-exact snippet-ast)))


(defn
  make-constraining-function
  [type]
  (cond
    (= type :exact)
    cf-exact
    (= type :node)
    cf-node
    (= type :list-exact)
    cf-list-exact
    (= type :list-contains)
    cf-list-contains
    (= type :epsilon)
    make-epsilon-function
    :default
    (throw (Exception. (str "Unknown constraining function type: " type))))) 


(defn 
  ast-primitive-as-string
  "Returns the string representation of a primitive-valued JDT node (e.g., instances of Modifier.ModifierKeyword)."
  [primitive]
  ;could dispatch on this as well
  (cond (nil? primitive) 
        nil
        (or (true? primitive) (false? primitive))
        primitive
        (number? primitive)
        primitive
        :else  (.toString primitive) ))



(defn 
  walk-jdt-node
  [ast node-f list-f primitive-f]
  (defn walk-jdt-nodes [nodes]
    (when-not (empty? nodes)
      (let [ast (first nodes)
            others (rest nodes)]
        (cond 
          (instance? ASTNode ast) 
          (do
            (node-f ast)
            (recur (concat (node-children ast) others)))
          (instance? java.util.AbstractList ast) 
          (do 
            (list-f ast)
            (recur (concat ast others)))
          :else 
          (do 
            (primitive-f ast)
            (recur others))))))
  (walk-jdt-nodes (list ast)))
    
    
(defn
  class-simplename
  [clazz]
  (last (.split #"\." (.getName clazz))))

(defn
  gen-readable-lvar-for-node
  [ast]
  (gen-lvar
    (cond 
      (instance? java.util.AbstractList ast)
      "List"
      :else
      (class-simplename (class ast)))))

(defn 
  jdt-node-as-snippet
  [n]
  (defn assoc-snippet-ast [snippet ast]
    (let [lvar (gen-readable-lvar-for-node ast)]
      (->
        snippet
        (assoc-in [:ast2var ast] lvar)
        (assoc-in [:ast2groundf ast] :minimalistic)
        (assoc-in [:ast2constrainf ast] :exact)
        (assoc-in [:var2ast lvar] ast))))
  (let [snippet (atom (Snippet. n {} {} {} {} {}))]
    (walk-jdt-node 
      n
      (fn [ast] (swap! snippet assoc-snippet-ast ast))
      (fn [ast-list] (swap! snippet assoc-snippet-ast ast-list))
      (fn [primitive]))
    @snippet))
    
;query

(defn 
  snippet-query
  "Returns the Ekeko query that will retrieve matches for the given snippet."
  [snippet]
  (defn 
    conditions
    [ast-or-list]
    (concat (((make-grounding-function (snippet-grounder-for-node snippet ast-or-list)) ast-or-list) snippet)
            (((make-constraining-function (snippet-constrainer-for-node snippet ast-or-list)) ast-or-list) snippet)))
  (let [ast (:ast snippet)
        query (atom '())]
    (walk-jdt-node 
      ast
      (fn [ast-node] (swap! query concat (conditions ast-node)))
      (fn [ast-list] (swap! query concat (conditions ast-list)))
      (fn [primitive]))
    @query))


;todo: fix code duplication
(defn
  query-by-snippet*
  "Queries the Ekeko projects for matches for the given snippet. Opens Eclipse view on results."
  [snippet]
  (let [conditions (snippet-query snippet)
        ast-var (snippet-var-for-node snippet (:ast snippet))
        vars (disj (ekeko-extract-vars conditions) ast-var)
        query `(ekeko* [~ast-var]
                       (fresh [~@vars]
                              ~@conditions))]
    (println "Evaluating: " query)
    (eval query)))

(defn
  query-by-snippet
  "Queries the Ekeko projects for matches for the given snippet."
  [snippet]
  (let [conditions (snippet-query snippet)
        ast-var (snippet-var-for-node snippet (:ast snippet))
        vars (disj (ekeko-extract-vars conditions) ast-var)
        query `(ekeko [~ast-var]
                       (fresh [~@vars]
                              ~@conditions))]
    (println "Evaluating: " query)
    (eval query)))

      
(defn 
  query-by-snippet-condition-by-condition
  [snippet]
  (let [ast-var (snippet-var-for-node snippet (:ast snippet))
        conditions (snippet-query snippet)
        vars (disj (ekeko-extract-vars conditions) ast-var)]
    (defn eval-conditions [remaining]
      (when-not (nil? remaining)
        (let [query `(ekeko* [~ast-var]
                             (fresh [~@vars]
                                    ~@remaining))]
          (eval query)
          (recur (butlast remaining)))))
    (eval-conditions conditions)))  

;operator

(defn update-groundf 
  "Update grounding function of a given node in a given snippet with new grounding function of given type
   Example: (update-groundf snippet node :node-deep)"
  [snippet node type]
  (update-in snippet [:ast2groundf node] (fn [x] type)))

(defn update-constrainf 
  "Update constraining function of a given node in a given snippet with the new constraining function of given type
   Example: (update-constrainf snippet node :list-contains)"
  [snippet node type]
  (update-in snippet [:ast2constrainf node] (fn [x] type)))


(defn ignore-elements-sequence 
  "Ignore elements sequence in a given node(list)"
  [snippet node]
  (update-constrainf snippet node :list-contains))

(defn introduce-logic-variable 
  "Introduce logic variable to a given node"
  [snippet node uservar]
  (let [newsnippet (assoc-in snippet [:var2uservar (snippet-var-for-node snippet node)] uservar)]
    (update-constrainf newsnippet node :node)))



; Eclipse view on snippets
; ------------------------


(defn
  snippetviewer-elements
  [snippet input]
  ;roots of treeview
  (if 
    (= input snippet)
    (to-array [(:ast snippet)])
    nil))


(defn
  snippetviewer-children
  [snippet p]
  ;treeview children of given treeview parent
  (cond 
     (instance? ASTNode p) 
     (to-array (node-children p)) 
     (instance? java.util.AbstractList p) 
     (to-array (seq p))
     :else 
     (to-array [])))


(defn
  snippetviewer-parent
  [snippet c]
  ;treeview parent of given treeview child
  (cond 
    (instance? ASTNode c) 
    (owner c) 
    (instance? java.util.AbstractList c) 
    (owner c)
    :else 
    nil))

(defn
  snippetviewercolumn-kind
  [snippet element]
  (class-simplename (class element)))

(defn
  snippetviewercolumn-variable
  [snippet element]
  (str (snippet-var-for-node snippet element)))

(defn
  snippetviewercolumn-grounder
  [snippet element]
  (str (snippet-grounder-for-node snippet element)))

(defn
  snippetviewercolumn-constrainer
  [snippet element]
  (str (snippet-constrainer-for-node snippet element)))

   
(def snippet-viewer-cnt (atom 0))

(defn 
  open-snippet-viewer
  [snippet]
  (let [page (-> (PlatformUI/getWorkbench)
               .getActiveWorkbenchWindow ;nil if called from non-ui thread 
               .getActivePage)
        qvid (damp.ekeko.SnippetViewer/ID)
        uniqueid (str @snippet-viewer-cnt)
        viewpart (.showView page qvid uniqueid (IWorkbenchPage/VIEW_ACTIVATE))]
    (swap! snippet-viewer-cnt inc)
    (.setViewID viewpart uniqueid)
    (.setInput (.getViewer viewpart) snippet)
    viewpart))



(defn
  view-snippet
  [snippet]
  (eclipse-uithread-return (fn [] (open-snippet-viewer snippet))))


    


; next: use /* */ after or before concrete syntax of a node to specity what condition generator function to use for that node (since jdt uses that syntax)


(comment 
  (use 'damp.ekeko.snippets)
  (in-ns 'damp.ekeko.snippets)
  
  ;;Example REPL session against JHotDraw51
  
  
  ;;Example 1: snippet linked to program it originates from
  ;;-------------------------------------------------------
  
  ;; select an AST node from the program as the starting point for the snippet
  (def selected (first (first 
                          (damp.ekeko/ekeko [?m] 
                                            (ast :MethodDeclaration ?m) 
                                            (fresh [?n]
                                                   (has :name ?m ?n)
                                                   (has :identifier ?n "LocatorHandle"))))))
  
  ;; create a snippet from the selected AST node 
  (def snippet (jdt-node-as-snippet selected))
  
  ;; convert the snippet to an Ekeko query
  (def query (snippet-query snippet))
  
  ;;Example 2: snippet originating from a string 
  ;;--------------------------------------------
 
  (snippet-query (jdt-node-as-snippet (parse-string-expression "fLocator.locate(owner())")))
    
  
  ;;Example 3: introduce logic variable 
  ;;--------------------------------------------

  (def astnode (parse-string-statement "return foo;"))
  (def snippet (jdt-node-as-snippet astnode))
  (def query (snippet-query snippet))
  
  ;(damp.ekeko.jdt.reification/ast :ReturnStatement ?ReturnStatement14017) --> still double
  ;(damp.ekeko.jdt.reification/ast :ReturnStatement ?ReturnStatement14017) 
  ;(damp.ekeko.jdt.reification/has :expression ?ReturnStatement14017 ?SimpleName14018) 
  ;(damp.ekeko.jdt.reification/ast :SimpleName ?SimpleName14018) 
  ;(damp.ekeko.jdt.reification/has :identifier ?SimpleName14018 "foo")
  
  (def snippet2 (introduce-logic-variable snippet (snippet-node-for-var snippet '?SimpleName14018) '?vfoo))
  (def query2 (snippet-query snippet2))
  ;(damp.ekeko.jdt.reification/has :identifier ?SimpleName14018 ?lvar)

  
  ;;Example 4: ignore elements sequence of the list
  ;;--------------------------------------------------------------------------------

  (def astnode (parse-string-statement "{int y; int x;}"))
  (def snippet (jdt-node-as-snippet astnode))
  (def query (snippet-query snippet))
  ;....
  ;(damp.ekeko.jdt.reification/has :statements ?Block14974 ?List14975) 
  ;(damp.ekeko.logic/equals 2 (.size ?List14975)) 
  ;(damp.ekeko.logic/equals ?VariableDeclarationStatement14976 (clojure.core/get ?List14975 0)) 
  ;(damp.ekeko.logic/equals ?VariableDeclarationStatement14982 (clojure.core/get ?List14975 1))
  ;.... 
  
  (def snippet2 (ignore-elements-sequence snippet (snippet-node-for-var snippet '?List14975)))  
  (def query2 (snippet-query snippet2))
  ;....
  ;(damp.ekeko.jdt.reification/has :statements ?Block14974 ?List14975) 
  ;(damp.ekeko.logic/equals 2 (.size ?List14975)) 
  ;(damp.ekeko.logic/contains ?List14975 ?VariableDeclarationStatement14976) 
  ;(damp.ekeko.logic/contains ?List14975 ?VariableDeclarationStatement14982)  
  ;....


  ;;Misc Examples
  ;;-------------
  
  
  (def s (jdt-node-as-snippet(parse-string-expression "x.m()")))             ;ok
  (def s (jdt-node-as-snippet(parse-string-statement "this.methodC();")))    ;ok
  (def s (jdt-node-as-snippet(parse-string-expression "o.f")))               ;ok
  (def s (jdt-node-as-snippet(parse-string-statement "o.f = x.m();")))       ;not ok
  
  (query-by-snippet s)
  
  (view-snippet s)

  
)
