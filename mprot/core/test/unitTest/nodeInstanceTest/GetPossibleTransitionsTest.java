package mprot.core.test.unitTest.nodeInstanceTest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import mprot.core.model.*;
import mprot.core.model.exceptions.*;

public class GetPossibleTransitionsTest {

    public Application testApp;
    public Node nodeA;
    public NodeInstance instanceOfA;

    /**
     * create a simple custom application with one node with 3 transitions and 3
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

    @Test
    public void getPossibleTransitionsTest() 
        throws
            IllegalArgumentException, 
            NullPointerException,
            OperationNotAvailableException,
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        ArrayList<Transition> transitions = (ArrayList<Transition>) this.instanceOfA.getPossibleTransitions();
        ArrayList<String> transitionsNames = new ArrayList<String>();  

        for (Transition t : transitions)
            transitionsNames.add(t.getName());

        assertTrue(this.instanceOfA.getPossibleTransitions().size() == 2);
        assertTrue(transitionsNames.contains("state1goToState3state3"));
        assertTrue(transitionsNames.contains("state1goToState3Bisstate3"));

        this.testApp.opStart("instanceOfA", "goToState3");
        this.testApp.opEnd("instanceOfA", "goToState3");

        assertTrue(this.instanceOfA.getPossibleTransitions().size() == 1);
        assertTrue(this.instanceOfA.getPossibleTransitions().get(0).getName().equals("state3goToState2state2"));

        this.testApp.opStart("instanceOfA", "goToState2");
        this.testApp.opEnd("instanceOfA", "goToState2");

        assertTrue(this.instanceOfA.getPossibleTransitions().size() == 0);
    }
}