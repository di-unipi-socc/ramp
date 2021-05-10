package unipi.di.socc.ramp.unit.model.globalState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.GlobalState;
import unipi.di.socc.ramp.core.model.ManagementProtocol;
import unipi.di.socc.ramp.core.model.Node;
import unipi.di.socc.ramp.core.model.NodeCap;
import unipi.di.socc.ramp.core.model.NodeReq;
import unipi.di.socc.ramp.core.model.PiVersion;
import unipi.di.socc.ramp.core.model.Requirement;
import unipi.di.socc.ramp.core.model.RequirementSort;
import unipi.di.socc.ramp.core.model.exceptions.AlreadyUsedIDException;
import unipi.di.socc.ramp.core.model.exceptions.InstanceUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.RuleNotApplicableException;

public class GetPendingFaultsTest {
    
    //pending faults are all those <instance, req> where req is 
    //needed but not satisfied (req !containment)

    public Application testApp;
    public Requirement contReq;
    public Requirement unawReq;
    public Requirement awReq;

    @BeforeEach
    public void before() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException
    {
        this.contReq = new Requirement("contReq", RequirementSort.CONTAINMENT);
        this.awReq = new Requirement("awReq", RequirementSort.REPLICA_AWARE);
        this.unawReq = new Requirement("unawReq", RequirementSort.REPLICA_UNAWARE);

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.createNeedy());
        this.testApp.addNode(this.createServer());

        this.testApp.addStaticBinding(new NodeReq("needy", "contReq"), new NodeCap("server", "contCap"));
        this.testApp.addStaticBinding(new NodeReq("needy", "awReq"), new NodeCap("server", "awCap"));
        this.testApp.addStaticBinding(new NodeReq("needy", "unawReq"), new NodeCap("server", "unawCap"));
    }

    @Test
    public void getPendingFaultsTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException    
    {
        GlobalState gs = this.testApp.getGlobalState();

        //execption cases 
        assertThrows(NullPointerException.class, () -> gs.getPendingFaults(null));
        assertThrows(IllegalArgumentException.class, () -> gs.getPendingFaults(""));
        assertThrows(InstanceUnknownException.class, () -> gs.getPendingFaults("unknownID"));

        //REAL TESTS
        this.testApp.scaleOut1("server", "serverID");
        //serverID has no reqs hence no pending faults
        assertTrue(gs.getPendingFaults("serverID").isEmpty());

        this.testApp.scaleOut2("needy", "needyID", "serverID");
        //needyID has everything satisfied now
        assertTrue(gs.getSatisfiedReqs("needyID").size() == 3);
        //remove a runtime binding of needyID (about unawReq)
        gs.removeRuntimeBinding("needyID", this.unawReq);
        //now needyID has only two reqs (contReq, awReq) satisfied
        assertTrue(gs.getSatisfiedReqs("needyID").size() == 2);
        //hence there is a pending fault (unawReq is NEEDED but NOT SATISFIED)
        assertTrue(gs.getPendingFaults("needyID").size() == 1);
        //the fault is in fact about unawReq
        assertEquals(this.unawReq, gs.getPendingFaults("needyID").get(0).getReq());

        //now remove the binding of awReq
        gs.removeRuntimeBinding("needyID", this.awReq);
        //needyID has 1 satisfied req (the containment 1)
        assertTrue(gs.getSatisfiedReqs("needyID").size() == 1);
        //needyID is not a broken instance since the containment is on
        assertFalse(gs.isBrokenInstance("needyID"));
        //needy has 2 pending faults (awReq, unawReq)
        assertTrue(gs.getPendingFaults("needyID").size() == 2);

        //TODO more complete test
        //-> pending because no runtime binding
        //-> pending because runtime binding but dead server
        //-> pending because runtime binding but server not offering cap
    }

    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.awReq);
        needy.addRequirement(this.unawReq);
        needy.addRequirement(this.contReq);

        ManagementProtocol needyMP = needy.getManProtocol();
        needyMP.getRho().get("state").add(this.awReq);
        needyMP.getRho().get("state").add(this.unawReq);
        needyMP.getRho().get("state").add(this.contReq);

        return needy;
    }

    public Node createServer(){
        Node server = new Node("server", new ManagementProtocol("state"));
        server.addCapability("awCap");
        server.addCapability("unawCap");
        server.addCapability("contCap");

        ManagementProtocol serverMP = server.getManProtocol();
        serverMP.getGamma().get("state").add("awCap");
        serverMP.getGamma().get("state").add("unawCap");
        serverMP.getGamma().get("state").add("contCap");

        return server;
    }
}
