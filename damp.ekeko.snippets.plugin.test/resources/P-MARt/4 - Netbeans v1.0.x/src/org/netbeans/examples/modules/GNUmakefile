HERE=org/netbeans/examples/modules
UP=../../../..
JAR=/usr/local/jdk1.2/bin/jar
# Should have org/netbeans/ide & org/netbeans/examples/modules under it
# (e.g. symlinks); override like `make SRCHOME=...'.
SRCHOME=/home/jglick/starteam/gandalfsrc

modules:=$(shell find * -name \*.mf \! -name apiwiz.mf -print | sed -e 's/\.mf//g')

all: $(modules:%=%.jar)

-include $(modules:%=%.moddep)

%.moddep: %.mf
	find $* -name \*.java -print | sed -e 's!\(.*\)\.java!$*.jar: \1.class!g' > $@
# XXX also deps on images, bundles, templates, ...

%.jar: %.mf
	if [ -d $*.templates/Templates ]; then \
		cd $*.templates/Templates; \
		$(JAR) cvfM0 ../../$*/templates.jar *; \
	fi
	ln -sf $*.mf $*-input-manifest.txt
	cd $(UP); $(JAR) cvfm $(HERE)/$*.jar $(HERE)/$*.mf `find $(HERE)/$* \! -type d \! -name \*.dep -print` $(HERE)/$*-input-manifest.txt
	rm -f $*-input-manifest.txt
# rm -f $*/templates.jar

%.class: %.java
	cd $(SRCHOME); ./Compile --cleanup --what $(HERE)/$(*D)

.PRECIOUS: %.class
