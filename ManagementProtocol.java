import java.util.List;
import java.util.Map;

//represents the protocol of a Node
public class ManagementProtocol {

    private final String initialState;
    // transition name -> Transition
    private final Map<String, Transition> transition;
    /**
     * rho: state -> reqs that must hold in that state. 
     * mind that "state" can be a node state but can 
     * even represent a Transition
     */
    private final Map<String, List<Requirement>> rho;
    // gamma: state -> cpas offred in that state
    private final Map<String, List<String>> gamma;

    // phi: state -> list of states for fault
    private final Map<String, List<String>> phi;
    
    /**
     * @param initialState initial state of the node
     * @param transition transition model: all the possible transitions
     * @param rho map the state with the needed requirements
     * @param gamma map the state with the offered capabilities
     * @param phi fault handling
     * @throws NullPonterException
     * @throws InvalidArgumentException
     */
    public ManagementProtocol(
        String initialState, 
        Map<String, Transition> transition, 
        Map<String, List<Requirement>> rho,
        Map<String, List<String>> gamma, 
        Map<String, List<String>> phi
    ){
        assert initialState.length() > 0;
        assert transition != null;
        assert rho != null;
        assert gamma != null;
        assert phi != null;

        this.transition = transition;
        this.initialState = initialState;
        this.gamma = gamma;
        this.rho = rho;
        this.phi = phi;
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

    /**
     * @return first state of the node
     */
    public String getInitialState() {
        return this.initialState;
    }
}