#!/usr/bin/env bash

#install XES package from lib folder to local maven repo
mvn install:install-file -Dfile=libs/OpenXES-20181205.jar -DgroupId=org.deckfour -Dversion=1.0.0 -Dpackaging=jar -DartifactId=xes

#install original BP Plus package (though own implementation is used due to too many bugs in paper implementation)
mvn install:install-file -Dfile=libs/ExRORU.jar -DgroupId=com.iise.shudi -Dversion=1.0 -Dpackaging=jar -DartifactId=exroru

#install gurobi 8.0.0 connector (this does NOT install gurobi!)
mvn install:install-file -Dfile=libs/gurobi-8-0-0.jar -DgroupId=gurobi -Dversion=8.0.0 -Dpackaging=jar -DartifactId=gurobi-linux64

#install wordnet package
mvn install:install-file -Dfile=libs/ws4j-1.0.0.jar -DgroupId=org.xerial -Dversion=3.7.2 -Dpackaging=jar -DartifactId=sqlite-jdbc



#create standalone jar
mvn clean compile test assembly:single -f pom.xml

#copy jar to this folder
cp ./target/ilp-profile-matcher.jar ./ilp-profile-matcher.jar

#copy jar to the server folders
#cp ./target/ilp-profile-matcher.jar ./server-tests/pads-shell/ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar
#cp ./target/ilp-profile-matcher.jar ./server-tests/rwth-cluster-shell/ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar
