package bpm.matcher;

import java.io.File;

public class Pipeline {
    private boolean complexMatches;
    private double  similarityWeight;
    private double  postprocessThreshold;

    private Pipeline(){
    }

    public void run(File net1, File net2){

    }


    /**
     * Builder for class Pipeline
     */
    public static class Builder{
        boolean complexMatches = false;
        double  similarityWeight = 0.3;
        double  postprocessThreshold = 0.0;

        public Builder(){
        }

        public Pipeline.Builder withComplexMatches(){
            this.complexMatches = true;
            return this;
        }

        public Pipeline.Builder atSimilarityWeight(double s){
            if( s < 0 || s > 1){
                throw new NumberFormatException("Value SimilarityWeight is out of range 0 to 1");
            }
            this.similarityWeight = s;
            return this;
        }

        public Pipeline.Builder atPostprocessThreshold(double p){
            if( p < 0 || p > 1){
                throw new NumberFormatException("Value PostprocessThreshold is out of range 0 to 1");
            }
            this.postprocessThreshold = p;
            return this;
        }

        public Pipeline Build(){
            Pipeline pip = new Pipeline();
            pip.similarityWeight = this.similarityWeight;
            pip.complexMatches = this.complexMatches;
            pip.postprocessThreshold = this.postprocessThreshold;
            return pip;
        }

    }


}
