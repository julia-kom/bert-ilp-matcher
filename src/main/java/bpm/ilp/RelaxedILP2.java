package bpm.ilp;

import bpm.alignment.Alignment;
import bpm.alignment.Correspondence;
import bpm.alignment.Result;
import bpm.similarity.Matrix;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import org.jbpt.bp.RelSet;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import java.util.Arrays;
import java.util.Set;

import static bpm.matcher.Pipeline.PRINT_ENABLED;


public class RelaxedILP2 extends AbstractILP {
    public RelaxedILP2(){

    }

    /**
     * Compute the Relaxed2 1:1 *LP* behavior/label simialrity match:
     * In comparison to the BASIC ILP here are just all Binary Variables Contineous.
     * No hint on objective function used
     * This ILP produces a valid matching. No similarity score!
     * @param relNet1 Profile of Net 1
     * @param relNet2 Profile of Net 2
     * @param net1 Net 1
     * @param net2 Net 2
     * @return
     * @throws GRBException
     */
    @Override
    public Result solve(RelSet relNet1, RelSet relNet2, Set<Transition> net1, Set<Transition> net2, Matrix matrix,Alignment preAlignment, String name) throws GRBException {
        //setup variables
        Node[] nodeNet1 =  net1.toArray(new Node[net1.size()]);
        Node[] nodeNet2 =  net2.toArray(new Node[net2.size()]);
        int nodesNet1 = nodeNet1.length;
        int nodesNet2 = nodeNet2.length;
        int minSize = Math.min(nodesNet1,nodesNet2);

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
                        y[i][k][j][l] = model.addVar(0.0, 1.0, 0.0, GRB.CONTINUOUS, "y_" + i + "_" + j+"_"+k+"_"+l);
                    }
                }
            }
        }

        //GRBVar sum = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "sum_y");
        //GRBVar sum_x = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "sum_x");

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
                            con3.addTerm(2, y[i][k][j][l]);
                            con3.addTerm(-1, x[i][j]);
                            con3.addTerm(-1, x[k][l]);
                            model.addConstr(con3, GRB.LESS_EQUAL, 0, "linking");
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

        // add prematches
        for (Correspondence c: preAlignment.getCorrespondences()){
            Node n1 =  c.getNet1Nodes().iterator().next();
            Node n2 =  c.getNet2Nodes().iterator().next();
            int i = Arrays.asList(nodeNet1).indexOf(n1);
            int j = Arrays.asList(nodeNet2).indexOf(n2);
            GRBLinExpr conPre = new GRBLinExpr();
            conPre.addTerm(1,x[i][j]);
            model.addConstr(conPre, GRB.EQUAL, 1, "pre matched");
        }


        // Optimize model
        model.optimize();

        //print alignment
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++) {
                if(PRINT_ENABLED) System.out.println(x[i][j].get(GRB.StringAttr.VarName) + " " + x[i][j].get(GRB.DoubleAttr.X));
            }
        }

        /*for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k< nodesNet1; k++) {
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        if(PRINT_ENABLED) System.out.println(y[i][k][j][l].get(GRB.StringAttr.VarName) + " " + y[i][k][j][l].get(GRB.DoubleAttr.X));
                    }
                }
            }
        }*/

        //if(PRINT_ENABLED) System.out.println(sum.get(GRB.StringAttr.VarName) + " " + sum.get(GRB.DoubleAttr.X));
        //if(PRINT_ENABLED) System.out.println(sum_x.get(GRB.StringAttr.VarName) + " " + sum_x.get(GRB.DoubleAttr.X));

        //Results
        Alignment.Builder builder = new Alignment.Builder();
        for (int i = 0; i< nodesNet1; i++) {
            for (int j = 0; j < nodesNet2; j++) {
                if (x[i][j].get(GRB.DoubleAttr.X) >= 0.0001){
                    //TODO test what works better!
                    //builder.add(nodeNet1[i],nodeNet2[j]);
                    builder.addCorrespondence(new Correspondence.Builder().addNodeFromNet1(nodeNet1[i]).addNodeFromNet2(nodeNet2[j]).withLikelihood(x[i][j].get(GRB.DoubleAttr.X)).build());
                }            }
        }
        Result res = new Result(model.get(GRB.DoubleAttr.ObjVal),builder.build(name));

        // Dispose of model and environment
        model.dispose();
        env.dispose();

        return res;
    }

}
