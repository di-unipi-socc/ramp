package unipi.di.socc.ramp;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.ManagementProtocol;
import unipi.di.socc.ramp.core.model.Node;
import unipi.di.socc.ramp.core.model.NodeCap;
import unipi.di.socc.ramp.core.model.NodeReq;
import unipi.di.socc.ramp.core.model.PiVersion;
import unipi.di.socc.ramp.core.model.Requirement;
import unipi.di.socc.ramp.core.model.RequirementSort;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;

//TODO: cambiati i protocolli!! correggi


public class ThinkingAppFactory {

    public static Application createThinking(PiVersion piVersion) 
        throws 
            NullPointerException, 
            NodeUnknownException
    {
        Application app = new Application("thinking", piVersion);
        
        app.addNode(ThinkingAppFactory.createGUI());
        app.addNode(ThinkingAppFactory.createAPI());
        app.addNode(ThinkingAppFactory.createNode());
        app.addNode(ThinkingAppFactory.createMongo());
        app.addNode(ThinkingAppFactory.createMaven());
        
        //gui topology
        app.addStaticBinding(new NodeReq("gui", "host"), new NodeCap("node", "host"));
        app.addStaticBinding(new NodeReq("gui", "backend"), new NodeCap("api", "endpoint"));
        //api topology
        app.addStaticBinding(new NodeReq("api", "host"), new NodeCap("maven", "host"));
        app.addStaticBinding(new NodeReq("api", "data"), new NodeCap("mongo", "db"));

        return app;
    }

