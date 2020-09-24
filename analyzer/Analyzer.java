package analyzer;

import java.util.ArrayList;
import java.util.List;

import analyzer.executable_element.*;
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

    public boolean isValidSequence(Application app, List<ExecutableElement> sequence)
        throws 
            IllegalSequenceElementException, 
            NullPointerException
    {
        if (app == null)
            throw new NullPointerException();

        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        if (sequence.isEmpty() == true)
            return true;

        ExecutableElement seqElement = sequence.remove(0);
        try {
            app.execute(seqElement);
        } catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (OperationNotAvailableException e) {
            return false;
        } catch (RuleNotApplicableException e) {
            return false;
        } catch (InstanceUnknownException e) {
            return false;
        } catch (AlreadyUsedIDException e) {
            return false;
        }

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        Application cloneApp; 

        if(brokenInstances.isEmpty() == false){
            cloneApp = app.clone();
            try {
                cloneApp.scaleIn(brokenInstances.get(0).getID());
                if (this.isValidSequence(cloneApp, sequence) == false)
                    return false;

            } catch (RuleNotApplicableException E) {
                return false;
            }
        }
        //cloneApp was used if brokenInstances is not empty
        cloneApp = app.clone(); 

        //branch where the faults are not handled whatsoever
        if (this.isValidSequence(cloneApp, sequence) == false)
            return false;
        
        //branch where the faults are hanled by their type
        if(pendingFaults.isEmpty() == false){

            //for each fault check if it is pending or resolvable
            for(Fault f : pendingFaults) {
                cloneApp = app.clone();

                if(app.getGlobalState().isResolvableFault(f) == true) {
                    try {
                        cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                        //if the fault is resolved keep exploring the branch
                        if (this.isValidSequence(cloneApp, sequence) == false)
                            return false;
                    } catch (RuleNotApplicableException e) {
                        return false;
                    } catch (InstanceUnknownException e) {
                        return false;
                    }
                }else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                        if (this.isValidSequence(cloneApp, sequence) == false)
                            return false;

                    } catch (FailedFaultHandlingExecption e) {
                        return false;
                    } catch (RuleNotApplicableException e) {
                        return false;
                    } catch (InstanceUnknownException e) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isWeaklyValidSequence(Application app, List<ExecutableElement> sequence)
        throws 
            IllegalSequenceElementException, 
            NullPointerException, 
            InstanceUnknownException
    {
        
        if (app == null)
            throw new NullPointerException();

        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        if (sequence.isEmpty() == true)
            return true;

        ExecutableElement seqElement = sequence.remove(0);
        try {
            app.execute(seqElement);
        }catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (OperationNotAvailableException e) {
            return false;
        } catch (RuleNotApplicableException e) {
            return false;
        } catch (InstanceUnknownException e) {
            return false;
        } catch (AlreadyUsedIDException e) {
            return false;
        }

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        Application cloneApp;

        if(brokenInstances.isEmpty() == false){
            cloneApp = app.clone();
            try {
                cloneApp.scaleIn(brokenInstances.get(0).getID());
                if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                    return true;

            } catch (RuleNotApplicableException E) {
                return false;
            }
        }

        cloneApp = app.clone();

        if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
            return true;

        if(pendingFaults.isEmpty() == false){
            //for each fault check if it is pending or resolvable
            for(Fault f : pendingFaults) {
                cloneApp = app.clone();

                if(app.getGlobalState().isResolvableFault(f) == true) {
                    try {
                        cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                        //if the fault is resolved keep exploring the branch
                        if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                            return true;
                    } catch (RuleNotApplicableException e) {
                        return false;
                    }
                    
                }else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                        //if the fault is resolved keep exploring the branch
                        if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                            return true;

                    } catch (FailedFaultHandlingExecption e) {
                        return false;
                    } catch (RuleNotApplicableException e) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public boolean isNotValidSequence(Application app, List<ExecutableElement> sequence)
            throws NullPointerException, IllegalSequenceElementException, InstanceUnknownException {
        return !this.isWeaklyValidSequence(app, sequence);
    }

    private boolean wellFormattedSequence(List<ExecutableElement> sequence){
        boolean res = true;
        
        for (ExecutableElement element : sequence) { 
            if(element.wellFormattedSequenceElement() == false)
                res = false;
        }

        return res;
    }   
}