#!/bin/sh

# run batch test
java -cp ilp-profile-matcher-1.0-SNAPSHOT.jar Evaluation -batch -gold-standard-path ./eval-data/goldstandard/birth -net-path ./eval-data/pnml/birth -ilp Relaxed -pre-match