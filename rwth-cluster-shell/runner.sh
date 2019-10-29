#!/usr/bin/env zsh
#BSUB -J TEST
#BSUB -o ouput.txt
#BSUB -n 2
#BSUB -W 15
#BSUB -M 700
#BSUB -a openmp
#BSUB -u dominik.hueser@rwth-aachen.de
#BSUB -N
module switch intel gcc/6
sh batch-test.sh