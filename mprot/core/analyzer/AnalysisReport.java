package mprot.core.analyzer;

import java.util.List;

import mprot.core.analyzer.executable_element.*;

public class AnalysisReport {

    private  List<ExecutableElement> sequence;
    private final Exception failException;
    private ExecutableElement failedElement;

    public AnalysisReport(ExecutableElement failedElement, Exception failException) {
        this.failException = failException;
        this.failedElement = failedElement;
    }

    public void setFailedElement(ExecutableElement failedElement){
        this.failedElement = failedElement;
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

    public void setSequence(List<ExecutableElement> sequence){
        this.sequence = sequence;
    }

}
