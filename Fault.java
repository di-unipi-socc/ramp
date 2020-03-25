import java.util.List;

/**
 * this is just a pair (tuple). we do not use Map.Entry for readibility this
 * represent a fault, given the node istance n and the requirement r of n, if n
 * find r is not respected anymore we have a fault, such as <n, r>
 */
public class Fault {
    private final NodeIstance ist;
    private final List<Requirement> req;

    /**
     * @param ist node istance that required req
     * @param req requirement of ist that is not respected
     * @throws NullPointerException
     */
    public Fault(NodeIstance ist, List<Requirement> req) {
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