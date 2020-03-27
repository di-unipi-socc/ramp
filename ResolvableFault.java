import java.util.List;

/**
 * this is just a couple (tuple). We do not use Map.Entry for readibility. 
 * this represent a resolvable fault. 
 * given a not met requirement r of some node istance a resolvable fault is a couple such as:
 * <requirement r, list of unique ids of those node istances that can take care of r>
 */
public class ResolvableFault {

    private final Requirement req;
    private final List<String> capableNodeIstances;

    public ResolvableFault(Requirement req, List<String> capNodeIstances) {
        assert req != null;
        assert capNodeIstances != null;

        this.req = req;
        this.capableNodeIstances = capNodeIstances;
    }

    public List<String> getCapableNodeIstances() {
        return this.capableNodeIstances;
    }

    public Requirement getReq() {
        return this.req;
    }

    
}