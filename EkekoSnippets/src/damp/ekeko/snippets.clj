(ns 
  ^{:doc "Snippet-driven querying of Java projects."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets
  (:refer-clojure :exclude [== type])
  (:use clojure.core.logic)
  (:import [org.eclipse.jdt.core.dom ASTParser AST ASTNode ASTNode$NodeList CompilationUnit])
  (:use [damp ekeko])
  (:use [damp.ekeko logic])
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

(declare jdt-parse-snippet)

(defn 
  parse-snippet-statements
  "Parses the given string as a sequence of Java statements."
  [snippet]
  (jdt-parse-snippet snippet (ASTParser/K_STATEMENTS)))

(defn 
  parse-snippet-expression 
  "Parses the given string as a Java expression."
  [snippet]
  (jdt-parse-snippet snippet (ASTParser/K_EXPRESSION)))

(defn 
  parse-snippet-unit 
  "Parses the given string as a Java compilation unit."
  [snippet]
  (jdt-parse-snippet snippet (ASTParser/K_COMPILATION_UNIT)))

(defn 
  parse-snippet-declarations 
  "Parses the given string as a sequence of Java class body declarations."
  [snippet]
  (jdt-parse-snippet snippet (ASTParser/K_CLASS_BODY_DECLARATIONS)))

(defn 
  jdt-parse-snippet 
  "Parses the given string as a Java construct of the given kind
   (expression, statements, class body declarations, compilation unit),
   or as the first kind for which the JDT parser returns a valid ASTNode."
  ([^String snippet snippet-kind]
    (let [parser (ASTParser/newParser AST/JLS3)]                
      (.setSource parser (.toCharArray snippet))
      (.setKind parser snippet-kind)
      (.createAST parser nil)))
  ([snippet]
    (let [kinds (list (ASTParser/K_EXPRESSION) (ASTParser/K_STATEMENTS) (ASTParser/K_CLASS_BODY_DECLARATIONS) (ASTParser/K_COMPILATION_UNIT))]
      (some (fn [k] 
              (let [result (jdt-parse-snippet snippet k)]
                (when (jdt-node-valid? result)
                  result)))
            kinds))))


; Actual snippets
; ---------------


(defn 
  gen-lvar
  "Generates a unique symbol starting with ?v
   (i.e., a symbol to be used as the name for a logic variable)."
  [] 
  (gensym '?v))


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

(defrecord 
  Snippet
  [ast ast2var ast2groundf ast2constrainf])

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
  ast-primitive-as-string
  [primitive]
  ;used on things like Modifier.ModifierKeyword                        
  ;could dispatch on this as well
  (cond (nil? primitive)  nil
        (or (true? primitive)  (false? primitive)) primitive
        :else (str "\"" (.toString primitive) "\"")))

(defn 
  make-constraining-function
  [template-ast]
  (if 
    (instance? ASTNode template-ast)
    (let [template-keyw (ekeko-keyword-for-class-of template-ast)
          template-properties (node-ekeko-properties template-ast)]
      (fn [snippet]
        (let [var-match (snippet-var-for-node snippet template-ast)
              child-conditions (for [[property-keyw retrievalf] 
                                     (seq template-properties)
                                     :let [child (retrievalf)
                                           var-child (or (snippet-var-for-node snippet child)
                                                         (ast-primitive-as-string child))]]
                                 `(has ~property-keyw ~var-match ~var-child))]
          `((ast ~template-keyw ~var-match)
             ~@child-conditions))))
    (let [template-list-size (.size template-ast)]
      (fn [snippet]
        (let [var-match (snippet-var-for-node snippet template-ast)
              element-conditions (for [element template-ast
                                       :let [idx-el (.indexOf template-ast element)
                                             var-el (or 
                                                     (snippet-var-for-node snippet element)
                                                     (ast-primitive-as-string element))]]
                                   `(equals ~var-el (get ~var-match ~idx-el)))]
          `((equals ~template-list-size (.size ~var-match))
             ~@element-conditions))))))


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


(defn walk-jdt-node [ast node-f list-f primitive-f]
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
    
    
(defn jdt-node-as-snippet [n]
  (defn assoc-snippet-ast [snippet ast]
    (->
      snippet
      (assoc-in [:ast2var ast] (gen-lvar))
      (assoc-in [:ast2groundf ast] (make-grounding-function ast))
      (assoc-in [:ast2constrainf ast] (make-constraining-function ast))))
  (let [snippet (atom (Snippet. n {} {} {}))]
    (walk-jdt-node 
      n
      (fn [ast] (swap! snippet assoc-snippet-ast ast))
      (fn [ast-list] (swap! snippet assoc-snippet-ast ast-list))
      (fn [primitive]))
    @snippet))
    
  

(defn 
  snippet-query
  "Returns the Ekeko query that will retrieve matches for the given snippet."
  [snippet]
  (defn conditions [ast-or-list]
    (concat ((snippet-grounder-for-node snippet ast-or-list) snippet)
            ((snippet-constrainer-for-node snippet ast-or-list) snippet)))
  (let [ast (:ast snippet)
        query (atom '())]
    (walk-jdt-node 
      ast
      (fn [ast] (swap! query concat (conditions ast)))
      (fn [ast-list] (swap! query concat (conditions ast)))
      (fn [primitive]))
    @query))


(defn
  eval-snippet
  [snippet]
  (let [conditions (snippet-query snippet)
        ast-var (snippet-var-for-node snippet (:ast snippet))
        vars (disj (ekeko-extract-vars conditions) ast-var)
        query `(ekeko* [~ast-var]
                       (fresh [~@vars]
                              ~@conditions))]
    (println "Evaluating: " query)
    (eval query)))
      
(defn eval-snippet-condition-by-condition [snippet]
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



; a compilationUnit
; (ast :CompilationUnit ?ast)
; (child ?ast ... ?child1)
; (child ?ast ... ?childn)



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

  
  (snippet-query (jdt-node-as-snippet (parse-snippet-expression "fLocator.locate(owner())")))
  
  )








                  
  
  

