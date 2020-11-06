package mprot.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//represents the protocol of a Node
public class ManagementProtocol {

    //private final String initialState;
    // transition name -> Transition
    private final Map<String, Transition> transition;
    /**
     * rho: state -> reqs that must hold in that state. 
     * mind that "state" can be a node state but can 
     * even represent a Transition
     */
    private final Map<String, List<Requirement>> rho;
    
    // gamma: state -> caps offred in that state
    private final Map<String, List<String>> gamma;

    // phi: state -> list of states for fault
    private final Map<String, List<String>> phi;
    
    /**
     * @param transition transition model: all the possible transitions
     * @param rho map the state with the needed requirements
     * @param gamma map the state with the offered capabilities
     * @param phi fault handling
     * @throws NullPonterException
     */
    public ManagementProtocol(
        Map<String, Transition> transition, 
        Map<String, List<Requirement>> rho,
        Map<String, List<String>> gamma, 
        Map<String, List<String>> phi) 
        throws NullPointerException
    {
        if(transition == null)
            throw new NullPointerException("transition null");
        if(rho == null)
            throw new NullPointerException("rho null");
        if(gamma == null)
            throw new NullPointerException("gamma null");
        if(phi == null)
            throw new NullPointerException("phi null");

        this.transition = transition;
        this.gamma = gamma;
        this.rho = rho;
        this.phi = phi;
    }

    public ManagementProtocol(){
        this.transition = new HashMap<String, Transition>();
        this.phi = new HashMap<String, List<String>>();
        this.gamma = new HashMap<String, List<String>>();
        this.rho = new HashMap<String, List<Requirement>>();
    }

    /**
     * @return map a state (which can be a transition) to a list of state for the fault handling
     */
    public Map<String, List<String>> getPhi() {
        return this.phi;
    }

    /**
     * @return map such as: state -> offred caps in that state
     */
    public Map<String, List<String>> getGamma() {
        return this.gamma;
    }

    /**
     * @return map such as: state -> needed reqs in that state
     */
    public Map<String, List<Requirement>> getRho() {
        return this.rho;
    }

    /**
     * @return map such as: transition's name -> Transition <s, op, s1> 
     */
    public Map<String, Transition> getTransition() {
        return this.transition;
    }

    public void addRhoEntry(String stateOrTransition, List<Requirement> reqs)
        throws 
            NullPointerException, 
            IllegalArgumentException
    {
        if(reqs == null)
            throw new NullPointerException("reqs null");
        if(stateOrTransition == null)
            throw new NullPointerException("stateOrTransition null");
        if(stateOrTransition.isEmpty() == true)
            throw new IllegalArgumentException("stateOrTransition empty");

        this.rho.put(stateOrTransition, reqs);
    }

    public void addGammaEntry(String stateOrTransition, List<String> caps)
        throws 
            NullPointerException, 
            IllegalArgumentException
    {
        if(stateOrTransition == null)
            throw new NullPointerException("stateOrTransition null");
        if(stateOrTransition.isEmpty() == true)
            throw new IllegalArgumentException("stateOrTransition empty");
        if(caps == null)
            throw new NullPointerException("caps null");

        this.gamma.put(stateOrTransition, caps);
    }

    public void addPhiEntry(String stateOrTransition, List<String> faultHandlingStates)
        throws 
            NullPointerException, 
            IllegalArgumentException
    {
        if(stateOrTransition == null)
            throw new NullPointerException("stateOrTransition null");
        if(stateOrTransition.isEmpty() == true)
            throw new IllegalArgumentException("stateOrTransition empty");
        if(faultHandlingStates == null)
            throw new NullPointerException("faultHandlingStates");

        this.phi.put(stateOrTransition, faultHandlingStates);
    }


    //TODO would be nice if this somehow add the transitional state to the states of the node
    public void addTransition(String source, String op, String target){
        if(source == null)
            throw new NullPointerException("source null");
        if(op == null)
            throw new NullPointerException("op null");
        if(target == null)
            throw new NullPointerException("target null");

        if(source.isEmpty() == true)
            throw new IllegalArgumentException("source empty");
        if(op.isEmpty() == true)
            throw new IllegalArgumentException("op empty");
        if(target.isEmpty() == true)
            throw new IllegalArgumentException("target empty");
        
        this.transition.put(source+op+target, new Transition(source+op+target, source, op, target));
    }

    @Override
    /**
     */
    public boolean equals(Object f){
        ManagementProtocol toCheck = (ManagementProtocol) f;
        boolean ret = true;

        if(this.transition.equals(toCheck.getTransition()) == false)
            ret = false;

        if(this.gamma.equals(toCheck.getGamma()) == false)
            ret = false;
        
        if(this.phi.equals(toCheck.getPhi()) == false)
            ret = false;
        
        if(this.rho.equals(toCheck.getRho()) == false)
            ret = false;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.transition.hashCode();
        result = 31 * result + this.gamma.hashCode();
        result = 31 * result + this.phi.hashCode();
        result = 31 * result + this.rho.hashCode();
        return result;
    }

}