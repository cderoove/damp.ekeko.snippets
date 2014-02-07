(ns 
  ^{:doc "Test suite for snippet precondition."
    :author "Siltvani, Coen De Roover"}
  test.damp.ekeko.snippets.precondition
  (:refer-clojure :exclude [== type declare])
  (:use [clojure.core.logic :exclude [is]] :reload)
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [operators :as operators]
             [precondition :as precondition]
             [matching :as matching]
             [parsing :as parsing]
             [util :as util]])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [reification :as reification]
             ])
  (:require [damp.ekeko])
  (:require [damp.ekeko 
             [logic :as el]
             [snippets :as snippets]])
  (:use clojure.test))

;; Operator safety
;; ---------------

(deftest
  ^{:doc "Operator safety : contains-elements"}
  operatorsafety-contains-elements
  (let [root      (parsing/parse-string-declaration "public void methodA() { this.methodM(); this.methodC();} ") 
        node      (snippet/snippet-node-with-value "" (.statements (.getBody root)))] 
    (is (true? 
          (precondition/safe-operator-for-node? :contains-elements node)))))

(deftest
  ^{:doc "Operator safety : contains-elements false result"}
  operatorsafety-contains-elements-false
  (let [root      (parsing/parse-string-declaration "public void methodA() { this.methodM(); this.methodC();} ") 
        node      (.getBody root)] 
    (is (false? 
          (precondition/safe-operator-for-node? :contains-elements node)))))

(deftest
  ^{:doc "Operator safety : split-variable-declaration-statement"}
  operatorsafety-split-variable-declaration-statement
  (let [root      (parsing/parse-string-declaration "public int rmethodB() {int x = 0, y = 0; int z = x + y; return z; } ") 
        node      (first (.statements (.getBody root)))] 
    (is (true? 
          (precondition/safe-operator-for-node? :split-variable-declaration-statement node)))))

(deftest
  ^{:doc "Operator safety : split-variable-declaration-statement false result"}
  operatorsafety-split-variable-declaration-statement-false
  (let [root      (parsing/parse-string-declaration "public int rmethodB() {int x = 0, y = 0; int z = x + y; return z; } ") 
        node      (.get (.statements (.getBody root)) 2)] 
    (is (false? 
          (precondition/safe-operator-for-node? :split-variable-declaration-statement node)))))

(deftest
  ^{:doc "Operator safety : allow-ifstatement-with-else"}
  operatorsafety-allow-ifstatement-with-else
  (let [root      (parsing/parse-string-declaration "public int rmethodF(int val) {	int r = 0; if (val == 0) {	r = val;	} return r; } ") 
        node      (fnext (.statements (.getBody root)))] 
    (is (true? 
          (precondition/safe-operator-for-node? :allow-ifstatement-with-else node)))))

(deftest
  ^{:doc "Operator safety : allow-ifstatement-with-else false result"}
  operatorsafety-allow-ifstatement-with-else-false
  (let [root      (parsing/parse-string-declaration "public int rmethodF(int val) {	int r = 0; if (val == 0) {	r = val;	} return r; } ") 
        node      (first (.statements (.getBody root)))] 
    (is (false? 
          (precondition/safe-operator-for-node? :allow-ifstatement-with-else node)))))

(deftest
  ^{:doc "Operator safety : allow-subtype-on-variable-declaration"}
  operatorsafety-allow-subtype-on-variable-declaration
  (let [root      (parsing/parse-string-declaration "public int rmethodF(int val) {	int r = 0; if (val == 0) {	r = val;	} return r; } ") 
        node      (.getType (first (.statements (.getBody root))))] 
    (is (true? 
          (precondition/safe-operator-for-node? :allow-subtype-on-variable-declaration node)))))

(deftest
  ^{:doc "Operator safety : allow-subtype-on-variable-declaration false result"}
  operatorsafety-allow-subtype-on-variable-declaration-false
  (let [root      (parsing/parse-string-declaration "public int rmethodF(int val) {	int r = 0; if (val == 0) {	r = val;	} return r; } ") 
        node      (first (.statements (.getBody root)))] 
    (is (false? 
          (precondition/safe-operator-for-node? :allow-subtype-on-variable-declaration node)))))

(deftest
  ^{:doc "Operator safety : allow-variable-declaration-with-initializer"}
  operatorsafety-allow-variable-declaration-with-initializer
  (let [root      (parsing/parse-string-declaration "public char myMethodK() { s = 'm'; return s; } ") 
        node      (first (.statements (.getBody root)))] 
    (is (true? 
          (precondition/safe-operator-for-node? :allow-variable-declaration-with-initializer node)))))

(deftest
  ^{:doc "Operator safety : allow-variable-declaration-with-initializer false result"}
  operatorsafety-allow-variable-declaration-with-initializer-false
  (let [root      (parsing/parse-string-declaration "public char myMethodK() { s = 'm'; return s; } ") 
        node      (fnext (.statements (.getBody root)))] 
    (is (false? 
          (precondition/safe-operator-for-node? :allow-variable-declaration-with-initializer node)))))

