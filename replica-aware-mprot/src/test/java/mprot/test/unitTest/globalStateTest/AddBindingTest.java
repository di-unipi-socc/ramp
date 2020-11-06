package mprot.test.unitTest.globalStateTest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mprot.core.model.*;
import mprot.core.model.exceptions.*;

public class AddBindingTest {
    public Application testApp;
    public Node nodeA;
    public Node nodeB;
    public NodeInstance instanceOfA;
    public NodeInstance instanceOfB;

    public Requirement req;

    /**
     * creates a custom simple application with 2 nodes, nodeA and nodeB nodeA has a
     * requirement and nodeB has the capability to satisfy it. we add de runtime
     * binding <instanceOfA, req> -> <instanceOfB, cap>
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

        StaticBinding unawareFirstHalf = new StaticBinding("nodeA", "req");
        StaticBinding unawareSecondHalf = new StaticBinding("nodeB", "cap");
        this.testApp.addStaticBinding(unawareFirstHalf, unawareSecondHalf);

        //A has a fault resolvable
        this.instanceOfA = this.testApp.scaleOut1(this.nodeA.getName(), "instanceOfA");
        this.instanceOfB = this.testApp.scaleOut1(this.nodeB.getName(), "instanceOfB");
        
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

    //addBinding throws a NullPointerException if the passed askingInstance is null
    @Test(expected = NullPointerException.class)
    public void addBindingNullAskingInstanceTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        this.testApp.getGlobalState().addBinding(null, this.req, "instanceOfB");
    }

    //addBinding throws a NullPointerException if the passed requirement is null
    @Test(expected = NullPointerException.class)
    public void addBindingNullReqTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        this.testApp.getGlobalState().addBinding("instanceOfA", null, "instanceOfB");
    }

    //addBinding thorws a NullPointerException if the passed servingInstance is null
    @Test(expected = NullPointerException.class)
    public void addBindingNullServingInstanceTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        this.testApp.getGlobalState().addBinding("instanceOfA", this.req, null);
    }

    @Test
    public void addBindingTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        //instanceOfA has a fault and no binding
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").size() == 0);
        assertTrue(this.testApp.getGlobalState().getPendingFaults("instanceOfA").size() == 1);
        assertTrue(this.testApp.getGlobalState().getResolvableFaults("instanceOfA").size() == 1);

        this.testApp.getGlobalState().addBinding("instanceOfA", this.req, "instanceOfB");

        //fault is resolved
        assertTrue(this.testApp.getGlobalState().getPendingFaults("instanceOfA").size() == 0);
        //instanceOfA has 1 runtime 
        assertTrue(this.testApp.getGlobalState().getRuntimeBindings().get("instanceOfA").size() == 1);
    }
}