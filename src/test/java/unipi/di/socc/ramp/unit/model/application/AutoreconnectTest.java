package unipi.di.socc.ramp.unit.model.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.Fault;
import unipi.di.socc.ramp.core.model.ManagementProtocol;
import unipi.di.socc.ramp.core.model.Node;
import unipi.di.socc.ramp.core.model.NodeCap;
import unipi.di.socc.ramp.core.model.NodeReq;
import unipi.di.socc.ramp.core.model.PiVersion;
import unipi.di.socc.ramp.core.model.Requirement;
import unipi.di.socc.ramp.core.model.RequirementSort;
import unipi.di.socc.ramp.core.model.RuntimeBinding;
import unipi.di.socc.ramp.core.model.exceptions.AlreadyUsedIDException;
import unipi.di.socc.ramp.core.model.exceptions.InstanceUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.RuleNotApplicableException;

public class AutoreconnectTest {
    public Application testApp;
    public Requirement awReq;
    public Requirement unawReq;

    @BeforeEach
    public void before() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException
    {
        this.awReq = new Requirement("awReq", RequirementSort.REPLICA_AWARE);
        this.unawReq = new Requirement("unawReq", RequirementSort.REPLICA_UNAWARE);

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.createNeedy());
        this.testApp.addNode(this.createServer());
        
        this.testApp.addStaticBinding(new NodeReq("needy", "awReq"), new NodeCap("server", "awCap"));
        this.testApp.addStaticBinding(new NodeReq("needy", "unawReq"), new NodeCap("server", "unawCap"));
    }

    @Test
    public void autoreconnectTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException
    {
        //exception cases
        assertThrows(NullPointerException.class, () -> this.testApp.autoreconnect(null));

        this.testApp.scaleOut1("needy", "needyID");
        //needyID has two fault pending non resolvable fault now
        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").size() == 2);
        assertTrue(this.testApp.getGlobalState().getResolvableFaults("needyID").isEmpty());

        Fault nonResolvableFault = this.testApp.getGlobalState().getPendingFaults("needyID").get(0);
        assertThrows(
            RuleNotApplicableException.class, 
            () -> this.testApp.autoreconnect(nonResolvableFault)
        );
        Fault unknownInstanceFault = new Fault("unknownID", this.awReq);
        assertThrows(
            InstanceUnknownException.class, 
            () -> this.testApp.autoreconnect(unknownInstanceFault)
        );

        //REAL TESTS
        //starts from scratch
        this.testApp.scaleIn("needyID");

        this.testApp.scaleOut1("server", "serverID1");
        this.testApp.scaleOut1("needy", "needyID");

        this.testApp.scaleOut1("server", "serverID2");

        //needyID has both of its reqs satisfied 
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyID").size() == 2);
        //both of the reqs are handled by serverID1
        for(RuntimeBinding needyRB : this.testApp.getGlobalState().getRuntimeBindings().get("needyID"))
            assertEquals("serverID1", needyRB.getNodeInstanceID());

        //now we kill serverID1
        this.testApp.scaleIn("serverID1");
        //needyID has 2 pending faults, 1 of them is resolvable (unawReq by serverID2)
        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").size() == 2);
        assertTrue(this.testApp.getGlobalState().getResolvableFaults("needyID").size() == 1);
        
        //we fix the resolvable fault
        Fault resolvableFault = this.testApp.getGlobalState().getResolvableFaults("needyID").get(0);
        this.testApp.autoreconnect(resolvableFault);
        //now needyID has 1 satisfied req, 1 pending fault and 0 resolvable fault
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyID").size() == 1);
        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").size() == 1);
        assertTrue(this.testApp.getGlobalState().getResolvableFaults("needyID").size() == 0);
    }

    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.awReq);
        needy.addRequirement(this.unawReq);

        ManagementProtocol needyMP = needy.getManProtocol();
        //rho: state -> needed req in that state
        needyMP.getRho().get("state").add(this.awReq);
        needyMP.getRho().get("state").add(this.unawReq);

        return needy;
    }

    public Node createServer(){
        Node server = new Node("server", new ManagementProtocol("state"));
        server.addCapability("awCap");
        server.addCapability("unawCap");

        ManagementProtocol serverMP = server.getManProtocol();
        serverMP.getGamma().get("state").add("awCap");
        serverMP.getGamma().get("state").add("unawCap");

        return server;
    }


}
