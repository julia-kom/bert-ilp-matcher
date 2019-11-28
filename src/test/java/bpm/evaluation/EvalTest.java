package bpm.evaluation;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import bpm.ilp.RelaxedILP3;
import bpm.matcher.Matcher;
import org.jbpt.petri.Node;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;

public class EvalTest {

    @Test
    public void BinaryEvalTest(){
        Node n1 = new Node("n1");
        n1.setId("id1");
        Node n2 = new Node("n2");
        n2.setId("id2");
        Node n3 = new Node("n3");
        n3.setId("id3");
        Node n4 = new Node("n4");
        n4.setId("id4");
        Node n5 = new Node("n5");
        n5.setId("id5");
        Node n6 = new Node("n6");
        n6.setId("id6");
        Node n7 = new Node("n7");
        n7.setId("id7");

        Alignment match = new Alignment.Builder().add(n1,n5).add(n2,n6).add(n3,n7).add(n4,n7).build("test1");
        Alignment gs1 = new Alignment.Builder().add(n1,n5).add(n2,n6).add(n4,n7).build("gs1"); // 1 matcher found one to many
        Alignment gs2 = new Alignment.Builder().add(n1,n5).add(n2,n6).add(n3,n7).add(n4,n7).build("gs2"); // Exact match

        // precision = 3/4 , recall = 0
        Eval e1 = Eval.Builder.BinaryEvaluation(new Result(0,match),gs1);
        Assert.assertTrue((abs(e1.getPrecision() - 0.75)) <0.0001 && (abs(e1.getRecall() -1.0)) <0.0001);

        // Full precision = 1, recall = 1, fscore = 1
        Eval e2 = Eval.Builder.BinaryEvaluation(new Result(0,match),gs2);
        Assert.assertTrue((abs(e2.getPrecision() - 1.0)) <0.0001 && (abs(e2.getRecall()- 1.0)) <0.0001 && (abs(e2.getFscore()- 1.0)) <0.0001);

        // precision = 1, recall = 3/4
        Eval e3 = Eval.Builder.BinaryEvaluation(new Result(0,gs1), match);
        Assert.assertTrue((abs(e3.getPrecision() - 1.0)) <0.0001 && (abs(e3.getRecall()- 0.75)) <0.0001);
    }

    @Test
    public void StrictBinaryEvalTest(){
        Node n1 = new Node("n1");
        n1.setId("id1");
        Node n2 = new Node("n2");
        n2.setId("id2");
        Node n3 = new Node("n3");
        n3.setId("id3");
        Node n4 = new Node("n4");
        n4.setId("id4");
        Node n5 = new Node("n5");
        n5.setId("id5");
        Node n6 = new Node("n6");
        n6.setId("id6");
        Node n7 = new Node("n7");
        n7.setId("id7");

        Alignment match = new Alignment.Builder().add(n1,n5).add(n2,n6).add(n3,n7).add(n4,n7).build("test");
        Alignment gs1 = new Alignment.Builder().add(n1,n5).add(n2,n6).add(n4,n7).build("gs1"); // 1 matcher found one to many
        Alignment gs2 = new Alignment.Builder().add(n1,n5).add(n2,n6).add(n3,n7).add(n4,n7).build("gs2"); // Exact match

        // precision = 2/3 , recall = 2/3
        Eval e1 = Eval.Builder.StrictBinaryEvaluation(new Result(0,match),gs1);
        Assert.assertTrue((abs(e1.getPrecision() - (2.0/3))) <0.0001 && (abs(e1.getRecall() - (2.0/3))) <0.0001);

        // Full precision = 1, recall = 1, fscore = 1
        Eval e2 = Eval.Builder.StrictBinaryEvaluation(new Result(0,match),gs2);
        Assert.assertTrue((abs(e2.getPrecision() - 1.0)) <0.0001 && abs(e2.getRecall()- 1.0) <0.0001 && abs(e2.getFscore()- 1.0) <0.0001);
    }

    @Test
    public void ProbabilisticEvalTest(){}


