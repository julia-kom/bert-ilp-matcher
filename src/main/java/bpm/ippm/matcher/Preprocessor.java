package bpm.ippm.matcher;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.similarity.Matrix;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XLog;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.Transition;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Preprocessor {

    /**
     * Prematches those pairs of transitions which have label similarity = 1 and
     * there is no other transition which got label similarity of one with one of the two prematched
     * @param net1 net system 1
     * @param net2 net system 2
     * @param sim simialrity matrix of net system 1 and net system 2
     * @return prematch alignment
     */
    public static Alignment prematch(Set<Transition> net1, Set<Transition> net2, Matrix sim){
        // Create a matching that contains all optimal label matches
        Alignment.Builder builder = new Alignment.Builder();
        for(Transition t1 : net1){
            for(Transition t2: net2){
                if(Math.abs(sim.between(t1,t2)-1.0) < 0.0001){
                   builder = builder.add(t1,t2);
                }
            }
        }

        // delete those which are not unique aka in a complex correspondence
        builder = builder.removeComplexMatches();
        return builder.build("prematch");
    }


    /**
     * Deletes the tau transitions from the set of transitions
     * @param transitions set of transitions
     * @return reduced set of transitions
     */
    public static Set<Transition> reduceTauTransitions(Set<Transition> transitions){
        // copy the set of transitions before deleting
        Set result = new HashSet(transitions);
        Iterator<Transition> i = result.iterator();
        while(i.hasNext()){
            Transition current = i.next();
            if (isTau(current)){
                i.remove();
            }
        }
        return result;
    }

    /**
     * Checks if transition is tau.
     * Tau if either: empty label, id == label, t_??, t??, tr_??, tr??, MESSAGE_??
     * @param t Transition
     * @return true if silent
     */
    public static boolean isTau(Transition t){
        // silent
        if(t.isSilent()){
            return true;
        }
        // id equals name
        if(t.getLabel().equals(t.getId())) {
            return true;
        }
        // name is t_??
        if(t.getLabel().matches("t_[0-9]+")){
            return true;
        }

        // name is t??
        if(t.getLabel().matches("t[0-9]+")){
            return true;
        }

        //if label is tr??
        if(t.getLabel().matches("tr[0-9]+")){
            return true;
        }

        //empty label
        if(t.getLabel().equals("")){
            return true;
        }

        // empty intermediate events (MESSAGE_??) are like tau transitions
        if(t.getLabel().equals("MESSAGE_") || t.getLabel().matches("MESSAGE_[0-9]+")){
            return true;
        }

        return false;
    }

    /**
     * Parses a PNML file to a NetSystem
     * @param f file path of the petri net in PNML format
     * @return NetSystem
     */
    public static NetSystem parseFile(File f){
        //PNMLSerializer serializer = new PNMLSerializer();
        //return serializer.parse(f.getAbsolutePath());
        Parser p = new Parser();
        try {
            return p.parse(f);
        }catch(Exception e){
            System.err.println("Exception while parsing "+ e.getStackTrace());
            return null;
        }
    }

    /**
     * Parses a XES file to a XLog file
     * @param f
     * @return XLog
     */
    public static XLog parseLog(File f){
        // no log file given
        if(f == null){
            return Pipeline.DUMMY_LOG;
        }

        XUniversalParser parser = new XUniversalParser();
        Collection<XLog> collection;
        try {
            collection = parser.parse(f);
        }catch(Exception e){
            throw new Error("File " + f.toString() + " is not possible to parse as XES" + e.getStackTrace());
        }

        // non or more than one log found in directory
        if(collection.size() != 1){
            throw new Error("Under path " +f.toString() + " " + collection.size() + "!= 1 Logs were found" );
        }
        // return only log of collection
        return collection.iterator().next();
    }

    /**
     * checks if petri net is WF-net, sound and free choice
     * throws illegal argument exception if not
     * @param net NetSystem
     */
    public static void checkPetriNetProperties(NetSystem net){
        // TODO soundness
        if (!PetriNet.STRUCTURAL_CHECKS.isWorkflowNet(net)){
            throw new IllegalArgumentException("net is not WF-net:" + net.toString());
        }
        if(!PetriNet.STRUCTURAL_CHECKS.isExtendedFreeChoice(net)){
            throw new IllegalArgumentException("net is not free choice:" + net.toString());
        }

        //TODO soundness incl 1 bounded.

    }

    /**
     * Returns those files in path which are of one of the types defined in the second parameter.
     * @param path Path to crawl
     * @param types Types to search for
     * @return List of files in that path with given type
     */
    public static File[] listFilesOfType(File path, final String[] types){
        File[] files =  path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                for(String t : types) {
                    if(name.toLowerCase().endsWith("."+t)){
                        return true;
                    }
                }
                return false;
            }
        });
        return files;
    }

    /**
     * Returns those files in path which are of one of the types defined in the second parameter.
     * @param path Path to crawl
     * @param type Type to search for
     * @return List of files in that path with given type
     */
    public static File[] listFilesOfType(File path, String type){
        String[] types = new String[]{type};
        return listFilesOfType(path,types);
    }
}
