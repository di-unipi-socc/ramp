import java.util.List;

/**
 * this is just a pair (tuple). we do not use Map.Entry for readibility.
 * this represent a PendingFault. given a node istance ist, a PendingFault is a pair such as
 * <ist, list of requirement no longer met and there is no node istance that can take care of it>
 * if there is just one requirement no longer met this is a simple tuple 
 */
public class PendingFault {
    private final NodeIstance ist;
    private final List<Requirement> req;

    /**
     * @param ist node istance that required req
     * @param req requirements of ist that are not respected
     * @throws NullPointerException
     */
    public PendingFault(NodeIstance ist, List<Requirement> req) {
        assert ist != null;
        assert req != null;
        this.ist = ist;
        this.req = req;
    }

    /**
     * @return node istance with the failed requirement
     */
    public NodeIstance getIst() {
        return this.ist;
    }

    /**
     * @return failed requirements of the node istance
     */
    public List<Requirement> getReq() {
        return this.req;
    }
}