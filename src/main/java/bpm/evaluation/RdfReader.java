package bpm.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import static bpm.ippm.matcher.Preprocessor.parseFile;

public class RdfReader {

    public static void rdfToCsv(String gs_path, String n1_path, String n2_path, FileWriter csvWriter)
            throws AlignmentException {

        File gold;
        File n1;
        File n2;

        try {
            gold = new File(gs_path);
            if (!gold.exists()) {
                throw new FileNotFoundException("Goldstandard file not found under: " + gs_path);
            }
            n1 = new File(n1_path);
            if (!n1.exists()) {
                throw new FileNotFoundException("Net 1 file not found under: " + n1_path);
            }
            n2 = new File(n2_path);
            if (!n2.exists()) {
                throw new FileNotFoundException("Net 2 file not found under: " + n2_path);
            }
        } catch (FileNotFoundException fileExp) {
            throw new Error("Parsing Failed: Petri Net File not found: " + fileExp.getMessage());
        }

        // File to NetSystem
        NetSystem net1 = parseFile(n1);
        NetSystem net2 = parseFile(n2);

        // create an alignment parser
        AlignmentParser aparser = new AlignmentParser(0);
        // load an alignment from a file (a reference alignment)
        org.semanticweb.owl.align.Alignment reference = aparser.parse(gold.toURI());

        // parse alignment and get labels
        for (Cell c : reference) {
            String[] correspondence = new String[3];
            Node node1 = new Node();
            String uri1 = c.getObject1().toString();
            node1.setId(uri1.substring(uri1.indexOf('#') + 1, uri1.length()));
            Node node2 = new Node();
            String uri2 = c.getObject2().toString();
            node2.setId(uri2.substring(uri2.indexOf('#') + 1, uri2.length()));

            // fetch actual label net1 (as this is not stored in the goldstandard)
            String labelNode1 = "";
            for (Node n : net1.getNodes()) {
                if (n.getId().equals(node1.getId())) {
                    labelNode1 = n.getLabel();
                    correspondence[0] = labelNode1;
                }
            }

            String labelNode2 = "";
            for (Node n : net2.getNodes()) {
                if (n.getId().equals(node2.getId())) {
                    labelNode2 = n.getLabel();
                    correspondence[1] = labelNode2;
                }
            }

            // value 1 indicates a match
            correspondence[2] = "1";

            // write[label1,label2,1] into csv
            try {
                System.out.println(correspondence[0] + " | " + correspondence[1] + " | " + correspondence[2]);
                csvWriter.append(correspondence[0] + "," + correspondence[1] + "," + correspondence[2] + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {

        // choose dataset
        String[] datasets = { "uni", "birth", "sap" };
        String[] pathnames;

        for (String dataset : datasets) {

            File f = new File("./eval-data/goldstandard/" + dataset);

            // create csv and file writer
            File csv = new File("./eval-data/csv/" + dataset + "-matches.csv");

            // Populates the array with names of files and directories
            pathnames = f.list();

            // open one file wrtiter for each dataset
            FileWriter csvWriter = null;
            try {
                csvWriter = new FileWriter(csv);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // run through whole dataset
            for (String rdf : pathnames) {
                rdf = rdf.substring(0, rdf.length() - 4);
                String nets[] = rdf.split("-");
                System.out.println(nets[0] + " | " + nets[1]);

                try {
                    rdfToCsv("./eval-data/goldstandard/" + dataset + "/" + rdf + ".rdf",
                            "./eval-data/pnml/" + dataset + "/" + nets[0] + ".pnml",
                            "./eval-data/pnml/" + dataset + "/" + nets[1] + ".pnml", csvWriter);

                } catch (AlignmentException e) {
                    e.printStackTrace();
                }
            }

            // close csv writer properly
            try {
                csvWriter.flush();
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}