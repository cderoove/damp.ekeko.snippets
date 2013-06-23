# damp.ekeko.snippets.EkekoSnippets

EkekoSnippets is a template-driven program transformation tool, consists of three main parts, Ekeko Template, Ekeko Candidate Subjects Checking, and Ekeko Transformation. 

Ekeko Template enables generalizing a code snippet into an appropriate template, and provides a set of operators to modify the template in iterative and incremental way.

Ekeko Candidate Subjects Checking enables verifying the code that matches the template as this will become the subject of the transformation, and provides suggestion for operator application based on positive and negative examples in order to get the appropriate template.

Ekeko Transformation enables specifying the imperative change operations that will change each match, and applying the resulting transformation to the software system.

EkekoSnippets is based on [Ekeko](https://github.com/cderoove/damp.ekeko/tree/master/EkekoPlugin).



## Installation

EkekoSnippets plugin can be built from [EkekoSnippets](https://github.com/cderoove/damp.ekeko.snippets/tree/master/EkekoSnippets)

EkekoSnippets plugin has been tested against [Eclipse Juno (4.2)](http://www.eclipse.org)



## Documentation

How To Get Started

See [screen recording](http://youtu.be/x5lt-jh7P-M):

Open view by launching "Ekeko Snippets" (with eclipse icon) in the toolbar, or on menu Window -> Show View -> Other -> EkekoSnippets.

Ekeko Group
1. Add group.
2. Click on the group to open Ekeko Template view.

Ekeko Template

3. Add code as snippet. Select source code from editor and click "Add Snippet". Initial code template will be generated in Snippet Column.
4. Manipulate code template. Select an element of snippet, choose operator, and apply it by clicking "Apply Operator". Some operators may need arguments.
5. Run Query. Select a snippet, and click "Run Query" to get candidate subjects match the code template.
6. Check Query Result. Select a snippet, and click "Check Query Result" to open view Ekeko Candidate Subjects Checking.

Ekeko Candidate Subjects Checking   

7. Add an example from query Result. Select element on Result table, click "Add as Positive Example" to add it as positive example, or click "Add as Negative Example" to add it as Negative Example.
8. Add an example from editor. Select source code and click "+" button on Expected Result table. Then click "Add as Positive Example" to add it as positive example, or click "Add as Negative Example" to add it as Negative Example.
9. Run operator suggestion. Click button "Start" to search operator to be applied on snippet to get expected result.

Ekeko Tranformation
In Ekeko Template view, click "Program Transformation" to open Ekeko Transformation view.

10. Add rewritten code. Select source code from editor and click "Add Rewritten code". The relation of "Before" and "After" snippet is one to many.
11. Add rewrite action. Select an element of "Before" snippet and an element of "After" snippet, click on action to add a rewrite action. 
12. Transform subjects. Click "Transform" in Ekeko Group to transform subjects to new code.
 
Note: Logic variable should begin with ? for example ?method


## License  

Copyright Â© 2013 Ekeko Snippets contributors: 

* [Coen De Roover](http://soft.vub.ac.be/~cderoove/)
* [Siltvani](siltvani@vub.ac.be)


Distributed under the Eclipse Public License.

External dependencies:

* Eclipse plugin [org.eclipse.jdt.astview](http://www.eclipse.org/jdt/ui/astview/index.php) (Eclipse Public License)
* Eclipse plugin [Counterclockwise](http://code.google.com/p/counterclockwise/) (Eclipse Public License)
* Ekeko plugin [Ekeko](https://github.com/cderoove/damp.ekeko/tree/master/EkekoPlugin) (Eclipse Public License)
  