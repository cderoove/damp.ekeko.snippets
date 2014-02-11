(ns damp.ekeko.snippets.snippetgrouphistory
  (:require [damp.ekeko.snippets 
	            [snippet :as snippet]
             [snippetgroup :as snippetgroup]]))
  
;; Snippets Group History Datatype
;; --------------------------------

; Datatype representing a group (list) of Original Snippet(s) and 
; history of applied operators in order
; original-snippetgroup --> original snippet group
; operators-history --> vector of [applied operator-id, var-node, args]
; operators-undo-history --> applied operator which are undo by user
;                            list of [applied operator-id, var-node, args]
;; use vector for history, because always add element as last element
;; and use list for undohistory, because always add element as first element

(defrecord SnippetGroupHistory [original-snippetgroup current-snippetgroup operators-history operators-undohistory])


(defn 
  snippetgrouphistory-original
  "Returns original snippet group of the given snippet group history."
  [snippetgrouphistory]
  (:original-snippetgroup snippetgrouphistory))

(defn 
  snippetgrouphistory-current
  "Returns current snippet group of the given snippet group history."
  [snippetgrouphistory]
  (:current-snippetgroup snippetgrouphistory))

(defn 
  snippetgrouphistory-history
  "Returns history of applied operators of the given snippet group history."
  [snippetgrouphistory]
  (:operators-history snippetgrouphistory))

(defn 
  snippetgrouphistory-undohistory
  "Returns history of undo applied operators of the given snippet group history."
  [snippetgrouphistory]
  (:operators-undohistory snippetgrouphistory))

(defn 
  snippetgrouphistory-var-for-node
  [grp node]
  (snippetgroup/snippetgroup-var-for-node (:current-snippetgroup grp) node))

(defn 
  snippetgrouphistory-node-for-var
  [grp var]
  (snippetgroup/snippetgroup-node-for-var (:current-snippetgroup grp) var))

(defn 
  history-operator
  "Returns operator id of the given history [op-id, var-node, args]."
  [history]
  (first history))

(defn 
  history-varnode
  "Returns var node of the given history [op-id, var-node, args]."
  [history]
  (fnext history))

(defn 
  history-args
  "Returns arguments of the given history [op-id, var-node, args]."
  [history]
  (last history))

;; Constructing SnippetGroupHistory instances
;; -------------------------------------------

(defn 
  make-snippetgrouphistory
  "Create SnippetGroupHistory instance and keeping the track of operators history."
  [name]
  (let [snippetgroup (snippetgroup/make-snippetgroup name)]
    (SnippetGroupHistory. snippetgroup snippetgroup [] '())))

(defn 
  make-snippetgrouphistory-from-snippetgroup
  "Create SnippetGroupHistory instance from given group."
  [snippetgroup]
  (SnippetGroupHistory. snippetgroup snippetgroup [] '()))

(defn 
  reset-snippetgrouphistory
  "Reset SnippetGroupHistory instance, but keep the undo history."
  [grouphistory]
  (let [snippetgroup (:original-snippetgroup grouphistory)
        undohistory (:operators-undohistory grouphistory)]
    (SnippetGroupHistory. snippetgroup snippetgroup [] undohistory)))

(defn
  snippetgrouphistory-clean-history
  [grouphistory]
  (update-in 
    (update-in grouphistory [:operators-undohistory] (fn [x] '()))
    [:operators-history]
    (fn [x] [])))

(defn
  snippetgrouphistory-new-state
  [grp]
  (let [user-cond (:userquery (:current-snippetgroup grp))
        new-grp (update-in (snippetgroup/snippetgroup-new-state (:current-snippetgroup grp)) [:userquery] (fn [x] user-cond))]
    (update-in
      (update-in 
        (snippetgrouphistory-clean-history grp) 
        [:original-snippetgroup] (fn [x] new-grp))
      [:current-snippetgroup] (fn [x] new-grp))))


  
;; Updating SnippetGroupHistory instances
;; -------------------------------------------

(defn snippetgrouphistory-update-original-group
  [grouphistory newgroup]
  (update-in grouphistory [:original-snippetgroup] (fn [x] newgroup)))

(defn snippetgrouphistory-update-group
  [grouphistory newgroup]
  (update-in grouphistory [:current-snippetgroup] (fn [x] newgroup)))

(defn snippetgrouphistory-add-history
  [grouphistory op-id node args]
  (let [newhistory (conj (snippetgrouphistory-history grouphistory) [op-id node args])]
    (update-in grouphistory [:operators-history] (fn [x] newhistory))))

(defn snippetgrouphistory-last-history
  [grouphistory]
  "Returns last applied operators." 
  (last (snippetgrouphistory-history grouphistory)))

(defn snippetgrouphistory-first-undohistory
  [grouphistory]
  "Returns first undo applied operators." 
  (first (snippetgrouphistory-undohistory grouphistory)))

(defn snippetgrouphistory-add-undohistory
  [grouphistory]
  "Add the last element of list history as first element of undo history 
   and returns new snippet group history." 
  (let [lasthistory (snippetgrouphistory-last-history grouphistory)
        newundohistory (cons lasthistory (snippetgrouphistory-undohistory grouphistory))]
    (update-in grouphistory [:operators-undohistory] (fn [x] newundohistory))))

(defn snippetgrouphistory-remove-undohistory
  [grouphistory]
  "Remove first undo applied operator from the list undo history,
   and returns new snippet group history." 
  (let [firstundo (snippetgrouphistory-first-undohistory grouphistory)
        newundo (rest (snippetgrouphistory-undohistory grouphistory))]
    (update-in grouphistory [:operators-undohistory] (fn [x] newundo))))


(defn
  configure-callbacks
  []
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_MAKE_SNIPPETGROUPHISTORY) make-snippetgrouphistory)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_MAKE_SNIPPETGROUPHISTORY_FROM_SNIPPETGROUP) make-snippetgrouphistory-from-snippetgroup)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_SNIPPETGROUPHISTORY_CURRENT) snippetgrouphistory-current))

(configure-callbacks)
