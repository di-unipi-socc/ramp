//represent the runtime istances of a Node
public class NodeInstance {
    private final Node nodeType;
    private String currenState;
    private final String id;

    /**
     * @param n node of which this is an instance
     * @param cS current state of the new node istance
     * @param id unique identifier of the new node istance
     * @throws NullPointerException
     * @throws InvalidArgumentException
     */
    public NodeInstance(Node n, String cS, String id) {
        assert n != null;
        assert cS.length() > 4;
        assert id.length() > 4;

        this.currenState = cS;
        //TODO: come arrivo in questo stato? Ci saranno un set di op da fare
        this.id = id;
        this.nodeType = n;
    }
    
    /**
     * @return Node of which node istance is an istance
     */
    public Node getNodeType() {
        return this.nodeType;
    }

    /**
     * @return unique identifier of NodeIstance
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return current state of NodeIstance
     */
    public String getCurrenState() {
        return this.currenState;
    }

    /**
     * @param currenState state to set to NodeIstance
     */
    public void setCurrenState(String currenState) {
        this.currenState = currenState;
    }

}