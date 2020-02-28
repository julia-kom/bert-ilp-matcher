package bpm.matcher;

import bpm.alignment.Result;
import bpm.profile.AbstractProfile;
import org.apache.commons.cli.*;
import java.io.*;

public class Matcher {

    /**
     * Input parser for a single matching task
     *
     * @param args see -h for args
     */
    public static void main(String[] args) {
        // create each option neeeded
        Option optComplexMatches = new Option("c", "complex-matches", false, "Run Matcher which detects complex matches (n:m, 1:n)");
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
                .required(true)
                .hasArg(true)
                .longOpt("net-1")
                .desc("Sound, free-choice WF net 1")
                .build();
        Option optPathNet2 = Option.builder("n2")
                .required(true)
                .hasArg(true)
                .longOpt("net-2")
                .desc("Sound, free-choice WF net 2")
                .build();
        Option optPathLog1 = Option.builder("l1")
                .required(false)
                .hasArg(true)
                .longOpt("log-1")
                .desc("Sound, free-choice WF net 1")
                .build();
        Option optPathLog2 = Option.builder("l2")
                .required(false)
                .hasArg(true)
                .longOpt("log-2")
                .desc("Sound, free-choice WF net 2")
                .build();
        Option optProfile = Option.builder("p")
                .hasArg(true)
                .longOpt("profile")
                .desc("Relational Profile to use: BP, BPP, ARP")
                .build();

        Option ilp = Option.builder("i")
                .hasArg(true)
                .longOpt("ilp")
                .desc("Choose an ILP Matcher here: \n Basic: 1:1 Matcher with ILP. Slow but returns similarity and matching. \n" +
                        "Relaxed: 1:1 Matcher with LP. Slow but returns similarity and matching \n" +
                        "Relaxed2: 1:1 Matcher with LP. Qucik but only matching.")
                .build();

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

        Option wordSim = Option.builder("w")
                .hasArg(true)
                .longOpt("word-sim")
                .desc("Choose a used word similarity function, used inside the Bag-of-Words Label Similarity: \n " +
                        "Lin: Lin Similarity \n" +
                        "Levenshtein: Levenshtein Similarity \n" +
                        "Jiang: Jiang Similarity \n" +
                        "Levenshtein-Lin-Max: Maximum of Levenshtein and Lin Similarity \n" +
                        "Levenshtein-Jiang-Max: Maximum of Levenshtein and Jiang Similarity")
                .build();

        Option optPreMatch = new Option("pm", "pre-match", false, "Run Prematcher before running the ILP, reducing runtime");
        Option optPrint= new Option("sys", "sys-print", false, "Enable System out print commands");



        //combine options
        Options options = new Options();
        options.addOption(optComplexMatches);
        options.addOption(optSimilarityWeight);
        options.addOption(optPostprocessThreshold);
        options.addOption(ilp);
        options.addOption(wordSim);
        options.addOption(optPathNet1);
        options.addOption(optPathNet2);
        options.addOption(optPathLog1);
        options.addOption(optPathLog2);
        options.addOption(optPreMatch);
        options.addOption(optPrint);
        options.addOption(optTl);
        options.addOption(optNl);
        options.addOption(optProfile);


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
        Pipeline.Builder builder = new Pipeline.Builder();

        // parse complexMatches
        if (line.hasOption("c")) {
            builder = builder.withComplexMatches();
        }

        // parse prematcher
        if (line.hasOption("pm")) {
            builder = builder.withPreMatching();
        }

        // print commands
        if (line.hasOption("sys")) {
            Pipeline.PRINT_ENABLED = true;
        }

        // parse ilpTimeLimit
        if (line.hasOption("tl")) {
            String sString = line.getOptionValue("tl");
            try {
                double s = Double.parseDouble(sString);
                builder = builder.withILPTimeLimit(s);
            } catch (NumberFormatException numExp) {
                 System.err.println("Parsing Failed: Time Limit " + numExp.getMessage());
            }
        }


        // parse ilpNodeLimit
        if (line.hasOption("nl")) {
            String sString = line.getOptionValue("nl");
            try {
                double s = Double.parseDouble(sString);
                builder = builder.withILPNodeLimit(s);
            } catch (NumberFormatException numExp) {
                System.err.println("Parsing Failed: Time Limit " + numExp.getMessage());
            }
        }

        // parse profile
        if (line.hasOption("p")) {
            String sString = line.getOptionValue("p");
            try {
                builder = builder.withProfile(AbstractProfile.Profile.valueOf(sString));
            } catch (NumberFormatException numExp) {
                System.err.println("Parsing Failed: Time Limit " + numExp.getMessage());
            }
        }


        // parse similarityWeight
        if (line.hasOption("s")) {
            String sString = line.getOptionValue("s");
            try {
                double s = Double.parseDouble(sString);
                builder = builder.atSimilarityWeight(s);
            } catch (NumberFormatException numExp) {
                System.err.println("Parsing Failed: Number Input s " + numExp.getMessage());
            }
        }

        // word similarity
        if (line.hasOption("w")) {
            String n2String = line.getOptionValue("w");
            builder = builder.withWordSimilarity(n2String);
        }

        // parse postprocessThreshold
        if (line.hasOption("pp")) {
            String pString = line.getOptionValue("pp");
            try {
                double p = Double.parseDouble(pString);
                builder = builder.atPostprocessThreshold(p);
            } catch (NumberFormatException numExp) {
                System.err.println("Parsing Failed: Number Input p " + numExp.getMessage());
                System.exit(1);
            }
        }

        //parse ILP
        if (line.hasOption("i")) {
            String sIlp = line.getOptionValue("i");
            if(sIlp != null) {
                builder.withILP(sIlp);
            }else{
                throw new IllegalArgumentException("ilp argument null");
            }
        }


        // parse petri net 1 and 2
        File net1;
        File net2;
        String n1String = line.getOptionValue("n1");
        String n2String = line.getOptionValue("n2");
        try {
            net1 = new File(n1String);
            if (!net1.exists()) {
                throw new FileNotFoundException("Net 1 file not found under" + n1String);
            }
            net2 = new File(n2String);
            if (!net2.exists()) {
                throw new FileNotFoundException("Net 2 file not found under" + n2String);
            }
        } catch (FileNotFoundException fileExp) {
            System.err.println("Parsing Failed: Petri Net File not found:" + fileExp.getMessage());
            System.exit(1);
            net1 = null;
            net2 = null;

        }


        // parse log net 1 and 2 (those are optional. If given and a profile that can handle those is
        // chosen they will be used. Otherwise not
        File log1 = null;
        File log2 = null;
        String log1String = line.getOptionValue("l1");
        String log2String = line.getOptionValue("l2");

        if(log1String == null || log2String == null){
            System.out.println("Run without logs");
        }else {
            try {
                log1 = new File(log1String);
                if (!log1.exists()) {
                    throw new FileNotFoundException("Log 1 file not found under" + log1String);
                }
            } catch (FileNotFoundException fileExp) {
                //if no log given, then we work without a log
                log1 = null;
            }
            try {
                log2 = new File(log2String);
                if (!log2.exists()) {
                    throw new FileNotFoundException("Log 2 file not found under" + log2String);
                }
            } catch (FileNotFoundException fileExp) {
                //if no log given, then we work without a log
                log2 = null;
            }
        }

        //Build pipeline
        Pipeline pip = builder.Build();

        //run files
        Result r = pip.run(net1, log1, net2, log2);

        //print
        if(Pipeline.PRINT_ENABLED) System.out.println(r.toString());
    }



}
