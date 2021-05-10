package unipi.di.socc.ramp.unit.model.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.ManagementProtocol;
import unipi.di.socc.ramp.core.model.Node;
import unipi.di.socc.ramp.core.model.NodeCap;
import unipi.di.socc.ramp.core.model.NodeInstance;
import unipi.di.socc.ramp.core.model.NodeReq;
import unipi.di.socc.ramp.core.model.PiVersion;
import unipi.di.socc.ramp.core.model.Requirement;
import unipi.di.socc.ramp.core.model.RequirementSort;
import unipi.di.socc.ramp.core.model.exceptions.AlreadyUsedIDException;
import unipi.di.socc.ramp.core.model.exceptions.InstanceUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.RuleNotApplicableException;


public class ScaleOut1Test {

    public Application testApp;
    public Requirement needyReq;

    @BeforeEach
    public void before() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException
    {
        this.needyReq = new Requirement("needyReq", RequirementSort.REPLICA_UNAWARE);

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.createNeedy());
        this.testApp.addNode(this.createServer());
        this.testApp.addNode(this.createContainerNeedy());

        this.testApp.addStaticBinding(new NodeReq("needy", "needyReq"), new NodeCap("server", "serverCap"));
    }

    @Test
    public void testScaleOut1() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException
        {

        // EXCEPTION CASES 
        assertThrows(NullPointerException.class,  () -> this.testApp.scaleOut1(null, "newNodeInstanceID"));
        assertThrows(NullPointerException.class,  () -> this.testApp.scaleOut1("nodeName", null));
        
        assertThrows(IllegalArgumentException.class,  () -> this.testApp.scaleOut1("", "newNodeInstanceID"));
        assertThrows(IllegalArgumentException.class,  () -> this.testApp.scaleOut1("nodeName", ""));

        //hard create needyInstance (to test already used exception)
        NodeInstance dummyInstance = new NodeInstance(this.testApp.getNodes().get("needy"), "state", "dummyInstance");
        this.testApp.getGlobalState().getActiveInstances().put("dummyInstance", dummyInstance);
        this.testApp.getGlobalState().getRuntimeBindings().put("dummyInstance", new ArrayList<>());

        //scaleOut1 an instance whoose id-to-be is already used (dummyInstance)
        assertThrows(AlreadyUsedIDException.class, () -> this.testApp.scaleOut1("needy", "dummyInstance"));
        //scaleOut1 of an unknown node
        assertThrows(NodeUnknownException.class, () -> this.testApp.scaleOut1("unknownNode", "newInstanceID"));
        //scaleOut1 a node with a containment requirement
        assertThrows(RuleNotApplicableException.class, () -> this.testApp.scaleOut1("containerNeedy", "newInstanceID"));

        //REAL TESTS
        this.testApp.scaleOut1("server", "serverInstance");
        //the new instance is in active instances
        assertNotNull(this.testApp.getGlobalState().getActiveInstances().get("serverInstance"));
        //the new instance has inatializated runtime bindings
        assertNotNull(this.testApp.getGlobalState().getRuntimeBindings().get("serverInstance"));
        //the new instance has no runtime binding
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("serverInstance").isEmpty());

        //create needyInstance
        this.testApp.scaleOut1("needy", "needyInstance");
        //the new instance is in active instances 
        assertNotNull(this.testApp.getGlobalState().getActiveInstances().get("needyInstance"));
        //scaleOut1 calls addNewRuntimeBindings and there is server who can satisfy needyReq
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("needyInstance").size() == 1);
        assertEquals(this.needyReq, this.testApp.getGlobalState().getRuntimeBindings().get("needyInstance").get(0).getReq());

    }

    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.needyReq);
        ManagementProtocol needyMP = needy.getManProtocol();
        
        //rho: state -> needed req in that state
        needyMP.getRho().get("state").add(this.needyReq);
        
        return needy;
    }

    public Node createContainerNeedy(){
        Node containerNeedy = new Node("containerNeedy", new ManagementProtocol("state"));

        Requirement contReq = new Requirement("contReq", RequirementSort.CONTAINMENT);
        containerNeedy.addRequirement(contReq);

        ManagementProtocol containerNeedyMP = containerNeedy.getManProtocol();        
        //rho: state -> needed req in that state
        containerNeedyMP.getRho().get("state").add(contReq);
        
        return containerNeedy;
    }

    public Node createServer(){
        Node serverA = new Node("server", new ManagementProtocol("state"));
        serverA.addCapability("serverCap");
        ManagementProtocol serverMP = serverA.getManProtocol();
        
        //gamma: state -> cap offered in that state
        serverMP.getGamma().get("state").add("serverCap");

        return serverA;
    }

}
