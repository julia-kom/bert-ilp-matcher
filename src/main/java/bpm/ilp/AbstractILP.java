package bpm.ilp;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import bpm.similarity.Matrix;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import org.jbpt.bp.RelSet;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;

import java.io.File;
import java.util.Set;


public abstract class AbstractILP {
    GRBEnv env;
    GRBModel model;
    double  similarityWeight;

    /**
     * Different ILP implementations
     */
    public enum ILP{
        BASIC, //Basic 1:1 behavior and label matcher
        RELAXED, // 1:1 behavioral and label matcher, converted BASIC to an LP problem, where linking between x and y is split into two constraints => similarity score but slow LP
        RELAXED2 // 1:1 behavioral and label matcher, converted BASIC to an LP problem, just by making all variables contineous. => no similarity score but fast LP.
    }

    /**
     * Init the ILP with a log file path and a similarity weight
     * @param log
     * @param similarityWeight
     * @throws GRBException
     */
    public void init(File log, double similarityWeight) throws GRBException {
        // Set similarity weight
        this.similarityWeight = similarityWeight;

        // Create empty environment, set options, and start
        env = new GRBEnv(true);
        env.set("logFile", log.getAbsolutePath());
        env.start();

        // Create empty model
        model = new GRBModel(env);
    }

    // todo maybe replace sim with a function pointer
    public abstract Result solve(RelSet relNet1, RelSet relNet2, Set<Transition> net1, Set<Transition> net2, Matrix matrix, Alignment preAlignment, String name) throws GRBException ;


}
