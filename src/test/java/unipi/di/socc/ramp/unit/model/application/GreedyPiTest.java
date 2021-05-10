package unipi.di.socc.ramp.unit.model.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

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

public class GreedyPiTest {
    
    public Application testApp;
    public Requirement needyReq = new Requirement("needyReq", RequirementSort.REPLICA_UNAWARE);


    @BeforeEach
    public void before() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException
    {
        /**
         * creates a simple custom application with two nodes: needy and server, both with 
         * just one state
         * - needy requires the requirement needyReq
         * - server offer the right capability serverCap for needyReq
         */

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.createNeedy());
        this.testApp.addNode(this.createServer());

        testApp.addStaticBinding(new NodeReq("needy", "needyReq"), new NodeCap("server", "serverCap"));
    }
    
    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.needyReq);

        ManagementProtocol needyMP = needy.getManProtocol();        
        //rho: state -> needed req in that state
        needyMP.getRho().get("state").add(this.needyReq);

        return needy;
    }
    public Node createServer(){
        Node server = new Node("server", new ManagementProtocol("state"));
        server.addCapability("serverCap");

        ManagementProtocol serverMP = server.getManProtocol();
        serverMP.addState("state");
        
        //gamma: state -> cap offered in that state
        serverMP.getGamma().get("state").add("serverCap");

        return server;
    }

    @Test
    public void greedyPiTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException
    {
        //null instanceID
        assertThrows(NullPointerException.class, () -> this.testApp.pi(null, this.needyReq));
        //empty instanceID
        assertThrows(IllegalArgumentException.class, () -> this.testApp.pi("", this.needyReq));
        //unknown instanceID
        assertThrows(InstanceUnknownException.class, () -> this.testApp.pi("unknown", this.needyReq));

        //create first needy (that have an unsatisfied requiremnet needyReq)
        NodeInstance needyInstance = new NodeInstance(this.testApp.getNodes().get("needy"), "state", "needyInstance");
        this.testApp.getGlobalState().getActiveInstances().put("needyInstance", needyInstance);
        this.testApp.getGlobalState().getRuntimeBindings().put("needyInstance", new ArrayList<>());

        //null requirement
        assertThrows(NullPointerException.class, () -> this.testApp.pi("needyInstance", null));

        //create three instances of server (this do not impose an order, they are inserted in an hash tabtle)
        //create first needy (that have an unsatisfied requiremnet needyReq)
        NodeInstance serverA = new NodeInstance(this.testApp.getNodes().get("server"), "state", "serverA");
        this.testApp.getGlobalState().getActiveInstances().put("serverA", serverA);
        this.testApp.getGlobalState().getRuntimeBindings().put("serverA", new ArrayList<>());
        NodeInstance serverB = new NodeInstance(this.testApp.getNodes().get("server"), "state", "serverB");
        this.testApp.getGlobalState().getActiveInstances().put("serverB", serverB);
        this.testApp.getGlobalState().getRuntimeBindings().put("serverB", new ArrayList<>());
        NodeInstance serverC = new NodeInstance(this.testApp.getNodes().get("server"), "state", "serverC");
        this.testApp.getGlobalState().getActiveInstances().put("serverC", serverC);
        this.testApp.getGlobalState().getRuntimeBindings().put("serverC", new ArrayList<>());

        //there are now 4 active instances
        assertTrue(this.testApp.getGlobalState().getActiveInstances().size() == 4);

        //list of instances that can take care of needyReq (serverA, serverB) (unordered)
        List<NodeInstance> capableInstances = this.testApp.getGlobalState().getCapableInstances("needyInstance", this.needyReq);
        assertTrue(capableInstances.size() == 3);
        assertFalse(capableInstances.contains(this.testApp.getGlobalState().getActiveInstances().get("needyInstance")));
        
        //greedyPi returns the first instance in the list (thus the first instance that can satisfy needyReq)
        assertEquals(capableInstances.get(0), this.testApp.pi("needyInstance", this.needyReq));
    }

}
