package test.EndToEndTest;

import java.util.ArrayList;
import java.util.Collection;

import model.*;
import model.exceptions.FailedFaultHandlingExecption;
import model.exceptions.FailedOperationException;
import model.exceptions.NodeUnknownException;
import model.exceptions.OperationNotAvailableException;
import model.exceptions.RuleNotApplicableException;
import test.ThesisAppFactory;

public class Main {

    public static void main(String[] args) 
        throws 
            NullPointerException, 
            RuleNotApplicableException,
            NodeUnknownException, 
            IllegalArgumentException, 
            OperationNotAvailableException, 
            FailedOperationException,
            FailedFaultHandlingExecption 
    {
        System.out.println("textbook test");
        testbookAppStart();

        System.out.println("\n\n\n tricky test");
        errorsTest();
    }

    public static void printRuntimeBindings(Application app) {

        System.out.println("ALL RUNTIME BINDINGS");

        Collection<NodeInstance> activeInstancesCollection = app.getGlobalState().getActiveNodeInstances().values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);

        for (NodeInstance instance : activeInstances) {
            ArrayList<RuntimeBinding> runtimeBindings = (ArrayList<RuntimeBinding>) app.getGlobalState().getRuntimeBindings().get(instance.getID());
           
            System.out.println("runtime bindings of: " + instance.getNodeType().getName() + " " + instance.getID());
            if (runtimeBindings.size() == 0)
                System.out.println("\tnone");

            for (RuntimeBinding binding : runtimeBindings) {
                System.out.println("\t <"
                        + binding.getReq().getName() + "; " + app.getGlobalState().getActiveNodeInstances()
                                .get(binding.getNodeInstanceID()).getNodeType().getName()
                        + " " + binding.getNodeInstanceID() + ">");
            }
            System.out.print("\n");

        }

