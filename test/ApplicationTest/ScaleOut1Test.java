package test.ApplicationTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.NodeUnknownException;
import model.exceptions.RuleNotApplicableException;
import test.ThesisAppFactory;

public class ScaleOut1Test {

    public Application testApp;

    @Before
    public void createApp() {
        this.testApp = ThesisAppFactory.createApplication();
    }

    @Test(expected = NullPointerException.class)
    public void scaleOut1NullNodeTest() 
        throws 
            NullPointerException, 
            RuleNotApplicableException,
            NodeUnknownException 
    {
        this.testApp.scaleOut1(null);
    }

    /**
     * trying to scale out a node that is not a part of app raise an exception
     * 
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     * @throws NodeUnknownException
     */
    @Test(expected = NodeUnknownException.class)
    public void scaleOut1NodeUnknown() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        Node randomNode = new Node("unknownInThisApp", "null", new ManagementProtocol());
        this.testApp.scaleOut1(randomNode);
    }


    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut1NotApplicableTest()
        throws
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        //frontend has a containmanent requirement and scaleOut1 do not handle that
        Node frontend = this.testApp.getNodes().get("frontend");
        this.testApp.scaleOut1(frontend);
    }

    @Test
    public void basicScaleOut1Test() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        //scaling out a node with no reqs and no bindings when it is created
        Node node = this.testApp.getNodes().get("node");
        NodeInstance nodeInstance = this.testApp.scaleOut1(node);
        assertNotNull("mongoInstance null", nodeInstance);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().size() == 1);
    }
}