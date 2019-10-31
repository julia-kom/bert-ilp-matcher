#!/usr/local_rwth/bin/zsh
# ask for 10 GB memory
#SBATCH --mem-per-cpu=10240M   #M is the default and can therefore be omitted, but could also be K(ilo)|G(iga)|T(era)
# name the job
#SBATCH --job-name=BATCH_TEST
# declare the merged STDOUT/STDERR file
#SBATCH --output=output.%J.txt

### begin of executable commands
#Add Gurobi
sh batch-test.sh