(deftest
  ^{:doc "Operator safety : inline-method-invocation"}
  operatorsafety-inline-method-invocation
  (let [root      (parsing/parse-string-declaration "public void myMethod() { this.methodA(); int x = 0; } ") 
        node      (first (.statements (.getBody root)))] 
    (is (true? 
          (precondition/safe-operator-for-node? :inline-method-invocation node)))))

(deftest
  ^{:doc "Operator safety : inline-method-invocation false result"}
  operatorsafety-inline-method-invocation-false
  (let [root      (parsing/parse-string-declaration "public void myMethod() { this.methodA(); int x = 0; } ") 
        node      (fnext (.statements (.getBody root)))] 
    (is (false? 
          (precondition/safe-operator-for-node? :inline-method-invocation node)))))

(deftest
  ^{:doc "Operator safety : negated-node"}
  operatorsafety-negated-node
  (let [root      (parsing/parse-string-declaration "public void myMethod() { this.methodA(); int x = 0; } ") 
        node      (first (.statements (.getBody root)))] 
    (is (true? 
          (precondition/safe-operator-for-node? :negated-node node)))))

(deftest
  ^{:doc "Operator safety : negated-node false result"}
  operatorsafety-negated-node-false
  (let [root      (parsing/parse-string-declaration "public void myMethod() { this.methodA(); int x = 0; } ") 
        node      (.statements (.getBody root))] 
    (is (false? 
          (precondition/safe-operator-for-node? :negated-node node)))))


;; Possible nodes and Applicable operator
;; ------------------------------------------

