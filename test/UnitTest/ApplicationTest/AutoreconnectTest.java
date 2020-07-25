package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.NodeUnknownException;
import model.exceptions.RuleNotApplicableException;

public class AutoreconnectTest {
    public Application testApp;
    public Node nodeA;
    public Node nodeB;
    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;

    public Requirement reqUnaware;
    public Requirement faultyReq;

    @Before
    public void setUp() throws NullPointerException, RuleNotApplicableException, NodeUnknownException {
        this.reqUnaware = new Requirement("req", RequirementSort.REPLICA_UNAWARE);
        this.faultyReq = new Requirement("faultyReq", RequirementSort.REPLICA_UNAWARE);

        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();

        this.testApp = new Application("testApp");
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);

        StaticBinding unawareFirstHalf = new StaticBinding("nodeA", "req");
        StaticBinding unawareSecondHalf = new StaticBinding("nodeB", "cap");
        this.testApp.addStaticBinding(unawareFirstHalf, unawareSecondHalf);

        //scaling out nodeB so nodeA has 2 fault, 1 resolvable, 1 pending
        this.instanceOfA = this.testApp.scaleOut1(this.nodeA);
        this.instanceOfB = this.testApp.scaleOut1(this.nodeB);
        

    }

    public Node createNodeA(){
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addRequirement(this.faultyReq);
        ret.addRequirement(this.reqUnaware);

        List<Requirement> reqs = new ArrayList<Requirement>();
        reqs.add(this.reqUnaware);
        reqs.add(this.faultyReq);
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

    @Test(expected = NullPointerException.class)
    public void autoreconnectInstanceNullTest() throws NullPointerException, RuleNotApplicableException {
        this.testApp.autoreconnect(null, this.reqUnaware);
    }

    @Test(expected = NullPointerException.class)
    public void autoreconnectReqNullTest() throws NullPointerException, RuleNotApplicableException {
        this.testApp.autoreconnect(this.instanceOfA, null);
    }

    @Test
    public void autoreconnectTest() throws NullPointerException, RuleNotApplicableException {
        //instanceOfA has 2 pending fault
        assertTrue(this.testApp.getGlobalState().getPendingFaults(this.instanceOfA).size() == 2);
        //one of the 2 pending fault is resolvable (reqUnaware)
        assertTrue(this.testApp.getGlobalState().getResolvableFaults(this.instanceOfA).size() == 1);

        //we fix the resolvable fault
        this.testApp.autoreconnect(this.instanceOfA, this.reqUnaware);

        //now instanceOfA has only one fault and a not resolvable fault (faultyReq)
        assertTrue(this.testApp.getGlobalState().getPendingFaults(this.instanceOfA).size() == 1);
        assertTrue(this.testApp.getGlobalState().getResolvableFaults(this.instanceOfA).size() == 0);

    }

}