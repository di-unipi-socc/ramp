package mprot.lib.test.unitTest.nodeInstanceTest;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import mprot.lib.model.*;
import mprot.lib.model.exceptions.*;

public class GetTransitionByOpTest {

    public Application testApp;
    public Node nodeA;
    public NodeInstance instanceOfA;

    /**
     * create a simple custom application with one node with 3 transition and 3
     * operations
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
        this.nodeA = this.createNodeA();
        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.nodeA);

        this.instanceOfA = testApp.scaleOut1("nodeA", "instanceOfA");
    }

    public Node createNodeA() {
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addState("state2");
        ret.addState("state3");
        ret.addState("state1goToState3state3");
        ret.addState("state1goToState3Bisstate3");
        ret.addState("state3goToState2state2");

        ret.addOperation("goToState2");
        ret.addOperation("goToState3");
        ret.addOperation("goToState3Bis");

        mp.addTransition("state1", "goToState3", "state3");
        mp.addTransition("state1", "goToState3Bis", "state3");
        mp.addTransition("state3", "goToState2", "state2");

        for (String state : ret.getStates())
            mp.addRhoEntry(state, new ArrayList<Requirement>());

        for (String state : ret.getStates())
            mp.addGammaEntry(state, new ArrayList<String>());

        for (String state : ret.getStates())
            mp.addPhiEntry(state, new ArrayList<String>());

        return ret;
    }

    //getTranstionByOp throws a NullPointerException if the passed op is null
    @Test(expected = NullPointerException.class)
    public void getTransitionByOpNullOpTest() {
        this.instanceOfA.getTransitionByOp(null);
    }

    //getTransitionByOp throws a NullPointerException if the passed op is empty
    @Test(expected = IllegalArgumentException.class)
    public void getTransitionByOpEmptyStateTest() {
        this.instanceOfA.getTransitionByOp("");
    }

    @Test
    public void getTransitionByOpTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException,
            OperationNotAvailableException, 
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        assertTrue(this.instanceOfA.getTransitionByOp("goToState3").getName().equals("state1goToState3state3"));
        assertTrue(this.instanceOfA.getTransitionByOp("goToState3Bis").getName().equals("state1goToState3Bisstate3"));

        this.testApp.opStart("instanceOfA", "goToState3");
        this.testApp.opEnd("instanceOfA", "goToState3");

        assertTrue(this.instanceOfA.getTransitionByOp("goToState2").getName().equals("state3goToState2state2"));

        this.testApp.opStart("instanceOfA", "goToState2");
        this.testApp.opEnd("instanceOfA", "goToState2");

        //in state 2 no op is defined so getTransitionByOp should return null
        assertNull(this.instanceOfA.getTransitionByOp("random")); 
    }
}