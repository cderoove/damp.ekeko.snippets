(ns 
    ^{:doc "Core functionality related to groups of snippets."
      :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.snippetgroup
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [directives :as directives]
             [util :as util]
             [parsing :as parsing]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]])
  (:import [org.eclipse.jdt.core.dom.rewrite ASTRewrite])
  (:import [org.eclipse.jdt.core.dom ASTNode ASTNode$NodeList CompilationUnit]))


;; Snippets Group Datatype
;; ----------------------

; Datatype representing a group (list) of Snippet(s) and additional logic condition

(defrecord SnippetGroup [name snippetlist])

(defn 
  snippetgroup-name
  "Returns name of the given snippet group."
  [snippetgroup]
  (:name snippetgroup))

(defn 
  snippetgroup-snippetlist
  "Returns the list of Snippet(s) of the given snippet group."
  [snippetgroup]
  (:snippetlist snippetgroup))

(defn snippetgroup?
  [snippetgroup]
  (instance? SnippetGroup snippetgroup))

(defn 
  snippetgroup-snippets-userqueries
  "Returns the logic conditions defined by users of the snippets in the snippet group."
  [snippetgroup]
  (mapcat snippet/snippet-userquery (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-nodes
  "Returns all nodes from the given snippet group."
  [snippetgroup]
  (mapcat snippet/snippet-nodes (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-rootvars
  "Returns all logic variables of root node of all snippets in snippet group."
  [snippetgroup]
  (sort 
    (map snippet/snippet-var-for-root (snippetgroup-snippetlist snippetgroup))))


(defn
  snippetgroup-vars
  "Returns all logic variables from the given snippet group."
  [snippetgroup]
  (mapcat snippet/snippet-vars (snippetgroup-snippetlist snippetgroup)))


(defn 
  snippetgroup-snippet-for-node
  [group node]
  (some 
    (fn [snippet]
      (when 
        (contains? (:ast2var snippet) node)
        snippet))
    (snippetgroup-snippetlist group)))
 


(defn 
  add-snippet
  "Add snippet to snippetgroup."
  [snippetgroup snippet]
  (let [new-snippetlist 
        (into [] (concat (snippetgroup-snippetlist snippetgroup) [snippet]))]
    (assoc snippetgroup :snippetlist new-snippetlist)))

(defn 
  remove-snippet
  "Remove snippet from snippetgroup."
  [snippetgroup snippet]
  (let [new-snippetlist 
        (into [] (remove #{snippet} (snippetgroup-snippetlist snippetgroup)))]
    (assoc snippetgroup :snippetlist new-snippetlist)))

(defn
  update-snippet|ui
  "Updates the given snippet in this snippet group with the result of the given function.
   Returns array of new snippetgroup, new snippet. Called from UI."
  [snippetgroup snippet updater]
  (let [newsnippet (updater snippet)]
    (to-array 
      [(assoc snippetgroup 
              :snippetlist 
              (replace
                [(.indexOf (snippetgroup-snippetlist snippetgroup) snippet)
                 newsnippet]
                (snippetgroup-snippetlist snippetgroup)))
       newsnippet])))

                     
(defn
  replace-snippet
  [group oldsnippet newsnippet]
  "Replaces oldsnippet in group by newsnippet."
  (let [newlist (replace {oldsnippet newsnippet} (:snippetlist group))]
    (assoc group :snippetlist newlist)))            

(defn
  snippetgroup-update-snippetlist
  [snippetgroup snippetlist]
  (assoc snippetgroup :snippetlist snippetlist))

(defn 
  snippetgroup-remove-bounddirective|ui
  "Returns array of new snippetgroup, new snippet."
  [snippetgroup snippet node bounddirective]
  (update-snippet|ui snippetgroup
                  snippet
                  (fn [snippet] (snippet/remove-bounddirective snippet node bounddirective))))

(defn 
  snippetgroup-add-directive|ui
  "Returns array of new snippetgroup, new snippet."
  [snippetgroup snippet node directive]
  (let [bounddirective 
        (directives/bind-directive-with-defaults directive snippet node)]
    (update-snippet|ui snippetgroup
                    snippet
                    (fn [snippet] (snippet/add-bounddirective snippet node bounddirective)))))
                                      
  
;; Constructing SnippetGroup instances
;; -----------------------------------

(defn 
  make-snippetgroup
  "Create SnippetGroup instance."
  ([name]
    (SnippetGroup. name []))
  ([name snippetseq]
    (SnippetGroup. name snippetseq)))
    



;;OTHER FUNCTIONS' NAME
;;---------------------------

(def template-root snippet/snippet-root)
(def make-templategroup make-snippetgroup)

(defn
  register-callbacks 
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_MAKE_SNIPPETGROUP) make-snippetgroup)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_NAME) snippetgroup-name)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_SNIPPET_FOR_NODE) snippetgroup-snippet-for-node)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_ADD_SNIPPET_TO_SNIPPETGROUP) add-snippet)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_REMOVE_SNIPPET_FROM_SNIPPETGROUP) remove-snippet)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_UPDATE_SNIPPET_IN_SNIPPETGROUP) update-snippet|ui)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_SNIPPETS) snippetgroup-snippetlist)
  (set! (damp.ekeko.snippets.gui.BoundDirectivesViewer/FN_GROUP_REMOVE_BOUNDDIRECTIVE_FROM_NODE) snippetgroup-remove-bounddirective|ui)
  (set! (damp.ekeko.snippets.gui.BoundDirectivesViewer/FN_GROUP_ADD_DIRECTIVE_TO_NODE) snippetgroup-add-directive|ui)
  
  )

(register-callbacks)
