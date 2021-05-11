package unipi.di.socc.ramp.cli.parser.wrappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import unipi.di.socc.ramp.core.analyzer.actions.Action;

public class PlanOrSequenceWrapper {
    private final Map<String, Action> actions;
    private List<ConstraintWrapper> partialOrdering;

    public PlanOrSequenceWrapper(
        Map<String, Action> actions, 
        List<ConstraintWrapper> partialOrdering
    ){
        this.actions = actions;
        this.partialOrdering = partialOrdering;
    }

    public void initializePartialOrdering(){
        this.partialOrdering = new ArrayList<>();
    }
    public Map<String, Action> getActions() {
        return actions;
    }
    public List<ConstraintWrapper> getPartialOrdering() {
        return partialOrdering;
    }
}
