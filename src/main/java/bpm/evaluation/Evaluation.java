package bpm.evaluation;

import org.apache.commons.cli.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Evaluation {

    /**
     * Input parser for the evaluation
     *
     * @param args see -h for args
     */
    public static void main(String[] args) {
        Option optComplexMatches = new Option("c", "complex-matches", false, "Run Matcher which detects complex matches (n:m, 1:n)");
        Option optSimilarityWeight = Option.builder("s")
                .required(false)
                .longOpt("similarity-weight")
                .desc("Weight between 0 and 1 which defines how much the behavioral similarity should define the match.")
                .type(Double.TYPE)
                .build();
        Option optPostprocessThreshold = Option.builder("pp")
                .required(false)
                .hasArg(true)
                .longOpt("postprocess-threshold")
                .desc("Minimum Similarity value between 0 and 1 a, by the ILP proposed correspondance, got to have, to account as a match.")
                .type(Double.TYPE)
                .build();
        Option optPathNet1 = Option.builder("n1")
                .hasArg(true)
                .longOpt("net-1")
                .desc("Sound, free-choice WF net 1")
                .build();
        Option optPathNet2 = Option.builder("n2")
                .hasArg(true)
                .longOpt("net-2")
                .desc("Sound, free-choice WF net 2")
                .build();
        Option optIlp = Option.builder("i")
                .hasArg(true)
                .longOpt("ilp")
                .desc("Choose an ILP Matcher here: \n Basic: 1:1 Matcher with ILP. Slow but returns similarity and matching. \n" +
                        "Relaxed: 1:1 Matcher with LP. Slow but returns similarity and matching \n" +
                        "Relaxed2: 1:1 Matcher with LP. Qucik but only matching.")
                .build();
        Option optWordSim = Option.builder("w")
                .hasArg(true)
                .longOpt("word-sim")
                .desc("Choose a used word similarity function, used inside the Bag-of-Words Label Similarity: \n " +
                        "Lin: Lin Similarity \n" +
                        "Levenshtein: Levenshtein Similarity \n" +
                        "Jiang: Jiang Similarity \n" +
                        "Levenshtein-Lin-Max: Maximum of Levenshtein and Lin Similarity \n" +
                        "Levenshtein-Jiang-Max: Maximum of Levenshtein and Jiang Similarity")
                .build();
        Option optGoldStandard = Option.builder("gs")
                .hasArg(true)
                .longOpt("gold-standard")
                .desc("Gold Standard for single evaluation")
                .build();
        Option optBatch = Option.builder("b")
                .longOpt("batch")
                .desc("Perform batch evaluation")
                .build();
        Option optNetPath = Option.builder("np")
                .hasArg(true)
                .longOpt("net-path")
                .desc("Path to the folder containing all the nets that should be compared")
                .build();
        Option optGoldStandardPath = Option.builder("gsp")
                .hasArg(true)
                .longOpt("gold-standard-path")
                .desc("Gold Standard Path for batch evaluation")
                .build();

        //combine options
        Options options = new Options();
        options.addOption(optComplexMatches);
        options.addOption(optSimilarityWeight);
        options.addOption(optPostprocessThreshold);
        options.addOption(optIlp);
        options.addOption(optWordSim);
        options.addOption(optGoldStandard);
        options.addOption(optPathNet1);
        options.addOption(optPathNet2);
        options.addOption(optBatch);
        options.addOption(optNetPath);
        options.addOption(optGoldStandardPath);

        //parse input
        CommandLine line;
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            line = null;
            System.exit(1);
        }

        //create matching pipeline
        Pipeline.Builder evalBuilder = new Pipeline.Builder();
        bpm.matcher.Pipeline.Builder matcherBuilder= new bpm.matcher.Pipeline.Builder();


        // parse complexMatches
        if (line.hasOption("c")) {
            matcherBuilder = matcherBuilder.withComplexMatches();
        }

        // parse similarityWeight
        if (line.hasOption("s")) {
            String sString = line.getOptionValue("s");
            try {
                double s = Double.parseDouble(sString);
                matcherBuilder = matcherBuilder.atSimilarityWeight(s);
            } catch (NumberFormatException numExp) {
                System.out.println("Parsing Failed: Number Input s " + numExp.getMessage());
            }
        }

        // parse postprocessThreshold
        if (line.hasOption("p")) {
            String pString = line.getOptionValue("pp");
            try {
                double p = Double.parseDouble(pString);
                matcherBuilder = matcherBuilder.atPostprocessThreshold(p);
            } catch (NumberFormatException numExp) {
                System.out.println("Parsing Failed: Number Input p " + numExp.getMessage());
                System.exit(1);
            }
        }

        // net 1 for single eval
        if(line.hasOption("n1")) {
            String n1String = line.getOptionValue("n1");
            File net1 = new File(n1String);
            evalBuilder.onNet1(net1);
        }

        // net 2 for single eval
        if(line.hasOption("n2")) {
            String n2String = line.getOptionValue("n2");
            File net2 = new File(n2String);
            evalBuilder.onNet2(net2);
        }

        // gold standard for single eval
        if(line.hasOption("gs")) {
            String n2String = line.getOptionValue("gs");
            File gs = new File(n2String);
            evalBuilder.withGoldStandard(gs);
        }

        // Perform Batch
        if(line.hasOption("b")){
            evalBuilder.withBatch();
        }

        // path that contains all nets to compare
        if(line.hasOption("np")){
            String netString = line.getOptionValue("np");
            Path nets = Paths.get(netString);
            evalBuilder.withPath(nets);
        }

        // Gold Standard Path needed for batch
        if(line.hasOption("gsp")){
            String gsString = line.getOptionValue("gsp");
            Path gs = Paths.get(gsString);
            evalBuilder.withGoldStandard(gs);
        }

        //build and run
        bpm.matcher.Pipeline matchingPip = matcherBuilder.Build();
        evalBuilder.withMatcher(matchingPip);
        Pipeline evalPip = evalBuilder.build();
        evalPip.run();
    }
}
