(ns test.damp.ekeko.snippets.experiments.launcher
  "Launcher for genetic search experiments, which will be executed in a separate Eclipse instance
   Note that you can load this namespace from a plain REPL; no Ekeko needed.."
  (:require [clojure.java.shell :as sh]))

(def config-path "/Users/soft/Documents/workspace-runtime2/experiment-config.txt")

(defn launch-eclipse-pde-test-runner [port]
  (sh/sh "java"
         "-Dosgi.requiredJavaVersion=1.7"
         "-XstartOnFirstThread" "-Dorg.eclipse.swt.internal.carbon.smallFonts"
         "-XX:MaxPermSize=256m" "-Xms40m" "-Xmx6144m" "-XX:MaxMetaspaceSize=4096m" "-XX:+CMSClassUnloadingEnabled" "-XX:+UseConcMarkSweepGC"
         "-Xdock:icon=../Resources/Eclipse.icns" "-XstartOnFirstThread" "-Dorg.eclipse.swt.internal.carbon.smallFonts"
         "-Declipse.pde.launch=true"
         "-Declipse.p2.data.area=@config.dir/p2"
         "-Dfile.encoding=UTF-8"
         
         ; Eclipse Mars params .. doesn't work! (on OS X) Once Eclipse starts, it quits on SWT invalid thread access .. used to be fixed with -XstartOnFirstThread VM param, but that doesn't seem to work
         ;"-Xbootclasspath/p:/Users/soft/Downloads/Eclipse_EkekoX.app/Contents/Eclipse/plugins/org.eclipse.jdt.debug_3.9.0.v20150528-1838/jdi.jar"
         ;"-classpath" "/Users/soft/Downloads/Eclipse_EkekoX.app/Contents/Eclipse/plugins/org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar" "org.eclipse.equinox.launcher.Main"
         
         "-Xbootclasspath/p:/Applications/eclipse/plugins/org.eclipse.jdt.debug_3.8.102.v20150115-1323/jdi.jar"
         "-classpath" "/Applications/eclipse/plugins/org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar" "org.eclipse.equinox.launcher.Main"
         "-os" "macosx" "-ws" "cocoa" "-arch" "x86_64" "-nl" "en_US"
         "-consoleLog" "-version" "3"
         "-port" (str port)
         "-testLoaderClass" "org.eclipse.jdt.internal.junit4.runner.JUnit4TestLoader"
         "-loaderpluginname" "org.eclipse.jdt.junit4.runtime"
         "-classNames" "test.damp.ekeko.snippets.experiments.GeneticSearchTest"
         "-application" "org.eclipse.pde.junit.runtime.uitestapplication"
         "-product" "org.eclipse.sdk.ide"
         "-data" "/Users/soft/Documents/workspace-runtime2" ;"/Users/soft/Documents/workspace/../junit-workspace"
         "-configuration" "file:/Users/soft/Documents/workspace2/.metadata/.plugins/org.eclipse.pde.core/pde-junit/"
         "-dev" "file:/Users/soft/Documents/workspace2/.metadata/.plugins/org.eclipse.pde.core/pde-junit/dev.properties"
         "-os" "macosx" "-ws" "cocoa" "-arch" "x86_64" "-nl" "en_US" 
         "-consoleLog" "-testpluginname" "damp.ekeko.snippets.plugin"))

