package test;
import model.*;
import java.util.*;
public class Main {

    public static void main(String[] args) {
        
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


        Map<String, List<Requirement>> rho = new HashMap<>();

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
        rho.put("not-installed", requirementsOfNotIntalled);
        rho.put("t_install", requirementsOfInstall);
        rho.put("installed", requirementsOfInstalled);
        rho.put("t_config", requirementsOfConfig);
        rho.put("t_config1", requirementsOfConfig);
        rho.put("configured", requirementsOfConfigured);
        rho.put("t_start", requirementsOfStart);
        rho.put("working", requirementsOfWorking);
        rho.put("t_stop", requirementsOfStop);
        rho.put("t_uninstall", requirementsOfUninstall);
        rho.put("t_uninstall1", requirementsOfUninstall);
        rho.put("damaged", requirementsOfDamaged);


        Map<String, Transition> allTransitions = new HashMap<>();

        Transition t_install = new Transition("t_install", "not-installed", "install", "installed");
        Transition t_config = new Transition("t_config", "installed", "config", "configured");
        Transition t_config1 = new Transition("t_config1", "configured", "config", "configured");
        Transition t_start = new Transition("t_start", "configured", "start", "working");
        Transition t_stop = new Transition("t_stop", "working", "stop", "configured");
        Transition t_uninstall = new Transition("t_uninstall", "installed", "uninstall", "not-installed");
        Transition t_uninstall1 = new Transition("t_uninstall1", "configured", "uninstall", "not-installed");
        allTransitions.put("t_install", t_install);
        allTransitions.put("t_config", t_config);
        allTransitions.put("t_config1", t_config1);
        allTransitions.put("t_start", t_start);
        allTransitions.put("t_stop", t_stop);
        allTransitions.put("t_uninstall", t_uninstall);
        allTransitions.put("t_uninstall1", t_uninstall1);

        //gamma: state -> caps offered in that state 
        Map<String, List<String>> gamma = new HashMap<>();
        //frontend do not offer any caps
        for(String state : frontendStates)
            gamma.put(state, new ArrayList<String>());
        

        //phi: state -> list<stat> for fault handling
        Map<String, List<String>> phi = new HashMap<>();

        List<String> damagedList = new ArrayList<>();
        damagedList.add("damaged");

        List<String> configuredList = new ArrayList<>();
        configuredList.add("configured");

        List<String> installedList = new ArrayList<>();
        installedList.add("installed");

        phi.put("t_config", installedList);
        phi.put("t_config1", installedList);
        phi.put("t_install", damagedList);
        phi.put("t_uninstall", damagedList);
        phi.put("t_uninstall1", damagedList);
        phi.put("t_start", configuredList);
        phi.put("t_stop", configuredList);

        ManagementProtocol frontendMP = new ManagementProtocol(allTransitions, rho, gamma, phi);

        Node frontend = new Node(
            "frontend", 
            "not-installed", 
            frontendMP, 
            frontendReqs, 
            frontendCaps, 
            frontendStates,
            frontendOps
        );

        //Application testApp = new Application();
    }
}