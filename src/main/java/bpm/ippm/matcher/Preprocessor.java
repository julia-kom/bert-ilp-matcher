package bpm.ippm.matcher;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.similarity.Matrix;
import org.jbpt.petri.Transition;

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
     * @param t
     * @return
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

}
