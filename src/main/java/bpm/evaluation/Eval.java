package bpm.evaluation;

import bpm.alignment.Alignment;
import bpm.alignment.Correspondence;
import org.apache.commons.lang3.NotImplementedException;
import org.jbpt.petri.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Eval {

    public enum Strategies{
        BINARY,
        STRICT_BINARY,
        PROBABILISTICALLY
    }

    // File paths of the compared nets
    private File net1;
    private File net2;

    private String name;

    //Confusion Matrix
    private int tp = 0;
    private int fn = 0;
    private int fp = 0;

    //Scores
    private double precision = 0;
    private double recall = 0;
    private double fscore = 0;

    // Detailed Information
    private Set<Correspondence> tpCorrespondeces = new HashSet<>();
    private Set<Correspondence> fpCorrespondeces = new HashSet<>();
    private Set<Correspondence> fnCorrespondeces = new HashSet<>();

    private Eval(){

    }

    /**
     * Get TP
     * @return tp
     */
    public int getTP(){
        return tp;
    }

    /**
     * Get FP
     * @return fp
     */
    public int getFP() {
        return fp;
    }

    /**
     * Get TN
     * @return tn
     */
    public int getFN() {
        return fn;
    }

    /**
     * Get Precision
     * @return precision
     */
    public double getPrecision() {
        return precision;
    }

    /**
     * Get fscore
     * @return F Score
     */
    public double getFscore() {
        return fscore;
    }

    /**
     * Get Recall
     * @return recall
     */
    public double getRecall() {
        return recall;
    }

    public String getName(){ return name;}

    /**
     * Evalaution to String
     * @return evaluation
     */
    @Override
    public String toString() {
        return "##" + this.name + "## \n" +
                "TP: " +tp + "\n" +
                "FP: " +fp + "\n" +
                "FP: " +fn + "\n" +
                "PRECISION: " + precision + "\n" +
                "RECALL: " + recall + "\n" +
                "FSCORE: " + fscore + "\n";
    }

    /**
     * Converts to CSV. First row contains all statisitcs
     * Rows with name CORRESPONDENCE contain all correspondences according to their confusion class
     * @param file file where to write
     * @throws IOException
     */
    public void toCSV(File file) throws IOException {
        FileWriter csvWriter = new FileWriter(file.getAbsolutePath());
        // column description
        csvWriter.append("Name,").append("TP,").append("FP,").append("FN,").append("PRECISION,").append("RECALL,").append("FSCORE\n");
        // stats
        csvWriter.append(this.getName().replace(',',';').replace("\n"," ")+",")
                 .append(this.getTP()+",").append(this.getFP()+",").append(this.getFN()+",")
                 .append(this.getPrecision()+",").append(this.getRecall()+",").append(this.getFscore()+"\n");

        // tp,fp,fn correspondences:
        Iterator<Correspondence> tpIt = tpCorrespondeces.iterator();
        Iterator<Correspondence> fnIt = fnCorrespondeces.iterator();
        Iterator<Correspondence> fpIt = fpCorrespondeces.iterator();
        String corTP;
        String corFN;
        String corFP;
        for(int i = 0; i < Math.min(tp,Math.min(fp,fn));i++){
            corFN = "";
            corFP = "";
            corTP = "";
            if(tpIt.hasNext()){
                corTP = tpIt.next().toString();
            }
            if(fpIt.hasNext()){
                corFP = fpIt.next().toString();
            }
            if(fnIt.hasNext()){
                corFN = fnIt.next().toString();
            }
            csvWriter.append("CORRESPONDENCES,").append(corTP+",").append(corFP+",").append(corFN+",").append(",,,\n");
        }
    }


    /**
     * Based on the detailed confusion matrix entries tpCorrespondeces, fpCorrespondeces, tnCorrespondeces, fnCorrespondeces
     * all binary matrics are computed.
     */
    private void computeBinaryStats(){
        // Confusion computation
        // todo maybe catch zero cases
        this.tp = this.tpCorrespondeces.size();
        this.fn = this.fnCorrespondeces.size();
        this.fp = this.fpCorrespondeces.size();

        // Scores
        this.precision = Metrics.precision(this.tp, this.fp); // 1.0 * this.tp /(this.tp + this.fp);
        this.recall = Metrics.recall(this.tp, this.fn); //1.0 * this.tp /(this.tp + this.fn);
        this.fscore = Metrics.fscore(this.tp, this.fp, this.fn); // 2.0* this.precision*this.recall/(this.precision + this.recall);
    }

    public static class Builder {
        /**
         * Compute Binary Evaluation.
         * If (a;b,c) was detected but only (a,b) is in the gold standard this accounts here: (a;b) a TP and (a;c) a FP.
         * If (a;b) was detected but (a;b,c) is in gold standard, this is (a;b) a TP and (a;c) a FN.
         * @param matcher      alignment computed by the matcher
         * @param goldstandard alignment acc. to the gold standard
         * @return Eval
         */
        public static Eval BinaryEvaluation(Alignment matcher, Alignment goldstandard) {
            Eval res = new Eval();
            res.name = matcher.getName();

            // fill the tpCorrespondeces, fpCorrespondeces,fnCorrespondeces
            //TP and FP Correspondences
            for (Correspondence m : matcher.getCorrespondences()) {
                for (Node n1 : m.getNet1Nodes()) {
                    for (Node n2 : m.getNet2Nodes()) {
                        if (goldstandard.isMapped(n1, n2)) {
                            // mapped in goldstandard and in match
                            res.tpCorrespondeces.add(new Correspondence.Builder().addNodeFromNet1(n1).addNodeFromNet2(n2).build());
                        } else {
                            // mapped in match but not in gold standard
                            res.fpCorrespondeces.add(new Correspondence.Builder().addNodeFromNet1(n1).addNodeFromNet2(n2).build());
                        }
                    }
                }
            }

            //FN Correspondences
            for (Correspondence g : goldstandard.getCorrespondences()) {
                //Negatives
                for(Node n1 : g.getNet1Nodes()){
                    for(Node n2 : g.getNet2Nodes()){
                        if(!matcher.isMapped(n1,n2)){
                            // mapped in goldstandard but not in match
                            res.fnCorrespondeces.add(new Correspondence.Builder().addNodeFromNet1(n1).addNodeFromNet2(n2).build());
                        }
                    }
                }
            }

            //compute metrics based on tpCorrespondeces, fpCorrespondeces, fnCorrespondeces
            res.computeBinaryStats();
            return res;
        }

        /**
         * Compute Binary Evaluation.
         * Only EXACT matches count as TP or TN.
         * @param matcher      alignment computed by the matcher
         * @param goldstandard alignment acc. to the gold standard
         * @return Eval
         */
        public static Eval StrictBinaryEvaluation(Alignment matcher, Alignment goldstandard) {
            Eval res = new Eval();
            res.name = matcher.getName();

            //Compute metrics based on tpCorrespondeces, fpCorrespondeces, tnCorrespondeces, fnCorrespondeces
            //TP and FP
            for(Correspondence m : matcher.getCorrespondences()){
                if(goldstandard.contains(m)){
                    res.tpCorrespondeces.add(m);
                }else{
                    res.fpCorrespondeces.add(m);
                }
            }

            //FN
            for(Correspondence g : goldstandard.getCorrespondences()){
                if(!matcher.contains(g)){
                    res.fnCorrespondeces.add(g);
                }
            }
            res.computeBinaryStats();
            return res;
        }

        /**
         * Compute Probabilistic Evaluation
         * As defined in "Probabilistic Evaluation of Process Model Matching Techniques" - Kuss
         *
         * @param matcher      alignment computed by the matcher
         * @param goldstandard alignment acc. to the gold standard
         * @return Eval
         */
        public static Eval ProbabilisticEvaluation(Alignment matcher, Alignment goldstandard) {
            //todo
            throw new NotImplementedException("Probabilistic Evaluation is not yet implemented");
        }
    }
}