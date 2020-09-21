package test.UnitTest.GlobalStateTest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;


import model.*;
import exceptions.AlreadyUsedIDException;
import exceptions.InstanceUnknownException;
import exceptions.NodeUnknownException;
import exceptions.RuleNotApplicableException;

public class GetPendingFaultsTest {

    public Application testApp;
    public Node nodeA;
    public NodeInstance instanceOfA;

    /**
     * creates a custom simple application with 1 node (nodeA) nodeA has two
     * requirements and since there is no other nodes that leads to two pending
     * fault
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
            NodeUnknownException,
            IllegalArgumentException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.nodeA = this.createNodeA();
        this.testApp = new Application("testApp");
        this.testApp.addNode(this.nodeA);

        this.instanceOfA = this.testApp.scaleOut1(this.nodeA.getName(), "instanceOfA");
    }

    //getPendingFaults throws NullPointerException if the passed instance is null
    @Test(expected = NullPointerException.class)
    public void getPendingFaultsNullInstanceTest(){
        this.testApp.getGlobalState().getPendingFaults(null);
    }  

    @Test
    public void getPendingFaultsTest(){
        List<Fault> faults = this.testApp.getGlobalState().getPendingFaults(this.instanceOfA);
        assertTrue(faults.size() == 2);
    }

    public Node createNodeA(){
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();
        ret.addState("state1");

        Requirement req1 = new Requirement("req1", RequirementSort.REPLICA_UNAWARE);
        Requirement req2 = new Requirement("req2", RequirementSort.REPLICA_AWARE);

        ret.addRequirement(req1);
        ret.addRequirement(req2);

        List<Requirement> reqs = new ArrayList<>();
        reqs.add(req1);
        reqs.add(req2);
        mp.addRhoEntry("state1", reqs);

        mp.addGammaEntry("state1", new ArrayList<>());
        mp.addPhiEntry("state1", new ArrayList<>());

        return ret; 
    }
    
}