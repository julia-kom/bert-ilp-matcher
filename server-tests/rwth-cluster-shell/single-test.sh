#!/usr/local_rwth/bin/zsh
# set up environment
#SBATCH -t 0-02:00:00 # wallclock time
#SBATCH --exclusive #exclusive use
#SBATCH --partition c18m #choose the partition (https://doc.itc.rwth-aachen.de/display/CC/Hardware+of+the+RWTH+Compute+Cluster)
#SBATCH -N 1 #min max nodes needed
#SBATCH -n 1 #number of tasks to be launched
#SBATCH --mem-per-cpu=4GB # Memory per CPU in MB
#SBATCH --cpus-per-task=4 # CPU per task
#SBATCH -o ./slurm-report/output_%j.txt #output

#set missing links
module load MATH
module load gurobi/8.0.0

# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -n1 $HOME/eval-data/pnml/birth/birthCertificate_p31.pnml -n2 $HOME/eval-data/pnml/birth/birthCertificate_p250.pnml -gs $HOME/eval-data/goldstandard/birth/birthCertificate_p31-birthCertificate_p250.rdf -ilp Relaxed3 -pre-match