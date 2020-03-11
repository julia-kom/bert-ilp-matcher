#!/bin/bash
#parameters $1 is ilp = {BASIC, RELAXED, RELAXED2, RELAXED3}
#parameters $2 is word similarity = {LIN, LEVENSHTEIN, JIANG, LEVENSHTEIN_LIN_MAX, LEVENSHTEIN_JIANG_MAX}
#parameters $3 ilp-time-limit
#parameters $4 profile
#parameters stepsize is $5..$n
ilp=$1
word=$2
time=$3
profile=$4

#start all birth jobs
for stepsize in "${@:5}";
do
  ./batch-birth-test.sh $stepsize $ilp $word $time $profile
done

#start all uni jobs
for stepsize in "${@:5}";
do
 ./batch-uni-test.sh $stepsize $ilp $word $time $profile
done

#start all sap jobs
for stepsize in "${@:5}";
do
 ./batch-sap-test.sh $stepsize $ilp $word $time $profile
done
