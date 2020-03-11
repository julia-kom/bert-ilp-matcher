package bpm;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Main function to call. Forwards arguments to the chosen main function.
 */
public class Main {

    /**
     * Choose the running mode of the jar:
     * eval: Use the Evaluation modus. Main jar is bpm.evaluation.Evaluation
     *       Contains all classes for evaluating the ilp matcher approach
     * matcher: Use the Matcher modus. Main jar is bpm.ippm.matcher.Matcher
     *       Pure Model Matching tool. Noe evaluation. Proceses in, Alignment out.
     *
     * @param args eval/machter <args-for-the-respective-main>
     */
    public static void main(String[] args){
        if(args.length < 1){
            throw new Error("Please Select a mode: eval or matcher");
        }

        String[] newArgs = ArrayUtils.subarray(args,1,args.length);
        String modus = args[0];

        if(modus.equals("eval")){
            bpm.evaluation.Evaluation.main(newArgs);
        }else if(modus.equals("matcher")){
            bpm.ippm.matcher.Matcher.main(newArgs);
        }else{
            throw new Error("Mode " + args[0] +" does not exist. Either use matcher or eval.");
        }

    }

}
