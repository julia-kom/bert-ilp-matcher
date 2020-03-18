package bpm.evaluation;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.alignment.Result;
import bpm.ippm.ilp.AbstractILP;
import bpm.ippm.matcher.MatchingPipeline;
import bpm.ippm.matcher.Preprocessor;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.similarity.Word;
import org.json.simple.parser.JSONParser;
import org.jbpt.petri.NetSystem;
import org.json.JSONException;
import org.json.simple.JSONObject;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static bpm.ippm.profile.AbstractProfile.createProfile;
import static bpm.ippm.matcher.Preprocessor.parseFile;
import static java.lang.System.exit;

/**
 * Pipeline for evaluation. Following evaluations are possible:
 *
 * Net evaluation: Analysis of a set of petri nets in pnml format with respect to a given profile (no log information used)
 * Goldstandard Evaluation: Analysis of a set of goldstandards: #complex, #simple, #trivial correspondences
 * Batch Evaluation: Perform the matching followed by an evaluation of each pair of processes in the batch path for which a goldstandard exists in the goldstandard path
 * Retrospective Evaluation: Given a matching hypothesis in RDF format, calculate the evaluation over the set of alignments
 * Single Evaluation: Evaluation of a single pair of models (optionally with logs)
 */
public class Pipeline{
    //Standard matcher options
    private MatchingPipeline matchingPipeline;

    //Evaluation specific options
    private boolean batch;
    private boolean netEval;
    private AbstractProfile.Profile netProfile;
    private boolean retrospective;
    private Path resultPath;
    private Path batchPath;
    private Path processLogPath;
    private File net1;
    private File log1;
    private File net2;
    private File log2;
    private File goldStandard;
    private Path goldStandardPath;
    private Eval.Strategies evalStrat;
    private String logFolder = "";
    private double postprocessingThreshold;
    private boolean gsEval;


    public void run(){

        // Run a net evaluation test
        if(netEval){
            netAnalysis();

        // Run a gold standard evaluation
        }else if(gsEval){
            goldstandardAnalysis();

        //run a batch evaluation
        } else if(batch) {
            List<Eval> evals = batchEval();
            AggregatedEval aggregatedEval = new AggregatedEval(evals);
            try {
                aggregatedEval.toCSV(new File("./eval-results/" + logFolder + "/aggResults.eval"));
            }catch(IOException e){
                System.out.println("It was not possible to write the aggregated result CSV!");
            }

        // Run a retrospective test
        }else if(retrospective){
            List<Eval> evals = retrospectiveEval(resultPath,goldStandardPath);
            AggregatedEval aggregatedEval = new AggregatedEval(evals);
            try {
                aggregatedEval.toCSV(new File("./eval-results/" + logFolder + "/aggRetrospectiveResults.eval"));
            }catch(IOException e){
                System.out.println("It was not possible to write the aggregated result CSV!");
            }
        }
        //Run a single evaluation test
        else {
            try {
                Eval eval = singleEval(net1, log1, net2, log2, goldStandard);
                try {
                    eval.toCSV(new File("eval-results/" + logFolder + "/" + eval.getName() + ".eval"));
                } catch (IOException e) {
                    System.out.println("It was not possible to write the single result CSV: " + eval.getName());
                }
            } catch (Exception e) {
                System.out.println("Evaluation of " + net1.getName() + " to " + net2.getName() +
                        "threw and Exception: " + e.getMessage());
                exit(1);
            }
        }
    }

