(ns damp.ekeko.snippets.parsing-template
  (:require [damp.ekeko.snippets
             [parsing :as parsing]
             [util :as util]]))

; Shorter aliases for some of the directive names to speed up writing templates in text form
(def directive-aliases
  {"..." "replace-by-wildcard"
   "var" "replace-by-metavariable"
   "eq" "equals"
   "set" "match|set"})

(defn ch 
  "Retrieve the character at a certain line-no and character-no"
  [lines line char]
  (nth (nth lines line) char))

(defn remove-from-lines [lines start-line start-char end-line end-char]
  "Removes a section from a list of strings, given a starting line no. and character,
   and an end line no. and index"
  (if (= start-line end-line)
    ; Base case
    (let [line (nth lines start-line)
          new-line (str 
                     (subs line 0 start-char) 
                     (subs line end-char (count line)))]
      (assoc lines start-line new-line))
    ; Recursive step
    (let [line (nth lines start-line)
          new-line (subs line 0 start-char)
          new-lines (assoc lines start-line new-line)]
      (recur new-lines (inc start-line) 0 end-line end-char))))

(defn matching-bracket [lines line-no char-no]
  "Given the position of an opening bracket, return the position of the corresponding closing bracket
   and the contents in between the two brackets"
  (let [opening-char (ch lines line-no char-no)
        closing-char (case opening-char
                       \{ \}
                       \[ \]
                       \( \)
                       (println "Not a bracket!"))]
    (loop [cur-line line-no 
           cur-char (inc char-no)
           cur-contents ""
           open-brackets 1]
      (let [[cur-line2 cur-char2] ; If at the end of a line, move to the next
            (if (= char-no (count (nth lines cur-line)))
              [(inc cur-line) 0]
              [cur-line cur-char])
            cur-character (ch lines cur-line2 cur-char2)]
        (cond
          (= cur-character opening-char) 
          (recur cur-line2 (inc cur-char2) (str cur-contents cur-character) (inc open-brackets))
          
          (= cur-character closing-char)
          (if (= 1 open-brackets)
            [cur-line2 cur-char2 cur-contents]
            (recur cur-line2 (inc cur-char2) (str cur-contents cur-character) (dec open-brackets)))
          
          :else
          (recur cur-line2 (inc cur-char2) (str cur-contents cur-character) open-brackets)
          )))))

(defn position-from-start 
  "Convert a line+character position into a single number, which counts how many characters we've seen from
   the very beginning of lines"
  [lines line char]
  (reduce
    (fn [i cur-line]
      (if (= line cur-line)
        (+ i char)
        (+ i (count (nth lines cur-line)))))
    0
    (range 0 (inc line))))

(defn next-open-bracket 
  "Starting from a given position, find the next bracket"
  [lines line char bracket-character]
  (cond 
    (>= line (count lines))
    [nil nil]
    
    (= bracket-character (ch lines line char))
    [line char]
    
    (= (inc char) (count (nth lines line)))
    (recur lines (inc line) 0 bracket-character)
    
    :else
    (recur lines line (inc char) bracket-character)))

