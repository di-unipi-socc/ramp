package mprot.lib.test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mprot.lib.model.*;
import mprot.lib.model.exceptions.*;

public class ScaleOut1Test {

    public Application testApp;

    public Node nodeA;
    public Node nodeB;
    public Node nodeC;

    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;

    /**
     * create a custom simple application with 3 nodes, nodeA, nodeB and nodeC nodeA
     * has a replica unaware requirement that is satisfied by nodeB nodeB has only
     * the capability that is is offering nodeC has a containment requirment
     */
    @Before
    public void setUp() {
        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();
        this.nodeC = this.createNodeC();

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);
        this.testApp.addNode(this.nodeC);

        StaticBinding firstHalf = new StaticBinding("nodeA", "req");
        StaticBinding secondHalf = new StaticBinding("nodeB", "cap");
        this.testApp.addStaticBinding(firstHalf, secondHalf);
    }

    public Node createNodeA() {
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");

        Requirement req = new Requirement("req", RequirementSort.REPLICA_UNAWARE);
        ret.addRequirement(req);

        List<Requirement> reqs = new ArrayList<>();
        reqs.add(req);

        mp.addRhoEntry("state1", reqs);
        mp.addGammaEntry("state1", new ArrayList<>());
        mp.addPhiEntry("state1", new ArrayList<>());

        return ret;
    }

    public Node createNodeC() {
        Node ret = new Node("nodeC", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");

        Requirement req = new Requirement("req", RequirementSort.CONTAINMENT);
        ret.addRequirement(req);

        List<Requirement> reqs = new ArrayList<>();
        reqs.add(req);
        mp.addRhoEntry("state1", reqs);

        mp.addGammaEntry("state1", new ArrayList<>());
        mp.addPhiEntry("state1", new ArrayList<>());

        return ret;
    }

    public Node createNodeB() {
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

    // scaleOut1 throws a NullPointerException when the passed nodeName is null
    @Test(expected = NullPointerException.class)
    public void scaleOut1NullNodeNameTest() 
        throws
            NullPointerException, 
            RuleNotApplicableException, 
            IllegalArgumentException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.testApp.scaleOut1(null, "dontcare");
    }

    //scaleOut1 throws an IllegalArgumentException when the passed nodeName is empty
    @Test(expected = IllegalArgumentException.class)
    public void scaleOut1EmptyNodeNameTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException,
            RuleNotApplicableException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.testApp.scaleOut1("", "dontcare");
    }

    //scaleOut1 throws AlreadyUsedIDException if the ID is already in use by another instance
    @Test(expected = AlreadyUsedIDException.class)
    public void scaleOut1AlreadyUsedIDTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException,
            RuleNotApplicableException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.instanceOfA = this.testApp.scaleOut1("nodeA", "test");
        this.instanceOfB = this.testApp.scaleOut1("nodeB", "test");
    }

    //scaleOut1 throws a RuleNotApplicable when the passed nodeName is not an application's nodes 
    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut1NodeUnknown() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            IllegalArgumentException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.testApp.scaleOut1("nodeNameUnknown", "dontcare");
    }

    //scaleOut1 throws a RuleNotAppilicableException if the passed node has a containment requirement
    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut1NotApplicableTest()
        throws
            NullPointerException, 
            RuleNotApplicableException, 
            IllegalArgumentException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        //nodeC has a containment req
        this.testApp.scaleOut1("nodeC", "instanceOfC");
    }

    @Test
    public void scaleOut1Test() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            IllegalArgumentException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        //scaling out a node with no reqs and no bindings when it is created
        this.instanceOfB = this.testApp.scaleOut1("nodeB", "instanceOfB");
        assertNotNull(this.instanceOfB);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().size() == 1);

        //scaling out the nodeA that has a reqs that can be offred by B
        this.instanceOfA = this.testApp.scaleOut1("nodeA", "instanceOfA");
        assertNotNull(this.instanceOfA);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().size() == 2);
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("instanceOfA").size() == 1);
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").get(0).getReq().getName().equals("req"));
    
        this.testApp.scaleIn("instanceOfA");
        this.testApp.scaleIn("instanceOfB");

        //scaleOut1 first A and then B, A will have a fault
        this.instanceOfA = this.testApp.scaleOut1("nodeA", "instanceOfA");
        this.instanceOfB = this.testApp.scaleOut1("nodeB", "instanceOfB");

        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs("instanceOfA").size() == 0);
    }
}