package unipi.di.socc.ramp.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManagementProtocol {
    
    //transition name -> Transition
    private final Map<String, Transition> transitions;

    private final List<String> states;
    private final String initialState; 

    //state s -> list of requirement needed in s 
    private final Map<String, List<Requirement>> rho;
    //state s -> list of caps offered in s
    private final Map<String, List<String>> gamma;
    //state s -> reachable fault states from s 
    private final Map<String, List<String>> phi;

    /**
     * mind that transitions are also states (and in fact can be seen as strings)
        * thus a transitions can need requirements and induce capabilities 
     */

    public ManagementProtocol(String initialState)
        throws 
            NullPointerException, 
            IllegalArgumentException
    {
        if(initialState == null)
            throw new NullPointerException();
        if(initialState.isBlank())
            throw new IllegalArgumentException();

        this.transitions = new HashMap<String, Transition>();
        this.gamma = new HashMap<String, List<String>>();
        this.phi = new HashMap<String, List<String>>();
        this.rho = new HashMap<String, List<Requirement>>();
        this.states = new ArrayList<String>();
        this.initialState = initialState;
        this.addState(this.initialState);
    }

    public void addState(String state)
        throws 
            NullPointerException, 
            IllegalArgumentException
    {

        if(state == null)
            throw new NullPointerException();
        if(state.isBlank())
            throw new IllegalArgumentException();
        
        if(!this.states.contains(state)){
            this.states.add(state);
            this.rho.put(state, new ArrayList<>());
            this.gamma.put(state, new ArrayList<>());
            this.phi.put(state, new ArrayList<>());
        }
        //TODO else something??
    }

    public void addTransition(String startState, String op, String endState){
        Transition t = new Transition(startState, op, endState);
        this.transitions.put(t.getName(), t);
        this.addState(t.getName());
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();
        
        ManagementProtocol mp = (ManagementProtocol) obj;
        return 
            this.gamma.equals(mp.getGamma()) && 
            this.rho.equals(mp.getRho()) && 
            this.phi.equals(mp.getPhi()) &&
            this.transitions.equals(mp.getTransitions()) &&
            this.states.equals(mp.states) &&
            this.initialState.equals(mp.initialState)
        ;
        
    }
    @Override
    public int hashCode(){
        return Objects.hash(this.transitions, this.rho, this.gamma, this.phi);
    }

    public Map<String, Transition> getTransitions() {
        return this.transitions;
    }
    public Map<String, List<String>> getPhi() {
        return this.phi;
    }
    public Map<String, List<String>> getGamma() {
        return this.gamma;
    }
    public Map<String, List<Requirement>> getRho() {
        return this.rho;
    }
    public List<String> getStates(){
        return this.states;
    }
    public String getInitialState(){
        return this.initialState;
    }
}