        System.out.println("\n");
    }

    public static void testbookAppStart() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException,
            IllegalArgumentException, 
            OperationNotAvailableException, 
            FailedOperationException 
    {
        Application app = ThesisAppFactory.createApplication();
        Node frontend = app.getNodes().get("frontend");
        Node backend = app.getNodes().get("backend");
        Node node = app.getNodes().get("node");
        Node mongo = app.getNodes().get("mongo");

        NodeInstance nodeN3;
        NodeInstance nodeN2;
        NodeInstance nodeN1;
        NodeInstance mongoM1;
        NodeInstance frontendF1;
        NodeInstance backendB1;
        NodeInstance backendB2;

        System.out.println("before start, no active nodes");

        printActiveNodes(app);

        mongoM1 = app.scaleOut1(mongo);
        nodeN1 = app.scaleOut1(node);
        nodeN2 = app.scaleOut1(node);
        nodeN3 = app.scaleOut1(node);
        frontendF1 = app.scaleOut2(frontend, nodeN3);
        backendB1 = app.scaleOut2(backend, nodeN1);
        backendB2 = app.scaleOut2(backend, nodeN2);

        System.out.println("scaled out all nodes, 3 of them have a containment requirement");

        printActiveNodes(app);
        printRuntimeBindings(app);

        System.out.println("all instances going in working states");

        // server instance goes in states in which they offer caps
        app.opStart(nodeN1, "start");
        app.opEnd(nodeN1, "start");

        app.opStart(nodeN2, "start");
        app.opEnd(nodeN2, "start");

        app.opStart(nodeN3, "start");
        app.opEnd(nodeN3, "start");

        app.opStart(mongoM1, "start");
        app.opEnd(mongoM1, "start");

        // asking instances go
        app.opStart(backendB1, "install");
        app.opEnd(backendB1, "install");

        app.opStart(backendB1, "start");
        app.opEnd(backendB1, "start");

        app.opStart(backendB2, "install");
        app.opEnd(backendB2, "install");

        app.opStart(backendB2, "start");
        app.opEnd(backendB2, "start");

        app.opStart(frontendF1, "install");
        app.opEnd(frontendF1, "install");

        app.opStart(frontendF1, "config");
        app.opEnd(frontendF1, "config");

        app.opStart(frontendF1, "start");
        app.opEnd(frontendF1, "start");

        System.out.println("all instances in working states, runtime bindings accordingly");

        printActiveNodes(app);
        printRuntimeBindings(app);

        System.out.println("scale in nodeN1, that destroy nodeN1 and frontendF1");
        app.scaleIn(nodeN1);

        printActiveNodes(app);
        printRuntimeBindings(app);

        System.out.println("scale in everything, no active instances");

        app.scaleIn(nodeN2);
        app.scaleIn(mongoM1);
        app.scaleIn(frontendF1);
        app.scaleIn(nodeN3);

        printActiveNodes(app);
        printRuntimeBindings(app);
    }

    public static void errorsTest()
            throws
                NullPointerException, 
                RuleNotApplicableException, 
                NodeUnknownException, 
                IllegalArgumentException,
                FailedOperationException, 
                OperationNotAvailableException, 
                FailedFaultHandlingExecption 
    {

        Application app = ThesisAppFactory.createApplication();
        Node frontend = app.getNodes().get("frontend");
        Node backend = app.getNodes().get("backend");
        Node node = app.getNodes().get("node");
        Node mongo = app.getNodes().get("mongo");

        NodeInstance nodeN3;
        NodeInstance nodeN2;
        NodeInstance nodeN1;
        NodeInstance mongoM1;
        NodeInstance frontendF1;
        NodeInstance backendB1;
        NodeInstance backendB2;

        System.out.println("before start, no active nodes");

        printActiveNodes(app);
        printRuntimeBindings(app);

        mongoM1 = app.scaleOut1(mongo);
        nodeN1 = app.scaleOut1(node);
        nodeN2 = app.scaleOut1(node);
        nodeN3 = app.scaleOut1(node);
        frontendF1 = app.scaleOut2(frontend, nodeN3);
        backendB1 = app.scaleOut2(backend, nodeN1);
        backendB2 = app.scaleOut2(backend, nodeN2);

        System.out.println("scaled out all nodes");

        printActiveNodes(app);
        printRuntimeBindings(app);
    
        /**
         * test 1: frontendF1 has the requirement "host" that is provieded by the instance nodeN3 whent
         * it is in the state "running". 
         * Since nodeN3 is not in "running" the op fails and opEnd raise a FailedOpException. 
         * One catched the exception the nodeN3 go in the running state, so the operation on frontendF1 
         * can be completed
         */

        app.opStart(frontendF1, "install");
        try { //fails because nodeN3 is not offering the right cap
            app.opEnd(frontendF1, "install");
        } catch (FailedOperationException e) {
            app.opStart(nodeN3, "start");
            app.opEnd(nodeN3, "start");
            app.opEnd(frontendF1, "install");
        };

        /**
         * test2: once again nodeN3 is stopped. frontendF1 tries to do the "uninstall" operation, which requires
         * "host". 
         * Since nodeN3 do not offer host in the "stopped" state the operation fails. This time the fail is not
         * resolved explicitly but we let the fault handler kick in.
         */
        
        app.opStart(nodeN3, "stop");
        app.opEnd(nodeN3, "stop");

        app.opStart(frontendF1, "uninstall");
        try { 
            app.opEnd(frontendF1, "uninstall");
        } catch (FailedOperationException e) {
            //the fault handler now put frontendF1 in the right fault handling state
            ArrayList<Fault> faultsList = (ArrayList<Fault>) app.getGlobalState().getPendingFaults(frontendF1);
            
            //faults list has just one member
            if(faultsList.size() != 1 || faultsList.get(0).getInstanceID().equals(frontendF1.getID()) == false)
                System.out.println("error");
            
            if(app.getGlobalState().getResolvableFaults(frontendF1).size() != 0)
                System.out.println("error");

            app.fault(frontendF1, faultsList.get(0).getReq());

            //now frontendF1 should be in the fault handling state, which is "damaged"
            System.out.println("faulted instance: " + frontendF1.getID());

            printActiveNodes(app); 
        }

        /**
         * test3: for the operation "config" frontendF1 requires "host" and "conn". 
         * "conn" is provided by a backend node, but neither backendB1 nor backendB2 is offering the cap.
         * this leads to a pending fault. Then we make backendB2 go in the "running" state in which it offer
         * the right cap, we see that the pending fault is now a resolvable one, and then it is resolved by
         * autoreconnect
         */

        app.scaleIn(frontendF1);
        app.scaleIn(nodeN2);
        app.scaleIn(nodeN3);
        app.scaleIn(backendB2);
        nodeN2 = app.scaleOut1(node);
        nodeN3 = app.scaleOut1(node);
        backendB2 = app.scaleOut2(backend, nodeN2);

        //needed for backendB2
        app.opStart(nodeN2, "start");
        app.opEnd(nodeN2, "start");

        app.opStart(mongoM1, "start");
        app.opEnd(mongoM1, "start");

        //needed for frontendF1
        app.opStart(nodeN3, "start");
        app.opEnd(nodeN3, "start");

        frontendF1 = app.scaleOut2(frontend, nodeN3);

        app.opStart(frontendF1, "install");
        app.opEnd(frontendF1, "install");

        app.opStart(frontendF1, "config");
        try {
            app.opEnd(frontendF1, "config"); //fails because "conn" is needed but backendB1 is not offering
        } catch (FailedOperationException e) {
            
            //at this time this fault is a pending, non resolvable, fault
            if(app.getGlobalState().getPendingFaults(frontendF1).size() != 1 && app.getGlobalState().getResolvableFaults(frontendF1).size() != 0)
                System.out.println("error 257");
            
            //we put backendB2 in the state where it offers "conn" and see that a 
            //pending non resolvable fault become resolvable (conn is replica unaw)
            app.opStart(backendB2, "install");
            app.opEnd(backendB2, "install");

            app.opStart(backendB2, "start");
            app.opEnd(backendB2, "start");

            //now backendB2 offer conn, so frontendF1 has a resolvable Fault
            if(app.getGlobalState().getPendingFaults(frontendF1).size() != 1 && app.getGlobalState().getResolvableFaults(frontendF1).size() != 1)
                System.out.println("error 268");

            //now we fix the error
            Fault f = app.getGlobalState().getResolvableFaults(frontendF1).get(0);
            app.autoreconnect(frontendF1, f.getReq());

            //and complete the op
            app.opEnd(frontendF1, "config"); 
            
            //now frontendF1 is in the "configured" state 
            printActiveNodes(app);
            printRuntimeBindings(app);
        }
    }

    public static void printActiveNodes(Application app){

        System.out.println("ALL ACTIVE INSTANCES (G SET)");

        Collection<NodeInstance> activeInstancesCollection =  app.getGlobalState().getActiveNodeInstances().values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);
        
        for (NodeInstance instance : activeInstances) {
            System.out.println("\t" + instance.getNodeType().getName() + " " + instance.getID() + ";\t" + instance.getCurrentState());
        }

        System.out.println("\n");

    }




}