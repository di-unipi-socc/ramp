package unipi.di.socc.ramp.core.analyzer;

import unipi.di.socc.ramp.core.analyzer.actions.Action;

public class AnalysisReport {
    
    private Sequence failedSequence;
    private Exception failException;
    private Action failedAction;

    public AnalysisReport() {
        this.failedSequence = null;
        this.failException = null;
        this.failedAction = null;
    }

    //#region getter & setter

    public Action getFailedAction() {
        return failedAction;
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
        if(this.failedSequence == null)
            this.failedSequence = failedSequence;
    }


    //#endregion

}
