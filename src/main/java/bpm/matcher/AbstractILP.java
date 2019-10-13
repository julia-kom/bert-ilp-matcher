package bpm.matcher;

import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;
import org.jbpt.bp.RelSet;
import org.jbpt.petri.NetSystem;

import java.io.File;


public abstract class AbstractILP {
    GRBEnv env;
    GRBModel model;
    double  similarityWeight;
    LabelSimilarity sim;

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
    void init(File log, double similarityWeight) throws GRBException {
        // Set similarity weight
        this.similarityWeight = similarityWeight;
        this.sim = new LabelSimilarity();

        // Create empty environment, set options, and start
        env = new GRBEnv(true);
        env.set("logFile", log.getAbsolutePath());
        env.start();

        // Create empty model
        model = new GRBModel(env);
    }

    // todo maybe replace sim with a function pointer
    protected abstract AbstractILP.Result solve(RelSet relNet1, RelSet relNet2, NetSystem net1, NetSystem net2) throws GRBException ;

    public class Result{
        double similarity;

        //todo add alignment

        public Result(double objective, GRBVar[][] x){
            similarity = objective;
            //todo compute alignment
        }

    }


}
