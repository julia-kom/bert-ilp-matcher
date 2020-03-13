#!/bin/bash
#parameters $1 is ilp = {BASIC, SYMMETRIC or CUSTOM_IDENTIFICATION}
#parameters $2 is word similarity = {LIN, LEVENSHTEIN, JIANG, LEVENSHTEIN_LIN_MAX, LEVENSHTEIN_JIANG_MAX}
#parameters $3 ilp-time-limit
#parameters $4 profile
#parameters stepsize is $5..$n
ilp=$1
word=$2
time=$3
profile=$4

#start all bpi15 jobs
for stepsize in "${@:5}";
do
 ./batch-bpi15-test.sh $stepsize $ilp $word $time $profile
done
