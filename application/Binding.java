package application;

/**
 * this is just a pair (tuple). we do not use Map.Entry for readibility
 * this represent a binding, given the requirement r, if the node istance n 
 * is the one who satisfy r offering the right capability, we have a bond, 
 * such as <r, unique id of n> in the global state (binding)
 */
public class Binding {
    private final String ist;
    private final Requirement req;

    /**
     * @param req requirement needed by some node istance 
     * @param ist node istance that take care of req
     * @throws NullPointerException
     */
    public Binding(Requirement req, String ist) {
        assert ist != null;
        assert req != null;

        this.ist = ist;
        this.req = req;
    }

    /**
     * @return node istance that take care of the requirement
     */
    public String getIst() {
        return ist;
    }

    /**
     * @return requirement needed by some node istance
     */
    public Requirement getReq() {
        return req;
    }
}