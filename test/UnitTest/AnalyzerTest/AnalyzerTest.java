package test.UnitTest.AnalyzerTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import analyzer.Analyzer;
import analyzer.executable_element.*;

import exceptions.AlreadyUsedIDException;
import exceptions.IllegalSequenceElementException;
import exceptions.InstanceUnknownException;
import exceptions.RuleNotApplicableException;
import model.Application;
import model.ManagementProtocol;
import model.Node;
import model.NodeInstance;
import model.Requirement;
import model.RequirementSort;
import model.RuntimeBinding;
import model.StaticBinding;
import utilities.ThesisAppFactory;

public class AnalyzerTest {

    public Application app;
    public Analyzer analyzer;

    public Application toyApp; //for the create combinations test

    @Before
    public void setUp()
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            InstanceUnknownException 
    {
        this.analyzer = new Analyzer();
        this.toyApp = this.createToyApp();
    }

    @Test
    // a valid sequence is declared valid
    public void validSequenceTest() 
        throws 
            NullPointerException, 
            IllegalSequenceElementException,
            IllegalArgumentException
             
    {    
        assertTrue(analyzer.isValidSequence(ThesisAppFactory.createApplication(), this.createValidSequence()));
        assertFalse(analyzer.isValidSequence(ThesisAppFactory.createApplication(), this.createWeaklyValidSequence()));
    }

    @Test
    public void weaklyValidSequenceTest() 
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            RuleNotApplicableException, 
            InstanceUnknownException
    {
        assertTrue(analyzer.isWeaklyValidSequence(ThesisAppFactory.createApplication(), this.createValidSequence())); 
        assertTrue(analyzer.isWeaklyValidSequence(ThesisAppFactory.createApplication(), this.createWeaklyValidSequence()));

    }

    public ArrayList<ExecutableElement> createValidSequence(){
        ArrayList<ExecutableElement> validSequence = new ArrayList<>();

        ExecutableElement e1 = new ScaleOut1("node", "n1");
        ExecutableElement e2 = new ScaleOut1("mongo", "m1");
        ExecutableElement e3 = new ScaleOut2("backend", "b1", "n1");
        ExecutableElement e4 = new ScaleOut2("frontend", "f1", "n1");

        validSequence.add(e1);
        validSequence.add(e2);
        validSequence.add(e3);
        validSequence.add(e4);

        this.addOpStartEnd(validSequence, "n1", "start");
        this.addOpStartEnd(validSequence, "m1", "start");
        this.addOpStartEnd(validSequence, "b1", "install");
        this.addOpStartEnd(validSequence, "b1", "start");
        this.addOpStartEnd(validSequence, "f1", "install");
        this.addOpStartEnd(validSequence, "f1", "config");
        this.addOpStartEnd(validSequence, "b1", "stop");

        this.addOpStartEnd(validSequence, "f1", "start"); //creates fault (hence biforcation)
        //after the fault f1 is in the transitional state configured-start-working

        //this two ops will be tested among two brenches (the one where the 
        //pending fault created is handled and the one where it is not)
        //since this two ops do not use f1 the sequence will be valid
        this.addOpStartEnd(validSequence, "m1", "stop");
        this.addOpStartEnd(validSequence, "n1", "stop");

        ExecutableElement e5 = new ScaleIn("n1");
        ExecutableElement e6 = new ScaleIn("m1");
        ExecutableElement e7 = new ScaleIn("f1");
        ExecutableElement e8 = new ScaleIn("b1");

        //inverted order for broken instances
        //mind that if there is a broke instance there will be scaleIn on cascade
        //if we scaleIn an instance that is not alive (or known) the system raise an error

        validSequence.add(e7); 
        validSequence.add(e8);
        validSequence.add(e5);
        validSequence.add(e6);
        
        return validSequence;
    }

    public ArrayList<ExecutableElement> createWeaklyValidSequence(){

        ArrayList<ExecutableElement> weaklyValidSequence = new ArrayList<>();

        ExecutableElement e1 = new ScaleOut1("node", "n1");
        ExecutableElement e2 = new ScaleOut1("mongo", "m1");
        ExecutableElement e3 = new ScaleOut2("backend", "b1", "n1");
        ExecutableElement e4 = new ScaleOut2("frontend", "f1", "n1");

        weaklyValidSequence.add(e1);
        weaklyValidSequence.add(e2);
        weaklyValidSequence.add(e3);
        weaklyValidSequence.add(e4);
        
        this.addOpStartEnd(weaklyValidSequence, "n1", "start");
        this.addOpStartEnd(weaklyValidSequence, "m1", "start");
        this.addOpStartEnd(weaklyValidSequence, "b1", "install");
        this.addOpStartEnd(weaklyValidSequence, "b1", "start");
        this.addOpStartEnd(weaklyValidSequence, "f1", "install");
        this.addOpStartEnd(weaklyValidSequence, "f1", "config");
        this.addOpStartEnd(weaklyValidSequence, "b1", "stop");

        this.addOpStartEnd(weaklyValidSequence, "f1", "start"); //creates fault (hence biforcation)
        //after the fault f1 is in the transitional state configured-start-working

        /**
         * weakly valid because uninstall is an op available in the "configured", which 
         * is reached if the fault handler is executed (that put f1 in configured)
         * if the fault handler is not executed there is not a global state to go
         */
        this.addOpStartEnd(weaklyValidSequence, "f1", "uninstall");
        this.addOpStartEnd(weaklyValidSequence, "n1", "stop");

        return weaklyValidSequence;
    }

