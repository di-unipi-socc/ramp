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
import utilities.ThesisAppFactory;

public class AnalyzerTest {

    public Application app;
    public Analyzer analyzer;

    @Before
    public void setUp()
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            InstanceUnknownException 
    {
        analyzer = new Analyzer();
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

}
