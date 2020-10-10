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

        if (brokenInstances.isEmpty() == false) {
            cloneApp = app.clone();
            try {
                cloneApp.scaleIn(brokenInstances.get(0).getID());
                if (this.isValidSequence(cloneApp, sequence) == false)
                    return false;

            } catch (RuleNotApplicableException E) {
                return false;
            }
        }
        // cloneApp was used if brokenInstances is not empty
        cloneApp = app.clone();

        // branch where the faults are not handled whatsoever
        if (this.isValidSequence(cloneApp, sequence) == false)
            return false;

        // branch where the faults are hanled by their type
        if (pendingFaults.isEmpty() == false) {
            // for each fault check if it is pending or resolvable
            for (Fault f : pendingFaults) {
                cloneApp = app.clone();

                if (cloneApp.getGlobalState().isResolvableFault(f) == true) {
                    try {
                        cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
                        if (this.isValidSequence(cloneApp, sequence) == false)
                            return false;

                    } catch (RuleNotApplicableException e) {
                        return false;
                    } catch (InstanceUnknownException e) {
                        return false;
                    }
                } else {
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
            throws IllegalSequenceElementException, NullPointerException, InstanceUnknownException {

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

        if (brokenInstances.isEmpty() == false) {
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
                    } catch (RuleNotApplicableException e) {
                        return false;
                    }

                } else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
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

    public boolean nonDetIsWeaklyValid(Application app, List<ExecutableElement> sequence)
            throws IllegalSequenceElementException, NullPointerException, InstanceUnknownException {
        if (app == null)
            throw new NullPointerException();

        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        if (sequence.isEmpty() == true)
            return true;

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        Application cloneApp;

        if (brokenInstances.isEmpty() == false) {
            cloneApp = app.clone();
            try {
                cloneApp.scaleIn(brokenInstances.get(0).getID());
                if (this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
                    return true;

            } catch (RuleNotApplicableException E) {
                return false;
            }

            cloneApp = app.clone(); // clone this to reset the clone well
        }

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
                    } catch (RuleNotApplicableException e) {
                        return false;
                    }

                } else {
                    try {
                        cloneApp.fault(f.getInstanceID(), f.getReq());
                        // if the fault is resolved keep exploring the branch
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

        List<List<RuntimeBinding>> combinations = null;
        ExecutableElement op = sequence.remove(0);

        if (op instanceof OpStart) {
            OpStart todo = (OpStart) op;
            NodeInstance instance = app.getGlobalState().getActiveNodeInstances().get(todo.getInstnaceID());

            List<Requirement> neededReqsBeforeOp = instance.getNeededReqs();
            try {
                // TODO alcune eccezioni sono date da execute ma qui non dovrebbero essere
                // catturate, fixare
                app.execute(todo);
            } catch (FailedOperationException | RuleNotApplicableException | OperationNotAvailableException
                    | AlreadyUsedIDException e) {
                return false;
            }

            List<Requirement> neededReqsAfterOp = instance.getNeededReqs();
            List<Requirement> neededReqsBeforeAndAfter = new ArrayList<>();

            for (Requirement req : neededReqsBeforeOp) {
                if (neededReqsAfterOp.contains(req))
                    neededReqsBeforeAndAfter.add(req);
            }

            List<RuntimeBinding> instanceRuntimeBinding = app.getGlobalState().getRuntimeBindings()
                    .get(instance.getID());
            List<RuntimeBinding> runtimeBindingBeforeAndAfter = new ArrayList<>();

            for (RuntimeBinding rb : instanceRuntimeBinding) {
                for (Requirement req : neededReqsBeforeAndAfter) {
                    if (req.equals(rb.getReq()))
                        runtimeBindingBeforeAndAfter.add(rb);
                }
            }

            combinations = this.createBindingCombinations(app, todo.getInstnaceID());

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
                // TODO alcune eccezioni sono date da execute ma qui non dovrebbero essere
                // catturate, fixare
                app.execute(todo);
            } catch (FailedOperationException | RuleNotApplicableException | OperationNotAvailableException
                    | AlreadyUsedIDException e) {
                return false;
            }

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

            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runtimeBindingBeforeAndAfter) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) {
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    }
                    if (this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
                        return true;
                }
            }
        }

        if (op instanceof ScaleIn) {
            ScaleIn todo = (ScaleIn) op;

            try {
                app.execute(todo);
            } catch (IllegalArgumentException | FailedOperationException | RuleNotApplicableException
                    | OperationNotAvailableException | AlreadyUsedIDException e) {
                return false;
            }

            cloneApp = app.clone();
            if(this.nonDetIsWeaklyValid(cloneApp, sequence) == true)
                return true;
            
        }

        if (op instanceof ScaleOut1) {
            ScaleOut1 todo = (ScaleOut1) op;

            try {
                app.execute(todo);
            } catch (IllegalArgumentException | FailedOperationException | RuleNotApplicableException
                    | OperationNotAvailableException | AlreadyUsedIDException e) {
                return false;
            }
            
            app.getGlobalState().removeAllBindingsBothWays(todo.getIDToAssign());
            combinations = this.createBindingCombinations(app, todo.getIDToAssign());

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
            } catch (IllegalArgumentException | FailedOperationException | RuleNotApplicableException
                    | OperationNotAvailableException | AlreadyUsedIDException e) {
                return false;
            }
            
            
            RuntimeBinding containmentRB = null;
            for(RuntimeBinding rb : app.getGlobalState().getRuntimeBindings().get(todo.getIDToAssign())){
                if(rb.getReq().isContainment() == true)
                    containmentRB = rb;
            }

            app.getGlobalState().removeAllBindingsBothWays(todo.getIDToAssign());
            app.getGlobalState().addBinding(todo.getIDToAssign(), containmentRB.getReq(), todo.getContainerID());
            combinations = this.createBindingCombinations(app, todo.getIDToAssign());

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
        NodeInstance instance = app.getGlobalState().getNodeInstanceByID(instanceID);

        Map<Requirement, List<NodeInstance>> reqToCapableInstance = new HashMap<>();

        for(Requirement r : instance.getNeededReqs())
            reqToCapableInstance.put(r, app.getGlobalState().getCapableInstances(instanceID, r));
        
        int reqsStart = 0; 
        List<Requirement> neededReqs = instance.getNeededReqs();

        Requirement containmentReq = null;
        for(Requirement r : neededReqs){
            if(r.isContainment() == true)
                containmentReq = r;
        }
        
        //TODO rischiosa con i ref?
        if(containmentReq != null)
            neededReqs.remove(containmentReq);

        int reqsEnd = instance.getNeededReqs().size() - 1; 
        
        List<List<RuntimeBinding>> combinations = new ArrayList<>();
        List<RuntimeBinding> currentCombination = new ArrayList<>();  

        this.recursiveCombinations(
            reqsStart, 
            reqsEnd, 
            neededReqs, 
            reqToCapableInstance, 
            combinations, 
            currentCombination
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

}