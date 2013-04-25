# damp.ekeko.snippets.EkekoSnippets

EkekoSnippets is a tool for Program Transformation in Eclipse, consists of three main parts, Ekeko Snippet, Ekeko Result Checking, and Ekeko Transformation. 

Ekeko Snippet is a template-based program query tool. It enables manipulating a code template based on an actual code snippet via its operator.

Ekeko Result Checking enables selecting positive and negative examples of query result. It provides suggestion for operator application based on positive and negative examples.

Ekeko Transformation enables specifying a code template for rewritten code based on actual code snippet, defining relation between code template of original code and rewritten code, and transforming subjects based on those code templates.

EkekoSnippets is based on [Ekeko](https://github.com/cderoove/damp.ekeko/tree/master/EkekoPlugin).



## Installation

EkekoSnippets plugin can be built from [EkekoSnippets](https://github.com/cderoove/damp.ekeko.snippets/tree/master/EkekoSnippets)

EkekoSnippets plugin has been tested against [Eclipse Juno (4.2)](http://www.eclipse.org)



## Documentation

How To Get Started

See [screen recording](http://youtu.be/qAQ5oFbtFjg):

Open view by launching "Ekeko Snippets" (with eclipse icon) in the toolbar, or on menu Window -> Show View -> Other -> EkekoSnippets.

Ekeko Snippet

1. Add code as snippet. Select source code from editor and click "Add Snippet". Initial code template will be generated in Snippet Column.
2. Change mandatory status. Select a snippet, click "Update snippet status" to change the status. Mandatory snippet is a snippet that will be included in querying process of all other snippets.
3. Manipulate code template. Select an element of snippet, choose operator, and apply it by clicking "Apply Operator". Some operators may need arguments.
4. Run Query. Select a snippet, and click "Run Query" to get candidate subjects match the code template.
5. Check Query Result. Select a snippet, and click "Check Query Result" to open view Ekeko Result Checking.

Ekeko Result Checking   

6. Add an example from query Result. Select element on Result table, click "Add as Positive Example" to add it as positive example, or click "Add as Negative Example" to add it as Negative Example.
7. Add an example from editor. Select source code and click "+" button on Expected Result table. Then click "Add as Positive Example" to add it as positive example, or click "Add as Negative Example" to add it as Negative Example.
8. Run operator suggestion. Click button "Start" to search operator to be applied on snippet to get expected result.

Ekeko Tranformation
In Ekeko Snippet view, click "Program Transformation" to open Ekeko Transformation view.

9. Add rewritten code. Select a "Before" snippet, select source code from editor and click "Add Rewritten code". The relation of "Before" and "After" snippet is one to many.
10. Manipulate rewritten code. Select an element of "After" snippet, click on operator to apply it. Some operators may need arguments.
11. Define relation of "Before" and "After" snippets. Select an "After" snippet, apply operator "Introduce logic variables for snippet". This operation will bind all user logic variables of elements in "Before" snippet to same elements in "After" snippet. Apply operator "Introduce logic variable" to add another relation.
12. Add import declaration. Select a "Before" snippet, select source code of import declaration from editor and click "Add Import Code".
13. Transform subjects. Click "Transform" to transform subjects to new code.
 
Note: Logic variable should begin with ? for example ?method


## License  

Copyright © 2013 Ekeko Snippets contributors: 

* [Coen De Roover](http://soft.vub.ac.be/~cderoove/)
* [Siltvani](siltvani@vub.ac.be)


Distributed under the Eclipse Public License.

External dependencies:

* Eclipse plugin [org.eclipse.jdt.astview](http://www.eclipse.org/jdt/ui/astview/index.php) (Eclipse Public License)
* Eclipse plugin [Counterclockwise](http://code.google.com/p/counterclockwise/) (Eclipse Public License)
* Ekeko plugin [Ekeko](https://github.com/cderoove/damp.ekeko/tree/master/EkekoPlugin) (Eclipse Public License)
  