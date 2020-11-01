package mprot.lib.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//represent the runtime istances of a Node
public class NodeInstance {
    private final Node nodeType;
    private String currentState;
    private final String id;

    /**
     * @param n node of which this is an instance
     * @param cS current state of the new node istance
     * @param id unique identifier of the new node istance
     * @throws NullPointerException
     * @throws InvalidArgumentException
     */
    public NodeInstance(Node node, String currentState, String id)
        throws 
            NullPointerException,
            IllegalArgumentException 
    {
        if(node == null)
            throw new NullPointerException("node null");
        if(currentState == null)
            throw new NullPointerException("currentState null");
        if(id == null)
            throw new NullPointerException("id null");

        if(currentState.isEmpty() == true)
            throw new IllegalArgumentException("currentState empty");
        if(id.isEmpty() == true)
            throw new IllegalArgumentException("id empty");

        this.currentState = currentState;
        //TODO: come arrivo in questo stato? Ci saranno un set di op da fare (chain di transizioni ottimistiche)
        this.id = id;
        this.nodeType = node;
    }
    
    /**
     * @return Node of which node istance is an istance
     */
    public Node getNodeType() {
        return this.nodeType;
    }

    /**
     * @return unique identifier of NodeIstance
     */
    public String getID() {
        return this.id;
    }

    /**
     * @return current state of NodeIstance
     */
    public String getCurrentState() {
        return this.currentState;
    }

    /**
     * @param currentState state to set to NodeIstance
     */
    public void setCurrentState(String currentState) 
        throws 
            NullPointerException, 
            IllegalArgumentException
    {
        if(currentState == null)
            throw new NullPointerException("currentState null");
        if(currentState.isEmpty() == true)
            throw new IllegalArgumentException("currentState empty");

        this.currentState = currentState;
    }

    /**
     * @return list of all the transitions that start in the current state
     */
    public List<Transition> getPossibleTransitions(){
        List<Transition> possibleTransitions = new ArrayList<>();

        //list of all the transitions of the this node instance
        Collection<Transition> transitionsCollection = this.getNodeType().getManagementProtocol().getTransition().values();
        ArrayList<Transition> transitions = new ArrayList<Transition>(transitionsCollection);

        //for each transition we check if it starts in the current state, if so it is a (theorically) possible transition
        for(Transition t : transitions){
            if(t.getStartingState().equals(this.currentState))
                possibleTransitions.add(t);
        }

        return possibleTransitions;
    }

    public Transition getTransitionByOp(String op){

        if(op == null)
            throw new NullPointerException("op null");
        if(op.isEmpty() == true)
            throw new IllegalArgumentException("op empty");
    
        Transition ret = null;
        ArrayList<Transition> possibleTransitions = (ArrayList<Transition>) this.getPossibleTransitions();

        for (Transition transition : possibleTransitions){
            if(transition.getOp().equals(op) == true)
                ret = transition;   
        }
        
        return ret;
    }

     /**
     * @return list of requirements that the node instance is currently asking
     */
    public List<Requirement> getNeededReqs(){
        return this.getNodeType().getManagementProtocol().getRho().get(this.getCurrentState());
    }

    /**
     * @return list of capabilities offred by the node instance
     */
    public List<String> getOfferedCaps(){
        return this.getNodeType().getManagementProtocol().getGamma().get(this.getCurrentState());
    }

    @Override
    /**
     * two fault are equal when they are about the same insance and requirement
     */
    public boolean equals(Object n){
        NodeInstance toCheck = (NodeInstance) n;
        boolean ret = false;

        if(toCheck.getID().equals(this.id))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.id.hashCode();
        result = 31 * result + this.currentState.hashCode();
        result = 31 * result + this.nodeType.getName().hashCode();
        return result;
    }
}