package test.UnitTest.GlobalStateTest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;


import model.*;
import model.exceptions.NodeUnknownException;
import model.exceptions.RuleNotApplicableException;

public class GetPendingFaultsTest {

    public Application testApp;
    public Node nodeA;
    public NodeInstance instanceOfA;

    @Before
    public void setUp() throws NullPointerException, RuleNotApplicableException, NodeUnknownException {
        this.nodeA = this.createNodeA();
        this.testApp = new Application("testApp");
        this.testApp.addNode(nodeA);

        this.instanceOfA = this.testApp.scaleOut1(this.nodeA);
    }

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