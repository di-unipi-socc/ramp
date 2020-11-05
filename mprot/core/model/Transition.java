package mprot.core.model;

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
    public Transition(String name, String startingState, String op, String endingState) 
        throws
            NullPointerException, 
            IllegalArgumentException
    {

        if(name == null)
            throw new NullPointerException("name null");
        if(startingState == null)
            throw new NullPointerException("startingState null");
        if(op == null)
            throw new NullPointerException("op null");
        if(endingState == null)
            throw new NullPointerException("endingState null");
        
        if(name.isEmpty() == true)
            throw new IllegalArgumentException("name empty");
        if(startingState.isEmpty() == true)
            throw new IllegalArgumentException("startingState empty");
        if(op.isEmpty() == true)
            throw new IllegalArgumentException("op empty");
        if(endingState.isEmpty() == true)
            throw new IllegalArgumentException("endingState empty");

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

    @Override
    /**
     */
    public boolean equals(Object f){
        Transition toCheck = (Transition) f;
        boolean ret = false;

        if( toCheck.getName().equals(this.name) && 
            toCheck.getEndingState().equals(this.endingState) && 
            toCheck.getOp().equals(this.op) && 
            toCheck.getStartingState().equals(this.startingState)
        )
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.name.hashCode();
        result = 31 * result + this.startingState.hashCode();
        result = 31 * result + this.op.hashCode();
        result = 31 * result + this.endingState.hashCode();

        return result;
    }
  
}