(ns 
  ^{:doc "Auxiliary functions for snippet-driven querying."
    :author "Coen De Roover, Siltvani, Tim Molderez."}
  damp.ekeko.snippets.util
  (:require [damp.ekeko.jdt [astnode :as astnode]]))

(defn
  class-simplename
  "Returns the unqualified name (a String) of the given java.lang.Class."
  [clazz]
  (last (.split #"\." (.getName clazz))))

(defn 
  gen-lvar
  "Generates a unique symbol starting with ?v
   (i.e., a symbol to be used as the name for a logic variable)."
  ([] (gen-lvar "?v"))
  ([prefix] (gensym (str "?" prefix))))

(defn
  gen-readable-lvar-for-value
  "Generates a unique logic variable, of which the name 
   gives a hint about the class of the given JDT property value."
  [value]
  (gen-lvar
    (cond
      (nil? value)
      "NullShouldNotBeHere"
      (astnode/lstvalue? value)
      "ListVal"
      (astnode/primitivevalue? value)
      "PrimVal"
      (astnode/nilvalue? value)
      "NilVal"
      (astnode/ast? value)
      (class-simplename (class value)))))

(defn 
  filtered-node-propertyvalues
  "Returns all property values of given node, except :javadoc"
  [val]
  (filter 
    (fn [propertyvalue]
      (if (not (nil? (:owner propertyvalue)))
        (let [parent (:owner propertyvalue)
              property (astnode/node-property-descriptor-for-ekeko-keyword parent :javadoc)]
          (not (= (:property propertyvalue) property)))
        true))
    (astnode/node-propertyvalues val)))

(defn 
  walk-jdt-node
  "Recursive descent through a JDT node (or JDT lst / primitive property value),
   applying given functions to the encountered 
   ASTNode instances and Ekeko wrappers for their property values.

   Consider using snippet/walk-snippet-element instead."
  ([n f]
    (walk-jdt-node n f f f f))
  ([n node-f list-f primitive-f null-f]
    (loop
      [nodes (list n)]
      (when-not (empty? nodes)
        (let [val (first nodes)
              others (rest nodes)]
          (cond 
            (astnode/ast? val)
            (do
              (node-f val)
              (recur (concat (filtered-node-propertyvalues val) others)))
            (astnode/lstvalue? val)
            (do 
              (list-f val)
              (recur (concat (:value val) others)))
            (astnode/primitivevalue? val)
            (do
              (primitive-f val)
              (recur others))
            (astnode/nilvalue? val)
            (do
              (null-f val)
              (recur others))
            :default
            (throw (Exception. "Don't know how to walk this value."))))))))


(defn viable-repeat 
  "Keep on applying func until we get cnt results for which test-func is true
   @param cnt  We want this many viable results
   @param func  The function to apply repeatedly (has no args)
   @param test-func  This test-function determines whether a return value of func is viable (has 1 arg, returns a boolean)
   @return a list of cnt viable results"
  [cnt func test-func]
  (repeatedly 
    cnt 
    (fn []
      (loop []
       (let [result (func)]
         (print ".")
         (if (test-func result)
           result 
           (recur)))))))

(defmacro dbg
  "Identity function, that prints x as a side-effect"
  [x] 
  `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(def log-enabled true)

(defmacro log
  "Identity function, that writes x to file y.txt as a side-effect.
   If the file already exists, x is appended."
  [x y]
  (if log-enabled
    `(let [x# ~x] 
      (spit (str ~y ".txt") (str x# "\n") :append true :create true)
      x#)
    `~x))

; Source: http://stackoverflow.com/questions/6694530/executing-a-function-with-a-timeout/6697469#6697469
(defmacro with-timeout
  "Execute the given body in a new future.
   A TimeoutException is thrown if the execution takes longer than a certain amount of milliseconds."
  [millis & body]
  `(let [future# (future ~@body)]
     (try
       (.get future# ~millis java.util.concurrent.TimeUnit/MILLISECONDS)
       (catch java.util.concurrent.TimeoutException x# 
         (do
           (println "Timed out!!")
           (future-cancel future#)
           nil)))))