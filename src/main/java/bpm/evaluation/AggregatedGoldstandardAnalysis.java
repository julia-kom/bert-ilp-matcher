package bpm.evaluation;

import bpm.alignment.Alignment;
import bpm.alignment.Correspondence;
import org.jbpt.petri.NetSystem;
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
        csvWriter.append("Nets, correspondences, simple, complex, trivial");
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
    public void addGoldstandard(Alignment alignment, NetSystem net1, NetSystem net2) throws IOException {
        AggregatedGoldstandardAnalysis.GoldstandardAnalysis analysis = new AggregatedGoldstandardAnalysis.GoldstandardAnalysis(alignment, net1, net2);

        // transition stats
        csvWriter.append(analysis.name.replace(',', ';').replace("\n", " ") + ",").
                append(analysis.correspondences + ",").append(analysis.simple+ ",").append(analysis.complex + ",").append(analysis.trivial + "\n");
    }

    public int getTrivialCorrespondences(Alignment alignment, NetSystem n1, NetSystem n2){
        int count = 0;
        for(Correspondence c : alignment.getCorrespondences()){

            //check if direct or indirect complex correspondece
            if(c.isComplexCorrespondence()) {
                continue;
            }
            Node node1 = c.getNet1Nodes().iterator().next();
            Node node2 = c.getNet2Nodes().iterator().next();
            if(alignment.getCorrespondenceOfNode(node1).size() != 1 || alignment.getCorrespondenceOfNode(node2).size() != 1){
                continue;
            }

            //fetch actual label net1 (as this is not stored in the goldstandard)
            String labelNode1 = "";
            boolean found = false;
            for (Node n : n1.getNodes()){
                if(n.getId().equals(node1.getId())){
                    labelNode1 = n.getLabel();
                    found = true;
                }
            }

            if(!found){
                System.err.println("Id not found in model: "+ n1.getTransitions() + node1.getId());
            }

            found = false;
            //fetch actual label net2 (as this is not stored in the goldstandard)
            String labelNode2 = "";
            for (Node n : n2.getNodes()){
                if(n.getId().equals(node2.getId())){
                    labelNode2 = n.getLabel();
                    found = true;
                }
            }

             if(!found){
                System.err.println("Id not found in model: "+ n2.getTransitions() + node2.getId());
            }

            //test if labels are equal
            if(!labelNode1.equals(labelNode2)){
                System.out.println(alignment.getName()+ ": " +labelNode1 + " --- " +labelNode2);
                continue;
            }

            //test if label is used for several transitions
            int tmp = 0;
            for(Node n : n1.getNodes()){
                if(n.getLabel().equals(labelNode1)){
                    tmp++;
                }
            }
            if(tmp > 1){
                System.out.println(alignment.getName()+ " is not unique labeled: " +labelNode1  );
                continue;
            }

            tmp = 0;
            for(Node n : n2.getNodes()){
                if(n.getLabel().equals(labelNode2)){
                    tmp++;
                }
            }
            if(tmp > 1){
                System.out.println(alignment.getName()+ " is not unique labeled: " +labelNode2  );
                continue;
            }

            //if nothing failed before. Congrats it is a trivial correspondence.
            count++;

        }
        return count;
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
        int trivial = 0;

        private GoldstandardAnalysis(Alignment alignment, NetSystem net1, NetSystem net2) {
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


            trivial = getTrivialCorrespondences(alignment,net1, net2);
        }
    }


}