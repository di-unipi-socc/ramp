package analyzer;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.midi.Sequence;

import analyzer.executable_element.*;
import exceptions.*;
import model.*;

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

    /**
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @return
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws IllegalSequenceElementException
     * @throws InstanceUnknownException
     */
    public boolean isValidSequence(Application app, List<ExecutableElement> sequence) 
        throws 
            NullPointerException,
            IllegalArgumentException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {

        if(app.isPiDeterministic() == true)
            return this.deterministicIsValidSequence(app, sequence);
        else
            return this.nonDeterministicIsValidSeq(app, sequence);

    }

    /**
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @return
     * @throws NullPointerException
     * @throws IllegalSequenceElementException
     * @throws InstanceUnknownException
     */
    public boolean isWeaklyValidSequence(Application app, List<ExecutableElement> sequence)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {

        if(app.isPiDeterministic() == true)
            return this.deterministicIsWeaklyValidSequence(app, sequence);
        else
            return this.nonDeterministicIsWeaklyValidSeq(app, sequence);

    }


    private boolean deterministicIsValidSequence(Application app, List<ExecutableElement> sequence)
        throws 
            IllegalSequenceElementException, 
            NullPointerException, 
            IllegalArgumentException,
            InstanceUnknownException 
    {
        if (app == null)
            throw new NullPointerException();

        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        if (sequence.isEmpty() == true)
            return true;

        //pop one element from the sequence than execute it
        ExecutableElement seqElement = sequence.remove(0);
        try {
            app.execute(seqElement);
        } catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (Exception e) {
            return false;
        }

        //for every fault the tree has n new biforcation to check
        if(this.checkFaultsValid(app, sequence, true) == false)
            return false;

        return true;
    }

    private boolean deterministicIsWeaklyValidSequence(Application app, List<ExecutableElement> sequence)
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
        } catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (Exception e) {
            return false;
        }

        if(this.checkFaultsWeaklyValid(app, sequence, true) == true)
            return true;
        
        return false;
    }

    private boolean nonDeterministicIsWeaklyValidSeq(Application app, List<ExecutableElement> sequence)
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

        List<List<RuntimeBinding>> combinations = null;
        Application cloneApp = null;

        if(sequence.isEmpty() == true)
            return true;
        
        ExecutableElement op = sequence.remove(0);

        //for each kind of op there is a specific check
        if (op instanceof OpStart) {
            OpStart todo = (OpStart) op;
            NodeInstance instance = app.getGlobalState().getNodeInstanceByID(todo.getInstnaceID());

            //requirements needed by instance before doing the op
            List<Requirement> neededReqsBeforeOp = instance.getNeededReqs();
            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }

            //check the fault branches
            if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                return true;
            //requirements needed after doing the op
            List<Requirement> neededReqsAfterOp = instance.getNeededReqs();

            //here will be the requirements that were needed before op and are still needed
            List<Requirement> neededReqsBeforeAndAfter = new ArrayList<>();
            for (Requirement req : neededReqsBeforeOp) {
                if (neededReqsAfterOp.contains(req))
                    neededReqsBeforeAndAfter.add(req);
            }

            //current runtime binding of instance
            List<RuntimeBinding> instanceRuntimeBinding = app.getGlobalState().getRuntimeBindings().get(instance.getID());

            //here will be the runtime binding that are about the requirement that was and are still needed 
            //those runtime bindings will not be changed with the combinations
            List<RuntimeBinding> runtimeBindingBeforeAndAfter = new ArrayList<>();

            for (RuntimeBinding rb : instanceRuntimeBinding) {
                for (Requirement req : neededReqsBeforeAndAfter) {
                    if (req.equals(rb.getReq()))
                        runtimeBindingBeforeAndAfter.add(rb);
                }
            }

            combinations = this.createBindingCombinations(app, todo.getInstnaceID());

            if(combinations.isEmpty() == true)
                return this.nonDeterministicIsWeaklyValidSeq(app.clone(), sequence);
            
            //for each combination of runtime bindings we check if the combination contains the "unchangable" runtimebindings
            //if so we complement with the new runtime binding, checking for each possible combination
            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runtimeBindingBeforeAndAfter) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                        return true;
                }
            }
        }

        if (op instanceof OpEnd) {
            //exactly the same as opStart
            //TODO: trova come risparmiare codice
            OpEnd todo = (OpEnd) op;
            NodeInstance instance = app.getGlobalState().getActiveNodeInstances().get(todo.getInstanceID());

            List<Requirement> neededReqsBeforeOp = instance.getNeededReqs();
            
            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }
            
            if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                return true;

            List<Requirement> neededReqsAfterOp = instance.getNeededReqs();
            List<Requirement> neededReqsBeforeAndAfter = new ArrayList<>();

            for (Requirement req : neededReqsBeforeOp) {
                if (neededReqsAfterOp.contains(req))
                    neededReqsBeforeAndAfter.add(req);
            }

            List<RuntimeBinding> instanceRuntimeBinding = app.getGlobalState().getRuntimeBindings().get(instance.getID());
            List<RuntimeBinding> runtimeBindingBeforeAndAfter = new ArrayList<>();

            for (RuntimeBinding rb : instanceRuntimeBinding) {
                for (Requirement req : neededReqsBeforeAndAfter) {
                    if (req.equals(rb.getReq()))
                        runtimeBindingBeforeAndAfter.add(rb);
                }
            }

            combinations = this.createBindingCombinations(app, todo.getInstanceID());

            if(combinations.isEmpty() == true)
                return this.nonDeterministicIsValidSeq(app.clone(), sequence);

            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runtimeBindingBeforeAndAfter) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                        return true;
                }
            }
        }

        if (op instanceof ScaleIn) {
            ScaleIn todo = (ScaleIn) op;

            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }

            if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                return true;

            cloneApp = app.clone();
                
            if(this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                return true;
            
        }

        if (op instanceof ScaleOut1) {
            ScaleOut1 todo = (ScaleOut1) op;

            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }

            if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                return true;
            
            //ScaleOut1 creates automatically the needed runtime binding but we want every possible cominations
            //so we removes the bindings and go with the combinations
            //TODO: chiedi se e' giusto both ways oppure quelli devono essere lasciati
            app.getGlobalState().removeAllBindingsBothWays(todo.getIDToAssign());

            combinations = this.createBindingCombinations(app, todo.getIDToAssign());
            if(combinations.isEmpty() == true)
                return this.nonDeterministicIsWeaklyValidSeq(app.clone(), sequence);

            for(List<RuntimeBinding> comb : combinations){
                cloneApp = app.clone();
                for(RuntimeBinding rb : comb)
                    cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
                
                if(this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                    return true;
            }
        }

        if (op instanceof ScaleOut2){
            ScaleOut2 todo = (ScaleOut2) op;

            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }
            
            if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                return true;

            //saving the containment runtime binding that was specified explitly in the scale out 2
            RuntimeBinding containmentRB = null;
            for(RuntimeBinding rb : app.getGlobalState().getRuntimeBindings().get(todo.getIDToAssign())){
                if(rb.getReq().isContainment() == true)
                    containmentRB = rb;
            }

            //as before removing all bindings
            app.getGlobalState().removeAllBindingsBothWays(todo.getIDToAssign());
            //we put again the "old" containment binding
            app.getGlobalState().addBinding(todo.getIDToAssign(), containmentRB.getReq(), todo.getContainerID());

            combinations = this.createBindingCombinations(app, todo.getIDToAssign());
            if(combinations.isEmpty() == true)
                return this.nonDeterministicIsValidSeq(app.clone(), sequence);

            for(List<RuntimeBinding> comb : combinations){
                cloneApp = app.clone();
                for(RuntimeBinding rb : comb)
                    cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
                
                if(this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                    return true;
            }
        }
        return false;
    }


    private boolean wellFormattedSequence(List<ExecutableElement> sequence){
        boolean res = true;
        
        for (ExecutableElement element : sequence) { 
            if (element.wellFormattedSequenceElement() == false)
                res = false;
        }

        return res;
    }
    
    public List<List<RuntimeBinding>> createBindingCombinations(Application app, String instanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        List<List<RuntimeBinding>> combinations = new ArrayList<>();
        NodeInstance instance = app.getGlobalState().getNodeInstanceByID(instanceID);

        Map<Requirement, List<NodeInstance>> reqToCapableInstance = new HashMap<>();

        for(Requirement r : instance.getNeededReqs())
            reqToCapableInstance.put(r, app.getGlobalState().getCapableInstances(instanceID, r));
        
        List<Requirement> neededReqs = instance.getNeededReqs();

        Requirement containmentReq = null;
        for(Requirement r : neededReqs){
            if(r.isContainment() == true)
                containmentReq = r;
        }

        //TODO rischiosa con i ref?
        if(containmentReq != null)
            neededReqs.remove(containmentReq);

        int reqsStart = 0; 
        int reqsEnd = neededReqs.size() - 1; 
        
        if(reqsEnd <= 0)
            return combinations;

        this.recursiveCombinations(
            reqsStart, 
            reqsEnd, 
            neededReqs, 
            reqToCapableInstance, 
            combinations, 
            new ArrayList<RuntimeBinding>()
        );
        
        return combinations;
    }

    public void recursiveCombinations(
        int reqsStart, 
        int reqsEnd, 
        List<Requirement> neededReqs, 
        Map<Requirement, List<NodeInstance>> reqToCapableInstance, 
        List<List<RuntimeBinding>> combinations, 
        List<RuntimeBinding> currentCombination)
    {
        Requirement req = neededReqs.get(reqsStart);
        ArrayList<NodeInstance> capableInstance = (ArrayList<NodeInstance>) reqToCapableInstance.get(req);
        
        List<RuntimeBinding> combination = null;

        if(reqsEnd == reqsStart){
            for(NodeInstance i : capableInstance){
                RuntimeBinding newRunBinding = new RuntimeBinding(req, i.getID());

                combination = this.cloneList(currentCombination);
                combination.add(newRunBinding);
                combinations.add(combination);
            }
            return; 
        }else{
            for(NodeInstance i : capableInstance){
                combination = this.cloneList(currentCombination);
                combination.add(new RuntimeBinding(req, i.getID()));
                this.recursiveCombinations(reqsStart + 1, reqsEnd, neededReqs, reqToCapableInstance, combinations, combination);
            }
        }   
        return;
    }

    private List<RuntimeBinding> cloneList(List<RuntimeBinding> currentCombination){
        List<RuntimeBinding> ret = new ArrayList<>();

        for (RuntimeBinding runtimeBinding : currentCombination) 
            ret.add(new RuntimeBinding(runtimeBinding.getReq(), runtimeBinding.getNodeInstanceID()));
        
        return ret;
    }

    private boolean nonDeterministicIsValidSeq(Application app, List<ExecutableElement> sequence)
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

        List<List<RuntimeBinding>> combinations = null;
        Application cloneApp = null;

        //needed to times because if there is a fault the control go back to the try catch, which is later than the first control
        if(sequence.isEmpty() == true)
            return true;
        
        ExecutableElement op = sequence.remove(0);

        if (op instanceof OpStart) {
            OpStart todo = (OpStart) op;
            NodeInstance instance = app.getGlobalState().getNodeInstanceByID(todo.getInstnaceID());

            List<Requirement> neededReqsBeforeOp = instance.getNeededReqs();
            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }

            if(this.checkFaultsValid(app, sequence, false) == false)
                return false;

            List<Requirement> neededReqsAfterOp = instance.getNeededReqs();
            List<Requirement> neededReqsBeforeAndAfter = new ArrayList<>();

            for (Requirement req : neededReqsBeforeOp) {
                if (neededReqsAfterOp.contains(req))
                    neededReqsBeforeAndAfter.add(req);
            }

            List<RuntimeBinding> instanceRuntimeBinding = app.getGlobalState().getRuntimeBindings().get(instance.getID());
            List<RuntimeBinding> runtimeBindingBeforeAndAfter = new ArrayList<>();

            for (RuntimeBinding rb : instanceRuntimeBinding) {
                for (Requirement req : neededReqsBeforeAndAfter) {
                    if (req.equals(rb.getReq()))
                        runtimeBindingBeforeAndAfter.add(rb);
                }
            }

            combinations = this.createBindingCombinations(app, todo.getInstnaceID());

            if(combinations.isEmpty() == true)
                return this.nonDeterministicIsValidSeq(app.clone(), sequence);

            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runtimeBindingBeforeAndAfter) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if (this.nonDeterministicIsValidSeq(cloneApp, sequence) == false)
                        return false;
                }
            }
        }

        if (op instanceof OpEnd) {
            OpEnd todo = (OpEnd) op;
            NodeInstance instance = app.getGlobalState().getActiveNodeInstances().get(todo.getInstanceID());

            List<Requirement> neededReqsBeforeOp = instance.getNeededReqs();
            
            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }

            if(this.checkFaultsValid(app, sequence, false) == false)
                return false;

            List<Requirement> neededReqsAfterOp = instance.getNeededReqs();
            List<Requirement> neededReqsBeforeAndAfter = new ArrayList<>();

            for (Requirement req : neededReqsBeforeOp) {
                if (neededReqsAfterOp.contains(req))
                    neededReqsBeforeAndAfter.add(req);
            }

            List<RuntimeBinding> instanceRuntimeBinding = app.getGlobalState().getRuntimeBindings().get(instance.getID());
            List<RuntimeBinding> runtimeBindingBeforeAndAfter = new ArrayList<>();

            for (RuntimeBinding rb : instanceRuntimeBinding) {
                for (Requirement req : neededReqsBeforeAndAfter) {
                    if (req.equals(rb.getReq()))
                        runtimeBindingBeforeAndAfter.add(rb);
                }
            }

            combinations = this.createBindingCombinations(app, todo.getInstanceID());

            if(combinations.isEmpty() == true)
                return this.nonDeterministicIsValidSeq(app.clone(), sequence);

            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runtimeBindingBeforeAndAfter) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if (this.nonDeterministicIsValidSeq(cloneApp, sequence) == false)
                        return false;
                }
            }
        }

        if (op instanceof ScaleIn) {
            ScaleIn todo = (ScaleIn) op;

            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }

            if(this.checkFaultsValid(app, sequence, false) == false)
                return false;

            cloneApp = app.clone();
                
            if(this.nonDeterministicIsValidSeq(cloneApp, sequence) == false)
                return false;
            
        }

        if (op instanceof ScaleOut1) {
            ScaleOut1 todo = (ScaleOut1) op;

            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }

            if(this.checkFaultsValid(app, sequence, false) == false)
                return false;
            
            app.getGlobalState().removeAllBindingsBothWays(todo.getIDToAssign());
            combinations = this.createBindingCombinations(app, todo.getIDToAssign());

            if(combinations.isEmpty() == true)
                return this.nonDeterministicIsValidSeq(app.clone(), sequence);

            for(List<RuntimeBinding> comb : combinations){
                cloneApp = app.clone();
                for(RuntimeBinding rb : comb)
                    cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
                
                if(this.nonDeterministicIsValidSeq(cloneApp, sequence) == false)
                    return false;
            }
        }

        if (op instanceof ScaleOut2){
            ScaleOut2 todo = (ScaleOut2) op;

            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                return false;
            }
            
            if(this.checkFaultsValid(app, sequence, false) == false)
                return false;

            RuntimeBinding containmentRB = null;
            for(RuntimeBinding rb : app.getGlobalState().getRuntimeBindings().get(todo.getIDToAssign())){
                if(rb.getReq().isContainment() == true)
                    containmentRB = rb;
            }

            app.getGlobalState().removeAllBindingsBothWays(todo.getIDToAssign());
            app.getGlobalState().addBinding(todo.getIDToAssign(), containmentRB.getReq(), todo.getContainerID());
            combinations = this.createBindingCombinations(app, todo.getIDToAssign());

            if(combinations.isEmpty() == true)
                return this.nonDeterministicIsValidSeq(app.clone(), sequence);

            for(List<RuntimeBinding> comb : combinations){
                cloneApp = app.clone();
                for(RuntimeBinding rb : comb)
                    cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
                
                if(this.nonDeterministicIsValidSeq(cloneApp, sequence) == false)
                    return false;
            }
        }

        return true;
    }

    private boolean checkFaultsValid(Application app, List<ExecutableElement> sequence, boolean isDeterministic)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {

        if(isDeterministic == false){
            ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
            ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();
    
            Application cloneApp = null;
    
            if (brokenInstances.isEmpty() == false) {
                cloneApp = app.clone();
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                    if (this.nonDeterministicIsValidSeq(cloneApp, sequence) == false)
                        return false;
    
                } catch (RuleNotApplicableException | InstanceUnknownException e) {
                    return false;
                }
                cloneApp = app.clone(); // clone this to reset the clone well
            }
    
            cloneApp = app.clone();
    
            if (this.nonDeterministicIsValidSeq(cloneApp, sequence) == false)
                return false;
    
            if (pendingFaults.isEmpty() == false) {
                // for each fault check if it is pending or resolvable
                for (Fault f : pendingFaults) {
                    cloneApp = app.clone();
    
                    if (app.getGlobalState().isResolvableFault(f) == true) {
                        try {
                            cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.nonDeterministicIsValidSeq(cloneApp, sequence) == false)
                                return false;
                        } catch (RuleNotApplicableException | InstanceUnknownException e) {
                            return false;
                        }
                    } else {
                        try {
                            cloneApp.fault(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.nonDeterministicIsValidSeq(cloneApp, sequence) == false)
                                return false;
    
                        } catch (FailedFaultHandlingExecption | RuleNotApplicableException | InstanceUnknownException e) {
                            return false;
                        } 
                    }
                }
            }
            return true;
        }else{
            ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
            ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

            Application cloneApp = null;

            if (brokenInstances.isEmpty() == false) {
                cloneApp = app.clone();
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                    if (this.deterministicIsValidSequence(cloneApp, sequence) == false)
                        return false;

                } catch (RuleNotApplicableException | InstanceUnknownException e) {
                    return false;
                }
                cloneApp = app.clone(); // clone this to reset the clone well
            }

            cloneApp = app.clone();

            if (this.deterministicIsValidSequence(cloneApp, sequence) == false)
                return false;

            if (pendingFaults.isEmpty() == false) {
                // for each fault check if it is pending or resolvable
                for (Fault f : pendingFaults) {
                    cloneApp = app.clone();

                    if (app.getGlobalState().isResolvableFault(f) == true) {
                        try {
                            cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.deterministicIsValidSequence(cloneApp, sequence) == false)
                                return false;
                        } catch (RuleNotApplicableException | InstanceUnknownException e) {
                            return false;
                        }
                    } else {
                        try {
                            cloneApp.fault(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.deterministicIsValidSequence(cloneApp, sequence) == false)
                                return false;

                        } catch (FailedFaultHandlingExecption | RuleNotApplicableException | InstanceUnknownException e) {
                            return false;
                        } 
                    }
                }
            }
            return true;
        }

    }

    private boolean checkFaultsWeaklyValid(Application app, List<ExecutableElement> sequence, boolean isDeterministic)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        if(isDeterministic == false){
            ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
            ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();
    
            Application cloneApp = null;
    
            if (brokenInstances.isEmpty() == false) {
                cloneApp = app.clone();
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                    if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                        return true;
    
                } catch (RuleNotApplicableException | InstanceUnknownException e) {
                    return false;
                }
                cloneApp = app.clone(); // clone this to reset the clone well
            }
    
            cloneApp = app.clone();
    
            if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                return true;
    
            if (pendingFaults.isEmpty() == false) {
                // for each fault check if it is pending or resolvable
                for (Fault f : pendingFaults) {
                    cloneApp = app.clone();
    
                    if (app.getGlobalState().isResolvableFault(f) == true) {
                        try {
                            cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                                return true;
                        } catch (RuleNotApplicableException | InstanceUnknownException e) {
                            return false;
                        }
                    } else {
                        try {
                            cloneApp.fault(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                                return true;
    
                        } catch (FailedFaultHandlingExecption | RuleNotApplicableException | InstanceUnknownException e) {
                            return false;
                        } 
                    }
                }
            }
            return false;
        }else{
            ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
            ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

            Application cloneApp = null;

            if (brokenInstances.isEmpty() == false) {
                cloneApp = app.clone();
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                    if (this.deterministicIsWeaklyValidSequence(cloneApp, sequence) == true)
                        return true;

                } catch (RuleNotApplicableException | InstanceUnknownException e) {
                    return false;
                }
                cloneApp = app.clone(); // clone this to reset the clone well
            }

            cloneApp = app.clone();

            if (this.deterministicIsWeaklyValidSequence(cloneApp, sequence) == true)
                return true;

            if (pendingFaults.isEmpty() == false) {
                // for each fault check if it is pending or resolvable
                for (Fault f : pendingFaults) {
                    cloneApp = app.clone();

                    if (app.getGlobalState().isResolvableFault(f) == true) {
                        try {
                            cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.deterministicIsWeaklyValidSequence(cloneApp, sequence) == true)
                                return true;
                        } catch (RuleNotApplicableException | InstanceUnknownException e) {
                            return false;
                        }
                    } else {
                        try {
                            cloneApp.fault(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.deterministicIsWeaklyValidSequence(cloneApp, sequence) == true)
                                return true;

                        } catch (FailedFaultHandlingExecption | RuleNotApplicableException | InstanceUnknownException e) {
                            return false;
                        } 
                    }
                }
            }
            return false;
        }
    }
  

    public boolean validPlan(Application app, List<List<ExecutableElement>> plan, List<Constraint> constraints)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            IllegalSequenceElementException,
            InstanceUnknownException 
    {
        List<ExecutableElement> planExecutableElements = new ArrayList<>();
        List<ExecutableElement> constraintsExecutableElements = new ArrayList<>();

        List<ExecutableElement> freeExecutableElements = new ArrayList<>();

        for (List<ExecutableElement> list : plan) {
            for (ExecutableElement element : list) {
                planExecutableElements.add(element);
            }
        }

        for(Constraint constraint : constraints){
            constraintsExecutableElements.add(constraint.getBefore());
            constraintsExecutableElements.add(constraint.getAfter());
        }

        for(ExecutableElement element : planExecutableElements){
            //TODO: ridefinisci equals e hashmap di executableelements
            //forse non serve, usa quelli di object
            if(constraintsExecutableElements.contains(element) == false)
                freeExecutableElements.add(element);
        }

        List<List<Constraint>> permutationsOfConstraints = this.generatePerm(constraints);
        List<List<ExecutableElement>> sequences = new ArrayList<>();

        for(List<Constraint> perm : permutationsOfConstraints){
            List<ExecutableElement> baseSequence = new ArrayList<>();

            for(Constraint constraint : perm){
                baseSequence.add(constraint.getBefore());
                baseSequence.add(constraint.getAfter());
            }

            this.generateSequences(sequences, baseSequence, freeExecutableElements);
        }

        for(List<ExecutableElement> sequence : sequences){
            if(this.isValidSequence(app, sequence) == false)
                return false;
        }

        return true;

    }

    public void generateSequences(List<List<ExecutableElement>> sequences, List<ExecutableElement> baseSequence, List<ExecutableElement> freeExecutableElement){
        
        int currentMixing = 0;

        List<ExecutableElement> currentSequence = new ArrayList<>(baseSequence);

        if(freeExecutableElement.size() == 1){
            List<ExecutableElement> currentClone = this.cloneExElementList(currentSequence);

            for(int j = 0; j < currentClone.size() + 1; j ++){
                List<ExecutableElement> finalClone = this.cloneExElementList(currentClone);
                finalClone.add(j, freeExecutableElement.get(currentMixing));
                sequences.add(finalClone);
            }

            return;
        }

        while(currentMixing != freeExecutableElement.size()){

            List<ExecutableElement> currentClone = this.cloneExElementList(currentSequence);

            for(int i = 0; i < freeExecutableElement.size(); i++){
                if(i != currentMixing)
                    currentClone.add(freeExecutableElement.get(i));
            }

            for(int j = 0; j < currentClone.size() + 1; j ++){
                List<ExecutableElement> finalClone = this.cloneExElementList(currentClone);
                finalClone.add(j, freeExecutableElement.get(currentMixing));
                sequences.add(finalClone);
            }

            currentMixing ++;
        }
        return;
    }

    public <E> List<List<E>> generatePerm(List<E> original) {
        if (original.isEmpty()) {
          List<List<E>> result = new ArrayList<>(); 
          result.add(new ArrayList<>()); 
          return result; 
        }

        E firstElement = original.remove(0);
        List<List<E>> returnValue = new ArrayList<>();
        List<List<E>> permutations = generatePerm(original);

        for (List<E> smallerPermutated : permutations) {
          for (int index=0; index <= smallerPermutated.size(); index++) {
            List<E> temp = new ArrayList<>(smallerPermutated);
            temp.add(index, firstElement);
            returnValue.add(temp);
          }
        }
        return returnValue;
      }


    private List<ExecutableElement> cloneExElementList(List<ExecutableElement> list){
        List<ExecutableElement> clone = new ArrayList<>();

        for(ExecutableElement element : list)
            clone.add(element);
        
        return clone;
    }


}