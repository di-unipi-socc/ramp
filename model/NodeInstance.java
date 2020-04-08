package model;

import java.util.ArrayList;
import java.util.List;

//represent the runtime istances of a Node
public class NodeInstance {
    private final Node nodeType;
    private String currenState;
    private final String id;

    /**
     * @param n node of which this is an instance
     * @param cS current state of the new node istance
     * @param id unique identifier of the new node istance
     * @throws NullPointerException
     * @throws InvalidArgumentException
     */
    public NodeInstance(Node n, String cS, String id) {
        assert n != null;
        assert cS.length() > 0;
        assert id.length() > 0;

        this.currenState = cS;
        //TODO: come arrivo in questo stato? Ci saranno un set di op da fare (chain di transizioni ottimistiche)
        this.id = id;
        this.nodeType = n;
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
    public String getId() {
        return this.id;
    }

    /**
     * @return current state of NodeIstance
     */
    public String getCurrenState() {
        return this.currenState;
    }

    /**
     * @param currenState state to set to NodeIstance
     */
    public void setCurrenState(String currenState) {
        this.currenState = currenState;
    }

    /**
     * @return list of all the transitions that start in the current state
     */
    public List<Transition> getPossibleTransitions(){
        List<Transition> possibleTransitions = new ArrayList<>();

        //list of all the transitions of the this node instance
        ArrayList<Transition> transitions = (ArrayList<Transition>) this.getNodeType().getMp().getTransition().values();

        //for each transistion we check if it starts in the current state, if so it is a 
        //(theorically) possible transition
        for(Transition t : transitions){
            if(t.getStartingState().equals(this.currenState))
                possibleTransitions.add(t);
        }
        return possibleTransitions;
    }

    public Transition getTransitionByOp(String op){
        Transition ret = null;
        ArrayList<Transition> possibleTransitions = (ArrayList<Transition>) this.getPossibleTransitions();
        for (Transition t : possibleTransitions){
            if(t.getOp().equals(op) == true)
               ret = t;   
        }
        return ret;
    }

     /**
     * @return list of requirements that the node instance is currently asking
     */
    public List<Requirement> getNeededReqs(){
        return this.getNodeType().getMp().getRho().get(this.getCurrenState());
    }

    /**
     * @return list of capabilities offred by the node instance
     */
    public List<String> getOfferedCaps(){
        return this.getNodeType().getMp().getGamma().get(this.getCurrenState());
    }
}