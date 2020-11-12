package mprot.unitTest.globalStateTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mprot.core.model.*;
import mprot.core.model.exceptions.*;

public class IsBrokenInstanceTest {

    public Application testApp;
    public Node nodeA; // need containment
    public Node nodeB; // container
    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;
    public Requirement reqCont;

    /**
     * create a custom simple application with 2 nodes, nodeA and nodeB nodeA has
     * only 1 state and requires one containment requirement (reqCont) nodeB has 2
     * state, in state1 it offer the cap that satisfy reqCont, in state2 do not
     * offer any caps
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
        this.reqCont = new Requirement("reqCont", RequirementSort.CONTAINMENT);
        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);

        BindingPair firstHalf = new BindingPair("nodeA", "reqCont");
        BindingPair secondHalf = new BindingPair("nodeB", "capCont");
        this.testApp.addStaticBinding(firstHalf, secondHalf);

        this.instanceOfB = this.testApp.scaleOut1("nodeB", "instanceOfB");
        this.instanceOfA = this.testApp.scaleOut2("nodeA", "instanceOfA", "instanceOfB");
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

    //isBrokenInstance throws a NullPointerException if the passed instance is null
    @Test(expected = NullPointerException.class)
    public void isBrokenInstanceNullInstanceTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        this.testApp.getGlobalState().isBrokenInstance(null);
    }

    @Test
    public void isBrokenInstanceTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException,
            OperationNotAvailableException, 
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        //instanceOfB is alive and offering the right cap
        assertFalse(this.testApp.getGlobalState().isBrokenInstance("instanceOfA"));
        assertTrue(this.testApp.getGlobalState().getPendingFaults("instanceOfA").size() == 0);
       
        //in state2 instanceOfB do not offer anymore the containment capabilty
        //hence instanceOfA is not a broken instance but a pending fault
        this.testApp.opStart("instanceOfB", "goToState2");
        this.testApp.opEnd("instanceOfB", "goToState2");

        assertFalse(this.testApp.getGlobalState().isBrokenInstance("instanceOfA"));
        assertTrue(this.testApp.getGlobalState().getPendingFaults("instanceOfA").size() == 1);

        //now we remove instanceOfB from the active nodes, instanceOfA become a broken instance
        this.testApp.getGlobalState().getActiveNodeInstances().remove("instanceOfB");
        assertTrue(this.testApp.getGlobalState().isBrokenInstance("instanceOfA"));
    }

}