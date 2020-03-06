package bpm.evaluation;

import org.junit.Test;

public class ioTest {
    // batch  no gsp
    @Test(expected = Error.class)
    public void rejectInputTest1(){
        String args1 = "eval -batch -net-path ./eval-data/pnml/bpi15 -log-path ./eval-data/xes/bpi15  -ilp BASIC5 -p LOG_DF -sys -s 0.1 -tl 10";
        bpm.Main.main(args1.split(" "));
    }

    // batch no net path
    @Test(expected = Error.class)
    public void rejectInputTest2(){
        String args1 = "eval -batch -gold-standard-path ./eval-data/goldstandard/bpi15  -log-path ./eval-data/xes/bpi15  -ilp BASIC5 -p LOG_DF -sys -s 0.1 -tl 10";
        bpm.Main.main(args1.split(" "));
    }

    // no gsp existing
    @Test(expected = Error.class)
    public void rejectInputTest3(){
        String args1 = "eval -batch -gold-standard-path ./eval-data/goldstandard/noex -net-path ./eval-data/pnml/bpi15 -log-path ./eval-data/xes/bpi15  -ilp BASIC5 -p LOG_DF -sys -s 0.1 -tl 10";
        bpm.Main.main(args1.split(" "));
    }

    // no netpaht existing
    @Test(expected = Error.class)
    public void rejectInputTest4(){
        String args1 = "eval -batch -gold-standard-path ./eval-data/goldstandard/bpi15 -net-path ./eval-data/pnml/noex -log-path ./eval-data/xes/bpi15  -ilp BASIC5 -p LOG_DF -sys -s 0.1 -tl 10";
        bpm.Main.main(args1.split(" "));
    }

    // no log path  existing
    @Test(expected = Error.class)
    public void rejectInputTest5(){
        String args1 = "eval -batch -gold-standard-path ./eval-data/goldstandard/noex -net-path ./eval-data/pnml/bpi15 -log-path ./eval-data/xes/noex  -ilp BASIC5 -p LOG_DF -sys -s 0.1 -tl 10";
        bpm.Main.main(args1.split(" "));
    }

    // sim larger 1
    @Test(expected = Error.class)
    public void rejectInputTest6(){
        String args1 = "eval -batch -gold-standard-path ./eval-data/goldstandard/bpi15 -net-path ./eval-data/pnml/bpi15 -log-path ./eval-data/xes/bpi15  -ilp BASIC5 -p LOG_DF -sys -s 1.1 -tl 10";
        bpm.Main.main(args1.split(" "));
    }

    // wrong profile
    @Test(expected = Error.class)
    public void rejectInputTest7(){
        String args1 = "eval -batch -gold-standard-path ./eval-data/goldstandard/bpi15 -net-path ./eval-data/pnml/bpi15 -log-path ./eval-data/xes/bpi15  -ilp BASIC5 -p NOEX -sys -s 0.1 -tl 10";
        bpm.Main.main(args1.split(" "));
    }

    //wrong ILP
    @Test(expected = Error.class)
    public void rejectInputTest8(){
        String args1 = "eval -batch -gold-standard-path ./eval-data/goldstandard/bpi15 -net-path ./eval-data/pnml/bpi15 -log-path ./eval-data/xes/bpi15  -ilp NOEX -p LOG_DF -sys -s 0.1 -tl 10";
        bpm.Main.main(args1.split(" "));
    }

    // wrong word sim
    @Test(expected = Error.class)
    public void rejectInputTest9(){
        String args1 = "eval -batch -gold-standard-path ./eval-data/goldstandard/bpi15 -net-path ./eval-data/pnml/bpi15 -log-path ./eval-data/xes/bpi15 -w NoEX -ilp BASIC5 -p LOG_DF -sys -s 0.1 -tl 10";
        bpm.Main.main(args1.split(" "));
    }

    // retrospective no result path
    @Test(expected = Error.class)
    public void rejectInputTest11(){
        String args1 = "eval -retrospective -gold-standard-path ./eval-data/goldstandard/sap_original -pp 0.3";
        bpm.Main.main(args1.split(" "));
    }

    // retrospective no result path exist
    @Test(expected = Error.class)
    public void rejectInputTest12(){
        String args1 = "eval -retrospective -gold-standard-path ./eval-data/goldstandard/sap_original -result-path ./eval-data/noEx -pp 0.3";
        bpm.Main.main(args1.split(" "));
    }
}