    private static Node createAPI(){
        Node api = new Node("api", new ManagementProtocol("unavailable"));
        ManagementProtocol apiMP = api.getManProtocol();

        //add operations
        api.addOperation("install");
        api.addOperation("uninstall");
        api.addOperation("start");
        api.addOperation("stop");
        api.addOperation("config");

        //add requirements
        Requirement host = new Requirement("host", RequirementSort.CONTAINMENT);
        Requirement data = new Requirement("data", RequirementSort.REPLICA_AWARE);
        api.addRequirement(host);
        api.addRequirement(data);

        //add capabilities
        api.addCapability("endpoint");

        //add states and transitions (both as states and objects)
        apiMP.addState("available");
        apiMP.addState("running");
        apiMP.addState("damaged");
        apiMP.addTransition("unavailable", "install", "available");
        apiMP.addTransition("available", "uninstall", "unavailable");
        apiMP.addTransition("available", "start", "running");
        apiMP.addTransition("running", "config", "running");
        apiMP.addTransition("running", "stop", "available");

        //add requirements for the states who needs them
        apiMP.getRho().get("unavailable" + "install" + "available").add(host);
        apiMP.getRho().get("available" + "uninstall" + "unavailable").add(host);
        apiMP.getRho().get("available" + "start" + "running").add(host);
        apiMP.getRho().get("available" + "start" + "running").add(data);
        apiMP.getRho().get("running" + "stop" + "available").add(host);
        apiMP.getRho().get("running").add(host);
        apiMP.getRho().get("running").add(data);
        apiMP.getRho().get("running" + "config" + "running").add(host);
        apiMP.getRho().get("running" + "config" + "running").add(data);

        //add capabilities for the state that offers them
        apiMP.getGamma().get("running").add("endpoint");

        //add fault handling states for the states who needs them
        apiMP.getPhi().get("unavailable"+"install"+"available").add("damaged");
        apiMP.getPhi().get("available"+"uninstall"+"unavailable").add("damaged");
        apiMP.getPhi().get("running"+"config"+"running").add("damaged"); 
        apiMP.getPhi().get("available"+"start"+"running").add("available");
        apiMP.getPhi().get("running"+"stop"+"available").add("available");
        apiMP.getPhi().get("running").add("available");

        return api;
    }
    private static Node createGUI(){
        Node gui = new Node("gui", new ManagementProtocol("not-installed"));
        ManagementProtocol guiMP = gui.getManProtocol();

        //add operations
        gui.addOperation("install");
        gui.addOperation("config");
        gui.addOperation("start");
        gui.addOperation("uninstall");
        gui.addOperation("stop");

        //add requirements
        Requirement host = new Requirement("host", RequirementSort.CONTAINMENT);
        Requirement backend = new Requirement("backend", RequirementSort.REPLICA_UNAWARE);
        gui.addRequirement(host);
        gui.addRequirement(backend);

        //add states and transition (as states and as object)
        guiMP.addState("installed");
        guiMP.addState("configured");
        guiMP.addState("working");
        guiMP.addState("damaged");
        guiMP.addTransition("not-installed", "install", "installed");
        guiMP.addTransition("installed", "config", "configured");
        guiMP.addTransition("configured", "config", "configured");
        guiMP.addTransition("configured", "start", "working");
        guiMP.addTransition("working", "stop", "configured");
        guiMP.addTransition("installed", "uninstall", "not-installed");
        guiMP.addTransition("configured", "uninstall", "not-installed");

        //add requirements for the states who needs them
        guiMP.getRho().get("not-installed" + "install" + "installed").add(host);
        guiMP.getRho().get("installed" + "config" + "configured").add(host);
        guiMP.getRho().get("configured" + "config" + "configured").add(backend);
        guiMP.getRho().get("configured" + "start" + "working").add(host);
        guiMP.getRho().get("configured" + "start" + "working").add(backend);
        guiMP.getRho().get("installed" + "uninstall" + "not-installed").add(host);
        guiMP.getRho().get("configured" + "uninstall" + "not-installed").add(host);
        guiMP.getRho().get("working" + "stop" + "configured").add(host);
        guiMP.getRho().get("working").add(host);
        guiMP.getRho().get("working").add(backend);

        //add fault handling states for the states who needs them
        guiMP.getPhi().get("not-installed"+"install"+"installed").add("damaged");
        guiMP.getPhi().get("installed"+"uninstall"+"not-installed").add("damaged");
        guiMP.getPhi().get("configured"+"uninstall"+"not-installed").add("damaged");
        guiMP.getPhi().get("configured"+"start"+"working").add("configured");
        guiMP.getPhi().get("working"+"stop"+"configured").add( "configured");
        guiMP.getPhi().get("working").add("configured");
        guiMP.getPhi().get("installed"+"config"+"configured").add("installed");
        guiMP.getPhi().get("configured"+"config"+"configured").add("installed");
      
        return gui;
    }
    private static Node createNode(){
        Node node = new Node("node", new ManagementProtocol("stopped"));
        node.addCapability("host");
        node.addOperation("start");
        node.addOperation("stop");
        
        ManagementProtocol nodeMP = node.getManProtocol();
        nodeMP.addState("running");
        nodeMP.addTransition("stopped", "start", "running");
        nodeMP.addTransition("running", "stop", "stopped");

        //node offer the capability host when it is in running        
        nodeMP.getGamma().get("running").add("host");

        return node;
    }
    private static Node createMaven(){
        Node maven = new Node("maven", new ManagementProtocol("stopped"));
        maven.addCapability("host");

        maven.addOperation("start");
        maven.addOperation("stop");
        
        ManagementProtocol mavenMP = maven.getManProtocol();
        mavenMP.addState("running");
        mavenMP.addTransition("stopped", "start", "running");
        mavenMP.addTransition("running", "stop", "stopped");

        //maven offer the capability host when it is in running        
        mavenMP.getGamma().get("running").add("host");

        return maven;
    }
    private static Node createMongo(){
        Node mongo = new Node("mongo", new ManagementProtocol("stopped"));
        mongo.addCapability("db");

        mongo.addOperation("start");
        mongo.addOperation("stop");
        
        ManagementProtocol mongoMP = mongo.getManProtocol();
        mongoMP.addState("running");
        mongoMP.addTransition("stopped", "start", "running");
        mongoMP.addTransition("running", "stop", "stopped");

        //mongo offer the capability db when it is in running        
        mongoMP.getGamma().get("running").add("db");

        return mongo;
    }

    @Test
    public void test() throws NullPointerException, NodeUnknownException{
        Application thinking = ThinkingAppFactory.createThinking(PiVersion.GREEDYPI);

        assertNotNull(thinking);
        assertNotNull(thinking.getNodes().get("api"));
        assertNotNull(thinking.getNodes().get("gui"));
        assertNotNull(thinking.getNodes().get("node"));
        assertNotNull(thinking.getNodes().get("mongo"));
        assertNotNull(thinking.getNodes().get("maven"));

        // Node api = thinking.getNodes().get("api");
        // Node mongo = thinking.getNodes().get("mongo");
        // Node gui = thinking.getNodes().get("gui");
        // Node maven = thinking.getNodes().get("maven");
        // Node node = thinking.getNodes().get("node");

        //TODO: test that checks if thinking is defined as in theory


    }



}
