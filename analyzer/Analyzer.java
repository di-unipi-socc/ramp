package analyzer;

import java.util.ArrayList;
import java.util.List;

import analyzer.sequence.*;
import exceptions.AlreadyUsedIDException;
import exceptions.FailedFaultHandlingExecption;
import exceptions.FailedOperationException;
import exceptions.IllegalSequenceElementException;
import exceptions.InstanceUnknownException;
import exceptions.OperationNotAvailableException;
import exceptions.RuleNotApplicableException;

import model.Application;
import model.Fault;
import model.NodeInstance;

/**
 * idea del metodo analisi della sequenza con /pi deterministica algoritmo
 * ricorsivo che prende una lista di coppie <app, op> cioe' <app su cui
 * applicare op, op>
 * 
 * -> per la biforcazione dei fault ci serve una deep copy
 */

public class Analyzer {

    public Analyzer() throws NullPointerException {
    }

    public boolean isValidSequence(Application app, List<SequenceElement> sequence)
        throws 
            IllegalSequenceElementException, 
            NullPointerException, 
            InstanceUnknownException,
            IllegalArgumentException, 
            AlreadyUsedIDException 
    {
        if (app == null)
            throw new NullPointerException();

        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        if (sequence.isEmpty() == true)
            return true;

        SequenceElement seqElement = sequence.remove(0);
        try {
            this.executeSequenceElement(app, seqElement);
        } catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (OperationNotAvailableException e) {
            return false;
        } catch (RuleNotApplicableException e) {
            return false;
        }

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        // spezza il controllo (togli l'&& e fai due if, prima elimini le broken
        // instance (no broken instances))
        if (pendingFaults.isEmpty() == true && brokenInstances.isEmpty() == true)
            // there is no biforcation with a deterministic pi, proceeding
            return this.isValidSequence(app, sequence);

        else {
            // there are some faults. there is a biforcation and the sequence must be tested
            // for both branches
            // the not-handled-fault branch goes frist (might fail faster)
            Application cloneApp = app.clone();

            // TODO: questo va comunque fatto dopo l'aver eliminato le broken instances
            if (this.isValidSequence(cloneApp, sequence) == false)
                return false;

            // now we explore the fault handled branch
            // first goes scaleIn that implies the no-broken-instances
            if (brokenInstances.isEmpty() == false) {
                cloneApp = app.clone();
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                } catch (RuleNotApplicableException E) {
                    return false;
                }

                if (this.isValidSequence(cloneApp, sequence) == false)
                    return false;
            }
            // for each fault check both the handled and not handled branch
            for (Fault f : pendingFaults) {
                cloneApp = app.clone();

                if (app.getGlobalState().isResolvableFault(f) == true) {
                    try {
                        cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                    } catch (RuleNotApplicableException e) {
                        return false;
                    }

                    if (this.isValidSequence(cloneApp, sequence) == false)
                        return false;
                } else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                    } catch (FailedFaultHandlingExecption e) {
                        return false;
                    } catch (RuleNotApplicableException e) {
                        return false;
                    }

                    if (this.isValidSequence(cloneApp, sequence) == false)
                        return false;
                }
            }
        }

        return true;
    }

    public boolean isWeaklyValidSequence(Application app, List<SequenceElement> sequence)
            throws IllegalSequenceElementException, NullPointerException {
        if (app == null)
            throw new NullPointerException();

        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        if (sequence.isEmpty() == true)
            return true;

        SequenceElement seqElement = sequence.remove(0);
        try {
            this.executeSequenceElement(app, seqElement);
            // TODO: qui ci vogliono le stesse eccezioni che su valid sequence
        } catch (Exception e) {
            // keep going
        }

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        if (pendingFaults.isEmpty() == true && brokenInstances.isEmpty() == true)
            // there is no biforcation with a deterministic pi, proceeding
            return this.isWeaklyValidSequence(app, sequence);
        else {
            // there are some faults. there is a biforcation and the sequence must be tested
            // for both branches
            // the not-handled-fault branch goes frist (might fail faster)
            Application cloneApp = app.clone();
            if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                return true;

            // now we explore the fault handled branch
            // first goes scaleIn that implies the no-broken-instances
            if (brokenInstances.isEmpty() == false) {
                cloneApp = app.clone();
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                    // keep exploring the tree only if it is possible
                    if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                        return true;
                } catch (RuleNotApplicableException e) {
                }
            }

            // for each fault check both the handled and not handled branch
            for (Fault f : pendingFaults) {
                cloneApp = app.clone();

                if (app.getGlobalState().isResolvableFault(f) == true) {
                    try {
                        cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                        if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                            return true;
                    } catch (Exception e) {
                    }
                } else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                        if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                            return true;
                    } catch (Exception e) {
                    }
                }
            }
        }
        return false;
    }

    public boolean isNotValidSequence(Application app, List<SequenceElement> sequence)
            throws NullPointerException, IllegalSequenceElementException {
        return !this.isWeaklyValidSequence(app, sequence);
    }

    public void executeSequenceElement(Application app, SequenceElement seqElement)
        throws 
            IllegalArgumentException, 
            NullPointerException,
            OperationNotAvailableException,
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            AlreadyUsedIDException 
    {
        switch (seqElement.getRule()) {
            case "opStart":
                OpStart el = (OpStart) seqElement;
                app.opStart(el.getInstnaceID(), el.getOp());                    
                break;
            case "opEnd":
                OpEnd el1 = (OpEnd) seqElement;
                app.opEnd(el1.getInstanceID(), el1.getOp());
                break;
            case "scaleIn":
                ScaleIn el2 = (ScaleIn) seqElement;
                app.scaleIn(el2.getInstanceID());
                break;
            case "scaleOut1":
                ScaleOut1 el3 = (ScaleOut1) seqElement;
                app.scaleOut1(el3.getNodeName(), el3.getIDToAssign());
                break;
            case "scaleOut2": 
                ScaleOut2 el4 = (ScaleOut2) seqElement;
                app.scaleOut2(el4.getNodeName(), el4.getIDToAssign(), el4.getContainerID());
            default:
                break;
        }
    }

    private boolean wellFormattedSequence(List<SequenceElement> sequence){
        boolean res = true;
        
        for (SequenceElement element : sequence) { 
            if(element.wellFormattedSequenceElement() == false)
                res = false;
        }

        return res;
    }   
}