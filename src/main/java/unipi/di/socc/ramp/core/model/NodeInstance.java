package unipi.di.socc.ramp.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class NodeInstance {
    
    private final String nodeTypeName; //for parsing purposes only

    private Node nodeType;
    private String currentState;
    private final String id;

    public NodeInstance(Node nodeType, String currentState, String id) 
        throws
            NullPointerException, 
            IllegalArgumentException
    {

        if(nodeType == null || id == null)
            throw new NullPointerException();
        if(id.isBlank())
            throw new IllegalArgumentException();

        this.nodeType = nodeType;
        this.nodeTypeName = nodeType.getName();
        this.setCurrentState(currentState);
        this.id = id;
    }
    public void setNode(Node n){
        this.nodeType = n;
    }
    
    public void setCurrentState(String state){
        if(state == null)
            throw new NullPointerException();
        if(state.isBlank())
            throw new IllegalArgumentException();
        
        this.currentState = state;
    }
    public Node getNodeType() {
        return nodeType;
    }
    public String getNodeTypeName(){
        return this.nodeTypeName;
    }
    public String getID() {
        return id;
    }
    public String getCurrentState() {
        return currentState;
    }
    public List<Requirement> getNeededReqs(){
        return this.nodeType.getManProtocol().getRho().get(this.currentState);
    }
    public List<String> getOfferedCaps(){
        return this.nodeType.getManProtocol().getGamma().get(this.currentState);
    }


    //TODO: questi due metodi starebbero meglio in ManProtocol? (JS: il secondo si, il primo no)
    //return the list of transition that could be performed in the current state
    public List<Transition> getPossibleTransitions(){
        List<Transition> possibleTransitions = new ArrayList<>();

        Collection<Transition> allTransitions = this.nodeType.getManProtocol().getTransitions().values();
        for(Transition t : allTransitions){
            if(this.currentState.equals(t.getStartState()))
                possibleTransitions.add(t);
        }

        return possibleTransitions;
    }
    
    public Transition getTransitionByOp(String op){
        if(op == null)
            throw new NullPointerException();
        if(op.isBlank())
            throw new IllegalArgumentException();

        Transition t = null;
        for(Transition possibileTransition : this.getPossibleTransitions()){
            if(possibileTransition.getOp().equals(op)){
                t = possibileTransition;
                break;
            }
        }
        
        return t;
    }


    @Override
    public int hashCode(){
        return Objects.hash(this.currentState, this.id, this.nodeType);
    }
    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();

        NodeInstance instance = (NodeInstance) obj;
        return 
            this.currentState.equals(instance.currentState) && 
            this.id.equals(instance.getID()) && 
            this.nodeType.equals(instance.getNodeType());
    }

}
