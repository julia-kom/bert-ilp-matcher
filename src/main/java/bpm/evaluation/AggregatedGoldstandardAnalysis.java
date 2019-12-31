package bpm.evaluation;

import bpm.alignment.Alignment;
import bpm.alignment.Correspondence;
import org.jbpt.petri.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class AggregatedGoldstandardAnalysis {
    FileWriter csvWriter;

    /**
     * Create an empty aggregated net analysis in the parameter file
     * @param file where to create the net information
     * @throws IOException
     */
    public AggregatedGoldstandardAnalysis(File file) throws IOException {
        csvWriter = new FileWriter(file.getAbsolutePath());
        csvWriter.append("Nets, correspondences, simple, complex");
        csvWriter.append("\n");
    }

    /**
     * Add a goldstandards stats to the csv file
     * correspondences = # correspondences (complex correspondences count #inner relations times (in case of 1:n => n times)
     * simple correspondences # 1:1 relations
     * complex correspondences
     * BTW correspondences = simple + complex
     *
     * @param alignment
     * @throws IOException
     */
    public void addGoldstandard(Alignment alignment) throws IOException {
        AggregatedGoldstandardAnalysis.GoldstandardAnalysis analysis = new AggregatedGoldstandardAnalysis.GoldstandardAnalysis(alignment);

        // transition stats
        csvWriter.append(analysis.name.replace(',', ';').replace("\n", " ") + ",").
                append(analysis.correspondences + ",").append(analysis.simple+ ",").append(analysis.complex + "\n");
    }

    /**
     * Complete the CSV
     * @throws IOException
     */
    public void toCSV() throws IOException {
        //properly close everything
        csvWriter.flush();
        csvWriter.close();
    }

    private class GoldstandardAnalysis{
        String name;
        int correspondences;
        int simple = 0;
        int complex = 0;

        private GoldstandardAnalysis(Alignment alignment) {
            name = alignment.getName();
            correspondences = alignment.getCorrespondences().size();

            for(Correspondence c : alignment.getCorrespondences()){
                //complex correspondece (should be irrelevant)
                if(c.isComplexCorrespondence()){
                    complex++;
                    continue;
                }

                //indirect complex correspondence
                Set<Node> nodes1 = c.getNet1Nodes();
                Set<Node> nodes2 = c.getNet2Nodes();
                boolean isComplex = false;
                for(Node n : nodes1) {
                    if (alignment.getCorrespondenceOfNode(n).size() > 1){
                        isComplex = true;
                    }
                }

                for(Node n : nodes2) {
                    if (alignment.getCorrespondenceOfNode(n).size() > 1){
                        isComplex = true;
                    }
                }

                //adapt counters
                if(isComplex){
                    complex++;
                }else {
                    simple++;
                }
            }
        }
    }
}