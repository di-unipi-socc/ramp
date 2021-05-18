package unipi.di.socc.ramp.core.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import unipi.di.socc.ramp.cli.parser.PrintingUtilities;
import unipi.di.socc.ramp.core.analyzer.actions.Action;
import unipi.di.socc.ramp.core.analyzer.actions.OpEnd;
import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.Fault;
import unipi.di.socc.ramp.core.model.NodeInstance;

import unipi.di.socc.ramp.core.model.exceptions.FailedOperationException;

public class Analyzer {
    
    private AnalysisReport report;


    public Analyzer(){
        this.report = new AnalysisReport();
    }

    public AnalysisReport getReport(){
        return report;
    }



    //#region utilities


    //clone a list making another list with the same object refs
    private <E> List<E> cloneList(List<E> list){
        List<E> cloneList = new ArrayList<>();
        for(E element : list)
            cloneList.add(element);

        return cloneList;
    }

    //#endregion


    //########################### OFFERED METHODS ###########################
    public boolean sequenceAnalysis(Application app, Sequence sequence, String property){

        if(property.equals("--valid")){
            //saves the sequence
            this.report.setFailedSequence(new Sequence(this.cloneList(sequence.getActions())));
            return isValidSequence(app, sequence);
        }

        if(property.equals("--weakly-valid")){
            //saves the sequence
            this.report.setFailedSequence(new Sequence(this.cloneList(sequence.getActions())));
            return isWeaklyValidSequence(app, sequence);
        }

        //TOOD fix
        return false;


    }

    public boolean planAnalysis(Application app, Plan plan, String property){

        if(property.equals("--valid"))
            return this.isValidPlan(app, plan);
        
        if(property.equals("--weakly-valid"));
            //return is weakly valid plan
            
        

        //TOOD fix
        return false;

    }
    

    //#region SEQUENCE ANALYSIS

