package mprot.core.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mprot.core.analyzer.execptions.*;
import mprot.core.analyzer.executable_element.*;
import mprot.core.model.*;
import mprot.core.model.exceptions.*;

public class Analyzer {

    //app's name -> fail report
    private Map<String, AnalysisReport> fails;

    public Map<String, AnalysisReport> getAnalysisReport() {
        return this.fails;
    }

    public Analyzer() throws NullPointerException {
        this.fails = new HashMap<>();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ MUTLIPURPOSE UTILITIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @param list
     * @return new list with the same object refs as list
     */
    private <E> List<E> cloneList(List<E> list){
        List<E> clonedList = new ArrayList<>();

        for(E element : list)
            clonedList.add(element);

        return clonedList;
    }   

    /**
     * @param sequence
     * @return true if all elements of the list have the right fields valorized
     */
    private boolean wellFormattedSequence(List<ExecutableElement> sequence){
        boolean res = true;
        
        for (ExecutableElement element : sequence) { 
            if (element.wellFormattedSequenceElement() == false)
                res = false;
        }

        return res;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ANALYZER UTILITIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void setFailReport(Application app, ExecutableElement element){
        AnalysisReport fail = this.fails.get(app.getName());
        if(fail.getFailedElement() == null)
            fail.setFailedElement(element);
    }

    /**
     * @param app
     * @param instanceID of the instance of which the runtimebinding combination is needed
     * @return all the possible combinations of the runtime binding that instanceID could have
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public List<List<RuntimeBinding>> createRunBindingCombs(Application app, String instanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {

        List<List<RuntimeBinding>> combinations = new ArrayList<>();

        NodeInstance instance = app.getGlobalState().getNodeInstanceByID(instanceID);

        // map: requiremet -> list of instance that can satisfy that req
        Map<Requirement, List<NodeInstance>> reqToCapableInstance = new HashMap<>();
        for (Requirement r : instance.getNeededReqs())
            reqToCapableInstance.put(r, app.getGlobalState().getCapableInstances(instanceID, r));

        // cloning the neededReqs of instance to avoid the removal of the containment req
        List<Requirement> neededReqs = new ArrayList<>();
        for (Requirement r : instance.getNeededReqs())
            neededReqs.add(r);

        Requirement containmentReq = null;
        for (Requirement r : neededReqs) {
            if (r.isContainment() == true)
                containmentReq = r;
        }

        // the runtime binding of the containment requirement is not touched since it is unique and "final"
        if (containmentReq != null)
            neededReqs.remove(containmentReq);

        int reqsStart = 0;
        int reqsEnd = neededReqs.size() - 1;

        if (reqsEnd <= 0)
            return combinations;

        this.recursiveGeneration(
            reqsStart, 
            reqsEnd, 
            neededReqs, 
            reqToCapableInstance, 
            combinations,
            new ArrayList<RuntimeBinding>()
        );

        return combinations;
    }

    /**
     * @param reqsStart index of the requirement now in analysis
     * @param reqsEnd index of the last requirement needed by the instance
     * @param neededReqs all the needed reqs (minus the containment) of instance
     * @param reqToCapableInstance map: req -> cable instances
     * @param combinations all the possible runtime binding combinations
     * @param currentCombination current combination of runtime bindings
     */
    private void recursiveGeneration(
        int reqsStart, int reqsEnd, 
        List<Requirement> neededReqs,
        Map<Requirement, List<NodeInstance>> reqToCapableInstance, 
        List<List<RuntimeBinding>> combinations,
        List<RuntimeBinding> currentCombination
    ) {

        /**
         * for each requirement 
         *  - if reqsStart is not the last req needed (reqsEnd != reqsStart) add to the current 
         *    combination the runtime binding composed by <req, fisrt capable instance>
         * 
         * - if reqsStart points to the last req needed
         *      - for each capable instance i
         *          - add <req, i> to currentCombination
         *          - add currentComb to combinations
         */

        Requirement req = neededReqs.get(reqsStart);
        ArrayList<NodeInstance> capableInstance = (ArrayList<NodeInstance>) reqToCapableInstance.get(req);

        List<RuntimeBinding> combination = null;

        if (reqsEnd == reqsStart) {
            for (NodeInstance i : capableInstance) {
                RuntimeBinding newRunBinding = new RuntimeBinding(req, i.getID());

                combination = this.cloneList(currentCombination);
                combination.add(newRunBinding);
                combinations.add(combination);
            }
            return;

        } else {
            for (NodeInstance i : capableInstance) {
                combination = this.cloneList(currentCombination);
                combination.add(new RuntimeBinding(req, i.getID()));

                this.recursiveGeneration(
                    reqsStart + 1, 
                    reqsEnd, neededReqs, 
                    reqToCapableInstance, 
                    combinations,
                    combination
                );
            }
        }
        return;
    }

    /**
     * @param planExecutableElements executable elements of the plan
     * @param constraints constraints that the plan must respect
     * @return map such as executable element x -> [x1, x2, ...] that must be executed after x
     */
    public Map<ExecutableElement, List<ExecutableElement>> buildConstraintMap(
        List<ExecutableElement> planExecutableElements, 
        List<Constraint> constraints
    ){
        Map<ExecutableElement, List<ExecutableElement>> constraintsMap = new HashMap<>();

        for(ExecutableElement elem : planExecutableElements)
            constraintsMap.put(elem, new ArrayList<ExecutableElement>());

        for(Constraint constraint : constraints)
            constraintsMap.get(constraint.getBefore()).add(constraint.getAfter());

        return constraintsMap;
    }

    /**
     * @param sequence sequence of ex elements that must be checked
     * @param constraintsMap constraints that have to be respeceted by sequence
     * @return true if the sequence do not violate the constraints
     */
    public boolean checkConstraints(List<ExecutableElement> sequence, Map<ExecutableElement, List<ExecutableElement>> constraintsMap){
        
        /**
         * for each element x of sequence
         *    - take the elements in sequence that must be executed after x and put them in afterElements
         *      - take the portion of sequence starting right after x (subsequence)
         *          - if the afterElements are inside the subsequence this means that they will actually be 
         *            executer fater x in the sequence
         *          - otherwise the constriant are violated
         */

        for(ExecutableElement elem : sequence){

            List<ExecutableElement> afterElements = constraintsMap.get(elem);

            if(afterElements.isEmpty() == false){

                List<ExecutableElement> subSequence = sequence.subList(sequence.indexOf(elem) + 1, sequence.size());

                if(subSequence.isEmpty() == true)
                    return false;
                
                for(ExecutableElement afterElem : afterElements){
                    if(subSequence.contains(afterElem) == false)
                        return false;
                }
            }
        }

        return true;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ANALYSIS METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Mind that isValid* and isWeaklyValid* do pratically the same thing, the only difference is that 
     * a sequence, to be declared valid, has to be completed in every brench, so just a brench that can't perform an
     * executable element (in codes words, just a call of isValid* == false) is enough to declare the sequence non totally
     * valid (hence the if(isValid* == false) return false)
     * On the other hands, weakly valid means that at least one brench of the tree is completed (means that the sequence
     * can be executed at least one time), hence the if(isWeaklyValid* == true) return true
     * Not valid means that the sequence can't be completed no matter the branch we explore
     * 
     * Mind that deterministic or notDeterministic are given by the function PI that the application uses
     *      - pi: <instance x, requirement of x> -> instance y that can satisfy x
     * 
     * Mind that code with the same semantic will not be commented (i.e checkFaultValid, checkFaultWeaklyValid)
     * 
     * Mind that a plan is a set of executable elements that can be executed concurrently or sequentially
     * a plan is represented by a list of ex elements and a list of constraints. The ex elemenet that are not 
     * binded by any constraint can be executed at any time, the one who are ruled by the constraint must respect them
     * This basically means that a plan generates a set of sequences, this sequences respect the constraints 
     * A plan is valid if every sequences that it generates is valid, if at least one sequence is weakly valid or not-valid then it is 
     * weakly valid. A plan is not-valid if every sequence is not-valid
     */

    /**
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @return true if the sequence is valid
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

        /**
         * the analysis (such as deterministicValidSequence) are recursive methods that end their recursion 
         * when sequence is empty (at each iteration they perform a sequence.remove)
         * 
         * this means that to save the original sequence this must be saved before
         */

        List<ExecutableElement> backupSequence = this.cloneList(sequence);

        if (app.isPiDeterministic() == true){
            if(this.deterministicIsValidSequence(app.clone(), this.cloneList(sequence)) == false){
                AnalysisReport fail = fails.get(app.getName());
                fail.setSequence(backupSequence);
                return false;
            }
        }
        else{
            if(this.nonDeterministicIsValidSequence(app.clone(), this.cloneList(sequence)) == false){
                AnalysisReport fail = fails.get(app.getName());
                fail.setSequence(backupSequence);
                return false;
            }
        }

        return true;
    }

    /**
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @return true if the sequence is valid
     * @throws IllegalSequenceElementException
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
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

        // pop one element from the sequence than execute it
        ExecutableElement seqElement = sequence.remove(0);
        try {
            app.execute(seqElement);
        } catch(FailedOperationException e){
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (Exception e) {
            fails.put(app.getName(), new AnalysisReport(seqElement, e));
            return false;
        }

        /**
         * checkFault* open two brenches
         *  - keep expolring anyway (faults or not faults)
         *  - if there are faults it fixes and then keep exploring       
         */

        //mind that checkFaultsValid and checkFaultsWeaklyValid differs only for the method call they 
        //perform based of the analysis of the validity or the weakly validity
        if(this.checkFaultsValid(app, sequence, true) == false){
            this.setFailReport(app, seqElement);
            return false;
        }
          
        return true;
    }

    /**
     * 
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @return true if the sequence is valid
     * @throws IllegalSequenceElementException
     * @throws NullPointerException
     * @throws InstanceUnknownException
     */
    private boolean nonDeterministicIsValidSequence(Application app, List<ExecutableElement> sequence)
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
        
        ExecutableElement op = sequence.remove(0);

        //nonDet* are custom management protocols that creates every possible runtime binding 
        //that pi can provide and check for each one of them

        if (op instanceof OpStart) {
            if(this.nonDetOpStartOpEnd(app, op, false, sequence) == false){
                this.setFailReport(app, op);
                return false;
            }

        }

        if (op instanceof OpEnd) {
            if(this.nonDetOpStartOpEnd(app, op, false, sequence) == false){
                this.setFailReport(app, op);
                return false;
            }
            
        }
        if (op instanceof ScaleIn){
            if(this.nonDetScaleIn(app, op, false, sequence) == false){
                this.setFailReport(app, op);
                return false;
            }
        
        }
        if (op instanceof ScaleOut1) {
            if(this.nonDetScaleOut1(app, op, false, sequence) == false){
                this.setFailReport(app, op);
                return false;
            }
            
        }
        if (op instanceof ScaleOut2){
            if(this.nonDetScaleOut2(app, op, false, sequence) == false){
                this.setFailReport(app, op);
                return false;
            }
            
        }

        return true;
    }