    /**
     * Perform the matching and the evaluation of a single pair of processes (log model pairs)
     * @param n1 net 1
     * @param l1 log 1 optional if no log is present
     * @param n2 net 2
     * @param l2 log 2
     * @param gs gold standard
     * @return
     */
    private Eval singleEval(File n1, File l1, File n2, File l2, File gs) throws Exception{
        // Initialize timer
        ExecutionTimer timer = new ExecutionTimer();
        timer.startOverallTime();

        // Compute Alignment
        Result result = matchingPipeline.run(n1, l1, n2, l2, timer);

        // Stop timer
        timer.stopOverallTime();

        // Save alignment
        RdfAlignmentReader rdfParser = new RdfAlignmentReader();

        //extract net-names
        String model1 = result.getAlignment().getName().substring(0,result.getAlignment().getName().indexOf('-')-5);
        String model2 = result.getAlignment().getName().substring(result.getAlignment().getName().indexOf('-')+1,result.getAlignment().getName().length()-5);

        //Write alignment to rdf file
        File rdfFile = new File("eval-results/" + logFolder + "/"+model1 +"-"+model2+".rdf");
        rdfFile.getParentFile().mkdirs();
        rdfFile.createNewFile();
        rdfParser.writeAlignmentTo(rdfFile,result.getAlignment(), model1, model2);

        // Read Gold Standard
        if(!gs.exists()){
            throw new FileNotFoundException("Gold Standard File not found:" + gs.toString());
        }

        //fetch goldstandard matching and matcher hypothesis
        RdfAlignmentReader reader = new RdfAlignmentReader();
        Alignment goldstandard = reader.readAlignmentFrom(gs);
        Alignment matcherResult = result.getAlignment();
        matcherResult = matcherResult.filter(postprocessingThreshold);

        //Perform evaluation
        Eval eval = evaluate(new Result(result.getSimilarity(),matcherResult,result.getGAP()),goldstandard);
        eval.setBenchmark(timer);

        System.out.println(eval);
        return eval;

    }


    /**
     * Perform Batch Evaluation: On the given batchPath all combinations of process models for which
     * a goldstandard exists in goldStandardPath
     * @return list of evals
     */
    private List<Eval> batchEval(){
        // Get all pnml/epml files in the batch dir
        File[] files =  Preprocessor.listFilesOfType(batchPath.toFile(),new String[]{"pnml","epml"});

        // run evaluation for each combination
        List<Eval> evals = new ArrayList<>();
        for(File f1 : files){
            for(File f2 : files){
                if(f1 != f2) {
                    try {
                        System.gc(); // hint to garbage collect old entries which are not needed anymore
                        String model1 = f1.getName().substring(0,f1.getName().length()-5);
                        String model2 = f2.getName().substring(0,f2.getName().length()-5);

                        //get gold standard
                        File gs = new File(goldStandardPath+"/"+model1+"-"+model2+".rdf");
                        if(!gs.exists()){
                            throw new FileNotFoundException("File not found:" + gs.toString());
                        }

                        //get log files if exist
                        File log1 = null;
                        File log2 = null;
                        if(processLogPath != null) {
                            log1 = new File(processLogPath + "/" + f1.getName().replace("pnml", "xes"));
                            log2 = new File(processLogPath + "/" + f1.getName().replace("pnml", "xes"));
                            if (!log1.exists() || !log2.exists()) {
                                throw new Exception("Log file does not exist. Continue without log information: " + log1.toString() + " or "+ log2.toString());
                            }
                        }

                        //run eval and append to list of single evaluations
                        evals.add(singleEval(f1, log1, f2, log2, gs));

                    }catch(Exception e){
                        System.out.println("Evaluation of "+  f1.getName() + " to " + f2.getName() +
                                "threw an Exception: " + e.getMessage());
                    }
                }
            }
        }
        return evals;
    }


