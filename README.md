# Ekeko/X 
Ekeko/X (*aka damp.ekeko.snippets*) is an Eclipse plugin for transforming Java programs. Its transformation specifications consist of a left-hand side and a right-hand side component. The left-hand side component identifies the subjects of the transformation, while the right-hand side component defines how each identified subject should be rewritten. 

Ekeko/X specifications are decidedly **template**-based. On the **left-hand side**, code templates are used to specify the structural (i.e., instructions and their organization) as well as the behavioral (i.e., the order in which instructions are executed at run-time and the values instructions operate on) characteristics of the intended transformation subjects. This requires a rich, inter-procedural matching semantics. On the **right-hand side**, code templates exemplify the changes to each subject. 

Ekeko/X owes its peculiar name to our meta-programming library for Clojure on which it is founded. This library, called [Ekeko](https://github.com/cderoove/damp.ekeko/tree/master/EkekoPlugin), enables implementing Eclipse plugins as functional programs in which logic queries are embedded seamlessly. 

## Automated template generalization and refinement

To assist Ekeko/X users in writing templates, the tool also provides a search-based approach to automatically generalize/refine a template, such that it produces a given desired set of source code snippets. Usage information, as well as how to reproduce the experiments that evaluate the approach are available in the [genetic algorithm documentation](https://github.com/cderoove/damp.ekeko.snippets/blob/master/damp.ekeko.snippets.plugin/src/damp/ekeko/snippets/geneticsearch/README.md). 

## Demo

The following screencast demonstrates an early prototype of Ekeko/X:

[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/CXNKyBIuAv8/0.jpg)](http://youtu.be/CXNKyBIuAv8)

Example program transformations can be found in directory [damp.ekeko.snippets.plugin.test/resources/TestCase-JDT-CompositeVisitor-Templates](
https://github.com/cderoove/damp.ekeko.snippets/tree/master/damp.ekeko.snippets.plugin.test/resources/TestCase-JDT-CompositeVisitor-Templates). 

For our SCAM14 submission, the relevant files start with a ``chaqdemo_`` prefix. Opening these files from within Eclipse launches the transformation editor on them. The program under transformation can be found at [csmrwcrecase.zip](http://soft.vub.ac.be/~resteven/).

## Installation

The Ekeko/X plugin can be built from source ([EkekoSnippets](https://github.com/cderoove/damp.ekeko.snippets/tree/master/EkekoSnippets)) or installed from our Eclipse update-site: [http://soft.vub.ac.be/~cderoove/eclipse/](http://soft.vub.ac.be/~cderoove/eclipse/)

The installation instructions for [Ekeko](https://github.com/cderoove/damp.ekeko/tree/master/EkekoPlugin) should be consulted as well: [README.md](https://github.com/cderoove/damp.ekeko/blob/master/damp.ekeko.plugin/README.md)

Ekeko/X has been tested against [Eclipse Kepler (4.4)](http://www.eclipse.org)


## License  

Copyright © 2013-2015 Ekeko/X contributors: 

* [Coen De Roover](http://soft.vub.ac.be/~cderoove/)
* [Tim Molderez](http://timmolderez.be)
* [Siltvani](siltvani@vub.ac.be)


Distributed under the Eclipse Public License.

External dependencies:

* Eclipse plugin [Ekeko](https://github.com/cderoove/damp.ekeko/tree/master/EkekoPlugin) (Eclipse Public License)
  
