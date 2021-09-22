package bpm.ippm.matcher;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.alignment.Result;
import bpm.evaluation.ExecutionTimer;
import bpm.ippm.ilp.*;
import bpm.ippm.profile.*;
import bpm.ippm.similarity.LabelSimilarity;
import bpm.ippm.similarity.Matrix;
import bpm.ippm.similarity.Word;

import gurobi.GRB;
import org.deckfour.xes.model.XLog;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import org.json.JSONException;
import org.json.simple.JSONObject;


import java.sql.Timestamp;
import java.io.File;
import java.util.Set;

import static java.lang.System.exit;


/**
 * Matching Pipeline. This is the main flow of the ILP matcher:
 * 1) Read File ( sound Petri net in PNML format)
 * 2) Read Log Files (optionally)
 * 3) Check Petri Nets (free-choice, WF-net)
 * 4) Create Profiles
 * 5) Preprocess: Delete tau transitions
 * 6) Prematch (optionally)
 * 7) Set up the ILP and optimize it
 * 8) Postprocess the matching hypothesis
 *
 * Note, use builder to construct.
 */
public class Pipeline implements MatchingPipeline{
    public static boolean PRINT_ENABLED = false;
    private boolean complexMatches;
    private boolean prematch;
    private double  similarityWeight;
    private double  postprocessThreshold;
    private AbstractProfile.Profile profile;
    private AbstractILP.ILP ilp;
    private Word.Similarities wordSimilarity;
    private double ilpTimeLimit;
    private double ilpNodeLimit;

    public static final XLog DUMMY_LOG = null;
    private LabelSimilarity.Similarities labelSimilarity;

    /**
     * Empty Constructor for the Builder
     */
    private Pipeline(){
    }


    /**
     * Run the matching pipeline for the given two petri nets.
     * @param fileNet1 petri net file path 1 in PNML format
     * @param fileLog1 log file path 1 of petri net 1 in  XES format
     * @param fileNet2 petri net file path 2 in PNML format
     * @param fileLog2 log file path 2 of petri net 2 in  XES format
     * @param timer the timer object
     */
    @Override
    public Result run(File fileNet1, File fileLog1, File fileNet2, File fileLog2, ExecutionTimer timer){
        System.out.println("########"+fileNet1.getName()+ " to " +fileNet2.getName()+"#########");

        //parse the net files
        if(PRINT_ENABLED) System.out.println("##### Start Net Parsing #####");
        NetSystem net1 = Preprocessor.parseFile(fileNet1);
        net1.setName(fileNet1.getName());
        NetSystem net2 = Preprocessor.parseFile(fileNet2);
        net2.setName(fileNet2.getName());
        if(PRINT_ENABLED) System.out.println("##### Net Parsing Complete #####");

        //parse log files
        if(PRINT_ENABLED) System.out.println("##### Start Log Parsing #####");
        XLog log1 = Preprocessor.parseLog(fileLog1);
        XLog log2 = Preprocessor.parseLog(fileLog2);
        if(PRINT_ENABLED) System.out.println("##### Log Parsing Complete #####");

        return run(net1, log1, net2, log2, timer);
    }

    /**
     * Run the matching pipeline for the given two petri nets.
     * @param fileNet1 petri net file path 1 in PNML format
     * @param fileLog1 log file path 1 of petri net 1 in  XES format
     * @param fileNet2 petri net file path 2 in PNML format
     * @param fileLog2 log file path 2 of petri net 2 in  XES format
     */
    @Override
    public Result run(File fileNet1, File fileLog1, File fileNet2, File fileLog2){
       return run(fileNet1, fileLog1, fileNet2, fileLog2, new ExecutionTimer());
    }

    /**
     * Run the matching pipeline for the given two petri nets.
     * @param fileNet1 petri net file path 1 in PNML format
     * @param fileNet2 petri net file path 2 in PNML format
     * @param timer the timer object
     * @return
     */
    @Override
    public Result run(File fileNet1, File fileNet2, ExecutionTimer timer) {
        System.out.println("########"+fileNet1.getName()+ " to " +fileNet2.getName()+"#########");

        //parse the net files
        if(PRINT_ENABLED) System.out.println("##### Start Net Parsing #####");
        NetSystem net1 = Preprocessor.parseFile(fileNet1);
        net1.setName(fileNet1.getName());
        NetSystem net2 = Preprocessor.parseFile(fileNet2);
        net2.setName(fileNet2.getName());
        if(PRINT_ENABLED) System.out.println("##### Net Parsing Complete #####");
        return run(net1, null, net2, null, timer);

    }

    /**
     * Run the matching pipeline for the given two petri nets.
     * @param fileNet1 petri net file path 1 in PNML format
     * @param fileNet2 petri net file path 2 in PNML format
    */
    @Override
    public Result run(File fileNet1, File fileNet2){
        return run(fileNet1, null, fileNet2, null, new ExecutionTimer());
    }

