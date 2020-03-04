package bpm.evaluation;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import bpm.matcher.MatchingPipeline;
import bpm.profile.AbstractProfile;
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

import static bpm.matcher.Pipeline.createProfile;
import static bpm.matcher.Pipeline.parseFile;
import static java.lang.System.exit;

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
    private File net2;
    private File goldStandard;
    private Path goldStandardPath;
    private Eval.Strategies evalStrat;
    private String logFolder = "";
    private double postprocessingThreshold;
    private boolean gsEval;

    /**
     * Returns those files in path which are of one of the types defined in the second parameter.
     * @param path
     * @param types
     * @return
     */
    // TODO move this somewhere better. Doesnt really suit here
    public static File[] listFilesOfType(File path, final String[] types){
        File[] files =  path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                for(String t : types) {
                    if(name.toLowerCase().endsWith("."+t)){
                        return true;
                    }
                }
                return false;
            }
        });
        return files;
    }

    /**
     *  Returns those files in path which is of the type defined in the second parameter.
     * @param path
     * @param type
     * @return
     */
    public static File[] listFilesOfType(File path, String type){
        String[] types = new String[]{type};
        return listFilesOfType(path,types);
    }


    public void run(){

        // Run a net evaluation test
        if(netEval){
            AggregatedNetAnalysis netAnalysis;
            try {
                netAnalysis = new AggregatedNetAnalysis(new File("./eval-results/" + logFolder + "/net.eval"));
            }catch(Exception e){
                System.out.println("Net Analysis initialization not possible " + e.toString());
                netAnalysis = null;
            }
            // get files
            File[] files = listFilesOfType(batchPath.toFile(), new String[]{"epml","pnml"});


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

        // Run a gold standard evaluation
        }else if(gsEval){
            AggregatedGoldstandardAnalysis gsAnalysis;
            try {
                gsAnalysis = new AggregatedGoldstandardAnalysis(new File("./eval-results/" + logFolder + "/goldstandard.eval"));
            }catch(Exception e){
                System.out.println("Goldstandard initialization not possible " + e.toString());
                gsAnalysis = null;
            }
            // get files
            File[] files = listFilesOfType(goldStandardPath.toFile(),"rdf");


            // add to csv
            RdfAlignmentReader reader = new RdfAlignmentReader();
            for(File f : files){
                try {
                    //get alignment
                    Alignment a = reader.readAlignmentFrom(f);

                    //extract net-names
                    String model1Name = f.getName().substring(0,f.getName().indexOf('-'));
                    File f1 = new File("."+batchPath+"/"+model1Name+".pnml");
                    NetSystem net1 = parseFile(f1);
                    String model2Name = f.getName().substring(f.getName().indexOf('-')+1,f.getName().length()-4);
                    File f2 = new File("."+ batchPath+"/"+model2Name+".pnml");
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

        //run a batch evaluation
        } else if(batch) {
            List<Eval> evals = batchEval();
            AggregatedEval aggregatedEval = new AggregatedEval(evals);
            try {
                aggregatedEval.toCSV(new File("./eval-results/" + logFolder + "/aggResults.eval"));
            }catch(IOException e){
                System.out.println("It was not possible to write the aggregated result CSV!");
            }

        //Run a single evaluation test
        } else if(!retrospective){
            try {
                Eval eval = singleEval(net1, null, net2, null, goldStandard); // todo add log. But actually single eval is never done.
                try {
                    eval.toCSV(new File("eval-results/" + logFolder + "/"+eval.getName()+".eval"));
                }catch(IOException e){
                    System.out.println("It was not possible to write the single result CSV: " + eval.getName());
                }
            }catch(Exception e){
                System.out.println("Evaluation of "+  net1.getName() + " to " + net2.getName() +
                        "threw and Exception: " + e.getMessage());
                exit(1);
            }
        // Run a retrospective test
        }else{
            List<Eval> evals = retrospectiveEval(resultPath,goldStandardPath);
            AggregatedEval aggregatedEval = new AggregatedEval(evals);
            try {
                aggregatedEval.toCSV(new File("./eval-results/" + logFolder + "/aggRetrospectiveResults.eval"));
            }catch(IOException e){
                System.out.println("It was not possible to write the aggregated result CSV!");
            }
        }
    }

    /**
     * Perform single evaluation
     * @param n1 net 1
     * @param l1 log 1
     * @param n2 net 2
     * @param l2 log 2
     * @param gs gold standard
     * @return
     */
    public Eval singleEval(File n1, File l1, File n2, File l2, File gs) throws Exception{
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

        //Write to file
        File rdfFile = new File("eval-results/" + logFolder + "/"+model1 +"-"+model2+".rdf");
        rdfFile.getParentFile().mkdirs();
        rdfFile.createNewFile();
        rdfParser.writeAlignmentTo(rdfFile,result.getAlignment(), model1, model2);


        // Read Gold Standard
        if(!gs.exists()){
            throw new FileNotFoundException("Gold Standard File not found:" + gs.toString());
        }
        RdfAlignmentReader reader = new RdfAlignmentReader();
        Alignment goldstandard = reader.readAlignmentFrom(gs);
        Alignment matcherResult = result.getAlignment();
        matcherResult = matcherResult.filter(postprocessingThreshold);
        Eval eval = evaluate(new Result(result.getSimilarity(),matcherResult,result.getGAP()),goldstandard);
        eval.setBenchmark(timer);
        System.out.println(eval);

        //Evaluate
        return eval;

    }


    /**
     * Perform Batch Evaluation
     * @return
     */
    public List<Eval> batchEval(){
        // Get all pnml files in the batch dir
        File[] files =  listFilesOfType(batchPath.toFile(),new String[]{"pnml","epml"});

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
                        //run eval and append
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


    public List<Eval> retrospectiveEval(Path resultPath, Path goldstandardPath){
        File[] resultFiles =  listFilesOfType(resultPath.toFile(),"rdf");

        File[] goldstandardFiles =  listFilesOfType(goldstandardPath.toFile(),"rdf");

        if(goldstandardFiles.length == 0){
            throw new Error("Gold Standard Path is empty");
        }

        if(resultFiles.length == 0){
            throw new Error("Result Path is empty");
        }

        //read config file and set matcher parameters if possible
        try{
            File config = new File(resultPath +"/config.log");
            FileReader reader = new FileReader(config);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(reader);
            JSONObject matcher = (JSONObject) json.get("matcher");
            bpm.matcher.Pipeline.Builder builder = new bpm.matcher.Pipeline.Builder()
                    .atPostprocessThreshold(Double.valueOf(matcher.get("postprocessing-thresh").toString()))
                    .atSimilarityWeight(Double.valueOf(matcher.get("sim-weight").toString()))
                    .withWordSimilarity(matcher.get("word-sim").toString())
                    .withILP(matcher.get("ilp").toString())
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





    /**
     * Call correct Evaluator for matcher and goldstandard
     * @param matcher
     * @param goldstandard
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

    public Path getLogPath(){
        return Paths.get("./eval-results/" + this.logFolder);
    }


    /**
     * Builder for the evaluation pipeline
     */
    public static class Builder{
        // general options
        private bpm.matcher.MatchingPipeline matchingPipeline = new bpm.matcher.Pipeline.Builder().Build();

        // Evaluation specific options
        private boolean batch = false;
        private boolean retrospective = false;
        private boolean netEval = false;
        private AbstractProfile.Profile netProfile;
        private Path resultPath;
        private Path batchPath;
        private File net1;
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
         * Perform a batch test on given folder
         * @return
         */
        public Builder withMatcher(bpm.matcher.MatchingPipeline matchingPipeline){
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
         * Batch path to be used
         * @param p
         * @return
         */
        public Builder withBatchPath(Path p){
            this.batchPath = p;
            return this;
        }

        /**
         * Result path for retrospective evalaution
         * @param p
         * @return
         */
        public Builder withResultPath(Path p){
            this.resultPath = p;
            return this;
        }

        /**
         * for normal evaluation choose net1 one here
         * @param net1
         * @return
         */
        public Builder onNet1(File net1){
            this.net1 = net1;
            return this;
        }

        /**
         * for normal evaluation choose net2 one here
         * @param net2
         * @return
         */
        public Builder onNet2(File net2){
            this.net2 = net2;
            return this;
        }

        public Builder withGoldStandard(File goldStandard){
            this.goldStandard = goldStandard;
            return this;
        }

        public Builder withGoldStandard(Path goldStandard){
            this.goldStandardPath = goldStandard;
            return this;
        }

        public Builder withEvalStrat(Eval.Strategies strat){
            this.evalStrat = strat;
            return this;
        }

        public Builder withLogPath(Path processLogPath) {
            this.processLogPath = processLogPath;
            return this;
        }

        /**
         * Set post processing threshold for the pipeline
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
            pip.net2 = this.net2;
            pip.goldStandard = this.goldStandard;
            pip.goldStandardPath = this.goldStandardPath;
            pip.evalStrat = this.evalStrat;
            pip.netEval = this.netEval;
            pip.netProfile = this.netProfile;
            pip.postprocessingThreshold = this.postprecessingThreshold;
            pip.gsEval = this.gsEval;
            pip.processLogPath = this.processLogPath;

            //tests
            if(pip.batch && pip.batchPath == null){
                throw new IllegalArgumentException("When batch mode on, then -batchPath argument needed");
            }

            if(!pip.batch && !retrospective && !netEval && !gsEval && (pip.net1 == null || pip.net2 == null)){
                throw new IllegalArgumentException("When single evaluation, then -net1, -net2 argument needed");
            }

            if(!pip.batch && !retrospective  && !netEval && !gsEval && pip.goldStandard == null){
                throw new IllegalArgumentException("When single evaluation, then goldstandard file -g argument needed");
            }

            if(pip.batch && pip.goldStandardPath == null){
                throw new IllegalArgumentException("When batch mode on, then goldstandard path -gsp argument needed");
            }

            if(pip.retrospective && (pip.goldStandardPath == null || pip.resultPath == null)){
                throw new IllegalArgumentException("When retrospective mode on, then goldstandard path and result path to rdf files are needed");
            }

            if(pip.netEval && (pip.netProfile == null || pip.batchPath == null)){
                throw new IllegalArgumentException("When netEval mode on, then batch path and net profile must be given");
            }

            if(pip.gsEval &&  (pip.goldStandardPath == null || pip.batchPath == null)){
                throw new IllegalArgumentException("When gsEval mode on, then goldstandardPath and batchPath must be given");
            }

            // Define log folder where all log files are generated
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
            //replace blanks : . in path name
            pip.logFolder = pip.logFolder.replace(" ", "-");
            pip.logFolder = pip.logFolder.replace(":", "-");
            pip.logFolder = pip.logFolder.replace(".", "-");
            File f = new File("eval-results/"+pip.logFolder);
            f.mkdirs();


            return pip;
        }
    }
}
