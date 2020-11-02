package mprot.lib.test.unitTest.analyzerTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import mprot.lib.analyzer.*;
import mprot.lib.analyzer.execptions.IllegalSequenceElementException;
import mprot.lib.analyzer.executable_element.*;
import mprot.lib.model.*;
import mprot.lib.model.exceptions.*;
import mprot.lib.test.utilities.ThesisAppFactory;


public class AnalyzerTest {

    public Analyzer analyzer;

    public Application toyApp; //for the runtime binding create permutations test

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
    public void analysisFailReportTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException,
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {    
        //returns false, because a weakly valid sequence is not a valid sequence, hence there will be a report;
        //Application testApp = ThesisAppFactory.createApplication(PiVersion.GREEDYPI);
        Application nonDetTestApp = ThesisAppFactory.createApplication(PiVersion.RANDOMPI);

        analyzer.isValidSequence(nonDetTestApp, this.createWeaklyValidSequence());
        
        assertNotNull(analyzer.getFailReports().get(nonDetTestApp.getName()));
        assertTrue(analyzer.getFailReports().get(nonDetTestApp.getName()).getFailType() == FailType.OPERATION);

        Application nonDetTestApp1 = ThesisAppFactory.createApplication(PiVersion.RANDOMPI);
        nonDetTestApp1.setName("nuovaAPP");
        analyzer.isValidSequence(nonDetTestApp, this.createValidSequence());

        assertTrue(analyzer.getFailReports().containsKey("nuovaAPP") == false);
    }

