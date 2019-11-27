#!/bin/sh
#parameters: $1 weight, $2 ilp, $3 word-sim, $4 ilp-time-limit

# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -batch -gold-standard-path $HOME/eval-data/goldstandard/birth -net-path $HOME/eval-data/pnml/birth -similarity-weight $1 -ilp $2 -pre-match -word-sim $3 -ilp-time-limit $4