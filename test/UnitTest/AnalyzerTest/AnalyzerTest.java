package test.UnitTest.AnalyzerTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import analyzer.Analyzer;
import analyzer.SequenceElement;
import exceptions.IllegalSequenceElementException;
import exceptions.NodeUnknownException;
import exceptions.RuleNotApplicableException;
import model.Application;
import model.NodeInstance;
import test.ThesisAppFactory;

public class AnalyzerTest {

    public Application app;

    public NodeInstance n1;
    public NodeInstance m1;
    public NodeInstance b1;
    public NodeInstance f1;

    public Analyzer analyzer;

    @Before
    public void setUp() throws NullPointerException, RuleNotApplicableException, NodeUnknownException {
        app = ThesisAppFactory.createApplication();
        n1 = app.scaleOut1(app.getNodes().get("node"));
        m1 = app.scaleOut1(app.getNodes().get("mongo"));
        b1 = app.scaleOut2(app.getNodes().get("backend"), n1);
        f1 = app.scaleOut2(app.getNodes().get("frontend"), n1);
        analyzer = new Analyzer();
    }

    @Test
    // a valid sequence is declared valid
    public void validSequenceTest() throws NullPointerException, IllegalSequenceElementException {
        List<SequenceElement> validSequence = this.createValidSequence();
        List<SequenceElement> weaklyValidSequence = this.createWeaklyValidSequence();    

        assertTrue(analyzer.isValidSequence(app, validSequence));
        assertFalse(analyzer.isValidSequence(app, weaklyValidSequence));
    }

    @Test
    public void weaklyValidSequenceTest() throws NullPointerException, IllegalSequenceElementException {
        //a valid sequence is obv a weakly valid sequence
        assertTrue(analyzer.isWeaklyValidSequence(app, this.createValidSequence())); 
        assertTrue(analyzer.isWeaklyValidSequence(app, this.createWeaklyValidSequence()));
    }

    public ArrayList<SequenceElement> createValidSequence(){
        ArrayList<SequenceElement> validSequence = new ArrayList<>();

        this.addSequenceElement(validSequence, "start", n1);
        this.addSequenceElement(validSequence, "start", m1);
        this.addSequenceElement(validSequence, "install", b1);
        this.addSequenceElement(validSequence, "start", b1);
        this.addSequenceElement(validSequence, "install", f1);
        this.addSequenceElement(validSequence, "config", f1);
        this.addSequenceElement(validSequence, "stop", b1);

        this.addSequenceElement(validSequence, "start", f1); //creates fault (hence biforcation)
        //after the fault f1 is in the transitional state configured-start-working

        //this two ops will be tested among two brenches (the one where the 
        //pending fault created is handled and the one where it is not)
        //since this two ops do not use f1 the sequence will be valid
        this.addSequenceElement(validSequence, "stop", m1);
        this.addSequenceElement(validSequence, "stop", n1);

        return validSequence;
    }

    public ArrayList<SequenceElement> createWeaklyValidSequence(){
        ArrayList<SequenceElement> weaklyValidSequence = new ArrayList<>();

        this.addSequenceElement(weaklyValidSequence, "start", n1);
        this.addSequenceElement(weaklyValidSequence, "start", m1);
        this.addSequenceElement(weaklyValidSequence, "install", b1);
        this.addSequenceElement(weaklyValidSequence, "start", b1);
        this.addSequenceElement(weaklyValidSequence, "install", f1);
        this.addSequenceElement(weaklyValidSequence, "config", f1);
        this.addSequenceElement(weaklyValidSequence, "stop", b1);

        this.addSequenceElement(weaklyValidSequence, "start", f1); //creates fault (hence biforcation)
        //after the fault f1 is in the transitional state configured-start-working

        /**
         * weakly valid because uninstall is an op available in the "configured", which 
         * is reached if the fault handler is executed (that put f1 in configured)
         * if the fault handler is not executed there is not a global state to go
         */
        this.addSequenceElement(weaklyValidSequence, "uninstall", f1);
        this.addSequenceElement(weaklyValidSequence, "stop", n1);

        return weaklyValidSequence;
    }


    public void addSequenceElement(List<SequenceElement> sequence, String op, NodeInstance targetInstance){
        SequenceElement start = new SequenceElement("opStart");
        start.setOp(op);
        start.setTargetInstance(targetInstance);

        SequenceElement stop = new SequenceElement("opEnd");
        stop.setOp(op);
        stop.setTargetInstance(targetInstance);

        sequence.add(start);
        sequence.add(stop);
    }

}
