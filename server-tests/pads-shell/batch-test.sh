#!/bin/sh
#parameters $1 is ilp = {BASIC, RELAXED, RELAXED2, RELAXED3}
#parameters $2 is word similarity = {LIN, LEVENSHTEIN, JIANG, LEVENSHTEIN_LIN_MAX, LEVENSHTEIN_JIANG_MAX}
#parameters $3 ilp-time-limit
#parameters stepsize is $4..$n
ilp=$1
word=$2
time=$3

#start all birth jobs
for stepsize in "${@:4}";
do
  sh batch-birth-test.sh $stepsize $ilp $word $time
done

#start all uni jobs
#for stepsize in "${@:3}";
#do
#  sbatch batch-uni-test.sh $stepsize $ilp $word $time
#done