#!/bin/sh

# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -batch -gold-standard-path $HOME/eval-data/goldstandard/birth -net-path $HOME/eval-data/pnml/birth -ilp Relaxed -pre-match