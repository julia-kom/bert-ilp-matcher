package bpm.ippm.ilp;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.alignment.Correspondence;
import bpm.ippm.alignment.Result;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.similarity.Matrix;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import java.util.Set;

import static bpm.ippm.matcher.Pipeline.PRINT_ENABLED;

//This ILP system is too large to actually set it up in most cases.
@Deprecated
public class BasicILP3 extends AbstractILP {
    public BasicILP3() {

    }

    /**
     * Compute the basic 1:1 ILP behavior/label simialrity match.
     * In BASIC3 we decrease the number of variables by deleting the X variable
     * and derive the matching state from the Y variables only.
     *
     * Result: There was no positive impact on the computational complexity
     *
     * @param relNet1 Profile of Net 1
     * @param relNet2 Profile of Net 2
     * @param net1    Net 1
     * @param net2    Net 2
     * @return
     * @throws GRBException
     */
    @Override
    public Result solve(AbstractProfile relNet1, AbstractProfile relNet2, Set<Transition> net1, Set<Transition> net2, Matrix matrix, Alignment preAlignment, String name) throws GRBException {
        //setup variables
        Transition[] nodeNet1 =  net1.toArray(new Transition[net1.size()]);
        Transition[] nodeNet2 =  net2.toArray(new Transition[net2.size()]);
        int nodesNet1 = nodeNet1.length;
        int nodesNet2 = nodeNet2.length;
        int minSize = Math.min(nodesNet1, nodesNet2);


        GRBVar[][][][] y = new GRBVar[nodesNet1][nodesNet1][nodesNet2][nodesNet2];
        for (int i = 0; i < nodesNet1; i++) {
            for (int k = 0; k < nodesNet1; k++) {
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        y[i][k][j][l] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y_" + i + "_" + k + "_" + j + "_" + l);
                    }
                }
            }
        }

        // Objective weighted between behavioral correspondance and label similarity
        // Behavioral part
        GRBLinExpr behavior = new GRBLinExpr();
        for (int i = 0; i < nodesNet1; i++) {
            for (int k = 0; k < nodesNet1; k++) {
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        if (relNet1.getRelationForEntities(nodeNet1[i], nodeNet1[k]).equals(relNet2.getRelationForEntities(nodeNet2[j], nodeNet2[l]))) {
                            behavior.addTerm(1.0 / (minSize * minSize), y[i][k][j][l]);
                        }
                    }
                }
            }
        }

        // Label Similarity Part
        GRBLinExpr label = new GRBLinExpr();
        for (int i = 0; i < nodesNet1; i++) {
            for (int j = 0; j < nodesNet2; j++) {
                GRBLinExpr lab = new GRBLinExpr();
                for (int k = 0; k < nodesNet1; k++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        lab.addTerm(1.0, y[i][k][j][l]);
                    }
                }
                label.multAdd(matrix.between(nodeNet1[i],nodeNet2[j])/ ((minSize) * (minSize) ), lab);
            }
        }

        GRBLinExpr obj = new GRBLinExpr();
        obj.multAdd(this.similarityWeight, behavior);
        obj.multAdd((1 - this.similarityWeight), label);

        model.setObjective(obj, GRB.MAXIMIZE);

        //setup model

        // hint expressions
        // model.addConstr(obj,GRB.LESS_EQUAL,1.0, "objective hint");
        // model.addConstr(label,GRB.LESS_EQUAL,1.0, "objective hint");
        //model.addConstr(behavior,GRB.LESS_EQUAL,1.0, "objective hint");


        // max 1 matching partner
        GRBLinExpr con1 = new GRBLinExpr();
        for (int i = 0; i < nodesNet1; i++) {
            for (int j = 0; j < nodesNet2; j++) {
                for (int k = 0; k < nodesNet1; k++) {
                    for (int l = 0; l < nodesNet2; l++) {

                        //Symmetry
                        con1.clear();
                        con1.addTerm(1.0, y[i][k][j][l]);
                        con1.addTerm(-1.0, y[k][i][l][j]);
                        model.addConstr(con1, GRB.EQUAL, 0.0, "");

                        //equal constraint
                        for (int u = 0; u < nodesNet1; u++) {
                            for (int v = 0; v < nodesNet2; v++) {
                                for (int w = 0; w < nodesNet2; w++) {
                                    /*if (v != j) {
                                        con1.clear();
                                        con1.addTerm(1.0, y[i][k][j][l]);
                                        con1.addTerm(1.0, y[i][u][v][w]);
                                        model.addConstr(con1, GRB.LESS_EQUAL, 1.0, "");
                                    }*/

                                    if (w != l) {
                                        con1.clear();
                                        con1.addTerm(1.0, y[i][k][j][l]);
                                        con1.addTerm(1.0, y[u][k][v][w]);
                                        model.addConstr(con1, GRB.LESS_EQUAL, 1.0, "");
                                    }
                                }
                            }
                        }

                        //equal constraint
                       for (int u = 0; u < nodesNet1; u++) {
                            for (int v = 0; v < nodesNet1; v++) {
                                for (int w = 0; w < nodesNet2; w++) {
                                    /*if (u != i) {
                                        con1.clear();
                                        con1.addTerm(1.0, y[i][v][j][l]);
                                        con1.addTerm(1.0, y[u][v][j][w]);
                                        model.addConstr(con1, GRB.LESS_EQUAL, 1.0, "");
                                    }*/

                                   if (v != k) {
                                        con1.clear();
                                        con1.addTerm(1.0, y[i][k][j][l]);
                                        con1.addTerm(1.0, y[u][v][w][l]);
                                        model.addConstr(con1, GRB.LESS_EQUAL, 1.0, "");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }




        // Optimize model
        model.optimize();


        //if(PRINT_ENABLED) System.out.println(sum.get(GRB.StringAttr.VarName) + " " + sum.get(GRB.DoubleAttr.X));
        //if(PRINT_ENABLED) System.out.println(sum_x.get(GRB.StringAttr.VarName) + " " + sum_x.get(GRB.DoubleAttr.X));

        // create result
        Alignment.Builder builder = new Alignment.Builder();
        for (int i = 0; i< nodesNet1; i++) {
            for (int j = 0; j < nodesNet2; j++) {
                double sum = 0;
                for (int k = 0; k< nodesNet1; k++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        sum += y[i][k][j][l].get(GRB.DoubleAttr.X);
                    }
                }

                if( Math.abs(sum- minSize) < 0.0001){
                    builder.addCorrespondence(new Correspondence.Builder().addNodeFromNet1(nodeNet1[i]).addNodeFromNet2(nodeNet2[j]).withLikelihood(matrix.between(nodeNet1[i],nodeNet2[j])).build());
                }
            }
        }


        Result res = new Result(model.get(GRB.DoubleAttr.ObjVal), builder.build(name), model.get(GRB.DoubleAttr.MIPGap));

        // Dispose of model and environment
        model.dispose();
        env.dispose();

        return res;
    }


}
