package bpm.matcher;

import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

import java.io.FileNotFoundException;
import java.util.HashMap;


/**
 * Pnml Parser
 */
public  class Parser {

    Xml xmlParser = new Xml();

    HashMap<String, org.jbpt.petri.Node> nodeMapping = new HashMap();

    public NetSystem parse(File file) throws Exception {

        // create new net
        NetSystem target = new NetSystem();
        if(!file.exists()){
            throw new FileNotFoundException("File not found: "+ file.toString());
        }

        //parse as xml
        Document doc = xmlParser.parse(file);

        // handle all components
        handleNetName(doc,target);
        handleTransitions(doc,target);
        handlePlaces(doc,target);
        handleArcs(doc,target);

        return target;
    }

    private void handleNetName(Document doc, NetSystem target) {
        Node name = doc.getElementsByTagName("name").item(0);
        if(!name.getParentNode().getOwnerDocument().getNodeName().equals("net")){
            //no name found
            target.setName("");
        }else{
            target.setName(name.getTextContent());
        }
    }


    private void handleTransitions(Document doc, NetSystem target) throws Exception{
        //handle transitions
        NodeList transitions = doc.getElementsByTagName("transition");
        for(int i = 0; i< transitions.getLength(); i++) {
            Element t = (Element) transitions.item(i);
            String tId = t.getAttribute("id");

            if (tId == null ||tId.equals("")) {
                throw new Exception("Id of transtion is not given. Iteration " + i);
            }

            Node label = t.getElementsByTagName("name").item(0);
            String tLabel;

            if (label == null) {
                tLabel = "";
            } else {
                tLabel = xmlParser.normalize(label.getTextContent());
            }

            //create transition
            Transition pTransition = new Transition();
            pTransition.setName(tLabel);
            pTransition.setLabel(tLabel);
            pTransition.setId(tId);
            target.addTransition(pTransition);

            // add transition to map
            nodeMapping.put(tId,pTransition);
        }

    }


    private void handlePlaces(Document doc, NetSystem target) throws Exception{
        // handle places
        NodeList places = doc.getElementsByTagName("place");
        for(int i = 0; i< places.getLength(); i++){
            Element p = (Element) places.item(i);
            String pId = p.getAttribute("id");

            //if part of final marking then ignore
            if(p.getParentNode().getNodeName().equals("marking")){
                continue;
            }

            if (pId == null || pId.equals("")) {
                throw new Exception("Id of place is not given. Iteration " + i);
            }

            Node label = p.getElementsByTagName("name").item(0);
            String pLabel;
            if (label == null) {
                pLabel = "";
            } else {
                pLabel = xmlParser.normalize(label.getTextContent());
            }

            if (pLabel == null) {
                pLabel = "";
            }

            // create a place
            Place pPlace = new Place();
            pPlace.setName(pLabel);
            pPlace.setLabel(pLabel);
            pPlace.setId(pId);
            target.addPlace(pPlace);

            // put tokens if given in file
            Node initialMarking = p.getElementsByTagName("initialMarking").item(0);
            if (initialMarking != null) {
                String init = xmlParser.normalize(initialMarking.getTextContent());
                int nTokens =  Integer.valueOf(init);
                target.putTokens(pPlace,nTokens);
            }

            // add place to map
            nodeMapping.put(pId,pPlace);
        }
    }


    private void handleArcs(Document doc, NetSystem target) throws Exception{
        //<arc id="2198527b-49c4-4eb8-b9ec-bdf256cdd12f" source="n6" target="n76"/>
        // handle arcs
        // handle places
        NodeList arcs = doc.getElementsByTagName("arc");
        for(int i = 0; i< arcs.getLength(); i++) {
            Element a = (Element) arcs.item(i);
            String aId = a.getAttribute("id");

            if (aId == null) {
                throw new Exception("Id of arc is not given. Iteration " + i);
            }

            String aSource = a.getAttribute("source");

            if (aSource == null) {
                throw new Exception("Source Id of arc is not given. Iteration " + i);
            }

            String aTarget = a.getAttribute("target");

            if (aTarget == null) {
                throw new Exception("Target Id of arc is not given. Iteration " + i);
            }

            if(!nodeMapping.containsKey(aSource)){
                throw new Exception("Id "+aSource + " is used in arc but not defined as place or transition");
            }

            if(!nodeMapping.containsKey(aTarget)){
                throw new Exception("Id "+aTarget + " is used in arc but not defined as place or transition");
            }

            // ad arc to net
            target.addFlow(nodeMapping.get(aSource),nodeMapping.get(aTarget));
            //target.addEdge(nodeMapping.get(aSource),nodeMapping.get(aTarget));
        }

    }

    public class Xml {
        public  Document parse(File f) throws ParserConfigurationException, IOException, SAXException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(f);
        }

        /**
         * replace backslash n by space and remove double spaces afterwards
         * @return
         */
        public  String normalize(String label){
            label = label.replace("\n", " ");
            label = label.trim().replaceAll(" +", " ");
            return label;

        }

        public  void saveToFile(Document ref, File f) throws TransformerException, IOException {
            f.createNewFile();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transf = transformerFactory.newTransformer();
            transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transf.setOutputProperty(OutputKeys.INDENT, "yes");
            transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(ref);

            //StreamResult console = new StreamResult(System.out);
            StreamResult file = new StreamResult(f);

            //transf.transform(source, console);
            transf.transform(source, file);
        }
    }
}
