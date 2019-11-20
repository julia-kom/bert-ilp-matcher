#!/usr/local_rwth/bin/zsh
#parameters $1 is ilp = {Basic, Relaxed, Relaxed2, Relaxed3}
#parameters $2 is word similarity = {Lin, Levenshtein, Jiang, Levenshtein-Lin-Max, Levenshtein-Jiang-Max}
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