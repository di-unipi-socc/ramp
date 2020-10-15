package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import exceptions.AlreadyUsedIDException;
import exceptions.FailedOperationException;
import exceptions.InstanceUnknownException;
import exceptions.OperationNotAvailableException;
import exceptions.RuleNotApplicableException;
import model.*;
import utilities.ThesisAppFactory;

public class CloneTest {

    public Application app;
    public Application clonedApp;

    @Before
    public void setUp() {
        app = ThesisAppFactory.createApplication(PiVersion.GREEDYPI);
    }

    @Test
    public void newwlyCreatedAppCloneTest() {
        clonedApp = app.clone();

        assertTrue(app.getName().equals(clonedApp.getName()));

        assertTrue(app.getBindingFunction().size() == clonedApp.getBindingFunction().size());

        // check that the binding function is well cloned
        for (StaticBinding firstHalf : app.getBindingFunction().keySet()) {
            StaticBinding secondHalf = app.getBindingFunction().get(firstHalf);

            assertTrue(clonedApp.getBindingFunction().containsKey(firstHalf));
            assertTrue(clonedApp.getBindingFunction().containsValue(secondHalf));
        }

        assertTrue(clonedApp.getNodes().size() == app.getNodes().size());

        // check that each node is well cloned
        for (String nodeName : app.getNodes().keySet()) {
            assertTrue(clonedApp.getNodes().containsKey(nodeName));

            Node appNode = app.getNodes().get(nodeName);
            Node clonedNode = clonedApp.getNodes().get(nodeName);

            assertTrue(appNode.getName().equals(clonedNode.getName()));
            assertTrue(appNode.getInitialState().equals(clonedNode.getInitialState()));

            for (Requirement appNodeReq : appNode.getReqs())
                assertTrue(clonedNode.getReqs().contains(appNodeReq));

            for (String appNodeState : appNode.getStates())
                assertTrue(clonedNode.getStates().contains(appNodeState));

            for (String op : appNode.getOps())
                assertTrue(clonedNode.getOps().contains(op));

            for (String cap : appNode.getCaps())
                assertTrue(clonedNode.getCaps().contains(cap));
        }

        // check that the global state is well cloned
        assertTrue(app.getGlobalState().getActiveNodeInstances().size() == 0);
        assertTrue(app.getGlobalState().getActiveNodeInstances().size() == clonedApp.getGlobalState()
                .getActiveNodeInstances().size());

        assertTrue(app.getGlobalState().getRuntimeBindings().size() == 0);
        assertTrue(app.getGlobalState().getRuntimeBindings().size() == clonedApp.getGlobalState().getRuntimeBindings()
                .size());
    }

    @Test
    public void RunningAppCloneTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            RuleNotApplicableException,
            OperationNotAvailableException, 
            FailedOperationException, 
            InstanceUnknownException,
            AlreadyUsedIDException 
    {
        this.setUpApp();
        clonedApp = app.clone();

        assertTrue(app.getName().equals(clonedApp.getName()));

        assertTrue(app.getBindingFunction().size() == clonedApp.getBindingFunction().size());

        // check that the binding function is well cloned
        for (StaticBinding firstHalf : app.getBindingFunction().keySet()) {
            StaticBinding secondHalf = app.getBindingFunction().get(firstHalf);

            assertTrue(clonedApp.getBindingFunction().containsKey(firstHalf));
            assertTrue(clonedApp.getBindingFunction().containsValue(secondHalf));
        }

        assertTrue(clonedApp.getNodes().size() == app.getNodes().size());

        // check that each node is well cloned
        for (String nodeName : app.getNodes().keySet()) {
            assertTrue(clonedApp.getNodes().containsKey(nodeName));

            Node appNode = app.getNodes().get(nodeName);
            Node clonedNode = clonedApp.getNodes().get(nodeName);

            assertTrue(appNode.getName().equals(clonedNode.getName()));
            assertTrue(appNode.getInitialState().equals(clonedNode.getInitialState()));

            for (Requirement appNodeReq : appNode.getReqs())
                assertTrue(clonedNode.getReqs().contains(appNodeReq));

            for (String appNodeState : appNode.getStates())
                assertTrue(clonedNode.getStates().contains(appNodeState));

            for (String op : appNode.getOps())
                assertTrue(clonedNode.getOps().contains(op));

            for (String cap : appNode.getCaps())
                assertTrue(clonedNode.getCaps().contains(cap));
        }

        // check that the global state is well cloned
        assertTrue(app.getGlobalState().getActiveNodeInstances().size() == clonedApp.getGlobalState()
                .getActiveNodeInstances().size());
        assertTrue(app.getGlobalState().getRuntimeBindings().size() == clonedApp.getGlobalState().getRuntimeBindings()
                .size());

        Collection<NodeInstance> appInstancesCollection = app.getGlobalState().getActiveNodeInstances().values();
        ArrayList<NodeInstance> appActiveInstances = new ArrayList<>(appInstancesCollection);

        for (NodeInstance appInstance : appActiveInstances) {
            assertTrue(clonedApp.getGlobalState().getActiveNodeInstances().containsKey(appInstance.getID()));
            assertTrue(clonedApp.getGlobalState().getActiveNodeInstances().containsValue(appInstance));

            NodeInstance clonedAppInstance = clonedApp.getGlobalState().getActiveNodeInstances()
                    .get(appInstance.getID());

            assertTrue(appInstance.getCurrentState().equals(clonedAppInstance.getCurrentState()));

            for (Requirement appReq : appInstance.getNeededReqs())
                assertTrue(clonedAppInstance.getNeededReqs().contains(appReq));

            for (String appCap : appInstance.getOfferedCaps())
                assertTrue(clonedAppInstance.getOfferedCaps().contains(appCap));

            for (Transition appTransition : appInstance.getPossibleTransitions())
                assertTrue(clonedAppInstance.getPossibleTransitions().contains(appTransition));

            for (RuntimeBinding appBinding : app.getGlobalState().getRuntimeBindings().get(appInstance.getID()))
                assertTrue(clonedApp.getGlobalState().getRuntimeBindings().get(clonedAppInstance.getID())
                        .contains(appBinding));
        }
    }

    public void setUpApp()
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            IllegalArgumentException,
            OperationNotAvailableException, 
            FailedOperationException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        app.scaleOut1("mongo", "mongoM1");
        app.scaleOut1("node", "nodeN1");
        app.scaleOut1("node", "nodeN2");
        app.scaleOut1("node", "nodeN3");
        app.scaleOut2("frontend", "frontendF1", "nodeN3");
        app.scaleOut2("backend", "backendB1", "nodeN1");
        app.scaleOut2("backend", "backendB2", "nodeN2");

        app.opStart("mongoM1", "start");
        app.opEnd("mongoM1", "start");

        app.opStart("nodeN1", "start");
        app.opEnd("nodeN1", "start");
        app.opStart("nodeN2", "start");
        app.opEnd("nodeN2", "start");
        app.opStart("nodeN3", "start");
        app.opEnd("nodeN3", "start");

        app.opStart("backendB1", "install");
        app.opEnd("backendB1", "install");
        app.opStart("backendB1", "start");
        app.opEnd("backendB1", "start");

        app.opStart("backendB2", "install");
        app.opEnd("backendB2", "install");
        app.opStart("backendB2", "start");
        app.opEnd("backendB2", "start");

        app.opStart("frontendF1", "install");
        app.opEnd("frontendF1", "install");
        app.opStart("frontendF1", "config");
        app.opEnd("frontendF1", "config");
        app.opStart("frontendF1", "start");
        app.opEnd("frontendF1", "start");
    }
}
