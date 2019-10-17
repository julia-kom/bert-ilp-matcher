package bpm.evaluation;

import bpm.alignment.Alignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import bpm.alignment.Correspondence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import org.jbpt.petri.Node;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

/**
 * Use an adapter to read files from the PMMC'15 challenge
 */
public class RdfAlignmentReader implements Reader{

    @Override
    public Alignment readAlignmentFrom(File file) {
        return null;
    }

    @Override
    public void writeAlignmentTo(File file, Alignment alignment, String model1, String model2) throws AlignmentException, FileNotFoundException {
        // Init Process Alignment
        ProcessAlignment ali = new ProcessAlignment("http://"+model1, "http://"+model2);

        // Generate Correspondences
        for(Correspondence c : alignment.getCorrespondences()){
            for(Node n1 : c.getNet1Nodes()){
                for(Node n2 : c.getNet2Nodes()){
                    ali.addAlignCell(n1.getId(),n2.getId(),"=", 1.0);
                }
            }
        }

        //Store Alignment
        PrintWriter writer = new PrintWriter(file.getAbsolutePath());
        AlignmentVisitor renderer = new RDFRendererVisitor(writer);
        ali.render(renderer);
        writer.flush();
        writer.close();
    }


    /**
     * Source of this class is the framework for PMMC'15: https://ai.wu.ac.at/emisa2015/contest.php
     */
    private class ProcessAlignment extends URIAlignment {

        private String nsModel1;
        private String nsModel2;

        public ProcessAlignment(String nsModel1, String nsModel2) {
            this.nsModel1 = nsModel1;
            this.nsModel2 = nsModel2;
        }

        /**
         * Warning: Use this constructor not for generating testcase specific alignment. It is recommended to use it for
         * aggregated alignments that are computed for evaluation purpose. The unspecified namespaces are then never used.
         */
        public ProcessAlignment() {
            this.nsModel1 = "http://undefined1";
            this.nsModel2 = "http://undefined2";
        }


        /**
         * Generates a cell (= correspondence or mapping) in this alignment.
         *
         * @param localId1 The local id of the activity in the first process model.
         * @param localId2 The local id of the activity in the second process model.
         * @param relation The generated relation, usually = is used.
         * @param confidence The confidence score should be a value between 0.0 and 1.0.
         * @throws AlignmentException
         */
        public void addAlignCell(String localId1, String localId2, String relation, double confidence) throws AlignmentException {
            URI u1 = null;
            URI u2 = null;
            try {
                u1 = new URI(nsModel1 + "#" + localId1);
            }
            catch (URISyntaxException e) {
                throw new AlignmentException("The specified string '" + this.nsModel1 + "#" + localId1 + "' is not a valid URI");
            }
            try {
                u2 = new URI(nsModel2 + "#" + localId2);
            }
            catch (URISyntaxException e) {
                throw new AlignmentException("The specified string '" + this.nsModel2 + "#" + localId2 + "' is not a valid URI");
            }
            super.addAlignCell(u1, u2, relation, confidence);
        }

        /**
         * Generates a cell (= correspondence or mapping) in this alignment.
         * The relation is set to = and the confidence is set to 1.0.
         *
         *
         * @param localName1 The local id of the activity in the first process model.
         * @param localName2 The local id of the activity in the second process model.
         * @throws AlignmentException
         */
        public void addAlignCell(String localName1, String localName2) throws AlignmentException {
            this.addAlignCell(localName1, localName2, "=", 1.0);
        }
    }
}
