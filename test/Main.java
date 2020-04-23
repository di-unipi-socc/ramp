package test;

import model.*;
import model.exceptions.FailedOperationException;
import model.exceptions.OperationNotAvailableException;
import model.exceptions.RuleNotApplicableException;

import java.util.*;

public class Main {
    public static void main(String[] args) throws NullPointerException, RuleNotApplicableException,
        OperationNotAvailableException, FailedOperationException {
        //static setup of the application
        Node frontend = createFrontend();
        Node backend = createBackend();
        Node node = createNode();
        Node mongo = createMongo();
        
        Application demo = new Application("demo");
        demo.addNode(frontend);
        demo.addNode(backend);
        demo.addNode(node);
        demo.addNode(mongo);

        // frontend
        StaticBinding frontendAskingHost = new StaticBinding("frontend", "host");
        StaticBinding frontendHostServer = new StaticBinding("node", "host");
        demo.addStaticBinding(frontendAskingHost, frontendHostServer);

        StaticBinding frontendAskingConn = new StaticBinding("frontend", "conn");
        StaticBinding frontendConnServer = new StaticBinding("backend", "conn");
        demo.addStaticBinding(frontendAskingConn, frontendConnServer);

        // backend
        StaticBinding backendAskingHost = new StaticBinding("backend", "host");
        StaticBinding backendHostServer = new StaticBinding("node", "host");
        demo.addStaticBinding(backendAskingHost, backendHostServer);

        StaticBinding backendAskingDB = new StaticBinding("backend", "db");
        StaticBinding backendDBServer = new StaticBinding("mongo", "db");
        demo.addStaticBinding(backendAskingDB, backendDBServer);

        //now the static "composition" (nodes and static bindings) are ready
        //ready for unit testing
    }
    
    public static Node createFrontend(){
        Node frontend = new Node("frontend", "not-installed", new ManagementProtocol());
        ManagementProtocol frontendMP = frontend.getMp();

        frontend.addState("not-installed");
        frontend.addState("installed");
        frontend.addState("configured");
        frontend.addState("working");
        frontend.addState("damaged");

        frontend.addOperation("install");
        frontend.addOperation("config");
        frontend.addOperation("start");
        frontend.addOperation("unistall");
        frontend.addOperation("stop");

        Requirement host = new Requirement("host", RequirementSort.CONTAINMENT);
        Requirement conn = new Requirement("conn", RequirementSort.REPLICA_UNAWARE);
        frontend.addRequirement(host);
        frontend.addRequirement(conn);

        frontendMP.addTransition("not-installed", "install", "installed");
        frontendMP.addTransition("installed", "config", "configured");
        frontendMP.addTransition("configured", "config", "configured");
        frontendMP.addTransition("configured", "start", "working");
        frontendMP.addTransition("working", "stop", "configured");
        frontendMP.addTransition("installed", "uninstall", "not-installed");
        frontendMP.addTransition("configured", "uninstall", "not-installed");
        
        //for each state (or transition) we create the list of requirement needed in that state
        frontendMP.addRhoEntry("not-installed", new ArrayList<Requirement>());

        List<Requirement> requirementsOfInstall = new ArrayList<>();
        requirementsOfInstall.add(host);
        frontendMP.addRhoEntry("install", requirementsOfInstall);

        List<Requirement> requirementsOfConfig = new ArrayList<>();
        requirementsOfConfig.add(host);
        requirementsOfConfig.add(conn);
        frontendMP.addRhoEntry("config", requirementsOfConfig);

        List<Requirement> requirementsOfStart = new ArrayList<>();
        requirementsOfStart.add(host);
        requirementsOfStart.add(conn);
        frontendMP.addRhoEntry("start", requirementsOfStart);

        List<Requirement> requirementsOfUninstall = new ArrayList<>();
        requirementsOfUninstall.add(host);
        frontendMP.addRhoEntry("uninstall", requirementsOfUninstall);

        List<Requirement> requirementsOfStop = new ArrayList<>();
        requirementsOfStop.add(host);
        frontendMP.addRhoEntry("stop", requirementsOfStop);

        List<Requirement> requirementsOfWorking = new ArrayList<>();
        requirementsOfWorking.add(host);
        requirementsOfWorking.add(conn);
        frontendMP.addRhoEntry("working", requirementsOfWorking);

        List<Requirement> requirementsOfConfigured = new ArrayList<>();
        frontendMP.addRhoEntry("configured", requirementsOfConfigured);

        List<Requirement> requirementsOfInstalled = new ArrayList<>();
        frontendMP.addRhoEntry("installed", requirementsOfInstalled);

        List<Requirement> requirementsOfDamaged = new ArrayList<>();
        frontendMP.addRhoEntry("damaged", requirementsOfDamaged);


        ArrayList<String> tmp; 
        //frontend do not offer any caps
        for (String state : frontend.getStates()){
            tmp = new ArrayList<>();
            frontendMP.addGammaEntry(state, tmp);
        }

        //phi: state -> list of states for fault handling
        List<String> damagedList = new ArrayList<>();
        damagedList.add("damaged");
        List<String> configuredList = new ArrayList<>();
        configuredList.add("configured");
        List<String> installedList = new ArrayList<>();
        installedList.add("installed");

        frontendMP.addPhiEntry("not-installed"+"install"+"installed", damagedList);
        frontendMP.addPhiEntry("installed"+"uninstall"+"not-installed", damagedList);
        frontendMP.addPhiEntry("configured"+"uninstall"+"not-installed", damagedList);

        frontendMP.addPhiEntry("configured"+"start"+"working", configuredList);
        frontendMP.addPhiEntry("working"+"stop"+"configured", configuredList);
        frontendMP.addPhiEntry("working", configuredList);


        frontendMP.addPhiEntry("installed"+"config"+"configured", installedList);
        frontendMP.addPhiEntry("configured"+"config"+"configured", installedList);
       return frontend;
    }

