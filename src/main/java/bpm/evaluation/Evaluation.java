package bpm.evaluation;

import bpm.ippm.ilp.AbstractILP;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.similarity.LabelSimilarity;
import bpm.ippm.similarity.Word;
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
        Option optHelp= new Option("h", "help", false, "Get help");
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
                .desc("Minimum Similarity value between 0 and 1 a, by the ILP proposed correspondence, got to have, to account as a match.")
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
        Option optPathLog1 = Option.builder("l1")
                .hasArg(true)
                .longOpt("log-1")
                .desc("Log for WF net 1")
                .build();
        Option optPathLog2 = Option.builder("l2")
                .hasArg(true)
                .longOpt("log-2")
                .desc("Log for WF net 2")
                .build();
        Option optIlp = Option.builder("i")
                .hasArg(true)
                .longOpt("ilp")
                .desc("Choose an ILP Matcher here: \n BASIC: 1:1 Matcher with ILP. Slow but returns similarity and matching. \n" +
                        "SYMMETRIC: 1:1 Matcher with ILP and makes use of similarity property of the profile. Dont use with BP+ or log based approaches. \n" +
                        "CUSTOM_IDENTIFICATION: 1:1 Matcher with ILP. Uses relation similarity function of a profile. Use this for BP+ and log based profiles.")
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
                .desc("Result path of a previous evaluation for performing a retrospective evaluation")
                .build();
        Option optEvalStrat = Option.builder("e")
                .hasArg(true)
                .longOpt("eval-strat")
                .desc("Choose the evaluation strategy: \n" +
                        "Binary: Lose Binary Relations. Partley correct matches account partly \n" +
                        "StrictBinary: Only exact matches account as true positives \n" +
                        "Probabilistically: Acc. to \"Probabilistic Evaluation of Process Model Matching Techniques\" NOT IMPLEMENTED ")
                .build();
        Option optNetEvalProfile = Option.builder("p")
                .hasArg(true)
                .longOpt("profile")
                .desc("Profile for matching/net evaluation")
                .build();
        Option optPreMatch = new Option("pm", "pre-match", false, "Run Prematcher before running the ILP.");

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
        Option optLS = Option.builder("ls")
                .hasArg(true)
                .longOpt("label-sim")
                .desc("Choose a label similarity function: \n" +
                        "BERT: Bert Similarity \n" +
                        "BERTFT: Bert Similarity \n" +
                        "BOW: Bag-of-Words Similarity")
                .build();

        //combine options
        Options options = new Options();
        options.addOption(optSimilarityWeight);
        options.addOption(optPostprocessThreshold);
        options.addOption(optIlp);
        options.addOption(optWordSim);
        options.addOption(optGoldStandard);
        options.addOption(optPathNet1);
        options.addOption(optPathNet2);
        options.addOption(optPathLog1);
        options.addOption(optPathLog2);
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
        options.addOption(optHelp);
        options.addOption(optLS);

        //parse input
        CommandLine line;
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
           throw new Error("Parsing failed.  Reason: " + exp.getMessage());

        }

        //create matching pipeline
        Pipeline.Builder evalBuilder = new Pipeline.Builder();
        bpm.ippm.matcher.Pipeline.Builder matcherBuilder = new bpm.ippm.matcher.Pipeline.Builder();

        // help
        if (line.hasOption("h")) {
            new HelpFormatter().printHelp("evaluation", options);
            return;
        }

        // use system out println
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
            } catch (Exception numExp) {
                throw new Error("Parsing Failed: Number Input s " + numExp.getMessage());
            }
        }

        // parse postprocessThreshold
        if (line.hasOption("pp")) {
            String pString = line.getOptionValue("pp");
            try {
                double p = Double.parseDouble(pString);
                evalBuilder = evalBuilder.atThreshold(p);
                matcherBuilder = matcherBuilder.atPostprocessThreshold(p);
            } catch (Exception numExp) {
                throw new Error("Parsing Failed: Number Input pp " + numExp.getMessage());
            }
        }

        // parse ilp
        if (line.hasOption("i")) {
            String iString = line.getOptionValue("i");
            try {
                matcherBuilder = matcherBuilder.withILP(AbstractILP.ILP.valueOf(iString));
            }catch(Exception e){
                throw new Error("It was not possible to interpret the ILP" + iString);
            }
        }

        // profile
        if (line.hasOption("p")) {
            try{
                String sString = line.getOptionValue("p");
                evalBuilder = evalBuilder.withNetEvalProfile(sString);
                matcherBuilder =matcherBuilder.withProfile(AbstractProfile.Profile.valueOf(sString));
            } catch (Exception numExp) {
                throw new Error("Parsing Failed: Profile " + numExp.getMessage());
            }
        }

        // word similarity
        if (line.hasOption("w")) {
            String n2String = line.getOptionValue("w");
            try {
                matcherBuilder = matcherBuilder.withWordSimilarity(Word.Similarities.valueOf(n2String));
            } catch (Exception e) {
                throw new Error("Not possible to read the word similarity" + n2String);
            }
        }

        //time limit
        if (line.hasOption("tl")) {
            String sString = line.getOptionValue("tl");
            try {
                double s = Double.parseDouble(sString);
                matcherBuilder = matcherBuilder.withILPTimeLimit(s);
            } catch (Exception numExp) {
                throw new Error("Parsing Failed: Time Limit " + numExp.getMessage());
            }
        }

        // node limit
        if (line.hasOption("nl")) {
            String sString = line.getOptionValue("nl");
            try {
                double s = Double.parseDouble(sString);
                matcherBuilder= matcherBuilder.withILPNodeLimit(s);
            } catch (Exception numExp) {
                throw new Error ("Parsing Failed: Node Limit " + numExp.getMessage());
            }
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

        // log 1 for single eval
        if (line.hasOption("l1")) {
            String n1String = line.getOptionValue("l1");
            File log1 = new File(n1String);
            evalBuilder = evalBuilder.onLog1(log1);
        }

        // log 2 for single eval
        if (line.hasOption("l2")) {
            String n2String = line.getOptionValue("l2");
            File log2 = new File(n2String);
            evalBuilder = evalBuilder.onLog2(log2);
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

        //eval strategy
        if (line.hasOption("e")) {
            Eval.Strategies strat = Eval.Strategies.valueOf(line.getOptionValue("e"));
            evalBuilder.withEvalStrat(strat);
        }

        if(line.hasOption("ls")){
            String lsString = line.getOptionValue("ls");
            try {
                matcherBuilder = matcherBuilder.withLabelSimilarity(LabelSimilarity.Similarities.valueOf(lsString));
            } catch (Exception e) {
                throw new Error("Not possible to read the label similarity" + lsString);
            }
        }


        //build matcher and evaluation pipeline
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
