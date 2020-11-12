package mprot.unitTest.globalStateTest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mprot.core.model.*;
import mprot.core.model.exceptions.*;

public class RemoveAllRunBindingsBothWaysTest {

    public Application testApp;

    // A is serverd by B and A is serving C
    public Node nodeA;
    public Node nodeB;
    public Node nodeC;
    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;
    public NodeInstance instanceOfC;

    public Requirement req1AtoB; // rep aw
    public Requirement req2AtoB; // rep unaw
    public Requirement req3AtoB; // cont

    public Requirement req1CtoA; // rep aw
    public Requirement req2CtoA; // rep unaw
    public Requirement req3CtoA; // cont

    /**
     * create a simple custom application with three nodes, nodeA, nodeB and nodeC
     * nodeA has a requirement that is satisfied by nodeB and nodeC has a reuirement
     * that is satisfied by nodeC. we see how removeAllRunBindingsBothWays remove from
     * the global state the runtime bindings <instanceOfA, req> -> <instanceOfB,
     * cap> AND <instanceOfC, req> -> <instanceOfA, cap>
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
        this.req1AtoB = new Requirement("req1AtoB", RequirementSort.REPLICA_AWARE);
        this.req2AtoB = new Requirement("req2AtoB", RequirementSort.REPLICA_UNAWARE);
        this.req3AtoB = new Requirement("req3AtoB", RequirementSort.CONTAINMENT);

        this.req1CtoA = new Requirement("req1CtoA", RequirementSort.REPLICA_AWARE);
        this.req2CtoA = new Requirement("req2CtoA", RequirementSort.REPLICA_UNAWARE);
        this.req3CtoA = new Requirement("req3CtoA", RequirementSort.CONTAINMENT);

        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();
        this.nodeC = this.createNodeC();

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);
        this.testApp.addNode(this.nodeC);

        BindingPair firstHalfAtoB1 = new BindingPair("nodeA", "req1AtoB");
        BindingPair secondHalfAtoB1 = new BindingPair("nodeB", "cap1AtoB");
        this.testApp.addStaticBinding(firstHalfAtoB1, secondHalfAtoB1);

        BindingPair firstHalfAtoB2 = new BindingPair("nodeA", "req2AtoB");
        BindingPair secondHalfAtoB2 = new BindingPair("nodeB", "cap2AtoB");
        this.testApp.addStaticBinding(firstHalfAtoB2, secondHalfAtoB2);

        BindingPair firstHalfAtoB3 = new BindingPair("nodeA", "req3AtoB");
        BindingPair secondHalfAtoB3 = new BindingPair("nodeB", "cap3AtoB");
        this.testApp.addStaticBinding(firstHalfAtoB3, secondHalfAtoB3);  
        
        BindingPair fisrtHalfCtoA1 = new BindingPair("nodeC", "req1CtoA");
        BindingPair secondHalfCtoA1 = new BindingPair("nodeA", "cap1CtoA");
        this.testApp.addStaticBinding(fisrtHalfCtoA1, secondHalfCtoA1);

        BindingPair fisrtHalfCtoA2 = new BindingPair("nodeC", "req2CtoA");
        BindingPair secondHalfCtoA2 = new BindingPair("nodeA", "cap2CtoA");
        this.testApp.addStaticBinding(fisrtHalfCtoA2, secondHalfCtoA2);
        
        BindingPair fisrtHalfCtoA3 = new BindingPair("nodeC", "req3CtoA");
        BindingPair secondHalfCtoA3 = new BindingPair("nodeA", "cap3CtoA");
        this.testApp.addStaticBinding(fisrtHalfCtoA3, secondHalfCtoA3);

        this.instanceOfB = this.testApp.scaleOut1("nodeB", "instanceOfB");
        this.instanceOfA = this.testApp.scaleOut2("nodeA", "instanceOfA", "instanceOfB");
        this.instanceOfC = this.testApp.scaleOut2("nodeC", "instanceOfC", "instanceOfA");
    }

    public Node createNodeA(){
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");

        ret.addRequirement(this.req1AtoB);
        ret.addRequirement(this.req2AtoB);
        ret.addRequirement(this.req3AtoB);

        ret.addCapability("cap1CtoA");
        ret.addCapability("cap2CtoA");
        ret.addCapability("cap3CtoA");

        //rho: state -> reqs needed in that state
        List<Requirement> reqs = new ArrayList<>();
        reqs.add(this.req1AtoB);
        reqs.add(this.req2AtoB);
        reqs.add(this.req3AtoB);
        mp.addRhoEntry("state1", reqs);

        //gamma: state -> caps offered in that state
        List<String> caps = new ArrayList<>();
        caps.add("cap1CtoA");
        caps.add("cap2CtoA");
        caps.add("cap3CtoA");
        mp.addGammaEntry("state1", caps);

        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());

        return ret;
    }

    public Node createNodeB(){
        Node ret = new Node("nodeB", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");

        ret.addCapability("cap1AtoB");
        ret.addCapability("cap2AtoB");
        ret.addCapability("cap3AtoB");

        //gamma: state -> caps offered in that state
        List<String> caps = new ArrayList<>();
        caps.add("cap1AtoB");
        caps.add("cap2AtoB");
        caps.add("cap3AtoB");
        mp.addGammaEntry("state1", caps);

        for (String state : ret.getStates()) 
            mp.addRhoEntry(state, new ArrayList<Requirement>());
        
        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());

        return ret;
    }

    public Node createNodeC(){
        Node ret = new Node("nodeC", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");

        ret.addRequirement(this.req1CtoA);
        ret.addRequirement(this.req2CtoA);
        ret.addRequirement(this.req3CtoA);

        //rho: state -> reqs needed in that state
        List<Requirement> reqs = new ArrayList<>();
        reqs.add(this.req1CtoA);
        reqs.add(this.req2CtoA);
        reqs.add(this.req3CtoA);
        mp.addRhoEntry("state1", reqs);

        //gamma: state -> caps offered in that state
        mp.addGammaEntry("state1", new ArrayList<String>());

        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());

        return ret;
    }

    //removeAllRunBindingsBothWays throws NullPointerException if the passed instance is null
    @Test(expected = NullPointerException.class)
    public void removeAllRunBindingsBothWaysNullInstanceTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        this.testApp.getGlobalState().removeAllRunBindingsBothWays(null);
    }

    @Test
    public void removeAllRunBindingsBothWaysTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").size() == 3);
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfC").size() == 3);
        this.testApp.getGlobalState().removeAllRunBindingsBothWays("instanceOfA");

        //mind that removeAllRunBindingsBothWays remove even the containment binding (in fact this method is 
        //called only after the scaleIn())
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").size() == 0);
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfC").size() == 0);
    }

}