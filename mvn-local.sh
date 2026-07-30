#!/usr/bin/env bash
# Local Maven launcher for this project.
# The bundled ./mvnw is broken in this environment (classworlds jar lives at boot/ not lib/boot/),
# and Windows java.exe cannot parse Git-bash /c/... paths, so we launch the classworlds launcher
# directly with Windows-style absolute paths.
#
# Usage: ./mvn-local.sh <maven args>   e.g.  ./mvn-local.sh -o compile   ./mvn-local.sh -o test
set -u

export JAVA_HOME="C:/Program Files/Java/jdk-18.0.2.1"
D="C:/Users/laotie_nb666/.m2/wrapper/dists/apache-maven-3.9.15/0226a00282e400185496f3b60ec5a3f029cbdc6893912937d4876d57695224e1"
CW="$D/boot/plexus-classworlds-2.9.0.jar"
PROJ="E:/Desktop/work/Life Guard/Life Gruad AgerEnd"

exec java -cp "$CW" \
  "-Dmaven.home=$D" \
  "-Dmaven.multiModuleProjectDirectory=$PROJ" \
  "-Dclassworlds.conf=$D/bin/m2.conf" \
  org.codehaus.plexus.classworlds.launcher.Launcher "$@"
