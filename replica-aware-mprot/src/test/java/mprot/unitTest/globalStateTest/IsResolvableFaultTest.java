package mprot.unitTest.globalStateTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mprot.core.model.*;
import mprot.core.model.exceptions.*;

public class IsResolvableFaultTest {

    public Application testApp;
    public Node nodeA;
    public Node nodeB;

    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;
    public NodeInstance secondInstanceOfB;

    /**
     * create a custom simple application with 2 nodes, nodeA and nodeB nodeA has
     * two states, state1 and state2. in state1 nodeA requires req, in state2
     * requires req2 req2 will be always non resolvable since there is no instance
     * with the right cap nodeB has 1 state in which it offer one capability (that
     * satisfy req)
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
        this.nodeB = this.createNodeB();

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);

        BindingPair firstHalf = new BindingPair("nodeA", "req");
        BindingPair secondHalf = new BindingPair("nodeB", "cap");
        this.testApp.addStaticBinding(firstHalf, secondHalf);

        this.instanceOfB = this.testApp.scaleOut1("nodeB", "instanceOfB");
        this.instanceOfA = this.testApp.scaleOut1("nodeA", "instanceOfA");
        this.secondInstanceOfB = this.testApp.scaleOut1("nodeB", "instanceOfB1");
    }

    public Node createNodeA() {
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();
        
        ret.addState("state1");
        ret.addState("state2");
        ret.addState("state1goToState2state2");

        ret.addOperation("goToState2");

        mp.addTransition("state1", "goToState2", "state2");

        Requirement req = new Requirement("req", RequirementSort.REPLICA_UNAWARE);
        ret.addRequirement(req);

        Requirement req2 = new Requirement("req2", RequirementSort.REPLICA_UNAWARE);
        ret.addRequirement(req2);

        List<Requirement> reqs = new ArrayList<>();
        reqs.add(req);

        List<Requirement> reqs2 = new ArrayList<>();
        reqs2.add(req2);

        mp.addRhoEntry("state1", reqs);
        mp.addRhoEntry("state2", reqs2);
        mp.addRhoEntry("state1goToState2state2", new ArrayList<>());

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

    //isResolvableFault throws a NullPointerException if the passed instance is null    
    @Test(expected = NullPointerException.class)
    public void isResolvableFaultNullFaultTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        this.testApp.getGlobalState().isResolvableFault(null);
    }

    @Test
    public void isResolvableFaultTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException,
            IllegalArgumentException, 
            FailedOperationException, 
            OperationNotAvailableException, 
            InstanceUnknownException 
    {
        //now there is no fault (instanceOfA is in state1 and instanceOfB is offering the right cap)
        assertTrue(this.testApp.getGlobalState().getPendingFaults().isEmpty());
        assertTrue(this.testApp.getGlobalState().getResolvableFaults().isEmpty());

        //A has a binding with B
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").size() == 1);
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").get(0).getReq().getName().equals("req"));
        //the binding is with the first instance of b
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").get(0).getNodeInstanceID().equals(this.instanceOfB.getID()));

        //now we kill instanceOfB and we get a fault (resolvable, thanks to secondInstanceOfB)
        this.testApp.scaleIn("instanceOfB");
        assertTrue(this.testApp.getGlobalState().getPendingFaults("instanceOfA").size() == 1);
        Fault f = this.testApp.getGlobalState().getResolvableFaults("instanceOfA").get(0);
        assertTrue(this.testApp.getGlobalState().isResolvableFault(f));

        //this now ricreate the binding of AreqB with secondInstanceOfB
        this.testApp.autoreconnect(f);

        //now there is no fault (once again, as before)
        assertTrue(this.testApp.getGlobalState().getPendingFaults().isEmpty());
        assertTrue(this.testApp.getGlobalState().getResolvableFaults().isEmpty());

        //A has a binding with B (secondInstanceOfB)
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").size() == 1);
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").get(0).getReq().getName().equals("req"));
        //the binding is with the second instance of b
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").get(0).getNodeInstanceID().equals(this.secondInstanceOfB.getID()));

        //now we move A to state2 where there will be a fault not resolvable
        this.testApp.opStart("instanceOfA", "goToState2");
        this.testApp.opEnd("instanceOfA", "goToState2");

        //there is now a pending fault 
        assertTrue(this.testApp.getGlobalState().getPendingFaults().size() == 1);

        //the pending fault is not resolvable
        Fault f1 = this.testApp.getGlobalState().getPendingFaults("instanceOfA").get(0);
        assertFalse(this.testApp.getGlobalState().isResolvableFault(f1));

        assertTrue(this.testApp.getGlobalState().getResolvableFaults().size() == 0);

    }
}