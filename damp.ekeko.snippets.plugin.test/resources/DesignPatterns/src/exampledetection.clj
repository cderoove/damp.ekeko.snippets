(ns exampledetection
  (:refer-clojure :exclude [== type])
  (:use [clojure.core.logic])
  (:use [damp.ekeko])
  (:use [damp.ekeko logic ekekomodel])
  (:use [damp.ekeko.jdt ast structure aststructure soot convenience]))

;; Returning null

(defn
  statement|returningnull
  [?s]
  (fresh [?e] 
         (ast :ReturnStatement ?s)
         (has :expression ?s ?e)
         (ast :NullLiteral ?e)))

(defn 
  method|returningnull-returntype
  [?method ?type]
  (fresh [?s ?t]
         (statement|returningnull ?s)
         (ast-methoddeclaration|encompassing ?s ?method)
         (has :returnType2 ?method ?t)
         (ast|type-type ?t ?type)))

(defn
  method|returningnull-returntype|collection
  [?method ?type]
  (fresh [?collectiontype]
         (method|returningnull-returntype ?method ?type)
         (conde [(equals ?type ?collectiontype)]
                [(type-type|super+ ?type ?collectiontype)])
         (type-name|qualified|string ?collectiontype "java.util.Collection")))
         
(comment
  
  (ekeko* [?m ?t] (method|returningnull-returntype|collection ?m ?t))
  
  (def 
    listener
    (model-update-listener 
      (incremental-node-marker
        (fn []
          (map first 
               (damp.ekeko/ekeko [?m ?t] 
                                 (method|returningnull-returntype|collection ?m ?t)))))))
  
  (register-listener listener)
  ;change file
  (unregister-listener listener)
   
  
  )

;; Composite design pattern

(defn
  name-prefix
  "Succeeds when ?name starts with ?prefix. 
   Non-relational, as ?prefix has to be bound."
  [?name ?prefix]
  (fresh [?string]
         (name|simple-string ?name ?string)
         (succeeds (.startsWith ?string ?prefix))))

(defn
  name|startingWithAdd
  [?name]
  (all 
    (name-prefix ?name "add")))

(defn
  method|startingWithAdd
  [?method]
  (fresh [?name] 
         (ast :MethodDeclaration ?method)
         (has :name ?method ?name)
         (name|startingWithAdd ?name)))
  
(defn
  invocation|onCollection
  [?invocation]
  (fresh [?receiver ?receivertype ?collectiontype]
         (ast :MethodInvocation ?invocation)
         (has :expression ?invocation ?receiver)
         (ast|expression-type ?receiver ?receivertype)
         (type-type|super+ ?receivertype ?collectiontype)
         (type-name|qualified|string ?collectiontype "java.util.Collection")))

(defn
  method|invokingAddOnCollection
  [?method]
  (fresh [?invocation ?name]
         (ast :MethodDeclaration ?method)
         (child+ ?method ?invocation)
         (invocation|onCollection ?invocation)
         (has :name ?invocation ?name)
         (name|simple-string ?name "add")))

(defn
  method-parametertype 
  [?method ?resolvedtype]
  (fresh [?parameter ?parametertype]
         (ast :MethodDeclaration ?method)
         (child :parameters ?method ?parameter)
         (has :type ?parameter ?parametertype) 
         (ast|type-type ?parametertype ?resolvedtype)))

         
(defn 
  composite-component
  [?composite ?component ]
  (fresh [?method ?invocation ?compositetype ?componenttype]
         (ast :TypeDeclaration ?composite)
         (typedeclaration-type ?composite ?compositetype)
         (child :bodyDeclarations ?composite ?method)
         (method|startingWithAdd ?method)
         (method|invokingAddOnCollection ?method)
         (method-parametertype ?method ?componenttype)
         (typedeclaration-type ?component ?componenttype)
         (type-type|super+ ?compositetype ?componenttype)))




;; Example REPL session
(comment 
  
  (require 'exampledetection)
  (in-ns 'exampledetection)
  
  (ekeko* [?composite ?component]
     (composite-component ?composite  ?component))
  
)
