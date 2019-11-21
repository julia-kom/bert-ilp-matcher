#!/usr/bin/env bash
#$1 result paths, $2 goldstandard, $3 threshold, $4 strategy
for d in $(find $1 -depth 1 -type d)
do
    echo $d
   java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -retrospective -gold-standard-path $2 -result-path $d -pp $3 -eval-strat $4
done