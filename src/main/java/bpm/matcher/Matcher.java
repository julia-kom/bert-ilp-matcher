package bpm.matcher;

import org.apache.commons.cli.*;
import java.io.*;

public class Matcher {

    /**
     * Input parser for a single matching task
     *
     * @param args see -h for args
     */
    public static void main(String[] args){
        // create each option neeeded
        Option optComplexMatches = new Option("c", "complex-matches", false,"Run Matcher which detects complex matches (n:m, 1:n)");
        Option optSimilarityWeight = Option.builder("s")
                .required(false)
                .longOpt("similarity-weight")
                .desc("Weight between 0 and 1 which defines how much the behavioral similarity should define the match.")
                .type(Double.TYPE)
                .build();
        Option optPostprocessThreshold = Option.builder("p")
                .required(false)
                .longOpt("postprocess-threshold")
                .desc("Minimum Similarity value between 0 and 1 a, by the ILP proposed correspondance, got to have, to account as a match.")
                .type(Double.TYPE)
                .build();
        Option optPathNet1 = Option.builder("n1")
                .required(true)
                .longOpt("net-1")
                .desc("Sound, free-choice WF net 1")
                .build();
        Option optPathNet2 = Option.builder("n2")
                .required(true)
                .longOpt("net-2")
                .desc("Sound, free-choice WF net 2")
                .build();

        //combine options
        Options options = new Options();
        options.addOption(optComplexMatches);
        options.addOption(optSimilarityWeight);
        options.addOption(optPostprocessThreshold);
        options.addOption(optPathNet1);
        options.addOption(optPathNet2);

        //parse input
        CommandLine line;
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
             line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            line = null;
            System.exit(1);
        }

        //create matching pipeline
        Pipeline.Builder builder = new Pipeline.Builder();

        // parse complexMatches
        if(line.hasOption("c")){
           builder = builder.withComplexMatches();
        }

        // parse similarityWeight
        if(line.hasOption("s")){
            String sString = line.getOptionValue("s");
            try{
                double s = Double.parseDouble(sString);
                builder = builder.atSimilarityWeight(s);
            }catch (NumberFormatException numExp) {
                System.out.println("Parsing Failed: Number Input s " + numExp.getMessage());
            }
        }

        // parse postprocessThreshold
        if(line.hasOption("p")){
            String pString = line.getOptionValue("p");
            try{
                double p = Double.parseDouble(pString);
                builder = builder.atPostprocessThreshold(p);
            }catch (NumberFormatException numExp) {
                System.out.println("Parsing Failed: Number Input p " + numExp.getMessage());
                System.exit(1);
            }
        }

        // parse petri net 1 and 2
        File net1;
        File net2;
        String n1String = line.getOptionValue("n1");
        String n2String = line.getOptionValue("n2");
        try{
            net1 = new File(n1String);
            if(!net1.exists()) {
                throw new FileNotFoundException("Net 1 file not found under" + n1String);
            }
            net2 = new File(n2String);
            if(!net2.exists()) {
                throw new FileNotFoundException("Net 2 file not found under" + n2String);
            }
        }catch (FileNotFoundException fileExp) {
            System.out.println("Parsing Failed: Petri Net File not found:" + fileExp.getMessage());
            System.exit(1);
            net1 = null;
            net2 = null;

        }

        //Build pipeline
        Pipeline pip = builder.Build();

        //run files
        pip.run(net1,net2);


    }
}
