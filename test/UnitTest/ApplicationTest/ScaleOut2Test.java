package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.NodeUnknownException;
import model.exceptions.RuleNotApplicableException;
import test.ThesisAppFactory;

public class ScaleOut2Test {

    public Application testApp;
    public NodeInstance n1;
    public NodeInstance n2;
    public NodeInstance n3;
    public NodeInstance m1;

    @Before
    public void setUp() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        this.testApp = ThesisAppFactory.createApplication();
        Node node = testApp.getNodes().get("node");
        Node mongo = testApp.getNodes().get("mongo");

        this.n1 = testApp.scaleOut1(node);
        this.n2 = testApp.scaleOut1(node);
        this.n3 = testApp.scaleOut1(node);
        this.m1 = testApp.scaleOut1(mongo);
    }

    @Test(expected = NullPointerException.class)
    public void scaleOut2NullNodeTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        testApp.scaleOut2(null, this.n3);
    }

    @Test(expected = NullPointerException.class)
    public void scaleOut2NullContainerTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        Node frontend = testApp.getNodes().get("frontend");
        testApp.scaleOut2(frontend, null);
    }

    //want RuleNotApplicable because the node has not a containament requirement
    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut2NoContainReq() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        //node has not a containment requirement
        testApp.scaleOut2(this.n1.getNodeType(), this.n1);
    }

    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut2WrongServantNode() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        Node frontend = testApp.getNodes().get("frontend");
        //m1 is instance of mongo and do not offer the right cap for frontend
        testApp.scaleOut2(frontend, this.m1);
    }

    @Test
    public void scaleOut2Test() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        Node frontend = testApp.getNodes().get("frontend");
        NodeInstance frontendInstance = testApp.scaleOut2(frontend, this.n3);

        assertNotNull("frontendInstance null", frontendInstance);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().containsValue(frontendInstance));
        //frontendInstance has just 1 runtime binding
        assertTrue(testApp.getGlobalState().getRuntimeBindings().get(frontendInstance.getID()).size() == 1);
        //the servant of the frontendInstance is as expected the instance n3 of node
        assertTrue(testApp.getGlobalState().getRuntimeBindings().get(frontendInstance.getID()).get(0).getNodeInstanceID().equals(this.n3.getID()));
    }

}