;; Snippet
;; The test will be against the snippet below
(def root-snippet 
  (damp.ekeko.snippets.parsing/parse-string-declaration 
            "public void myMethod() { 
                 int x = 0, y = 1; 
                 val = 5; 
                 if (val > 0) {
                    this.methodM(); 
                 } else {
                    this.methodC();
                 }
             } "))

(deftest 
  ^{:doc "Possible nodes for contains-elements"}
  possiblenodes-for-contains-elements
  (test/tuples-correspond 
    (precondition/possible-nodes-for-operator root-snippet :contains-elements)
    "#{(\"[]\") 
       (\"[int x=0, y=1;\\n, val=5;\\n, if (val > 0) {\\n  this.methodM();\\n}\\n else {\\n  this.methodC();\\n}\\n]\") 
       (\"[x=0, y=1]\") 
       (\"[this.methodM();\\n]\") 
       (\"[public]\") 
       (\"[this.methodC();\\n]\")}"))
;;note: to check, empty result ([]) returned by ekeko

(deftest 
  ^{:doc "Possible nodes for allow-ifstatement-with-else"}
  possiblenodes-for-allow-ifstatement-with-else
  (test/tuples-correspond 
    (precondition/possible-nodes-for-operator root-snippet :allow-ifstatement-with-else)
    "#{(\"if (val > 0) {\\n  this.methodM();\\n}\\n else {\\n  this.methodC();\\n}\\n\")}"))

(deftest 
  ^{:doc "Possible nodes for allow-subtype-on-variable-declaration"}
  possiblenodes-for-allow-subtype-on-variable-declaration
  (test/tuples-correspond 
    (precondition/possible-nodes-for-operator root-snippet :allow-subtype-on-variable-declaration)
    "#{(\"int\") (\"void\")}"))

(deftest 
  ^{:doc "Possible nodes for allow-variable-declaration-with-initializer"}
  possiblenodes-for-allow-variable-declaration-with-initializer
  (test/tuples-correspond 
    (precondition/possible-nodes-for-operator root-snippet :allow-variable-declaration-with-initializer)
    "#{(\"val=5;\\n\")}"))

(deftest 
  ^{:doc "Possible nodes for inline-method-invocation"}
  possiblenodes-for-inline-method-invocation
  (test/tuples-correspond 
    (precondition/possible-nodes-for-operator root-snippet :inline-method-invocation)
    "#{(\"this.methodC();\\n\") (\"this.methodM();\\n\")}"))

(deftest 
  ^{:doc "Possible nodes for negated-node"}
  possiblenodes-for-negated-node
  (test/tuples-correspond 
    (precondition/possible-nodes-for-operator 
      (first (.statements (.getBody root-snippet))) 
      :negated-node)
    "#{(\"x=0\") 
       (\"y=1\") 
       (\"int x=0, y=1;\\n\") 
       (\"int\") 
       (\"0\") 
       (\"1\") 
       (\"x\") 
       (\"y\")}"))

(deftest 
  ^{:doc "Applicable operators for snippet"}
  precondition-applicable-operators-for-snippet
  (let [node    (parsing/parse-string-statement "int x=m();")
        snippet (snippet/jdt-node-as-snippet node)
        results (precondition/applicable-operators-for-snippet snippet)
        results-in-list
        (map (fn [x] (concat (list (key x)) (val x))) results)]
  (test/tuples-correspond 
    results-in-list
			"#{
			(\":contains-elements-with-same-size\" \"[(#<VariableDeclarationFragment x=m()>)]\" \"[()]\" \"[()]\" \"[()]\") 
			(\":introduce-logic-variables\" \"[#<VariableDeclarationStatement int x=m();\\n>]\" \"[#<VariableDeclarationFragment x=m()>]\" \"[#<PrimitiveType int>]\" \"[#<MethodInvocation m()>]\" \"[#<SimpleName x>]\" \"[#<SimpleName m>]\") 
			(\":introduce-logic-variables-with-condition\" \"[#<VariableDeclarationStatement int x=m();\\n>]\" \"[#<VariableDeclarationFragment x=m()>]\" \"[#<PrimitiveType int>]\" \"[#<MethodInvocation m()>]\" \"[#<SimpleName x>]\" \"[#<SimpleName m>]\") 
			(\":add-node\" \"[(#<VariableDeclarationFragment x=m()>)]\" \"[()]\" \"[()]\" \"[()]\") 
			(\":allow-subtype-on-class-declaration-extends\" \"[#<PrimitiveType int>]\") 
			(\":negated-node\" \"[#<VariableDeclarationStatement int x=m();\\n>]\" \"[#<VariableDeclarationFragment x=m()>]\" \"[#<PrimitiveType int>]\" \"[#<MethodInvocation m()>]\" \"[#<SimpleName x>]\" \"[#<SimpleName m>]\") 
			(\":contains-elements\" \"[(#<VariableDeclarationFragment x=m()>)]\" \"[()]\" \"[()]\" \"[()]\") 
			(\":contains-variable-declaration-statement\" \"[#<VariableDeclarationStatement int x=m();\\n>]\") 
			(\":contains-elements-with-relative-order\" \"[(#<VariableDeclarationFragment x=m()>)]\" \"[()]\" \"[()]\" \"[()]\") 
			(\":introduce-logic-variable-of-node-exact\" \"[#<VariableDeclarationStatement int x=m();\\n>]\" \"[#<VariableDeclarationFragment x=m()>]\" \"[#<PrimitiveType int>]\" \"[#<MethodInvocation m()>]\" \"[#<SimpleName x>]\" \"[#<SimpleName m>]\") 
			(\":remove-node\" \"[#<VariableDeclarationStatement int x=m();\\n>]\" \"[#<VariableDeclarationFragment x=m()>]\" \"[#<PrimitiveType int>]\" \"[#<MethodInvocation m()>]\" \"[#<SimpleName x>]\" \"[#<SimpleName m>]\") 
			(\":split-variable-declaration-statement\" \"[#<VariableDeclarationStatement int x=m();\\n>]\") 
			(\":introduce-logic-variable\" \"[#<VariableDeclarationStatement int x=m();\\n>]\" \"[#<VariableDeclarationFragment x=m()>]\" \"[#<PrimitiveType int>]\" \"[#<MethodInvocation m()>]\" \"[#<SimpleName x>]\" \"[#<SimpleName m>]\") 
			(\":contains-elements-with-repetition\" \"[(#<VariableDeclarationFragment x=m()>)]\" \"[()]\" \"[()]\" \"[()]\") 
			(\":allow-subtype-on-variable-declaration\" \"[#<PrimitiveType int>]\")
      }"
    )))


;; Test suite
;; ----------
(deftest
   test-suite 
   
   ;(test/against-project-named "TestCase-Snippets-BasicMatching" false  predicate-ast-variable-samebinding)

   (operatorsafety-contains-elements)                             
   (operatorsafety-contains-elements-false)                             
   (operatorsafety-allow-ifstatement-with-else)                   
   (operatorsafety-allow-ifstatement-with-else-false)                   
   (operatorsafety-allow-subtype-on-variable-declaration)       
   (operatorsafety-allow-subtype-on-variable-declaration-false)       
   (operatorsafety-allow-variable-declaration-with-initializer)   
   (operatorsafety-allow-variable-declaration-with-initializer-false)   
   (operatorsafety-inline-method-invocation)                      
   (operatorsafety-inline-method-invocation-false)                      
   (operatorsafety-negated-node)                                  
   (operatorsafety-negated-node-false)                                  

   (possiblenodes-for-contains-elements)                             
   (possiblenodes-for-allow-ifstatement-with-else)                   
   (possiblenodes-for-allow-subtype-on-variable-declaration)       
   (possiblenodes-for-allow-variable-declaration-with-initializer)   
   (possiblenodes-for-inline-method-invocation)                      
   (possiblenodes-for-negated-node)                                  

   (precondition-applicable-operators-for-snippet)
)

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

