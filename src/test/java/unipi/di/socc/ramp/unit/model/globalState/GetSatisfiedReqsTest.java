package unipi.di.socc.ramp.unit.model.globalState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.GlobalState;
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

public class GetSatisfiedReqsTest {
    
    public Application testApp;
    public GlobalState testGS;
    public Requirement reqA;
    public Requirement reqB;
    public Requirement reqC;

    @BeforeEach
    public void before() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException
    {
        this.reqA = new Requirement("reqA", RequirementSort.REPLICA_UNAWARE);
        this.reqB = new Requirement("reqB", RequirementSort.REPLICA_UNAWARE);
        this.reqC = new Requirement("reqC", RequirementSort.REPLICA_UNAWARE);

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testGS = testApp.getGlobalState();

        this.testApp.addNode(this.createNeedy());
        this.testApp.addNode(this.createServerA());
        this.testApp.addNode(this.createServerB());
        this.testApp.addNode(this.createServerC());

        this.testApp.addStaticBinding(new NodeReq("needy", "reqA"), new NodeCap("serverA", "capA"));
        this.testApp.addStaticBinding(new NodeReq("needy", "reqB"), new NodeCap("serverB", "capB"));
        this.testApp.addStaticBinding(new NodeReq("needy", "reqC"), new NodeCap("serverC", "capC"));
    }

    @Test
    public void getSatisfiedReqsTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException
    {
        //exception cases
        assertThrows(NullPointerException.class, () -> this.testGS.getSatisfiedReqs(null));
        assertThrows(IllegalArgumentException.class, () -> this.testGS.getSatisfiedReqs(""));
        assertThrows(InstanceUnknownException.class, () -> this.testGS.getSatisfiedReqs("unknown"));

        //create needyInstance
        NodeInstance needyInstance = new NodeInstance(this.testApp.getNodes().get("needy"), "state", "needyInstance");
        this.testGS.getActiveInstances().put("needyInstance", needyInstance);
        this.testGS.getRuntimeBindings().put("needyInstance", new ArrayList<>());
        
        //needyInstance has 3 requirements in the current state
        assertTrue(needyInstance.getNeededReqs().size() == 3);
        //needyInstance has not satisfied reqs
        assertTrue(this.testGS.getSatisfiedReqs("needyInstance").size() == 0);
        
        //create first server (serverA)
        NodeInstance instanceServerA = new NodeInstance(this.testApp.getNodes().get("serverA"), "state", "instanceServerA");
        this.testApp.getGlobalState().getActiveInstances().put("instanceServerA", instanceServerA);
        this.testApp.getGlobalState().getRuntimeBindings().put("instanceServerA", new ArrayList<>());
        //add explicitly the runtime bidning
        this.testGS.addRuntimeBinding("needyInstance", this.reqA, "instanceServerA");

        //now needyInstance has 1 satisfied requirement (which is about reqA)
        assertTrue(this.testGS.getSatisfiedReqs("needyInstance").size() == 1);
        assertEquals(this.testGS.getSatisfiedReqs("needyInstance").get(0).getName(), "reqA");

        //create last two servers (serverB, serverC)
        NodeInstance instanceServerB = new NodeInstance(this.testApp.getNodes().get("serverB"), "state", "instanceServerB");
        this.testApp.getGlobalState().getActiveInstances().put("instanceServerB", instanceServerB);
        this.testApp.getGlobalState().getRuntimeBindings().put("instanceServerB", new ArrayList<>());
        NodeInstance instanceServerC = new NodeInstance(this.testApp.getNodes().get("serverC"), "state", "instanceServerC");
        this.testApp.getGlobalState().getActiveInstances().put("instanceServerC", instanceServerC);
        this.testApp.getGlobalState().getRuntimeBindings().put("instanceServerC", new ArrayList<>());

        //add the new runtime bindings needyInstance -> <serverB, reqB>, <serverC, reqC>
        this.testGS.addRuntimeBinding("needyInstance", this.reqB, "instanceServerB");
        this.testGS.addRuntimeBinding("needyInstance", this.reqC, "instanceServerC");

        //all the requirements are now satisfied
        assertTrue(this.testGS.getSatisfiedReqs("needyInstance").size() == 3);
        
        //remove one runtime binding of needyInstance about reqA
        this.testGS.removeRuntimeBinding("needyInstance", this.reqA);

        //now only two requirements are satisfied
        assertTrue(this.testGS.getSatisfiedReqs("needyInstance").size() == 2);
        //reqA is not satisfied
        assertFalse(this.testGS.getSatisfiedReqs("needyInstance").contains(this.reqA));
    }

    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.reqA);
        needy.addRequirement(this.reqB);
        needy.addRequirement(this.reqC);

        ManagementProtocol needyMP = needy.getManProtocol();
        
        //rho: state -> needed req in that state
        needyMP.getRho().get("state").add(this.reqA);
        needyMP.getRho().get("state").add(this.reqB);
        needyMP.getRho().get("state").add(this.reqC);

        return needy;
    }
    public Node createServerA(){
        Node serverA = new Node("serverA", new ManagementProtocol("state"));
        serverA.addCapability("capA");

        ManagementProtocol serverMP = serverA.getManProtocol();
        
        //gamma: state -> cap offered in that state
        serverMP.getGamma().get("state").add("capA");

        return serverA;
    }
    public Node createServerB(){
        Node serverB = new Node("serverB", new ManagementProtocol("state"));
        serverB.addCapability("capB");

        ManagementProtocol serverMP = serverB.getManProtocol();
        //gamma: state -> cap offered in that state
        serverMP.getGamma().get("state").add("capB");

        return serverB;
    }
    public Node createServerC(){
        Node serverC = new Node("serverC", new ManagementProtocol("state"));
        serverC.addCapability("capC");

        ManagementProtocol serverMP = serverC.getManProtocol();
        
        //gamma: state -> cap offered in that state
        serverMP.getGamma().get("state").add("capC");

        return serverC;
    }

   
}
