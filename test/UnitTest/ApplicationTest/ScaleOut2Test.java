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

    public Node frontend;
    public Node node;
    public Node mongo;

    @Before
    public void setUp() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        this.testApp = ThesisAppFactory.createApplication();
       
        this.frontend = testApp.getNodes().get("frontend");
        this.node = testApp.getNodes().get("node");
        this.mongo= testApp.getNodes().get("mongo");

        this.n1 = testApp.scaleOut1(node);
        this.n2 = testApp.scaleOut1(node);
        this.n3 = testApp.scaleOut1(node);
        this.m1 = testApp.scaleOut1(mongo);
    }

    //sclaeOut2 throws a NullPointerException if the passed node is null
    @Test(expected = NullPointerException.class)
    public void scaleOut2NullNodeTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        testApp.scaleOut2(null, this.n3);
    }

    //scaleOut2 throws a NullPointerException if the passed container instance is null
    @Test(expected = NullPointerException.class)
    public void scaleOut2NullContainerTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        testApp.scaleOut2(frontend, null);
    }

    //scaleOut2 throws a RuleNotApplicableException if the passed node has not a containment requirement
    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut2NoContainReq() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        testApp.scaleOut2(this.n1.getNodeType(), this.n1);
    }

    //scaleOut2 throws a RuleNotApplicableException if the passed container do not offer the right 
    //containement serving cap 
    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut2WrongServantNode() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        //m1 is instance of mongo and do not offer the right cap for frontend
        //frontend need a instance of "node"
        testApp.scaleOut2(this.frontend, this.m1);
    }

    @Test
    public void scaleOut2Test() 
        throws 
            NullPointerException, 
            RuleNotApplicableException 
    {
        NodeInstance frontendInstance = testApp.scaleOut2(this.frontend, this.n3);
        assertNotNull("frontendInstance null", frontendInstance);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().containsValue(frontendInstance));
        //frontendInstance has just 1 runtime binding
        assertTrue(testApp.getGlobalState().getRuntimeBindings().get(frontendInstance.getID()).size() == 1);
        //the servant of the frontendInstance is as expected the instance n3 of node
        assertTrue(testApp.getGlobalState().getRuntimeBindings().get(frontendInstance.getID()).get(0).getNodeInstanceID().equals(this.n3.getID()));
    }

}