package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import model.*;
import exceptions.FailedOperationException;
import exceptions.NodeUnknownException;
import exceptions.OperationNotAvailableException;
import exceptions.RuleNotApplicableException;
import test.ThesisAppFactory;

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
            NodeUnknownException 
    {
        this.testApp = ThesisAppFactory.createApplication();
        this.mongoM1 = this.testApp.scaleOut1(this.testApp.getNodes().get("mongo"));

        this.nodeN3 = this.testApp.scaleOut1(this.testApp.getNodes().get("node"));
        this.frontendF1 = this.testApp.scaleOut2(this.testApp.getNodes().get("frontend"), nodeN3);
        
        this.testApp.opStart(this.mongoM1, "start");
        this.testApp.opStart(this.frontendF1, "install");
    }

    //opEnd throws FailedOperationException when there is a fault
    @Test(expected = FailedOperationException.class)
    public void opEndFailedOperationExceptionTest() throws Exception{
        this.testApp.opEnd(this.frontendF1, "install");

    }

    //opEnd throws a NullPointerException when the passed instance is null
    @Test(expected = NullPointerException.class)
    public void opEndNullInstanceTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException 
    {
        testApp.opEnd(null, "start");
    }

    //opEnd throws a NullPointerException when the passed op is null
    @Test(expected = NullPointerException.class)
    public void opEndNullOpTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException 
    {
        testApp.opEnd(this.mongoM1, null);
    }

    //opEnd throws a NullPointerException when the op is empty
    @Test(expected = IllegalArgumentException.class)
    public void opEndEmptyOpTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException 
    {
        testApp.opEnd(this.mongoM1, "");
    }

    @Test
    public void opEndTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException 
    {
        testApp.opEnd(this.mongoM1, "start");
        assertTrue("wrong current state", this.mongoM1.getCurrentState().equals("running"));
        assertTrue("wrong number of bindings", testApp.getGlobalState().getRuntimeBindings().get(this.mongoM1.getID()).size() == 0);

        //now mongoM1 offer one cap
        assertTrue("wrong number of offered caps", this.mongoM1.getOfferedCaps().size() == 1);
    }
}