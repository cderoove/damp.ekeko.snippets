(ns 
  ^{:doc "Auxiliary functions for snippet-driven querying."
    :author "Coen De Roover, Siltvani, Tim Molderez."}
  damp.ekeko.snippets.util
  (:require [damp.ekeko.jdt [astnode :as astnode]])
  (:import (java.util.concurrent TimeoutException TimeUnit FutureTask)
           (clojure.lang LispReader$ReaderException)))

(defn
  class-simplename
  "Returns the unqualified name (a String) of the given java.lang.Class."
  [clazz]
  (last (.split #"\." (.getName clazz))))

(defn 
  gen-lvar
  "Generates a unique symbol starting with ?v
   (i.e., a string to be used as the name for a logic variable)."
  ([] (gen-lvar "?v"))
  ([prefix] (gensym (str "?" prefix))))

(defn
  gen-readable-lvar-for-value|classbased
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
  gen-readable-lvar-for-value
  [value]
  ;(if-let [ownerprop (astnode/owner-property value)]
  ;  (gen-lvar (astnode/property-descriptor-id ownerprop))
  (gen-readable-lvar-for-value|classbased value))


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
              (recur (concat (astnode/node-propertyvalues val) others)))
            (astnode/lstvalue? val)
            (do 
              (list-f val)
              (recur (concat (astnode/value-unwrapped val) others)))
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


(defn 
  walk-jdt-nodes
  "See walk-jdt-node, but walks two corresponding elements from different nodes simultaneously.
   Function arguments therefore take pairs of elements, rather than a single element."
  ([e1 e2 f]
    (walk-jdt-nodes e1 e2 f f f f))
  ([e1 e2 node-f list-f primitive-f null-f]
    (loop
      [nodes (list [e1 e2])]
      (when-not (empty? nodes)
        (let [[v1 v2 :as v] (first nodes)
              others (rest nodes)]
          (cond 
            (astnode/ast? v1)
            ;;;todo: check v2 is an astnode as well, otherwise throw friendly exception
            (do
              (node-f v)
              (recur 
                (concat 
                  (map vector 
                       (astnode/node-propertyvalues v1)
                       (astnode/node-propertyvalues v2))
                  others)))
            (astnode/lstvalue? v1)
            (do 
              (list-f v)
              (recur (concat
                       (map vector 
                            (astnode/value-unwrapped v1)
                            (astnode/value-unwrapped v2))
                       others)))
            (astnode/primitivevalue? v1)
            (do
              (primitive-f v)
              (recur others))
            (astnode/nilvalue? v1)
            (do
              (null-f v)
              (recur others))
            :default
            (throw (Exception. (str "Don't know how to walk this value:" val)))
            ))))))




; TODO: Maybe throw an exception after we retried X times? To avoid infinite recursions..
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

(defn average
  "Calculate the average in a collection of numbers"
  [coll]
  (/ (reduce + coll) (count coll)))

(defmacro dbg
  "Identity function, that prints x as a side-effect"
  [x] 
  `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(def log-enabled true)

(defmacro log
  "Identity function, that writes msg to file-name.txt as a side-effect.
   If the file already exists, msg is appended."
  [file-name msg]
  (if log-enabled
    `(let [msg# ~msg] 
      (spit (str ~file-name ".txt") (str msg# "\n") :append true :create true)
      msg#)
    `~msg))

(defn append-csv
  "Appends a new row of values to a .csv file"
  [file-name vals]
  (spit file-name 
        (str (apply str (interpose ";" vals)) "\n") 
        :append true :create true))

; Source: https://github.com/flatland/clojail/blob/master/src/clojail/core.clj#L40
(defn thunk-timeout
  "Takes a function and an amount of time to wait for thse function to finish
   executing. The sandbox can do this for you. unit is any of :ns, :us, :ms,
   or :s which correspond to TimeUnit/NANOSECONDS, MICROSECONDS, MILLISECONDS,
   and SECONDS respectively."
  ([thunk ms]
     (thunk-timeout thunk ms :ms nil)) ; Default to milliseconds, because that's pretty common.
  ([thunk time unit]
     (thunk-timeout thunk time unit nil))
  ([thunk time unit tg]
     (let [task (FutureTask. thunk)
           thr (if tg (Thread. tg task) (Thread. task))]
       (try
         (.start thr)
         (.get task time TimeUnit/MILLISECONDS)
         (catch TimeoutException e
           (.cancel task true)
           (.stop thr) 
           (throw (TimeoutException. "Execution timed out.")))
         (catch Exception e
           (.cancel task true)
           (.stop thr) 
           (throw e))
         (finally (when tg (.stop tg)))))))

(defmacro with-timeout [time & body]
  "Apply this macro to an expression and an exception is thrown if it takes longer than a given time to evaluate the expression" 
  `(thunk-timeout (fn [] ~@body) ~time))

(defn current-time 
  "Returns the current time (as a Unix timestamp)"
  []
  (.getTime (new java.util.Date)))

(defn current-date
  "Returns the current date as a String"
  []
  (.format (new java.text.SimpleDateFormat "dd-MM-yyyy--HH-mm-ss") (new java.util.Date)))

(defn time-elapsed 
  "Returns the number of milliseconds that have passed since start-time.
   Note that start-time must be obtained via (.System (nanoTime))"
  [start-time]
  (/ (double (- (. System (nanoTime)) start-time)) 1000000.0))

(defn make-dir 
  [path]
  (.mkdir (java.io.File. path)))

(defn trace-element-to-string
  [e]
  (let [class (.getClassName e)
        method (.getMethodName e)] 
    (let [match (re-matches #"^([A-Za-z0-9_.-]+)\$(\w+)__\d+$" (str class))
          chunk (if (and match (= "invoke" method))
                  (apply format "%s/%s" (rest match))
                  (format "%s.%s" class method))]
      (str
        chunk
        (format " (%s:%d)" (or (.getFileName e) "") (.getLineNumber e))))))

(defn stacktrace-to-string [tr]
  (let [st (.getStackTrace tr)
        prefix (str (.getName (class tr)) " " (.getMessage tr) " at \n")]
    (str prefix 
         (apply str 
                (for [e st]
                  (str "    " (trace-element-to-string e) "\n"))))))


(defn combinations [coll]
  "Takes a map, where each value is a list of elements.
   Returns all possible combinations, such that each combination contains one element from each list.

   For example {:a [1 2 3] :b [5 6]}
   Returns [[1 5] [1 6] [2 5] [2 6] [3 5] [3 6]]"
  (if (= 1 (count (keys coll)))
    (for [val ((first (keys coll)) coll)]
      [val])
    
    (let [rest-combinations (combinations (dissoc coll (first (keys coll))))]
      (for [val ((first (keys coll)) coll)
            combo rest-combinations]
        (cons val combo)))))
