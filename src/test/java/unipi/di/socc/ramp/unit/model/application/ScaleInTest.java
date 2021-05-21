package unipi.di.socc.ramp.unit.model.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

//test BOTH scaleIn and autodestroy

public class ScaleInTest {
    
    public Application testApp;
    public Requirement contReq;

    @BeforeEach
    public void before() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException
    {
        this.contReq = new Requirement("contReq", RequirementSort.CONTAINMENT);
        this.testApp = new Application("testApp", PiVersion.GREEDYPI);

        this.testApp.addNode(this.createNeedy());
        this.testApp.addNode(this.createServer());

        this.testApp.addStaticBinding(new NodeReq("needy", "contReq"), new NodeCap("server", "contCap"));

    }

    @Test
    public void scaleInTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException
    {
        //exception cases
        assertThrows(NullPointerException.class, () -> this.testApp.scaleIn(null));
        assertThrows(IllegalArgumentException.class, () -> this.testApp.scaleIn(""));
        assertThrows(InstanceUnknownException.class, () -> this.testApp.scaleIn("unknownID"));

        //REAL TESTS
        //-- scale in tests --
        this.testApp.scaleOut("server", "serverID");
        //now serverID is an active instance
        assertTrue(this.testApp.getGlobalState().getActiveInstances().size() == 1);
        assertNotNull(this.testApp.getGlobalState().getActiveInstances().get("serverID"));
        assertNotNull(this.testApp.getGlobalState().getRuntimeBindings().get("serverID"));

        //now we kill serverID
        this.testApp.scaleIn("serverID");
        //as if serverID was never existed
        assertNull(this.testApp.getGlobalState().getActiveInstances().get("serverID"));
        assertNull(this.testApp.getGlobalState().getRuntimeBindings().get("serverID"));

        //now we create again serverID
        this.testApp.scaleOut("server", "serverID");
        assertTrue(this.testApp.getGlobalState().getActiveInstances().size() == 1);

        //now we create an instance of needy contained by serverID
        this.testApp.scaleOutC("needy", "needyID", "serverID");
        //needyID has 1 satisfied req (about containment)
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("needyID").size() == 1);

        //now we kill needyID
        this.testApp.scaleIn("needyID");
        //as if serverID was never existed
        assertNull(this.testApp.getGlobalState().getActiveInstances().get("needyID"));
        assertNull(this.testApp.getGlobalState().getRuntimeBindings().get("needyID"));

        //-- autodestroy test --
        //create again needyID
        this.testApp.scaleOutC("needy", "needyID", "serverID");
        //now we kill serverID
        this.testApp.scaleIn("serverID");
        /*
         * since serverID is dead needyID became a broken instance
         * there can not be broken instances
         * scaleIn calls autodestroy, that for each broken instance call again scaleIn
         * -> needyID is killed as well 
         */
        assertNull(this.testApp.getGlobalState().getActiveInstances().get("serverID"));
        assertNull(this.testApp.getGlobalState().getRuntimeBindings().get("serverID"));
        assertNull(this.testApp.getGlobalState().getActiveInstances().get("needyID"));
        assertNull(this.testApp.getGlobalState().getRuntimeBindings().get("needyID"));
    }


    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.contReq);

        ManagementProtocol needyMP = needy.getManProtocol();
        //rho: state -> needed req in that state
        needyMP.getRho().get("state").add(this.contReq);

        return needy;
    }

    public Node createServer(){
        Node server = new Node("server", new ManagementProtocol("state"));
        server.addCapability("contCap");

        ManagementProtocol serverMP = server.getManProtocol();
        //gamma: state -> cap offered in that state
        serverMP.getGamma().get("state").add("contCap");

        return server;
    }

}
