package bpm.matcher;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import bpm.evaluation.ExecutionTimer;
import bpm.ilp.*;
import bpm.profile.AbstractProfile;
import bpm.profile.AlphaRelations;
import bpm.profile.BP;
import bpm.profile.BPPlus;
import bpm.similarity.Matrix;
import bpm.similarity.Word;

import gurobi.GRB;
import org.jbpt.bp.RelSet;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.Transition;
import org.jbpt.petri.io.PNMLSerializer;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONException;
import org.json.simple.JSONObject;


import java.sql.Timestamp;
import java.io.File;
import java.util.Set;

import static java.lang.System.exit;


/**
 * Matching Pipeline. Note, use builder to construct.
 */
public class Pipeline {
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

    /**
     * Empty Constructor for the Builder
     */
    private Pipeline(){
    }

    /**
     * Run the matching pipeline for the given two petri nets.
     * @param fileNet1 petri net file path 1 in PNML format
     * @param fileNet2 petri net file path 2 in PNML format
     */
    public Result run(File fileNet1, File fileNet2){
       return run(fileNet1,fileNet2,new ExecutionTimer());
    }

    /**
     * Run the timed matching pipeline for the given two petri nets.
     * @param fileNet1 petri net file path 1 in PNML format
     * @param fileNet2 petri net file path 2 in PNML format
     * @param timer timer object which is updated while execution (call by reference)
     */
    public Result run(File fileNet1, File fileNet2, ExecutionTimer timer){
        //parse the two petri nets
        System.out.println("########"+fileNet1.getName()+ " to " +fileNet2.getName()+"#########");
        if(PRINT_ENABLED) System.out.println("##### Start Parsing #####");
        NetSystem net1 = parseFile(fileNet1);
        net1.setName(fileNet1.getName());
        NetSystem net2 = parseFile(fileNet2);
        net2.setName(fileNet2.getName());
        if(PRINT_ENABLED) System.out.println("##### Parsing Complete #####");

        //  wf-net and free choice check
        if(PRINT_ENABLED) System.out.println("##### Start Check Up #####");
        checkPetriNetProperties(net1);
        checkPetriNetProperties(net2);
        if(PRINT_ENABLED) System.out.println("##### Check Up Complete #####");
        // Create Profile
        if(PRINT_ENABLED) System.out.println("##### Start Creating Profiles #####");
        timer.startBPTime();
        AbstractProfile relNet1 = createProfile(net1, this.profile);
        //System.out.print("Net 1" +relNet1.toString());
        AbstractProfile relNet2 = createProfile(net2, this.profile);
        //System.out.print("Net 2" +relNet1.toString());
        timer.stopBPTime();
        if(PRINT_ENABLED) System.out.println("##### Creating Profiles Complete #####");

        // Preprocess ignore taus
        if(PRINT_ENABLED) System.out.println("##### Start Preprocessing Tau Transitions#####");
        Set<Transition> reducedNet1 = Preprocessor.reduceTauTransitions(net1.getTransitions());
        Set<Transition> reducedNet2 = Preprocessor.reduceTauTransitions(net2.getTransitions());
        if(PRINT_ENABLED) System.out.println("##### Complete Preprocessing Tau Transitions#####");

        // Create Label Similarity Matrix
        if(PRINT_ENABLED) System.out.println("##### Start Creating Similarity Matrix #####");
        timer.startLabelSimilarityTime();
        Matrix simMatrix = new Matrix.Builder()
                .withWordSimilarity(this.wordSimilarity)
                .build(reducedNet1,reducedNet2);
        timer.stopLabelSimilarityTime();
        if(PRINT_ENABLED) System.out.println("##### Creating Similarity Matrix Complete #####");


        // Preprocess ignore taus and prematch
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
        AbstractILP ilp = getILP();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Result res;
        try {
            ilp.init(new File("gurobi-logs/log-"+ timestamp+".log"), similarityWeight,ilpTimeLimit,ilpNodeLimit);
            res = ilp.solve(relNet1, relNet2, reducedNet1, reducedNet2, simMatrix, preAlignment, net1.getName()+"-"+net2.getName());
        } catch (Exception e) {
            if(PRINT_ENABLED) System.out.println(e.getCause()+ ": " + e.getMessage());
            exit(1);
            res = null;
        }
        timer.stopLpTime();
        if(PRINT_ENABLED) System.out.println("##### ILP Complete #####");

        //Postprocess

        if(PRINT_ENABLED) System.out.println(res.toString());

        //Return
        return res;
    }

