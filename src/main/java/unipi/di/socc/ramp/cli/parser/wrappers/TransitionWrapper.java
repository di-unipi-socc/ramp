package unipi.di.socc.ramp.cli.parser.wrappers;

import java.util.List;

public class TransitionWrapper {
    private final String startState;
    private final String operation;
    private final String targetState;
    private final List<String> requires;
    private final List<String> offers;
    private final List<String> faultHandlers;

    public TransitionWrapper(
        String startState, 
        String operation, 
        String targetState, 
        List<String> requires,
        List<String> offers, 
        List<String> faultHandlers
    ){
        this.startState = startState;
        this.operation = operation;
        this.targetState = targetState;
        this.requires = requires;
        this.offers = offers;
        this.faultHandlers = faultHandlers;
    }

    public List<String> getFaultHandlers() {
		return faultHandlers;
	}
	public List<String> getOffers() {
        return offers;
    }
    public List<String> getRequires() {
        return requires;
    }
    public String getTargetState() {
        return targetState;
    }
    public String getOperation() {
        return operation;
    }
    public String getStartState() {
        return startState;
    }
}
