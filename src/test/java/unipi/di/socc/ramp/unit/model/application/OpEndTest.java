package unipi.di.socc.ramp.unit.model.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.ManagementProtocol;
import unipi.di.socc.ramp.core.model.Node;
import unipi.di.socc.ramp.core.model.NodeCap;
import unipi.di.socc.ramp.core.model.NodeReq;
import unipi.di.socc.ramp.core.model.PiVersion;
import unipi.di.socc.ramp.core.model.Requirement;
import unipi.di.socc.ramp.core.model.RequirementSort;
import unipi.di.socc.ramp.core.model.exceptions.*;

public class OpEndTest {
    
    public Application testApp;
    public Requirement contReq;
    public Requirement awReq;
    public Requirement unawReq;

    @BeforeEach
    public void before() throws NullPointerException, IllegalArgumentException, NodeUnknownException{
        this.contReq = new Requirement("contReq", RequirementSort.CONTAINMENT);
        this.awReq = new Requirement("awReq", RequirementSort.REPLICA_AWARE);
        this.unawReq = new Requirement("unawReq", RequirementSort.REPLICA_UNAWARE);

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        testApp.addNode(this.createNeedy());
        testApp.addNode(this.createServer());

        this.testApp.addStaticBinding(
            new NodeReq("needy", "contReq"), 
            new NodeCap("server", "contCap")
        );
        this.testApp.addStaticBinding(
            new NodeReq("needy", "unawReq"), 
            new NodeCap("server", "unawCap")
        );
        this.testApp.addStaticBinding(
            new NodeReq("needy", "awReq"), 
            new NodeCap("server", "awCap")
        );
    }

    @Test
    public void opEndTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException, 
            OperationNotAvailableException, 
            FailedOperationException
    {
        //EXCEPTION CASES
        //-- parameters validation --
        assertThrows(NullPointerException.class, () -> this.testApp.opEnd(null, "go"));
        assertThrows(IllegalArgumentException.class, () -> this.testApp.opEnd("", "go"));
        assertThrows(InstanceUnknownException.class, () -> this.testApp.opEnd("unknownID", "go"));

        this.testApp.scaleOut("server", "serverID");
        assertThrows(NullPointerException.class, () -> this.testApp.opEnd("serverID", null));
        assertThrows(IllegalArgumentException.class, () -> this.testApp.opEnd("serverID", ""));

        //-- operation failures --
        this.testApp.scaleOutC("needy", "needyID", "serverID");

        //needyID has 1 req satisfied (containment req)
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyID").size() == 1);
        
        //kill the container
        this.testApp.getGlobalState().getRuntimeBindings().remove("serverID");
        this.testApp.getGlobalState().getActiveInstances().remove("serverID");
        //now needyID is a broken instance
        assertTrue(this.testApp.getGlobalState().isBrokenInstance("needyID"));
        //opEnd fails because the instance we pass is a broken instance
        assertThrows(FailedOperationException.class, () -> this.testApp.opEnd("needyID", "go"));

        //this would be automatized in the scaleIn, here I have to explicitally, otherwise
        //when I put again the binding I have 2 bindings about contReq
        this.testApp.getGlobalState().removeRuntimeBinding("needyID", this.contReq);
        //create again the container
        this.testApp.scaleOut("server", "serverID");
        //put back the containment runtime bindding
        this.testApp.getGlobalState().addRuntimeBinding("needyID", this.contReq, "serverID");
        //now needyID is no longer broken
        assertFalse(this.testApp.getGlobalState().isBrokenInstance("needyID"));

        this.testApp.opStart("needyID", "go");
        //needyID has 3 satisfied reqs
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyID").size() == 3);
        //we remove one runtime binding (about awReq)
        this.testApp.getGlobalState().removeRuntimeBinding("needyID", this.awReq);
        //needyID has 1 pending fault (since in the current state it needs every reqs)
        assertTrue(this.testApp.getGlobalState().getPendingFaults().size() == 1);
        //opEnd fails because needyID has a pending fault
        assertThrows(FailedOperationException.class, () -> this.testApp.opEnd("needyID", "go"));
        //put back the runtime binding about awReq
        this.testApp.getGlobalState().addRuntimeBinding("needyID", this.awReq, "serverID");
        //needyID has 0 pending faults
        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").isEmpty());
       
        //REAL TESTS (at this point is identical to scaleOut, not tested all over again)
        this.testApp.opEnd("needyID", "go");
        //needyID is in the new state (state2) since it has completed the op "go"
        assertEquals(
            "state2", 
            this.testApp.getGlobalState().getActiveInstances().get("needyID").getCurrentState()
        );
        //needyID in state2 has only the containment req satisfied
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyID").size() == 1);
        assertEquals(this.contReq, this.testApp.getGlobalState().getSatisfiedReqs("needyID").get(0));

        assertTrue(this.testApp.getGlobalState().getPendingFaults("needyID").isEmpty());
        assertFalse(this.testApp.getGlobalState().isBrokenInstance("needyID"));


    }

    public Node createNeedy(){
        //two states node:i in the initial state it needs req, in the second one no
        Node needy = new Node("needy", new ManagementProtocol("state1"));
        needy.addRequirement(this.contReq);
        needy.addRequirement(this.awReq);
        needy.addRequirement(this.unawReq);

        needy.addOperation("go");

        ManagementProtocol needyMP = needy.getManProtocol();
        needyMP.addState("state2");
        needyMP.addTransition("state1", "go", "state2");

        //rho: state -> needed req in that state
        needyMP.getRho().get("state1" + "go" + "state2").add(this.contReq);
        needyMP.getRho().get("state1" + "go" + "state2").add(this.awReq);
        needyMP.getRho().get("state1" + "go" + "state2").add(this.unawReq);

        return needy;        
    }

    public Node createServer(){
        Node server = new Node("server", new ManagementProtocol("state"));
        server.addCapability("contCap");
        server.addCapability("awCap");
        server.addCapability("unawCap");

        ManagementProtocol serverMP = server.getManProtocol();        
        //gamma: state -> cap offered in that state
        serverMP.getGamma().get("state").add("contCap");
        serverMP.getGamma().get("state").add("awCap");
        serverMP.getGamma().get("state").add("unawCap");

        return server;
    }
}
