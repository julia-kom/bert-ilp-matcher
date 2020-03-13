## Installation
Run the program on a UNIX system:
run `install.sh`, which sets up the local maven repository, cleans, compiles, tests and package the project into a jar `ilp-matcher.jar` located in the root folder of this project. 
This jar is stand-alone, meaning all dependencies are included in the jar. It can be transfered to a server without recompiling it for that system.

**Quickstart:**

Use the `ilp-matcher.jar` provided in this repository containing all needed dependencies.

## Run the Matcher
Executing the matcher is reached by executing the jar with the additional argument `matcher` as in the following example:

`java -jar ilp-profile-matcher.jar matcher net-1 <pnml-file> net-2 <pnml-file> <additional args>`

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
  * `BASIC` - BASIC ILP without any variable reduction. 
  * `SYMMETRIC` (former: `BASIC2`) - Like `BASIC` but we make use of the symmetry property of symmetric Profiles (ARP, BP). Please do not use this with non symmetric profiles (BPP.LOG_EF,LOG_DF)
  * `CUSTOM_IDENTIFICATION`(former `BASIC5`)- Like `BASIC` but we use the Profile specific identification function for relation similarity in the target function. Use this especially for LOG_EF and LOG_DF.
* `postprocess-threshold <double>` (`pp <double>`) - Value in range [0,1] defining the minimum confidence of a produced correspondence to be included in to the returned matching. Default is _0_
* `pre-match` (`pm`) - Enable pre-matching _(Default: disabled)_
* `ilp-time-limit <int>` (`tl <int>`) - Maximum time (in seconds) until the ILP is terminated. The up to that point best solution is returned as result of the ILP. Default is _INFINITE_.
* `ilp-node-limit <int>` (`nl <int>`) - Maximum number of nodes traversed in the Branch and Bound algorithm until the ILP is terminated. The up to that point best solution is returned as result of the ILP. Default is _INFINITE_.
* `sys-print` (`sys`) - Show all command line outputs (not recommended, as it I/O operations are slow)

The resulting matching as well as the similarity score is printed to std out.
Let us regard exemplary executions: 

### Label Only Matcher (Lin Word Sim)
The label only matcher is reached by setting the behavior share to zero.
`java -jar ./ilp-profile-matcher.jar matcher -n1 ./eval-data/pnml/app_store/app_create_account_comp1.pnml 
-n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml 
-s 0 
-w LIN 
-i SYMMETRIC
-sys`

### 50:50 Label and BP Matcher
`java -jar ./ilp-profile-matcher.jar matcher -n1 ./eval-data/pnml/app_store/app_create_account_comp1.pnml -n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0.5 -p BP -w LIN -i SYMMETRIC -sys`

### 50:50 Label and BP+ Matcher with Prematch
We want to make use of the similarity functions defining soft similarities between profile relations: 
`java -jar ./ilp-profile-matcher.jar matcher -n1 ./eval-data/pnml/app_store/app_create_account_comp1.pnml -n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0.5 -p BPP -w LIN -i CUSTOM_IDENTIFICATION -pm -sys`

### 50:50 Label and Eventually Follows with Logs
`java -jar ./ilp-profile-matcher.jar matcher -n1 ./eval-data/pnml/bpi15/BPIC15_2_07_OPS.pnml -l1 ./eval-data/xes/bpi15/BPIC15_2_07_OPS.xes -n2 ./eval-data/pnml/bpi15/BPIC15_4_07_OPS.pnml -l2 ./eval-data/xes/bpi15/BPIC15_4_07_OPS.xes -s 0.5 -p LOG_EF -w LIN -i CUSTOM_IDENTIFICATION -sys`


## Run the Evaluation
Executing the evaluation tool is reached by executing the jar with the additional argument `eval` as in the following example.

`java -jar ./ilp-profile-matcher.jar eval <additional args>`

The output is written to `./eval-results/`.


The additional arguments are the above which define the matcher to be evaluated and additionally the following evaluation specific parameters:
### Batch Evaluation
Run the matcher on each possible pair of nets in the net-path folder which got a corresponding goldstandard file. 
Association of pairs of nets and the goldstandard is done via the name:

_net1.pnml_ and _net2.pnml_ correspond to a goldstandard _net1-net2.rdf_ in the goldstandard folder.

If additional logs are used, then the log needs to be named exactly as the net it corresponds to. For example:

_net1.xes_ corresponds to _net1.pnml_

* **`batch` (`b`) - Enable batch evaluation**
* **`gold-standard-path <path>` (`gsp`) - Path to the folder containing the Goldstandard Files.**
* **`net-path` (`np`) - Path to the folder containing the net files in PNML**
* `log-path <path>` (`lp`) - Path to the folder with the XES logs
* `eval-strat <strategy>` (`e`)  - Choose the evaluation strategy used `<strategy>` can either be 
  _* `BINARY`- Every 1:1 correspondence counts as one TP/FP/FN/TN. Complex correspondences are interpreted as several 1:1 correspondences._
  * `STRIC_BINARY` - Every correspondence, regardless if simple or complex counts as one TP/FP/FN/TN. This was not used in evaluation of the thesis.


Additionally the parameters of the matcher (described in the previous section) can be specified. Exemmplary analysis are:

`java -jar ./ilp-profile-matcher.jar eval -batch -gold-standard-path ./eval-data/goldstandard/bpi15 -net-path ./eval-data/pnml/bpi15 -log-path ./eval-data/xes/bpi15  -ilp CUSTOM_IDENTIFICATION -p LOG_DF -sys -s 0.1 -tl 10`

