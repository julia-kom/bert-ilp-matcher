package bpm.matcher;

import org.jbpt.bp.RelSet;
import org.jbpt.bp.construct.BPCreator;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.io.PNMLSerializer;

import java.io.File;

import static java.lang.System.exit;

/**
 * Matching Pipeline. Note, use builder to construct.
 */
public class Pipeline {
    private boolean complexMatches;
    private double  similarityWeight;
    private double  postprocessThreshold;
    private Matcher.Profile profile;

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
    public void run(File fileNet1, File fileNet2){
        //parse the two petri nets
        NetSystem net1 = parseFile(fileNet1);
        NetSystem net2 = parseFile(fileNet2);

        //  wf-net and free choice check
        checkPetriNetProperties(net1);
        checkPetriNetProperties(net2);

        // Create Profile
        RelSet relNet1 = createProfile(net1);
        RelSet relNet2 = createProfile(net2);

        // Run ILP
        // TODO Implement

        //Postprocess if needed


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
        return serializer.parse(f.toString());
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
                exit(1);
            default:
                throw new UnsupportedOperationException("Operator not yet implemented: " profile.toString());
                exit(1);
        }
        return r;
    }

    /**
     * checks if petri net is WF-net, sound and free choice
     * throws illegal argument exception if not
     * @param net
     */
    private void checkPetriNetProperties(NetSystem net){
        // TODO soundness
        if (!PetriNet.STRUCTURAL_CHECKS.isWorkflowNet(net)){
            throw new IllegalArgumentException("net is not WF-net:" net.toString());
        }
        if(!PetriNet.STRUCTURAL_CHECKS.isFreeChoice(net)){
            throw new IllegalArgumentException("net is not free choice:" net.toString());
        }

        //TODO soundness incl 1 bounded.

    }



    /**
     * Builder for class Pipeline
     */
    public static class Builder{
        boolean complexMatches = false;
        double  similarityWeight = 0.3;
        double  postprocessThreshold = 0.0;
        Matcher.Profile profile = Matcher.Profile.BP;

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
         * Set the profile type used to represent the behavior
         * @param p a profile of type Match.Profile
         * @return Builder
         */
        public Pipeline.Builder withProfile(Matcher.Profile p){
            this.profile = p;
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
            return pip;
        }
    }

}
