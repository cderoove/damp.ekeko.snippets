(ns 
  ^{:doc "Snippet-driven querying of Java projects."
    :author "Coen De Roover, Siltvani, Tim Molderez"}
  damp.ekeko.snippets
  (:refer-clojure :exclude [== type])
  (:require [clojure.core.logic :as cl]) 
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [matching :as matching]
             [matching2 :as matching2]
             [querying :as querying]
             [snippetgroup :as snippetgroup]
             [parsing :as parsing]
             [util :as util]
             [persistence :as persistence]
             [operators]
             [operatorsrep]
             [querying]
             [gui]
             [runtime]
             [transformation :as transformation]
             [rewrites :as rewrites]
             [rewriting :as rewriting]
             [rewriting2 :as rewriting2]
             ])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]])
  (:require [damp.ekeko
             [gui :as gui]
             [logic :as el]]))

(defn
  query-by-snippetgroup*
  "Queries the Ekeko projects for matches for the given snippetgroup. Opens Eclipse view on results."
  [snippetgroup]
  (binding [querying/*print-queries-to-console* true]    
    (let [start (System/nanoTime)
          results (matching2/query-templategroup-list snippetgroup true)
          elapsed  (/ (double (- (System/nanoTime) start)) 1000000.0)
          vars (:columns (meta results))]
      (gui/eclipse-uithread-return 
        (fn [] (gui/open-barista-results-viewer* 
                 "-" 
                 vars 
                 results
                 elapsed 
                 (count results)))))
    
    ;    (querying/query-by-snippetgroup snippetgroup 'damp.ekeko/ekeko*)
    ))

(defn
  query-by-snippetgroup
  "Queries the Ekeko projects for matches for the given snippetgroup."
  [snippetgroup]
  (matching2/query-templategroup-list snippetgroup true)
;  (querying/query-by-snippetgroup snippetgroup 'damp.ekeko/ekeko)
  )


(defn
  query-by-snippetgroup|java
  [snippetgroup]
  (let [solutions (matching2/query-templategroup-list snippetgroup false) 
        ;(querying/query-by-snippetgroup snippetgroup 'damp.ekeko/ekeko '() '() true)
        ]
    (into #{} solutions)))

(defn
  query-by-snippet*
  "Queries the Ekeko projects for matches for the given snippet. Opens Eclipse view on results."
  [snippet]
  (query-by-snippetgroup* (snippetgroup/make-snippetgroup "" [snippet]))
  )

(defn
  query-by-snippet
  "Queries the Ekeko projects for matches for the given snippet."
  [snippet]
  (query-by-snippetgroup (snippetgroup/make-snippetgroup "" [snippet]))
  ;(querying/query-by-snippet snippet 'damp.ekeko/ekeko)
  )

(defn
  transform-by-snippetgroups
  "Performs the program transformation defined by the lhs and rhs snippetgroups." 
  [snippetgroup|lhs snippetgroup|rhs]
  (rewriting2/apply-transformation
    (transformation/make-transformation snippetgroup|lhs snippetgroup|rhs)) 
;  (let [qinfo (querying/snippetgroup-snippetgroupqueryinfo snippetgroup|lhs)
;        defines (:preddefs qinfo)
;        lhsuservars (into #{} (querying/snippetgroup-uservars snippetgroup|lhs))
;        rhsuservars (into #{} (querying/snippetgroup-uservars snippetgroup|rhs))
;        rhsconditions (querying/snippetgroup-conditions|rewrite snippetgroup|rhs lhsuservars)
;        query (querying/snippetgroupqueryinfo-query qinfo 'damp.ekeko/ekeko rhsconditions rhsuservars false)] ;should these be hidden?
;     (querying/pprint-sexps (conj defines query))
;    (doseq [define defines]
;      (eval define))
;    (eval query)
;    (rewrites/apply-and-reset-rewrites))
  )


(defn
  snippetgroup-from-editor
  "Returns the snippetgroup in the currently active editor."
  []
  (damp.ekeko.gui/eclipse-uithread-return 
    (fn []
      (let [editor (damp.ekeko.gui/workbench-editor)]
        (when (instance? damp.ekeko.snippets.gui.TemplateEditor editor)
          (.getGroup (.getGroup editor)))))))

(defn-
  editor-on-snippetgroup
  [snippetgroup]
  (let [page (damp.ekeko.gui/workbench-activepage)
        activeeditor (.getActiveEditor page)
        editorid (damp.ekeko.snippets.gui.TemplateEditor/ID)
        openededitor (.openEditor page (damp.ekeko.snippets.gui.TemplateEditorInput.) editorid)]
    (doto
      openededitor
      (.setPreviouslyActiveEditor activeeditor)
      (.setGroup (damp.ekeko.snippets.data.TemplateGroup/newFromClojureGroup snippetgroup)))))

(defn
  open-editor-on-snippetgroup
  "Opens an editor on the given snippetgroup."
  [snippetgroup]
  (damp.ekeko.gui/eclipse-uithread-return (fn [] (editor-on-snippetgroup snippetgroup))))


(defn
  snippetgroup-from-file
  "Returns the snippetgroup persisted in the given file."
  [file]
  (damp.ekeko.snippets.persistence/slurp-snippetgroup file))

  (defn
    register-callbacks
    []
    (set! (damp.ekeko.snippets.data.TemplateGroup/FN_QUERY_BY_SNIPPET) query-by-snippet*)
    (set! (damp.ekeko.snippets.data.TemplateGroup/FN_QUERY_BY_SNIPPETGROUP) query-by-snippetgroup*)
    (set! (damp.ekeko.snippets.data.TemplateGroup/FN_QUERY_BY_SNIPPETGROUP_NOGUI) query-by-snippetgroup|java)
    (set! (damp.ekeko.snippets.data.TemplateGroup/FN_TRANSFORM_BY_SNIPPETGROUPS) transform-by-snippetgroups))

  (register-callbacks)


  