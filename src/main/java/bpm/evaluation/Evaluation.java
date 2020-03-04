package bpm.evaluation;

import bpm.ippm.profile.AbstractProfile;
import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
        Option optRetrospectiveEval = new Option("r", "retrospective", false, "Run retrospective evaluation");
        Option optSysPrint = new Option("sys", "system-print", false, "Run code with System println commands");
        Option optNetEval= new Option("ne", "net-eval", false, "Run petri net evaluation");
        Option optGSEval= new Option("gse", "gs-eval", false, "Run gold standard evaluation");

        Option optSimilarityWeight = Option.builder("s")
                .required(false)
                .hasArg(true)
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
                        "LIN: Lin Similarity \n" +
                        "LEVENSHTEIN: Levenshtein Similarity \n" +
                        "JIANG: Jiang Similarity \n" +
                        "LEVENSHTEIN-LIN-MAX: Maximum of Levenshtein and Lin Similarity \n" +
                        "LEVENSHTEIN-JIANG-MAX: Maximum of Levenshtein and Jiang Similarity")
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
        Option optLogPath = Option.builder("lp")
                .hasArg(true)
                .longOpt("log-path")
                .desc("Path to the folder containing all the logs that should be compared")
                .build();
        Option optGoldStandardPath = Option.builder("gsp")
                .hasArg(true)
                .longOpt("gold-standard-path")
                .desc("Gold Standard Path for batch or retrospective evaluation")
                .build();
        Option optResultPath = Option.builder("rp")
                .hasArg(true)
                .longOpt("result-path")
                .desc("ResultPath of a previous evaluation for performing a retrospective evaluation")
                .build();
        Option optEvalStrat = Option.builder("e")
                .hasArg(true)
                .longOpt("eval-strat")
                .desc("Choose the evaluation strategy: \n" +
                        "Binary: Lose Binary Relations. Partley correct matches account partly \n" +
                        "StrictBinary: Only exact matches account as true positives \n" +
                        "Probabilistically: Acc. to \"Probabilistic Evaluation of Process Model Matching Techniques\" ")
                .build();
        Option optNetEvalProfile = Option.builder("p")
                .hasArg(true)
                .longOpt("profile")
                .desc("Profile for matching/net evaluation")
                .build();
        Option optPreMatch = new Option("pm", "pre-match", false, "Run Prematcher before running the ILP, reducing runtime");

        Option optTl = Option.builder("tl")
                .hasArg(true)
                .longOpt("ilp-time-limit")
                .desc("Choose the time limit for the ILP in seconds.")
                .build();
        Option optNl = Option.builder("nl")
                .hasArg(true)
                .longOpt("ilp-node-limit")
                .desc("Choose the node limit for the ILP in seconds.")
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
        options.addOption(optResultPath);
        options.addOption(optEvalStrat);
        options.addOption(optPreMatch);
        options.addOption(optRetrospectiveEval);
        options.addOption(optNetEval);
        options.addOption(optNetEvalProfile);
        options.addOption(optSysPrint);
        options.addOption(optTl);
        options.addOption(optNl);
        options.addOption(optGSEval);
        options.addOption(optLogPath);

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
        bpm.ippm.matcher.Pipeline.Builder matcherBuilder = new bpm.ippm.matcher.Pipeline.Builder();


        // parse complexMatches
        if (line.hasOption("c")) {
            matcherBuilder = matcherBuilder.withComplexMatches();
        }

        // parse complexMatches
        if (line.hasOption("sys")) {
            bpm.ippm.matcher.Pipeline.PRINT_ENABLED = true;
        }

        // parse prematch
        if (line.hasOption("pm")) {
            matcherBuilder = matcherBuilder.withPreMatching();
        }

        // parse similarityWeight
        if (line.hasOption("s")) {
            String sString = line.getOptionValue("s");
            try {
                double s = Double.parseDouble(sString);
                matcherBuilder = matcherBuilder.atSimilarityWeight(s);
            } catch (NumberFormatException numExp) {
                System.err.println("Parsing Failed: Number Input s " + numExp.getMessage());
            }
        }

        // parse postprocessThreshold
        if (line.hasOption("pp")) {
            String pString = line.getOptionValue("pp");
            try {
                double p = Double.parseDouble(pString);
                evalBuilder = evalBuilder.atThreshold(p);
                matcherBuilder = matcherBuilder.atPostprocessThreshold(p);
            } catch (NumberFormatException numExp) {
                System.err.println("Parsing Failed: Number Input p " + numExp.getMessage());
                System.exit(1);
            }
        }

        // net 1 for single eval
        if (line.hasOption("i")) {
            String iString = line.getOptionValue("i");
            matcherBuilder = matcherBuilder.withILP(iString);
        }

        // net 1 for single eval
        if (line.hasOption("n1")) {
            String n1String = line.getOptionValue("n1");
            File net1 = new File(n1String);
            evalBuilder = evalBuilder.onNet1(net1);
        }

        // net 2 for single eval
        if (line.hasOption("n2")) {
            String n2String = line.getOptionValue("n2");
            File net2 = new File(n2String);
            evalBuilder = evalBuilder.onNet2(net2);
        }

        // gold standard for single eval
        if (line.hasOption("gs")) {
            String n2String = line.getOptionValue("gs");
            File gs = new File(n2String);
            evalBuilder = evalBuilder.withGoldStandard(gs);
        }

        // Perform Batch
        if (line.hasOption("b")) {
            evalBuilder = evalBuilder.withBatch();
        }

        // Perform Net Eval
        if (line.hasOption("ne")) {
            evalBuilder = evalBuilder.withNetEval();
        }

        // Perform Gold Standard Eval
        if (line.hasOption("gse")) {
            evalBuilder = evalBuilder.withGSEval();
        }

        // gold standard for single eval
        if (line.hasOption("p")) {
            try{
                String sString = line.getOptionValue("p");
                evalBuilder = evalBuilder.withNetEvalProfile(sString);
                matcherBuilder =matcherBuilder.withProfile(AbstractProfile.Profile.valueOf(sString));
            } catch (NumberFormatException numExp) {
                System.err.println("Parsing Failed: Time Limit " + numExp.getMessage());
            }
        }


        // word similarity
        if (line.hasOption("w")) {
            String n2String = line.getOptionValue("w");
            matcherBuilder = matcherBuilder.withWordSimilarity(n2String);
        }

        // path that contains all nets to compare
        if (line.hasOption("np")) {
            String netString = line.getOptionValue("np");
            Path nets = Paths.get(netString);
            evalBuilder = evalBuilder.withBatchPath(nets);
        }

        // path that contains all nets to compare
        if (line.hasOption("lp")) {
            String netString = line.getOptionValue("lp");
            Path processLogPath = Paths.get(netString);
            evalBuilder = evalBuilder.withLogPath(processLogPath);
        }

        // Gold Standard Path needed for batch
        if (line.hasOption("gsp")) {
            String gsString = line.getOptionValue("gsp");
            Path gs = Paths.get(gsString);
            evalBuilder = evalBuilder.withGoldStandard(gs);
        }

        // parse complexMatches
        if (line.hasOption("r")) {
            evalBuilder = evalBuilder.withRetrospective();
        }

        // Retrospective Result Path needed for retrospective analysis
        if (line.hasOption("rp")) {
            String rString = line.getOptionValue("rp");
            Path rp = Paths.get(rString);
            evalBuilder = evalBuilder.withResultPath(rp);
        }

        if (line.hasOption("e")) {
            Eval.Strategies strat = Eval.Strategies.valueOf(line.getOptionValue("e"));
            evalBuilder.withEvalStrat(strat);
        }


        if (line.hasOption("tl")) {
            String sString = line.getOptionValue("tl");
            try {
                double s = Double.parseDouble(sString);
                matcherBuilder = matcherBuilder.withILPTimeLimit(s);
            } catch (NumberFormatException numExp) {
                System.err.println("Parsing Failed: Time Limit " + numExp.getMessage());
            }
        }

        if (line.hasOption("nl")) {
            String sString = line.getOptionValue("nl");
            try {
                double s = Double.parseDouble(sString);
                matcherBuilder= matcherBuilder.withILPNodeLimit(s);
            } catch (NumberFormatException numExp) {
                System.err.println("Parsing Failed: Time Limit " + numExp.getMessage());
            }
        }

        //build
        bpm.ippm.matcher.Pipeline matchingPip = matcherBuilder.Build();
        evalBuilder.withMatcher(matchingPip);
        Pipeline evalPip = evalBuilder.build();


        //run
        evalPip.run();

        //Write Config to File
        try {
            File f = new File(evalPip.getLogPath() + "/config.log");
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.write(evalPip.toJSON().toString());

            writer.close();
        } catch (Exception e) {
            System.err.println("Unable to write Evaluation/Matcher Config file: " + e.getMessage());
        }
    }

}
