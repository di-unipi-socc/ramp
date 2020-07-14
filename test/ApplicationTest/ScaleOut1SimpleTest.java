package test.ApplicationTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.NodeUnknownException;
import model.exceptions.RuleNotApplicableException;
import test.AppFactory;

public class ScaleOut1SimpleTest {

    public Application testApp = null;

    @Before
    public void createApp() {
        this.testApp = AppFactory.createApplication();
    }

    @Test(expected = NullPointerException.class)
    public void scaleOut1NullNodeTest() throws Exception {
        this.testApp.scaleOut1(null);
    }

    /**
     * trying to scale out a node that is not a part of app raise an exception
     * @throws NodeUnknownException
     */
    @Test(expected = NodeUnknownException.class)
    public void scaleOut1NodeUnknown() throws Exception {
        Node randomNode = new Node("unknownInThisApp", "null", new ManagementProtocol());
        this.testApp.scaleOut1(randomNode);
    }


    @Test(expected = RuleNotApplicableException.class)
    public void scaleOut1NotApplicableTest() throws Exception{
        //frontend has a containmanent requirement and scaleOut1 do not handle that
        Node frontend = this.testApp.getNodes().get("frontend");
        this.testApp.scaleOut1(frontend);
    }

    @Test
    public void basicScaleOut1Test() throws Exception{
        //scaling out a node with no reqs and no bindings when it is created
        Node node = this.testApp.getNodes().get("node");
        NodeInstance nodeInstance = this.testApp.scaleOut1(node);
        assertNotNull("mongoInstance null", nodeInstance);
        assertTrue(this.testApp.getGlobalState().getActiveNodeInstances().size() == 1);
    }
    
}