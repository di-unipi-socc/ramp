package unipi.di.socc.ramp.unit.model.globalState;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
import unipi.di.socc.ramp.core.model.exceptions.AlreadyUsedIDException;
import unipi.di.socc.ramp.core.model.exceptions.InstanceUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.RuleNotApplicableException;

public class IsResolvableFaultTest {
    
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
    public void isResolvableFaultTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException
    {
        //exception cases
        assertThrows(NullPointerException.class, () -> this.testApp.getGlobalState().isResolvableFault(null));
        Fault unknownInstanceFault = new Fault("unknownID", this.awReq);
        assertThrows(
            InstanceUnknownException.class,
            () -> this.testApp.getGlobalState().isResolvableFault(unknownInstanceFault)
        );

        //real tests
        this.testApp.scaleOut1("needy", "needyID");
        //needyID has no satisfied requirements
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyID").isEmpty());
        //needyID requires two requirements in its state, so there are two pending faults
        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").size() == 2);
        //since server is not instantiated needyID has 0 resolvable faults 
        for(Fault f : this.testApp.getGlobalState().getPendingFaults("needyID"))
            assertFalse(this.testApp.getGlobalState().isResolvableFault(f));

        //now we create an instance of server
        this.testApp.scaleOut1("server", "serverID");
        //still 2 pending faults
        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").size() == 2);
        //one of the pending faults is now a resolvable fault (the one about unawReq)
        for(Fault f : this.testApp.getGlobalState().getPendingFaults("needyID")){
            if(f.getReq().isReplicaUnaware())
                assertTrue(this.testApp.getGlobalState().isResolvableFault(f));
            else
                assertFalse(this.testApp.getGlobalState().isResolvableFault(f));
        }
        assertTrue(this.testApp.getGlobalState().getResolvableFaults("needyID").size() == 1);
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
