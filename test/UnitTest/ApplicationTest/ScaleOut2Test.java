package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import model.*;
import exceptions.AlreadyUsedIDException;
import exceptions.InstanceUnknownException;
import exceptions.NodeUnknownException;
import exceptions.RuleNotApplicableException;
import test.ThesisAppFactory;

//TODO mancando dei casi di test (generazione eccezioni)

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
            NodeUnknownException,
            IllegalArgumentException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.testApp = ThesisAppFactory.createApplication();
       
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
            NodeUnknownException, 
            InstanceUnknownException 
    {
        testApp.scaleOut2(null, "dontcare", this.n3.getID());
    }

    //scaleOut2 throws a NullPointerException if the passed containerID is null
    @Test(expected = NullPointerException.class)
    public void scaleOut2NullContainerTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            NodeUnknownException, 
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
            NodeUnknownException, 
            InstanceUnknownException 
    {
        testApp.scaleOut2(this.n1.getNodeType().getName(), "n1", this.n1.getID());
    }

    //scaleOut2 throws a RuleNotApplicableException if the passed container do not offer the right 
    //containement serving cap 
    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut2WrongServantNode() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            NodeUnknownException, 
            InstanceUnknownException 
    {
        //m1 is instance of mongo and do not offer the right cap for frontend
        //frontend need a instance of "node"
        testApp.scaleOut2(this.frontend.getName(), "frontendF1", this.m1.getID());
    }

    @Test
    public void scaleOut2Test() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            NodeUnknownException, 
            InstanceUnknownException 
    {
        NodeInstance frontendInstance = testApp.scaleOut2(this.frontend.getName(), "frontendF1", this.n3.getID());
        assertNotNull("frontendInstance null", frontendInstance);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().containsValue(frontendInstance));
        //frontendInstance has just 1 runtime binding
        assertTrue(testApp.getGlobalState().getRuntimeBindings().get(frontendInstance.getID()).size() == 1);
        //the servant of the frontendInstance is as expected the instance n3 of node
        assertTrue(testApp.getGlobalState().getRuntimeBindings().get(frontendInstance.getID()).get(0).getNodeInstanceID().equals(this.n3.getID()));
    }

}