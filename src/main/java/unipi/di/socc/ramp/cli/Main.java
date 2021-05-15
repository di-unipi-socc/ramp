package unipi.di.socc.ramp.cli;

import java.io.IOException;

import unipi.di.socc.ramp.cli.parser.Parser;
import unipi.di.socc.ramp.cli.parser.PrintingUtilities;
import unipi.di.socc.ramp.core.analyzer.Plan;
import unipi.di.socc.ramp.core.analyzer.Sequence;
import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;

public class Main {

    public static void main(String[] args) 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException, IOException
    {

        String appPath = null;
        String globalStatePath = null;
        String toAnalizePath = null;






        
        // String property = null;
        // String type = null;   

        // if(args.length == 4){
        //     //no global state given
        //     appPath = args[0];
        //     toAnalizePath = args[1];
        //     type = args[2];
        //     property = args[3];
        // }
        // else if(args.length == 5){
        //     //given global state from where start the analysis
        //     appPath = args[0];
        //     globalStatePath = args[1];
        //     toAnalizePath = args[2];
        //     type = args[3];
        //     property = args[4];
        // }else{
        //     help();
        //     return;
        // }

        // Application app = null;
        // Sequence sequence = null;
        // Plan plan = null;

        // try {
        //     app = Parser.parseApplication(appPath, globalStatePath);
        // } catch (IOException e) {
        //     e.printStackTrace();
        //     return;
        // }

        // if(type.equals("--sequence")){
        //     try {
        //         sequence = Parser.parseSequence(toAnalizePath);
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //         return;
        //     }
        // }
        // else if(type.equals("--plan")){
        //     try {
        //         plan = Parser.parsePlan(toAnalizePath);
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //         return;
        //     }
        // }else{
        //     help();
        //     return;
        // }
         
        // //TODO: weak-valid plan: almeno 1 sequenza valida, valid plan: tutte quante valide, not-valid come sempre (!weakly)

        // //we start the analysis
        // if(sequence != null){}
        //     //Analyzer.performAnalysis(app, sequence, property)
        // else{}
        //     //Analyzer,performAnalysis(app, plan, property);

        
    }

    public static void help(String s) {
        System.out.println("\n" + s);
        help();
    }

    public static void help() {
        System.out.print("\n\n");
        System.out.println(
            "java -jar ramp.jar " + 
            "app-spec.json " + 
            "[global-state.json] " + 
            "plan-or-sequence.json " + 
            "<type> " + 
            "<property>"
        );

        System.out.println("\t <type> : --plan, --sequence");
        System.out.println("\t <property> : --valid, --weakly-valid , --not-valid");

        System.out.print("\n\n");
    }

}
