package bpm.matcher;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import bpm.ilp.AbstractILP;
import bpm.ilp.BasicILP;
import bpm.ilp.RelaxedILP;
import bpm.ilp.RelaxedILP2;
import bpm.similarity.Matrix;
import bpm.similarity.Word;

import gurobi.GRBException;

import org.jbpt.bp.RelSet;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.Transition;
import org.jbpt.petri.io.PNMLSerializer;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.sql.Timestamp;
import java.io.File;
import java.util.Set;

import static java.lang.System.exit;


/**
 * Matching Pipeline. Note, use builder to construct.
 */
public class Pipeline {
    private boolean complexMatches;
    private boolean prematch;
    private double  similarityWeight;
    private double  postprocessThreshold;
    private Matcher.Profile profile;
    private AbstractILP.ILP ilp;
    private Word.Similarities wordSimilarity;

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
        //parse the two petri nets
        System.out.println("##### Start Parsing #####");
        NetSystem net1 = parseFile(fileNet1);
        net1.setName(fileNet1.getName());
        NetSystem net2 = parseFile(fileNet2);
        net2.setName(fileNet2.getName());
        System.out.println("##### Parsing Complete #####");

        //  wf-net and free choice check
        System.out.println("##### Start Check Up #####");
        checkPetriNetProperties(net1);
        checkPetriNetProperties(net2);
        System.out.println("##### Check Up Complete #####");

        // Create Profile
        System.out.println("##### Start Creating Profiles #####");
        RelSet relNet1 = createProfile(net1);
        //System.out.print("Net 1" +relNet1.toString());
        RelSet relNet2 = createProfile(net2);
        //System.out.print("Net 2" +relNet1.toString());
        System.out.println("##### Creating Profiles Complete #####");

        // Preprocess ignore taus
        System.out.println("##### Start Preprocessing Tau Transitions#####");
        Set<Transition> reducedNet1 = Preprocessor.reduceTauTransitions(net1.getTransitions());
        Set<Transition> reducedNet2 = Preprocessor.reduceTauTransitions(net2.getTransitions());
        System.out.println("##### Complete Preprocessing Tau Transitions#####");

        // Create Label Similarity Matrix
        System.out.println("##### Start Creating Similarity Matrix #####");
        Matrix simMatrix = new Matrix.Builder()
                .withWordSimilarity(this.wordSimilarity)
                .build(reducedNet1,reducedNet2);
        System.out.println("##### Creating Similarity Matrix Complete #####");

        // Preprocess ignore taus and prematch
        System.out.println("##### Start Prematch #####");
        Alignment preAlignment;
        if(prematch){
            preAlignment = Preprocessor.prematch(reducedNet1,reducedNet2,simMatrix);
            System.out.println("Prematched " + preAlignment.getCorrespondences().size() + " Pairs of Transitions.");
        }else{
            System.out.println("Prematching is disabled.");
            preAlignment = new Alignment.Builder().build("empty prematch");
        }
        System.out.println("##### Preprocessing Complete #####");

        // Run ILP
        System.out.println("##### Start ILP #####");
        AbstractILP ilp = getILP();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Result res;
        try {
            ilp.init(new File("gurobi-logs/log-"+ timestamp+".log"), similarityWeight);
            res = ilp.solve(relNet1, relNet2, reducedNet1, reducedNet2, simMatrix, preAlignment, net1.getName()+"-"+net2.getName());
        } catch (Exception e) {
            System.out.println(e.getCause()+ ": " + e.getMessage());
            exit(1);
            res = null;
        }
        System.out.println("##### ILP Complete #####");


        //Postprocess

        System.out.println(res.toString());

        //Return
        return res;
    }

    private PNMLSerializer serializer = null;
    /**
     * Parses a PNML file to a NetSystem
     * @param f file path of the petri net in PNML format
     * @return NetSystem
     */
    private NetSystem parseFile(File f){
        if (serializer == null){
            serializer = new PNMLSerializer();
        }else{
            serializer.clear();
        }
        return serializer.parse(f.getAbsolutePath());
    }

    private RelSet createProfile(NetSystem net){
        RelSet r;
        switch(profile){
            case BP:
                BPCreatorNet creator = BPCreatorNet.getInstance();
                r = creator.deriveRelationSet(net);
                break;
            case CBP:
                throw new UnsupportedOperationException("Causal BP not yet implemented");
            default:
                throw new UnsupportedOperationException("Operator not yet implemented: " + profile.toString());
        }
        return r;
    }

    private AbstractILP getILP() throws NotImplementedException {
        switch(ilp) {
            case BASIC:
                return new BasicILP();
            case RELAXED:
                return  new RelaxedILP();
            case RELAXED2:
                return new RelaxedILP2();
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



    /**
     * Builder for class Pipeline
     */
    public static class Builder{
        protected boolean complexMatches = false;
        protected double  similarityWeight = 0.3;
        protected double  postprocessThreshold = 0.0;
        protected AbstractILP.ILP ilp = AbstractILP.ILP.BASIC;
        protected Matcher.Profile profile = Matcher.Profile.BP;
        protected Word.Similarities wordSimilarity = Word.Similarities.LEVENSHTEIN_LIN_MAX;
        protected boolean prematch = false;

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
            switch (wordSim){
                case "Lin":
                    this.wordSimilarity = Word.Similarities.LIN;
                    break;
                case "Levenshtein":
                    this.wordSimilarity = Word.Similarities.LEVENSHTEIN;
                    break;
                case "Jiang":
                    this.wordSimilarity = Word.Similarities.JIANG;
                case "Levenshtein-Lin-Max":
                    this.wordSimilarity = Word.Similarities.LEVENSHTEIN_LIN_MAX;
                    break;
                case "Levenshtein-Jiang-Max":
                    this.wordSimilarity = Word.Similarities.LEVENSHTEIN_JIANG_MAX;
                    default:
                        throw new IllegalArgumentException("Word Similarity Parameter not supported: " + wordSim);
            }
            return this;
        }

        /**
         * Sets the ILP Matcher to Basic
         * @return
         */
        public Pipeline.Builder withILP(String sIlp){
            switch(sIlp) {
                case "Basic":
                    this.ilp = AbstractILP.ILP.BASIC;
                    break;
                case "Relaxed":
                    this.ilp = AbstractILP.ILP.RELAXED;
                    break;
                case "Relaxed2":
                    this.ilp = AbstractILP.ILP.RELAXED2;
                    break;

                    default:
                        throw new IllegalArgumentException("ilp argument is not valid: " +sIlp);
            }
            return this;
        }

        /**
         * Set the profile type used to represent the behavior
         * @param p a profile of type Match.Profile
         * @return Builder
         */
        public Pipeline.Builder withProfile(Matcher.Profile p){
            this.profile = p;
            return this;
        }

        public Pipeline.Builder withPreMatching() {
            this.prematch = true;
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
            return pip;
        }


    }

}
