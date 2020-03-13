package bpm.ippm.ilp;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.alignment.Result;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.similarity.Matrix;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import org.apache.commons.lang3.NotImplementedException;
import org.jbpt.petri.Transition;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static bpm.ippm.matcher.Pipeline.PRINT_ENABLED;

/**
 * Abstract ILP class. Here all solver specific parameters are set.
 */
public abstract class AbstractILP {
    GRBEnv env;
    GRBModel model;
    double  similarityWeight;

    /**
     * Returns the ILP Matcher instance for the given ILP Enum type
     * @param ilp
     * @return
     * @throws NotImplementedException
     */
    public static AbstractILP getILP(ILP ilp) throws NotImplementedException {
        switch(ilp) {
            case BASIC:
                return new BasicILP();
            case SYMMETRIC:
                return new BasicILP2();
            case BASIC3:
                System.err.println("You are about to use a deprecated ILP. Please use BASIC, SYMMETRIC or CUSTOM_IDENTIFICATION");
                return new BasicILP3();
            case BASIC4:
                System.err.println("You are about to use a deprecated ILP. Please use BASIC, SYMMETRIC or CUSTOM_IDENTIFICATION");
                return new BasicILP4();
            case CUSTOM_IDENTIFICATION:
                return new BasicILP5();
            case RELAXED:
                System.err.println("You are about to use a deprecated ILP. Please use BASIC, SYMMETRIC or CUSTOM_IDENTIFICATION");
                return  new RelaxedILP();
            case RELAXED2:
                System.err.println("You are about to use a deprecated ILP. Please use BASIC, SYMMETRIC or CUSTOM_IDENTIFICATION");
                return new RelaxedILP2();
            case RELAXED3:
                System.err.println("You are about to use a deprecated ILP. Please use BASIC, SYMMETRIC or CUSTOM_IDENTIFICATION");
                return new RelaxedILP3();
            case RELAXED4:
                System.err.println("You are about to use a deprecated ILP. Please use BASIC, SYMMETRIC or CUSTOM_IDENTIFICATION");
                return new RelaxedILP4();
            case QUADRATIC:
                System.err.println("You are about to use a deprecated ILP. Please use BASIC, SYMMETRIC or CUSTOM_IDENTIFICATION");
                return new QuadraticILP();
            default:
                throw new NotImplementedException("ILP you searched for is not in switch");
        }

    }

    /**
     * Different ILP implementations
     */
    public enum ILP{
        BASIC, //Basic 1:1 behavior and label matcher with binary identification function and no variable reduction
        SYMMETRIC, //Basic 1:1 behavior and label matcher  with binary identification function and reduced number of variables
        BASIC3, // Basic 1:1 behavior and label matcher, with removed X variable. DEPRECATED.
        BASIC4, // Basic 1:1 behavior and label matcher, create Y variables for those pairs of relations only, which are equal. This is the minimum possible number of y variables. DEPRECATED.
        CUSTOM_IDENTIFICATION, // As BASIC 2 but pull the identification function into the target and use non binary system for relational similarity.
        RELAXED, // WRONG 1:1 behavioral and label matcher, converted BASIC to an LP problem, where linking between x and y is split into two constraints => similarity score but slow LP
        RELAXED2, // WRONG 1:1 behavioral and label matcher, converted BASIC to an LP problem, just by making all variables contineous. => no similarity score but fast LP.
        RELAXED3, // WRONG 1:1 behavioral and label matcher, converted BASIC to an LP problem, where linking between x and y is split into two constraints => similarity score but slow LP, now use of symmetry of the matrix
        RELAXED4, // WRONG 1:1 behavioral and label matcher, converted BASIC to an LP problem, where linking between x and y is split into two constraints => similarity score but slow LP addionally we constraint the y values per match.
        QUADRATIC //Quadratic formulation of the ILP DEPRECATED
    }

    public void init(File log, double similarityWeight) throws GRBException, IOException {
        init(log,similarityWeight,GRB.INFINITY, GRB.INFINITY);
    }

