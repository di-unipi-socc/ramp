package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import model.*;
import exceptions.AlreadyUsedIDException;
import exceptions.InstanceUnknownException;
import exceptions.OperationNotAvailableException;
import exceptions.RuleNotApplicableException;
import myUtils.ThesisAppFactory;

public class OpStartTest {

    public Application testApp;
    public NodeInstance mongoM1;

    @Before
    public void setUp() 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            IllegalArgumentException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.testApp = ThesisAppFactory.createApplication();
        this.mongoM1 = this.testApp.scaleOut1("mongo", "mongoM1");
    }

    //opStart throws a NullPointerException when the passed instanceID is null
    @Test(expected = NullPointerException.class)
    public void opStartNullInstanceIDTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opStart(null, "start");
    }

    //opStart throws a NullPointerException when the passed op is null
    @Test(expected = NullPointerException.class)
    public void opStartNullOpTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opStart("mongoM1", null);
    }

    //opStart throws an IllegalArgumentException when the op is empty
    @Test(expected = IllegalArgumentException.class)
    public void opStartEmptyOpTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opStart("mongoM1", "");
    }

    //opStart throws an IllegalArgumentException when the instanceID is empty
    @Test(expected = IllegalArgumentException.class)
    public void opStartEmptyInstanceIDOpTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opStart("", "start");
    }

    //opStart throws a RuleNotApplicableException when the passed instanceID is not associated with an instance
    @Test(expected = RuleNotApplicableException.class)
    public void opStartUnknownInstanceTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opStart("unknownInstanceID", "start");
    }


    //opStart throws an OperationNotAvailableException when the operation is not known
    @Test(expected = OperationNotAvailableException.class)
    public void opStartOpNotAvailableTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opStart("mongoM1", "notKnownOp");
    }

    @Test
    public void opStartTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opStart("mongoM1", "start");
        assertTrue("wrong current state", this.mongoM1.getCurrentState().equals("stoppedstartrunning"));
        assertTrue("wrong number of bindings", testApp.getGlobalState().getRuntimeBindings().get(this.mongoM1.getID()).size() == 0);    
    }
}