(defn split-directives-list [directives-string]
  (if (empty? directives-string) 
    []
    (let [[line char] (next-open-bracket [directives-string] 0 0 \()]
      (if (nil? char) 
        [directives-string]
        (let [[close-line close-char directive] (matching-bracket [directives-string] line char)]
          (cons directive 
                (split-directives-list (subs directives-string (inc close-char) (count directives-string)))))))))

(defn- attach-directives [template root node dir-map]
  (let [template-processed-children
        (reduce
          (fn [cur-template child] (attach-directives cur-template root child dir-map))
          template
          (damp.ekeko.snippets.snippet/snippet-node-children|conceptually-refs template node))
        
        
        [start length]
        (cond
          ; Skip primitive wrappers, since their parent already is an equivalent SimpleName, NumberLiteral, ...
          (damp.ekeko.jdt.astnode/primitivevalue? node)
          [-1 -1]
          (damp.ekeko.jdt.astnode/lstvalue? node)
          (let [elements (damp.ekeko.snippets.snippet/snippet-node-children|conceptually-refs template node)]
            (if (empty? elements)
              [-1 -1] ; Tricky to determine the location of something that isn't there... To attach directives to an empty list, attach it to e.g. a wildcard
              (let [start (.getExtendedStartPosition root (first elements))
                    end (+ (.getExtendedStartPosition root (last elements)) (.getExtendedLength root (last elements)))]
                [start (- end start)])))
          :rest
          [(.getExtendedStartPosition root node) (.getExtendedLength root node)])
        
        dirs-txt (get dir-map [start length])]
    (reduce 
      (fn [template dir-txt]
        (let [dir-parts (clojure.string/split dir-txt #" ")
              dir-name (let [alias-lookup (get directive-aliases (first dir-parts))]
                         (if (nil? alias-lookup) (first dir-parts) alias-lookup))
              directive (some
                          (fn [dir] (if (= dir-name (damp.ekeko.snippets.directives/directive-name dir)) dir))
                          (damp.ekeko.snippets.matching/registered-directives))
              operands (damp.ekeko.snippets.directives/directive-operands directive)
              operand-values (rest dir-parts)
              ]
          (damp.ekeko.snippets.snippet/add-bounddirective 
            template 
            node
            (damp.ekeko.snippets.directives/make-bounddirective 
              directive
              (cons (damp.ekeko.snippets.operators/make-directiveoperandbinding-for-match node)
                      (for [i (range 0 (count operands))]
                        (damp.ekeko.snippets.directives/make-directiveoperand-binding (nth operands i) (nth operand-values i)))
                      )))))
      template-processed-children
      (split-directives-list dirs-txt))))

(defn parse-template
  "Parses a template in text form
   A few shorthands available in text form:
   - If there's only one directive, parentheses are optional
   - There are shorter aliases available for some directive names; see directive-aliases
   There are a few caveats to take into account:
   - Replacing by wildcard or by metavariable must be written in the [code]@[directives] notation!
   - Must distinguish between a list and a list element"
  [template-string]
  (let [lines (clojure.string/split-lines template-string)
        
        [stripped-lines directives-map]
        (loop [cur-line 0
               cur-char 0
               cur-lines lines
               cur-dirs {}]
          (let [[open-line open-char] (next-open-bracket cur-lines cur-line cur-char \[)]
            (cond 
              ; Reached the end
              (nil? open-char)
              [cur-lines cur-dirs]
              
              ; Reached a directives list; retrieve it, then remove it from the template text
              (let [[close-line close-char txt] (matching-bracket cur-lines open-line open-char)]
                (= \@ (ch cur-lines close-line (inc close-char))))
              (let [[close-line close-char txt] (matching-bracket cur-lines open-line open-char)
                    [dir-line dir-char directives-txt] (matching-bracket cur-lines close-line (+ close-char 2))
                    extended-position (position-from-start cur-lines open-line open-char)
                    extended-length (- (position-from-start cur-lines close-line (dec close-char)) extended-position) ; !! TODO Must exclude nested directives
                    new-dirs (assoc cur-dirs [extended-position extended-length] directives-txt)
                    new-cur-lines (-> cur-lines
                                    (remove-from-lines close-line close-char dir-line (inc dir-char))
                                    (remove-from-lines open-line open-char open-line (inc open-char)))]
                (recur open-line open-char new-cur-lines new-dirs))
              
              ; Not a directives list; moving on
              :rest
              (recur open-line (inc open-char) cur-lines cur-dirs))))
        ast (parsing/parse-string-ast (clojure.string/join "\n" stripped-lines))
        snippet (damp.ekeko.snippets.matching/jdt-node-as-snippet ast)
        decorated-snippet (attach-directives 
                            snippet 
                            (.getRoot ast)
                            (damp.ekeko.snippets.snippet/snippet-root snippet)
                            directives-map)]
    (inspector-jay.core/inspect decorated-snippet)
    )
  )

(comment
  (damp.ekeko.snippets.persistence/slurp-transformation "/Users/soft/Desktop/addParam.ekx")
  
  (split-directives-list "(bla) fds  (ble) (blu)")
  
  (parse-template (util/clipboard-string))
  (parse-template "[System.[out]@[eq ?x].println(\"Hello world\");]@[eq ?y]")
  (parse-template "System.[out]@[eq ?x].println(\"Hello world\");")
  
  (matching-bracket ["[System.[out]@[eq ?x].println(\"Hello world\");]@[eq ?y]"] 0 0)
  )