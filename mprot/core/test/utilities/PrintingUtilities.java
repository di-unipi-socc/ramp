package mprot.core.test.utilities;

import java.util.ArrayList;
import java.util.Collection;

import mprot.cli.parsing.ConstraintLabel;
import mprot.cli.parsing.wrappers.*;

import mprot.core.analyzer.Constraint;
import mprot.core.analyzer.executable_element.ExecutableElement;
import mprot.core.analyzer.executable_element.OpEnd;
import mprot.core.analyzer.executable_element.OpStart;
import mprot.core.analyzer.executable_element.ScaleIn;
import mprot.core.analyzer.executable_element.ScaleOut1;
import mprot.core.analyzer.executable_element.ScaleOut2;
import mprot.core.model.Application;
import mprot.core.model.GlobalState;
import mprot.core.model.ManagementProtocol;
import mprot.core.model.NodeInstance;
import mprot.core.model.Requirement;
import mprot.core.model.RuntimeBinding;
import mprot.core.model.StaticBinding;

public class PrintingUtilities {

    public static void printGlobalState(GlobalState gs){
        PrintingUtilities.printActiveNodes(gs);
        PrintingUtilities.printRuntimeBindings(gs);
    }


    public static void printActiveNodes(GlobalState gs){
        System.out.println("ALL ACTIVE INSTANCES (G SET)");

        Collection<NodeInstance> activeInstancesCollection =  gs.getActiveNodeInstances().values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);
    
        for (NodeInstance instance : activeInstances)
            System.out.printf("\t" + "%-15s" + "%-15s" + "%-15s" + "\n", instance.getNodeType().getName(), instance.getID(), instance.getCurrentState());
        
        System.out.println("\n");
    }

    public static void printRuntimeBindings(GlobalState gs) {

        System.out.println("ALL RUNTIME BINDINGS");

        Collection<NodeInstance> activeInstancesCollection = gs.getActiveNodeInstances().values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);

        for (NodeInstance instance : activeInstances) {
            ArrayList<RuntimeBinding> runtimeBindings = (ArrayList<RuntimeBinding>) gs.getRuntimeBindings().get(instance.getID());

            System.out.println(instance.getID());
            if (runtimeBindings.size() == 0)
                System.out.println("     " + "none");

            for (RuntimeBinding binding : runtimeBindings) 
                System.out.printf("\t" + "%-10s" + "%-10s" + "\n", binding.getReq().getName(), binding.getNodeInstanceID());            
        }

        System.out.println("\n");
    }

    public static void printStaticApp(Application app) {

        System.out.println("STATIC BINDINGS: ");
        for(StaticBinding key : app.getBindingFunction().keySet())
            System.out.println(
                "<" + key.getNodeName() + ", " + key.getCapOrReq() + "> " + 
                "<" + app.getBindingFunction().get(key).getNodeName() + ", " + app.getBindingFunction().get(key).getCapOrReq()  + ">");

        System.out.print("\n");

        for(String nodeName : app.getNodes().keySet()){
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
            System.out.println("NODE: " + app.getNodes().get(nodeName).getName());
            System.out.println("initial state: " + app.getNodes().get(nodeName).getInitialState());

            System.out.print("\n");

            System.out.println("REQUIREMENTS: ");
            for (Requirement r : app.getNodes().get(nodeName).getReqs())
                System.out.println("\t" + r.getName() + " " + r.getRequirementSort());

            System.out.print("\n");

            System.out.println("CAPABILITIES: ");
            for (String cap : app.getNodes().get(nodeName).getCaps())
                System.out.println("\t" +cap);

            System.out.print("\n");

            System.out.println("STATES: ");
            for (String state : app.getNodes().get(nodeName).getStates())
                System.out.println("\t" +state);

            System.out.print("\n");

            System.out.println("OPS: ");
            for (String op : app.getNodes().get(nodeName).getOps())
                System.out.println("\t" +op);

            System.out.print("\n");


            ManagementProtocol mp = app.getNodes().get(nodeName).getManagementProtocol();
        
            System.out.println("TRANSITIONS: ");
            for(String t : mp.getTransition().keySet()){
                System.out.println("\t" +
                    mp.getTransition().get(t).getName() + " " + 
                    mp.getTransition().get(t).getStartingState() + " " + 
                    mp.getTransition().get(t).getOp() + " " + 
                    mp.getTransition().get(t).getEndingState());
            
            }        
            System.out.print("\n");

            System.out.println("RHO: ");
            for(String s : app.getNodes().get(nodeName).getStates()){
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
            for(String s : app.getNodes().get(nodeName).getStates()){
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
            for(String s : app.getNodes().get(nodeName).getStates()){
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

    public static void printExecutablePlan(PlanWrapper plan){
        for(ExecutableElement element : plan.getPlanExecutableElements().values()){
            if(element instanceof ScaleOut1){
                ScaleOut1 op = (ScaleOut1) element;
                System.out.println(op.getRule() + ": " +op.getNodeName() + " " + op.getIDToAssign());
            }
            if(element instanceof ScaleOut2){
                ScaleOut2 op = (ScaleOut2) element;
                System.out.println(op.getRule() + ": " +op.getNodeName() + " " + op.getIDToAssign() + " " + op.getContainerID());
            }
            if(element instanceof OpStart){
                OpStart op = (OpStart) element;
                System.out.println(op.getRule() + ": " +op.getInstnaceID() + " " + op.getOp());
            }
            if(element instanceof OpEnd){
                OpEnd op = (OpEnd) element;
                System.out.println(op.getRule() + ": " + op.getInstanceID() + " " + op.getOp());
            }
            if(element instanceof ScaleIn){
                ScaleIn op = (ScaleIn) element;
                System.out.println(op.getRule() + ": " +op.getInstanceID());
            }
        }

        if(plan.getIsSequence() == false){
            //printing the constraints
            for(ConstraintLabel label : plan.getConstraintsLables())
                System.out.println(label.getBefore() + " must be executed before " + label.getAfter());

            for(Constraint c : plan.getConstraints())
                System.out.println(c.getBefore() + " must be executed before " + c.getAfter());
        }

    }

}
