#!/usr/bin/env bash
#$1 result paths, $2 goldstandard, $3 strategy ,$4...$n thresholds
for thresh in "${@:4}";
do
    for d in $(find $1 -depth 1 -type d)
    do
        echo $d
       java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -retrospective -gold-standard-path $2 -result-path $d -pp $thresh -eval-strat $3
    done
done