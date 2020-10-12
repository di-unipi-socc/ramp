package analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean isValidSequence(Application app, List<ExecutableElement> sequence)
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

        ExecutableElement seqElement = sequence.remove(0);
        try {
            app.execute(seqElement);
        } catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (Exception e) {
            return false;
        }

        if(this.faultBiforcationValid(app, sequence) == false)
            return false;

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
        } catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (Exception e) {
            return false;
        }

        if(this.faultBiforcationWeaklyValid(app, sequence) == true)
            return true;
        
        return false;
    }

    public boolean isNotValidSequence(Application app, List<ExecutableElement> sequence)
            throws NullPointerException, IllegalSequenceElementException, InstanceUnknownException {
        return !this.isWeaklyValidSequence(app, sequence);
    }

    
    public boolean nonDetIsWeaklyValid(Application app, List<ExecutableElement> sequence)
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

            if(this.faultBiforcationNotDetWeaklyValid(app, sequence) == true)
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

            combinations = this.createBindingCombinations(app, todo.getInstnaceID());

            if(combinations.isEmpty() == true)
                return this.nonDetIsWeaklyValid(app.clone(), sequence);

            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runtimeBindingBeforeAndAfter) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if (this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
                        return true;
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

            if(this.faultBiforcationNotDetWeaklyValid(app, sequence) == true)
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
                return this.notDetIsValid(app.clone(), sequence);

            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runtimeBindingBeforeAndAfter) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if (this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
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

            if(this.faultBiforcationNotDetWeaklyValid(app, sequence) == true)
                return true;

            cloneApp = app.clone();
                
            if(this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
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

            if(this.faultBiforcationNotDetWeaklyValid(app, sequence) == true)
                return true;
            
            app.getGlobalState().removeAllBindingsBothWays(todo.getIDToAssign());
            combinations = this.createBindingCombinations(app, todo.getIDToAssign());

            if(combinations.isEmpty() == true)
                return this.nonDetIsWeaklyValid(app.clone(), sequence);

            for(List<RuntimeBinding> comb : combinations){
                cloneApp = app.clone();
                for(RuntimeBinding rb : comb)
                    cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
                
                if(this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
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
            
            if(this.faultBiforcationNotDetWeaklyValid(app, sequence) == true)
                return true;

            RuntimeBinding containmentRB = null;
            for(RuntimeBinding rb : app.getGlobalState().getRuntimeBindings().get(todo.getIDToAssign())){
                if(rb.getReq().isContainment() == true)
                    containmentRB = rb;
            }

            app.getGlobalState().removeAllBindingsBothWays(todo.getIDToAssign());
            app.getGlobalState().addBinding(todo.getIDToAssign(), containmentRB.getReq(), todo.getContainerID());
            combinations = this.createBindingCombinations(app, todo.getIDToAssign());

            if(combinations.isEmpty() == true)
                return this.notDetIsValid(app.clone(), sequence);

            for(List<RuntimeBinding> comb : combinations){
                cloneApp = app.clone();
                for(RuntimeBinding rb : comb)
                    cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
                
                if(this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
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

    public boolean notDetIsValid(Application app, List<ExecutableElement> sequence)
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

            if(this.faultBiforcationNotDetValid(app, sequence) == false)
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
                return this.notDetIsValid(app.clone(), sequence);

            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runtimeBindingBeforeAndAfter) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if (this.notDetIsValid(cloneApp, sequence) == false)
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

            if(this.faultBiforcationNotDetValid(app, sequence) == false)
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
                return this.notDetIsValid(app.clone(), sequence);

            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runtimeBindingBeforeAndAfter) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if (this.notDetIsValid(cloneApp, sequence) == false)
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

            if(this.faultBiforcationNotDetValid(app, sequence) == false)
                return false;

            cloneApp = app.clone();
                
            if(this.notDetIsValid(cloneApp, sequence) == false)
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

            if(this.faultBiforcationNotDetValid(app, sequence) == false)
                return false;
            
            app.getGlobalState().removeAllBindingsBothWays(todo.getIDToAssign());
            combinations = this.createBindingCombinations(app, todo.getIDToAssign());

            if(combinations.isEmpty() == true)
                return this.notDetIsValid(app.clone(), sequence);

            for(List<RuntimeBinding> comb : combinations){
                cloneApp = app.clone();
                for(RuntimeBinding rb : comb)
                    cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
                
                if(this.notDetIsValid(cloneApp, sequence) == false)
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
            
            if(this.faultBiforcationNotDetValid(app, sequence) == false)
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
                return this.notDetIsValid(app.clone(), sequence);

            for(List<RuntimeBinding> comb : combinations){
                cloneApp = app.clone();
                for(RuntimeBinding rb : comb)
                    cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
                
                if(this.notDetIsValid(cloneApp, sequence) == false)
                    return false;
            }
        }

        return true;
    }


    public boolean faultBiforcationNotDetValid(Application app, List<ExecutableElement> sequence)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        Application cloneApp = null;

        if (brokenInstances.isEmpty() == false) {
            cloneApp = app.clone();
            try {
                cloneApp.scaleIn(brokenInstances.get(0).getID());
                if (this.notDetIsValid(cloneApp, sequence) == false)
                    return false;

            } catch (RuleNotApplicableException | InstanceUnknownException e) {
                return false;
            }
            cloneApp = app.clone(); // clone this to reset the clone well
        }

        cloneApp = app.clone();

        if (this.notDetIsValid(cloneApp, sequence) == false)
            return false;

        if (pendingFaults.isEmpty() == false) {
            // for each fault check if it is pending or resolvable
            for (Fault f : pendingFaults) {
                cloneApp = app.clone();

                if (app.getGlobalState().isResolvableFault(f) == true) {
                    try {
                        cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
                        if (this.notDetIsValid(cloneApp, sequence) == false)
                            return false;
                    } catch (RuleNotApplicableException | InstanceUnknownException e) {
                        return false;
                    }
                } else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
                        if (this.notDetIsValid(cloneApp, sequence) == false)
                            return false;

                    } catch (FailedFaultHandlingExecption | RuleNotApplicableException | InstanceUnknownException e) {
                        return false;
                    } 
                }
            }
        }
        return true;
    }

    public boolean faultBiforcationWeaklyValid(Application app, List<ExecutableElement> sequence)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException,
            IllegalSequenceElementException 
    {
        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        Application cloneApp = null;

        if (brokenInstances.isEmpty() == false) {
            cloneApp = app.clone();
            try {
                cloneApp.scaleIn(brokenInstances.get(0).getID());
                if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                    return true;

            } catch (RuleNotApplicableException | InstanceUnknownException e) {
                return false;
            }
            cloneApp = app.clone(); // clone this to reset the clone well
        }

        cloneApp = app.clone();

        if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
            return true;

        if (pendingFaults.isEmpty() == false) {
            // for each fault check if it is pending or resolvable
            for (Fault f : pendingFaults) {
                cloneApp = app.clone();

                if (app.getGlobalState().isResolvableFault(f) == true) {
                    try {
                        cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
                        if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                            return true;
                    } catch (RuleNotApplicableException | InstanceUnknownException e) {
                        return false;
                    }
                } else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
                        if (this.isWeaklyValidSequence(cloneApp, sequence) == true)
                            return true;

                    } catch (FailedFaultHandlingExecption | RuleNotApplicableException | InstanceUnknownException e) {
                        return false;
                    } 
                }
            }
        }
        return false;
    }

    public boolean faultBiforcationValid(Application app, List<ExecutableElement> sequence) 
        throws 
            NullPointerException,
            IllegalArgumentException, 
            InstanceUnknownException, 
            IllegalSequenceElementException 
    {
        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        Application cloneApp = null;

        if (brokenInstances.isEmpty() == false) {
            cloneApp = app.clone();
            try {
                cloneApp.scaleIn(brokenInstances.get(0).getID());
                if (this.isValidSequence(cloneApp, sequence) == false)
                    return false;

            } catch (RuleNotApplicableException | InstanceUnknownException e) {
                return false;
            }
            cloneApp = app.clone(); // clone this to reset the clone well
        }

        cloneApp = app.clone();

        if (this.isValidSequence(cloneApp, sequence) == false)
            return false;

        if (pendingFaults.isEmpty() == false) {
            // for each fault check if it is pending or resolvable
            for (Fault f : pendingFaults) {
                cloneApp = app.clone();

                if (app.getGlobalState().isResolvableFault(f) == true) {
                    try {
                        cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
                        if (this.isValidSequence(cloneApp, sequence) == false)
                            return false;
                    } catch (RuleNotApplicableException | InstanceUnknownException e) {
                        return false;
                    }
                } else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
                        if (this.isValidSequence(cloneApp, sequence) == false)
                            return false;

                    } catch (FailedFaultHandlingExecption | RuleNotApplicableException | InstanceUnknownException e) {
                        return false;
                    } 
                }
            }
        }
        return true;
    }

    public boolean faultBiforcationNotDetWeaklyValid(Application app, List<ExecutableElement> sequence)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {
        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        Application cloneApp = null;

        if (brokenInstances.isEmpty() == false) {
            cloneApp = app.clone();
            try {
                cloneApp.scaleIn(brokenInstances.get(0).getID());
                if (this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
                    return true;

            } catch (RuleNotApplicableException | InstanceUnknownException e) {
                return false;
            }
            cloneApp = app.clone(); // clone this to reset the clone well
        }

        cloneApp = app.clone();

        if (this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
            return true;

        if (pendingFaults.isEmpty() == false) {
            // for each fault check if it is pending or resolvable
            for (Fault f : pendingFaults) {
                cloneApp = app.clone();

                if (app.getGlobalState().isResolvableFault(f) == true) {
                    try {
                        cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
                        if (this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
                            return true;
                    } catch (RuleNotApplicableException | InstanceUnknownException e) {
                        return false;
                    }
                } else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
                        if (this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
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