#!/bin/bash
#parameters: $1 weight, $2 ilp, $3 word-sim, $4 ilp-time-limit, $5 profile

# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar eval -batch -gold-standard-path $HOME/eval-data/goldstandard/uni -net-path $HOME/eval-data/pnml/uni -similarity-weight $1 -ilp $2 -word-sim $3 -ilp-time-limit $4 -profile $5
