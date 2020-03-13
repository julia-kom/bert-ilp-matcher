package bpm.ippm.matcher;

import bpm.ippm.alignment.Result;
import bpm.ippm.ilp.AbstractILP;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.similarity.Word;
import org.apache.commons.cli.*;
import java.io.*;

public class Matcher {

    /**
     * Input parser for a single matching task
     *
     * @param args see -h for args
     */
    public static void main(String[] args) {
        // create each option needed
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
                .required(false)
                .hasArg(true)
                .longOpt("net-1")
                .desc("Sound, free-choice WF net 1")
                .build();
        Option optPathNet2 = Option.builder("n2")
                .required(false)
                .hasArg(true)
                .longOpt("net-2")
                .desc("Sound, free-choice WF net 2")
                .build();
        Option optPathLog1 = Option.builder("l1")
                .required(false)
                .hasArg(true)
                .longOpt("log-1")
                .desc("Log of net 1")
                .build();
        Option optPathLog2 = Option.builder("l2")
                .required(false)
                .hasArg(true)
                .longOpt("log-2")
                .desc("Log of net 2")
                .build();
        Option optProfile = Option.builder("p")
                .hasArg(true)
                .longOpt("profile")
                .desc("Relational Profile to use: BP, BPP, ARP, LOG_EF,LOG_DF")
                .build();

        Option ilp = Option.builder("i")
                .hasArg(true)
                .longOpt("ilp")
                .desc("Choose an ILP Matcher here: \n BASIC: 1:1 Matcher with ILP. Slow but returns similarity and matching. \n" +
                        "SYMMETRIC: 1:1 Matcher with ILP and makes use of similarity property of the profile. Dont use with BP+ or log based approaches. \n" +
                        "CUSTOM_IDENTIFICATION: 1:1 Matcher with ILP. Uses relation similarity function of a profile. Use this for BP+ and log based profiles.")
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
                        "LIN: Lin Similarity \n" +
                        "LEVENSHTEIN: Levenshtein Similarity \n" +
                        "JIANG: Jiang Similarity \n" +
                        "LEVENSHTEIN-LIN-MAX: Maximum of Levenshtein and Lin Similarity \n" +
                        "LEVENSHTEIN-JIANG-MAX: Maximum of Levenshtein and Jiang Similarity")
                .build();

        Option optPreMatch = new Option("pm", "pre-match", false, "Run Prematcher before running the ILP, reducing runtime");
        Option optPrint= new Option("sys", "sys-print", false, "Enable System out print commands");

        Option optHelp= new Option("h", "help", false, "Get help.");




        //combine options
        Options options = new Options();
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
        options.addOption(optHelp);


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
        Pipeline.Builder builder = new Pipeline.Builder();

        // help
        if (line.hasOption("h")) {
            new HelpFormatter().printHelp("matcher", options);
            return;
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
            } catch (Exception numExp) {
                 throw new Error("Parsing Failed: Time Limit " + numExp.getMessage());
            }
        }


        // parse ilpNodeLimit
        if (line.hasOption("nl")) {
            String sString = line.getOptionValue("nl");
            try {
                double s = Double.parseDouble(sString);
                builder = builder.withILPNodeLimit(s);
            } catch (Exception numExp) {
                throw new Error("Parsing Failed: Node Limit " + numExp.getMessage());
            }
        }

        // parse profile
        if (line.hasOption("p")) {
            String sString = line.getOptionValue("p");
            try {
                builder = builder.withProfile(AbstractProfile.Profile.valueOf(sString));
            } catch (Exception numExp) {
                throw new Error("Parsing Failed: Profile does not exist. Either use BP, ARP, BPP, LOG_EF, or LOG_DF " + numExp.getMessage());
            }
        }


        // parse similarityWeight
        if (line.hasOption("s")) {
            String sString = line.getOptionValue("s");
            try {
                double s = Double.parseDouble(sString);
                builder = builder.atSimilarityWeight(s);
            } catch (Exception numExp) {
                throw new Error("Parsing Failed: Number Input sim-weight " + numExp.getMessage());
            }
        }

        // word similarity
        if (line.hasOption("w")) {
            String n2String = line.getOptionValue("w");
            try {
                builder = builder.withWordSimilarity(Word.Similarities.valueOf(n2String));
            }catch (Exception e){
                throw new Error("Parsing Failed: Word Similarity " +n2String+ ": " + e.getMessage());
            }
        }

        // parse postprocessThreshold
        if (line.hasOption("pp")) {
            String pString = line.getOptionValue("pp");
            try {
                double p = Double.parseDouble(pString);
                builder = builder.atPostprocessThreshold(p);
            } catch (Exception numExp) {
                throw new Error("Parsing Failed: Number Input pp " + numExp.getMessage());
            }
        }

        //parse ILP
        if (line.hasOption("i")) {
            String sIlp = line.getOptionValue("i");
            try{
                builder.withILP(AbstractILP.ILP.valueOf(sIlp));
            }catch(Exception e){
                throw new Error("ILP not possible to interpret" + sIlp);
            }
        }

        // parse petri net 1 and 2
        File net1;
        File net2;

        String n1String = line.getOptionValue("n1");
        if(n1String == null){
            throw new Error("Net 1 is a required parameter");
        }

        String n2String = line.getOptionValue("n2");
        if(n2String == null){
            throw new Error("Net 2 is a required parameter");
        }

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
            throw new Error("Parsing Failed: Petri Net File not found:" + fileExp.getMessage());
        }


        // parse log net 1 and 2 (those are optional. If given and a profile that can handle those is
        // chosen they will be used. Otherwise not
        File log1 = null;
        File log2 = null;
        String log1String = line.getOptionValue("l1");
        String log2String = line.getOptionValue("l2");

        if(log1String != null) {
            try {
                log1 = new File(log1String);
                if (!log1.exists()) {
                    throw new FileNotFoundException("Log 1 file not found under" + log1String);
                }
            } catch (FileNotFoundException fileExp) {
                //if no log given, then we work without a log
                throw new Error("Log file not found." +fileExp.getMessage());
            }
        }
        if(log2String != null){
            try {
                log2 = new File(log2String);
                if (!log2.exists()) {
                    throw new FileNotFoundException("Log 2 file not found under" + log2String);
                }
            } catch (FileNotFoundException fileExp) {
                //if no log given, then we work without a log
                throw new Error("Log file not found." +fileExp.getMessage());
            }
        }

        //Build pipeline
        Pipeline pip = builder.Build();

        //run files
        Result r = pip.run(net1, log1, net2, log2);

        //print
        System.out.println(r.toString());
    }



}
