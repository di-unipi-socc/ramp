public class Transition {

    //the name of a transition is need to identify the Transition as a 
    //string, this way it can be passed to mp.rho and be thought as a state
    private final String name;
    private final String startingState;
    private final String op;
    private final String endingState;

    /**
     * @param name of the Transition
     * @param startingState of the Transition
     * @param op management op that "cause" the Transition
     * @param endingState of the Transition
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public Transition(
        String name, 
        String startingState, 
        String op, 
        String endingState
    ){
        assert name.length() > 4;
        assert startingState != null && startingState.length() > 4;
        assert op.length() > 4;
        assert endingState.length() > 4;

        this.name = name;
        this.startingState = startingState;
        this.op = op;
        this.endingState = endingState;
    }

    /**
     * @return name of Transition
     */
    public String getName() {
        return name;
    }

    /**
     * @return management op of Transition
     */
    public String getOp() {
        return op;
    }

    /**
     * @return starting state of Transition
     */
    public String getStartingState() {
        return startingState;
    }

    /**
     * @return ending state of Transition
     */
    public String getEndingState() {
        return endingState;
    }
  
}