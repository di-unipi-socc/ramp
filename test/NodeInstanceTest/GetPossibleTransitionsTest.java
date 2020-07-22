package test.NodeInstanceTest;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.FailedOperationException;
import model.exceptions.NodeUnknownException;
import model.exceptions.OperationNotAvailableException;
import model.exceptions.RuleNotApplicableException;

public class GetPossibleTransitionsTest {

    public Application testApp;
    public Node nodeA;
    public NodeInstance instanceOfA;

    @Before
    public void setUp() throws NullPointerException, RuleNotApplicableException, NodeUnknownException {
        this.nodeA = this.createNodeA();
        this.testApp = new Application("testApp");
        this.testApp.addNode(this.nodeA);

        this.instanceOfA = testApp.scaleOut1(this.nodeA);
    }

    public Node createNodeA() {
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getMp();

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

    @Test
    public void getPossibleTransitionsTest() throws IllegalArgumentException, NullPointerException,
            OperationNotAvailableException, FailedOperationException {

        ArrayList<Transition> transitions = (ArrayList<Transition>) this.instanceOfA.getPossibleTransitions();
        ArrayList<String> transitionsNames = new ArrayList<String>();  

        for (Transition t : transitions)
            transitionsNames.add(t.getName());

        assertTrue(this.instanceOfA.getPossibleTransitions().size() == 2);
        assertTrue(transitionsNames.contains("state1goToState3state3"));
        assertTrue(transitionsNames.contains("state1goToState3Bisstate3"));

        this.testApp.opStart(this.instanceOfA, "goToState3");
        this.testApp.opEnd(this.instanceOfA, "goToState3");

        assertTrue(this.instanceOfA.getPossibleTransitions().size() == 1);
        assertTrue(this.instanceOfA.getPossibleTransitions().get(0).getName().equals("state3goToState2state2"));

        this.testApp.opStart(this.instanceOfA, "goToState2");
        this.testApp.opEnd(this.instanceOfA, "goToState2");

        assertNull(this.instanceOfA.getPossibleTransitions());

    }
}