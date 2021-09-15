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

    /**
     * @param file
     * @return
     * @throws AlignmentException
     */
    public static void rdfToCsv(File gold, File pnml1, File pnml2, File csv) throws AlignmentException {

        // File to NetSystem

        NetSystem net1 = parseFile(pnml1);
        NetSystem net2 = parseFile(pnml2);

        // create an alignment parser
        AlignmentParser aparser = new AlignmentParser(0);
        // load an alignment from a file (a reference alignment)
        org.semanticweb.owl.align.Alignment reference = aparser.parse(gold.toURI());

        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Cell c : reference) {
            String[] correspondence = new String[3];
            Node n1 = new Node();
            String uri1 = c.getObject1().toString();
            n1.setId(uri1.substring(uri1.indexOf('#') + 1, uri1.length()));
            Node n2 = new Node();
            String uri2 = c.getObject2().toString();
            n2.setId(uri2.substring(uri2.indexOf('#') + 1, uri2.length()));

            // fetch actual label net1 (as this is not stored in the goldstandard)
            String labelNode1 = "";
            for (Node n : net1.getNodes()) {
                if (n.getId().equals(n1.getId())) {
                    labelNode1 = n.getLabel();
                    correspondence[0] = labelNode1;
                }
            }

            String labelNode2 = "";
            for (Node n : net2.getNodes()) {
                if (n.getId().equals(n2.getId())) {
                    labelNode2 = n.getLabel();
                    correspondence[1] = labelNode2;
                }
            }

            correspondence[2] = "1";

            try {
                System.out.println(correspondence[0] + " | " + correspondence[1] + " | " + correspondence[2]);
                csvWriter.append(correspondence[0] + "," + correspondence[1] + "," + correspondence[2] + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            try {
                csvWriter.flush();
         
        } cat
            e.printStackTrace(
            
        
            
        
    public static void main(String args[]) {

        if (args.length < 1) {
            throw new Error("Please Select a mode: eval or matcher");
        }

        // parse petri net 1 and 2
        File gold;
        File net1;
        File net2;

        // write to csv
        File csv = new File("./eval-data/csv/test.csv");

        String gsString = "./eval-data/goldstandard/" + args[0];
        if (gsString == null) {
            throw new Error("Net 1 is a required parameter");
        }
        String n1String = "./eval-data/pnml/" + args[1];
        if (n1String == null) {
            throw new Error("Net 1 is a required parameter");
        }

        String n2String = "./eval-data/pnml/" + args[2];
        if (n2String == null) {
            throw new Error("Net 2 is a required parameter");
        }

        try {
            gold = new File(gsString);
            if (!gold.exists()) {
                throw new FileNotFoundException("Net 1 file not found under" + n1String);
            }
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

        try {
            rdfToCsv(gold, net1, net2, csv);
        } catch (AlignmentException e) {
            e.printStackTrace();
        }

    }
}
