public class Transition {

    //the name of a transition is need to identify the transition as a 
    //string, this way it can be passed to mp.rho and be thought as a state
    private final String name;
    private final String startingState;
    private final String op;
    private final String endingState;

    /**
     * @param name of the transition
     * @param startingState of the transition
     * @param op management op that "cause" the transition
     * @param endingState of the transition
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public Transition(
        String name, 
        String startingState, 
        String op, 
        String endingState
    ){
        assert name.length() > 0;
        assert startingState != null && startingState.length() > 0;
        assert op.length() > 0;
        assert endingState.length() > 0;

        this.name = name;
        this.startingState = startingState;
        this.op = op;
        this.endingState = endingState;
    }

    /**
     * @return name of transition
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return management op of transition
     */
    public String getOp() {
        return this.op;
    }

    /**
     * @return starting state of transition
     */
    public String getStartingState() {
        return this.startingState;
    }

    /**
     * @return ending state of transition
     */
    public String getEndingState() {
        return this.endingState;
    }
  
}