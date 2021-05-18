package unipi.di.socc.ramp.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import unipi.di.socc.ramp.cli.parser.Parser;
import unipi.di.socc.ramp.cli.parser.PrintingUtilities;
import unipi.di.socc.ramp.core.analyzer.Analyzer;
import unipi.di.socc.ramp.core.analyzer.Plan;
import unipi.di.socc.ramp.core.analyzer.Sequence;
import unipi.di.socc.ramp.core.analyzer.actions.Action;
import unipi.di.socc.ramp.core.analyzer.actions.OpEnd;
import unipi.di.socc.ramp.core.analyzer.actions.OpStart;
import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.Fault;
import unipi.di.socc.ramp.core.model.exceptions.AlreadyUsedIDException;
import unipi.di.socc.ramp.core.model.exceptions.FailedOperationException;
import unipi.di.socc.ramp.core.model.exceptions.InstanceUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.OperationNotAvailableException;
import unipi.di.socc.ramp.core.model.exceptions.RuleNotApplicableException;

public class Main {

    public static void main(String[] args) 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException, IOException, InstanceUnknownException, RuleNotApplicableException, AlreadyUsedIDException, OperationNotAvailableException, FailedOperationException
    {

        String appPath = null;
        String globalStatePath = null;
        String toAnalizePath = null;

        String appPathFile = System.getProperty("user.dir").concat("/data/thinking-app/thinking.json"); 
        String sequencePathFile = System.getProperty("user.dir").concat("/data/thinking-app/deployment/refactored-sequence.json");
        String planPathFile = System.getProperty("user.dir").concat("/data/thinking-app/undeployment/plan.json");
        String gsPathFile = System.getProperty("user.dir").concat("/data/thinking-app/running-globalstate.json");

        Application app = Parser.parseApplication(appPathFile, null);
        Application appGS = Parser.parseApplication(appPathFile, gsPathFile);
        Sequence sequence = Parser.parseSequence(sequencePathFile);
        Plan plan = Parser.parsePlan(planPathFile);
            

        Analyzer analyzer = new Analyzer();

        Sequence actions = new Sequence(plan.getActions());
        if(!analyzer._isValidPlanNew(appGS, plan, new Sequence(),actions.clone()))
            analyzer.printReport();
        else
            System.out.println("valid plan");

        // if(!analyzer.sequenceAnalysis(app, sequence, "--valid"))
        //     analyzer.printReport();
        // else
        //     System.out.println("valid plan");


        // if(!analyzer.planAnalysis(app, plan, "--valid"))
        //     analyzer.printReport();
        // else
        //     System.out.println("valid plan");


        // System.out.println("\n \n \n \n");
        // System.out.println("###########################################################################################");


        // List<Action> failedSeq = analyzer.getReport().getFailedSequence().getActions();

        // for(Action action : failedSeq){

        //     System.out.println("ACTION DA FARE");
        //     PrintingUtilities.printAction(action);
        //     System.out.println("");

            

        //     try {
        //         app.execute(action);
                
        //     } catch (Exception e) {
        //         System.out.println(app.getGlobalState().getResolvableFaults().size());
        //         app.autoreconnect(app.getGlobalState().getResolvableFaults().get(0));
        //         PrintingUtilities.printGlobalState(app.getGlobalState());

        //         app.execute(action);
        //         System.out.println("dopo exectue");
        //         PrintingUtilities.printGlobalState(app.getGlobalState());

        //     }
            
        //     // System.out.println("GLOBAL STATE");
        //     // PrintingUtilities.printGlobalState(app.getGlobalState());


        // }



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
