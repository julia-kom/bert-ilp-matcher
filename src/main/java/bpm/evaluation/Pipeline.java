package bpm.evaluation;

import bpm.ilp.AbstractILP;
import bpm.matcher.Matcher;
import bpm.similarity.Word;
import java.io.File;
import java.nio.file.Path;

public class Pipeline{
    //Standard matcher options
    private boolean complexMatches;
    private double  similarityWeight;
    private double  postprocessThreshold;
    private Matcher.Profile profile;
    private AbstractILP.ILP ilp;
    private Word.Similarities wordSimilarity;

    //Evaluation specific options
    private boolean batch;
    private Path batchPath;
    private File net1;
    private File net2;
    private File goldStandard;
    private Path goldStandardPath;

    public void run(){

    }


    /**
     * Builder for the evaluation pipeline
     */
    public static class Builder{
        // general options
        protected boolean complexMatches = false;
        protected double  similarityWeight = 0.3;
        protected double  postprocessThreshold = 0.0;
        protected AbstractILP.ILP ilp = AbstractILP.ILP.BASIC;
        protected Matcher.Profile profile = Matcher.Profile.BP;
        protected Word.Similarities wordSimilarity = Word.Similarities.LEVENSHTEIN_LIN_MAX;

        // Evaluation specific options
        private boolean batch = false;
        private Path batchPath;
        private File net1;
        private File net2;
        private File goldStandard;
        private Path goldStandardPath;

        public Builder(){
            super();
        }

        /**
         * Enable complex matches.
         * @return Builder
         */
        public Builder withComplexMatches(){
            this.complexMatches = true;
            return this;
        }

        /**
         * Set the weight between profile conformance influence and label similarity influence.
         * 0 means label only, 1 means profile only.
         * @param s between 0 and 1
         * @return Builder
         */
        public Builder atSimilarityWeight(double s){
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
        public Builder atPostprocessThreshold(double p){
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
        public Builder withWordSimilarity(String wordSim){
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
        public Builder withILP(String sIlp){
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
        public Builder withProfile(Matcher.Profile p){
            this.profile = p;
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
         * Batch path to be used
         * @param p
         * @return
         */
        public Builder withPath(Path p){
            this.batchPath = p;
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
            this.goldStandardPath = goldStandardPath;
            return this;
        }



        /**
         * Build the Pipeline
         * @return Pipeline
         */
        public Pipeline build(){
            Pipeline pip = new Pipeline();
            // standard information
            pip.complexMatches = this.complexMatches;
            pip.ilp = this.ilp;
            pip.postprocessThreshold = this.postprocessThreshold;
            pip.profile = this.profile;
            pip.similarityWeight = this.similarityWeight;
            pip.wordSimilarity = this.wordSimilarity;


            //evaluation specific information
            pip.batch = this.batch;
            pip.batchPath = this.batchPath;
            pip.net1 = this.net1;
            pip.net2 = this.net2;
            pip.goldStandard = this.goldStandard;

            //tests
            if(pip.batch && pip.batchPath == null){
                throw new IllegalArgumentException("When batch mode on, then -batchPath argument needed");
            }

            if(!pip.batch && (pip.net1 == null || pip.net2 == null)){
                throw new IllegalArgumentException("When batch mode off, then -net1, -net2 argument needed");
            }

            if(!pip.batch && pip.goldStandard == null){
                throw new IllegalArgumentException("When batch mode off, then goldstandard file -g argument needed");
            }

            if(pip.batch && pip.goldStandardPath == null){
                throw new IllegalArgumentException("When batch mode on, then goldstandard path -gp argument needed");
            }

            return pip;
        }
    }
}
