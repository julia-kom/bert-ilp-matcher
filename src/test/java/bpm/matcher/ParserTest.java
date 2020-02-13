package bpm.matcher;

import com.hp.hpl.jena.util.FileUtils;
import org.jbpt.petri.*;
import org.jbpt.petri.io.PNMLSerializer;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static bpm.matcher.Preprocessor.isTau;
import static org.jbpt.petri.io.WoflanSerializer.parse;

public class ParserTest {

    @Test
    public void newParserTest() throws Exception {
        File files = new File("eval-data/pnml/bpi15");
        for(File f : files.listFiles()){
            Parser parser = new Parser();
            NetSystem net = parser.parse(f);
            if(net.getTransitions().isEmpty()){
                throw  new Exception("no transitions found");
            }
            for(Transition t : net.getTransitions()){
                if(!isTau(t))
                    Assert.assertTrue(t.getId() + t.getLabel() + " is not a valid Id. Read op did not work.",Character.isDigit(t.getId().charAt(0)) && Character.isDigit(t.getId().charAt(1)));

            }


        }

    }

    @Test
    public void oldParserTest() throws Exception {
        File files = new File("eval-data/pnml/bpi15");
        for(File f : files.listFiles()){
            PNMLSerializer serializer = new PNMLSerializer();
            NetSystem net = serializer.parse(f.getAbsolutePath());
            if(net.getTransitions().isEmpty()){
                throw  new Exception("no transitions found");
            }
            for(Transition t : net.getTransitions()){
                if(!isTau(t))
                    Assert.assertTrue(t.getId() + t.getLabel() + " is not a valid Id. Read op did not work.",Character.isDigit(t.getId().charAt(0)) && Character.isDigit(t.getId().charAt(1)));
            }
        }

    }

    @Test
    public void parserCompTest() throws Exception {
        File birth = new File("eval-data/pnml/birth");
        File sap = new File("eval-data/pnml/sap");
        File uni = new File("eval-data/pnml/uni");
        File bpi15 = new File("eval-data/pnml/bpi15");
        parserComp(birth);
        parserComp(sap);
        parserComp(uni);
        parserComp(bpi15);
    }

    /**
     * The old paser sometimes removes a part of the label.
     * This test parses each petri net once with the old and once with the new parser and checks if labels and ids in
     * both parsed nets are the same.
     * As the old parser sometimes deletes the beginning of the label it is fine too if a transitions label of the
     * old parsers net is substring of the new parsers transition's label
     */
    private void parserComp(File path) throws Exception {
        File[] files = path.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pnml");
            }
        });
        PNMLSerializer oldSerializer = new PNMLSerializer();
        Parser newParser = new Parser();

        //test if transitions are the same (up to the point where the bug is fixed)
        System.out.println("Transitions: New Parser vs. Old Parser");
        for(File f : files){
            NetSystem oldNet = oldSerializer.parse(f.getAbsolutePath());
            NetSystem newNet = newParser.parse(f);

            //check all nodes of old net
            Set<Transition> newNodes = newNet.getTransitions();
            for(Node n : oldNet.getTransitions()){
              Assert.assertTrue("Node" +n.getId()+ " " +n.getLabel() + "not included in new net" +f.getName() ,isSubstring(newNet, n));
            }

        }

        System.out.println("Transitions: Old Parser vs. New Parser");
        for(File f : files){
            NetSystem oldNet = oldSerializer.parse(f.getAbsolutePath());
            NetSystem newNet = newParser.parse(f);

            //check all nodes of old net
            Set<Transition> oldNodes = oldNet.getTransitions();
            for(Node n : newNet.getTransitions()){
                Assert.assertTrue("Node" +n.getId() +n.getLabel()+ "not included in old net" ,isSuperstring(oldNet, n));
            }

        }

        // test if arcs are the same
        System.out.println("Arcs: Old Parser vs. New Parser");
        for(File f : files){
            NetSystem oldNet = oldSerializer.parse(f.getAbsolutePath());
            NetSystem newNet = newParser.parse(f);

            Collection<Flow> newEdges = newNet.getEdges();
            Collection<Flow> oldEdges = oldNet.getEdges();

            for(Flow nEdge : newEdges){
                Assert.assertTrue("New edge "+nEdge+"not included in new net",containsFlow(oldEdges,nEdge));
            }
            for(Flow oEdge : oldEdges){
                Assert.assertTrue("Old edge "+oEdge+"not included in new net",containsFlow(newEdges,oEdge));
            }

        }

        // net props
        for(File f : files) {
            NetSystem oldNet = oldSerializer.parse(f.getAbsolutePath());
            NetSystem newNet = newParser.parse(f);
            //Name
            Assert.assertTrue("Net Names not matching", oldNet.getName().equals(newNet.getName()));
            //Initial Marking
            Assert.assertTrue("Initial Marking not matching", oldNet.getMarkedPlaces().iterator().next().getId()
                    .equals(oldNet.getMarkedPlaces().iterator().next().getId()));

        }
    }



    private static boolean containsFlow(Collection<Flow> flows, Flow f){
        for(Flow g : flows){
            if(g.getSource().getId().equals(f.getSource().getId()) &&
                    g.getTarget().getId().equals(f.getTarget().getId())){
                return true;
            }else{

            }
        }
        return false;
    }

    /**
     * Returns true if Id and label match
     * Returns true and prints an error when the label is substring of the nets node
     * Returns false else
     * @param net
     * @param n
     * @return
     */
    private static boolean isSubstring(NetSystem net, Node n){
        String substring = "";
        for(Node m : net.getTransitions()){
            if(m.getId().equals(n.getId())){
                if(m.getLabel().equals(n.getLabel())){
                    return true;
                }else if(m.getLabel().contains(n.getLabel())){
                    substring =">>"+ m.getLabel() + "<< is superstring of net's " + net.getName() +" node >>" + n.getLabel() + "<< ( Id: "+ n.getId() + ")";
                }
            }
        }
        if(substring.equals("")){
            return false;
        }else{
            System.err.println(substring);
            return true;
        }
    }


    /**
     * Returns true if Id and label match
     * Returns true and prints an error when the label is substring of the nets node
     * Returns false else
     * @param net
     * @param n
     * @return
     */
    private static boolean isSuperstring(NetSystem net, Node n){
        String substring = "";
        for(Node m : net.getTransitions()){
            if(m.getId().equals(n.getId())){
                if(m.getLabel().equals(n.getLabel())){
                    return true;
                }else if(n.getLabel().contains(m.getLabel())){
                    substring =">>"+ m.getLabel() + "<< is substring of net's " + net.getName() +" node >>" + n.getLabel() + "<< ( Id: "+ n.getId() + ")";
                }
            }
        }
        if(substring.equals("")){
            return false;
        }else{
            System.err.println(substring);
            return true;
        }
    }

    @Test
    public void parseTest() throws IOException, SAXException, ParserConfigurationException {
        NetSystem net = Pipeline.parseFile(new File("eval-data/pnml/bpi15/BPIC15_5_08_AWB45.pnml"));

        return;
    }


}