    /**
     * Parses a PNML file to a NetSystem
     * @param f file path of the petri net in PNML format
     * @return NetSystem
     */
    public static NetSystem parseFile(File f){
        PNMLSerializer serializer = new PNMLSerializer();
        return serializer.parse(f.getAbsolutePath());
    }

    public static AbstractProfile createProfile(NetSystem net, AbstractProfile.Profile profile){
        AbstractProfile r;
        switch(profile){
            case BP:
                r = new BP(net);
                break;
            case BPP:
                r = new BPPlus(net);
                break;
            case ARP:
                r = new AlphaRelations(net);
                break;
            default:
                throw new UnsupportedOperationException("Operator not yet implemented: " + profile.toString());
        }
        return r;
    }

    private AbstractILP getILP() throws NotImplementedException {
        switch(ilp) {
            case BASIC:
                return new BasicILP();
            case BASIC2:
                return new BasicILP2();
            case BASIC3:
                return new BasicILP3();
            case RELAXED:
                return  new RelaxedILP();
            case RELAXED2:
                return new RelaxedILP2();
            case RELAXED3:
                return new RelaxedILP3();
            case RELAXED4:
                return new RelaxedILP4();
            case QUADRATIC:
                return new QuadraticILP();
            default:
                throw new NotImplementedException("ILP you searched for is not in switch");
        }

    }

    /**
     * checks if petri net is WF-net, sound and free choice
     * throws illegal argument exception if not
     * @param net
     */
    private void checkPetriNetProperties(NetSystem net){
        // TODO soundness
        if (!PetriNet.STRUCTURAL_CHECKS.isWorkflowNet(net)){
            throw new IllegalArgumentException("net is not WF-net:" + net.toString());
        }
        if(!PetriNet.STRUCTURAL_CHECKS.isExtendedFreeChoice(net)){
            throw new IllegalArgumentException("net is not free choice:" + net.toString());
        }

        //TODO soundness incl 1 bounded.

    }

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
     * @return
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
        return json;
    }

        /**
         * Builder for class Pipeline
         */
    public static class Builder{
        private boolean complexMatches = false;
        private double  similarityWeight = 0.3;
        private double  postprocessThreshold = 0.0;
        private AbstractILP.ILP ilp = AbstractILP.ILP.BASIC;
        protected AbstractProfile.Profile profile = AbstractProfile.Profile.BP;
        private Word.Similarities wordSimilarity = Word.Similarities.LEVENSHTEIN_LIN_MAX;
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
         * Set the Word Similarity Function
         * @return Builder
         */
        public Pipeline.Builder withWordSimilarity(String wordSim){
            this.wordSimilarity = Word.Similarities.valueOf(wordSim);
            return this;
        }

        /**
         * Sets the ILP Matcher to Basic
         * @return
         */
        public Pipeline.Builder withILP(String sIlp){
            this.ilp = AbstractILP.ILP.valueOf(sIlp);
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
         * @return
         */
        public Pipeline.Builder withILPTimeLimit(double limit) {
            this.ilpTimeLimit = limit;
            return this;
        }

            /**
             * set the ILP node limit in seconds
             * @param limit
             * @return
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
            pip.ilpTimeLimit = this.ilpTimeLimit;
            return pip;
        }


    }

}
