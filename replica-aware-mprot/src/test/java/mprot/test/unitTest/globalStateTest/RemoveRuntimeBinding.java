package mprot.test.unitTest.globalStateTest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mprot.core.model.*;
import mprot.core.model.exceptions.*;

public class RemoveRuntimeBinding {
    public Application testApp;
    public Node nodeA;
    public Node nodeB;
    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;

    public Requirement req;

    /**
     * create a simple custom application with two nodes, nodeA and nodeB nodeA has
     * a requirement (req) and nodeB offer the capability needed (cap)
     * 
     * @throws AlreadyUsedIDException
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     */
    @Before
    public void setUp() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            IllegalArgumentException,
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.req = new Requirement("req", RequirementSort.REPLICA_UNAWARE);

        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);

        StaticBinding unawareFirstHalf = new StaticBinding("nodeA", "req");
        StaticBinding unawareSecondHalf = new StaticBinding("nodeB", "cap");
        this.testApp.addStaticBinding(unawareFirstHalf, unawareSecondHalf);

        this.instanceOfB = this.testApp.scaleOut1("nodeB", "instanceOfB");
        //instanceOfA has a runtime binding with B
        this.instanceOfA = this.testApp.scaleOut1("nodeA", "instanceOfA");
    }

    public Node createNodeA(){
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addRequirement(this.req);

        List<Requirement> reqs = new ArrayList<Requirement>();
        reqs.add(this.req);
        mp.addRhoEntry("state1", reqs);

        mp.addPhiEntry("state1", new ArrayList<String>());
        mp.addGammaEntry("state1", new ArrayList<String>());

        return ret;
    }

    public Node createNodeB(){
        Node ret = new Node("nodeB", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");

        ret.addCapability("cap");

        List<String> caps = new ArrayList<>();
        caps.add("cap");

        mp.addRhoEntry("state1", new ArrayList<>());
        mp.addGammaEntry("state1", caps);
        mp.addPhiEntry("state1", new ArrayList<>());

        return ret;
    }

    //removeRuntimeBinding throws a NullPointerException is the passed instance is null
    @Test(expected = NullPointerException.class)
    public void removeRuntimeBindingInstanceNullTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        this.testApp.getGlobalState().removeRuntimeBinding(null, this.req);
    }   

    //removeRuntimeBinding throws a NullPointerException is the passed req is null
    @Test(expected = NullPointerException.class)
    public void removeRuntimeBindingReqNullTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException {
        this.testApp.getGlobalState().removeRuntimeBinding("instanceOfA", null);
    }

    @Test
    public void removeRuntimeBindingTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        //instanceOfA has a runtime binding with instanceofB
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").size() == 1);
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").get(0).getNodeInstanceID().equals(this.instanceOfB.getID()));

        //delete the binding
        this.testApp.getGlobalState().removeRuntimeBinding("instanceOfA", this.req);

        //now instanceOfA has no runtime bindings
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").size() == 0);

    }

}