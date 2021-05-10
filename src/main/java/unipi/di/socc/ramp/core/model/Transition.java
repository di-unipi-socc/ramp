package unipi.di.socc.ramp.core.model;

public class Transition {
    
    /**
     * model a transition of an instance from a state to another by the exectuion of an
     * operation that that instance offer
    */

    private final String name; 
    private final String startState;
    private final String op; 
    private final String endState;

    public Transition(String startState, String op, String endState)
        throws
            NullPointerException, 
            IllegalArgumentException
    {
        if(startState == null || op == null || endState == null)
            throw new NullPointerException();
        if(startState.isBlank() || op.isBlank() || endState.isBlank())
            throw new IllegalArgumentException();

        //needed for hash tables
        this.name = startState + op + endState;
        this.startState = startState;
        this.op = op;
        this.endState = endState;
    }

    public String getName() {
        return name;
    }
    public String getEndState() {
        return endState;
    }
    public String getOp() {
        return op;
    }
    public String getStartState() {
        return startState;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();
            
        Transition t = (Transition) obj;
        return this.name.equals(t.getName());
    }
    @Override
    public int hashCode(){
        return this.name.hashCode();
    }

}
