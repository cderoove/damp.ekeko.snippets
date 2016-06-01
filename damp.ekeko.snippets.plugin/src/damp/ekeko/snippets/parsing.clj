(ns 
  ^{:doc "Functions for parsing strings as Java code, resulting in a JDT ASTNode."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.parsing
  (:import 
    [org.eclipse.jdt.core.dom ASTParser AST ASTNode ASTNode$NodeList
     CompilationUnit TypeDeclaration Block Expression]
    [damp.ekeko JavaProjectModel]))

(defn 
  jdt-node-malformed?
  "Returns whether a JDT ASTNode has its MALFORMED bit set or is a CU which has an IProblem which is an error."
  [^ASTNode n]
  (letfn [(malformed [node] (not= 0 (bit-and (.getFlags node) (ASTNode/MALFORMED))))]
         (or
           (not n)
           (and (instance? java.util.AbstractList n)
                (some malformed n))
           (and (instance? CompilationUnit n)
                (some (fn [p] (.isError p))
                      (.getProblems n)))
           (and (instance? ASTNode n)
                (malformed n)))))

(defn 
  jdt-node-valid? 
  "Returns whether a JDT ASTNode is valid (i.e., is not malformed)."
  [n]
  (not (jdt-node-malformed? n)))

(declare jdt-parse-string)

(defn 
  parse-string-statements
  "Parses the given string as a sequence of Java statements."
  [string]
  (let [block (jdt-parse-string string (ASTParser/K_STATEMENTS))]
    (when 
      (instance? Block block)
      (let [list (.statements block)]
        (when
          (> (count list) 0)
          list)))))

(defn 
  parse-string-statement
  "Parses the given string as single Java statement."
  [string]
  (when-let [list (parse-string-statements string)]
    (when 
      (= (count list) 1)
      (first list))))

(defn 
  parse-string-expression 
  "Parses the given string as a Java expression."
  [string]
  (let [exp (jdt-parse-string string (ASTParser/K_EXPRESSION))]
    (when 
      (instance? Expression exp)
      exp)))

(defn 
  parse-string-unit 
  "Parses the given string as a Java compilation unit."
  [string]
  (let [unit (jdt-parse-string string (ASTParser/K_COMPILATION_UNIT))]
    (when 
      (instance? CompilationUnit unit)
      unit)))

(defn 
  parse-string-importdeclarations
  "Parses the given string as list of Import Declarations."
  [string]
  (when-let [unit (parse-string-unit string)]
    (when 
      (empty? (.types unit))
      (.imports unit))))

(defn 
  parse-string-importdeclaration
  "Parses the given string as an Import Declaration."
  [string]
  (when-let [list (parse-string-importdeclarations string)]
    (when 
      (= (count list) 1)
      (first list))))

(defn 
  parse-string-declarations 
  "Parses the given string as a sequence of Java class body declarations (type, method, field)."
  [string]
  (let [classdeclaration (jdt-parse-string string (ASTParser/K_CLASS_BODY_DECLARATIONS))]
    (when 
      (instance? TypeDeclaration classdeclaration)
      (let [list (.bodyDeclarations  classdeclaration)]
        (when (> (count list) 0)
          list)))))

(defn
  parse-string-declaration
  "Parses the given string as a Java class body declaration (type, method, field)."
  [string]
  (when-let [list (parse-string-declarations string)]
    (when 
      (= (count list) 1)
      (first list))))

(defn 
  jdt-parse-string 
  "Parses the given string as a Java construct of the given kind
   (expression, statements, class body declarations, compilation unit)."
  [^String string string-kind]
  (let [parser (ASTParser/newParser JavaProjectModel/JLS)]                
    (.setSource parser (.toCharArray string))
    (.setKind parser string-kind)
    (.createAST parser nil)))


(defn 
  parse-string
  "Attempts to parse the given string as a Java construct. 
   Due to ambiguity (e.g., a block can be a statement or an initializer declaration), 
   returns a _sequence of pairs_ of the resulting ASTNode or NodeList and 
   a keyword corresponding the construct's kind (:expression :statement :declaration
   :statements :unit) when successful.

   See also:
   parse-string-* for non-ambiguous parsing when the type of the construct is known."
  [string]
  (filter vector?
          (map
            (fn [pair]
              (let [[parsef symbol] pair
                    parsed (parsef string)]
                (and 
                  (not (jdt-node-malformed? parsed))
                  [parsed symbol])))
            [[parse-string-declaration :declaration] 
             [parse-string-declarations :declaration] 
             [parse-string-statement :statement]
             [parse-string-statements :statements]
             [parse-string-importdeclaration :unit] 
             [parse-string-importdeclarations :unit] 
             [parse-string-unit :unit]
             [parse-string-expression :expression]])))

(defn 
  parse-string-ast
  [string] 
  (first 
    (first 
      (filter (fn [x] (instance? ASTNode (first x))) (parse-string string)))))

(defn
  make-variable-declaration-statement
  [modifiers type fragment]
  (parse-string-statement 
    (str (apply str (interpose \space modifiers)) " " type " " fragment ";")))

(defn
  parse-strings-to-nodes
  [arr-string]
  (map parse-string-ast (filter (fn [x] (not (nil? x))) arr-string)))

(def
  jdt-modifier-keywords
  [org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/ABSTRACT_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/FINAL_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/NATIVE_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/PRIVATE_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/PROTECTED_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/PUBLIC_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/STATIC_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/STRICTFP_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/SYNCHRONIZED_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/TRANSIENT_KEYWORD
   org.eclipse.jdt.core.dom.Modifier$ModifierKeyword/VOLATILE_KEYWORD])

(defn register-callbacks []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_PARSE_TO_NODES) parse-strings-to-nodes))

(register-callbacks)