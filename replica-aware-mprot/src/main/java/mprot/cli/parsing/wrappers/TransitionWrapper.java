package mprot.cli.parsing.wrappers;

import java.util.List;

public class TransitionWrapper {
    private final String startingState;
    private final String operation;
    private final String targetState;

    // here only the names, the Requirement object will be derivated from the
    // node list of all requirements (like this the user do not have to reinsert the
    // req object in json)
    private final List<String> requires;
    private final List<String> offers; // caps
    private final List<String> faultHandlers;

    public TransitionWrapper(String startingState, String operation, String targetState, List<String> requires,
            List<String> offers, List<String> faultHandlers) {
        this.startingState = startingState;
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

    public String getStartingState() {
        return startingState;
    }
}
