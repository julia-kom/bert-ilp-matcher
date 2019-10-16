package bpm.evaluation;

import bpm.ilp.AbstractILP;
import bpm.matcher.Matcher;
import bpm.similarity.Word;
import java.io.File;
import java.nio.file.Path;

public class Pipeline{
    //Standard matcher options
    private bpm.matcher.Pipeline matchingPipeline;

    //Evaluation specific options
    private boolean batch;
    private Path batchPath;
    private File net1;
    private File net2;
    private File goldStandard;
    private Path goldStandardPath;

    public void run(){
        if(batch)
            batchEval();
        else
            singleEval();
    }


    public void singleEval(){


    }

    public void batchEval(){

    }


    /**
     * Builder for the evaluation pipeline
     */
    public static class Builder{
        // general options
        private bpm.matcher.Pipeline matchingPipeline = new bpm.matcher.Pipeline.Builder().Build();

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
         * Perform a batch test on given folder
         * @return
         */
        public Builder withMatcher(bpm.matcher.Pipeline matchingPipeline){
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
            pip.matchingPipeline = this.matchingPipeline;

            //evaluation specific information
            pip.batch = this.batch;
            pip.batchPath = this.batchPath;
            pip.net1 = this.net1;
            pip.net2 = this.net2;
            pip.goldStandard = this.goldStandard;
            pip.goldStandardPath = this.goldStandardPath;

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
