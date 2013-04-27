(ns damp.ekeko.snippets.rewrite
  ^{:doc "Rewrite AST in Compilation Unit."
    :author "Coen De Roover, Siltvani"}
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko.snippets 
             [gui :as gui]
             [querying :as querying]
             [operatorsrep :as operatorsrep]
             [representation :as representation]
             [util :as util]
             [parsing :as parsing]])
  (:require 
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt 
     [astnode :as astnode]
     [reification :as reification]])
  (:import 
    [damp.ekeko JavaProjectModel]
    [org.eclipse.jface.text Document]
    [org.eclipse.text.edits TextEdit]
    [org.eclipse.jdt.core ICompilationUnit IJavaProject]
    [org.eclipse.jdt.core.dom BodyDeclaration Expression Statement ASTNode ASTParser AST CompilationUnit]
    [org.eclipse.jdt.core.dom.rewrite ASTRewrite]))


; AST modification data

(defn make-rewrite-for-cu 
  [cu]
  (ASTRewrite/create (.getAST cu)))

(def current-rewrites (atom {}))

(defn reset-rewrites! []
  (reset! current-rewrites {}))

(defn current-rewrite-for-cu [cu]
  (if-let [rw (get @current-rewrites cu)]
    rw
    (let [nrw (make-rewrite-for-cu cu)]
      (swap! current-rewrites assoc cu nrw)
      nrw))) 

(defn apply-rewrite-to-node [rw node]
  (JavaProjectModel/applyRewriteToNode rw node))

(defn apply-rewrites []
  (doseq [[cu rw] @current-rewrites]
    (apply-rewrite-to-node rw cu)))

(defn apply-and-reset-rewrites []
  (do
    (apply-rewrites)
    (reset-rewrites!)))


; Interface

(defn remove-node 
  "Remove node."
  [node]
  (let [cu (.getRoot node)
        rewrite (current-rewrite-for-cu cu)
        list-rewrite (.getListRewrite rewrite (.getParent node) (.getLocationInParent node))]
    (.remove list-rewrite node nil)))

(defn add-node 
  "Add newnode to propertyList of the given parent at idx position."
  [parent propertykey newnode idx]
  (let [cu (.getRoot parent)
        rewrite (current-rewrite-for-cu cu)
        property (astnode/node-property-descriptor-for-ekeko-keyword parent propertykey) 
        list-rewrite (.getListRewrite rewrite parent property)
        index (if (instance? java.lang.String idx)
               (Integer/parseInt idx)
               idx)] 
    (println "add node" newnode)
    (.insertAt list-rewrite newnode index nil)))

(defn add-node-in-rewrite-code
  "Add new string code as new nodes to propertyList of the given parent starting from idx position."
  [parent property string idx]
  (let [newnodes (parsing/parse-string-ast string)]
    (println "add node in rewrite code")
    (println property string idx)
    (if (instance? ASTNode newnodes)
      (add-node parent property newnodes idx)
      (map (fn [newnode] (add-node parent property newnode idx)) (reverse newnodes)))))

(defn 
  add-sibling-node-in-rewrite-code
  [node string]
  (let [parent (.getParent node)
        property (.getLocationInParent node)
        idx (.indexOf (.getStructuralProperty parent property) node)]
    (println "add sibling node in rewrite code")
    (add-node-in-rewrite-code parent (keyword (.getId property)) string (+ 1 idx))))

(defn replace-node 
  "Replace node with newnode."
  [node newnode]
  (let [cu (.getRoot node)
        rewrite (current-rewrite-for-cu cu)] 
    (.replace rewrite node newnode nil)))

(defn replace-node-in-rewrite-code
  "Replace node with new string code as new nodes."
  [node string]
  (let [newnodes (parsing/parse-string-ast string)] 
    (if (instance? ASTNode newnodes)
      (replace-node node newnodes)
      (let [parent (.getParent node)
            property (.getLocationInParent node)
            idx (.indexOf (.getStructuralProperty parent property) node)]
        (remove-node node)
        (add-node-in-rewrite-code parent (keyword (.getId property)) string idx)))))

(defn change-property-node
  "Change property node."
  [node propertykey value]
  (let [cu (.getRoot node)
        rewrite (current-rewrite-for-cu cu)
        property (astnode/node-property-descriptor-for-ekeko-keyword node propertykey)] 
    (.set rewrite node property value))) 


; MAP FOR REWRITE SNIPPET
; -----------------------------
; rewrite-map: map of {rewrite-snippet original-snippet}


(defn 
  make-rewritemap
  []
  {})

(defn
  add-rewrite-snippet
  [rewrite-map rewrite-snippet template-snippet]
  (if (not (nil? template-snippet))
    (assoc rewrite-map rewrite-snippet template-snippet)))

(defn 
  get-original-snippet
  [rewrite-map rewrite-snippet]
  (get rewrite-map rewrite-snippet))

(defn 
  get-original-snippets
  [rewrite-map]
  (distinct (vals rewrite-map)))

(defn 
  get-rewrite-snippets
  [rewrite-map template-snippet]
  (let [found-map (filter (fn [map] (= (val map) template-snippet)) rewrite-map)]
    (keys found-map)))

(defn 
  remove-rewrite-snippet
  [rewrite-map rewrite-snippet]
  (dissoc rewrite-map rewrite-snippet))

