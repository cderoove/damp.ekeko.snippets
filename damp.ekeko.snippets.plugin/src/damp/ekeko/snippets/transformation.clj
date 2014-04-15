(ns 
  ^{:doc "Datatype for representing transformation specifications."
  :author "Coen De Roover"}
  damp.ekeko.snippets.transformation
  (:require [damp.ekeko.snippets 
             [snippetgroup :as snippetgroup]]))


;todo: implement clojure's IFn protocol such that they can be applied as functions?

(defrecord 
  Transformation
  [lhs rhs])

(defn
  make-transformation
  [lhs rhs]
  (Transformation. lhs rhs))

(defn
  transformation-lhs
  [t]
  (:lhs t))

(defn
  transformation-rhs
  [t]
  (:rhs t))


(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.gui.TransformationEditor/FN_MAKE_TRANSFORMATION) make-transformation)
  (set! (damp.ekeko.snippets.gui.TransformationEditor/FN_TRANSFORMATION_LHS) transformation-lhs)
  (set! (damp.ekeko.snippets.gui.TransformationEditor/FN_TRANSFORMATION_RHS) transformation-rhs)

  )

(register-callbacks)

  
             