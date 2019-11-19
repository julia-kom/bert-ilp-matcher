#!/usr/local_rwth/bin/zsh
#parameters: $1 weight, $2 ilp
#setup slurm environment
zsh slurm-setup.sh

# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -batch -gold-standard-path $HOME/eval-data/goldstandard/uni -net-path $HOME/eval-data/pnml/uni -similarity-weight $1 -ilp $2 -pre-match