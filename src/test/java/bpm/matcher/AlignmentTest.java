package bpm.matcher;

import bpm.alignment.Alignment;
import bpm.alignment.Correspondence;
import org.jbpt.petri.Node;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit Tests for Alignments
 */
public class AlignmentTest {

    @Test
    public void complexCorrespondenceTest(){
        Node p1n1 = new Node("n1");
        Node p1n2 = new Node("n2");

        Node p2n1 = new Node("n1");

        Correspondence c = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet1(p1n2)
                .addNodeFromNet2(p2n1)
                .build();
        Assert.assertTrue(c.isComplexCorrespondence());

        c = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet2(p1n2)
                .build();
        Assert.assertTrue(!c.isComplexCorrespondence());
    }

    @Test(expected = Exception.class)
    public void properCorrespondenceTest(){
        new Correspondence.Builder().build();
    }

    @Test
    public void MutuallyExclusivenessTest(){
        Node p1n1 = new Node("n1");
        Node p1n2 = new Node("n2");

        Node p2n1 = new Node("n1");

        Correspondence c1 = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet2(p2n1)
                .build();

        Correspondence c2 = new Correspondence.Builder()
                .addNodeFromNet1(p1n2)
                .addNodeFromNet2(p2n1)
                .build();

        Alignment a = new Alignment.Builder()
                .addCorrespondence(c1)
                .addCorrespondence(c2)
                .build();
        Assert.assertTrue(!a.isMutuallyExclusive());

        a = new Alignment.Builder().addCorrespondence(c1).build();
        Assert.assertTrue(a.isMutuallyExclusive());
    }

    @Test
    public void MappedTest(){
        Node p1n1 = new Node("n1");
        Node p1n2 = new Node("n2");

        Node p2n1 = new Node("n1");

        Correspondence c1 = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet2(p2n1)
                .build();

        Correspondence c2 = new Correspondence.Builder()
                .addNodeFromNet1(p1n2)
                .addNodeFromNet2(p2n1)
                .build();

        Alignment a = new Alignment.Builder()
                .addCorrespondence(c1)
                .addCorrespondence(c2)
                .build();
        Assert.assertTrue(a.isMapped(p1n1,p2n1));
        Assert.assertTrue(a.isMapped(p1n2,p2n1));
        Assert.assertTrue(!a.isMapped(p1n1,p1n2));
        Assert.assertTrue(!a.isMapped(p1n1,p1n1));
    }

    @Test
    public void getCorrespondenceTest(){
        Node p1n1 = new Node("n1");
        Node p1n2 = new Node("n2");

        Node p2n1 = new Node("n1");

        Correspondence c1 = new Correspondence.Builder()
                .addNodeFromNet1(p1n1)
                .addNodeFromNet2(p2n1)
                .build();

        Correspondence c2 = new Correspondence.Builder()
                .addNodeFromNet1(p1n2)
                .addNodeFromNet2(p2n1)
                .build();

        Alignment a = new Alignment.Builder()
                .addCorrespondence(c1)
                .addCorrespondence(c2)
                .build();
        Assert.assertTrue(a.getCorrespondenceOfNode(p1n1).size() == 1);
        Assert.assertTrue(a.getCorrespondenceOfNode(p2n1).size() == 2);
    }
}
