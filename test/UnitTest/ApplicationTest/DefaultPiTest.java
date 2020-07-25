package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.NodeUnknownException;
import model.exceptions.RuleNotApplicableException;

public class DefaultPiTest {

    public Application testApp;
    public Node nodeA;
    public Node nodeB;
    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;
    public Requirement testReq;  

    @Before
    public void setUp() throws NullPointerException, RuleNotApplicableException, NodeUnknownException {
        this.testReq = new Requirement("testReq", RequirementSort.REPLICA_UNAWARE);
        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();

        this.testApp = new Application("testApp");

        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);

        //just one static binding: nodeA -> nodeB
        //the nodeA requires the requirement testReq
        StaticBinding firstHalf = new StaticBinding("nodeA", "testReq");
        //the nodeB offer the capability testCap
        StaticBinding secondHalf = new StaticBinding("nodeB", "testCap");
        this.testApp.addStaticBinding(firstHalf, secondHalf);

        this.instanceOfA = this.testApp.scaleOut1(nodeA);
        this.instanceOfB = this.testApp.scaleOut1(nodeB);
    }


    public Node createNodeB(){
        Node ret = new Node("nodeB", "testState", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();
        ret.addState("testState");

        ret.addCapability("testCap");

        mp.addRhoEntry("testState", new ArrayList<Requirement>());
        
        List<String> runningCaps = new ArrayList<>();
        runningCaps.add("testCap");
        mp.addGammaEntry("testState", runningCaps);

        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());

        return ret;
    }

    public Node createNodeA(){
        Node ret = new Node("nodeA", "testState", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("testState");

        ret.addRequirement(this.testReq);
       
        List<Requirement> testReqs = new ArrayList<>();
        testReqs.add(this.testReq);
        mp.addRhoEntry("testState", testReqs);

        //gamma: state -> caps offered in that state
        for (String state : ret.getStates())
            mp.addGammaEntry(state, new ArrayList<String>());

        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());
        
        return ret;
    }

    @Test(expected = NullPointerException.class)
    public void defaultPINullAskingInstanceTest(){
        Requirement random = new Requirement("random", RequirementSort.REPLICA_UNAWARE);
        this.testApp.defaultPi(null, random);
    }


    @Test(expected = NullPointerException.class)
    public void defaultPINullRequirementTest(){
        this.testApp.defaultPi(this.instanceOfA, null);
    }

    @Test
    public void defaultPiTest(){
        NodeInstance returned = this.testApp.defaultPi(this.instanceOfA, this.testReq);
        assertTrue("wrong instance", returned.getID().equals(this.instanceOfB.getID()));
    }



}