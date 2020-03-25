//represent the runtime istances of a Node
public class NodeIstance {
    private final Node myNode;
    private String currenState;
    private final String id;

    /**
     * @param n Node of which NodeIstance is an instance
     * @param cS current state of the NodeIstance
     * @param id unique identifier of NodeIstance
     * @throws NullPointerException
     * @throws InvalidArgumentException
     */
    public NodeIstance(Node n, String cS, String id) {
        assert n != null;
        assert cS.length() > 4;
        assert id.length() > 4;

        this.currenState = cS;
        //TODO: come arrivo in questo stato? Ci saranno un set di op da fare
        this.id = id;
        this.myNode = n;
    }
    
    /**
     * @return Node of which NodeIstance is an istance
     */
    public Node getMyNode() {
        return myNode;
    }

    /**
     * @return unique identifier of NodeIstance
     */
    public String getId() {
        return id;
    }

    /**
     * @return current state of NodeIstance
     */
    public String getCurrenState() {
        return currenState;
    }

    /**
     * @param currenState state to set to NodeIstance
     */
    public void setCurrenState(String currenState) {
        this.currenState = currenState;
    }

}