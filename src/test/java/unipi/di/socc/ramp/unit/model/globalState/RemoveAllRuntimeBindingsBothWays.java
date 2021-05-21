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

public class RemoveAllRuntimeBindingsBothWays {
    
    //simple 2 nodes application with cycling requirements 
    //(nodeA requires reqA and nodeB requires reqB. nodeA offer capB for reqB and viceversa)
    public Application testApp;


    public Requirement reqA; 
    public Requirement reqB;

    @BeforeEach
    public void before() throws NullPointerException, IllegalArgumentException, NodeUnknownException{
        this.reqA = new Requirement("reqA", RequirementSort.REPLICA_UNAWARE);
        this.reqB = new Requirement("reqB", RequirementSort.REPLICA_UNAWARE);

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);

        this.testApp.addNode(this.createNodeA());
        this.testApp.addNode(this.createNodeB());

        this.testApp.addStaticBinding(new NodeReq("nodeA", "reqA"), new NodeCap("nodeB", "capA"));
        this.testApp.addStaticBinding(new NodeReq("nodeB", "reqB"), new NodeCap("nodeA", "capB"));
    }

    @Test
    public void removeAllRunBindingsBothWaysTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException
    {
        //exception cases
        assertThrows(
            NullPointerException.class, 
            () -> this.testApp.getGlobalState().removeAllRuntimeBindingsBothWays(null)
        );
        assertThrows(
            IllegalArgumentException.class, 
            () -> this.testApp.getGlobalState().removeAllRuntimeBindingsBothWays("")
        );
        assertThrows(
            InstanceUnknownException.class, 
            () -> this.testApp.getGlobalState().removeAllRuntimeBindingsBothWays("unknownID")
        );

        //real tests
        this.testApp.scaleOut("nodeA", "instanceA");
        this.testApp.scaleOut("nodeB", "instanceB");
        this.testApp.getGlobalState().addNewRuntimeBindings("instanceA");

        //now both nodes have what they needs
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("instanceA").size() == 1);
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceA").size() == 1);

        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("instanceB").size() == 1);
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceB").size() == 1);

        assertTrue(this.testApp.getGlobalState().getPendingFaults("instanceA").isEmpty());
        assertTrue(this.testApp.getGlobalState().getPendingFaults("instanceB").isEmpty());

        //now we remove bindings both ways of instanceA
        this.testApp.getGlobalState().removeAllRuntimeBindingsBothWays("instanceA");
        
        //instanceA have no satisfied reqs now
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("instanceA").isEmpty());
        //since instanceA was a server for instanceB now even instanceB has no satisfied reqs
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("instanceB").isEmpty());
    }

    public Node createNodeA(){
        Node nodeA = new Node("nodeA", new ManagementProtocol("state"));
        nodeA.addRequirement(this.reqA);
        nodeA.addCapability("capB");

        ManagementProtocol nodeAMP = nodeA.getManProtocol();
        nodeAMP.getRho().get("state").add(this.reqA);
        nodeAMP.getGamma().get("state").add("capB");
       
        return nodeA;
    }

    public Node createNodeB(){
        Node nodeB = new Node("nodeB", new ManagementProtocol("state"));
        nodeB.addRequirement(this.reqB);
        nodeB.addCapability("capA");

        ManagementProtocol nodeBMP = nodeB.getManProtocol();
        nodeBMP.getRho().get("state").add(this.reqB);
        nodeBMP.getGamma().get("state").add("capA");
       
        return nodeB;
    }



}
