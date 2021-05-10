package unipi.di.socc.ramp.unit.model.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import unipi.di.socc.ramp.core.model.exceptions.AlreadyUsedIDException;
import unipi.di.socc.ramp.core.model.exceptions.InstanceUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.OperationNotAvailableException;
import unipi.di.socc.ramp.core.model.exceptions.RuleNotApplicableException;

public class OpStartTest {
 
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

        this.testApp.addStaticBinding(new NodeReq("needy", "needyReq"), new NodeCap("server", "serverCap"));
    }


    @Test
    public void opStartTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException, 
            OperationNotAvailableException
    {
        //exception cases
        assertThrows(NullPointerException.class, () -> this.testApp.opStart(null, "go"));
        assertThrows(IllegalArgumentException.class, () -> this.testApp.opStart("", "go"));
        assertThrows(InstanceUnknownException.class, () -> this.testApp.opStart("unknownID", "go"));

        this.testApp.scaleOut1("server", "serverInstance");
        assertThrows(NullPointerException.class, () -> this.testApp.opStart("serverInstance", null));
        assertThrows(IllegalArgumentException.class, () -> this.testApp.opStart("serverInstance", ""));
        //undefined operation of a node (server has no op)
        assertThrows(OperationNotAvailableException.class, () -> this.testApp.opStart("serverInstance", "op"));

        //real tests
        this.testApp.scaleOut1("needy", "needyInstance");
        //needyInstance has one requirement (needyReq) satisfied thx to scaleOut1
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyInstance").size() == 1);

        //now we make the operation
        this.testApp.opStart("needyInstance", "go");
        //needyInstance is in a transient state
        assertEquals("state1gostate2", this.testApp.getGlobalState().getActiveInstances().get("needyInstance").getCurrentState());
        //needyInstance now do not need anything
        assertTrue(this.testApp.getGlobalState().getActiveInstances().get("needyInstance").getNeededReqs().isEmpty());
        //since needyInstance do not need anything opStart has called removeOldRuntimeBindings and needyInstance has no RBs
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("needyInstance").isEmpty());
    }

    public Node createNeedy(){
        //two states node:i in the initial state it needs req, in the second one no
        Node needy = new Node("needy", new ManagementProtocol("state1"));
        needy.addRequirement(this.needyReq);
        needy.addOperation("go");

        ManagementProtocol needyMP = needy.getManProtocol();
        needyMP.addState("state2");
        needyMP.addTransition("state1", "go", "state2");

        //rho: state -> needed req in that state
        needyMP.getRho().get("state1").add(this.needyReq);
       
        return needy;        
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
