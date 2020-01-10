#!/usr/local_rwth/bin/zsh
#parameters: $1 weight, $2 ilp, $3 word-sim, $4 ilp-time-limit
#setup slurm environment
#SBATCH -t 0-02:00:00 # wallclock time
##SBATCH --exclusive #exclusive use
#SBATCH --partition c18m #choose the partition (https://doc.itc.rwth-aachen.de/display/CC/Hardware+of+the+RWTH+Compute+Cluster)
#SBATCH -N 1 #min max nodes needed
#SBATCH -n 1 #number of tasks to be launched
#SBATCH --mem-per-cpu=4GB # Memory per CPU in MB
#SBATCH --cpus-per-task=12 # CPU per task
#SBATCH -o ./slurm-report/output_%j.txt #output

#set missing links
module load MATH
module load gurobi/8.0.0

# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -batch -gold-standard-path $HOME/eval-data/goldstandard/uni -net-path $HOME/eval-data/pnml/uni -similarity-weight $1 -ilp $2 -pre-match -word-sim $3 -ilp-time-limit $4