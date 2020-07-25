package test.UnitTest.GlobalStateTest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.*;

public class AddBindingTest {
    public Application testApp;
    public Node nodeA;
    public Node nodeB;
    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;

    public Requirement req;

    @Before
    public void setUp() throws NullPointerException, RuleNotApplicableException, NodeUnknownException {
        this.req = new Requirement("req", RequirementSort.REPLICA_UNAWARE);

        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();

        this.testApp = new Application("testApp");
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);

        StaticBinding unawareFirstHalf = new StaticBinding("nodeA", "req");
        StaticBinding unawareSecondHalf = new StaticBinding("nodeB", "cap");
        this.testApp.addStaticBinding(unawareFirstHalf, unawareSecondHalf);

        //A has a fault resolvable
        this.instanceOfA = this.testApp.scaleOut1(this.nodeA);
        this.instanceOfB = this.testApp.scaleOut1(this.nodeB);
        
    }

    public Node createNodeA(){
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addRequirement(this.req);

        List<Requirement> reqs = new ArrayList<Requirement>();
        reqs.add(this.req);
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
    public void addBindingNullAskingInstanceTest(){
        this.testApp.getGlobalState().addBinding(null, this.req, this.instanceOfB);
    }

    @Test(expected = NullPointerException.class)
    public void addBindingNullReqTest(){
        this.testApp.getGlobalState().addBinding(this.instanceOfA, null, this.instanceOfB);
    }

    @Test(expected = NullPointerException.class)
    public void addBindingNullServingInstanceTest(){
        this.testApp.getGlobalState().addBinding(this.instanceOfA, this.req, null);
    }

    @Test
    public void addBindingTest(){
        //instanceOfA has a fault and no binding
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get(this.instanceOfA.getID()).size() == 0);
        assertTrue(this.testApp.getGlobalState().getPendingFaults(this.instanceOfA).size() == 1);
        assertTrue(this.testApp.getGlobalState().getResolvableFaults(this.instanceOfA).size() == 1);

        this.testApp.getGlobalState().addBinding(this.instanceOfA, this.req, this.instanceOfB);

        //fault is resolved
        assertTrue(this.testApp.getGlobalState().getPendingFaults(this.instanceOfA).size() == 0);
        //instanceOfA has 1 runtime 
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get(this.instanceOfA.getID()).size() == 1);
    }
}