    /**
     * Run the timed matching pipeline for the given two petri nets.
     * @param net1 petri net file path 1 in PNML format
     * @param net2 petri net file path 2 in PNML format
     * @param timer timer object which is updated while execution (call by reference)
     */
    public Result run(NetSystem net1, XLog log1, NetSystem net2, XLog log2, ExecutionTimer timer){

        //  wf-net and free choice check
        if(PRINT_ENABLED) System.out.println("##### Start Check Up #####");
        Preprocessor.checkPetriNetProperties(net1);
        Preprocessor.checkPetriNetProperties(net2);
        if(PRINT_ENABLED) System.out.println("##### Check Up Complete #####");

        // Create Profile
        if(PRINT_ENABLED) System.out.println("##### Start Creating Profiles #####");
        timer.startBPTime();
        AbstractProfile relNet1 = AbstractProfile.createProfile(net1, this.profile, log1);
        AbstractProfile relNet2 = AbstractProfile.createProfile(net2, this.profile, log2);
        timer.stopBPTime();
        if(PRINT_ENABLED) System.out.println("##### Creating Profiles Complete #####");

        // Preprocess: ignore tau transitions
        if(PRINT_ENABLED) System.out.println("##### Start Preprocessing Tau Transitions#####");
        Set<Transition> reducedNet1 = Preprocessor.reduceTauTransitions(net1.getTransitions());
        Set<Transition> reducedNet2 = Preprocessor.reduceTauTransitions(net2.getTransitions());
        if(PRINT_ENABLED) System.out.println("##### Complete Preprocessing Tau Transitions#####");

        // Create Label Similarity Matrix
        if(PRINT_ENABLED) System.out.println("##### Start Creating Similarity Matrix #####");
        timer.startLabelSimilarityTime();
        Matrix simMatrix = new Matrix.Builder()
                .withLabelSimilarity(this.labelSimilarity)
                .withWordSimilarity(this.wordSimilarity)
                .build(reducedNet1,reducedNet2);
        timer.stopLabelSimilarityTime();
        if(PRINT_ENABLED) System.out.println("##### Creating Similarity Matrix Complete #####");

        // Preprocess: Prematch
        if(PRINT_ENABLED) System.out.println("##### Start Prematch #####");
        Alignment preAlignment;
        if(prematch){
            preAlignment = Preprocessor.prematch(reducedNet1,reducedNet2,simMatrix);
            if(PRINT_ENABLED) System.out.println("Prematched " + preAlignment.getCorrespondences().size() + " Pairs of Transitions.");
        }else{
            if(PRINT_ENABLED) System.out.println("Prematching is disabled.");
            preAlignment = new Alignment.Builder().build("empty prematch");
        }
        if(PRINT_ENABLED) System.out.println("##### Preprocessing Complete #####");

        // Run ILP
        if(PRINT_ENABLED) System.out.println("##### Start ILP #####");
        timer.startLpTime();
        AbstractILP ilpMatcher = AbstractILP.getILP(ilp);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Result res;
        try {
            ilpMatcher.init(new File("gurobi-logs/log-"+ timestamp+".log"), similarityWeight,ilpTimeLimit,ilpNodeLimit);
            res = ilpMatcher.solve(relNet1, relNet2, reducedNet1, reducedNet2, simMatrix, preAlignment, net1.getName()+"-"+net2.getName());
        } catch (Exception e) {
            if(PRINT_ENABLED) System.out.println(e.getCause()+ ": " + e.getMessage());
            exit(1);
            res = null;
        }
        timer.stopLpTime();
        if(PRINT_ENABLED) System.out.println("##### ILP Complete #####");

        //Postprocess
        if(PRINT_ENABLED) System.out.println("##### Start Postprocessing #####");
        res = relNet1.filterTemporaryTransitions(res);
        if(PRINT_ENABLED) System.out.println("##### Postprocessing Complete #####");

        if(PRINT_ENABLED) System.out.println(res.toString());

        //Return
        return res;
    }

    /**
     * Return a string representation of the pipeline configuration
     * @return String
     */
    @Override
    public String toString(){
        String res = "ILP: " + this.ilp.toString() + "\n" +
                "Profile: " + this.profile.toString() + "\n" +
                "Label Similarity: " + this.wordSimilarity.toString() +"\n" +
                "Complex Matches: " + Boolean.toString(this.complexMatches) + "\n" +
                "Prematch: "+ Boolean.toString(this.prematch) + "\n" +
                "Similarity Weight: " + this.similarityWeight + "\n" +
                "Postprocessing Thresh: " +  this.postprocessThreshold + "\n" +
                "ILP node limit: " + this.ilpNodeLimit + "\n" +
                "ILP time limit: " + this.ilpTimeLimit + "\n";
        return res;
    }

