package mprot.lib.analyzer;

import java.util.List;

import mprot.lib.analyzer.executable_element.*;
import mprot.lib.model.Fault;

public class AnalysisFailReport {

    private  List<ExecutableElement> sequence;
    private final Exception failException;

    private final ExecutableElement failedElement;

    private final String failedBrokenInstanceID;

    private final Fault fatalFault;

    private final FailType fail;

    public AnalysisFailReport(ExecutableElement failedElement, Exception failException) {
        this.fail = FailType.OPERATION;

        this.failException = failException;
        this.failedElement = failedElement;

        this.failedBrokenInstanceID = null;
        this.fatalFault = null;
    }

    public AnalysisFailReport(String brokenInstanceID, Exception failException) {
        this.fail = FailType.BROKENINSTANCE;

        this.failException = failException;
        this.failedBrokenInstanceID = brokenInstanceID;

        this.failedElement = null;
        this.fatalFault = null;

    }

    public AnalysisFailReport(Exception failException, Fault fatalFault) {
        this.fail = FailType.FAULT;
        this.failException = failException;
        this.fatalFault = fatalFault;

        this.failedBrokenInstanceID = null;
        this.failedElement = null;
    }

    public Fault getFatalFault() {
        return fatalFault;
    }

    public ExecutableElement getFailedElement() {
        return failedElement;
    }

    public Exception getFailException() {
        return failException;
    }

    public List<ExecutableElement> getSequence() {
        return sequence;
    }

    public String getFailedBrokenInstanceID() {
        return failedBrokenInstanceID;
    }

    public FailType getFailType() {
        return fail;
    }

    public void setSequence(List<ExecutableElement> sequence){
        this.sequence = sequence;
    }

}
