/**
 * this is just a pair (tuple). we do not use Map.Entry for readibility
 * this represent a bond, given the requirement r, if the node istance n 
 * is the one who satisfy r offering the right capability, we have a bond, 
 * such as <r, n>
 */
public class Bond {
    private final NodeIstance ist;
    private final Requirement req;

    /**
     * @param req requirement needed by some node istance 
     * @param ist node istance that take care of req
     * @throws NullPointerException
     */
    public Bond(Requirement req, NodeIstance ist) {
        assert ist != null;
        assert req != null;

        this.ist = ist;
        this.req = req;
    }

    /**
     * @return node istance that take care of the requirement
     */
    public NodeIstance getIst() {
        return ist;
    }

    /**
     * @return requirement needed by some node istance
     */
    public Requirement getReq() {
        return req;
    }
}