    public static Node createBackend(){
        Node backend = new Node("backend", "unavailable", new ManagementProtocol());
        ManagementProtocol backendMP = backend.getMp();

        backend.addState("unavailable");
        backend.addState("available");
        backend.addState("running");
        backend.addState("damaged");

        backend.addOperation("install");
        backend.addOperation("uninstall");
        backend.addOperation("start");
        backend.addOperation("stop");
        backend.addOperation("config");

        Requirement host = new Requirement("host", RequirementSort.CONTAINMENT);
        Requirement db = new Requirement("db", RequirementSort.REPLICA_AWARE);
        backend.addRequirement(host);
        backend.addRequirement(db);
        
        backend.addCapability("conn");
        
        backendMP.addTransition("unavailable", "install", "available");
        backendMP.addTransition("available", "uninstall", "unavailable");
        backendMP.addTransition("available", "start", "running");
        backendMP.addTransition("running", "config", "running");
        backendMP.addTransition("running", "stop", "available");

        // frontendRho: state (or transition) -> reqs for that state
        backendMP.addRhoEntry("unavailable", new ArrayList<Requirement>());

        List<Requirement> requirementsOfInstall = new ArrayList<>();
        requirementsOfInstall.add(host);
        backendMP.addRhoEntry("install", requirementsOfInstall);

        List<Requirement> requirementsOfUninstall = new ArrayList<>();
        requirementsOfUninstall.add(host);
        backendMP.addRhoEntry("uninstall", requirementsOfUninstall);

        backendMP.addRhoEntry("available", new ArrayList<Requirement>());

        List<Requirement> requirementsOfStart = new ArrayList<>();
        requirementsOfStart.add(host);
        requirementsOfStart.add(db);
        backendMP.addRhoEntry("start", requirementsOfStart);

        List<Requirement> requirementsOfStop = new ArrayList<>();
        requirementsOfStop.add(host);
        backendMP.addRhoEntry("stop", requirementsOfStop);

        List<Requirement> requirementsOfRunning = new ArrayList<>();
        requirementsOfRunning.add(host);
        requirementsOfRunning.add(db);
        backendMP.addRhoEntry("running", requirementsOfRunning);

        backendMP.addRhoEntry("damaged", new ArrayList<Requirement>());

        List<Requirement> requirementsOfConfig = new ArrayList<>();
        requirementsOfConfig.add(host);
        requirementsOfConfig.add(db);
        backendMP.addRhoEntry("config", requirementsOfConfig);

        // gamma: state -> caps offered
        ArrayList<String> tmp; 
        for (String state : backend.getStates()){
            tmp = new ArrayList<>();
            backendMP.addGammaEntry(state, tmp);
        }
        List<String> capsOfRunning = (ArrayList<String>) backendMP.getGamma().get("running");
        capsOfRunning.add("conn");
        backendMP.addGammaEntry("running", capsOfRunning);


        // phi: state -> list of states for fault handling
        List<String> damagedList = new ArrayList<>();
        damagedList.add("damaged");
        List<String> availableList = new ArrayList<>();
        availableList.add("available");

        backendMP.addPhiEntry("unavailable"+"install"+"available", damagedList);
        backendMP.addPhiEntry("available"+"uninstall"+"unavailable", damagedList);
        backendMP.addPhiEntry("running"+"confing"+"running", damagedList);

        backendMP.addPhiEntry("available"+"start"+"running", availableList);
        backendMP.addPhiEntry("running"+"stop"+"available", availableList);

        return backend;
    }
    
