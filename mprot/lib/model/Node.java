package mprot.lib.model;

import java.util.List;
import java.util.ArrayList;
//represents a component of the application
public class Node {

    private final String name;
    private final ManagementProtocol mp;

    private final String initialState;
    // all the requirements asked by the node, no matter in what state it is
    private final List<Requirement> reqs;
    // all the capabilities offered by the node, no matter in what state it is
    private final List<String> caps;
    // all the states of the Node
    private final List<String> states;
    // all the management operations executable on the node
    private final List<String> ops;

    /**
     * @param n name of the Node
     * @param m protocol of the Node
     * @param r list of all requirement of the Node
     * @param c list of all the capabilities of the Node
     * @param s list of all the states of the Node
     * @param o list of all the possible management operations
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public Node(
            String name, 
            String initialState, 
            ManagementProtocol manProtocol, 
            List<Requirement> reqs, 
            List<String> caps,
            List<String> states, 
            List<String> ops)
        throws
            NullPointerException, 
            IllegalArgumentException
    {
        
        if(name == null)
            throw new NullPointerException("name null");
        if(initialState == null)
            throw new NullPointerException("initialState null");
        if(reqs == null)
            throw new NullPointerException("reqs null");
        if(caps == null)
            throw new NullPointerException("caps null");
        if(states == null)
            throw new NullPointerException("states null");
        if(ops == null)
            throw new NullPointerException("ops null");
        
        if(name.isEmpty() == true)
            throw new IllegalArgumentException("name empty");
        if(initialState.isEmpty() == true)
            throw new IllegalArgumentException("initialState empty");

        this.name = name;
        this.caps = caps;
        this.mp = manProtocol;
        this.ops = ops;
        this.reqs = reqs;
        this.states = states;
        this.initialState = initialState;
    }

    public Node(String name, String initialState, ManagementProtocol mp)
        throws 
            NullPointerException, 
            IllegalArgumentException
    {
        if(name == null)
            throw new NullPointerException("name null");
        if(initialState == null)
            throw new NullPointerException("initialState null");

        if(name.isEmpty() == true)
            throw new IllegalArgumentException("name empty");
        if(initialState.isEmpty() == true)
            throw new IllegalArgumentException("initialState empty");
        
        this.name = name;
        this.initialState = initialState;
        this.mp = mp;
        this.caps = new ArrayList<>();
        this.reqs = new ArrayList<>();
        this.ops = new ArrayList<>();
        this.states = new ArrayList<>();
    }

    /**
     * @return the initial state of the Node
     */
    public String getInitialState() {
        return initialState;
    }

    /**
     * @return list of all the possible management ops on Node
     */
    public List<String> getOps() {
        return ops;
    }

    /**
     * @return list of all the states of Node
     */
    public List<String> getStates() {
        return states;
    }

    /**
     * @return list of all the capabilities offered by Node
     */
    public List<String> getCaps() {
        return caps;
    }

    /**
     * @return list of all the requirement needed by Node
     */
    public List<Requirement> getReqs() {
        return reqs;
    }

    /**
     * @return management protocol of Node
     */
    public ManagementProtocol getManagementProtocol() {
        return mp;
    }

    /**
     * @return name of Node
     */
    public String getName() {
        return name;
    }

    public void addState(String state) 
        throws
            NullPointerException, 
            IllegalArgumentException
    {
        if(state == null)
            throw new NullPointerException("state null");
        if(state.isEmpty() == true)
            throw new IllegalArgumentException("state empty");

        this.states.add(state);
    }

    public void addOperation(String op)
        throws
            NullPointerException, 
            IllegalArgumentException
    {
        if(op == null)
            throw new NullPointerException("op null");
        if(op.isEmpty() == true)
            throw new IllegalArgumentException("op empty");

        this.ops.add(op);
    }

    public void addRequirement(Requirement req) throws NullPointerException{
        if(req == null)
            throw new NullPointerException("req null");
            
        this.reqs.add(req);
    }

    public void addCapability(String cap){
        if(cap == null)
            throw new NullPointerException("cap null");
        if(cap.isEmpty() == true)
            throw new IllegalArgumentException("cap empty");

        this.caps.add(cap);
    }
}