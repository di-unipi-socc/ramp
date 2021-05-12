package unipi.di.socc.ramp.core.analyzer;

import java.util.List;
import java.util.Map;

import unipi.di.socc.ramp.core.analyzer.actions.Action;

public class Plan {
    
    private final List<Action> actions;

    //action x -> actions that have to be executed after x
    private final Map<Action, List<Action>> partialOrder;

    public Plan(List<Action> actions, Map<Action, List<Action>> partialOrder){
        if(actions == null || partialOrder == null)
            throw new NullPointerException();

        this.actions = actions;
        this.partialOrder = partialOrder;
    }

    public List<Action> getActions() {
        return actions;
    }
    public Map<Action, List<Action>> getPartialOrder() {
        return partialOrder;
    }

    
}
