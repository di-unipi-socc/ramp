package mprot.lib.analyzer;

import java.util.List;

import mprot.lib.analyzer.executable_element.ExecutableElement;

public class Plan {

    private final List<ExecutableElement> planExecutableElements;
    private final List<Constraint> constraints;

    private final boolean isSequence;

    public Plan(List<ExecutableElement> planExecutableElements, List<Constraint> constraints, boolean isSequence) {
        this.planExecutableElements = planExecutableElements;
        this.constraints = constraints;
        this.isSequence = isSequence;
    }

    public boolean getIsSequence() {
        return isSequence;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public List<ExecutableElement> getPlanExecutableElements() {
        return planExecutableElements;
    }

}
