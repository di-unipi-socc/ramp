package test.UnitTest.GlobalStateTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.FailedOperationException;
import model.exceptions.NodeUnknownException;
import model.exceptions.OperationNotAvailableException;
import model.exceptions.RuleNotApplicableException;

public class IsBrokenInstanceTest {

    public Application testApp;
    public Node nodeA; // need containment
    public Node nodeB; // container
    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;
    public Requirement reqCont;

    @Before
    public void setUp() throws NullPointerException, RuleNotApplicableException, NodeUnknownException {
        this.reqCont = new Requirement("reqCont", RequirementSort.CONTAINMENT);
        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();

        this.testApp = new Application("testApp");
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);

        StaticBinding firstHalf = new StaticBinding("nodeA", "reqCont");
        StaticBinding secondHalf = new StaticBinding("nodeB", "capCont");
        this.testApp.addStaticBinding(firstHalf, secondHalf);

        this.instanceOfB = this.testApp.scaleOut1(this.nodeB);
        this.instanceOfA = this.testApp.scaleOut2(this.nodeA, this.instanceOfB);
    }

    public Node createNodeA() {
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addRequirement(this.reqCont);
        ret.addState("state1");

        List<Requirement> reqs = new ArrayList<Requirement>();
        reqs.add(this.reqCont);
        mp.addRhoEntry("state1", reqs);

        mp.addGammaEntry("state1", new ArrayList<>());
        mp.addPhiEntry("state1", new ArrayList<>());

        return ret;
    }

    public Node createNodeB() {
        Node ret = new Node("nodeB", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addState("state2");
        ret.addState("state1goToState2state2");

        ret.addOperation("goToState2");

        ret.addCapability("capCont");

        mp.addTransition("state1", "goToState2", "state2");


        mp.addRhoEntry("state1", new ArrayList<>());
        mp.addRhoEntry("state2", new ArrayList<>());
        mp.addRhoEntry("state1goToState2state2", new ArrayList<>());

        List<String> caps = new ArrayList<>();
        caps.add("capCont");
        mp.addGammaEntry("state1", caps);
        mp.addGammaEntry("state2", new ArrayList<>());
        mp.addGammaEntry("state1goToState2state2", new ArrayList<>());

        mp.addPhiEntry("state1", new ArrayList<>());
        mp.addPhiEntry("state2", new ArrayList<>());
        mp.addPhiEntry("state1goToState2state2", new ArrayList<>());

        return ret;
    }

    @Test(expected = NullPointerException.class)
    public void isBrokenInstanceNullInstanceTest() {
        this.testApp.getGlobalState().isBrokenInstance(null);
    }

    @Test
    public void isBrokenInstanceTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException,
            OperationNotAvailableException, 
            FailedOperationException 
    {
        //now instanceOfB is offering the right cap
        assertFalse(this.testApp.getGlobalState().isBrokenInstance(this.instanceOfA));
       
        this.testApp.opStart(this.instanceOfB, "goToState2");
        this.testApp.opEnd(this.instanceOfB, "goToState2");

        //in state2 instanceOfB do not offer anymore the containment capabilty
        //hence instanceOfA is now a broken instance
        assertTrue(this.testApp.getGlobalState().isBrokenInstance(this.instanceOfA));
    }

}