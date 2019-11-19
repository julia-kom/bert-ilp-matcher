#!/usr/local_rwth/bin/zsh
# set up environment
zhs slurm-setup.sh

# run batch test for uni
java -jar ./ilp-profile-matcher-1.0-SNAPSHOT-jar-with-dependencies.jar -n1 $HOME/eval-data/pnml/birth/birthCertificate_p31.pnml -n2 $HOME/eval-data/pnml/birth/birthCertificate_p250.pnml -gs $HOME/eval-data/goldstandard/birth/birthCertificate_p31-birthCertificate_p250.rdf -ilp Relaxed3 -pre-match