    /**
     * Init the ILP with a log file path and a similarity weight
     * @param log gurobi log file path
     * @param similarityWeight simialrity weight
     * @throws GRBException
     */
    public void init(File log, double similarityWeight, double timeLimit, double nodeLimit) throws GRBException, IOException {
        // Set similarity weight
        this.similarityWeight = similarityWeight;

        // Create empty environment, set options, and start
        env = new GRBEnv(true);
        //mk dir and create the file
        log.getParentFile().mkdirs();
        log.createNewFile();
        env.set("logFile", log.getAbsolutePath());

        // Disable logging
        if(!PRINT_ENABLED) env.set(GRB.IntParam.OutputFlag, 0);

        env.start();

        // Create empty model
        model = new GRBModel(env);

        //set time limit. When solving MIP this does still result in an alignment
        model.set(GRB.DoubleParam.TimeLimit, timeLimit);

        // set node limit (for MIP only). Number of nodes to be traversed
        model.set(GRB.DoubleParam.NodeLimit, nodeLimit);

        /*
        The Threads parameter controls the number of threads used by the parallel MIP solver to solve the model.
        The default is to use all cores in the machine. If you wish to leave some available for other activities,
        adjust this parameter accordingly. (c) Gurobi
         */
        //model.set(GRB.IntParam.Threads, 11);

        /*
        The MIPFocus parameter allows you to modify your high-level solution strategy, depending on your goals.
        By default, the Gurobi MIP solver strikes a balance between finding new feasible solutions and proving that
        the current solution is optimal. If you are more interested in good quality feasible solutions, you can select
        MIPFocus=1. If you believe the solver is having no trouble finding the optimal solution,
        and wish to focus more attention on proving optimality, select MIPFocus=2.
        If the best objective bound is moving very slowly (or not at all), you may want to try MIPFocus=3 to
        focus on the bound. (c) Gurobi
         */
        model.set(GRB.IntParam.MIPFocus, 3);

        /*
        The MIP solver can change parameter settings in the middle of the search in order to adopt a strategy that
        gives up on moving the best bound and instead devotes all of its effort towards finding better feasible
        solutions. This parameter allows you to specify the time when the MIP solver switches to a solution
        improvement strategy. For example, setting this parameter to 10 will cause the MIP solver to switch strategies
        10 seconds after starting the optimization.
         */
        model.set(GRB.DoubleParam.ImproveStartTime, GRB.INFINITY);

        /*
        The MIP solver can change parameter settings in the middle of the search in order to adopt a strategy
        that gives up on moving the best bound and instead devotes all of its effort towards finding better feasible
        solutions. This parameter allows you to specify an optimality gap at which the MIP solver switches to
        a solution improvement strategy. For example, setting this parameter to 0.1 will cause the MIP solver to
        switch strategies once the relative optimality gap is smaller than 0.1. (c) Gurobi
         */
        model.set(GRB.DoubleParam.ImproveStartGap,0.0);

        /*
        The Gurobi MIP solver employs a wide range of cutting plane strategies. The aggressiveness of these strategies
        can be controlled at a coarse level through the Cuts parameter, and at a finer grain through a further set of
        cuts parameters (e.g., FlowCoverCuts, MIRCuts, etc.). Each cut parameter can be set to Aggressive (2),
        Conservative (1), Automatic (-1), or None (0). The more specific parameters override the more general,
        so for example setting MIRCuts to None (0) while also setting Cuts to Aggressive (2) would aggressively
        generate all cut types, except MIR cuts which would not be generated. Very easy models can sometimes benefit
        from turning cuts off, while extremely difficult models can benefit from turning them to their Aggressive setting. (c) Gurobi
         */
        model.set(GRB.IntParam.Cuts, -1);

        /*
        Controls the presolve level. A value of -1 corresponds to an automatic setting. Other options are off (0),
        conservative (1), or aggressive (2). More aggressive application of presolve takes more time, but can sometimes
        lead to a significantly tighter model. (c) Gurobi
         */
        model.set(GRB.IntParam.Presolve,-1);

        /*
        The PreSparsify parameter enables an algorithm that can sometimes significantly reduce the number of nonzero
        values in the constraint matrix.
         */
        model.set(GRB.IntParam.PreSparsify,-1);

    }

    public abstract Result solve(AbstractProfile relNet1, AbstractProfile relNet2, Set<Transition> net1, Set<Transition> net2, Matrix matrix, Alignment preAlignment, String name) throws GRBException ;


}
