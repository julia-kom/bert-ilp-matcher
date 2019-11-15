#!/usr/local_rwth/bin/zsh
#SBATCH -t 0-01:00:00 # wallclock time
#SBATCH -J "batch gold-standard-path $HOME/eval-data/goldstandard/birth net-path $HOME/eval-data/pnml/birth ilp Relaxed pre-match" # Job name
# don't forget to >> chmod +x ./batch-test.sh
#set missing links
module load MATH
module load gurobi/8.0.0
# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -n1 $HOME/eval-data/pnml/birth/birthCertificate_p31.pnml -n2 $HOME/eval-data/pnml/birth/birthCertificate_p250.pnml -gs $HOME/eval-data/goldstandard/birth/birthCertificate_p31-birthCertificate_p250.rdf -ilp Relaxed3 -pre-match