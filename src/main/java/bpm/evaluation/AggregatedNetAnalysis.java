package bpm.evaluation;

import bpm.ippm.matcher.Preprocessor;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.profile.Relation;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Aggregation of several single Net Analysis
 */
public class AggregatedNetAnalysis{
    FileWriter csvWriter;
    Set<NetAnalysis> netAnalyses = new HashSet<>();

    /**
     * Create an empty aggregated net analysis in the parameter file
     * @param file where to create the net information
     * @throws IOException
     */
    public AggregatedNetAnalysis(File file) throws IOException {
        csvWriter = new FileWriter(file.getAbsolutePath());
        csvWriter.append("Name, nSilentTransitions, nNonSilentTransitions,");
        // Number of relations
        for (Relation.RelationType t : Relation.RelationType.values()) {
            csvWriter.append("n" + t.toString() + ",");
        }
        csvWriter.append("\n");
    }


    /**
     * Add a net's stats to the csv file (wrt the profile given as parameter)
     * @param net
     * @param profile
     * @throws IOException
     */
    public void addNet(NetSystem net, AbstractProfile profile) throws IOException {
        NetAnalysis analysis = new NetAnalysis(net,profile);
        netAnalyses.add(analysis);
        // transition stats
        csvWriter.append(analysis.name.replace(',', ';').replace("\n", " ") + ",").
                append(analysis.nSilentTransitions + ",").append(analysis.nNonSilentTransitions + ",");
        // Profile Stats
        for (Relation.RelationType t : Relation.RelationType.values()) {
            csvWriter.append(getNumberOfRelations(t, net, profile)+ ",");
        }
        csvWriter.append("\n");
    }

    /**
     * Get the number of times two transitions of net are in a relation rel inside the profile.
     * @param rel
     * @param net
     * @param profile
     * @return
     */
    private int getNumberOfRelations(Relation.RelationType rel, NetSystem net, AbstractProfile profile){
        int n = 0;
        for (Transition t1 :net.getTransitions()){
            for(Transition t2 : net.getTransitions()){
                Relation r = profile.getRelationForEntities(t1,t2);
                if(r.getType().equals(rel)){
                   n++;
                }
            }
        }
        return n;
    }

    /**
     * Complete the CSV
     * @throws IOException
     */
    public void toCSV() throws IOException {
        //compute averages
        this.addAverage();
        //properly close everything
        csvWriter.flush();
        csvWriter.close();
    }

    private void addAverage() throws IOException {
        int sumSilentTransitions = 0;
        int sumNonSilentTransitions = 0;
        for(NetAnalysis netAnalysis : netAnalyses){
            sumNonSilentTransitions += netAnalysis.nNonSilentTransitions;
            sumSilentTransitions += netAnalysis.nSilentTransitions;
        }
        double avgNonSilentTransitions = (1.0 * sumNonSilentTransitions) / netAnalyses.size();
        double avgSilentTransitions = (1.0 * sumSilentTransitions) / netAnalyses.size();

        csvWriter.append("AVERAGES:,").append(avgSilentTransitions + ",").append(avgNonSilentTransitions + ",");


    }


    private class NetAnalysis{
        String name;
        int nSilentTransitions = 0;
        int nNonSilentTransitions = 0;
        AbstractProfile profile;

        private NetAnalysis(NetSystem net, AbstractProfile profile) {
            // Analyze Transitions
            Set<Transition> transitions = net.getTransitions();
            Iterator<Transition> iterator = transitions.iterator();
            while(iterator.hasNext()) {
                Transition t = iterator.next();
                if (Preprocessor.isTau(t)) {
                    nSilentTransitions += 1;
                } else {
                    nNonSilentTransitions += 1;
                }
            }
            //name and profile
            this.name = net.getName();
            this.profile = profile;
        }
    }

}