    /**
     *
     * @param resultPath Path to the computed matching hypothesis (alignment files in RDF format)
     * @param goldstandardPath Goldstandard of the dataset
     * @return list of single evaluations
     */
    private List<Eval> retrospectiveEval(Path resultPath, Path goldstandardPath){

        //fetch matching hypthesis and goldstandards
        File[] resultFiles =  Preprocessor.listFilesOfType(resultPath.toFile(),"rdf");
        File[] goldstandardFiles =  Preprocessor.listFilesOfType(goldstandardPath.toFile(),"rdf");

        // check if empty
        if(goldstandardFiles.length == 0){
            throw new Error("Gold Standard Path is empty");
        }
        if(resultFiles.length == 0){
            throw new Error("Result Path is empty");
        }

        //read config file of the matcher and set matcher parameters in the retrospective config.log file if possible
        //if not possible, then this step is skipped. The config.log file for the matcher then contains
        try{
            File config = new File(resultPath +"/config.log");
            FileReader reader = new FileReader(config);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);
            JSONObject matcher = (JSONObject) json.get("matcher");

            //map renamed ILP enum: BASIC2 => SYMMETRIC, BASIC5 => CUSTOM_IDENTIFICATION
            AbstractILP.ILP ilp;
            switch(matcher.get("ilp").toString()){
                case "BASIC2":
                    ilp  = AbstractILP.ILP.SYMMETRIC;
                    break;
                case "BASIC5":
                    ilp = AbstractILP.ILP.CUSTOM_IDENTIFICATION;
                    break;
                    default:
                        ilp = AbstractILP.ILP.valueOf(matcher.get("ilp").toString());
            }

            bpm.ippm.matcher.Pipeline.Builder builder = new bpm.ippm.matcher.Pipeline.Builder()
                    .atPostprocessThreshold(Double.valueOf(matcher.get("postprocessing-thresh").toString()))
                    .atSimilarityWeight(Double.valueOf(matcher.get("sim-weight").toString()))
                    .withWordSimilarity(Word.Similarities.valueOf(matcher.get("word-sim").toString()))
                    .withILP(ilp)
                    .withILPNodeLimit(Double.valueOf(matcher.get("ilp-node-limit").toString()))
                    .withILPTimeLimit(Double.valueOf(matcher.get("ilp-time-limit").toString()))
                    .withProfile(AbstractProfile.Profile.valueOf(matcher.get("profile").toString()));
            if(Boolean.valueOf(matcher.get("complex matches").toString())){
                builder.withComplexMatches();
            }
            if(Boolean.valueOf(matcher.get("prematch").toString())){
                builder.withPreMatching();
            }
            this.matchingPipeline = builder.Build();

        }catch(Exception e){
            System.out.println("Reading Config file was not possible: \n" + e.toString());
        }

