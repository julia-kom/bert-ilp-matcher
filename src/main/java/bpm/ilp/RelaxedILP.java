package bpm.ilp;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import bpm.similarity.Matrix;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import org.jbpt.bp.RelSet;
import org.jbpt.bp.RelSetType;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;


public class RelaxedILP extends AbstractILP {
    public RelaxedILP(){

    }

    /**
     * Compute the Relaxed 1:1 *LP* behavior/label simialrity match.
     * Variables are contineous and the linking between x and y is split into two functions
     * Hint on Objective function is used.
     * This ILP produces a valid matching and similarity score
     * @param relNet1 Profile of Net 1
     * @param relNet2 Profile of Net 2
     * @param net1 Net 1
     * @param net2 Net 2
     * @return
     * @throws GRBException
     */
    @Override
    public Result solve(RelSet relNet1, RelSet relNet2, NetSystem net1, NetSystem net2, Matrix matrix) throws GRBException {
        //setup variables
        Node[] nodeNet1 =  net1.getTransitions().toArray(new Node[net1.getTransitions().size()]);
        Node[] nodeNet2 =  net2.getTransitions().toArray(new Node[net2.getTransitions().size()]);
        int nodesNet1 = nodeNet1.length;
        int nodesNet2 = nodeNet2.length;
        int minSize = Math.min(nodesNet1,nodesNet2);



        model.set(GRB.IntParam.Crossover,0);
        //model.set(GRB.DoubleParam.TimeLimit,60);

        GRBVar[][] x = new GRBVar[nodesNet1][nodesNet2];
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++){
                x[i][j] = model.addVar(0.0, 1.0,0.0, GRB.CONTINUOUS, "x_"+i+"_"+j);
            }
        }
        
        GRBVar[][][][] y = new GRBVar[nodesNet1][nodesNet1][nodesNet2][nodesNet2];
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k< nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        y[i][k][j][l] = model.addVar(0.0, 1.0, 0.0, GRB.CONTINUOUS, "y_" + i + "_" + k+"_"+j+"_"+l);
                    }
                }
            }
        }

        GRBVar sum = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "sum_y");
        GRBVar sum_x = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "sum_x");

        // Objective weighted between behavioral correspondance and label similarity
        // Behavioral part
        GRBLinExpr behavior = new GRBLinExpr();
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k< nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        behavior.addTerm(1.0/(minSize*minSize), y[i][k][j][l]);
                    }
                }
            }
        }

        // Label Similarity Part
        GRBLinExpr label = new GRBLinExpr();
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++){
                label.addTerm(matrix.between(nodeNet1[i],nodeNet2[j])/(minSize), x[i][j]);
            }
        }
        GRBLinExpr obj = new GRBLinExpr();
        obj.multAdd(this.similarityWeight, behavior);
        obj.multAdd(1-this.similarityWeight, label);

        model.setObjective(obj, GRB.MAXIMIZE);

        //setup model

        // hint expressions
        model.addConstr(obj,GRB.LESS_EQUAL,1.0, "objective hint");
        model.addConstr(label,GRB.LESS_EQUAL,1.0, "objective hint");
        model.addConstr(behavior,GRB.LESS_EQUAL,1.0, "objective hint");

        /*GRBLinExpr conTest = new GRBLinExpr();
        conTest.clear();
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k< nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        conTest.addTerm(1, y[i][k][j][l]);
                    }
                }
            }
        }
        conTest.addTerm(-1, sum);
        model.addConstr(conTest, GRB.EQUAL, 0.0, "Max Matches");


        GRBLinExpr conTest2 = new GRBLinExpr();
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++) {
                conTest2.addTerm(1, x[i][j]);
            }
        }
        conTest2.addTerm(-1, sum_x);
        model.addConstr(conTest2, GRB.EQUAL, 0.0, "Max Matches");
        */

        // matching from at most one constraint
        for (int i = 0; i< nodesNet1; i++){
            GRBLinExpr con1 = new GRBLinExpr();
            con1.clear();
            for (int j = 0; j< nodesNet2; j++){
                con1.addTerm(1, x[i][j]);
            }
            model.addConstr(con1, GRB.LESS_EQUAL, 1.0, "Max Matches");
        }

        // matching to at most 1 constraint
        for (int j = 0; j< nodesNet2; j++){
            GRBLinExpr con2 = new GRBLinExpr();
            con2.clear();
            for (int i = 0; i< nodesNet1; i++){
                con2.addTerm(1, x[i][j]);
            }
            model.addConstr(con2, GRB.LESS_EQUAL, 1.0, "Max Matches");
        }

        // linking between similar entries in the F matrices and the mapping
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k < nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++){
                    for (int l = 0; l < nodesNet2; l++) {
                        if (relNet1.getRelationForEntities(nodeNet1[i], nodeNet1[k]).equals(relNet2.getRelationForEntities(nodeNet2[j], nodeNet2[l]))) {
                            GRBLinExpr con3 = new GRBLinExpr();
                            con3.clear();
                            con3.addTerm(-1, x[i][j]);
                            con3.addTerm(1, y[i][k][j][l]);
                            model.addConstr(con3, GRB.LESS_EQUAL, 0, "linking");

                            GRBLinExpr con4 = new GRBLinExpr();
                            con4.clear();
                            con4.addTerm(1, y[i][k][j][l]);
                            con4.addTerm(-1, x[k][l]);
                            model.addConstr(con4, GRB.LESS_EQUAL, 0, "linking");
                        } else {
                            GRBLinExpr con3 = new GRBLinExpr();
                            con3.clear();
                            con3.addTerm(1, y[i][k][j][l]);
                            model.addConstr(con3, GRB.EQUAL, 0, "zero setter");
                        }
                    }
                }
            }
        }

        // Optimize model
        model.optimize();

        //print alignment
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++) {
                System.out.println(x[i][j].get(GRB.StringAttr.VarName) + " " + x[i][j].get(GRB.DoubleAttr.X));
            }
        }

        /*for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k< nodesNet1; k++) {
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        System.out.println(y[i][k][j][l].get(GRB.StringAttr.VarName) + " " + y[i][k][j][l].get(GRB.DoubleAttr.X));
                    }
                }
            }
        }*/

        //System.out.println(sum.get(GRB.StringAttr.VarName) + " " + sum.get(GRB.DoubleAttr.X));
        //System.out.println(sum_x.get(GRB.StringAttr.VarName) + " " + sum_x.get(GRB.DoubleAttr.X));

        //Create Results
        Alignment.Builder builder = new Alignment.Builder();
        for (int i = 0; i< nodesNet1; i++) {
            for (int j = 0; j < nodesNet2; j++) {
                if( Math.abs(x[i][j].get(GRB.DoubleAttr.X)) > 0.7){
                    builder.add(nodeNet1[i],nodeNet2[j]);
                }
            }
        }
        Result res = new Result(model.get(GRB.DoubleAttr.ObjVal),builder.build(net1.getName()+"-"+net2.getName()));
        // Dispose of model and environment
        model.dispose();
        env.dispose();

        return res;
    }

}