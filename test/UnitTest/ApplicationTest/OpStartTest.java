package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import model.*;
import model.exceptions.NodeUnknownException;
import model.exceptions.OperationNotAvailableException;
import model.exceptions.RuleNotApplicableException;
import test.ThesisAppFactory;

public class OpStartTest {

    public Application testApp;
    public NodeInstance mongoM1;

    @Before
    public void setUp() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        this.testApp = ThesisAppFactory.createApplication();
        this.mongoM1 = this.testApp.scaleOut1(this.testApp.getNodes().get("mongo"));
    }

    //opStart throws a NullPointerException when the passed instance is null
    @Test(expected = NullPointerException.class)
    public void opStartNullInstanceTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException 
    {
        testApp.opStart(null, "start");
    }

    //opStart throws a NullPointerException when the passed op is null
    @Test(expected = NullPointerException.class)
    public void opStartNullOpTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException 
    {
        testApp.opStart(this.mongoM1, null);
    }

    //opStart throws an IllegalArgumentException when the op is empty
    @Test(expected = IllegalArgumentException.class)
    public void opStartEmptyOpTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException 
    {
        testApp.opStart(this.mongoM1, "");
    }

    //opStart throws an OperationNotAvailableException when the operation is not known
    @Test(expected = OperationNotAvailableException.class)
    public void opStartOpNotAvailableTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException 
    {
        testApp.opStart(this.mongoM1, "notKnownOp");
    }

    @Test
    public void opStartTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException 
    {
        testApp.opStart(this.mongoM1, "start");
        assertTrue("wrong current state", this.mongoM1.getCurrentState().equals("stoppedstartrunning"));
        assertTrue("wrong number of bindings", testApp.getGlobalState().getRuntimeBindings().get(this.mongoM1.getID()).size() == 0);    
    }
}