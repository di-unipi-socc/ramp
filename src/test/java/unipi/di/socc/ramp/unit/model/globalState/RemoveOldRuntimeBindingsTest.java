package unipi.di.socc.ramp.unit.model.globalState;

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
import unipi.di.socc.ramp.core.model.exceptions.RuleNotApplicableException;

public class RemoveOldRuntimeBindingsTest {
    
    //removeOldRuntimeBindingsTest remove satisfiedReqs that are not needed


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
    public void removeOldRuntimeBindingsTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException
    {
        //exception cases
        assertThrows(NullPointerException.class, () -> this.testApp.getGlobalState().removeOldRuntimeBindings(null));
        assertThrows(IllegalArgumentException.class, () -> this.testApp.getGlobalState().removeOldRuntimeBindings(""));
        assertThrows(InstanceUnknownException.class, () -> this.testApp.getGlobalState().removeOldRuntimeBindings("unknownID"));

        //real tests
        this.testApp.scaleOut("server", "serverInstance");
        //create needyInstance
        this.testApp.scaleOut("needy", "needyInstance");
        //scaleOut1 automatically satisfy the needed requirement of needy (addNewRuntimeBindings)
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("needyInstance").size() == 1);

        //now we forcefully put needyInstance in state2 
        this.testApp.getGlobalState().getActiveInstances().get("needyInstance").setCurrentState("state2");
        //in state2 needyInstance requires nothing
        assertTrue(this.testApp.getGlobalState().getActiveInstances().get("needyInstance").getNeededReqs().size() == 0);
        //we remove the old runtime bindings (those satisfied but not needed anymore)
        this.testApp.getGlobalState().removeOldRuntimeBindings("needyInstance");
        //now needyInstance has 0 satisfied reqs
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("needyInstance").size() == 0);
    }


    public Node createNeedy(){
        //two states node:i in the initial state it needs req, in the second one no
        Node needy = new Node("needy", new ManagementProtocol("state1"));
        needy.addRequirement(this.needyReq);

        ManagementProtocol needyMP = needy.getManProtocol();
        needyMP.addState("state2");
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
