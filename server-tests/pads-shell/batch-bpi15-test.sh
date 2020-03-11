#!/bin/bash
#parameters: $1 weight, $2 ilp, $3 word-sim, $4 ilp-time-limit, $5 profile, $6 log path

# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar eval -batch -gold-standard-path $HOME/eval-data/goldstandard/bpi15 -log-path $HOME/eval-data/xes/bpi15  -net-path $HOME/eval-data/pnml/bpi15  -similarity-weight $1 -ilp $2 -word-sim $3 -ilp-time-limit $4 -profile $5
