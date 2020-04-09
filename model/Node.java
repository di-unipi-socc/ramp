package model;

import java.util.List;

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
    public Node(String name, String initialState, ManagementProtocol manProtocol, List<Requirement> reqs, List<String> caps,
            List<String> states, List<String> ops) {
        assert name.length() > 0;
        assert initialState.length() > 0;
        assert manProtocol != null;
        assert reqs != null;
        assert caps != null;
        assert states != null;
        assert ops != null;

        this.name = name;
        this.caps = caps;
        this.mp = manProtocol;
        this.ops = ops;
        this.reqs = reqs;
        this.states = states;
        this.initialState = initialState;
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
    public ManagementProtocol getMp() {
        return mp;
    }

    /**
     * @return name of Node
     */
    public String getName() {
        return name;
    }
}