# Automated generalization and refinement

Ekeko/X is a template-base search and transformation tool. As it sometimes isn't straightforward to design a template that produces exactly the desired code snippets, we have provided support to automatically generalize or refine a template group, such that it will match with a set of desired matching code snippets. This page details the process of setting up Ekeko/X and its support for automated generalization and refinement, and how to use it. We also explain how to reproduce the experiments that evaluate the effectiveness of this automated generalization and refinement approach.

**[Download experiment data](http://soft.vub.ac.be/~tmoldere/ekekox/experiment%20data.zip)** (157 MB)

## Installation from source

* If you haven't already, first install the [Eclipse](https://www.eclipse.org) IDE. (Ekeko/X has been tested using Eclipse Luna and Mars.) Note that the chosen Eclipse package should include the Eclipse Plugin Development Environment. (This is the case for e.g. "Eclipse IDE for Java EE Developers" and "Eclipse for RCP and RAP Developers")
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
* Several sample Java projects are also included to try out program searches. They can be found in the damp.ekeko.snippets/damp.ekeko.snippets/resources directory. In order to match templates in a project, it should be Ekeko-enabled. (Right-click a project > Configure > Include in Ekeko Queries) Several sample templates and transformations can also be found in the damp.ekeko.snippets/damp.ekeko.snippets/resources/EkekoX-Specifications directory.
* Aside from the graphical user interface, Ekeko/X can also be controlled via a Clojure REPL. To start one, go to Ekeko > Start nREPL. Now go to the other Eclipse instance (from which Ekeko/X was launched) and check its console. It should show a link to the new REPL. Click this link to open the REPL.

## Running the genetic search algorithm

* Once the REPL is up and running, the code related to the genetic search algorithm, which performs automated generalization and refinement of templates, can be found in the damp.ekeko.snippets.plugin/src/damp/ekeko/snippets/geneticsearch package. To start the algorithm, first open search.clj in this package. This file can be loaded into the REPL via Clojure > Load file in REPL. You may also want to switch to the file's namespace via Clojure > Switch REPL to file's namespace.
* Before running the algorithm, note the "config-default" definition. This map defines all options available to the genetic algorithm, and their default values. All of these options can be set via keyword arguments when invoking the algorithm.
* The genetic algorithm can be started using the evolve function. The only required argument is verifiedmatches, the set of desired matches that the algorithm uses as an oracle. All other arguments are optional keyword arguments, and correspond to the options in the config-default map.
* An example invocation of the evolve function can be found in the large (comment) block at the bottom of the search.clj file. This comment block contains the slurp-from-resource and run-example function definitions. To add these function definitions, select them and press Ctrl/Cmd+Enter. You can now call run-example in the REPL by simply entering (run-example). Note that this example uses the TestCase-JDT-CompositeVisitor sample project. It should be imported into the Eclipse instance running Ekeko/X. (The project can be found in damp.ekeko.snippets/damp.ekeko.snippets/resources) Once imported, it should also be Ekeko-enabled: Right-click the project > Configure > Include in Ekeko queries
* When the evolve function is running, all templates of each generation will be saved in a new directory of Eclipse's workspace, i.e. the Eclipse instance running Ekeko/X. In this directory, you'll also find a results.csv file, which stores more details regarding running time and fitness values of each generation.

## Running the experiments

**[Experiment data](http://soft.vub.ac.be/~tmoldere/ekekox/experiment%20data.zip) is available online.** (157 MB ; updated Feb. 2016) This .zip file contains all measurements in .csv format, fitness and performance charts for each run, all template groups of each generation of all runs.

* The experiments to evaluate the genetic search algorithm are defined in the /damp.ekeko.snippets.plugin.test/src/test/damp/ekeko/snippets/experiments/experiments.clj file. Load this file into the REPL.
* Each experiment can be found in a test definition (i.e. a "deftest"). Simply call one of the tests in the REPL to run the corresponding experiment. For example, (jh-template-method) starts the experiment to generalize one template of the Template method design pattern into a template group that should match all instances of that design pattern. Note that the experiments require the "6 - JHotDraw v5.1" sample project to be imported. (can be found in damp.ekeko.snippets.plugin.test/resources/P-MARt)
