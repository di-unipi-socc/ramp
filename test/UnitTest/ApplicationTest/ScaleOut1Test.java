package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.NodeUnknownException;
import model.exceptions.RuleNotApplicableException;

public class ScaleOut1Test {

    public Application testApp;

    public Node nodeA;
    public Node nodeB;
    public Node nodeC;

    public NodeInstance instanceOfA; 
    public NodeInstance instanceOfB;

    @Before
    public void setUp() {
        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();
        this.nodeC = this.createNodeC();

        this.testApp = new Application("testApp");
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);
        this.testApp.addNode(this.nodeC);

        StaticBinding firstHalf = new StaticBinding("nodeA", "req");
        StaticBinding secondHalf = new StaticBinding("nodeB", "cap");
        this.testApp.addStaticBinding(firstHalf, secondHalf);
    }

    public Node createNodeA(){
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

    public Node createNodeC(){
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

    @Test(expected = NullPointerException.class)
    public void scaleOut1NullNodeTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException,
            NodeUnknownException 
    {
        this.testApp.scaleOut1(null);
    }

    /**
     * trying to scale out a node that is not a part of app raise an exception
     * 
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     * @throws NodeUnknownException
     */
    @Test(expected = NodeUnknownException.class)
    public void scaleOut1NodeUnknown() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        Node randomNode = new Node("unknownInThisApp", "null", new ManagementProtocol());
        this.testApp.scaleOut1(randomNode);
    }


    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut1NotApplicableTest()
        throws
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        //nodeC has a containment req
        this.testApp.scaleOut1(this.nodeC);
    }

    @Test
    public void scaleOut1Test() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        //scaling out a node with no reqs and no bindings when it is created
        this.instanceOfB = this.testApp.scaleOut1(this.nodeB);
        assertNotNull(this.instanceOfB);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().size() == 1);

        //scaling out the nodeA that has a reqs that can be offred by B
        this.instanceOfA = this.testApp.scaleOut1(this.nodeA);
        assertNotNull(this.instanceOfA);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().size() == 2);
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.instanceOfA).size() == 1);
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get(this.instanceOfA.getID()).get(0).getReq().getName().equals("req"));
    
        this.testApp.scaleIn(this.instanceOfA);
        this.testApp.scaleIn(this.instanceOfB);

        //scaleOut1 first A and then B, A will have a fault
        this.instanceOfA = this.testApp.scaleOut1(this.nodeA);
        this.instanceOfB = this.testApp.scaleOut1(this.nodeB);

        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.instanceOfA).size() == 0);



    
    }
}