        // find corresponding alignments and calculate evaluation
        List<Eval> evals = new ArrayList<>();
        for(File f1 : resultFiles){
            for(File f2 : goldstandardFiles){
                if(f1.getName().equals(f2.getName())) {
                    try{
                        RdfAlignmentReader reader = new RdfAlignmentReader();
                        Alignment result = reader.readAlignmentFrom(f1);
                        result = result.filter(postprocessingThreshold);
                        Alignment goldstandard = reader.readAlignmentFrom(f2);
                        evals.add(this.evaluate(new Result(0.0,result,0),goldstandard));
                    }catch(Exception e){
                        System.out.println("Evaluation of "+  f1.getName() + " to " + f2.getName() +
                                "threw an Exception: " + e.getMessage());
                    }
                }
            }
        }
        return evals;
    }


    private void goldstandardAnalysis(){
        AggregatedGoldstandardAnalysis gsAnalysis;
        try {
            gsAnalysis = new AggregatedGoldstandardAnalysis(new File("./eval-results/" + logFolder + "/goldstandard.eval"));
        }catch(Exception e){
            System.out.println("Goldstandard initialization not possible " + e.toString());
            gsAnalysis = null;
        }
        // get files
        File[] files = Preprocessor.listFilesOfType(goldStandardPath.toFile(),"rdf");


        // add to csv
        RdfAlignmentReader reader = new RdfAlignmentReader();
        for(File f : files){
            try {
                //get alignment
                Alignment a = reader.readAlignmentFrom(f);

                //extract net-names
                String model1Name = f.getName().substring(0,f.getName().indexOf('-'));
                File f1 = new File(batchPath+"/"+model1Name+".pnml");
                NetSystem net1 = parseFile(f1);
                String model2Name = f.getName().substring(f.getName().indexOf('-')+1,f.getName().length()-4);
                File f2 = new File( batchPath+"/"+model2Name+".pnml");
                NetSystem net2 = parseFile(f2);

                // add to goldstandard analysis
                gsAnalysis.addGoldstandard(a, net1, net2);
            }catch(Exception e){
                System.out.println("Analysis not possible for goldstandard " + f.getName() + e.toString());
            }
        }
        try {
            gsAnalysis.toCSV();
        }catch(Exception e){
            System.out.println("Flush not possible " + e.toString());
        }
    }

    private void netAnalysis(){
        AggregatedNetAnalysis netAnalysis;
        try {
            netAnalysis = new AggregatedNetAnalysis(new File("./eval-results/" + logFolder + "/net.eval"));
        }catch(Exception e){
            System.out.println("Net Analysis initialization not possible " + e.toString());
            netAnalysis = null;
        }
        // get files
        File[] files = Preprocessor.listFilesOfType(batchPath.toFile(), new String[]{"epml","pnml"});

        // add to csv
        for(File f : files){
            NetSystem net1 = parseFile(f);
            net1.setName(f.getName());
            try {
                netAnalysis.addNet(net1, createProfile(net1, netProfile,null)); // todo add log
            }catch(Exception e){
                System.out.println("Analysis not possible for net " + f.getName() );
                e.printStackTrace();
            }
        }
        try {
            netAnalysis.toCSV();
        }catch(Exception e){
            System.out.println("Flush not possible " + e.toString());
        }
    }


    /**
     * Call correct Evaluator for matcher and goldstandard
     * @param matcher matching hypothesis
     * @param goldstandard goldstandard
     * @return
     */
    protected Eval evaluate(Result matcher, Alignment goldstandard){
        switch(evalStrat){
            case BINARY:
                return Eval.Builder.BinaryEvaluation(matcher,goldstandard);
            case STRICT_BINARY:
                return Eval.Builder.StrictBinaryEvaluation(matcher,goldstandard);
            case PROBABILISTICALLY:
                return Eval.Builder.ProbabilisticEvaluation(matcher,goldstandard);
        }
        throw  new IllegalStateException("Evaluation Method not found");
    }

    /**
     * Create String representation of the Evaluation Pipeline
     * @return
     */
    @Override
    public String toString(){
        String res = "Batch: "+ Boolean.toString(this.batch) + "\n" +
                "Retrospective: "+ Boolean.toString(this.retrospective) + "\n" +
                "Eval Strategy:" + this.evalStrat.toString() + "\n" +
                "Postprocessing Threshold: " + this.postprocessingThreshold+ "\n";


                if(this.batch){
                    res +=  "Batch Path: " + this.batchPath.toString() + "\n" +
                            "Gold Standard Path" + this.goldStandardPath.toString() + "\n";
                    if(this.processLogPath != null) {
                       res += "Log Path: " + this.processLogPath.toString() + "\n";
                    }
                }else if(retrospective) {
                    res +=  "Gold Standard Path: " + this.goldStandardPath.toString() + "\n" +
                            "Result Path: " + this.resultPath.toString() + "\n";
                }else if(netEval) {
                    res += "Relational Profile: " + this.netProfile.toString()+ "\n";
                }else{
                    res += "Net 1: " + this.net1.toString() + "\n" +
                            "Net 2: " + this.net2.toString() + "\n" +
                            "Gold Standard:" + this.goldStandard.toString() + "\n";
                }
        return res;
    }

    /**
     * Create JSON Representation of the evaluation pipeline (used for config.log file)
     * @return
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        //Create Evaluation object
        JSONObject jsonEval = new JSONObject();
        jsonEval.put("batch",Boolean.toString(this.batch));
        jsonEval.put("retrospective", Boolean.toString(this.retrospective));
        jsonEval.put("eval-strat",this.evalStrat.toString());
        jsonEval.put("postprocessing-thresh", this.postprocessingThreshold);
        if(this.batch){
            jsonEval.put("batch-path", this.batchPath.toString());
            jsonEval.put("gs-path", this.goldStandardPath.toString());
            if(processLogPath!= null) {
                jsonEval.put("log-path", this.processLogPath.toString());
            }
        }else if(retrospective) {
            jsonEval.put("gs-path", this.goldStandardPath.toString());
            jsonEval.put("result-path", this.resultPath.toString());
        }else if(netEval) {
            jsonEval.put("relational-profile", this.resultPath.toString());
        }else{
            jsonEval.put("net-1", this.net1.toString());
            jsonEval.put("net-2", this.net2.toString());
            jsonEval.put("gs", this.goldStandard.toString());
        }

        // Merge Evaluation and Matcher object to one JSON object
        JSONObject json = new JSONObject();
        json.put("matcher", this.matchingPipeline.toJSON());
        json.put("evaluation",jsonEval);
        return json;
    }

    /**
     * Get logging and result path
     * @return Path
     */
    public Path getLogPath(){
        return Paths.get("./eval-results/" + this.logFolder);
    }


    /**
     * Builder for the evaluation pipeline
     */
    public static class Builder{
        // general options
        private MatchingPipeline matchingPipeline = new bpm.ippm.matcher.Pipeline.Builder().Build();

        // Evaluation specific options
        private boolean batch = false;
        private boolean retrospective = false;
        private boolean netEval = false;
        private AbstractProfile.Profile netProfile;
        private Path resultPath;
        private Path batchPath;
        private File net1;
        private File log1 = null;
        private File log2 = null;
        private File net2;
        private File goldStandard;
        private Path goldStandardPath;
        private Path processLogPath;
        private Eval.Strategies evalStrat = Eval.Strategies.BINARY;
        private double postprecessingThreshold = 0;
        private boolean gsEval = false;

        public Builder(){
            super();
        }

        /**
         * Set the matcher used to evaluate in on-demand matcher tests (single, or batch tests)
         * @return
         */
        public Builder withMatcher(MatchingPipeline matchingPipeline){
            this.matchingPipeline = matchingPipeline;
            return this;
        }


        /**
         * Perform a batch test on given folder
         * @return
         */
        public Builder withBatch(){
            this.batch = true;
            return this;
        }

        /**
         * Perform a  net test on given folder
         * @return
         */
        public Builder withNetEval(){
            this.netEval = true;
            return this;
        }

        /**
         * Perform a gs test on given folder
         * @return
         */
        public Builder withGSEval() {
            this.gsEval = true;
            return  this;
        }

        /**
         * Add a profile for net evaluation
         * @param profile
         * @return
         */
        public Builder withNetEvalProfile(String profile){
            AbstractProfile.Profile p = AbstractProfile.Profile.valueOf(profile);
            this.netProfile = p;
            return this;
        }

        /**
         * Perform a retrospective evaluation on given folders
         * @return
         */
        public Builder withRetrospective(){
            this.retrospective = true;
            return this;
        }

        /**
         * Batch path to be used where the nets are located
         * @param p Path
         * @return
         */
        public Builder withBatchPath(Path p){
            this.batchPath = p;
            return this;
        }

        /**
         * Result path, where the matching hypothesis rdf files are located, needed for retrospective evaluation
         * @param p path
         * @return
         */
        public Builder withResultPath(Path p){
            this.resultPath = p;
            return this;
        }

        /**
         * for single evaluation choose net1 here
         * @param net1
         * @return
         */
        public Builder onNet1(File net1){
            this.net1 = net1;
            return this;
        }

        /**
         * for single evaluation choose log1 here
         * @param log1
         * @return
         */
        public Builder onLog1(File log1){
            this.log1 = log1;
            return this;
        }

        /**
         * for normal evaluation choose net2 here
         * @param net2
         * @return
         */
        public Builder onNet2(File net2){
            this.net2 = net2;
            return this;
        }

        /**
         * for normal evaluation choose log2 here
         * @param log2
         * @return
         */
        public Builder onLog2(File log2){
            this.log2 = log2;
            return this;
        }

        /**
         * Goldstandard file for a single evaluation
         * @param goldStandard
         * @return
         */
        public Builder withGoldStandard(File goldStandard){
            this.goldStandard = goldStandard;
            return this;
        }

        /**
         * Goldstandard path for a batch evaluation
         * @param goldStandard
         * @return
         */
        public Builder withGoldStandard(Path goldStandard){
            this.goldStandardPath = goldStandard;
            return this;
        }

        /**
         * Choose the evaluation strategy
         * @param strat
         * @return
         */
        public Builder withEvalStrat(Eval.Strategies strat){
            this.evalStrat = strat;
            return this;
        }

        /**
         * set the subfolder for logging: ./eval-results/<processLogPath>
         * @param processLogPath
         * @return
         */
        public Builder withLogPath(Path processLogPath) {
            this.processLogPath = processLogPath;
            return this;
        }

        /**
         * Set post-processing threshold for the pipeline
         * After executing the matcher an additional postprocessing threshold can be applied.
         * @param p
         * @return
         */
        public Builder atThreshold(double p) {
            this.postprecessingThreshold = p;
            return this;
        }

        /**
         * Build the Pipeline
         * @return Pipeline
         */
        public Pipeline build(){
            Pipeline pip = new Pipeline();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            // standard information
            pip.matchingPipeline = this.matchingPipeline;

            //evaluation specific information
            pip.batch = this.batch;
            pip.retrospective = this.retrospective;
            pip.resultPath = this.resultPath;
            pip.batchPath = this.batchPath;
            pip.net1 = this.net1;
            pip.log1 = this.log1;
            pip.net2 = this.net2;
            pip.log2 = this.log2;
            pip.goldStandard = this.goldStandard;
            pip.goldStandardPath = this.goldStandardPath;
            pip.evalStrat = this.evalStrat;
            pip.netEval = this.netEval;
            pip.netProfile = this.netProfile;
            pip.postprocessingThreshold = this.postprecessingThreshold;
            pip.gsEval = this.gsEval;
            pip.processLogPath = this.processLogPath;

            //tests if all inputs are given, which are needed
            if(pip.batch && pip.batchPath == null){
                throw new Error("When batch mode on, then -batchPath argument needed");
            }

            if(!pip.batch && !retrospective && !netEval && !gsEval && (pip.net1 == null || pip.net2 == null)){
                throw new Error("When single evaluation, then -net1, -net2 argument needed");
            }

            if(!pip.batch && !retrospective  && !netEval && !gsEval && pip.goldStandard == null){
                throw new Error("When single evaluation, then goldstandard file -g argument needed");
            }

            if(pip.batch && pip.goldStandardPath == null){
                throw new Error("When batch mode on, then goldstandard path -gsp argument needed");
            }

            if(pip.retrospective && (pip.goldStandardPath == null || pip.resultPath == null)){
                throw new Error("When retrospective mode on, then goldstandard path and result path to rdf files are needed");
            }

            if(pip.netEval && (pip.netProfile == null || pip.batchPath == null)){
                throw new Error("When netEval mode on, then batch path and net profile must be given");
            }

            if(pip.gsEval &&  (pip.goldStandardPath == null || pip.batchPath == null)){
                throw new Error("When gsEval mode on, then goldstandardPath and batchPath must be given");
            }

            if(goldStandardPath != null && !goldStandardPath.toFile().exists()){
                throw new Error("Goldstandard Path doesn't exist.");
            }

            if(batchPath != null && !batchPath.toFile().exists()){
                throw new Error("Net Path doesn't exist.");
            }

            if(processLogPath != null && !processLogPath.toFile().exists()){
                throw new Error("Log Path doesn't exist.");
            }

            if(resultPath != null && !resultPath.toFile().exists()){
                throw new Error("Result Path doesn't exist.");
            }


            // Define log folder where all log files are generated inside ./eval-results
            if(batch) {
                pip.logFolder ="batch-"+batchPath.getFileName()+"-"+evalStrat.toString()+"-"+timestamp;
            }else if(retrospective){
                pip.logFolder ="retrospective-"+resultPath.getFileName()+"-"+evalStrat.toString()+"-"+timestamp;
            }else if(netEval) {
                pip.logFolder ="net-" +netProfile.toString()+"-"+timestamp;
            }else if(gsEval) {
                pip.logFolder ="goldstandard-" +goldStandardPath.getFileName()+"-"+timestamp;
            }else{
                pip.logFolder ="single-"+net1.getName()+"-"+net2.getName()+"-"+evalStrat.toString()+"-"+timestamp;
            }
            //replace blanks : . by - in path name
            pip.logFolder = pip.logFolder.replace(" ", "-");
            pip.logFolder = pip.logFolder.replace(":", "-");
            pip.logFolder = pip.logFolder.replace(".", "-");
            File f = new File("eval-results/"+pip.logFolder);
            f.mkdirs();
            return pip;
        }
    }
}
