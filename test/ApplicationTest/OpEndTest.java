package test.ApplicationTest;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import model.*;
import model.exceptions.FailedOperationException;
import model.exceptions.NodeUnknownException;
import model.exceptions.OperationNotAvailableException;
import model.exceptions.RuleNotApplicableException;
import test.ThesisAppFactory;

public class OpEndTest {

    public Application testApp;
    public NodeInstance mongoM1;

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
        this.testApp.opStart(this.mongoM1, "start");
    }

    // TODO: opEnd: failedOp non testato x2
    @Test(expected = NullPointerException.class)
    public void opEndNullInstanceTest()
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException 
    {
        testApp.opEnd(null, "start");
    }

    @Test(expected = NullPointerException.class)
    public void opEndNullOpTest() 
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            FailedOperationException 
    {
        testApp.opEnd(this.mongoM1, null);
    }

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