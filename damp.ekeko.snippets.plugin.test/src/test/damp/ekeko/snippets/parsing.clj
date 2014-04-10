(ns 
    ^{:doc "Test suite for damp.ekeko.snippets.parsing functions."
      :author "Coen De Roover"}
    test.damp.ekeko.snippets.parsing
    (:require [damp.ekeko.snippets [parsing :as parsing]])
    (:use clojure.test))

(deftest
  parsing
  ^{:doc "Tests whether strings are parsed as expected, using one of the specialized
          parse-string-* functions. "}
  (is (instance? org.eclipse.jdt.core.dom.MethodInvocation (parsing/parse-string-expression "f.g()")))
  (is (instance? org.eclipse.jdt.core.dom.ExpressionStatement (parsing/parse-string-statement "f.g();")))
  (is (instance? java.util.List (parsing/parse-string-statements "f.g(); m();")))
  (is (instance? org.eclipse.jdt.core.dom.MethodDeclaration (parsing/parse-string-declaration "public void f() { return; } ")))
  (is (instance? org.eclipse.jdt.core.dom.TypeDeclaration (parsing/parse-string-declaration "public class X { }" )))
  (is (instance? org.eclipse.jdt.core.dom.CompilationUnit (parsing/parse-string-unit "package Y; class X { public void f() { return; } }" ))))