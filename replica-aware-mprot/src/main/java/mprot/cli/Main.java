package mprot.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mprot.core.analyzer.AnalysisReport;
import mprot.core.analyzer.Analyzer;
import mprot.core.analyzer.execptions.IllegalSequenceElementException;
import mprot.core.analyzer.executableElement.ExecutableElement;
import mprot.core.model.Application;
import mprot.core.model.exceptions.AlreadyUsedIDException;
import mprot.core.model.exceptions.FailedOperationException;
import mprot.core.model.exceptions.InstanceUnknownException;
import mprot.core.model.exceptions.OperationNotAvailableException;
import mprot.core.model.exceptions.RuleNotApplicableException;
import mprot.cli.parsing.Parser;
import mprot.cli.parsing.wrappers.PlanWrapper;

public class Main {
    public static void main(String[] args) 
        throws 
            IOException, 
            NullPointerException, 
            IllegalSequenceElementException,
            IllegalArgumentException, 
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException,
            OperationNotAvailableException,
            AlreadyUsedIDException 
    {

        Application app = null;
        PlanWrapper plan = null;

        String appPath = null; 
        String planPath = null;
        String gsPath = null;
        String propertyToCheck = null;

        if (args.length == 3) {
            appPath = args[0];
            planPath = args[1];
            propertyToCheck = args[2];
        } else if(args.length == 4) {
            appPath = args[0];
            gsPath = args[1];
            planPath = args[2];
            propertyToCheck = args[3];
        } else{
            help();
            return;
        }

        if (gsPath == null) {
            try {
                app = Parser.parseApplication(appPath);
            } catch (IOException e) {
                System.err.println("application: file not found");
                return;
            }
            try {
                plan = Parser.parsePlan(planPath);
            } catch (IOException e) {
                System.err.println("plan: file not found");
                return;
            }
        } else {
            try {
                app = Parser.parseApplication(appPath, gsPath);
            } catch (IOException e) {
                System.err.println("application: file not found");
                return;
            }
            try {
                plan = Parser.parsePlan(planPath);
            } catch (IOException e) {
                System.err.println("plan: file not found");
                return;
            }
        }

        // check if the plan respect the validity asked
        boolean result;
        switch (propertyToCheck) {
            case "valid":
                result = performAnalysis(app, plan, propertyToCheck);
                if (plan.getIsSequence() == true)
                    System.out.println("submitted sequence valid: " + result);
                else
                    System.out.println("submitted plan valid: " + result);
                break;

            case "weaklyvalid":
                result = performAnalysis(app, plan, propertyToCheck);
                if (plan.getIsSequence() == true)
                    System.out.println("submitted sequence weakly valid: " + result);
                else
                    System.out.println("submitted plan weakly valid: " + result);
                break;

            case "notvalid":
                result = performAnalysis(app, plan, propertyToCheck);
                if (plan.getIsSequence() == true)
                    System.out.println("submitted sequence not valid: " + result);
                else
                    System.out.println("submitted plan not valid: " + result);
                break;
            default:
                help("wrong <validity>");
                return;
        }

        // webAppDeploymentUseCase();

    }

