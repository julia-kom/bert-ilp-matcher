#!/usr/bin/env bash

#install XES package from lib folder to local maven repo
mvn install:install-file -Dfile=libs/OpenXES-20181205.jar -DgroupId=org.deckfour -Dversion=1.0.0 -Dpackaging=jar -DartifactId=xes

#install original BP Plus package (though own implementation is used due to too many bugs in paper implementation)
mvn install:install-file -Dfile=libs/ExRORU.jar -DgroupId=com.iise.shudi -Dversion=1.0 -Dpackaging=jar -DartifactId=exroru

#create standalone jar
mvn clean compile test assembly:single -f pom.xml

#copy jar to this folder
cp ./target/ilp-profile-matcher.jar ./ilp-profile-matcher.jar

#copy jar to the server folders
#cp ./target/ilp-profile-matcher.jar ./server-tests/pads-shell/ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar
#cp ./target/ilp-profile-matcher.jar ./server-tests/rwth-cluster-shell/ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar
