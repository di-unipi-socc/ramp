package unipi.di.socc.ramp.core.analyzer;

import java.util.ArrayList;
import java.util.List;

import unipi.di.socc.ramp.cli.parser.PrintingUtilities;
import unipi.di.socc.ramp.core.analyzer.actions.Action;
import unipi.di.socc.ramp.core.analyzer.exceptions.UnsupportedAnalysisException;
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
    public boolean sequenceAnalysis(Application app, Sequence sequence, String property) throws UnsupportedAnalysisException {
        // Case: valid sequence analysis
        if(property.equals("--valid")){
            //saves the sequence
            this.report.setFailedSequence(new Sequence(this.cloneList(sequence.getActions())));
            return isValidSequence(app, sequence);
        }
        // Default: unsupported analysis
        throw new UnsupportedAnalysisException();
    }

    public boolean planAnalysis(Application app, Plan plan, String property) throws UnsupportedAnalysisException {
        Sequence actions = new Sequence(plan.getActions());
        // Case: weakly valid plan analysis
        if(property.equalsIgnoreCase("--weakly-valid"))
            return isValidPlan(app,plan,new Sequence(),actions.clone(),true);
        // Case: valid plan analysis
        if(property.equalsIgnoreCase("--valid"))
            return isValidPlan(app,plan,new Sequence(),actions.clone(),false);
        // Default: unsupported analysis
        throw new UnsupportedAnalysisException();
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

    // private boolean isWeaklyValidSequence(Application app, Sequence sequence){

    //     //base case
    //     if(sequence.getActions().isEmpty())
    //         return true;

    //     // pop the first action of the sequential plan
    //     Action action = sequence.getActions().remove(0);
    //     try {
    //         app.execute(action);
    //     } catch (FailedOperationException e) {
    //         //go on, this will be a fault
    //     } catch (Exception e) {
    //         this.report.setFailedAction(action);
    //         this.report.setFailException(e);
    //         return false;
    //     }

    //     //check the branches of the faults
    //     if(this.checkFaultsWeaklyValid(app, sequence))
    //         return true;
        
    //     this.report.setFailedAction(action);
    //     return false;
    // }
    // private boolean checkFaultsWeaklyValid(Application app, Sequence sequence){
    //     //list of broken instances and pending faults of app just after the execution of action
    //     List<NodeInstance> brokenInstances;
    //     List<Fault> pendingFaults;
    //     try {
    //         brokenInstances = app.getGlobalState().getBrokenInstances();
    //         pendingFaults = app.getGlobalState().getPendingFaults();
    //     } catch (Exception e) {
    //         return false;
    //     }

    //     if(app.isPiDeterministic()){
    //         //application of no-broken-instances
    //         if(!brokenInstances.isEmpty()){
    //             Application clonedApp = app.clone();
    //             try {
    //                 //this will kill all the broken instances
    //                 clonedApp.scaleIn(brokenInstances.get(0).getID());
    //             } catch (Exception e) {
    //                 return false;
    //             }
    //         }

    //         //branching: we keep exploring not handling a single fault
    //         if(this.isWeaklyValidSequence(app.clone(), sequence))
    //             return true;

    //         //branching: for each fault we fix it and starts exploring
    //         if(!pendingFaults.isEmpty()){
    //             for(Fault pendingFault : pendingFaults){
    //                 Application clonedApp = app.clone();

    //                 boolean isResolvableFault;
    //                 try {
    //                     isResolvableFault = clonedApp.getGlobalState().isResolvableFault(pendingFault);
    //                 } catch (Exception e) {
    //                     return false;
    //                 }

    //                 if(isResolvableFault){
    //                     try {
    //                         //fix the fault by creating a new runtime binding that safisfy it
    //                         clonedApp.autoreconnect(pendingFault);
    //                         if(this.isWeaklyValidSequence(clonedApp, sequence))
    //                             return true;

    //                     } catch (Exception e) {
    //                         return false;
    //                     }
    //                 }else{
    //                     try {
    //                         //handle the fault by applying the fault handler
    //                         clonedApp.faultHandler(pendingFault);
    //                         if(this.isWeaklyValidSequence(clonedApp, sequence))
    //                             return true;

    //                     } catch (Exception e) {
    //                         return false;
    //                     }
    //                 }
    //             }
    //         }
    //     }
    //     return false;
    // }

    //#endregion

    //#region PLAN ANALYSIS
    
    private boolean isValidPlan(Application app, Plan plan, Sequence traceFragment, Sequence remainingActions, boolean weakValidity) {
        // If there are no remainingActions, check the validity of the trace denoted by traceFragment
        if(remainingActions.getActions().isEmpty()) {
            boolean validTrace = isValidSequence(app.clone(), traceFragment.clone());
            // Case: Weakly valid plan analysis (found valid trace, return true)
            if(validTrace && weakValidity)
                return true;
            // Case: Valid plan analysis (violation to validity constraints, return false)
            if(!validTrace && !weakValidity) {
                this.report.setFailedSequence(traceFragment.clone());
                return false;
            }
        }
        
        // Otherwise, expand traceFragment with any of the remainingActions (if possible) and recur
        for(int i=0; i<remainingActions.getActions().size(); i++) {
            // Extract action "a" to consider and compute "new" remainingActions
            Sequence newRemainingActions = remainingActions.clone();
            Action a = newRemainingActions.getActions().remove(i);
            // If "a" can be added before the other remaining actions
            boolean orderPreservingAction = true;
            for(Action remaining : newRemainingActions.getActions()) {
                if(plan.getPartialOrder().get(remaining).contains(a)) {
                    orderPreservingAction = false;
                    break;
                }
            }
            if(orderPreservingAction) {
                // Concat "a" to "new" traceFragment
                Sequence newTraceFragment = traceFragment.clone();
                newTraceFragment.getActions().add(a);
                // Recur with new traceFragment and new remainingActions
                boolean validPlan = isValidPlan(app,plan,newTraceFragment,newRemainingActions,weakValidity);
                // Case: Weakly valid plan analysis (found valid trace, return true)
                if(validPlan && weakValidity)
                    return true;
                // Case: Valid plan analysis (violation to validity constraints, return false)
                if(!validPlan && !weakValidity) {
                    return false;
                }
            }
        }

        // Case: Weakly valid plan analysis (plan assumed to not be weakly valid)
        if(weakValidity) return false;
        // Case: Valid plan analysis (plan assumed to be valid)
        return true;
    }

    //#endregion





    public void printReport(){
        System.out.println("FAILED SEQUENCE: ");
        PrintingUtilities.printSequence(this.report.getFailedSequence());

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
