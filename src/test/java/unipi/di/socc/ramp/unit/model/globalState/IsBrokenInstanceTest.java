package unipi.di.socc.ramp.unit.model.globalState;

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

public class IsBrokenInstanceTest {
    
    public Application testApp;
    public Requirement containmentReq;
    public Requirement unawareReq;

    @BeforeEach
    public void before()
        throws 
            NullPointerException, 
            NodeUnknownException
    {
        this.containmentReq = new Requirement("containmentReq", RequirementSort.CONTAINMENT);
        this.unawareReq = new Requirement("unawareReq", RequirementSort.REPLICA_UNAWARE);

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.createNeedy());
        this.testApp.addNode(this.createContainmentServer());
        this.testApp.addNode(this.createUnawareServer());

        this.testApp.addStaticBinding(
            new NodeReq("needy", "containmentReq"), 
            new NodeCap("containerServer", "containmentCap")
        );

        this.testApp.addStaticBinding(
            new NodeReq("needy", "unawareReq"), 
            new NodeCap("unawareServer", "unawareCap")
        );
    }

    @Test
    public void isBrokenInstanceTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException
    {
        //exception cases
        assertThrows(NullPointerException.class, () -> this.testApp.getGlobalState().isBrokenInstance(null));
        assertThrows(IllegalArgumentException.class, () -> this.testApp.getGlobalState().isBrokenInstance(""));
        assertThrows(InstanceUnknownException.class, () -> this.testApp.getGlobalState().isBrokenInstance("unknownID"));

        //REAL TESTS
        this.testApp.scaleOut1("unawareServer", "unawareServerID");
        this.testApp.scaleOut1("containerServer", "containerServerID");
        this.testApp.scaleOut2("needy", "needyID", "containerServerID");

        //containerServerID has no requirement at all, so it is not a broken instance
        assertFalse(this.testApp.getGlobalState().isBrokenInstance("containerServerID"));
        
        //needyID has both its requirement satisfied (hence not broken instance)
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyID").size() == 2);
        //needyID is not a broken instance
        assertFalse(this.testApp.getGlobalState().isBrokenInstance("needyID"));

        //now we kill the containerServer
        this.testApp.getGlobalState().getRuntimeBindings().remove("containerServerID");
        this.testApp.getGlobalState().getActiveInstances().remove("containerServerID");

        //now needyID has 1 satisfied req (about unawareReq)
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyID").size() == 1);
        //needyID is a broken instance
        assertTrue(this.testApp.getGlobalState().isBrokenInstance("needyID"));
        
        //we recreate again the container instance
        this.testApp.scaleOut1("containerServer", "containerServerID");
        //we put again the runtime binding
        this.testApp.getGlobalState().addRuntimeBinding("needyID", this.containmentReq, "containerServerID");
        //needyID is no longer broken
        assertFalse(this.testApp.getGlobalState().isBrokenInstance("needyID"));
    }


    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.containmentReq);
        needy.addRequirement(this.unawareReq);

        ManagementProtocol needyMP = needy.getManProtocol();
        
        //rho: state -> needed req in that state
        needyMP.getRho().get("state").add(this.containmentReq);
        needyMP.getRho().get("state").add(this.unawareReq);

        return needy;
    }

    public Node createContainmentServer(){
        Node contServer = new Node("containerServer", new ManagementProtocol("state"));
        contServer.addCapability("containmentCap"); 
        contServer.getManProtocol().getGamma().get("state").add("containmentCap");
        return contServer;
    }

    public Node createUnawareServer(){
        Node unawareServer = new Node("unawareServer", new ManagementProtocol("state"));
        unawareServer.addCapability("unawareCap");
        unawareServer.getManProtocol().getGamma().get("state").add("unawareCap");
        
        return unawareServer;
    }

}
