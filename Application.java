import java.util.ArrayList;
import java.util.List;

import exceptions.InstanceNotAvailableException;
import exceptions.OperationNotAvailableException;

//represents the whole application
public class Application {

    //name of the application
    private final String name;
    //set T: list of application's component
    private List<Node> nodes;
    private GlobalState gState;
    // pi
    // b

    /**
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @param name application's name
     */
    public Application(String name) {
        //this assert is just to check that name is not null nor empty
        //used many more times in this project, it is just a reminder for 
        //a real exception handling
        assert name.length() > 0;
        
        this.name = name;
        this.nodes = new ArrayList<Node>();
        this.gState = new GlobalState(this);
    }

     /**
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @param name application's name
     * @param nodes list of applicaton's Node
     */
    public Application(String name, List<Node> nodes){
        assert name.length() > 0;
        this.name = name; 
        this.setNodes(nodes);
        this.gState = new GlobalState(this);
    }

    /**
     * @return current GlobalState
     */
    public GlobalState getGState() {
        return gState;
    }

    /**
     * @param gs given GlobalState 
     */
    public void setGState(GlobalState gs) {
        this.gState = gs;
    }

    /**
     * @return list of the application's Node
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes list of Node to bet set to the applicaton
     * @throws NullPointerException
     */
    public void setNodes(List<Node> nodes) {
        assert nodes != null;
        this.nodes = nodes;
    }

    /**
     * @return name of the applicaton
     */
    public String getName() {
        return name;
    }

    /**
     * @param r requirement that needs to be handled
     * @return the first node instance that can take care of r
     */
    public NodeInstance defaultPi(Requirement r){
        NodeInstance ret = null;
        ArrayList<NodeInstance> activeNodes = (ArrayList<NodeInstance>) this.gState.activeNodes.values();
        
        //the list of node instances that can offer the right capability for r
        ArrayList<NodeInstance> capableInstances = new ArrayList<>();
        //list of all binding of the application
        ArrayList<Binding> allBindings = new ArrayList<>();
        
        for(NodeInstance n : activeNodes){
            //we add to allBindings all the binding of n, for each n
            allBindings.addAll(this.gState.binding.get(n.getId()));
            if(this.getGState().getOfferedCaps(n).contains(r.getName()) == true){
                //n is offering the right capability
                capableInstances.add(n);
            }
        }

        //for each instance that can offer the right cap for r, we must check if the
        //capability it is already in use by some other instance
        for(NodeInstance n : capableInstances) {
            Binding tmp = new Binding(r, n.getId());
            if(allBindings.contains(tmp) == false){
                //this means that n is offering the right capabiltiy and
                //there is not another node using it (there is not a binding with n)
                ret = n;
                break;
            }
        }
        return ret;
    }

    /**
     * @param n  node instance on which it's required to do the managment operation
     *           op
     * @param op management operation to execute
     * @throws NullPointerException
     * @throws OperationNotAvailableException
     * @throws InstanceNotAvailableException
     */
    public void opStart(NodeInstance n, String op)
        throws OperationNotAvailableException, 
        InstanceNotAvailableException 
    {
        assert n != null;
        assert op.length() != 0;

        Transition transitionToHappen = null;
        ArrayList<Transition> possibleTransitions = (ArrayList<Transition>) n.getPossibleTransitions();
        for (Transition t : possibleTransitions){
            if(t.getOp().equals(op) == true && t.getEndingState().equals("damaged") == false)
               transitionToHappen = t;   
        }
        //among the possible transitions there is not a transition with this op, 
        //hence the op is not available
        if(transitionToHappen == null)
            throw new OperationNotAvailableException();
        
        //n goes in a new transient state
        n.setCurrenState(transitionToHappen.getName());
        //we kill old bindings (the ones that were about the old state)
        this.gState.removeOldBindings(n);
        //we add the new bindings (the ones that are about the new transient state)
        this.gState.psiMethod(n);
        
    }

    public void opEnd(NodeInstance n, String op){

    }

}