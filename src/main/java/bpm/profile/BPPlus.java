package bpm.profile;

import com.iise.shudi.exroru.RefinedOrderingRelation;
import com.iise.shudi.exroru.RefinedOrderingRelationsMatrix;
import org.jbpt.bp.RelSet;
import org.jbpt.bp.construct.AbstractRelSetCreator;
import org.jbpt.bp.construct.RelSetCreator;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;

import java.util.Collection;

/**
 * Wrapper Class for BP+
 */
public class BPPlus extends AbstractProfile {
    RefinedOrderingRelationsMatrix rorm;
    public BPPlus(){

    }

    @Override
    public Relation getRelationBetween(Node n1, Node n2) {
        return null;
    }
}
