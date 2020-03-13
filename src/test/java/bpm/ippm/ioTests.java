package bpm.ippm;
import org.junit.Test;

/**
 * IO tests for matcher interface
 */
public class ioTests {

    // not all nets given
    @Test(expected = Error.class)
    public void rejectInputTest1(){
        String args1 = "matcher -n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0 -w LIN -i SYMMETRIC -sys";
        bpm.Main.main(args1.split(" "));
    }

    // not all nets given
    @Test(expected = Error.class)
    public void rejectInputTest2(){
        String args1 = "matcher -n1 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0 -w LIN -i SYMMETRIC -sys";
        bpm.Main.main(args1.split(" "));
    }

    // unknown parameter
    @Test(expected = Error.class)
    public void rejectInputTest3(){
        String args1 = "matcher -n1 ./eval-data/pnml/app_store/app_create_account_comp1.pnml -n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0.5 -p BP -w LIN -i SYMMETRIC -sys -rd";
        bpm.Main.main(args1.split(" "));
    }

    // non exisiting mode
    @Test(expected = Error.class)
    public void rejectInputTest4(){
        String args1 = "matching -n1 ./eval-data/pnml/app_store/app_create_account_comp1.pnml -n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0.5 -p BP -w LIN -i SYMMETRIC -sys";
        bpm.Main.main(args1.split(" "));
    }

    // non exisiting net
    @Test(expected = Error.class)
    public void rejectInputTest5(){
        String args1 = "matcher -n1 ./eval-data/pnml/app_store/non-existing.pnml -n2 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0.5 -p BP -w LIN -i SYMMETRIC -sys";
        bpm.Main.main(args1.split(" "));
    }

    // non exisiting net
    @Test(expected = Error.class)
    public void rejectInputTest6(){
        String args1 = "matcher -n2 ./eval-data/pnml/app_store/non-existing.pnml -n1 ./eval-data/pnml/app_store/app_create_account_comp2.pnml -s 0.5 -p BP -w LIN -i SYMMETRIC -sys";
        bpm.Main.main(args1.split(" "));
    }

    // no log file found
    @Test(expected = Error.class)
     public void rejectInputTest7(){
        String args1 = "matcher -n1 ./eval-data/pnml/bpi15/BPIC15_2_07_OPS.pnml -l1 ./eval-data/xes/bpi15/non-exisiting.xes -n2 ./eval-data/pnml/bpi15/BPIC15_4_07_OPS.pnml -l2 ./eval-data/xes/bpi15/BPIC15_4_07_OPS.xes -s 0.5 -p LOG_EF -w LIN -i CUSTOM_IDENTIFICATION -sys";
        bpm.ippm.matcher.Matcher.main(args1.split(" "));
    }

    // no log file found
    @Test(expected = Error.class)
    public void rejectInputTest8(){
        String args1 = "matcher -n1 ./eval-data/pnml/bpi15/BPIC15_2_07_OPS.pnml -l2 ./eval-data/xes/bpi15/non-exisiting.xes -n2 ./eval-data/pnml/bpi15/BPIC15_4_07_OPS.pnml -l1 ./eval-data/xes/bpi15/BPIC15_4_07_OPS.xes -s 0.5 -p LOG_EF -w LIN -i CUSTOM_IDENTIFICATION -sys";
        bpm.Main.main(args1.split(" "));
    }

    // weight over 1
    @Test(expected = Error.class)
    public void rejectInputTest9(){
        String args1 = "matcher -n1 ./eval-data/pnml/bpi15/BPIC15_2_07_OPS.pnml -l2 ./eval-data/xes/bpi15/non-exisiting.xes -n2 ./eval-data/pnml/bpi15/BPIC15_4_07_OPS.pnml -l1 ./eval-data/xes/bpi15/BPIC15_4_07_OPS.xes -s 1.1 -p LOG_EF -w LIN -i CUSTOM_IDENTIFICATION -sys";
        bpm.Main.main(args1.split(" "));
    }

    // weight lower 0
    @Test(expected = Error.class)
    public void rejectInputTest10(){
        String args1 = "matcher -n1 ./eval-data/pnml/bpi15/BPIC15_2_07_OPS.pnml -l2 ./eval-data/xes/bpi15/non-exisiting.xes -n2 ./eval-data/pnml/bpi15/BPIC15_4_07_OPS.pnml -l1 ./eval-data/xes/bpi15/BPIC15_4_07_OPS.xes -s -1 -p LOG_EF -w LIN -i CUSTOM_IDENTIFICATION -sys";
        bpm.Main.main(args1.split(" "));
    }

    // non existing profile
    @Test(expected = Error.class)
    public void rejectInputTest11(){
        String args1 = "matcher -n1 ./eval-data/pnml/bpi15/BPIC15_2_07_OPS.pnml -l2 ./eval-data/xes/bpi15/non-exisiting.xes -n2 ./eval-data/pnml/bpi15/BPIC15_4_07_OPS.pnml -l1 ./eval-data/xes/bpi15/BPIC15_4_07_OPS.xes -s 0.5 -p NOT-EXISTING -w LIN -i CUSTOM_IDENTIFICATION -sys";
        bpm.Main.main(args1.split(" "));
    }

    // non existing word sim
    @Test(expected = Error.class)
    public void rejectInputTest12(){
        String args1 = "matcher -n1 ./eval-data/pnml/bpi15/BPIC15_2_07_OPS.pnml -l2 ./eval-data/xes/bpi15/non-exisiting.xes -n2 ./eval-data/pnml/bpi15/BPIC15_4_07_OPS.pnml -l1 ./eval-data/xes/bpi15/BPIC15_4_07_OPS.xes -s 0.5 -p BP -w NOT-EXISTING -i CUSTOM_IDENTIFICATION -sys";
        bpm.Main.main(args1.split(" "));
    }

    // positive example that doesn't throw an error
    @Test
    public void trueExample(){
        String args1 = "matcher -n1 ./eval-data/pnml/bpi15/BPIC15_2_07_OPS.pnml -l1 ./eval-data/xes/bpi15/BPIC15_2_07_OPS.xes -n2 ./eval-data/pnml/bpi15/BPIC15_4_07_OPS.pnml -l2 ./eval-data/xes/bpi15/BPIC15_4_07_OPS.xes -s 0.5 -p LOG_EF -w LIN -i CUSTOM_IDENTIFICATION -tl 1";
        bpm.Main.main(args1.split(" "));
    }


}