    public static void webAppDeploymentUseCase() 
        throws 
            IOException, 
            IllegalArgumentException, 
            NullPointerException,
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException,
            OperationNotAvailableException, 
            AlreadyUsedIDException, 
            IllegalSequenceElementException 
    {
        String appPath = System.getProperty("user.dir")
                .concat("\\replica-aware-mprot\\data\\examples\\web-app\\app.json");
        String deploymentValidPlanPath = System.getProperty("user.dir")
                .concat("\\replica-aware-mprot\\data\\examples\\web-app\\deploymentValidPlan.json");
        String undeploymentValidPlanPath = System.getProperty("user.dir")
                .concat("\\replica-aware-mprot\\data\\examples\\web-app\\undeploymentValidPlan.json");

        Application app = Parser.parseApplication(appPath);
        PlanWrapper depValidPlan = Parser.parsePlan(deploymentValidPlanPath);
        PlanWrapper undepValidPlan = Parser.parsePlan(undeploymentValidPlanPath);

        Collection<ExecutableElement> depPlanEECollection = depValidPlan.getPlanExecutableElements().values();
        List<ExecutableElement> depPlanEElist = new ArrayList<>(depPlanEECollection);

        Collection<ExecutableElement> undepPlanEECollection = undepValidPlan.getPlanExecutableElements().values();
        List<ExecutableElement> undepPlanEElist = new ArrayList<>(undepPlanEECollection);

        System.out.println("APP STRUCTURE");
        PrintingUtilities.printAppStructure(app);
        System.out.println("\n\n");

        System.out.println("BEFORE DEPLOYMENT PLAN");
        PrintingUtilities.printGlobalState(app.getGlobalState());
        System.out.println("\n\n");

        for (ExecutableElement e : depPlanEElist)
            app.execute(e);

        System.out.println("AFTER DEPLOYMENT PLAN");
        PrintingUtilities.printGlobalState(app.getGlobalState());
        System.out.println("\n\n");

        for (ExecutableElement e : undepPlanEElist)
            app.execute(e);

        System.out.println("AFTER UNDEPLOYMENT PLAN");
        PrintingUtilities.printGlobalState(app.getGlobalState());
    }

    public static void help() {
        System.out.println("\t java -jar analyzer.jar <application path> <plan path> <validity>");
        System.out.println("\t java -jar analyzer.jar <application path> <global state path> <plan path> <validity>");
        System.out.println("\t <validity> : valid, weaklyvalid, notvalid");
    }

    public static void help(String error) {
        System.out.println(error);
        help();
    }

    public static boolean performAnalysis(Application app, PlanWrapper plan, String propertyToCheck)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {
        Analyzer analyzer = new Analyzer();

        Collection<ExecutableElement> parsedPlanCollection = plan.getPlanExecutableElements().values();
        List<ExecutableElement> parsedPlanElements = new ArrayList<>(parsedPlanCollection);

        switch (propertyToCheck) {
            case "valid":
                if (plan.getIsSequence() == true) {
                    if(analyzer.isValidSequence(app, parsedPlanElements) == false){
                        errorHandling(analyzer.getAnalysisReport().get(app.getName()));
                        return false;
                    }
                } else {
                    if(analyzer.isValidPlan(app, parsedPlanElements, plan.getConstraints()) == false){
                        errorHandling(analyzer.getAnalysisReport().get(app.getName()));
                        return false;
                    }  
                }
                return true;      
            case "weaklyvalid":
                if (plan.getIsSequence() == true) {
                    if(analyzer.isWeaklyValidSequence(app, parsedPlanElements) == true)
                        return true;
                } else {
                    if(analyzer.isWeaklyValidPlan(app, parsedPlanElements, plan.getConstraints()) == true)
                        return true;
                }
                errorHandling(analyzer.getAnalysisReport().get(app.getName()));
                return false;    
            case "notvalid":
                if (plan.getIsSequence() == true) {
                    if(analyzer.isNotValidSequence(app, parsedPlanElements) == false)
                        return false;
                   
                } else {
                    if(analyzer.isNotValidPlan(app, parsedPlanElements, plan.getConstraints()) == false)
                        return false;
                }
                errorHandling(analyzer.getAnalysisReport().get(app.getName()));
                return true;                 
        }

        return false; //just shuts the ide
    }

    public static void errorHandling(AnalysisReport failRep){
        System.out.println("failed sequence: ");
        for(ExecutableElement e : failRep.getSequence())
            PrintingUtilities.printExecableElement(e);

        System.out.print("\n");
        System.out.println("failed element: ");
        PrintingUtilities.printExecableElement(failRep.getFailedElement());

        System.out.print("\n");
        System.out.println("message error: " + failRep.getFailException().getMessage());
    }
}