#!/usr/local_rwth/bin/zsh
#parameters $1 is ilp
#parameters stepsize $2..$n
ilp=$1

#start all birth jobs
for stepsize in "$@"
do
  sbatch batch-birth-test.sh --export=1=$stepsize,2=$ilp
done

#start all uni jobs
for stepsize in "$@"
do
  sbatch batch-uni-test.sh --export=1=$stepsize,2=$ilp
done