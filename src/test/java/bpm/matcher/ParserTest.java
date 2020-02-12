package bpm.matcher;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

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
            NetSystem net = Pipeline.parseFile(f);
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
    public void parseTest() throws IOException, SAXException, ParserConfigurationException {
        NetSystem net = Pipeline.parseFile(new File("eval-data/pnml/bpi15/BPIC15_5_08_AWB45.pnml"));

        return;
    }


}

