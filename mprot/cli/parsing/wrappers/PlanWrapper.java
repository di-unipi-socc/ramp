package mprot.cli.parsing.wrappers;

import java.util.List;
import java.util.Map;

import mprot.cli.parsing.ConstraintLabel;
import mprot.core.analyzer.Constraint;
import mprot.core.analyzer.executable_element.ExecutableElement;

/**
 * this is a wrapper class just for parsing
 */

public class PlanWrapper {

    private final Map<String, ExecutableElement> planExecutableElements;
    private List<Constraint> constraints;
    private final List<ConstraintLabel> constraintsLables;

    private final boolean isSequence; // true if we want to analyze a sequence and not a plan

    public PlanWrapper(
            Map<String, ExecutableElement> planExecutableElements, 
            List<Constraint> constraints,
            List<ConstraintLabel> constraintsLables,
            boolean isSequence) {

        this.planExecutableElements = planExecutableElements;
        this.constraints = constraints;
        this.constraintsLables = constraintsLables;
        this.isSequence = isSequence;
    }

    public List<ConstraintLabel> getConstraintsLables() {
        return constraintsLables;
    }

    public boolean getIsSequence() {
        return isSequence;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public Map<String, ExecutableElement> getPlanExecutableElements() {
        return planExecutableElements;
    }

    public void setConstraints(List<Constraint> constraints){
        this.constraints = constraints;
    }


}
