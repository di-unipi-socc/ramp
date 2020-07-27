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
import test.UnitTest.ApplicationTest.AutoreconnectTest;

public class Main {

    public static void main(String[] args) throws NullPointerException, RuleNotApplicableException,
            NodeUnknownException, IllegalArgumentException, OperationNotAvailableException, FailedOperationException,
            FailedFaultHandlingExecption {

        // testbookAppStart();

        errorsTest();
    }

    public static void printRuntimeBindings(Application app) {

        System.out.println("ALL RUNTIME BINDINGS");

        Collection<NodeInstance> activeInstancesCollection = app.getGlobalState().getActiveNodeInstances().values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);

        for (NodeInstance instance : activeInstances) {
            ArrayList<RuntimeBinding> runtimeBindings = (ArrayList<RuntimeBinding>) app.getGlobalState()
                    .getRuntimeBindings().get(instance.getID());
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

    public static void testbookAppStart() throws NullPointerException, RuleNotApplicableException, NodeUnknownException,
            IllegalArgumentException, OperationNotAvailableException, FailedOperationException {

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

        System.out.println("before start");

        printActiveNodes(app);

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

        printActiveNodes(app);
        printRuntimeBindings(app);

        System.out.println("scale in nodeN1");
        app.scaleIn(nodeN1);

        printActiveNodes(app);
        printRuntimeBindings(app);

        System.out.println("scale in everything");
        app.scaleIn(nodeN2);
        app.scaleIn(mongoM1);
        app.scaleIn(frontendF1);
        app.scaleIn(nodeN3);

        printActiveNodes(app);
        printRuntimeBindings(app);
    }

    public static void errorsTest()
            throws NullPointerException, RuleNotApplicableException, NodeUnknownException, IllegalArgumentException,
            FailedOperationException, OperationNotAvailableException, FailedFaultHandlingExecption {
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

        System.out.println("before start");

        printActiveNodes(app);
        printRuntimeBindings(app);

        mongoM1 = app.scaleOut1(mongo);
        nodeN1 = app.scaleOut1(node);
        nodeN2 = app.scaleOut1(node);
        nodeN3 = app.scaleOut1(node);
        frontendF1 = app.scaleOut2(frontend, nodeN3);
        backendB1 = app.scaleOut2(backend, nodeN1);
        backendB2 = app.scaleOut2(backend, nodeN2);

        printActiveNodes(app);
        printRuntimeBindings(app);

        app.opStart(frontendF1, "install");
        try {
            app.opEnd(frontendF1, "install");
        } catch (FailedOperationException e) {
            System.out.println(e.getMessage());
            app.opStart(nodeN3, "start");
            app.opEnd(nodeN3, "start");
            app.opEnd(frontendF1, "install");
        };

        printActiveNodes(app);
        printRuntimeBindings(app);

        app.opStart(frontendF1, "config");
        try {
            app.opEnd(frontendF1, "config"); //this one fails, backendB1 is not offering "conn"
        } catch (Exception e) {
            //e.printStackTrace();
            
            //now we run fault handling
            ArrayList<Fault> faultsList = (ArrayList<Fault>) app.getGlobalState().getPendingFaults(frontendF1);
            //faults list has just one member (but anyway)
            if(faultsList.size() != 1 || faultsList.get(0).getInstanceID().equals(frontendF1.getID()) == false)
                System.out.println("ERRORE");

            app.fault(frontendF1, faultsList.get(0).getReq());

            //now frontendF1 should be in the fault handling state, which is "installed"
            System.out.println("faulted instance: " + frontendF1.getID());

            printActiveNodes(app);
        }

        app.opStart(nodeN3, "stop");
        app.opEnd(nodeN3, "stop");

        //this one fails since nodeN3 is not offering anymore the capability "host"
        //hence instance is a broken instance
        app.opStart(frontendF1, "uninstall");
        try {
            app.opEnd(frontendF1, "uninstall");
        } catch (FailedOperationException e) {
            //e.printStackTrace();
            //TODO: forse dell'errore che avevo qui me ne dovevo accorgere con unit test

            //now we run fault handling
            ArrayList<Fault> faultsList = (ArrayList<Fault>) app.getGlobalState().getPendingFaults(frontendF1);
            
            //faults list has just one member (but anyway)
            if(faultsList.size() != 1 || faultsList.get(0).getInstanceID().equals(frontendF1.getID()) == false)
                System.out.println("ERRORE");

            app.fault(frontendF1, faultsList.get(0).getReq());

            //now frontendF1 should be in the fault handling state, which is "damaged"
            System.out.println("faulted instance: " + frontendF1.getID());

            printActiveNodes(app); 
        }
        
    }

    public static void printActiveNodes(Application app){

        System.out.println("ALL ACTIVE INSTANCES");

        Collection<NodeInstance> activeInstancesCollection =  app.getGlobalState().getActiveNodeInstances().values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);
        
        for (NodeInstance instance : activeInstances) {
            System.out.println("\t" + instance.getNodeType().getName() + " " + instance.getID() + ";\t" + instance.getCurrentState());
        }

        System.out.println("\n");

    }




}