package mprot.lib.test.UnitTest.GlobalStateTest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import mprot.lib.model.*;
import mprot.lib.model.exceptions.*;

import java.util.List;
import java.util.ArrayList;

public class GetSatisfiedReqsTest {

    public Application testApp;
    public Node nodeAsking;
    public Node nodeServer;
    public NodeInstance nodeAskingInstance;
    public NodeInstance nodeServerInstance;
    public Requirement reqA; // replica unaware
    public Requirement reqB; // replica aware
    public Requirement reqC; // containment

    /**
     * create a custom simple application with two nodes, nodeAsking and nodeServer
     * nodeAsking has 3 requirements (reqA, reqB and reqC) nodeServer has 2 states,
     * state1 and state2 in state1 offer the caps for reqA and reqC, in state2 offer
     * the caps for reqB and reqC we see that nodeAskingInstance has always 2
     * satisfied reqs when nodeServerInstance change state
     * 
     * @throws InstanceUnknownException
     * @throws AlreadyUsedIDException
     */
    @Before
    public void setUp() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            InstanceUnknownException 
    {
        this.reqA = new Requirement("reqA", RequirementSort.REPLICA_UNAWARE);
        this.reqB = new Requirement("reqB", RequirementSort.REPLICA_AWARE);
        this.reqC = new Requirement("reqC", RequirementSort.CONTAINMENT);
        
        this.nodeAsking = this.createNodeAsking();
        this.nodeServer = this.createNodeServer();

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
        this.testApp.addNode(this.nodeAsking);
        this.testApp.addNode(this.nodeServer);

        StaticBinding firstAsk = new StaticBinding("nodeAsking", "reqA");
        StaticBinding firstAns = new StaticBinding("nodeServer", "capA");
        this.testApp.addStaticBinding(firstAsk, firstAns);

        StaticBinding secondAsk = new StaticBinding("nodeAsking", "reqB");
        StaticBinding secondAns = new StaticBinding("nodeServer", "capB");
        this.testApp.addStaticBinding(secondAsk, secondAns);

        StaticBinding thirdAsk = new StaticBinding("nodeAsking", "reqC");
        StaticBinding thirdAns = new StaticBinding("nodeServer", "capC");
        this.testApp.addStaticBinding(thirdAsk, thirdAns);

        this.nodeServerInstance = this.testApp.scaleOut1("nodeServer", "serverInstance");
        this.nodeAskingInstance = this.testApp.scaleOut2("nodeAsking", "askingInstance", "serverInstance");

    }

    public Node createNodeServer(){
        Node ret = new Node("nodeServer", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();
        ret.addState("state1");
        ret.addState("state2");

        ret.addOperation("goToState2");

        ret.addCapability("capA");
        ret.addCapability("capB");
        ret.addCapability("capC");

        mp.addTransition("state1", "goToState2", "state2");

        mp.addRhoEntry("state1", new ArrayList<Requirement>());
        mp.addRhoEntry("state2", new ArrayList<Requirement>());
        mp.addRhoEntry("state1goToState2state2", new ArrayList<Requirement>());
        
        List<String> state1Caps = new ArrayList<>();
        state1Caps.add("capA");
        state1Caps.add("capC");
        mp.addGammaEntry("state1", state1Caps);

        List<String> state2Caps = new ArrayList<>();
        state2Caps.add("capB");
        state2Caps.add("capC");
        mp.addGammaEntry("state2", state2Caps);

        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());

        return ret;
    }

    public Node createNodeAsking(){
        Node ret = new Node("nodeAsking", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        ret.addState("state1");
        ret.addState("state2");

        ret.addOperation("goToState2");

        ret.addRequirement(this.reqA);
        ret.addRequirement(this.reqB);
        ret.addRequirement(this.reqC);

        mp.addTransition("state1", "goToState2", "state2");
       
        List<Requirement> testReqs = new ArrayList<>();
        testReqs.add(this.reqA);
        testReqs.add(this.reqB);
        testReqs.add(this.reqC);
        mp.addRhoEntry("state1", testReqs);
        mp.addRhoEntry("state2", testReqs);
        mp.addRhoEntry("state1goToState2state2", new ArrayList<Requirement>());

        //gamma: state -> caps offered in that state
        for (String state : ret.getStates())
            mp.addGammaEntry(state, new ArrayList<String>());

        //phi: state -> states for fault handling
        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());
        return ret;
    }

    //getSatisfiedReqs thorws a NullPointerException if the passed instance is null
    @Test(expected = NullPointerException.class)
    public void getSatisfiedReqsNullInstanceTest()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        this.testApp.getGlobalState().getSatisfiedReqs(null);
    }

    @Test
    public void getSatisfiedReqsTest() throws Exception{

        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.nodeAskingInstance.getID()).size() == 2);
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.nodeAskingInstance.getID()).contains(this.reqA));
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.nodeAskingInstance.getID()).contains(this.reqC));

        this.testApp.opStart("serverInstance", "goToState2");
        this.testApp.opEnd("serverInstance", "goToState2");

        assertTrue(this.nodeServerInstance.getOfferedCaps().contains("capB"));
       
        this.testApp.opStart("askingInstance", "goToState2");
        this.testApp.opEnd("askingInstance", "goToState2");

        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.nodeAskingInstance.getID()).size() == 2);
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.nodeAskingInstance.getID()).contains(this.reqB));
        assertTrue(this.testApp.getGlobalState().getSatisfiedReqs(this.nodeAskingInstance.getID()).contains(this.reqC));  
    }

}