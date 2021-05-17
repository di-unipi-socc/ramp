package unipi.di.socc.ramp.core.analyzer;

import unipi.di.socc.ramp.core.analyzer.actions.Action;
import unipi.di.socc.ramp.core.model.GlobalState;

public class AnalysisReport {
    
    private Sequence failedSequence;
    private Exception failException;
    private Action failedAction;


    public GlobalState faultedGS;

    public AnalysisReport() {
        this.failedSequence = null;
        this.failException = null;
        this.failedAction = null;
        this.faultedGS = null;
    }

    //#region getter & setter

    public Action getFailedAction() {
        return failedAction;
    }


    public GlobalState getGlobalState(){
        return this.faultedGS;
    }

    public void setGlobalState(GlobalState gs){
        if(this.faultedGS == null)
            this.faultedGS = gs;
    }

    public void setFailedAction(Action failedAction) {
        if(this.failedAction == null)
            this.failedAction = failedAction;
    }

    public Exception getFailException() {
        return failException;
    }

    public void setFailException(Exception failException) {
        if(this.failException == null)
            this.failException = failException;
    }

    public Sequence getFailedSequence() {
        return failedSequence;
    }

    public void setFailedSequence(Sequence failedSequence) {
        this.failedSequence = failedSequence;
    }


    //#endregion

}
