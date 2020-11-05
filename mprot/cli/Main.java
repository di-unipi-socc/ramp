package mprot.cli;

import java.io.IOException;

import mprot.core.analyzer.AnalysisFailReport;
import mprot.core.analyzer.Analyzer;
import mprot.core.model.Application;

import mprot.cli.parsing.Parser;
import mprot.cli.parsing.wrappers.PlanWrapper;

public class Main {
    public static void main(String[] args) throws IOException {

        return;

    }


    public static void cli(String[] args) {

        if (args.length < 3 || args.length > 5) {
            help();
            return;
        }

        Application app = null;
        PlanWrapper plan = null;

        // analyzing with an empty global state
        if (args.length == 3) {
            try {
                app = Parser.parseApplication(args[0]);
            } catch (IOException e) {
                System.err.println("application: file not found");
                return;
            }

            try {
                plan = Parser.parsePlan(args[1]);
            } catch (IOException e) {
                System.err.println("plan: file not found");
                return;
            }

            if(performAnalysis(app, plan, args[2]) == true){
                args[2] = args[2].replaceAll("-", "");

                if(plan.getIsSequence())
                    System.out.println("sequence is " + args[2] + ": " + "true");
                else
                    System.out.println("plan is " + args[2] + ": " + "true");
            }
        }

        // analyzing with a starting global state
        if (args.length == 4) {
            try {
                app = Parser.parseApplication(args[0], args[1]);
            } catch (IOException e) {
                System.err.println("application: file not found");
                return;
            }

            try {
                plan = Parser.parsePlan(args[2]);
            } catch (IOException e) {
                System.err.println("plan: file not found");
                return;
            }

            if(performAnalysis(app, plan, args[3]) == true){
                args[3] = args[3].replaceAll("-", "");

                if(plan.getIsSequence())
                    System.out.println("sequence is " + args[3] + ": " + "true");
                else
                    System.out.println("plan is " + args[3] + ": " + "true");
            }
        }
    }

    public static void help() {
        System.out.println("java -jar analyzer.jar <application path> <plan path> <validity>");
        System.out.println("java -jar analyzer.jar <application path> <global state path> <plan path> <validity>");
        System.out.println("<validity> : --valid, --weaklyvalid, --notvalid");
    }

    public static void help(String error) {
        System.out.println(error);
        help();
    }

    public static boolean performAnalysis(Application app, PlanWrapper plan, String propertyToCheck) {
        Analyzer analyzer = new Analyzer();

        switch (propertyToCheck) {
            case "--valid":
                if (plan.getIsSequence() == true) {
                    try {
                        return analyzer.isValidSequence(app, plan.getPlanExecutableElements());
                    } catch (Exception e) {
                        errorHandling(analyzer.getFailReports().get(app.getName()));
                        System.exit(0);
                    }
                } else {
                    try {
                        return analyzer.isValidPlan(app, plan.getPlanExecutableElements(), plan.getConstraints());
                    } catch (Exception e) {
                        errorHandling(analyzer.getFailReports().get(app.getName()));
                        System.exit(0);
                    }
                }        
            
            case "--weaklyvalid":
            if (plan.getIsSequence() == true) {
                try {
                    return analyzer.isWeaklyValidSequence(app, plan.getPlanExecutableElements());
                } catch (Exception e) {
                    errorHandling(analyzer.getFailReports().get(app.getName()));
                    System.exit(0);
                }
            } else {
                try {
                    return analyzer.isWeaklyValidPlan(app, plan.getPlanExecutableElements(), plan.getConstraints());
                } catch (Exception e) {
                    errorHandling(analyzer.getFailReports().get(app.getName()));
                    System.exit(0);
                }
            }    
                
            case "--notvalid":
            if (plan.getIsSequence() == true) {
                try {
                    return analyzer.isNotValidSequence(app, plan.getPlanExecutableElements());
                } catch (Exception e) {
                    errorHandling(analyzer.getFailReports().get(app.getName()));
                    System.exit(0);
                }
            } else {
                try {
                    return analyzer.isNotValidPlan(app, plan.getPlanExecutableElements(), plan.getConstraints());
                } catch (Exception e) {
                    errorHandling(analyzer.getFailReports().get(app.getName()));
                    System.exit(0);
                }
            }    
            default: {
                help("wrong <validity>");
                System.exit(0);
            }
        }

        return false; //shuts the ide warning

    }

    public  static void errorHandling(AnalysisFailReport failRep){

    }

}