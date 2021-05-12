package unipi.di.socc.ramp.cli.parser;

import unipi.di.socc.ramp.cli.parser.wrappers.*;
import unipi.di.socc.ramp.core.analyzer.Plan;
import unipi.di.socc.ramp.core.analyzer.actions.*;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.ManagementProtocol;
import unipi.di.socc.ramp.core.model.Node;
import unipi.di.socc.ramp.core.model.NodeReq;
import unipi.di.socc.ramp.core.model.Requirement;
import unipi.di.socc.ramp.core.model.Transition;

public class PrintingUtilities {
    
    public static void printPlanOrSequenceWrapper(PlanOrSequenceWrapper planWrap){

        System.out.println("~~~~~~~~~~~~~~~~~ ACTIONS ~~~~~~~~~~~~~~~~~~~~~~");

        for(ActionWrapper action : planWrap.getActions().values()){
            if(action instanceof ScaleOutWrapper){
                ScaleOutWrapper castedActionWrap = (ScaleOutWrapper) action;
                //scaleOut1
                if(castedActionWrap.getContainerID() == null)
                    System.out.println(
                        castedActionWrap.getActionName() + " " + 
                        castedActionWrap.getNodeName() + " " + 
                        castedActionWrap.getIDToAssign()
                    );
                else
                System.out.println(
                    castedActionWrap.getActionName() + " " + 
                    castedActionWrap.getNodeName() + " " + 
                    castedActionWrap.getIDToAssign() + " " +
                    castedActionWrap.getContainerID()
                );
            }
           
            if(action instanceof ScaleInWrapper){
                ScaleInWrapper castedActionWrap = (ScaleInWrapper) action;
                System.out.println(
                    castedActionWrap.getActionName() + " " + 
                    castedActionWrap.getInstanceID()
                );
            }
            if(action instanceof OperationWrapper){
                OperationWrapper castedActionWrap = (OperationWrapper) action;
                System.out.println(
                    castedActionWrap.getActionName() + " " + 
                    castedActionWrap.getInstanceID() + " " + 
                    castedActionWrap.getOpName()
                );
            }
        }

        System.out.println("~~~~~~~~~~~~~~~~~ CONSTRAINTS ~~~~~~~~~~~~~~~~~~~~~~");

        for(ConstraintWrapper contraintWrap : planWrap.getPartialOrderWrap())
            System.out.println(contraintWrap.getBefore() + " -> " + contraintWrap.getAfter());

    }

    public static void printAppStructure(Application app) {
        System.out.println("STATIC BINDINGS: ");
        for(NodeReq key : app.getBindingFunction().keySet())
            System.out.println(
                "(" + key.getNodeName() + ", " + key.getReqName() + ") -> " + 
                "(" + app.getBindingFunction().get(key).getNodeName() + ", " + app.getBindingFunction().get(key).getCap()  + ")");

        System.out.print("\n");

        for(Node node : app.getNodes().values()){
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
            System.out.println("NODE: " + node.getName());

            System.out.print("\n");

            System.out.println("REQUIREMENTS: ");
            for (Requirement r : node.getReqs())
                System.out.println("\t" + r.getName() + " " + r.getSort());

            System.out.print("\n");

            System.out.println("CAPABILITIES: ");
            for (String cap : node.getCaps())
                System.out.println("\t" +cap);

            System.out.print("\n");

            System.out.println("STATES: ");
            for (String state : node.getManProtocol().getStates())
                System.out.println("\t" +state);
            System.out.println("initial state: " + node.getManProtocol().getInitialState());

            System.out.print("\n");

            System.out.println("OPS: ");
            for (String op : node.getOps())
                System.out.println("\t" +op);

            System.out.print("\n");


            ManagementProtocol mp = node.getManProtocol();
        
            System.out.println("TRANSITIONS: ");
            for(Transition t : mp.getTransitions().values()){
                System.out.println("\t" +
                    t.getName() + " " + 
                    t.getStartState() + " " + 
                    t.getOp() + " " + 
                    t.getEndState());
            
            }        
            System.out.print("\n");

            System.out.println("RHO: ");
            for(String s : mp.getStates()){
                System.out.println("state: " + s);

                if(mp.getRho().get(s).isEmpty())
                    System.out.println("\t" + "no reqs");
                else{
                    for(Requirement r : mp.getRho().get(s))
                    System.out.println("\t" + r.getName());
                }
                
            }
            System.out.print("\n");

            System.out.println("GAMMA: ");
            for(String s : mp.getStates()){
                System.out.println("state: " + s);

                if(mp.getGamma().get(s).isEmpty())
                    System.out.println("\t" + "no caps");
                else{
                    for(String cap : mp.getGamma().get(s))
                        System.out.println("\t" + cap);
                }
                
            }

            System.out.print("\n");

            System.out.println("PHI: ");
            for(String s : mp.getStates()){
                System.out.println("state: " + s);

                if(mp.getPhi().get(s).isEmpty())
                    System.out.println("\t" +"no damaged states to go");
                else{
                    for(String fhs : mp.getPhi().get(s))
                    System.out.println("\t" + fhs);
                }
                
            }
        }
    }

    public static void printPartialOrder(Plan plan){
        for(Action before : plan.getPartialOrder().keySet()){

            PrintingUtilities.printAction(before);
            System.out.println(" -> ");

            for(Action after : plan.getPartialOrder().get(before)){
                System.out.print("\t");
                PrintingUtilities.printAction(after);
                System.out.println("");

            }

            System.out.println("\n");


        }
    }

    public static void printAction(unipi.di.socc.ramp.core.analyzer.actions.Action action){
        if(action instanceof ScaleOut1){
            ScaleOut1 castedAction = (ScaleOut1) action;
            System.out.print(
                castedAction.getActionName() + " " + 
                castedAction.getNodeName() + " " + 
                castedAction.getIDToAssign()
            );
        }
        if(action instanceof ScaleOut2){
            ScaleOut2 castedAction = (ScaleOut2) action;
            System.out.print(
                castedAction.getActionName() + " " + 
                castedAction.getNodeName() + " " + 
                castedAction.getIDToAssign() + " " + 
                castedAction.getContainerID()
            );
        }
        if(action instanceof ScaleIn){
            ScaleIn castedAction = (ScaleIn) action;
            System.out.print(
                castedAction.getActionName() + " " + 
                castedAction.getInstanceID()
            );
        }
        if(action instanceof OpStart){
            OpStart castedAction = (OpStart) action;
            System.out.print(
                castedAction.getActionName() + " " + 
                castedAction.getInstanceID() + " " + 
                castedAction.getOpName()
            );
        }
        if(action instanceof OpEnd){
            OpEnd castedAction = (OpEnd) action;
            System.out.print(
                castedAction.getActionName() + " " + 
                castedAction.getInstanceID() + " " + 
                castedAction.getOpName()
            );
        }

    }
}
