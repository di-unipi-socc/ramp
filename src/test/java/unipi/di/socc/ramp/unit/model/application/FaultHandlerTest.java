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
import unipi.di.socc.ramp.core.model.exceptions.AlreadyUsedIDException;
import unipi.di.socc.ramp.core.model.exceptions.FailedFaultHandlingExecption;
import unipi.di.socc.ramp.core.model.exceptions.InstanceUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.RuleNotApplicableException;


public class FaultHandlerTest {
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
    public void faultHandlerTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException, 
            FailedFaultHandlingExecption
    {
        //exception cases
        assertThrows(NullPointerException.class, () -> this.testApp.faultHandler(null));
        
        this.testApp.scaleOut1("needy", "needyID");
        //needyID has now 2 non resolvable faults
        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").size() == 2);
        assertTrue(this.testApp.getGlobalState().getResolvableFaults("needyID").isEmpty());

        this.testApp.scaleOut1("server", "serverID");
        //needyID now has 2 pending faults, 1 of them resolvable thanks to serverID
        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").size() == 2);
        assertTrue(this.testApp.getGlobalState().getResolvableFaults("needyID").size() == 1);

        Fault resolvableFault = this.testApp.getGlobalState().getResolvableFaults("needyID").get(0);
        assertThrows(RuleNotApplicableException.class, () -> this.testApp.faultHandler(resolvableFault));

        Fault tmp = null;
        for(Fault f : this.testApp.getGlobalState().getPendingFaults("needyID")){
            if(f.getReq().isReplicaAware())
                tmp = f;
        }
        //needed: lambda requries final local variables
        final Fault nonResolvableFault = tmp;
        //fails because needy has not a fault handling state for this fault
        assertThrows(
            FailedFaultHandlingExecption.class, 
            () -> this.testApp.faultHandler(nonResolvableFault)
        );

        //real tests    
        //add the fault handling state
        ManagementProtocol needyMP = this.testApp.getNodes().get("needy").getManProtocol();
        //phi: faulted state -> states for fault handling
        needyMP.getPhi().get("state").add("damaged");

        this.testApp.faultHandler(nonResolvableFault);
        //needyID is in damaged state
        assertEquals(
            "damaged", 
            this.testApp.getGlobalState().getActiveInstances().get("needyID").getCurrentState()
        );
        //in the current state needyID dont requires nothing (hence has no faults)
        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").isEmpty());
    }

    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.awReq);
        needy.addRequirement(this.unawReq);

        ManagementProtocol needyMP = needy.getManProtocol();
        //phi entry added in test
        needyMP.addState("damaged");

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
