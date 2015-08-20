#!/usr/bin/env python

import os,sys,time,re

chapters = '''
data
elastic-runtimes
scale-patterns
extensions
web
security
operations
integration
continuous-delivery
bootcamp
batch
testing
forklifting
build
ee
configuration
rest
'''

chapters = chapters.strip()

chapters = [x for x in chapters.split(os.linesep) if x.strip() != '']
#print len(chapters)


gi = '''
*~
.#*
*#
*.sw*
_site/
.factorypath
.gradletasknamecache
.DS_Store
/application.yml
/application.properties
asciidoctor.css
atlassian-ide-plugin.xml
bin/
build/
dump.rdb
out
target/
test-output

# Eclipse artifacts, including WTP generated manifests
.classpath
.project
.settings/
.springBeans
spring-*/src/main/java/META-INF/MANIFEST.MF

# IDEA artifacts and output dirs
*.iml
*.ipr
*.iws
.idea
'''.strip()

for c in chapters :
    git = ''' git@github.com:cloud-native-java/%s.git ''' % c
    cmd = ''' git clone git@github.com:cloud-native-java/%s.git''' % (c)
    cmd = ''' git submodule add %s code/%s ''' % (git, c )
    print cmd
