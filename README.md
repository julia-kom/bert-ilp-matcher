## Functionality
This is a simultaneous label and behavior process model matcher.
Install local repositories via maven commands:
`install:install-file -Dfile=libs/ws4j-1.0.1.jar`,

`install:install-file -Dfile=libs/jawjaw-1.0.0.jar`

`install:install-file -Dfile=libs/ExRoRUBPP-dev.jar -DgroupId=com.iise.shudi.exroru -Dversion=1.0 -Dpackaging=jar -DartifactId=ExRORUBPP-dev`
## Run jar on server
1. scp ./rwth-cluster-shell to RWTH Cluster $HOME
2. scp ./eval-data to RWTH CLuster Home
3. execute `sbatch batch-test.sh ` in ./rwth-cluster-shell.
## General Structure of the matcher
1.  Convert EPC etc. to Petri net via Prom. This is actually not implemented and needs to be done **once** manually.
2.  Read pair of Labeled Petri Nets (and maybe check if it is sound, free choice WF-net)
3.  Construct Profile Relations for both petri nets
4.  Construct Label Similarity Matrix between the two processes' activities
5.  Preprocess the matches
6.  Run the ILP that simultaneously maximizes behavior and label similarity
7.  Postprocess the matches

## First Test Runs with following parameters:
`-n1 ./src/main/resources/pnml-files/Uni/Frankfurt_reduced.pnml -n2 ./src/main/resources/pnml-files/Uni/Frankfurt_reduced.pnml`