    /**
     * Return JSON Object of the Pipeline
     * @return JSON Object
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("ilp", this.ilp.toString());
        json.put("profile", this.profile.toString());
        json.put("word-sim", this.wordSimilarity.toString());
        json.put("complex matches", Boolean.toString(this.complexMatches));
        json.put("prematch", Boolean.toString(this.prematch));
        json.put("sim-weight", this.similarityWeight);
        json.put("postprocessing-thresh", this.postprocessThreshold);
        json.put("ilp-time-limit", this.ilpTimeLimit);
        json.put("ilp-node-limit", this.ilpNodeLimit);
        json.put("label-sim", this.labelSimilarity.toString());
        return json;
    }

    /**
     * Builder for class Pipeline
     */
    public static class Builder{
        private boolean complexMatches = false;
        private double  similarityWeight = 0.3;
        private double  postprocessThreshold = 0.0;
        private AbstractILP.ILP ilp = AbstractILP.ILP.SYMMETRIC;
        protected AbstractProfile.Profile profile = AbstractProfile.Profile.BP;
        private Word.Similarities wordSimilarity = Word.Similarities.LEVENSHTEIN_LIN_MAX;
        private LabelSimilarity.Similarities labelSimilarity = LabelSimilarity.Similarities.BOW;
        private boolean prematch = false;
        private double ilpTimeLimit = GRB.INFINITY;
        private double ilpNodeLimit = GRB.INFINITY;

            /**
         * Create a Builder to define a Pipline Object.
         */
        public Builder(){
        }

        /**
         * Enable complex matches.
         * @return Builder
         */
        public Pipeline.Builder withComplexMatches(){
            this.complexMatches = true;
            return this;
        }

        /**
         * Set the weight between profile conformance influence and label similarity influence.
         * 0 means label only, 1 means profile only.
         * @param s between 0 and 1
         * @return Builder
         */
        public Pipeline.Builder atSimilarityWeight(double s){
            if( s < 0 || s > 1){
                throw new NumberFormatException("Value SimilarityWeight is out of range 0 to 1");
            }
            this.similarityWeight = s;
            return this;
        }

        /**
         * Set the post processing min label threshold.
         * Every match which has lover label similarity than the threshold p is post-pruned.
         * @param p value between 0 and 1
         * @return Builder
         */
        public Pipeline.Builder atPostprocessThreshold(double p){
            if( p < 0 || p > 1){
                throw new NumberFormatException("Value PostprocessThreshold is out of range 0 to 1");
            }
            this.postprocessThreshold = p;
            return this;
        }

        /**
         * Set the used label similarity (standard is BOW)
         * @param labelSimilarity
         * @return Builder
         */
        public Pipeline.Builder withLabelSimilarity(LabelSimilarity.Similarities labelSimilarity){
            this.labelSimilarity = labelSimilarity;
            return this;
        }

        /**
         * Set the Word Similarity Function
         * @return Builder
         */
        public Pipeline.Builder withWordSimilarity(Word.Similarities wordSim){
            this.wordSimilarity = wordSim;
            return this;
        }

        /**
         * Sets the ILP Matcher to Basic
         * @return Builder
         */
        public Pipeline.Builder withILP(AbstractILP.ILP ilp){
            this.ilp = ilp;
            return this;
        }

        /**
         * Set the profile type used to represent the behavior
         * @param p a profile of type Match.Profile
         * @return Builder
         */
        public Pipeline.Builder withProfile(AbstractProfile.Profile p){
            this.profile = p;
            return this;
        }

        public Pipeline.Builder withPreMatching() {
            this.prematch = true;
            return this;
        }

        /**
         * set the ILP time limit in seconds
         * @param limit
         * @return Builder
         */
        public Pipeline.Builder withILPTimeLimit(double limit) {
            this.ilpTimeLimit = limit;
            return this;
        }

            /**
             * set the ILP node limit in seconds
             * @param limit
             * @return Builder
             */
            public Pipeline.Builder withILPNodeLimit(double limit) {
                this.ilpNodeLimit = limit;
                return this;
            }

        /**
         *Build a Pipeline with the previously assigned arguments.
         * @return Executable Pipeline
         */
        public Pipeline Build(){
            Pipeline pip = new Pipeline();
            pip.similarityWeight = this.similarityWeight;
            pip.complexMatches = this.complexMatches;
            pip.postprocessThreshold = this.postprocessThreshold;
            pip.profile = this.profile;
            pip.ilp = this.ilp;
            pip.wordSimilarity = this.wordSimilarity;
            pip.prematch = this.prematch;
            pip.ilpNodeLimit = this.ilpNodeLimit;
            pip.labelSimilarity = this.labelSimilarity;
            pip.ilpTimeLimit = this.ilpTimeLimit;
            return pip;
        }
    }
}
