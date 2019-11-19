#!/usr/local_rwth/bin/zsh
#SBATCH -t 0-02:00:00 # wallclock time
#SBATCH --exclusive #exclusive use
#SBATCH --partition c18m #choose the partition (https://doc.itc.rwth-aachen.de/display/CC/Hardware+of+the+RWTH+Compute+Cluster)
#SBATCH -N 1 1 #min max nodes needed
#SBATCH -n 1 #number of tasks to be launched
#SBATCH --mem-per-cpu= 4GB # Memory per CPU in MB
#SBATCH --cpu-per-task=4 # CPU per task
#SBATCH --exclusive #make exclusive for benchmarking

#set missing links
module load MAT
module load gurobi/8.0.0