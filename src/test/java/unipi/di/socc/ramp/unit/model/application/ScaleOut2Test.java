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

public class ScaleOut2Test {

    public Application testApp;
    public Requirement containmentReq;

    @BeforeEach
    public void before() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException
    {
        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.containmentReq = new Requirement("containmentReq", RequirementSort.CONTAINMENT);

        this.testApp.addNode(this.createNeedy());
        this.testApp.addNode(this.createNotNeedy());
        this.testApp.addNode(this.createServer());

        this.testApp.addStaticBinding(new NodeReq("needy", "containmentReq"), new NodeCap("server", "containmentCap"));
    }

    @Test
    public void scaleOut2Test() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException
    {
        // EXCEPTION CASES 
        assertThrows(NullPointerException.class, () -> this.testApp.scaleOut2("needy", "needyInstance", null));
        assertThrows(IllegalArgumentException.class, () -> this.testApp.scaleOut2("needy", "needyInstance", ""));
        assertThrows(InstanceUnknownException.class, () -> this.testApp.scaleOut2("needy", "needyInstance", "serverInstance"));

        this.testApp.scaleOut1("server", "serverInstance");

        assertThrows(NullPointerException.class, () -> this.testApp.scaleOut2(null, "needyInstance", "serverInstance"));
        assertThrows(NullPointerException.class, () -> this.testApp.scaleOut2("needy", null, "serverInstance"));

        assertThrows(IllegalArgumentException.class, () -> this.testApp.scaleOut2("", "needyInstance", "serverInstance"));
        assertThrows(IllegalArgumentException.class, () -> this.testApp.scaleOut2("needy", "", "serverInstance"));
        
        //hard create needyInstance (then we scaleOut2 needy with the same ID and get the exeption)
        NodeInstance dummyInstance = new NodeInstance(this.testApp.getNodes().get("needy"), "state", "dummyInstance");
        this.testApp.getGlobalState().getActiveInstances().put("dummyInstance", dummyInstance);
        this.testApp.getGlobalState().getRuntimeBindings().put("dummyInstance", new ArrayList<>());
        assertThrows(AlreadyUsedIDException.class, () -> this.testApp.scaleOut2("needy", "dummyInstance", "serverInstance"));
        
        //scaleOut2 of an unknown node
        assertThrows(NodeUnknownException.class, () -> this.testApp.scaleOut2("unknownNode", "needyInstance", "serverInstance"));
        //scaleOut2 of a node without a containment requirement
        assertThrows(
            RuleNotApplicableException.class, 
            () -> this.testApp.scaleOut2("notNeedy", "notNeedyInstance", "serverInstance")
        );
    
        //REAL TESTS
        //create the needyInstance with its server (containment requirement satisfied)
        this.testApp.scaleOut2("needy", "needyInstance", "serverInstance");
        //the new instance is in active instances 
        assertNotNull(this.testApp.getGlobalState().getActiveInstances().get("needyInstance"));
        //scaleOut2 explicitally creates the runtime binding about the containment requirement
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("needyInstance").size() == 1);
        assertEquals(
            this.containmentReq, 
            this.testApp.getGlobalState().getRuntimeBindings().get("needyInstance").get(0).getReq()
        );

    }

    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.containmentReq);

        ManagementProtocol needyMP = needy.getManProtocol();        
        //rho: state -> needed req in that state
        needyMP.getRho().get("state").add(this.containmentReq);
        
        return needy;
    }
    public Node createNotNeedy(){
        Node notNeedy = new Node("notNeedy", new ManagementProtocol("state"));
        return notNeedy;
    }
    public Node createServer(){
        Node server = new Node("server", new ManagementProtocol("state"));
        server.addCapability("containmentCap");

        ManagementProtocol serverMP = server.getManProtocol();        
        //gamma: state -> needed req in that state
        serverMP.getGamma().get("state").add("containmentCap");
        
        return server;
    }


}
