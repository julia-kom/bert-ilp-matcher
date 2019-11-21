#!/usr/local_rwth/bin/zsh
#parameters $1 is ilp = {BASIC, RELAXED, RELAXED2, RELAXED3}
#parameters $2 is word similarity = {LIN, LEVENSHTEIN, JIANG, LEVENSHTEIN_LIN_MAX, LEVENSHTEIN_JIANG_MAX}
#parameters stepsize is $3..$n
ilp=$1
word=$2

#start all birth jobs
for stepsize in "${@:3}";
do
  sbatch batch-birth-test.sh $stepsize $ilp $word
done

#start all uni jobs
for stepsize in "${@:3}";
do
  sbatch batch-uni-test.sh $stepsize $ilp $word
done