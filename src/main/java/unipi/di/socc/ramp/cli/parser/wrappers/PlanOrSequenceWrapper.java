package unipi.di.socc.ramp.cli.parser.wrappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PlanOrSequenceWrapper {
    private final Map<String, ActionWrapper> actions;
    private List<ConstraintWrapper> partialOrder;

    public PlanOrSequenceWrapper(
        Map<String, ActionWrapper> actions, 
        List<ConstraintWrapper> partialOrder
    ){
        this.actions = actions;
        this.partialOrder = partialOrder;
    }

    public void initializePartialOrdering(){
        this.partialOrder = new ArrayList<>();
    }
    public Map<String, ActionWrapper> getActions() {
        return actions;
    }
    public List<ConstraintWrapper> getPartialOrderWrap() {
        return partialOrder;
    }
}