The output is a folder `batch-<dataset>-<eval-strategy>-<date><time>`, containing:
* `aggResults.eval`: CSV file with the evalaution results per matching as well as macro and micro aggregation over all matchings.
* `config.log`: Matcher configuration in JSON format.
* `<net1>-<net2>.rdf` the alignment of <net1> and <net2> in RDF format (standard in Process Model Matching PMMC15 and OAEI17)

The `aggResults.eval` file is in CSV format and contains for pair of nets in the net-path for which there exists a goldstandard a row with the matching result information (Name,TP,FP,FN,PRECISION,RECALL,FSCORE, Similarity score and timing, as well as GAP ILP information). 
The structure of the file is equivalent to the batch test, but obviously can timing information not be stored, as it is done retrosepctive. 



### Retrospective Evalution
When we do not want to run the matcher but evaluate an already given alignment, which was result of a previous matching or a different matcher, then we use the retrospective analysis function:
Needed therefore is the goldstandard, the result of the previous matching and the evaluation strategy. Optionally we can postprocess the matching based on a minimum confidence threshold.
The parameters are the following:
* **`retrospective` (`r`) - Enable retrospective evaluation**
* **`gold-standard-path <path>` (`gsp`) - Path to the folder containing the Goldstandard Files.**
* **`result-path <path>` (`rp`) - Path where the RDF alignments of a previous matching lay.**
* `eval-strat <strategy>` (`e`)  - Choose the evaluation strategy used `<strategy>` can either be 
  * _`BINARY`- Every 1:1 correspondence counts as one TP/FP/FN/TN. Complex correspondences are interpreted as several 1:1 correspondences._
  * `STRIC_BINARY` - Every correspondence, regardless if simple or complex counts as one TP/FP/FN/TN. This was not used in evaluation of the thesis.

* `postprocess-threshold` (`pp`) - postprocessing threshold of the given alignment. Default set ot _0_.

Exemplary execution would look like this:

`java -jar ./ilp-profile-matcher.jar eval -retrospective -gold-standard-path ./eval-data/goldstandard/sap_original -result-path ./eval-results/_pmmc15_result/AML-PM/sap -pp 0.3`

The output is a folder `retrospecitve-<dataset>-<eval-strategy>-<date><time>`, containing a `aggRetrosepctiveResults.eval`. 
The file is in CSV format and contains for pair of nets in the net-path for which there exists a goldstandard a row with the matching result information (Name,TP,FP,FN,PRECISION,RECALL,FSCORE). 
The structure of the file is equivalent to the batch test, but obviously can timing information not be stored, as it is done retrospective. 
Additionally the config.log file gives the setup of the retropective analysis in json format. If possible it also copies the setup of the previous matching execution if that included in the result path.

### Goldstandard Analysis
A simple goldstandard analysis about the number of correspondences (1:1,1:n and trivial correspondences) can be executed in the following way:

`java -jar ./ilp-profile-matcher.jar eval -gs-eval -gold-standard-path <gs-path> -net-path <net-path>`

where `<gs-path>` is the folder of goldstandards to analyze and `<net-path>` the corresponding nets the goldstandard are regarding.

An exemplary execution would look like this:

`java -jar ./ilp-profile-matcher.jar eval -gs-eval -gold-standard-path ./eval-data/goldstandard/bpi15 -net-path /eval-data/pnml/bpi15`

The output is a folder `goldstandard-<net-path>-<date><time>`, containing a `goldstandard.eval`. 
The file is in CSV format and contains for each goldstandard file in the <path> a row with the correspondence information.

### Simple Net Analysis
A simple net analysis gives us information about the data set. Especially how many transitions (silent and non silent) are included.
It can be run by
 
 `java -jar ./ilp-profile-matcher.jar eval -net-eval -net-path <path> -profile <profile>`

`<path>` is the path to the nets to be analyzed and `<profile>` the relational profile we count the number of relations in the model. 
The later information is not used in the paper though.  Exemplary execution looks like this:
 
`java -jar ./ilp-profile-matcher.jar eval -net-eval -net-path ./eval-data/pnml/sap -profile BP`

The output is a folder `net-<profile>-<date><time>`, containing a `net.eval`. 
The file is in CSV format and contains for each net file in the <path> a row with the net information, as well as the average over all of the nets as an own row.

## Server Tests
On a linux server with installed gurobi, scripts can be used to run a batch of tests automatically.
Copy the `eval-data` folder as well as the `pads-shell` folder into the `$HOME` directory of the server.
The script automatically iterates the sim weight parameter for a given configuration on either the PMMC datasets or the BPI15 log enhanced dataset.
### PMMC Dataset Tests
The batch evaluation for non log profile tests on the PMMC datasets (Uni,Birth, SAP) can be executed via 

`sh batch-test.sh <ilp> <word-sim> <ilp-time-limit> <profile> <sim-weight-1> ... <sim-weight-n>`

### Log Enhanced Dataset BPI15
The tests on the BPI15 daaset for log impact comparison can be run via 

`sh batch-log-test.sh <stepsize> <ilp> <word-sim> <ilp-time-limit> <profile> <sim-weight-1> ... <sim-weight-n>`

### Enable SLURM
There is a SLURM server version of the scripts in the `rwth-cluster-shell` folder. 
Though due to switching from SLURM to a stand alone linux node these scripts were not maintained nor tested. Therefore, denoted as **DEPRECATED**. 

### Retrospective Evaluation. 
The retrospective evaluation script allows us to perform a analysis of a given matching, where we vary the postprocessing threshold arbitrarily:

`sh retrospective-eval.sh <result-folder> <goldstandard-path> <post-processing-thresh-1> ... <post-processing-thresh-n>`