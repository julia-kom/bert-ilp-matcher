package bpm.matcher;

import com.hp.hpl.jena.util.FileUtils;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;
import org.jbpt.petri.io.PNMLSerializer;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
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
        parserComp(birth.listFiles());
        parserComp(sap.listFiles());
        parserComp(uni.listFiles());
        parserComp(bpi15.listFiles());
    }

    /**
     * The old paser sometimes removes a part of the label.
     * This test parses each petri net once with the old and once with the new parser and checks if labels and ids in
     * both parsed nets are the same.
     * As the old parser sometimes deletes the beginning of the label it is fine too if a transitions label of the
     * old parsers net is substring of the new parsers transition's label
     */
    private void parserComp(File[] files) throws Exception {
        PNMLSerializer oldSerializer = new PNMLSerializer();
        Parser newParser = new Parser();
        for(File f : files){
            NetSystem oldNet = oldSerializer.parse(f.getAbsolutePath());
            NetSystem newNet = newParser.parse(f);

            //check all nodes of old net
            Set<Node> newNodes = newNet.getNodes();
            for(Node n : oldNet.getNodes()){
              Assert.assertTrue("Node" +n.getId() +n.getLabel()+ "not included in new net" ,isInculded(newNet, n));
            }

            //check all nodes of old net
            Set<Node> oldNodes = oldNet.getNodes();
            for(Node n : newNet.getNodes()){
                Assert.assertTrue("Node" +n.getId() +n.getLabel()+ "not included in new net" ,isInculded(newNet, n));
            }

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
    private static boolean isInculded(NetSystem net, Node n){
        String substring = "";
        for(Node m : net.getNodes()){
            if(m.getId().equals(n.getId())){
                if(m.getLabel().equals(n.getLabel())){
                    return true;
                }else if(m.getLabel().contains(n.getLabel())){
                    substring = m.getLabel() + " is substring of net's " + net.getName() +" node " + n.getLabel() + "( Id: "+ n.getId() + ")";
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