    public void addOpStartEnd(List<ExecutableElement> sequence, String instanceID, String op){
        ExecutableElement opStart = new OpStart(instanceID, op);
        ExecutableElement opEnd = new OpEnd(instanceID, op);

        sequence.add(opStart);
        sequence.add(opEnd);
    }

    //creates the simple applications to use for the test of create combinations
    public Application createToyApp(){
        Application ret = new Application("toy");

        Node nodeA = this.createNodeA();
        Node nodeB = this.createNodeB();
        Node nodeC = this.createNodeC();

        ret.addNode(nodeA);
        ret.addNode(nodeB);
        ret.addNode(nodeC);

        StaticBinding firstHalf = new StaticBinding("nodeA", "server");
        StaticBinding secondHalf = new StaticBinding("nodeB", "server");
        ret.addStaticBinding(firstHalf, secondHalf);

        StaticBinding firstHalf1 = new StaticBinding("nodeA", "db");
        StaticBinding secondHalf1 = new StaticBinding("nodeB", "db");
        ret.addStaticBinding(firstHalf1, secondHalf1);

        StaticBinding firstHalf2 = new StaticBinding("nodeA", "server");
        StaticBinding secondHalf2 = new StaticBinding("nodeC", "server");
        ret.addStaticBinding(firstHalf2, secondHalf2);

        StaticBinding firstHalf3 = new StaticBinding("nodeA", "db");
        StaticBinding secondHalf3 = new StaticBinding("nodeC", "db");
        ret.addStaticBinding(firstHalf3, secondHalf3);

        return ret;
    }

    public Node createNodeB(){
        Node ret = new Node("nodeB", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();
        ret.addState("state1");

        ret.addCapability("server");
        ret.addCapability("db");

        mp.addRhoEntry("state1", new ArrayList<Requirement>());
        
        List<String> runningCaps = new ArrayList<>();
        runningCaps.add("server");
        runningCaps.add("db");
        mp.addGammaEntry("state1", runningCaps);

        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());

        return ret;
    }

    public Node createNodeC(){
        Node ret = new Node("nodeC", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();
        ret.addState("state1");

        ret.addCapability("server");
        ret.addCapability("db");

        mp.addRhoEntry("state1", new ArrayList<Requirement>());
        
        List<String> runningCaps = new ArrayList<>();
        runningCaps.add("server");
        runningCaps.add("db");
        mp.addGammaEntry("state1", runningCaps);

        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());

        return ret;
    }

    public Node createNodeA(){
        Node ret = new Node("nodeA", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();

        Requirement server = new Requirement("server", RequirementSort.REPLICA_UNAWARE);
        Requirement db = new Requirement("db", RequirementSort.REPLICA_UNAWARE);

        ret.addState("state1");

        ret.addRequirement(server);
        ret.addRequirement(db);

        List<Requirement> testReqs = new ArrayList<>();
        testReqs.add(server);
        testReqs.add(db);
        mp.addRhoEntry("state1", testReqs);

        //gamma: state -> caps offered in that state
        for (String state : ret.getStates())
            mp.addGammaEntry(state, new ArrayList<String>());

        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());
        
        return ret;
    }

    @Test
    public void recursiveCombinationsTest() throws NullPointerException, IllegalArgumentException,
            RuleNotApplicableException, InstanceUnknownException, AlreadyUsedIDException {

        this.toyApp.scaleOut1("nodeC", "instanceC");
        this.toyApp.scaleOut1("nodeB", "instanceB");
        NodeInstance instanceA = this.toyApp.scaleOut1("nodeA", "instanceA");

        analyzer.createBindingCombinations(this.toyApp, "instanceA");

        //List<List<RuntimeBinding>> combinations = analyzer.createBindingCombinations(this.toyApp, "instanceA");
        Requirement server = new Requirement("server", RequirementSort.REPLICA_UNAWARE);

        assertTrue(this.toyApp.getGlobalState().getCapableInstances(instanceA, server).get(0).getID(), false);

    }
}