(defn launch-eclipse-pde-test-listener [port]
  (sh/sh "java" 
         "-classpath" 
         (clojure.string/join ; TODO Probably don't need all of these jars...
                              ":"
                              ["/Users/soft/Downloads/pde_automate_Junit_tests-master/pde.test.utils/bin"
                               "/Applications/eclipse/plugins/org.eclipse.jdt.junit_3.7.300.v20140418-0836.jar"
                               "/Applications/eclipse/plugins/org.eclipse.jdt.junit.core_3.7.300.v20140409-1618.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-antlr.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-apache-bcel.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-apache-bsf.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-apache-log4j.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-apache-oro.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-apache-regexp.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-apache-resolver.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-apache-xalan2.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-commons-logging.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-commons-net.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-jai.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-javamail.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-jdepend.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-jmf.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-jsch.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-junit.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-junit4.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-launcher.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-netrexx.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-swing.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant-testutil.jar"
                               "/Applications/eclipse/plugins/org.apache.ant_1.9.2.v201404171502/lib/ant.jar"
                               "/Applications/eclipse/plugins/org.junit_4.11.0.v201303080030/junit.jar"
                               "/Applications/eclipse/plugins/org.hamcrest.core_1.3.0.v201303031735.jar"
                               "/Applications/eclipse/plugins/org.eclipse.equinox.common_3.6.200.v20130402-1505.jar"
                               "/Applications/eclipse/plugins/org.eclipse.core.runtime_3.10.0.v20140318-2214.jar"
                               "/Applications/eclipse/plugins/javax.annotation_1.2.0.v201401042248.jar"
                               "/Applications/eclipse/plugins/javax.inject_1.0.0.v20091030.jar"
                               "/Applications/eclipse/plugins/org.eclipse.osgi_3.10.2.v20150203-1939.jar"
                               "/Applications/eclipse/plugins/org.eclipse.equinox.weaving.hook_1.1.100.weaving-hook-20140821.jar"
                               "/Applications/eclipse/plugins/org.eclipse.osgi.compatibility.state_1.0.1.v20140709-1414.jar"
                               "/Applications/eclipse/plugins/org.eclipse.core.jobs_3.6.1.v20141014-1248.jar"
                               "/Applications/eclipse/plugins/org.eclipse.core.runtime.compatibility.registry_3.5.300.v20140128-0851/runtime_registry_compatibility.jar"
                               "/Applications/eclipse/plugins/org.eclipse.equinox.registry_3.5.400.v20140428-1507.jar"
                               "/Applications/eclipse/plugins/org.eclipse.equinox.preferences_3.5.200.v20140224-1527.jar"
                               "/Applications/eclipse/plugins/org.eclipse.core.contenttype_3.4.200.v20140207-1251.jar"
                               "/Applications/eclipse/plugins/org.eclipse.equinox.app_1.3.200.v20130910-1609.jar"])
         "pde.test.utils.PDETestResultsCollector" "Test" (str port)))

(defn launch-experiment 
  "Run the tests/experiments defined in test.damp.ekeko.snippets.experiments.experiments
   These experiments are executed in a separate Eclipse instance. 
   If Eclipse ever closes (due to running out of JVM metaspace), Eclipse will be automatically restarted and the experiment will continue.
   @param experiment-name    Experiment results will be written to a folder with this name
   @param resume             If false, we're starting an experiment from scratch. If true, we're resuming the experiment based on the output files that were already written.

  !! Make sure in advance an Ekeko/X editor is opened in the runtime Eclipse. 
  (This will trigger the Ekeko/X namespaces to be loaded. If this is postponed until the experiment, you run into 'method code too large' errors for some reason...)"
  [experiment-name resume]
  (let [workspace-folder "/Users/soft/Documents/workspace-runtime2/"
        config-path (str workspace-folder "experiment-config.txt")
        experiment-foldername (str experiment-name 
                                   "--" (.format (new java.text.SimpleDateFormat "dd-MM-yyyy--HH-mm") (new java.util.Date)))
        done-path (if resume
                    (str (slurp config-path) "done.txt")
                    (str workspace-folder experiment-foldername "/done.txt")
                    )]
    
    ; When starting a new experiment , write the folder that should contain the experiment's results to a configuration file
    (if (not resume)
      (spit config-path (str workspace-folder experiment-foldername "/")))
    
    ; Keep on running the experiment until it finishes
    (loop []
      (if (.exists (clojure.java.io/as-file done-path))
        (println "Experiment completed!")
        (do
          (println "Starting Eclipse...")
          (future (launch-eclipse-pde-test-listener "54974"))
          (launch-eclipse-pde-test-runner "54974")
          (recur))))
    ))

(comment
  (launch-experiment "JHotDraw-TemplateMethod-Experiment" true))