    private boolean isValidSequence(Application app, Sequence sequence) {

        //base case
        if(sequence.getActions().isEmpty())
            return true;

        // pop the first action of the sequential plan
        Action action = sequence.getActions().get(0);
        boolean faultedOpEnd = false;


        try {
            app.execute(action);
            sequence.getActions().remove(0);
        } catch (FailedOperationException e) {
            faultedOpEnd = true;
            //go on, this will be a fault
        } catch (Exception e) {
            this.report.setFailedAction(action);
            this.report.setFailException(e);
            this.report.setGlobalState(app.getGlobalState());
            return false;
        }



        //check the branches of the faults
        if(!checkFaultsValid(app, sequence, faultedOpEnd)){
            this.report.setFailedAction(action);
            this.report.setGlobalState(app.getGlobalState());
            return false;
        }

        return true;
    }
    private boolean checkFaultsValid(Application app, Sequence sequence, boolean faultedOpEnd) {
        //list of broken instances and pending faults of app just after the execution of action
        List<NodeInstance> brokenInstances;
        List<Fault> pendingFaults;
        try {
            brokenInstances = app.getGlobalState().getBrokenInstances();
            pendingFaults = app.getGlobalState().getPendingFaults();
        } catch (Exception e) {
            return false;
        }


        //application of no-broken-instances
        if(!brokenInstances.isEmpty()){
            try {
                //this will kill all the broken instances
                app.scaleIn(brokenInstances.get(0).getID());
            } catch (Exception e) {
                return false;
            }
        }

        if(app.isPiDeterministic()){

            //branching: we keep exploring not handling a single fault
            if(!faultedOpEnd && !this.isValidSequence(app.clone(), sequence))
                return false;

            //branching: for each fault we fix it and starts exploring
            if(!pendingFaults.isEmpty()){
                for(Fault pendingFault : pendingFaults){

                    Application clonedApp = app.clone();

                    boolean isResolvableFault;
                    try {
                        isResolvableFault = clonedApp.getGlobalState().isResolvableFault(pendingFault);
                    } catch (Exception e) {
                        this.report.faultedGS = clonedApp.getGlobalState();
                        return false;
                    }

                    if(isResolvableFault){
                        try {
                            //fix the fault by creating a new runtime binding that safisfy it
                            clonedApp.autoreconnect(pendingFault);
                            if(!this.isValidSequence(clonedApp, sequence))
                                return false;

                        } catch (Exception e) {
                            return false;
                        }
                    }else{
                        try {
                            //handle the fault by applying the fault handler
                            clonedApp.faultHandler(pendingFault);
                            

                            if(faultedOpEnd)
                                sequence.getActions().remove(0);

                            if(!this.isValidSequence(clonedApp, sequence))
                                return false;

                        } catch (Exception e) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isWeaklyValidSequence(Application app, Sequence sequence){

        //base case
        if(sequence.getActions().isEmpty())
            return true;

        // pop the first action of the sequential plan
        Action action = sequence.getActions().remove(0);
        try {
            app.execute(action);
        } catch (FailedOperationException e) {
            //go on, this will be a fault
        } catch (Exception e) {
            this.report.setFailedAction(action);
            this.report.setFailException(e);
            return false;
        }

        //check the branches of the faults
        if(this.checkFaultsWeaklyValid(app, sequence))
            return true;
        
        this.report.setFailedAction(action);
        return false;
    }
    private boolean checkFaultsWeaklyValid(Application app, Sequence sequence){
        //list of broken instances and pending faults of app just after the execution of action
        List<NodeInstance> brokenInstances;
        List<Fault> pendingFaults;
        try {
            brokenInstances = app.getGlobalState().getBrokenInstances();
            pendingFaults = app.getGlobalState().getPendingFaults();
        } catch (Exception e) {
            return false;
        }

        if(app.isPiDeterministic()){
            //application of no-broken-instances
            if(!brokenInstances.isEmpty()){
                Application clonedApp = app.clone();
                try {
                    //this will kill all the broken instances
                    clonedApp.scaleIn(brokenInstances.get(0).getID());
                } catch (Exception e) {
                    return false;
                }
            }

            //branching: we keep exploring not handling a single fault
            if(this.isWeaklyValidSequence(app.clone(), sequence))
                return true;

            //branching: for each fault we fix it and starts exploring
            if(!pendingFaults.isEmpty()){
                for(Fault pendingFault : pendingFaults){
                    Application clonedApp = app.clone();

                    boolean isResolvableFault;
                    try {
                        isResolvableFault = clonedApp.getGlobalState().isResolvableFault(pendingFault);
                    } catch (Exception e) {
                        return false;
                    }

                    if(isResolvableFault){
                        try {
                            //fix the fault by creating a new runtime binding that safisfy it
                            clonedApp.autoreconnect(pendingFault);
                            if(this.isWeaklyValidSequence(clonedApp, sequence))
                                return true;

                        } catch (Exception e) {
                            return false;
                        }
                    }else{
                        try {
                            //handle the fault by applying the fault handler
                            clonedApp.faultHandler(pendingFault);
                            if(this.isWeaklyValidSequence(clonedApp, sequence))
                                return true;

                        } catch (Exception e) {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    //#endregion

    //#region PLAN ANALYSIS

    public List<Sequence> generateTraces(Plan plan) {
        // Clone plan's list of actions and invoke "_generateTraces"
        Sequence actions = new Sequence(plan.getActions());
        return _generateTraces(plan, new Sequence(), actions.clone());
    }
    private List<Sequence> _generateTraces(Plan plan, Sequence traceFragment, Sequence remainingActions) {
        // Create empty list of traces
        List<Sequence> traces = new ArrayList<Sequence>();

        // If there are no remainingActions, return the singleton set containing
        // the sequence denoted by traceFragment
        if(remainingActions.getActions().isEmpty()) {
            List<Action> traceActions = new ArrayList<Action>();
            traceActions.addAll(traceFragment.getActions());
            traces.add(new Sequence(traceActions));
            return traces;
        }
        
        // Otherwise, expand traceFragment with any of the remainingActions (if possible) and recur
        for(int i=0; i<remainingActions.getActions().size(); i++) {
            // Extract action "a" to consider and compute "new" remainingActions
            Sequence newRemainingActions = remainingActions.clone();
            Action a = newRemainingActions.getActions().remove(i);
            // If "a" can be added before the other remaining actions
            boolean validChoice = true;
            for(Action remaining : newRemainingActions.getActions()) {
                if(plan.getPartialOrder().get(remaining).contains(a)) {
                    validChoice = false;
                    break;
                }
            }
            if(validChoice) {
                // Concat "a" to "new" traceFragment
                Sequence newTraceFragment = traceFragment.clone();
                newTraceFragment.getActions().add(a);
                // Recur with new traceFragment and new remainingActions
                traces.addAll(_generateTraces(plan, newTraceFragment, newRemainingActions));
            }
        }

        System.out.println(traces.size());
        // Return all computed traces
        return traces;
    }

    public boolean isValidPlanNew(Application app, Plan plan) {
        // Generate all plan's possible sequential traces
        List<Sequence> traces = generateTraces(plan);
        // If any trace is not valid, return false and set failing sequence
        for(Sequence trace : traces) {
            if(!this.sequenceAnalysis(app.clone(), trace, "--valid")){
                this.report.setFailedSequence(trace);
                return false;
            }
        }
        // Otherwise, return true
        return true;
    }

    public boolean _isValidPlanNew(Application app, Plan plan, Sequence traceFragment, Sequence remainingActions) {
        // If there are no remainingActions, check the trace corresponding to traceFragment
        if(remainingActions.getActions().isEmpty()) {
            if(!this.sequenceAnalysis(app.clone(), traceFragment, "--valid")){
                this.report.setFailedSequence(traceFragment);
                return false;
            }
        }
        
        // Otherwise, expand traceFragment with any of the remainingActions (if possible) and recur
        for(int i=0; i<remainingActions.getActions().size(); i++) {
            // Extract action "a" to consider and compute "new" remainingActions
            Sequence newRemainingActions = remainingActions.clone();
            Action a = newRemainingActions.getActions().remove(i);
            // If "a" can be added before the other remaining actions
            boolean validChoice = true;
            for(Action remaining : newRemainingActions.getActions()) {
                if(plan.getPartialOrder().get(remaining).contains(a)) {
                    validChoice = false;
                    break;
                }
            }
            if(validChoice) {
                // Concat "a" to "new" traceFragment
                Sequence newTraceFragment = traceFragment.clone();
                newTraceFragment.getActions().add(a);
                // Recur with new traceFragment and new remainingActions
                if(!_isValidPlanNew(app,plan,newTraceFragment,newRemainingActions))
                    return false;
            }
        }

        // Return all computed traces
        return true;
    }
    
    public boolean isValidPlan(Application app, Plan plan){
        int permSize = plan.getActions().size();



        //creates all perms and check one by one (heap algorithm)
        int[] c = new int[permSize];        
        for(int i = 0; i < permSize; i++)
            c[i] = 0;
        
        int i = 0;
        while(i < permSize){
            
            if(c[i] < i){            
                if(i % 2 == 0)
                    Collections.swap(plan.getActions(), 0, i);
                else
                    Collections.swap(plan.getActions(), i, c[i]);
              
                //for each generated permutation of planExecutableElements we check if it respect the constraints, 
                //if so this is a sequence generated by the management plan
                
                Sequence sequentialTrace = new Sequence(plan.getActions());

                if(this.checkConstraints(sequentialTrace, plan.getPartialOrder())){



                    Application clonedApp = app.clone();

                    if(!this.sequenceAnalysis(clonedApp, sequentialTrace.clone(), "--valid")){
                        this.report.setFailedSequence(sequentialTrace);
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
    private boolean checkConstraints(Sequence sequentialTrace, Map<Action, List<Action>> partialOrder){

        List<Action> seqTraceActions = sequentialTrace.getActions();

        for(Action action : seqTraceActions){
            
            //actions that have to be executed after action
            List<Action> afterActions = partialOrder.get(action);

            if(!afterActions.isEmpty()){

                List<Action> subsequence = seqTraceActions.subList(
                    seqTraceActions.indexOf(action) + 1, 
                    seqTraceActions.size()
                );

                if(subsequence.isEmpty())
                    return false;
                
                //for each afterAction we check if it is really scheduled to be executed after action
                for(Action afterAction : afterActions){
                    if(!subsequence.contains(afterAction))
                        return false;
                }

            }
        }
        return true;
    }

    //#endregion





    public void printReport(){
        System.out.println("FAILED SEQUENCE: ");
        for(Action action : this.report.getFailedSequence().getActions()){
            System.out.print("\t");
            PrintingUtilities.printAction(action);
            System.out.print("\n");

        }

        System.out.println("");
        System.out.println("GLOBAL STATE ");
        PrintingUtilities.printGlobalState(this.report.faultedGS);
    
        System.out.println("");
        System.out.println("FAILED ACTION: ");
        System.out.print("\t");
        PrintingUtilities.printAction(this.report.getFailedAction());

        System.out.println("");
        System.out.println("EXCEPTION: " + this.report.getFailException().getClass());
        
    }


}
