package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.*;
import exceptions.AlreadyUsedIDException;
import exceptions.InstanceUnknownException;
import exceptions.NodeUnknownException;
import exceptions.RuleNotApplicableException;

public class ScaleInTest {

    public Application testApp;

    public Node nodeA;
    public Node nodeB;
    public Node nodeC;

    public Node unkwownNode;

    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;
    public NodeInstance instanceOfC;

    public NodeInstance unknownInstance;

    /**
     * creates a custom singole application with 3 nodes, nodeA, nodeB and nodeC
     * 
     * nodeA has - two requirement - a contaiment requirement AreqC that is
     * satisfied by nodeC - a replica-unaware requirement AreqB that is satisfied by
     * nodeB - one capability - AcapB that satisfies the requirement of nodeB
     * 
     * nodeB has - one requirement - a replica-unaware requirement BreqA that is
     * satisfied by nodeA - one capability - BcapA that satisfies AreqB of nodeA
     * 
     * nodeC has only one capability (CcapA) that satisfies one requirement of nodeA
     * (AreqC)
     * 
     * unkownNode is a node that is not part of the application
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
            NodeUnknownException,
            IllegalArgumentException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();
        this.nodeC = this.createNodeC();
        this.unkwownNode = this.createUnknownNode();
        this.unknownInstance = new NodeInstance(this.unkwownNode, "state1", "AAAAAAAA");

        this.testApp = new Application("testApp");
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);
        this.testApp.addNode(this.nodeC);

        StaticBinding firstHalfAReqC = new StaticBinding("nodeA", "AreqC");
        StaticBinding secondHalfAReqC = new StaticBinding("nodeC", "CcapA");
        this.testApp.addStaticBinding(firstHalfAReqC, secondHalfAReqC);

        StaticBinding firstHalfAReqB = new StaticBinding("nodeA", "AreqB");
        StaticBinding secondHalfAReqB = new StaticBinding("nodeB", "BcapA");
        this.testApp.addStaticBinding(firstHalfAReqB, secondHalfAReqB);

        StaticBinding firstHalfBreqA = new StaticBinding("nodeB", "BreqA");
        StaticBinding secondHalfBreqA = new StaticBinding("nodeA", "AcapB");
        this.testApp.addStaticBinding(firstHalfBreqA, secondHalfBreqA);

        this.instanceOfC = this.testApp.scaleOut1(this.nodeC.getName(), "instanceOfC");
        this.instanceOfA = this.testApp.scaleOut2(this.nodeA.getName(), "instanceOfA", this.instanceOfC.getID());
        this.instanceOfB = this.testApp.scaleOut1(this.nodeB.getName(), "instanceOfB");
    }

    public Node createNodeA(){
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addCapability("AcapB");
        Requirement AreqC = new Requirement("AreqC", RequirementSort.CONTAINMENT);
        Requirement AreqB = new Requirement("AreqB", RequirementSort.REPLICA_UNAWARE);
        ret.addRequirement(AreqC);
        ret.addRequirement(AreqB);

        List<Requirement> reqs = new ArrayList<>();
        reqs.add(AreqC);
        reqs.add(AreqB);
        mp.addRhoEntry("state1", reqs);

        List<String> caps = new ArrayList<>();
        caps.add("AcapB");
        mp.addGammaEntry("state1", caps);

        mp.addPhiEntry("state1", new ArrayList<>());
    
        return ret;
    }

    public Node createNodeB(){
        Node ret = new Node("nodeB", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addCapability("BcapA");

        Requirement BreqA = new Requirement("BreqA", RequirementSort.REPLICA_UNAWARE);
        ret.addRequirement(BreqA);

        List<Requirement> reqs = new ArrayList<>();
        reqs.add(BreqA);
        mp.addRhoEntry("state1", reqs);

        List<String> caps = new ArrayList<>();
        caps.add("BcapA");
        mp.addGammaEntry("state1", caps);

        mp.addPhiEntry("state1", new ArrayList<>());

        return ret;
    }

    public Node createNodeC(){
        Node ret = new Node("nodeC", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addCapability("CcapA");

        mp.addRhoEntry("state1", new ArrayList<Requirement>());
        
        List<String> caps = new ArrayList<>();
        caps.add("CcapA");
        mp.addGammaEntry("state1", caps);

        mp.addPhiEntry("state1", new ArrayList<String>());
        return ret;
    }

    public Node createUnknownNode(){
        Node ret = new Node("unknownNode", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();
        ret.addState("state1");

        mp.addRhoEntry("state1", new ArrayList<Requirement>());
        mp.addGammaEntry("state1", new ArrayList<String>());
        mp.addPhiEntry("state1", new ArrayList<String>());

        return ret;
    }

    //scaleIn throws a NullPointerException when the passed instanceID is null
    @Test(expected = NullPointerException.class)
    public void scaleInNullInstanceIDTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        this.testApp.scaleIn(null);
    }

    //scaleIn throws a IllegalArgumentException when the passed instanceID is empty
    @Test(expected = IllegalArgumentException.class)
    public void scaleInEmptyInstanceIDTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        this.testApp.scaleIn("");
    }

    //scaleIn throws a RuleNotApplicableException when the passed instance is an instance of an unkown node
    @Test(expected = RuleNotApplicableException.class)
    public void scaleInUnknownInstance() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        this.testApp.scaleIn(this.unknownInstance.getID());
    }

    //test that if we scaleIn a container instance (instanceOfC) is destroyed the container and the contained
    //instance, and all the runtime bindings of both instances are deleted
    @Test
    public void scaleInOnContainerTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {

        //has only one binding because B is created after A, so there is resolvable fault
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.instanceOfA).size() == 1);

        Fault f = this.testApp.getGlobalState().getResolvableFaults(this.instanceOfA).get(0);
        this.testApp.autoreconnect(this.instanceOfA.getID(), f.getReq());

        //now A has the 2 binding: 1 with C for the containment and 1 with B for the other requirement
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.instanceOfA).size() == 2);
        //B has only 1 binding with A, since B has a requirement that A satisfies
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.instanceOfB).size() == 1);
        //C has no reqs
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.instanceOfC).size() == 0);

        //the scaleIn destroy instanceOfC so it is destroyed even instanceOfA that's contained in instanceOfC
        this.testApp.scaleIn(this.instanceOfC.getID());

        //the destroyed instances is remove from the set of active instances
        assertNull(this.testApp.getGlobalState().getActiveNodeInstances().get(this.instanceOfC.getID()));
        assertNull(this.testApp.getGlobalState().getActiveNodeInstances().get(this.instanceOfA.getID()));
        
        //the are no more binding for the destroyed instances
        assertNull(this.testApp.getGlobalState().getRuntimeBindings().get(this.instanceOfA.getID()));
        assertNull(this.testApp.getGlobalState().getRuntimeBindings().get(this.instanceOfC.getID()));

        assertTrue(this.testApp.getGlobalState().getPendingFaults(this.instanceOfB).size() == 1);
    }

    //test that is a non container instance is destroyed only that instance and its runtime bindings are destroyed
    @Test
    public void scaleOut1NotContainerTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException,
            InstanceUnknownException 
    {
        //has only one binding because B is created after A, so there is resolvable fault
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.instanceOfA).size() == 1);

        Fault f = this.testApp.getGlobalState().getResolvableFaults(this.instanceOfA).get(0);
        this.testApp.autoreconnect(this.instanceOfA.getID(), f.getReq());

        //now A has the 2 binding: 1 with C for the containment and 1 with B for the other requirement
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.instanceOfA).size() == 2);
        //B has only 1 binding with A, since B has a requirement that A satisfies
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.instanceOfB).size() == 1);

        this.testApp.scaleIn(this.instanceOfB.getID());

        assertNull(this.testApp.getGlobalState().getActiveNodeInstances().get(this.instanceOfB.getID()));
        assertNull(this.testApp.getGlobalState().getRuntimeBindings().get(this.instanceOfB.getID()));

        //since B is eliminated A has a fault, because B was providing a cap
        assertTrue(this.testApp.getGlobalState().getPendingFaults(this.instanceOfA).size() == 1);

    }

}