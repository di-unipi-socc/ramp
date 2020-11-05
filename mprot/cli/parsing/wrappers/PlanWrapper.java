package mprot.cli.parsing.wrappers;

import java.util.List;

import mprot.core.analyzer.Constraint;
import mprot.core.analyzer.executable_element.ExecutableElement;

/**
 * this is a wrapper class just for parsing
 */

public class PlanWrapper {

    private final List<ExecutableElement> planExecutableElements;
    private final List<Constraint> constraints;

    private final boolean isSequence; //true if we want to analyze a sequence and not a plan

    public PlanWrapper(List<ExecutableElement> planExecutableElements, List<Constraint> constraints, boolean isSequence) {
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
