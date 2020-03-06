## Installation

run `install.sh`, which sets up the local maven repository, cleans, compiles, tests and package the project into a jar `ilp-matcher.jar`. 
This jar is stand-alone, meaning all dependencies are included in the jar. It can be transfered to a server without recompiling it for that system.


## Run the Matcher
Executing the matcher is reached by executing the jar with the additional argument `matcher` as in the following example:

`java -jar ilp-matcher.jar matcher <additional args>`

Arguments to specify are the following. Mandatory arguments in **bold**. Default values in _italic_. Either the full argument name or the short version can be used as input
* **`net-1 <pnml-file>` (`n1 <pnml-file>`) - Path to sound, free choice, WF-Net File in PNML format of the first process**
* **`net-2 <pnml-file>` (`n2`) - Path to sound, free choice, WF-Net File in PNML format of the second process**
* `log-1 <xes-file>` (`l1`) - Path to Log file of the first process in XES format 
* `log-2 <xes-file>` (`l2`) - Path to Log file of the second process in XES format 
* `profile <profile>` (`p <profile>`) - Relational Profile to use. Options for `<profile>` are:
  * `ARP` - Alpha Relational Profile
  * _`BP` - Behavioral Profile_
  * `BPP` - BP+
  * `LOG_DF` - Directly Follows Relational Profile (can deal with additional log files)
  * `LOG_EF` - Eventually Follows Relational Profile (can deal with additional log files)
* `sim-weight <double>` (`s <double>`) - Similarity Weight determining how much behavior information is present in the target function in the ILP. 0: label information only. 1: profile information only. The value must be in range [0,1]. Default _0.3_.  
* `word-sim <word-sim>` (`w <word-sim>`) - Choose the word similarity, which is used in the basic bag of words label similarity. `<word-sim>`can take the following values:
  * `LIN` - Lin Similarity (Semantic Word Similarity based on WordNet)
  * `JIANG` - Jiang Similarity (Semantic Word Similarity based on WordNet)
  * `LEVENSHTEIN` - Levenshtein Similarity (Syntactic Word Similarity based on Editing Distance)
  * _`LEVENSHTEIN_LIN_MAX` - Maximum of Levenshtein and Lin Similarity_
  * `LEVENSHTEIN_JIANG_MAX` - Maximum of Levenshtein and Jiang Similarity
* `ilp <ilp>` (`i <ilp>`) 
  * 
* `postprocess-threshold <double>` (`pp <double>`) - Value in range [0,1] defining the minimum confidence of a produced correspondence to be included in to the returned matching. Default is _0_
* `pre-match` (`pm`) - Enable pre-matching _(Default: disabled)_
* `ilp-time-limit <int>` (`tl <int>`) - Maximum time (in seconds) until the ILP is terminated. The up to that point best solution is returned as result of the ILP. Default is _INFINITE_.
* `ilp-node-limit <int>` (`nl <int>`) - Maximum number of nodes traversed in the Branch and Bound algorithm until the ILP is terminated. The up to that point best solution is returned as result of the ILP. Default is _INFINITE_.
* `sys-print` (`sys`) - Show all command line outputs (not recommended, as it I/O operations are slow)

The resulting matching as well as the similarity score is printed to std out.
Let us regard exemplary executions: 

### Label Only Matcher (Lin Word Sim)
The label only matcher is reached by setting the behavior share to zero.
`java -jar ilp-matcher.jar matcher -n1 ./eval-data/pnml/app_store/app_create_account_comp1.pnml 
-n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml 
-s 0 
-w LIN 
-i BASIC2 
-sys`

### 50:50 Label and BP Matcher
`java -jar ilp-matcher.jar matcher -n1 ./eval-data/pnml/app_store/app_create_account_comp1.pnml -n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0.5 -p BP -w LIN -i BASIC2 -sys`

### 50:50 Label and BP+ Matcher with Prematch
We want to make use of the similarity functions defining soft similarities between profile relations: 
`java -jar ilp-matcher.jar matcher -n1 ./eval-data/pnml/app_store/app_create_account_comp1.pnml -n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0.5 -p BPP -w LIN -i BASIC5 -pm -sys`

### 50:50 Label and Eventually Follows with Logs
`java -jar ilp-matcher.jar matcher -n1 ./eval-data/pnml/bpi15/BPIC15_2_07_OPS.pnml -l1 ./eval-data/xes/bpi15/BPIC15_2_07_OPS.xes -n2 ./eval-data/pnml/bpi15/BPIC15_4_07_OPS.pnml -l2 ./eval-data/xes/bpi15/BPIC15_4_07_OPS.xes -s 0.5 -p LOG_EF -w LIN -i BASIC5 -sys`


## Run the Evaluation
Executing the evaluation tool is reached by executing the jar with the additional argument `eval` as in the following example:

`java -jar ilp-matcher.jar eval <additional args>`








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