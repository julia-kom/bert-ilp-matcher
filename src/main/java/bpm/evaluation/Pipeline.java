package bpm.evaluation;

import bpm.alignment.Alignment;
import bpm.alignment.Result;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
    private Eval.Strategies evalStrat;

    public void run(){
        if(batch) {
            List<Eval> evals = batchEval();
            AggregatedEval aggregatedEval = new AggregatedEval(evals);
        } else {
            Eval eval = singleEval(net1, net2, goldStandard);
        }
    }

    /**
     * Perform single evaluation
     * @param n1 net 1
     * @param n2 net 2
     * @param gs gold standard
     * @return
     */
    public Eval singleEval(File n1, File n2, File gs){
        // Compute Alignment
        Result result = matchingPipeline.run(n1,n2);

        // Read Gold Standard
        RdfAlignmentReader reader = new RdfAlignmentReader();
        Alignment goldstandard = reader.getAlignmentFrom(gs);

        //Evaluate
        return evaluate(result.getAlignment(),goldstandard);

    }


    /**
     * Perform Batch Evaluation
     * @return
     */
    public List<Eval> batchEval(){
        // Get all pnml files in the batch dir
        File[] files =  batchPath.toFile().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pnml");
            }
        });

        // run evaluation for each combination
        List<Eval> evals = new ArrayList<>();
        for(File f1 : files){
            for(File f2 : files){
                if(f1 != f2) {
                    evals.add(singleEval(f1, f2, null));
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
    private Eval evaluate(Alignment matcher, Alignment goldstandard){
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
        private Eval.Strategies evalStrat = Eval.Strategies.BINARY;

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

        public Builder withEvalStrat(Eval.Strategies strat){
            this.evalStrat = strat;
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
            pip.evalStrat = this.evalStrat;

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