    @Test
    public void AggregatedEvalTest(){
        Node n1 = new Node("n1");
        n1.setId("id1");
        Node n2 = new Node("n2");
        n2.setId("id2");
        Node n3 = new Node("n3");
        n3.setId("id3");
        Node n4 = new Node("n4");
        n4.setId("id4");
        Node n5 = new Node("n5");
        n5.setId("id5");
        Node n6 = new Node("n6");
        n6.setId("id6");
        Node n7 = new Node("n7");
        n7.setId("id7");

        Alignment match = new Alignment.Builder().add(n1,n5).add(n2,n6).add(n3,n7).add(n4,n7).build("test");
        Alignment gs1 = new Alignment.Builder().add(n1,n5).add(n2,n6).add(n4,n7).build("gs1"); // 1 matcher found one to many
        Alignment gs2 = new Alignment.Builder().add(n1,n5).add(n2,n6).add(n3,n7).add(n4,n7).build("gs2"); // Exact match

        // precision = 3/4 , recall = 1
        Eval e1 = Eval.Builder.BinaryEvaluation(new Result(0,match),gs1);
        // Full precision = 1, recall = 1, fscore = 1
        Eval e2 = Eval.Builder.BinaryEvaluation(new Result(0,match),gs2);
        // precision = 1, recall = 3/4
        Eval e3 = Eval.Builder.BinaryEvaluation(new Result(0,gs1), match);

        List<Eval> evals = new LinkedList<>();
        evals.add(e1);
        evals.add(e2);
        evals.add(e3);

        AggregatedEval aggEval = new AggregatedEval(evals);

        Assert.assertTrue(abs(aggEval.getPrecisionMacro() - (11.0/12)) < 0.0001);
        Assert.assertTrue(abs(aggEval.getRecallMacro() - (11.0/12)) < 0.0001);
        Assert.assertTrue(abs(aggEval.getPrecisionMicro() - (10.0/11)) < 0.0001);
        Assert.assertTrue(abs(aggEval.getRecallMicro() - (10.0/11)) < 0.0001);
    }

    @Test
    public void retrospectiveTest() throws IOException, JSONException, ParseException {
        File goldstandard = new File(getClass().getClassLoader().getResource("./goldstandard/birth").getFile());
        File result = new File(getClass().getClassLoader().getResource("./eval-results/test1").getFile());
        //run retrospecitve test
        Pipeline pip = new Pipeline.Builder().withGoldStandard(goldstandard.toPath()).atThreshold(0.7).withResultPath(result.toPath()).withRetrospective().build();
        pip.run();
        Path logPath = pip.getLogPath();
        File f =  new File(logPath+"/aggRetrospectiveResults.eval");
        BufferedReader reader = new BufferedReader(new FileReader(f));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();

        //test if aggResults are computed
        Assert.assertTrue(lines == 4);

        // get json file of the unfiltered evaluation run
        File config = new File(result +"/config.log");
        FileReader reader2 = new FileReader(config);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(reader2);
        JSONObject matcher1 = (JSONObject) json.get("matcher");

        // get the json file after the evaluation run
        JSONObject matcher2 = (JSONObject) pip.toJSON().get("matcher");

        //check if the matcher parts are equal
        System.out.println(matcher1.toString());
        System.out.println(matcher2.toString());
        Assert.assertTrue(matcher1.equals(matcher2));
    }


    public void batchEvaluationTest() throws IOException {
        File goldstandard = new File(getClass().getClassLoader().getResource("./goldstandard/birth").getFile());
        File batch = new File(getClass().getClassLoader().getResource("./pnml/birth").getFile());
        //run retrospecitve test
        bpm.matcher.Pipeline matcher = new bpm.matcher.Pipeline.Builder().withILP("RELAXED3").Build();
        Pipeline pip = new Pipeline.Builder().withGoldStandard(goldstandard.toPath()).atThreshold(0.7).withBatchPath(batch.toPath()).withBatch().withMatcher(matcher).atThreshold(0.7).build();
        pip.run();
        Path logPath = pip.getLogPath();
        File f =  new File(logPath+"/aggResults.eval");
        BufferedReader reader = new BufferedReader(new FileReader(f));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        Assert.assertTrue(lines == 4);
    }

}