    /**
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @param isDeterministic true if app use a deterministic PI
     * @return true if the fault brenches are valid
     * @throws NullPointerException
     * @throws IllegalSequenceElementException
     * @throws InstanceUnknownException
     */
    private boolean checkFaultsValid(Application app, List<ExecutableElement> sequence, boolean isDeterministic)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {

        if(isDeterministic == false){

            ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
            ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();
    
            Application cloneApp =  app.clone();;
            
            //application of the rule no-broken-instances: in the application will never be instances whose container 
            //was destroyed (scaleIn a container cause the scaleIn of the contained)
            if (brokenInstances.isEmpty() == false) {
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                    //explore the path where the scaleIn was performed
                    if (this.nonDeterministicIsValidSequence(cloneApp, sequence) == false)
                        return false;
    
                } catch (Exception e) {
                    //the scaleIn failed, store the reason and return false
                    return false;
                }
                cloneApp = app.clone(); // clone this to reset the clone
            }

            //no broken instances case

            //explore the branch as it is (if there are faults they are not handled whatsoever)
            if (this.nonDeterministicIsValidSequence(cloneApp, sequence) == false)
                return false;
    
            //explore the branch were the faults are handled
            if (pendingFaults.isEmpty() == false) {

                for (Fault f : pendingFaults) {
                    cloneApp = app.clone();
    
                    if (app.getGlobalState().isResolvableFault(f) == true) {
                        try {
                            cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.nonDeterministicIsValidSequence(cloneApp, sequence) == false)
                                return false;

                        } catch (Exception e) {
                            return false;
                        }
                    } else {
                        try {
                            cloneApp.fault(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch

                            if (this.nonDeterministicIsValidSequence(cloneApp, sequence) == false)
                                return false;
    
                        } catch (Exception e) {
                            return false;
                        } 
                    }
                }
            }
            return true;

        }else{ //isDeterministic == true

            ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
            ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

            Application cloneApp = app.clone();;

            if (brokenInstances.isEmpty() == false) {
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                    if (this.deterministicIsValidSequence(cloneApp, sequence) == false)
                        return false;

                } catch (RuleNotApplicableException | InstanceUnknownException e) {
                    return false;
                }
                cloneApp = app.clone(); // clone this to reset the clone well
            }

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
                        } catch (Exception e) {
                            return false;
                        }
                    } else {
                        try {
                            cloneApp.fault(f.getInstanceID(), f.getReq());
                            // if the fault is resolved keep exploring the branch
                            if (this.deterministicIsValidSequence(cloneApp, sequence) == false)
                                return false;

                        } catch (Exception e) {
                            return false;
                        } 
                    }
                }
            }
            return true;
        }

    }

    /**
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @return true if the sequence is weakly valid
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
        List<ExecutableElement> backupSequence = this.cloneList(sequence);

        if (app.isPiDeterministic() == true){
            if(this.deterministicIsWeaklyValidSequence(app.clone(), this.cloneList(sequence)) == true)
                return true;

        } else{
            if(this.nonDeterministicIsWeaklyValidSeq(app.clone(), this.cloneList(sequence)) == true)
                return true;
        }

        AnalysisReport fail = fails.get(app.getName());
        fail.setSequence(backupSequence);
        return false;
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
            fails.put(app.getName(), new AnalysisReport(seqElement, e));
            return false;
        }

        if(this.checkFaultsWeaklyValid(app, sequence, true) == true)
            return true;
        
        this.setFailReport(app, seqElement);
        return false;
    }

    /**
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @return true if the sequence is weakly valid
     * @throws IllegalSequenceElementException
     * @throws NullPointerException
     * @throws InstanceUnknownException
     */
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

        ExecutableElement op = sequence.remove(0);

        //for each kind of op there is a specific check
        if (op instanceof OpStart) {
            if(this.nonDetOpStartOpEnd(app, op, true, sequence) == true)
                return true;
        }
        if (op instanceof OpEnd) {
            if(this.nonDetOpStartOpEnd(app, op, true, sequence) == true)
                return true;
        }
        if (op instanceof ScaleIn) {
            if(this.nonDetScaleIn(app, op, true, sequence) == true)
                return true;
        }
        if (op instanceof ScaleOut1) {
            if(this.nonDetScaleOut1(app, op, true, sequence) == true)
                return true;
        }
        if (op instanceof ScaleOut2){
            if(this.nonDetScaleOut2(app, op, true, sequence) == true)
                return true;
        }
        this.setFailReport(app, op);
        return false;
    }

    /**
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @param isDeterministic true if the application use a deterministic PI
     * @return true if the fault brenches are valid
     * @throws NullPointerException
     * @throws IllegalSequenceElementException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
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
    
            Application cloneApp = app.clone();;
    
            if (brokenInstances.isEmpty() == false) {
                cloneApp = app.clone();
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                    if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                        return true;
    
                } catch (Exception e) {
                    return false;
                }
                cloneApp = app.clone(); 
            }
        
            if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                return true;
    
            if (pendingFaults.isEmpty() == false) {
                for (Fault f : pendingFaults) {
                    cloneApp = app.clone();
    
                    if (app.getGlobalState().isResolvableFault(f) == true) {
                        try {
                            cloneApp.autoreconnect(f.getInstanceID(), f.getReq());
                            if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                                return true;
                        } catch (Exception e) {
                            return false;
                        }
                    } else {
                        try {
                            cloneApp.fault(f.getInstanceID(), f.getReq());
                            if (this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                                return true;
    
                        } catch (Exception e) {
                            return false;
                        } 
                    }
                }
            }
        }else{
            ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
            ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

            Application cloneApp = app.clone();;

            if (brokenInstances.isEmpty() == false) {
                try {
                    cloneApp.scaleIn(brokenInstances.get(0).getID());
                    if (this.deterministicIsWeaklyValidSequence(cloneApp, sequence) == true)
                        return true;

                } catch (RuleNotApplicableException | InstanceUnknownException e) {
                    return false;
                }
                cloneApp = app.clone(); // clone this to reset the clone well
            }

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
        }
        return false;
    }

    /**
     * @param app application on which the analysis will be executed
     * @param sequence sequence of "operation" of which the validity check is needed
     * @return true if the sequence is not valid
     * @throws NullPointerException
     * @throws IllegalSequenceElementException
     * @throws InstanceUnknownException
     */
    public boolean isNotValidSequence(Application app, List<ExecutableElement> sequence)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {

        List<ExecutableElement> backupSequence = this.cloneList(sequence);

        if (app.isPiDeterministic() == true)
            if(this.deterministicIsWeaklyValidSequence(app, sequence) == false){
                AnalysisReport fail = fails.get(app.getName());
                fail.setSequence(backupSequence);
                return true;
            }
        else{
            if(this.nonDeterministicIsWeaklyValidSeq(app, sequence) == false){
                AnalysisReport fail = fails.get(app.getName());
                fail.setSequence(backupSequence);
                return true;
            }
        }
        return false;  
    }

    /**
     * @param app application on which the analysis will be executed
     * @param planExecutableElements executable elements that compose the plan
     * @param constraints constraints that the plan must respect
     * @return true if the plan is valid
     * @throws NullPointerException
     * @throws IllegalSequenceElementException
     * @throws InstanceUnknownException
     */
    public boolean isValidPlan(Application app, List<ExecutableElement> planExecutableElements, List<Constraint> constraints)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {   
        if(planExecutableElements.isEmpty())
            return true;
        
        //e1 -> [e2, e3, ...]: match e1 with the executable elements that must be executed after e1
        Map<ExecutableElement, List<ExecutableElement>> constraintsMap = this.buildConstraintMap(planExecutableElements, constraints);
        return this.planValidity(app, false, this.cloneList(planExecutableElements), constraintsMap);
    }

    /**
     * @param app application on which the analysis will be executed
     * @param planExecutableElements executable elements that compose the plan
     * @param constraints constraints that the plan must respect
     * @return true if the plan is weakly valid
     * @throws NullPointerException
     * @throws IllegalSequenceElementException
     * @throws InstanceUnknownException
     */
    public boolean isWeaklyValidPlan(Application app, List<ExecutableElement> planExecutableElements, List<Constraint> constraints)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {
        if(planExecutableElements.isEmpty())
            return true;
        
        //e1 -> [e2, e3, ...]: match e1 with the executable elements that must be executed after e1
        Map<ExecutableElement, List<ExecutableElement>> constraintsMap = this.buildConstraintMap(planExecutableElements, constraints);
        
        return this.planValidity(app, true, this.cloneList(planExecutableElements), constraintsMap);
    }

    /**
     * @param app application on which the analysis will be executed
     * @param planExecutableElements executable elements that compose the plan
     * @param constraints constraints that the plan must respect
     * @return true if the plan is not valid
     * @throws NullPointerException
     * @throws IllegalSequenceElementException
     * @throws InstanceUnknownException
     */
    public boolean isNotValidPlan(Application app, List<ExecutableElement> planExecutableElements, List<Constraint> constraints)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {
        return !this.isWeaklyValidPlan(app, planExecutableElements, constraints);
    }

    /**
     * @param app application on which the analysis will be executed
     * @param weaklyValid true if the anlysis that must be done is about the weakly validity (else validity)
     * @param planExecutableElements executable elements that compose the plan
     * @param constraintsMap map executable element x -> list of ex elements that must be executed after x
     * @return true if the plan has the validity requested
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws IllegalSequenceElementException
     * @throws InstanceUnknownException
     */
    public boolean planValidity
        (
            Application app, 
            boolean weaklyValid, 
            List<ExecutableElement> planExecutableElements, 
            Map<ExecutableElement, List<ExecutableElement>> constraintsMap
        )
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            IllegalSequenceElementException,
            InstanceUnknownException 
    {
        
        //if the constraints are respected for the original permutation of ex elements explore that sequence
        if(this.checkConstraints(planExecutableElements, constraintsMap) == true){
            if(weaklyValid == true){
                if(this.isWeaklyValidSequence(app.clone(), this.cloneList(planExecutableElements)) == true)
                    return true;
            }
            else{
                if(this.isValidSequence(app.clone(), this.cloneList(planExecutableElements)) == false)
                    return false;
            }
        }

        int permSize = planExecutableElements.size();

        //creates all perms and check one by one (heap algorithm)
        int[] c = new int[permSize];        
        for(int i = 0; i < permSize; i++)
            c[i] = 0;
        
        int i = 0;
        while(i < permSize){
            
            if(c[i] < i){
                
                if(i % 2 == 0)
                    Collections.swap(planExecutableElements, 0, i);
                else
                    Collections.swap(planExecutableElements, i, c[i]);

                //for each generated permutation of planExecutableElements we check if it respect the constraints, 
                //if so this is a sequence generated by the management plan
                if(this.checkConstraints(planExecutableElements, constraintsMap) == true){
                    if(weaklyValid == true){
                        if(this.isWeaklyValidSequence(app.clone(), this.cloneList(planExecutableElements)) == true)
                            return true;
                    }
                    else{
                        if(this.isValidSequence(app.clone(), this.cloneList(planExecutableElements)) == false)
                            return false;
                    }
                }
                
                c[i]++;
                i = 0;
            
            }else{
                c[i] = 0;
                i++;
            }
                                
        }
        
        return true;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ NON DETERMINISTIC APP'S MANAGEMENT OPERATION ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private boolean nonDetOpStartOpEnd(Application app, ExecutableElement op, boolean weaklyValid, List<ExecutableElement> sequence)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException,
            IllegalSequenceElementException 
    {
        if(op instanceof OpStart){
            OpStart todo = (OpStart) op;
            NodeInstance instance = app.getGlobalState().getNodeInstanceByID(todo.getInstanceID());
    
            //requirements needed by instance before doing the op
            List<Requirement> neededReqsBeforeOp = instance.getNeededReqs();
            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                fails.put(app.getName(), new AnalysisReport(todo, e));
                return false;
            }
            
            //check the fault branches
            if(weaklyValid == true){
                if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                    return true;
            }else{
                if(this.checkFaultsValid(app, sequence, false) == false)
                    return false;
            }

            //requirements needed after doing the op
            List<Requirement> neededReqsAfterOp = instance.getNeededReqs();
    
            //here will be the requirements that were needed before op and are still needed
            List<Requirement> neededReqsBeforeAndAfter = new ArrayList<>();
            for (Requirement req : neededReqsBeforeOp) {
                if (neededReqsAfterOp.contains(req))
                    neededReqsBeforeAndAfter.add(req);
            }
            //we analyze the possible combination of the new runtime bindings (the runtime binding that was already on)
            //before the operation are not altered

            //current runtime bindings of instance
            List<RuntimeBinding> instanceRuntimeBinding = app.getGlobalState().getRuntimeBindings().get(instance.getID());
    
            //here will be the runtime bindings that were about requirement that are still needed in the new state
            //those runtime bindings will not be changed with the combinations
            List<RuntimeBinding> runBindingsBeforeAndAfterOp = new ArrayList<>();
    
            for (RuntimeBinding rb : instanceRuntimeBinding) {
                for (Requirement req : neededReqsBeforeAndAfter) {
                    if (req.equals(rb.getReq()))
                        runBindingsBeforeAndAfterOp.add(rb);
                }
            }
            
            List<List<RuntimeBinding>> combinations = this.createRunBindingCombs(app, todo.getInstanceID());

            if(combinations.isEmpty() == true){
                if(weaklyValid == true)
                    return this.nonDeterministicIsWeaklyValidSeq(app.clone(), sequence);
                else
                    return this.nonDeterministicIsValidSequence(app.clone(), sequence);
            }

            Application cloneApp = null;

            //for each combination of runtime bindings we check if the combination contains the "unchangable" runtimebindings
            //then we complement with the new runtime bindings, checking for each possible combination
            for (List<RuntimeBinding> comb : combinations) {

                if (comb.containsAll(runBindingsBeforeAndAfterOp) == true) {
                    cloneApp = app.clone();

                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if(weaklyValid == true){
                        if(this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                            return true;
                    }else{
                        if(this.nonDeterministicIsValidSequence(cloneApp, sequence) == false)
                            return false;
                    }
                    
                }
            }

        }else if(op instanceof OpEnd){ //same as OpStart right above

            OpEnd todo = (OpEnd) op;
            NodeInstance instance = app.getGlobalState().getActiveNodeInstances().get(todo.getInstanceID());

            List<Requirement> neededReqsBeforeOp = instance.getNeededReqs();
            
            try {
                app.execute(todo);
            } catch (FailedOperationException e) {
                // keep going, this will be handled by the fault handler or leaved as it is
            } catch (Exception e) {
                fails.put(app.getName(), new AnalysisReport(todo, e));
                return false;
            }

            //check the fault branches
            if(weaklyValid == true){
                if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                    return true;
            }else{
                if(this.checkFaultsValid(app, sequence, false) == false)
                    return false;
            }

            List<Requirement> neededReqsAfterOp = instance.getNeededReqs();
            List<Requirement> neededReqsBeforeAndAfter = new ArrayList<>();

            for (Requirement req : neededReqsBeforeOp) {
                if (neededReqsAfterOp.contains(req))
                    neededReqsBeforeAndAfter.add(req);
            }

            List<RuntimeBinding> instanceRuntimeBinding = app.getGlobalState().getRuntimeBindings().get(instance.getID());
            List<RuntimeBinding> runBindingsBeforeAndAfterOp = new ArrayList<>();

            for (RuntimeBinding rb : instanceRuntimeBinding) {
                for (Requirement req : neededReqsBeforeAndAfter) {
                    if (req.equals(rb.getReq()))
                        runBindingsBeforeAndAfterOp.add(rb);
                }
            }

            List<List<RuntimeBinding>> combinations = this.createRunBindingCombs(app, todo.getInstanceID());

            if(combinations.isEmpty() == true){
                if(weaklyValid == true)
                    return this.nonDeterministicIsWeaklyValidSeq(app.clone(), sequence);
                else
                    return this.nonDeterministicIsValidSequence(app.clone(), sequence);
            }

            Application cloneApp = null;

            for (List<RuntimeBinding> comb : combinations) {
                if (comb.containsAll(runBindingsBeforeAndAfterOp) == true) {
                    cloneApp = app.clone();
                    for (RuntimeBinding rb : comb) 
                        cloneApp.getGlobalState().addBinding(instance.getID(), rb.getReq(), rb.getNodeInstanceID());
                    
                    if(weaklyValid == true){
                        if(this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                            return true;
                    }else{
                        if(this.nonDeterministicIsValidSequence(cloneApp, sequence) == false)
                            return false;
                    }
                }
            }
        }
        //returns true because if the analysis is about the validity and the last 
        // element of the sequence is valid this has to be true (same on other op)
        return true; 
    }

    private boolean nonDetScaleIn(Application app, ExecutableElement op, boolean weaklyValid, List<ExecutableElement> sequence)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {
        ScaleIn todo = (ScaleIn) op;

        try {
            app.execute(todo);
        } catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (Exception e) {
            fails.put(app.getName(), new AnalysisReport(todo, e));
            return false;
        }

        if(weaklyValid == true){
            if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                return true;
            else
                return false;
        }else{
            if(this.checkFaultsValid(app, sequence, false) == false)
                return false;
        }

        return true;
    }

    private boolean nonDetScaleOut1(Application app, ExecutableElement op, boolean weaklyValid, List<ExecutableElement> sequence)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {
        ScaleOut1 todo = (ScaleOut1) op;
        try {
            app.execute(todo);
        } catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (Exception e) {
            fails.put(app.getName(), new AnalysisReport(todo, e));
            return false;
        }

        if(weaklyValid == true){
            if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                return true;
        }else{
            if(this.checkFaultsValid(app, sequence, false) == false)
                return false;
        }
        
        //ScaleOut1 creates automatically the needed runtime binding but we want every 
        //possible cominations so we removes the bindings and go with the combinations
        app.getGlobalState().removeAllRunBindings(todo.getIDToAssign());
        
        List<List<RuntimeBinding>> combinations = this.createRunBindingCombs(app, todo.getIDToAssign());

        Application cloneApp = null;
        
        if(combinations.isEmpty() == true){
            if(weaklyValid == true)
                return this.nonDeterministicIsWeaklyValidSeq(app.clone(), sequence);
            else
                return this.nonDeterministicIsValidSequence(app.clone(), sequence);
        }
       
        //explore each brench given by the run binding combinations 
        for(List<RuntimeBinding> comb : combinations){
            cloneApp = app.clone();
            for(RuntimeBinding rb : comb)
                cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
            
            if(weaklyValid == true){
                if(this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                    return true;
            }else{
                if(this.nonDeterministicIsValidSequence(cloneApp, sequence) == false)
                    return false;
            }
        }

        return true;
    }

    private boolean nonDetScaleOut2(Application app, ExecutableElement op, boolean weaklyValid, List<ExecutableElement> sequence)
        throws 
            NullPointerException, 
            IllegalSequenceElementException, 
            InstanceUnknownException 
    {
        ScaleOut2 todo = (ScaleOut2) op;

        try {
            app.execute(todo);
        } catch (FailedOperationException e) {
            // keep going, this will be handled by the fault handler or leaved as it is
        } catch (Exception e) {
            fails.put(app.getName(), new AnalysisReport(todo, e));
            return false;
        }
        
        if(weaklyValid == true){
            if(this.checkFaultsWeaklyValid(app, sequence, false) == true)
                return true;
        }else{
            if(this.checkFaultsValid(app, sequence, false) == false)
                return false;
        }
        
        //as scaleOut1 with the exception that the runtime binding of the containment req is saved 
        //not generated with the combinations (means that each combinations will have this rb)
        RuntimeBinding containmentRB = null;
        for(RuntimeBinding rb : app.getGlobalState().getRuntimeBindings().get(todo.getIDToAssign())){
            if(rb.getReq().isContainment() == true)
                containmentRB = rb;
        }

        app.getGlobalState().removeAllRunBindingsBothWays(todo.getIDToAssign());
        app.getGlobalState().addBinding(todo.getIDToAssign(), containmentRB.getReq(), todo.getContainerID());

        
        List<List<RuntimeBinding>>combinations = this.createRunBindingCombs(app, todo.getIDToAssign());

        if(combinations.isEmpty() == true){
            if(weaklyValid == true)
                return this.nonDeterministicIsWeaklyValidSeq(app.clone(), sequence);
            else
                return this.nonDeterministicIsValidSequence(app.clone(), sequence);
        }
            
        Application cloneApp = null;

        for(List<RuntimeBinding> comb : combinations){
            cloneApp = app.clone();
            for(RuntimeBinding rb : comb)
                cloneApp.getGlobalState().addBinding(todo.getIDToAssign(), rb.getReq(), rb.getNodeInstanceID());
            
            if(weaklyValid == true){
                if(this.nonDeterministicIsWeaklyValidSeq(cloneApp, sequence) == true)
                    return true;
            }else{
                if(this.nonDeterministicIsValidSequence(cloneApp, sequence) == false)
                    return false;
            }
            
        }

        return true;
    }

}