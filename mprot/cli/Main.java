package mprot.cli;

import java.io.IOException;
import java.util.Scanner;

import mprot.lib.analyzer.Analyzer;
import mprot.lib.analyzer.execptions.IllegalSequenceElementException;
import mprot.lib.model.Application;
import mprot.lib.model.exceptions.*;

public class Main {
    public static void main(String[] args) throws IOException, NullPointerException, IllegalSequenceElementException,
        InstanceUnknownException, 
        IllegalArgumentException,
        FailedOperationException, 
        RuleNotApplicableException,
        OperationNotAvailableException, 
        AlreadyUsedIDException 
    {

        Scanner keyboard = new Scanner(System.in);

        String input;

        String appSpecFilePath;
        String gsFilePath = null;
        String planFilePath;


        System.out.print("insert the path of the application specific: ");
        appSpecFilePath = keyboard.nextLine();
        System.out.println();

        System.out.print("want to start from a defined global state? (yes / no): ");
        input = keyboard.nextLine();
        System.out.println();

        if (input.equals("yes")) {
            System.out.print("insert the path of the global state: ");
            gsFilePath = keyboard.nextLine();
        }

        System.out.println();

        System.out.print("insert the path of the sequence/plan: ");
        planFilePath = keyboard.nextLine();

        System.out.println();

        Application app;
        if (gsFilePath == null) {
            try {
                app = Parser.parseApplication(appSpecFilePath);
            } catch (IOException e) {
                System.out.print("wrong path");
                keyboard.close();
                return;
            }
        } else {
            try {
                app = Parser.parseApplication(appSpecFilePath, gsFilePath);
            } catch (IOException e) {
                System.out.print("wrong pathh");
                keyboard.close();

                return;
            }
        }

        PlanWrapper plan = null;
        try {
            plan = Parser.parsePlan(planFilePath);
        } catch (IOException e) {
            System.out.print("wrong pathh");
        }

        System.out.print("check validity, weakly-validity or not-validity? ");
        input = keyboard.nextLine();

        System.out.println();


        Analyzer analyzer = new Analyzer();

        switch (input) {
            case "validity":
                if (plan.getIsSequence() == true) {
                    boolean result = false;
                    try {
                        result = analyzer.isValidSequence(app, plan.getPlanExecutableElements());
                    } catch (Exception e) {
                        System.out.println("something in the configuration files is wrong");
                        e.printStackTrace();
                    }

                    if(result == true)
                        System.out.println("sequence is valid: true");
                    else{
                        //TODO qui devi aspettare la chiamata e decidere cosa fare con i fault
                        System.out.println("sequence is valid: false" + analyzer.getFailReports().get(app.getName()).getFailType());
                    }
                }else{
                    boolean result = false;
                    try {
                        result = analyzer.isValidPlan(app, plan.getPlanExecutableElements(), plan.getConstraints());
                    } catch (Exception e) {
                        System.out.println("something in the configuration files is wrong");
                        e.printStackTrace();
                    }

                    if(result == true)
                        System.out.println("plan is valid: true");
                    else{
                        //TODO qui devi aspettare la chiamata e decidere cosa fare con i fault
                        System.out.println("plan is valid: false" + analyzer.getFailReports().get(app.getName()).getFailType());
                    }
                }
                break;
            
            case "weakly-validity":{
                if (plan.getIsSequence() == true) {
                    boolean result = false;
                    try {
                        result = analyzer.isWeaklyValidSequence(app, plan.getPlanExecutableElements());
                    } catch (Exception e) {
                        System.out.println("something in the configuration files is wrong");
                        e.printStackTrace();
                    }

                    if(result == true)
                        System.out.println("sequence is weakly valid: true");
                    else{
                        //TODO qui devi aspettare la chiamata e decidere cosa fare con i fault
                        System.out.println("sequence is weakly valid: false" + analyzer.getFailReports().get(app.getName()).getFailType());
                    }
                }else{
                    boolean result = false;
                    try {
                        result = analyzer.isWeaklyValidPlan(app, plan.getPlanExecutableElements(), plan.getConstraints());
                    } catch (Exception e) {
                        System.out.println("something in the configuration files is wrong");
                        e.printStackTrace();
                    }

                    if(result == true)
                        System.out.println("plan is weakly valid: true");
                    else{
                        //TODO qui devi aspettare la chiamata e decidere cosa fare con i fault
                        System.out.println("plan is  weakly valid: false" + analyzer.getFailReports().get(app.getName()).getFailType());
                    }
                }
                break;
            }
            case "not-validity" : {
                if (plan.getIsSequence() == true) {
                    boolean result = false;
                    try {
                        result = analyzer.isNotValidSequence(app, plan.getPlanExecutableElements());
                    } catch (Exception e) {
                        System.out.println("something in the configuration files is wrong");
                        e.printStackTrace();
                    }

                    if(result == true)
                        System.out.println("sequence is not valid: true");
                    else{
                        //TODO qui devi aspettare la chiamata e decidere cosa fare con i fault
                        System.out.println("sequence is not valid: false" + analyzer.getFailReports().get(app.getName()).getFailType());
                    }
                }else{
                    boolean result = false;
                    try {
                        result = analyzer.isNotValidPlan(app, plan.getPlanExecutableElements(), plan.getConstraints());
                    } catch (Exception e) {
                        System.out.println("something in the configuration files is wrong");
                        e.printStackTrace();
                    }

                    if(result == true)
                        System.out.println("plan is not valid: true");
                    else{
                        //TODO qui devi aspettare la chiamata e decidere cosa fare con i fault
                        System.out.println("plan is  not valid: false" + analyzer.getFailReports().get(app.getName()).getFailType());
                    }
                }
                break;
            }
            default:
                break;
        }

        keyboard.close();

        return;

    }

}