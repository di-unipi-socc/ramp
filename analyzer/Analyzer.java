package analyzer;

import java.util.ArrayList;
import java.util.List;

import exceptions.FailedFaultHandlingExecption;
import exceptions.FailedOperationException;
import exceptions.IllegalSequenceElementException;
import exceptions.NodeUnknownException;
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
            NullPointerException 
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
            //keep going, this will be handled by the fault handler or leaved as it is
        } catch (OperationNotAvailableException e){
            return false;
        } catch(RuleNotApplicableException e){
            return false;
        } catch(NodeUnknownException e){
            return false;
        }

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        if(pendingFaults.isEmpty() == true && brokenInstances.isEmpty() == true)
            //there is no biforcation with a deterministic pi, proceeding
            return this.isValidSequence(app, sequence);
        else{
            //there are some faults. there is a biforcation and the sequence must be tested for both branches
            //the not-handled-fault branch goes frist (might fail faster)
            Application cloneApp = AppCloner.cloneApp(app);

            if(this.isValidSequence(cloneApp, sequence) == false)
                return false;
            
            //now we explore the fault handled branch
            //first goes scaleIn that implies the no-broken-instances
            if(brokenInstances.isEmpty() == false){
                cloneApp = AppCloner.cloneApp(app);
                try{
                    cloneApp.scaleIn(brokenInstances.get(0));
                } catch(RuleNotApplicableException E){
                    return false;
                }
            
                if(this.isValidSequence(cloneApp, sequence) == false)
                    return false;
            }
            //for each fault check both the handled and not handled branch
            for (Fault f : pendingFaults) { 
                cloneApp = AppCloner.cloneApp(app);

                if(app.getGlobalState().isResolvableFault(f) == true){
                    try {
                        cloneApp.autoreconnect(cloneApp.getGlobalState().getActiveNodeInstances().get(f.getInstanceID()), f.getReq());
                    } catch (RuleNotApplicableException e) {
                        return false;
                    }
                    
                    if(this.isValidSequence(cloneApp, sequence) == false)
                        return false;  
                }else{
                    try {
                        cloneApp.fault(cloneApp.getGlobalState().getActiveNodeInstances().get(f.getInstanceID()), f.getReq()); 
                    } catch (FailedFaultHandlingExecption e) {
                        return false;
                    } catch (RuleNotApplicableException e) {
                        return false;
                    }

                    if(this.isValidSequence(cloneApp, sequence) == false)
                        return false;  
                }
            }
        }
        
        return true;
    }

    public boolean isWeaklyValidSequence(Application app, List<SequenceElement> sequence)
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

        SequenceElement seqElement = sequence.remove(0);
        try {
            this.executeSequenceElement(app, seqElement);
        } catch (Exception e) {
            //keep going
        } 

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        if(pendingFaults.isEmpty() == true && brokenInstances.isEmpty() == true)
        //there is no biforcation with a deterministic pi, proceeding
            return this.isWeaklyValidSequence(app, sequence);
        else{
            //there are some faults. there is a biforcation and the sequence must be tested for both branches
            //the not-handled-fault branch goes frist (might fail faster)
            Application cloneApp = AppCloner.cloneApp(app);
            if(this.isWeaklyValidSequence(cloneApp, sequence) == true)
                return true;
            
            //now we explore the fault handled branch
            //first goes scaleIn that implies the no-broken-instances
            if(brokenInstances.isEmpty() == false){
                cloneApp = AppCloner.cloneApp(app);
                try {
                    cloneApp.scaleIn(brokenInstances.get(0));
                    //keep exploring the tree only if it is possible
                    if(this.isWeaklyValidSequence(cloneApp, sequence) == true)
                        return true;
                } catch (RuleNotApplicableException e) {}
            }

            //for each fault check both the handled and not handled branch
            for (Fault f : pendingFaults) {
                cloneApp = AppCloner.cloneApp(app);

                if(app.getGlobalState().isResolvableFault(f) == true){
                    try {
                        cloneApp.autoreconnect(cloneApp.getGlobalState().getActiveNodeInstances().get(f.getInstanceID()), f.getReq()); 
                        if(this.isWeaklyValidSequence(cloneApp, sequence) == true)
                            return true;  
                    } catch (Exception e) {} 
                }else{
                    try {
                        cloneApp.fault(cloneApp.getGlobalState().getActiveNodeInstances().get(f.getInstanceID()), f.getReq());  
                        if(this.isWeaklyValidSequence(cloneApp, sequence) == true)
                            return true;   
                    } catch (Exception e) {}
                }
            }
        }
        return false;
    }

    public boolean isNotValidSequence(Application app, List<SequenceElement> sequence)
        throws 
            NullPointerException,
            IllegalSequenceElementException
    {
        if (app == null)
            throw new NullPointerException();
        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        return !this.isWeaklyValidSequence(app, sequence);
    }

    public void executeSequenceElement(Application app, SequenceElement seqElement)
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException,
            FailedOperationException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        switch (seqElement.getRule()) {
            case "opStart":
                app.opStart(seqElement.getTargetInstance(), seqElement.getOp());
                break;
            case "opEnd":
                app.opEnd(seqElement.getTargetInstance(), seqElement.getOp());
                break;
            case "scaleIn":
                app.scaleIn(seqElement.getTargetInstance());
                break;
            case "scaleOut1":
                app.scaleOut1(seqElement.getTargetNode());
                break;
            case "scaleOut2": 
                app.scaleOut2(seqElement.getTargetNode(), seqElement.getServingInstance());
            default:
                break;
        }
    }

    //metodo privato che controlla che gli elementi della sequenza siano ben formattati (abbiano i giusti campi)
    private boolean wellFormattedSequence(List<SequenceElement> sequence){
        boolean res = true;
        
        for (SequenceElement element : sequence) { 
            if(element.wellFormattedOpSequence() == false)
                res = false;
        }

        return res;
    }   
}