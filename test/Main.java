package test;
import model.*;
import java.util.*;
public class Main {

    public static void main(String[] args) {
        
//~~~~~~~~~~~~~~ FRONTEND NODE ~~~~~~~~~~~~~~~~
        //remember that a transition is also a state
        List<String> frontendStates = new ArrayList<>();
        frontendStates.add("not-installed");
        frontendStates.add("installed");
        frontendStates.add("configured");
        frontendStates.add("working");
        frontendStates.add("damaged");
        frontendStates.add("t_install");
        frontendStates.add("t_uninstall");
        frontendStates.add("t_uninstall1");
        frontendStates.add("t_config");
        frontendStates.add("t_config1");
        frontendStates.add("t_start");
        frontendStates.add("t_stop");

        List<String> frontendOps = new ArrayList<>();
        frontendOps.add("install");
        frontendOps.add("config");
        frontendOps.add("start");
        frontendOps.add("uninstall");
        frontendOps.add("stop");

        List<Requirement> frontendReqs = new ArrayList<>();
        Requirement host = new Requirement("host", RequirementSort.CONTAINMENT);
        Requirement conn = new Requirement("conn", RequirementSort.REPLICA_UNAWARE);
        frontendReqs.add(host);
        frontendReqs.add(conn);
        List<String> frontendCaps = new ArrayList<>();


        Map<String, List<Requirement>> frontendRho = new HashMap<>();

        //for each state we create the list of requirement needed in that state
        List<Requirement> requirementsOfNotIntalled = new ArrayList<>();

        List<Requirement> requirementsOfInstall = new ArrayList<>();
        requirementsOfInstall.add(host);

        List<Requirement> requirementsOfConfig = new ArrayList<>();
        requirementsOfConfig.add(host);
        requirementsOfConfig.add(conn);

        List<Requirement> requirementsOfStart = new ArrayList<>();
        requirementsOfStart.add(host);
        requirementsOfStart.add(conn);

        List<Requirement> requirementsOfUninstall = new ArrayList<>();
        requirementsOfUninstall.add(host);

        List<Requirement> requirementsOfStop = new ArrayList<>();
        requirementsOfStop.add(host);

        List<Requirement> requirementsOfWorking = new ArrayList<>();
        requirementsOfWorking.add(host);
        requirementsOfWorking.add(conn);

        List<Requirement> requirementsOfConfigured = new ArrayList<>();
        List<Requirement> requirementsOfInstalled = new ArrayList<>();
        List<Requirement> requirementsOfDamaged = new ArrayList<>();

        //remember that a transition is also a state
        frontendRho.put("not-installed", requirementsOfNotIntalled);
        frontendRho.put("t_install", requirementsOfInstall);
        frontendRho.put("installed", requirementsOfInstalled);
        frontendRho.put("t_config", requirementsOfConfig);
        frontendRho.put("t_config1", requirementsOfConfig);
        frontendRho.put("configured", requirementsOfConfigured);
        frontendRho.put("t_start", requirementsOfStart);
        frontendRho.put("working", requirementsOfWorking);
        frontendRho.put("t_stop", requirementsOfStop);
        frontendRho.put("t_uninstall", requirementsOfUninstall);
        frontendRho.put("t_uninstall1", requirementsOfUninstall);
        frontendRho.put("damaged", requirementsOfDamaged);


        Map<String, Transition> frontendAllTranstitions = new HashMap<>();

        Transition t_install = new Transition("t_install", "not-installed", "install", "installed");
        Transition t_config = new Transition("t_config", "installed", "config", "configured");
        Transition t_config1 = new Transition("t_config1", "configured", "config", "configured");
        Transition t_start = new Transition("t_start", "configured", "start", "working");
        Transition t_stop = new Transition("t_stop", "working", "stop", "configured");
        Transition t_uninstall = new Transition("t_uninstall", "installed", "uninstall", "not-installed");
        Transition t_uninstall1 = new Transition("t_uninstall1", "configured", "uninstall", "not-installed");
        frontendAllTranstitions.put("t_install", t_install);
        frontendAllTranstitions.put("t_config", t_config);
        frontendAllTranstitions.put("t_config1", t_config1);
        frontendAllTranstitions.put("t_start", t_start);
        frontendAllTranstitions.put("t_stop", t_stop);
        frontendAllTranstitions.put("t_uninstall", t_uninstall);
        frontendAllTranstitions.put("t_uninstall1", t_uninstall1);

        //frontendGamma: state -> caps offered in that state 
        Map<String, List<String>> frontendGamma = new HashMap<>();
        //frontend do not offer any caps
        for(String state : frontendStates)
            frontendGamma.put(state, new ArrayList<String>());
        

        //frontendPhi: state -> list<stat> for fault handling
        Map<String, List<String>> frontendPhi = new HashMap<>();

        List<String> damagedList = new ArrayList<>();
        damagedList.add("damaged");

        List<String> configuredList = new ArrayList<>();
        configuredList.add("configured");

        List<String> installedList = new ArrayList<>();
        installedList.add("installed");

        frontendPhi.put("t_config", installedList);
        frontendPhi.put("t_config1", installedList);
        frontendPhi.put("t_install", damagedList);
        frontendPhi.put("t_uninstall", damagedList);
        frontendPhi.put("t_uninstall1", damagedList);
        frontendPhi.put("t_start", configuredList);
        frontendPhi.put("t_stop", configuredList);
        frontendPhi.put("working", configuredList);

        ManagementProtocol frontendMP = new ManagementProtocol(
            frontendAllTranstitions, 
            frontendRho, 
            frontendGamma, 
            frontendPhi
        );

        Node frontend = new Node(
            "frontend", 
            "not-installed", 
            frontendMP, 
            frontendReqs, 
            frontendCaps, 
            frontendStates,
            frontendOps
        );


// ~~~~~~~~~~~~~~~~ BACKEND NODE ~~~~~~~~~~~~~~~~~~~~~~~
        List<String> backendStates = new ArrayList<>();                                                             
        backendStates.add("unavailable");
        backendStates.add("available");
        backendStates.add("running");
        backendStates.add("damaged");
        backendStates.add("t_install");
        backendStates.add("t_start");
        backendStates.add("t_uninstall");
        backendStates.add("t_stop");
        backendStates.add("t_config");

        List<String> backendOps = new ArrayList<>();
        backendOps.add("install");
        backendOps.add("uninstall");                
        backendOps.add("start");
        backendOps.add("stop");
        backendOps.add("config");
        
        List<Requirement> backendReqs = new ArrayList<>();
        Requirement host1 = new Requirement("host", RequirementSort.CONTAINMENT);
        Requirement db = new Requirement("db", RequirementSort.REPLICA_AWARE);
        
        List<String> backendCaps = new ArrayList<>();
        backendCaps.add("conn");

        //frontendRho: state -> reqs for that state
        Map<String, List<Requirement>> backendRho = new HashMap<>();
        List<Requirement> requirementsOfUnavailable1 = new ArrayList<>();

        List<Requirement> requirementsOfInstall1 = new ArrayList<>();
        requirementsOfInstall1.add(host1);

        List<Requirement> requirementsOfUninstall1 = new ArrayList<>();
        requirementsOfUninstall1.add(host1);

        List<Requirement> requirementsOfAvailable1 = new ArrayList<>();

        List<Requirement> requirementsOfStart1 = new ArrayList<>();
        requirementsOfStart1.add(host1);
        requirementsOfStart1.add(db);

        List<Requirement> requirementsOfStop1 = new ArrayList<>();
        requirementsOfStop1.add(host1);

        List<Requirement> requirementsOfRunning1 = new ArrayList<>();
        requirementsOfRunning1.add(host1);
        requirementsOfRunning1.add(db);

        List<Requirement> requirementsOfDamaged1 = new ArrayList<>();

        List<Requirement> requirementsOfConfig1 = new ArrayList<>();
        requirementsOfConfig1.add(host);
        requirementsOfConfig1.add(db);

        backendRho.put("unavailable", requirementsOfUnavailable1);
        backendRho.put("t_install", requirementsOfInstall1);
        backendRho.put("t_uninstall", requirementsOfUninstall1);
        backendRho.put("available", requirementsOfAvailable1);
        backendRho.put("t_start", requirementsOfStart1);
        backendRho.put("t_config", requirementsOfConfig1);
        backendRho.put("running", requirementsOfRunning1);
        backendRho.put("damaged", requirementsOfDamaged1);

        Map<String, Transition> backendAllTransitions = new HashMap<>();
        Transition t_install1 = new Transition("t_install", "unavailable", "install", "available");
        Transition t_uninstall2 = new Transition("t_uninstall", "available", "uninstall", "unavailable");
        Transition t_start1 = new Transition("t_start", "available", "start", "running");
        Transition t_config2 = new Transition("t_config", "running", "config", "running");
        Transition t_stop1 = new Transition("t_stop", "running", "stop", "available");
        backendAllTransitions.put("t_install", t_install1);
        backendAllTransitions.put("t_uninstall", t_uninstall2);
        backendAllTransitions.put("t_start", t_start1);
        backendAllTransitions.put("t_config", t_config2);
        backendAllTransitions.put("t_stop", t_stop1);

        //gamma: state -> caps offered
        Map<String, List<String>> backendGamma = new HashMap<>();
        for(String state : backendStates)
            backendGamma.put(state, new ArrayList<String>());
        List<String> capsOfRunning = (ArrayList<String>) backendGamma.get("running");
        capsOfRunning.add("conn");
        backendGamma.put("running", capsOfRunning);

        //phi: state -> list of states for fault handling
        Map<String, List<String>> backendPhi = new HashMap<>();

        List<String> damagedList1 = new ArrayList<>();
        damagedList1.add("damaged");
        List<String> availableList1 = new ArrayList<>();
        availableList1.add("available");

        backendPhi.put("t_install", damagedList1);
        backendPhi.put("t_uninstall", damagedList1);
        backendPhi.put("t_config", damagedList1);
        backendPhi.put("t_start", availableList1);
        backendPhi.put("t_stop", availableList1);
        backendPhi.put("running", availableList1);

        ManagementProtocol backendMP = new ManagementProtocol(
            backendAllTransitions, 
            backendRho, 
            backendGamma, 
            backendPhi
        );

        Node backend = new Node(
            "backend",
            "unavailable", 
            backendMP, 
            backendReqs, 
            backendCaps, 
            backendStates, 
            backendOps
        );

//~~~~~~~~~~~~~~~~~ NODE ~~~~~~~~~~~~~~~~~~~~~
        List<String> nodeStates = new ArrayList<>();
        nodeStates.add("stopped");
        nodeStates.add("running");
        nodeStates.add("t_start");
        nodeStates.add("t_stop");

        List<String> nodeOps = new ArrayList<>();
        nodeOps.add("start");
        nodeOps.add("stop");

        List<Requirement> nodeAllReqs = new ArrayList<>();

        List<String> nodeAllCaps = new ArrayList<>();
        nodeAllCaps.add("host");

        Map<String, List<Requirement>> nodeRho = new HashMap<>();
        for(String state : nodeStates){
            nodeRho.put(state, new ArrayList<Requirement>());
        }

        Map<String, Transition> nodeAllTransitions = new HashMap<>();
        Transition t_start2 = new Transition("t_start", "stopped", "start", "running");
        Transition t_stop2 = new Transition("t_stop", "running", "stop", "stopped");
        nodeAllTransitions.put("t_start", t_start2);
        nodeAllTransitions.put("t_stop", t_stop2);

        //gamma: state -> list of caps offered
        Map<String, List<String>> nodeGamma = new HashMap<>();
        for(String state : nodeStates){
            nodeGamma.put(state, new ArrayList<String>());
        }
        List<String> runningCaps = nodeGamma.get("running");
        runningCaps.add("host");
        //might be useless, the ref should do it automatically
        nodeGamma.put("running", runningCaps);

        //phi: state -> list of states for fault handling 
        Map<String, List<String>> nodePhi = new HashMap<>();
        for(String state : nodeStates){
            nodePhi.put(state, new ArrayList<String>());
        }

        ManagementProtocol nodeMP = new ManagementProtocol(
            nodeAllTransitions, 
            nodeRho, 
            nodeGamma, 
            nodePhi
        );

        Node node = new Node(
            "node",
            "stopped",
            nodeMP, 
            nodeAllReqs, 
            nodeAllCaps, 
            nodeStates, 
            nodeOps
        );


//~~~~~~~~~~~~~~~ MONGO NODE ~~~~~~~~~~~~~~~~~~~~
        List<String> mongoStates = new ArrayList<>();
        mongoStates.add("stopped");
        mongoStates.add("running");
        mongoStates.add("t_start");
        mongoStates.add("t_stop");

        List<String> mongoAllOps = new ArrayList<>();
        mongoAllOps.add("start");
        mongoAllOps.add("stop");

        List<Requirement> mongoAllReqs = new ArrayList<>();

        List<String> mongoAllCaps = new ArrayList<>();
        mongoAllCaps.add("db");

        //rho: state -> reqs
        Map<String, List<Requirement>> mongoRho = new HashMap<>();
        for(String state : mongoStates){
            mongoRho.put(state, new ArrayList<Requirement>());
        }

        Map<String, Transition> mongoAllTransitions = new HashMap<>();
        Transition t_start3 = new Transition("t_start", "stopped", "start", "running");
        Transition t_stop3 = new Transition("t_stop", "running", "stop", "stopped");
        mongoAllTransitions.put("t_start", t_start3);
        mongoAllTransitions.put("t_stop", t_stop3);

        //gamma: state -> caps offered
        Map<String, List<String>> mongoGamma = new HashMap<>();
        for(String state : mongoStates){
            mongoGamma.put(state, new ArrayList<String>());
        }
        List<String> runningCaps1 = mongoGamma.get("running");
        runningCaps1.add("db");
        mongoGamma.put("running", runningCaps1);

        //phi: state -> list of states for fault handling
        Map<String, List<String>> mongoPhi = new HashMap<>();
        for(String state : mongoStates){
            mongoPhi.put(state, new ArrayList<String>());
        }

        ManagementProtocol mongoMP = new ManagementProtocol(
            mongoAllTransitions, 
            mongoRho, 
            mongoGamma, 
            mongoPhi
        );

        Node mongo = new Node(
            "mongo", 
            "stopped", 
            mongoMP, 
            mongoAllReqs, 
            mongoAllCaps, 
            mongoStates, 
            mongoAllOps
        );

        //now we setup the binding function
        Map<StaticBinding, StaticBinding> bindingFunction = new HashMap<>();

        //frontend
        StaticBinding frontendAskingHost = new StaticBinding("frontend", "host");
        StaticBinding frontendHostServer = new StaticBinding("node", "host");
        bindingFunction.put(frontendAskingHost, frontendHostServer);

        StaticBinding frontendAskingConn = new StaticBinding("frontend", "conn");
        StaticBinding frontendConnServer = new StaticBinding("backend", "conn");
        bindingFunction.put(frontendAskingConn, frontendConnServer);

        //backend
        StaticBinding backendAskingHost = new StaticBinding("backend", "host");
        StaticBinding backendHostServer = new StaticBinding("node", "host");
        bindingFunction.put(backendAskingHost, backendHostServer);

        StaticBinding backendAskingDB = new StaticBinding("backend", "db");
        StaticBinding backendDBServer = new StaticBinding("mongo", "db");
        bindingFunction.put(backendAskingDB, backendDBServer);

        Map<String, Node> applicationNodes = new HashMap<>();
        applicationNodes.put("frontend", frontend);
        applicationNodes.put("backend", backend);
        applicationNodes.put("mongo", mongo);
        applicationNodes.put("node", node);

        Application testApp = new Application("demo", applicationNodes, bindingFunction);
    }                       
}                               