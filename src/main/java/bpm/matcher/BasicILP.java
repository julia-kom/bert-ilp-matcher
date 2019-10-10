package bpm.matcher;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import org.jbpt.bp.RelSet;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;


public class BasicILP extends AbstractILP {
    public BasicILP(){

    }

    /**
     * Compute the basic 1:1 ILP behavior/label simialrity match.
     * @param relNet1 Profile of Net 1
     * @param relNet2 Profile of Net 2
     * @param net1 Net 1
     * @param net2 Net 2
     * @param sim Label Similarity Matrix between the two nets
     * @return
     * @throws GRBException
     */
    @Override
    protected AbstractILP.Result solve(RelSet relNet1, RelSet relNet2, NetSystem net1, NetSystem net2, LabelSimilarity sim) throws GRBException {
        //setup variables
        Node[] entitiesNet1 = (Node[]) net1.getEntities().toArray();
        Node[] entitiesNet2 = (Node[]) net2.getEntities().toArray();
        int nodesNet1 = entitiesNet1.length;
        int nodesNet2 = entitiesNet2.length;
        int minSize = Math.min(nodesNet1,nodesNet2);


        GRBVar[][] x = new GRBVar[nodesNet1][nodesNet2];
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++){
                x[i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_"+i+"_"+j);
            }
        }

        GRBVar[][][][] y = new GRBVar[nodesNet1][nodesNet1][nodesNet2][nodesNet2];
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k< nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        y[i][j][k][l] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_" + i + "_" + j);
                    }
                }
            }
        }

        // Objective weighted between behavioral correspondance and label similarity
        // Behavioral part
        GRBLinExpr expr = new GRBLinExpr();
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k< nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++) {
                    for (int l = 0; l < nodesNet2; l++) {
                        expr.addTerm(this.similarityWeight/(nodesNet1*nodesNet2), y[i][j][k][l]);
                    }
                }
            }
        }

        // Label Similarity Part
        for (int i = 0; i< nodesNet1; i++){
            for (int j = 0; j < nodesNet2; j++){

                expr.addTerm(((1- this.similarityWeight)*LabelSimilarity.BoWSim(entitiesNet1[i].getLabel(),entitiesNet2[j].getLabel()))/(minSize), x[i][j]);
            }
        }

        model.setObjective(expr, GRB.MAXIMIZE);

        //setup model

        // matching from at most one constraint
        for (int i = 0; i< nodesNet1; i++){
            GRBLinExpr con1 = new GRBLinExpr();
            for (int j = 0; j< nodesNet2; j++){
                expr.addTerm(1, x[i][j]);
            }
            model.addConstr(con1, GRB.LESS_EQUAL, 1, "Max Matches");
        }

        // matching to at most 1 constraint
        for (int j = 0; j< nodesNet2; j++){
            GRBLinExpr con2 = new GRBLinExpr();
            for (int i = 0; i< nodesNet1; i++){
                expr.addTerm(1, x[i][j]);
            }
            model.addConstr(con2, GRB.LESS_EQUAL, 1, "Max Matches");
        }

        // linking between similar entries in the F matrices and the mapping
        for (int i = 0; i< nodesNet1; i++){
            for (int k = 0; k < nodesNet1; k++){
                for (int j = 0; j < nodesNet2; j++){
                    for (int l = 0; l < nodesNet2; l++) {
                        if (relNet1.getRelationForEntities(entitiesNet1[i], entitiesNet1[k]).equals(relNet2.getRelationForEntities(entitiesNet1[j], entitiesNet1[l]))) {
                            GRBLinExpr con3 = new GRBLinExpr();
                            con3.addTerm(2, y[i][j][k][l]);
                            con3.addTerm(-1, x[i][j]);
                            con3.addTerm(-1, x[k][l]);
                            model.addConstr(con3, GRB.LESS_EQUAL, 0, "linking");
                        } else {
                            GRBLinExpr con3 = new GRBLinExpr();
                            con3.addTerm(1, y[i][j][k][l]);
                            model.addConstr(con3, GRB.EQUAL, 0, "zero setter");
                        }
                    }
                }
            }
        }

        // Optimize model
        model.optimize();

        // create result
        AbstractILP.Result res = new AbstractILP.Result(model.get(GRB.DoubleAttr.ObjVal),x);

        // Dispose of model and environment
        model.dispose();
        env.dispose();

        return res;
    }

}
