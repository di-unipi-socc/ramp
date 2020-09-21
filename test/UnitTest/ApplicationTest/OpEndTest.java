package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import model.*;
import exceptions.AlreadyUsedIDException;
import exceptions.FailedOperationException;
import exceptions.InstanceUnknownException;
import exceptions.OperationNotAvailableException;
import exceptions.RuleNotApplicableException;
import myUtils.ThesisAppFactory;

public class OpEndTest {

    public Application testApp;
    public NodeInstance mongoM1;
    public NodeInstance frontendF1;
    public NodeInstance nodeN3;

    @Before
    public void setUp() 
        throws 
            IllegalArgumentException, 
            NullPointerException,
            OperationNotAvailableException,
            RuleNotApplicableException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        this.testApp = ThesisAppFactory.createApplication();
        this.mongoM1 = this.testApp.scaleOut1("mongo", "mongoM1");

        this.nodeN3 = this.testApp.scaleOut1("node", "nodeN3");
        this.frontendF1 = this.testApp.scaleOut2("frontend", "frontendF1", "nodeN3");
        
        this.testApp.opStart(this.mongoM1.getID(), "start");
        this.testApp.opStart(this.frontendF1.getID(), "install");
    }

    //opEnd throws FailedOperationException when there is a fault
    @Test(expected = FailedOperationException.class)
    public void opEndFailedOperationExceptionTest() throws Exception{
        this.testApp.opEnd("frontendF1", "install");

    }

    //opEnd throws a NullPointerException when the passed instanceID is null
    @Test(expected = NullPointerException.class)
    public void opEndNullInstanceIDTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opEnd(null, "start");
    }

    //opEnd throws a IllegalArgumentException when the passed instanceID is empty
    @Test(expected = IllegalArgumentException.class)
    public void opEndEmptyInstanceIDTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opEnd("", "start");
    }

    //opEnd throws a RuleNotApplicableException when the passed instanceID is not associated with an instance
    @Test(expected = RuleNotApplicableException.class)
    public void opEndUnknownInstanceTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opEnd("unkownInstanceID", "start");
    }

    //opEnd throws a NullPointerException when the passed op is null
    @Test(expected = NullPointerException.class)
    public void opEndNullOpTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opEnd("mongoM1", null);
    }

    //opEnd throws a IllegalArgumentException when the op is empty
    @Test(expected = IllegalArgumentException.class)
    public void opEndEmptyOpTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opEnd("mongoM1", "");
    }

    @Test
    public void opEndTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException 
    {
        testApp.opEnd("mongoM1", "start");
        assertTrue("wrong current state", this.mongoM1.getCurrentState().equals("running"));
        assertTrue("wrong number of bindings", testApp.getGlobalState().getRuntimeBindings().get(this.mongoM1.getID()).size() == 0);

        //now mongoM1 offer one cap
        assertTrue("wrong number of offered caps", this.mongoM1.getOfferedCaps().size() == 1);
    }
}