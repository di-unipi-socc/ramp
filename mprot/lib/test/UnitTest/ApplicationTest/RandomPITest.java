package mprot.lib.test.unitTest.applicationTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mprot.lib.model.*;
import mprot.lib.model.exceptions.*;

//test both randomPI and getCapableInstances

public class RandomPITest {

    public Application testApp;
    public Node nodeA;
    public Node nodeB;

    public NodeInstance instanceA;
    public NodeInstance instanceB1;
    public NodeInstance instanceB2;
    public NodeInstance instanceB3;

    public Requirement req;

    /**
     * builds a simple custom application with 2 nodes, nodeA and nodeB nodeA
     * requires req and nodeB offer the right cap. there are 4 instances, 1 instance
     * of A and 3 instance of B. instanceA is created before the instances of B so
     * it can have a resolvable fault.
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
        this.req = new Requirement("req", RequirementSort.REPLICA_UNAWARE);

        this.nodeA = this.createNodeA();
        this.nodeB = this.createNodeB();

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);

        this.testApp.addNode(this.nodeA);
        this.testApp.addNode(this.nodeB);

        StaticBinding firstHalf = new StaticBinding("nodeA", "req");
        StaticBinding secondHalf = new StaticBinding("nodeB", "cap");

        this.testApp.addStaticBinding(firstHalf, secondHalf);

        this.instanceA = this.testApp.scaleOut1(this.nodeA.getName(), "instanceA");
        this.instanceB1 = this.testApp.scaleOut1(this.nodeB.getName(), "instanceB1");
        this.instanceB2 = this.testApp.scaleOut1(this.nodeB.getName(), "instanceB2");
        this.instanceB3 = this.testApp.scaleOut1(this.nodeB.getName(), "instanceB3");
    }

    public Node createNodeA(){
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addRequirement(this.req);

        List<Requirement> reqs = new ArrayList<>();
        reqs.add(this.req);
        mp.addRhoEntry("state1", reqs);

        mp.addGammaEntry("state1", new ArrayList<String>());
        mp.addPhiEntry("state1", new ArrayList<String>());
    
        return ret;
    }

    public Node createNodeB(){
        Node ret = new Node("nodeB", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addCapability("cap");

        List<String> caps = new ArrayList<>();
        caps.add("cap");

        mp.addGammaEntry("state1", caps);
        mp.addPhiEntry("state1", new ArrayList<String>());
        mp.addRhoEntry("state1", new ArrayList<>());

        return ret;
    }

    //randomPI thorw a NullPointerException when the passed instanceID is null
    @Test(expected = NullPointerException.class)
    public void randomPINullInstanceIDTest() 
        throws 
            NullPointerException, 
            InstanceUnknownException {
        this.testApp.randomPI(null, this.req);
    }

    //randomPI throws a NullPointerException when the passed req is null
    @Test(expected = NullPointerException.class)
    public void randomPINullReqTest() 
        throws 
            NullPointerException, 
            InstanceUnknownException 
    {
        this.testApp.randomPI("instanceA", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomPIEmptyInstanceIDTest()
        throws 
            NullPointerException, 
            InstanceUnknownException 
    {
        this.testApp.randomPI("", this.req);
    }

    @Test(expected = InstanceUnknownException.class)
    public void randomPIUnkonwnInstanceTest()
        throws 
            NullPointerException, 
            InstanceUnknownException 
    {
        this.testApp.randomPI("unkownInstanceID", this.req);
    }

    @Test
    public void randomPITest() 
        throws 
            NullPointerException, 
            InstanceUnknownException 
    {
        ArrayList<NodeInstance> capableInstances = (ArrayList<NodeInstance>) this.testApp.getGlobalState().getCapableInstances("instanceA", this.req);
        
        //the 3 instances of nodeB are capable
        assertTrue(capableInstances.size() == 3);
        //instanceA is not a capable instance for itself (obv)
        assertFalse(capableInstances.contains(this.instanceA));

        //really a trivial test, TODO implement a real test
        for(int i = 0; i < 109; i++)
            assertTrue(capableInstances.contains(this.testApp.randomPI(this.instanceA.getID(), this.req)));
        
    }

}