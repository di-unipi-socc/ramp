package mprot.unitTest.applicationTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import mprot.core.model.*;
import mprot.core.model.exceptions.*;
import mprot.utilities.WebAppFactory;

//TODO: might missing some test (exception generation (expected...))

public class ScaleOut2Test {

    public Application testApp;
    public NodeInstance n1;
    public NodeInstance n2;
    public NodeInstance n3;
    public NodeInstance m1;

    public Node frontend;
    public Node node;
    public Node mongo;

    @Before
    public void setUp() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            IllegalArgumentException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.testApp = WebAppFactory.createApplication(PiVersion.GREEDYPI);
       
        this.frontend = testApp.getNodes().get("frontend");
        this.node = testApp.getNodes().get("node");
        this.mongo = testApp.getNodes().get("mongo");

        this.n1 = testApp.scaleOut1("node", "n1");
        this.n2 = testApp.scaleOut1("node", "n2");
        this.n3 = testApp.scaleOut1("node", "n3");
        this.m1 = testApp.scaleOut1("mongo", "m1");
    }

    //sclaeOut2 throws a NullPointerException if the passed nodeName is null
    @Test(expected = NullPointerException.class)
    public void scaleOut2NullNodeTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            InstanceUnknownException 
    {
        testApp.scaleOut2(null, "dontcare", "n3");
    }

    //scaleOut2 throws a NullPointerException if the passed containerID is null
    @Test(expected = NullPointerException.class)
    public void scaleOut2NullContainerTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            InstanceUnknownException 
    {
        testApp.scaleOut2("frontend", "dontcare" ,  null);
    }

    //scaleOut2 throws a RuleNotApplicableException if the passed node has not a containment requirement
    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut2NoContainReq() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            InstanceUnknownException 
    {
        testApp.scaleOut2("node", "n1", "n1");
    }

    //scaleOut2 throws a RuleNotApplicableException if the passed container do not offer the right 
    //containement serving cap 
    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut2WrongServantNode() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            InstanceUnknownException 
    {
        //m1 is instance of mongo and do not offer the right cap for frontend
        //frontend need a instance of "node"
        testApp.scaleOut2("frontend", "frontendF1", "m1");
    }

    @Test
    public void scaleOut2Test() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            InstanceUnknownException 
    {
        NodeInstance frontendF1 = testApp.scaleOut2(this.frontend.getName(), "frontendF1", this.n3.getID());
        assertNotNull("frontendInstance null", frontendF1);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().containsValue(frontendF1));
        //frontendInstance has just 1 runtime binding
        assertTrue(testApp.getGlobalState().getRuntimeBindings().get("frontendF1").size() == 1);
        //the servant of the frontendInstance is as expected the instance n3 of node
        assertTrue(testApp.getGlobalState().getRuntimeBindings().get("frontendF1").get(0).getNodeInstanceID().equals(this.n3.getID()));
    }

}