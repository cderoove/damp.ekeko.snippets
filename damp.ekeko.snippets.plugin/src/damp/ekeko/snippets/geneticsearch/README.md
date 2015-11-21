# Automated generalization and refinement

Ekeko/X is a template-base search and transformation tool. As it sometimes isn't straightforward to design a template that matches with exactly the desired code snippets, we have provided support to automatically generalize or refine a template group, such that it will match with a set of desired matching code snippets. This page details the process of setting up Ekeko/X and its support for automated generalization and refinement, and how to use it. We also explain how to reproduce the experiments that evaluate the effectiveness of this automated generalization and refinement approach.

## Installation from source

* If you haven't already, first install the [Eclipse](https://www.eclipse.org) IDE. (Ekeko/X has been tested using Eclipse Mars and Luna.) Note that the chosen Eclipse package should include the Eclipse Plugin Development Environment. (This is the case for e.g. "Eclipse IDE for Java EE Developers" and "Eclipse for RCP and RAP Developers")
* Install the Counterclockwise Eclipse plugin, which adds support for the Clojure language. Counterclockwise can be found in the Eclipse marketplace. (Help > Eclipse Marketplace...)
* Clone the Github repositories of [Ekeko](https://github.com/cderoove/damp.ekeko) and [Ekeko/X](https://github.com/cderoove/damp.ekeko.snippets) to your computer.
* Import the following four Eclipse projects from the damp.ekeko directory (via File > Import... > General > Existing Projects into Workspace): 
  * damp.ekeko.feature
  * damp.ekeko.plugin
  * damp.ekeko.plugin.test
  * damp.libs. 
* Import the following three projects from the damp.ekeko.snippets directory:
  * damp.ekeko.snippets.feature
  * damp.ekeko.snippets.plugin
  * damp.ekeko.snippets.plugin.test

## Starting Ekeko/X

* To start Ekeko/X in a separate Eclipse instance, right-click the damp.ekeko.snippet.plugin project > Run As > Eclipse Application. Before starting, it may be necessary to change the run configuration: (Right-click the project > Run As > Run Configurations...) In the Main tab, check the "Location:" textbox. In the Arguments tab, check the "Working directory:" and the "VM arguments:". Our experiments were executed with the following VM arguments:

  -Dosgi.requiredJavaVersion=1.8 -Dhelp.lucene.tokenizer=standard -Xms128m -Xmx4192m -Xdock:icon=../Resources/Eclipse.icns -XstartOnFirstThread  -Dorg.eclipse.swt.internal.carbon.smallFonts -XX:MaxMetaspaceSize=4096m -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC

* Once Ekeko/X has started in the new Eclipse instance, new templates can be created and edited via the Ekeko > Ekeko/X menu. (A small [demonstration screencast](https://www.youtube.com/watch?v=CXNKyBIuAv8&feature=youtu.be) is available.)
* Several sample Java projects are also included to try out program searches. They can be found in the damp.ekeko.snippets/damp.ekeko.snippets/resources directory. Likewise, several sample templates and transformations can be found in the damp.ekeko.snippets/damp.ekeko.snippets/resources/EkekoX-Specificationss directory.
* Ekeko/X can also be controlled via a Clojure REPL. To start one, go to Ekeko > Start nREPL. Now go to the other Eclipse instance (from which Ekeko/X was launched) and check its console. It should print a link to the new REPL. Click the link to open the REPL.

**TODO Usage instructions search.clj and experiments.clj**
