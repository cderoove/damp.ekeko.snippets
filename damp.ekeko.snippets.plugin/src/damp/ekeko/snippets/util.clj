(ns 
  ^{:doc "Auxiliary functions for snippet-driven querying."
    :author "Coen De Roover, Siltvani, Tim Molderez."}
  damp.ekeko.snippets.util
  (:require [damp.ekeko.jdt [astnode :as astnode]]
            [damp.ekeko.workspace [workspace :as ws]])
  (:import 
    (java.util.concurrent TimeoutException TimeUnit FutureTask)
    (clojure.lang LispReader$ReaderException)
    [org.eclipse.core.resources ResourcesPlugin IWorkspace]
           [org.eclipse.jdt.core  IMember IJavaElement ITypeHierarchy JavaCore IType IJavaModel IJavaProject IPackageFragment ICompilationUnit]
           [org.eclipse.ui PlatformUI IWorkingSet IWorkingSetManager]
           [org.eclipse.core.runtime.jobs Job]
           [org.eclipse.core.runtime Status Path]
           ;[damp.ekeko EkekoModel JavaProjectModel ProjectModel]
           )
  )

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

(defn find-compilationunit [^String project-name ^String cls-name]
  "Find a compilation unit, given an Eclipse project name and an absolute class name"
  (let [all-projects (.getJavaProjects (JavaCore/create (.getRoot  (damp.ekeko.workspace.workspace/eclipse-workspace))))
        project (first (filter 
                        (fn [project]
                          (= project-name (.getElementName project)))
                        all-projects))]
    (.getCompilationUnit (.findType project cls-name))))

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

(defn pmap-group
  "Variant of pmap, where each future created is associated with the given thread group."
  [f coll tg]
  (let [n (+ 2 (.. Runtime getRuntime availableProcessors))
        rets (map 
               #(let [task (FutureTask. (fn [] (f %)))
                      thr (Thread. tg task)]
                  (.start thr)
                  [task thr])
               coll)
        
        run (fn [[task thr]]
              (try
                (.get task)
                (catch Exception e
                  (.cancel task true)
                  (.stop thr) 
                  (throw e))))
        
        step (fn step [[x & xs :as vs] fs]
               (lazy-seq
                 (if-let [s (seq fs)]
                   (cons (run x) (step xs (rest s)))
                   (map run vs))))]
    (step rets (drop n rets))))

(defn parallel-viable-repeat
  "Keep on applying func until we get cnt results for which test-func is true
   (This variation of viable-repeat produces all results concurrently.)
   @param cnt  We want this many viable results
   @param func  The function to apply repeatedly (has no args)
   @param test-func  This test-function determines whether a return value of func is viable (has 1 arg, returns a boolean)
   @return a list of cnt viable results"
  [cnt func test-func thread-group]
  (pmap
    (fn [idx]
      (loop []
       (let [result (func)]
         (print ".")
         (if (test-func result)
           result 
           (recur)))))
    (range 0 cnt))
;  (pmap-group
;    (fn [idx]
;      (loop []
;       (let [result (func)]
;         (print ".")
;         (if (test-func result)
;           result 
;           (recur)))))
;    (range 0 cnt)
;    thread-group)
  )

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
   executing."
  ([thunk ms]
     (thunk-timeout thunk ms nil))
  ([thunk time tg]
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
;         (finally (when tg (.stop tg)))
         ))))

(defmacro with-timeout
  "Apply this macro to an expression and an exception is thrown if it takes longer than a given time to evaluate the expression"
  ([time body]
  `(thunk-timeout (fn [] ~body) ~time))
  ([time body tg]
  `(thunk-timeout (fn [] ~body) ~time ~tg)))

(defmacro future-group [thread-group body]
  `(let [task# (FutureTask. (fn [] ~body))
         thr# (Thread. ~thread-group task#)]
     (try
       (.start thr#)
       task#
       (catch Exception e#
         (.cancel task# true)
         (.stop thr#) 
         (throw e#)))))

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

(defn printStackTrace []
  "Prints the current stack trace"
  (try 
    (throw (new Exception))
    (catch Exception e
      (.printStackTrace e))))

(defn metaspace-usage []
  (let [mx-beans (java.lang.management.ManagementFactory/getMemoryPoolMXBeans)]
    (some (fn [mx-bean]
            (if (= "Metaspace" (.getName mx-bean))
              (.getUsage mx-bean)))
          mx-beans)))

(defn metaspace-almost-full? []
  "Does the allocated Java metaspace size come close to its maximum size?"
  (let [usage (metaspace-usage)
        filled (/ (.getCommitted usage) (.getMax usage))]
;    (println (double filled))
    (> filled 0.4)))

(defn eval-in-ns 
  "Performs an eval in another namespace."
  [expr namespace]
  (binding [*ns* namespace]
    (eval expr))
  
;         (let [cur *ns*]
;           (try ; needed to prevent failures in the eval code from skipping ns rollback
;             (ns namespace)   
;             (eval expr)
;             (finally 
;               (in-ns (ns-name cur)))))
         )

(defn gen-ns
  "Generate a new namespace (with a random name)"
  []
  (let [namespace (create-ns (gensym "gen-ns-"))]
    (eval-in-ns '(clojure.core/use 'clojure.core) namespace)
    namespace))

;(defn reset-protocol-cache []
;  (doseq [protocol
;          [clojure.core.logic.protocols/IUninitialized
;           damp.ekeko.logic/ISupportContains
;           damp.ekeko.jdt.astbindings/IResolveToFieldBinding
;           damp.ekeko.jdt.astbindings/IResolveToMethodBinding
;           astnode/IHasProperties
;           astnode/IHasOwner
;           astnode/IAST
;           astnode/IIdentifiesProjectValue
;           ]]
;    (-reset-methods protocol)))

;(defn protocol? [x]
;  (and (instance? clojure.lang.PersistentArrayMap x)
;       (boolean (:on-interface x))))

;(defn protocols
;  "Get a seq of protocols in the given namespace."
;  ([namespace]
;    (binding [*ns* (if (symbol? namespace)
;                     (find-ns namespace)
;                     namespace)]
;      (doall (filter protocol?
;                     (map var-get
;                          (filter var?
;                                  (map second
;                                       (ns-map *ns*))))))))
;  ([]
;    (protocols *ns*)))

;(defonce simple-protocol-cache-cleaner
;  ;; Starts a simple daemon thread that purges the caches for all protocols in the current namespace.
;  (let [interval 1000 ;; ms
;        ps (protocols *ns*)]
;    (doto (new Thread (fn []
;                        (loop [i 0]
;                          (doseq [p ps] (-reset-methods p))
;                          (Thread/sleep interval)
;                          (recur (inc i)))))
;      (.setDaemon true)
;      (.start))))

;(defn clean-protocol-cache [ps]
;  (doseq [p ps] (-reset-methods p)))

;(comment
;  (doseq [ns (all-ns)]
;            (clean-protocol-cache (protocols ns))))