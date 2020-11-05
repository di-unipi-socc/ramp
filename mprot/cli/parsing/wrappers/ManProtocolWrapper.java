package mprot.cli.parsing.wrappers;

import java.util.List;

public class ManProtocolWrapper {

    private final String initialState;
    private final List<NodeStateWrapper> states;
    private final List<TransitionWrapper> transitions;

    public ManProtocolWrapper(String initialState, List<NodeStateWrapper> states, List<TransitionWrapper> transitions) {
        this.initialState = initialState;
        this.states = states;
        this.transitions = transitions;
    }

    public List<TransitionWrapper> getTransitions() {
        return transitions;
    }

    public List<NodeStateWrapper> getStates() {
        return states;
    }

    public String getInitialState() {
        return initialState;
    }


}
