#!/usr/local_rwth/bin/zsh
#SBATCH -t 0-01:00:00 # wallclock time

#SBATCH --partition c18m #choose the partition (https://doc.itc.rwth-aachen.de/display/CC/Hardware+of+the+RWTH+Compute+Cluster)
#SBATCH -N 1 1 #min max nodes needed
#SBATCH -n 1 #number of tasks to be launched
#SBATCH --mem-per-cpu= 10GB # Memory per CPU in MB
#SBATCH --cpu-per-task=1 # CPU per task

#set missing links
module load MATH
module load gurobi/8.0.0
# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -batch -gold-standard-path $HOME/eval-data/goldstandard/birth -net-path $HOME/eval-data/pnml/birth -ilp Relaxed3 -pre-match