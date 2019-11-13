package bpm.evaluation;

import bpm.matcher.Preprocessor;
import org.jbpt.bp.RelSet;
import org.jbpt.bp.RelSetType;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AggregatedNetAnalysis{
    FileWriter csvWriter;

    /**
     * Create an empty aggregated net analysis in the parameter file
     * @param file where to create the net information
     * @throws IOException
     */
    public AggregatedNetAnalysis(File file) throws IOException {
        csvWriter = new FileWriter(file.getAbsolutePath());
        csvWriter.append("Name, nSilentTransitions, nNonSilentTransitions,");
        // Number of relations
        for (RelSetType t : RelSetType.values()) {
            csvWriter.append("n" + t.toString() + ",");
        }
    }


    public void addNet(NetSystem net, RelSet profile) throws IOException {
        NetAnalysis analysis = new NetAnalysis(net,profile);

        // transition stats
        csvWriter.append(analysis.name.replace(',', ';').replace("\n", " ") + ",").
                append(analysis.nSilentTransitions + ",").append(analysis.nNonSilentTransitions + ",").append(analysis.nNonSilentTransitions + ",");

        // Profile Stats
        for (RelSetType t : RelSetType.values()) {
            HashSet<RelSetType> type = new HashSet<RelSetType>();
            type.add(t);
            csvWriter.append(analysis.profile.getRelationsByType(type).size()+ ",");
        }
    }

    public void toCSV() throws IOException {
        //properly close everything
        csvWriter.flush();
        csvWriter.close();
    }


    private class NetAnalysis{
        String name;
        int nSilentTransitions = 0;
        int nNonSilentTransitions = 0;
        RelSet profile;

        private NetAnalysis(NetSystem net, RelSet profile) {
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
