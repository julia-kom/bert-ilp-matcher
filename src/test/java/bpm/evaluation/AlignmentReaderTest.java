package bpm.evaluation;

import bpm.alignment.Alignment;
import bpm.alignment.Correspondence;
import org.jbpt.petri.Node;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Unit test
 */
public class AlignmentReaderTest
{
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    /**
     * Rigorous Test :-)
     */
    @Test
    public void RdfReadWriteTest() throws Exception
    {
        Node p1n1 = new Node("n1");
        p1n1.setId("p1n1");
        Node p1n2 = new Node("n2");
        p1n2.setId("p1n2");
        Node p2n1 = new Node("n1");
        p2n1.setId("p2n1");



        Alignment a = new Alignment.Builder()
                .add(p1n1,p2n1)
                .add(p2n1,p2n1)
                .build("test");

        RdfAlignmentReader reader = new RdfAlignmentReader();
        File file = folder.newFile("rdf-read-write-test.rdf");
        reader.writeAlignmentTo(file,a,"m1","m2");
        Alignment a2 = reader.readAlignmentFrom(file);
        boolean same = true;
        for(Correspondence c : a.getCorrespondences()){
            if(!a2.contains(c)){
                same = false;
                break;
            }
        }
        for(Correspondence c : a2.getCorrespondences()){
            if(!a.contains(c)){
                same = false;
                break;
            }
        }

        Assert.assertTrue(same);
    }
}