    @Test
    public void planValidityTest()
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            IOException
    {

        //before real tests we check the base cases
        assertTrue(analyzer.isValidPlan(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), new ArrayList<>(), new ArrayList<>()));
        assertTrue(analyzer.isValidPlan(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), new ArrayList<>(), new ArrayList<>()));

        assertTrue(analyzer.isWeaklyValidPlan(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), new ArrayList<>(), new ArrayList<>()));
        assertTrue(analyzer.isWeaklyValidPlan(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), new ArrayList<>(), new ArrayList<>()));

        //creates a valid plan
        List<ExecutableElement> planExecutableElements = new ArrayList<>();
        List<Constraint> constraints = new ArrayList<>();

        ExecutableElement e1 = new ScaleOut1("node", "n1");
        ExecutableElement e2 = new ScaleOut1("mongo", "m1");
        ExecutableElement e3 = new ScaleOut2("backend", "b1", "n1");
        ExecutableElement e4 = new ScaleOut2("frontend", "f1", "n1");
        
        planExecutableElements.add(e1);
        planExecutableElements.add(e2);
        planExecutableElements.add(e3);
        planExecutableElements.add(e4);

        //backend and frontend are in a containment relation with node
        Constraint c1 = new Constraint(e1, e3);
        Constraint c2 = new Constraint(e1, e4);
        constraints.add(c1);
        constraints.add(c2);

        assertTrue(analyzer.isValidPlan(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), planExecutableElements, constraints));
        assertTrue(analyzer.isWeaklyValidPlan(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), planExecutableElements, constraints));
        assertTrue(analyzer.isValidPlan(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), planExecutableElements, constraints));
        assertTrue(analyzer.isWeaklyValidPlan(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), planExecutableElements, constraints));

        //now remove one constraint and the plan becomes only weakly valid
        constraints.remove(c1);
        assertTrue(constraints.size() == 1);

        assertFalse(analyzer.isValidPlan(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), planExecutableElements, constraints));
        assertTrue(analyzer.isWeaklyValidPlan(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), planExecutableElements, constraints));
        assertFalse(analyzer.isValidPlan(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), planExecutableElements, constraints));
        assertTrue(analyzer.isWeaklyValidPlan(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), planExecutableElements, constraints));
    
        assertTrue(analyzer.isValidPlan(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), new ArrayList<>(), constraints));
    
    }

    @Test
    // a valid sequence is declared valid
    public void validSequenceTest() 
        throws 
            NullPointerException, 
            IllegalSequenceElementException,
            IllegalArgumentException, 
            InstanceUnknownException
             
    {   
        assertTrue(analyzer.isValidSequence(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), new ArrayList<>()));
        
        assertTrue(analyzer.isValidSequence(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), this.createValidSequence()));
        assertFalse(analyzer.isValidSequence(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), this.createWeaklyValidSequence()));
        assertTrue(analyzer.isValidSequence(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), this.createValidSequence()));
        assertFalse(analyzer.isValidSequence(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), this.createWeaklyValidSequence()));
    }

    @Test
    public void weaklyValidSequenceTest() 
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            RuleNotApplicableException, 
            InstanceUnknownException
    {
        assertTrue(analyzer.isWeaklyValidSequence(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), new ArrayList<>()));

        assertTrue(analyzer.isWeaklyValidSequence(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), this.createValidSequence())); 
        assertTrue(analyzer.isWeaklyValidSequence(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), this.createWeaklyValidSequence()));
        assertTrue(analyzer.isWeaklyValidSequence(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), this.createValidSequence())); 
        assertTrue(analyzer.isWeaklyValidSequence(ThesisAppFactory.createApplication(PiVersion.RANDOMPI), this.createWeaklyValidSequence()));
    }

    @Test 
    public void isNotValidSequenceTest()
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {
        assertFalse(analyzer.isNotValidSequence(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), new ArrayList<>()));
        assertFalse(analyzer.isNotValidSequence(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), this.createValidSequence())); 

        assertTrue(analyzer.isNotValidSequence(ThesisAppFactory.createApplication(PiVersion.GREEDYPI), this.createNotValidSequence()));
    }


    public ArrayList<ExecutableElement> createNotValidSequence(){
        ArrayList<ExecutableElement> notValidSequence = new ArrayList<>();

        ExecutableElement e1 = new ScaleOut1("node", "n1");
        ExecutableElement e2 = new ScaleOut1("mongo", "m1");
        ExecutableElement e3 = new ScaleOut2("backend", "b1", "n1");
        ExecutableElement e4 = new ScaleOut2("frontend", "f1", "n1");

        notValidSequence.add(e1);
        notValidSequence.add(e2);
        notValidSequence.add(e3);
        notValidSequence.add(e4);
        
        this.addOpStartEnd(notValidSequence, "n1", "start");
        this.addOpStartEnd(notValidSequence, "m1", "start");
        this.addOpStartEnd(notValidSequence, "b1", "install");
        this.addOpStartEnd(notValidSequence, "b1", "start");
        this.addOpStartEnd(notValidSequence, "f1", "install");
        this.addOpStartEnd(notValidSequence, "f1", "config");
        this.addOpStartEnd(notValidSequence, "b1", "stop");

        this.addOpStartEnd(notValidSequence, "f1", "install"); 
        //creates fault (hence biforcation)
        //after the fault f1 is in the transitional state configured-start-working
        //after fault() f1 goes in configured
        //f1 for start requires conn that backend is not providing since it's stopped

        /**
         * not valid because install is an op not available in the "configured", which 
         * is reached if the fault handler is executed (that put f1 in configured)
         * if the fault handler is or is not executed there is not a global state to go either
         */
        this.addOpStartEnd(notValidSequence, "f1", "install");
        this.addOpStartEnd(notValidSequence, "n1", "stop");

        return notValidSequence;
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

        //creates fault (hence biforcation) (f1 needs conn from b1)
        this.addOpStartEnd(validSequence, "f1", "start"); 

        //this two ops will be tested among two brenches (the one where the 
        //pending fault created is handled and the one where it is not)
        //since this two ops do not use f1 the sequence will be valid
        this.addOpStartEnd(validSequence, "m1", "stop");
        this.addOpStartEnd(validSequence, "n1", "stop");

        ExecutableElement e7 = new ScaleIn("f1");
        ExecutableElement e8 = new ScaleIn("b1");
        ExecutableElement e5 = new ScaleIn("n1");
        ExecutableElement e6 = new ScaleIn("m1");
        

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

    //creates the simple applications to use for the test of create permutations
    public Application createToyApp(){
        Application ret = new Application("toy", PiVersion.GREEDYPI);

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
        StaticBinding secondHalf1 = new StaticBinding("nodeC", "db");
        ret.addStaticBinding(firstHalf1, secondHalf1);

        return ret;
    }

    public Node createNodeB(){
        Node ret = new Node("nodeB", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();
        ret.addState("state1");

        ret.addCapability("server");

        mp.addRhoEntry("state1", new ArrayList<Requirement>());
        
        List<String> runningCaps = new ArrayList<>();
        runningCaps.add("server");
        mp.addGammaEntry("state1", runningCaps);

        for (String state : ret.getStates()) 
            mp.addPhiEntry(state, new ArrayList<String>());

        return ret;
    }

    public Node createNodeC(){
        Node ret = new Node("nodeC", "state1", new ManagementProtocol());
        ManagementProtocol mp = ret.getManagementProtocol();
        ret.addState("state1");

        ret.addCapability("db");

        mp.addRhoEntry("state1", new ArrayList<Requirement>());
        
        List<String> runningCaps = new ArrayList<>();
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
    public void recursiveRunBindingPerm() throws NullPointerException, IllegalArgumentException,
            RuleNotApplicableException, InstanceUnknownException, AlreadyUsedIDException {

        this.toyApp.scaleOut1("nodeC", "instanceC1");
        this.toyApp.scaleOut1("nodeC", "instanceC2");

        this.toyApp.scaleOut1("nodeB", "instanceB1");
        this.toyApp.scaleOut1("nodeB", "instanceB2");

        this.toyApp.scaleOut1("nodeA", "instanceA");

        List<List<RuntimeBinding>> permutations = analyzer.createRunBindingCombs(this.toyApp, "instanceA");
        assertTrue(permutations.size() == 4);

        assertTrue(printRunBindingPermutations(permutations), true);
    }

    public String printRunBindingPermutations(List<List<RuntimeBinding>> permutations){
        String s = "";

        s = s.concat("[ ");
        for (List<RuntimeBinding> list : permutations) {
            s = s.concat("[");
            for (RuntimeBinding runBinding : list) {
                s = s.concat("<" + runBinding.getNodeInstanceID() + " " + runBinding.getReq().getName() + "> "); 
            }
            s = s.concat("] ");
        }

        s = s.concat(" ]");
        return s;
    }


    
    @Test
    public void buildConstraintMapTest(){
        ExecutableElement e1 = new ScaleOut1("nodeName", "idToAssign");
        ExecutableElement e2 = new ScaleOut2("nodeName", "idToAssign", "containerID");
        ExecutableElement e3 = new ScaleIn("instanceID");
        ExecutableElement e4 = new OpStart("instanceID", "op");

        List<ExecutableElement> planExElements = new ArrayList<>();
        planExElements.add(e1);
        planExElements.add(e2);
        planExElements.add(e3);
        planExElements.add(e4);

        Constraint c1 = new Constraint(e1, e2);
        Constraint c2 = new Constraint(e1, e3);
        Constraint c3 = new Constraint(e2, e3);

        List<Constraint> constraints = new ArrayList<>();
        constraints.add(c1);
        constraints.add(c2);
        constraints.add(c3);

        Map<ExecutableElement, List<ExecutableElement>> constraintsMap = analyzer.buildConstraintMap(planExElements, constraints);

        assertTrue(constraintsMap.get(e1).size() == 2);
        assertTrue(constraintsMap.get(e1).contains(e2) && constraintsMap.get(e1).contains(e2));
        assertTrue(constraintsMap.get(e4).size() == 0);
        assertTrue(constraintsMap.get(e2).contains(e3));
    }
    
    @Test
    public void createPermsTest(){
        ExecutableElement e1 = new ScaleOut1("nodeName", "idToAssign");
        ExecutableElement e2 = new ScaleOut2("nodeName", "idToAssign", "containerID");
        ExecutableElement e3 = new ScaleIn("instanceID");

        List<ExecutableElement> planExElements = new ArrayList<>();
        planExElements.add(e1);
        planExElements.add(e2);
        planExElements.add(e3);

        Constraint c1 = new Constraint(e1, e2);
        Constraint c2 = new Constraint(e1, e3);
        Constraint c3 = new Constraint(e2, e3);

        List<Constraint> constraints = new ArrayList<>();
        constraints.add(c1);
        constraints.add(c2);
        constraints.add(c3);


        List<List<ExecutableElement>> allPerms = this.createExElementsPerm(planExElements);
        
        assertFalse("size: " + allPerms.size() + " " +   printEEPerms(allPerms), false);
    }

    //basically the same algorithm used in analyzer. usefull for testing
    private List<List<ExecutableElement>> createExElementsPerm(List<ExecutableElement> planExElements){
        List<List<ExecutableElement>> allPerms = new ArrayList<>();
        allPerms.add(this.clonePerm(planExElements));
        int permSize = planExElements.size();

        int[] c = new int[permSize];        
        for(int i = 0; i < permSize; i++)
            c[i] = 0;
        
        int i = 0;
        while(i < permSize){
            
            if(c[i] < i){
                
                if(i % 2 == 0)
                    Collections.swap(planExElements, 0, i);
                else
                    Collections.swap(planExElements, i, c[i]);

                //permutation complete;
                allPerms.add(this.clonePerm(planExElements));
                
                c[i]++;
                i = 0;
            
            }else{
                c[i] = 0;
                i++;
            }
                                
        }

        return allPerms;
    } 

    private List<ExecutableElement> clonePerm(List<ExecutableElement> perm){

        List<ExecutableElement> clone = new ArrayList<>();

        for (ExecutableElement executableElement : perm) 
            clone.add(executableElement);
        
        return clone;

    }


    public String printEEPerm(List<ExecutableElement> perm){
        String s = "";

        s = s.concat("\n[");
        for (ExecutableElement ee : perm){
            if(ee instanceof ScaleOut1){
                ScaleOut1 print = (ScaleOut1) ee;
                s = s.concat("<" + print.getRule() + " " + print.getIDToAssign() + "> "); 
            }
            if(ee instanceof ScaleOut2){
                ScaleOut2 print = (ScaleOut2) ee;
                s = s.concat("<" + print.getRule() + " " + print.getIDToAssign() + "> "); 
            }


        }
            
        s = s.concat("] \n");

       return s;
    }

    public String printEEPerms(List<List<ExecutableElement>> permutations){
        String s = "";

        s = s.concat("[ ");
        for (List<ExecutableElement> list : permutations) {
            s = s.concat("\n[");
            for (ExecutableElement ee : list) 
                s = s.concat(ee.getRule() + " "); 
                
            s = s.concat("] \n");
        }

        s = s.concat(" ]");
        return s;
    }


}