    public static Node createNode(){
        Node node = new Node("node", "stopped", new ManagementProtocol());
        ManagementProtocol nodeMP = node.getMp();

        node.addState("stopped");
        node.addState("running");

        node.addOperation("start");
        node.addOperation("stop");
        
        node.addCapability("host");

        nodeMP.addTransition("stopped", "start", "running");
        nodeMP.addTransition("running", "stop", "stopped");
        
        //rho: state -> reqs in that state
        for (String state : node.getStates())
            nodeMP.addRhoEntry(state, new ArrayList<Requirement>());
        

        //gamma: state -> caps offered in that state
        ArrayList<String> tmp; 
        for (String state : node.getStates()){
            tmp = new ArrayList<>();
            nodeMP.addGammaEntry(state, tmp);
        }
        List<String> runningCaps = (ArrayList<String>) nodeMP.getGamma().get("running");
        runningCaps.add("host");
        nodeMP.addGammaEntry("host", runningCaps);

        // phi: state -> list of states for fault handling
        for (String state : node.getStates()) 
            nodeMP.addPhiEntry(state, new ArrayList<String>());
        
        return node;
    }

    public static Node createMongo(){
        Node mongo = new Node("mongo", "stopped", new ManagementProtocol());
        ManagementProtocol mongoMP = mongo.getMp();

        mongo.addState("stopped");
        mongo.addState("running");
        
        mongo.addOperation("start");
        mongo.addOperation("stop");
       
        mongo.addCapability("db");

        mongoMP.addTransition("stopped", "start", "running");
        mongoMP.addTransition("running", "stop", "stopped");

        // rho: state -> reqs
        for (String state : mongo.getStates())
            mongoMP.addRhoEntry(state, new ArrayList<Requirement>());
        

        // gamma: state -> caps offered
        for (String state : mongo.getStates()) {
            mongoMP.addGammaEntry(state, new ArrayList<String>());
        }
        List<String> runningCaps = mongoMP.getGamma().get("running");
        runningCaps.add("db");
        mongoMP.addGammaEntry("running", runningCaps);

        // phi: state -> list of states for fault handling
        for (String state : mongo.getStates()) 
            mongoMP.addPhiEntry(state, new ArrayList<String>());
        return mongo; 
    }
}                               