(defn 
  update-rewrite-snippet
  [rewrite-map old-rewrite-snippet new-rewrite-snippet]
  (let [template-snippet (get-original-snippet rewrite-map old-rewrite-snippet)]
    (if (not (nil? template-snippet))
      (do
        (add-rewrite-snippet (remove-rewrite-snippet rewrite-map old-rewrite-snippet)
                             new-rewrite-snippet template-snippet))
      rewrite-map)))
  
; GENERATE EKEKO REWRITE QUERY
; ----------------------------------------------

(defn 
  print-rewrite-snippet
  "Returns new string, replaced user-var with actual node
   user-vars -> [[?lvar actual-node] [?lvar actual-node rule] ...].
   example rule : \"add + [?lvar 3 0] + s\"."
  [str-snippet user-vars]
  (if (empty? user-vars)
    str-snippet
    (let [var (first (first user-vars))
          node (fnext (first user-vars))
          rule (fnext (next (first user-vars)))
          rep-str (if (nil? rule)
                    (.replace str-snippet var (str node))
                    (.replace str-snippet var (util/convert-rule-to-name rule (str node))))]
      (print-rewrite-snippet rep-str (rest user-vars)))))

(defn
  snippet-rewrite-query
  [template-snippet rewrite-snippets]
  (defn user-vars-str [snippet user-vars]
    (let [user-vars-condition
          (for [[var uservar] user-vars
                :let [str-uservar (.replace (str uservar) "?" "*")
                      node (representation/snippet-node-for-var snippet var)
                      rule (if (= (representation/snippet-constrainer-for-node snippet node)  
                                  :change-name)
                             (.replace (first (representation/snippet-constrainer-args-for-node snippet node)) "?" "*")
                             nil)]]
            (if (nil? rule)
              `[~str-uservar ~uservar]
              `[~str-uservar ~uservar ~rule]))]
      user-vars-condition))
  (let [var-match (representation/snippet-var-for-root template-snippet)
        rewrite-str
        (for [snippet rewrite-snippets
              :let [snippet-str (.replace (gui/print-plain-snippet snippet) "?" "*")
                    user-vars (:var2uservar snippet)
                    user-vars-condition (distinct (user-vars-str snippet user-vars))]]             
          `(el/perform (add-sibling-node-in-rewrite-code 
                         ~var-match (print-rewrite-snippet ~snippet-str [~@user-vars-condition]))))]
    `(~@rewrite-str
       (el/perform (remove-node ~var-match)))))
        
(defn
  snippet-rewrite-import-declaration-query
  [template-snippet rewrite-snippets]
  (let [var-match (representation/snippet-var-for-root template-snippet)
        var-cu '?cu
        rewrite-str
        (for [snippet rewrite-snippets
              :let [snippet-str (gui/print-plain-snippet snippet)]]
          `(el/perform (add-node-in-rewrite-code ~var-cu :imports ~snippet-str 0)))] 
    `((cl/fresh [~var-cu]
                (el/equals ~var-cu (.getRoot ~var-match))
                ~@rewrite-str))))

(defn
  internal-snippetgrouphistory-rewrite-query
  "Generate query the Ekeko projects for rewrite of the given snippetgrouphistory and rewritemap."
  [grphistory rewritemap function-query]
  (defn rewrite-query-rec [template-snippets rewrite-query]
    (if (empty? template-snippets)
      rewrite-query
      (let [template-snippet (first template-snippets)
            rewrite-snippets (get-rewrite-snippets rewritemap template-snippet)
            grp (representation/snippetgrouphistory-current grphistory)
            query     (querying/add-query 
                        (querying/snippet-in-group-query template-snippet grp 'damp.ekeko/ekeko)      
                        (function-query template-snippet rewrite-snippets))] 
        (rewrite-query-rec 
          (rest template-snippets)
          (cons query rewrite-query))))) 
  `(~@(rewrite-query-rec (get-original-snippets rewritemap) '())))
      
(defn
  snippetgrouphistory-rewrite-query
  "Generate query the Ekeko projects for rewrite of the given snippetgrouphistory and rewritemap."
  ;note : the string ? should be changed to other character, otherwise
  ;error: Unsupported binding form: ?...
  [snippetgrouphistory rewritemap]
  (internal-snippetgrouphistory-rewrite-query
    snippetgrouphistory rewritemap snippet-rewrite-query))

(defn
  snippetgrouphistory-rewrite-import-declaration-query
  "Generate query the Ekeko projects for rewrite (add import declaration) of the given snippetgrouphistory and rewritemap."
  [snippetgrouphistory rewritemap]
  (internal-snippetgrouphistory-rewrite-query
    snippetgrouphistory rewritemap snippet-rewrite-import-declaration-query))

(defn
  rewrite-query-by-snippetgrouphistory
  "Excecute rewrite query of the given snippetgrouphistory and rewritemap."
  ;;note, if print is removed, when apply-rewrite is not working
  [snippetgrouphistory rewritemap rewritemap-import]
  (print
    (map eval (snippetgrouphistory-rewrite-query snippetgrouphistory rewritemap)))
  (print
    (map eval (snippetgrouphistory-rewrite-import-declaration-query snippetgrouphistory rewritemap-import))))

